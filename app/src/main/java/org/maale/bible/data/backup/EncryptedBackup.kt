package org.maale.bible.data.backup

import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Versioned, password-encrypted backup container.
 *
 * The caller supplies only user-owned records and attachments. Licensed Bible text,
 * source archives and reference payloads must never be passed to this API.
 */
object EncryptedBackup {
    private val magic = byteArrayOf(0x4d, 0x42, 0x4b, 0x50) // MBKP
    private const val FORMAT_VERSION = 1
    private const val KDF_ITERATIONS = 310_000
    private const val MIN_KDF_ITERATIONS = 100_000
    private const val MAX_KDF_ITERATIONS = 1_000_000
    private const val SALT_BYTES = 16
    private const val NONCE_BYTES = 12
    private const val TAG_BITS = 128
    private const val MAX_CONTAINER_BYTES = 64 * 1024 * 1024
    private const val MAX_TOTAL_PLAINTEXT = 128 * 1024 * 1024
    private const val MAX_ENTRY_BYTES = 32 * 1024 * 1024
    private const val MAX_ENTRIES = 128
    private const val MANIFEST = "manifest.json"

    fun create(entries: Map<String, ByteArray>, password: CharArray): ByteArray {
        require(password.size >= 12) { "Backup password must contain at least 12 characters" }
        require(entries.size <= MAX_ENTRIES) { "Too many backup entries" }
        require(MANIFEST !in entries) { "Reserved manifest path" }
        var total = 0L
        entries.forEach { (name, bytes) ->
            validatePath(name)
            require(bytes.size <= MAX_ENTRY_BYTES) { "Backup entry too large: $name" }
            total += bytes.size
            require(total <= MAX_TOTAL_PLAINTEXT) { "Backup exceeds total size limit" }
        }

        val manifest = JSONObject().put("formatVersion", FORMAT_VERSION)
        val fileList = JSONArray()
        entries.toSortedMap().forEach { (name, bytes) ->
            fileList.put(JSONObject()
                .put("path", name)
                .put("size", bytes.size)
                .put("sha256", sha256(bytes)))
        }
        manifest.put("entries", fileList)

        val zipBytes = ByteArrayOutputStream().use { output ->
            ZipOutputStream(output).use { zip ->
                zip.putNextEntry(ZipEntry(MANIFEST))
                zip.write(manifest.toString().toByteArray(Charsets.UTF_8))
                zip.closeEntry()
                entries.toSortedMap().forEach { (name, bytes) ->
                    zip.putNextEntry(ZipEntry(name))
                    zip.write(bytes)
                    zip.closeEntry()
                }
            }
            output.toByteArray()
        }
        require(zipBytes.size <= MAX_TOTAL_PLAINTEXT) { "Backup archive exceeds size limit" }

        val salt = ByteArray(SALT_BYTES).also(SecureRandom()::nextBytes)
        val nonce = ByteArray(NONCE_BYTES).also(SecureRandom()::nextBytes)
        val header = ByteBuffer.allocate(4 + 1 + 4 + SALT_BYTES + NONCE_BYTES)
            .put(magic).put(FORMAT_VERSION.toByte()).putInt(KDF_ITERATIONS)
            .put(salt).put(nonce).array()
        val keyBytes = deriveKey(password, salt, KDF_ITERATIONS)
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(keyBytes, "AES"), GCMParameterSpec(TAG_BITS, nonce))
            keyBytes.fill(0)
            cipher.updateAAD(header)
            val encrypted = cipher.doFinal(zipBytes)
            (header + encrypted).also {
                require(it.size <= MAX_CONTAINER_BYTES) { "Encrypted backup exceeds container limit" }
            }
        } finally {
            keyBytes.fill(0)
        }
    }

    fun restore(container: ByteArray, password: CharArray): Map<String, ByteArray> {
        require(container.size in (4 + 1 + 4 + SALT_BYTES + NONCE_BYTES + 16)..MAX_CONTAINER_BYTES) {
            "Invalid backup container size"
        }
        val headerSize = 4 + 1 + 4 + SALT_BYTES + NONCE_BYTES
        val header = container.copyOfRange(0, headerSize)
        val input = ByteBuffer.wrap(header)
        val foundMagic = ByteArray(4).also(input::get)
        require(foundMagic.contentEquals(magic)) { "Not a Maale Bible backup" }
        val version = input.get().toInt() and 0xff
        require(version == FORMAT_VERSION) { "Unsupported backup format version: $version" }
        val iterations = input.int
        require(iterations in MIN_KDF_ITERATIONS..MAX_KDF_ITERATIONS) { "Unsafe KDF parameters" }
        val salt = ByteArray(SALT_BYTES).also(input::get)
        val nonce = ByteArray(NONCE_BYTES).also(input::get)
        val keyBytes = deriveKey(password, salt, iterations)
        val plaintext = try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(keyBytes, "AES"), GCMParameterSpec(TAG_BITS, nonce))
            keyBytes.fill(0)
            cipher.updateAAD(header)
            cipher.doFinal(container.copyOfRange(headerSize, container.size))
        } catch (_: AEADBadTagException) {
            throw SecurityException("Backup authentication failed: wrong password or modified archive")
        } finally {
            keyBytes.fill(0)
        }
        require(plaintext.size <= MAX_TOTAL_PLAINTEXT) { "Backup plaintext exceeds size limit" }
        return readAndVerifyZip(plaintext)
    }

    private fun readAndVerifyZip(bytes: ByteArray): Map<String, ByteArray> {
        val files = linkedMapOf<String, ByteArray>()
        var manifest: JSONObject? = null
        var total = 0L
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                require(!entry.isDirectory) { "Directory entries are not permitted" }
                validatePath(entry.name)
                require(entry.name !in files && (entry.name != MANIFEST || manifest == null)) {
                    "Duplicate backup path: ${entry.name}"
                }
                val out = ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                while (true) {
                    val count = zip.read(buffer)
                    if (count < 0) break
                    total += count
                    require(total <= MAX_TOTAL_PLAINTEXT && out.size() + count <= MAX_ENTRY_BYTES) {
                        "Backup decompression limit exceeded"
                    }
                    out.write(buffer, 0, count)
                }
                val data = out.toByteArray()
                if (entry.name == MANIFEST) {
                    manifest = JSONObject(data.toString(Charsets.UTF_8))
                } else {
                    files[entry.name] = data
                    require(files.size <= MAX_ENTRIES) { "Too many backup entries" }
                }
                zip.closeEntry()
            }
        }
        val metadata = requireNotNull(manifest) { "Backup manifest missing" }
        require(metadata.getInt("formatVersion") == FORMAT_VERSION) { "Unsupported manifest version" }
        val declared = metadata.getJSONArray("entries")
        require(declared.length() == files.size) { "Manifest entry count mismatch" }
        val verified = linkedMapOf<String, ByteArray>()
        for (i in 0 until declared.length()) {
            val item = declared.getJSONObject(i)
            val path = item.getString("path")
            validatePath(path)
            val data = requireNotNull(files[path]) { "Manifest entry missing: $path" }
            require(item.getLong("size") == data.size.toLong()) { "Entry size mismatch: $path" }
            require(MessageDigest.isEqual(
                item.getString("sha256").toByteArray(Charsets.US_ASCII),
                sha256(data).toByteArray(Charsets.US_ASCII)
            )) { "Entry integrity check failed: $path" }
            verified[path] = data
        }
        return verified
    }

    private fun validatePath(path: String) {
        require(path.isNotBlank() && !path.startsWith("/") && !path.startsWith("\\")) {
            "Unsafe backup path"
        }
        val normalized = path.replace('\\', '/')
        require(normalized == path && normalized.split('/').all { it.isNotBlank() && it != "." && it != ".." }) {
            "Unsafe backup path: $path"
        }
        require(!normalized.contains(':') && normalized.none { it.code < 0x20 }) { "Unsafe backup path: $path" }
    }

    private fun deriveKey(password: CharArray, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password, salt, iterations, 256)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
}

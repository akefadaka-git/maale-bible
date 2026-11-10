package org.maale.bible.data.content

import android.content.Context
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.Properties

data class OfflineContentDescriptor(
    val editionId: String,
    val sourceArchiveSha256: String,
    val databaseSha256: String,
    val databaseBytes: Long,
    val schemaVersion: String,
    val parserVersion: String,
    val bookCount: Int,
    val chapterCount: Int,
    val verseCount: Int,
    val ftsCount: Int,
    val ftsTokenizer: String,
    val tokenizerQualification: String,
)

data class ScriptureSearchResult(
    val bookId: String,
    val chapter: Int,
    val verseRef: String,
    val text: String,
)

class OfflineScriptureStore private constructor(
    private val connection: SQLiteConnection,
    val descriptor: OfflineContentDescriptor,
) : AutoCloseable {
    override fun close() = connection.close()

    fun search(query: String, limit: Int = 30): List<ScriptureSearchResult> {
        require(limit in 1..100)
        val terms = query.trim().split(Regex("\\s+")).filter(String::isNotBlank)
        if (terms.isEmpty()) return emptyList()
        val matchExpression = terms.joinToString(" AND ") { term ->
            "\"" + term.replace("\"", "\"\"") + "\""
        }
        val results = mutableListOf<ScriptureSearchResult>()
        connection.prepare(
            """
            SELECT v.book_id, v.chapter, v.verse_ref, v.display_text
            FROM verse_fts
            JOIN verses v ON v.id = verse_fts.rowid
            WHERE verse_fts MATCH ?
            ORDER BY rank
            LIMIT ?
            """.trimIndent()
        ).use { statement ->
            statement.bindText(1, matchExpression)
            statement.bindLong(2, limit.toLong())
            while (statement.step()) {
                results += ScriptureSearchResult(
                    bookId = statement.getText(0),
                    chapter = statement.getLong(1).toInt(),
                    verseRef = statement.getText(2),
                    text = statement.getText(3),
                )
            }
        }
        return results
    }

    fun passage(bookId: String, chapter: Int, verseRef: String): ScriptureSearchResult? {
        require(bookId.matches(Regex("[A-Z0-9]{3}")))
        require(chapter > 0 && verseRef.isNotBlank())
        return connection.prepare(
            """
            SELECT book_id, chapter, verse_ref, display_text
            FROM verses
            WHERE book_id = ? AND chapter = ? AND verse_ref = ?
            """.trimIndent()
        ).use { statement ->
            statement.bindText(1, bookId)
            statement.bindLong(2, chapter.toLong())
            statement.bindText(3, verseRef)
            if (!statement.step()) null else ScriptureSearchResult(
                statement.getText(0),
                statement.getLong(1).toInt(),
                statement.getText(2),
                statement.getText(3),
            )
        }
    }

    companion object {
        private const val EDITION_ID = "MDYTWF / mdyeth"
        private const val DATABASE_NAME = "content.sqlite"
        private const val DESCRIPTOR_NAME = "manifest.properties"
        private const val MAX_DATABASE_BYTES = 100L * 1024L * 1024L
        private const val EXPECTED_SCHEMA_VERSION = "1"
        private const val EXPECTED_TOKENIZER = "unicode61 remove_diacritics 0"
        private const val BASELINE_TOKENIZER_STATUS = "ENGINEERING_BASELINE_NOT_LINGUISTICALLY_QUALIFIED"

        private fun contentRoot(context: Context) = File(context.filesDir, "offline-content")
        private fun currentDirectory(context: Context) = File(contentRoot(context), "current")

        fun openInstalled(context: Context): OfflineScriptureStore? {
            val current = currentDirectory(context)
            val database = File(current, DATABASE_NAME)
            val descriptorFile = File(current, DESCRIPTOR_NAME)
            if (!database.isFile || !descriptorFile.isFile) return null
            val descriptor = readDescriptor(descriptorFile)
            validateDescriptor(descriptor)
            require(database.length() == descriptor.databaseBytes) {
                "Installed content package size mismatch"
            }
            require(sha256(database) == descriptor.databaseSha256) {
                "Installed content package integrity check failed"
            }
            val connection = BundledSQLiteDriver().open(database.absolutePath)
            try {
                validateDatabase(connection, descriptor)
                return OfflineScriptureStore(connection, descriptor)
            } catch (failure: Throwable) {
                connection.close()
                throw failure
            }
        }

        /**
         * Installs a caller-supplied database stream into app-private storage.
         * The caller transfers stream ownership to this method.
         */
        fun install(
            context: Context,
            source: InputStream,
            descriptor: OfflineContentDescriptor,
        ): OfflineContentDescriptor {
            validateDescriptor(descriptor)
            val root = contentRoot(context)
            check(root.mkdirs() || root.isDirectory) { "Cannot create private content directory" }
            val stage = File(root, "staging-" + System.nanoTime().toString())
            val stageDatabase = File(stage, DATABASE_NAME)
            val stageDescriptor = File(stage, DESCRIPTOR_NAME)
            val current = currentDirectory(context)
            val backup = File(root, "previous")
            check(!stage.exists() && stage.mkdir()) { "Cannot create private staging directory" }

            try {
                if (!current.exists() && backup.exists()) moveAtomically(backup, current)
                if (backup.exists()) backup.deleteRecursively()
                copyAndVerify(source, stageDatabase, descriptor)
                writeDescriptor(stageDescriptor, descriptor)
                val validation = BundledSQLiteDriver().open(stageDatabase.absolutePath)
                try {
                    validateDatabase(validation, descriptor)
                } finally {
                    validation.close()
                }

                if (current.exists()) moveAtomically(current, backup)
                try {
                    moveAtomically(stage, current)
                } catch (failure: Throwable) {
                    if (backup.exists() && !current.exists()) moveAtomically(backup, current)
                    throw failure
                }
                if (backup.exists()) backup.deleteRecursively()
                return descriptor
            } finally {
                source.close()
                if (stage.exists()) stage.deleteRecursively()
            }
        }

        private fun copyAndVerify(
            source: InputStream,
            destination: File,
            descriptor: OfflineContentDescriptor,
        ) {
            val digest = MessageDigest.getInstance("SHA-256")
            var copied = 0L
            FileOutputStream(destination).use { output ->
                source.use { input ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        copied += count
                        require(copied <= MAX_DATABASE_BYTES && copied <= descriptor.databaseBytes) {
                            "Content package exceeds its declared size"
                        }
                        digest.update(buffer, 0, count)
                        output.write(buffer, 0, count)
                    }
                }
                output.fd.sync()
            }
            require(copied == descriptor.databaseBytes) { "Content package size mismatch" }
            val actual = digest.digest().joinToString("") { "%02x".format(it) }
            require(actual == descriptor.databaseSha256) { "Content package SHA-256 mismatch" }
        }

        private fun validateDatabase(
            connection: SQLiteConnection,
            descriptor: OfflineContentDescriptor,
        ) {
            val version = scalarLong(connection, "PRAGMA user_version").toString()
            require(version == EXPECTED_SCHEMA_VERSION && version == descriptor.schemaVersion) {
                "Unsupported offline content schema"
            }
            require(metadata(connection, "edition_id") == descriptor.editionId)
            require(metadata(connection, "source_archive_sha256") == descriptor.sourceArchiveSha256)
            require(metadata(connection, "schema_version") == descriptor.schemaVersion)
            require(metadata(connection, "parser_version") == descriptor.parserVersion)
            require(metadata(connection, "fts_tokenizer") == descriptor.ftsTokenizer)
            require(metadata(connection, "tokenizer_qualification") == descriptor.tokenizerQualification)
            require(descriptor.ftsTokenizer == EXPECTED_TOKENIZER)
            require(descriptor.tokenizerQualification == BASELINE_TOKENIZER_STATUS)
            require(scalarLong(connection, "SELECT count(*) FROM books") == descriptor.bookCount.toLong())
            require(scalarLong(connection, "SELECT count(DISTINCT book_id || ':' || chapter) FROM verses") == descriptor.chapterCount.toLong())
            require(scalarLong(connection, "SELECT count(*) FROM verses") == descriptor.verseCount.toLong())
            require(scalarLong(connection, "SELECT count(*) FROM verse_fts") == descriptor.ftsCount.toLong())
            connection.prepare(
                "INSERT INTO verse_fts(verse_fts, rank) VALUES('integrity-check', 1)"
            ).use { it.step() }
            require(scalarText(connection, "PRAGMA quick_check") == "ok") {
                "Offline content database integrity check failed"
            }
        }

        private fun metadata(connection: SQLiteConnection, key: String): String =
            connection.prepare("SELECT value FROM metadata WHERE key = ?").use { statement ->
                statement.bindText(1, key)
                require(statement.step()) { "Missing offline content metadata" }
                statement.getText(0)
            }

        private fun scalarLong(connection: SQLiteConnection, sql: String): Long =
            connection.prepare(sql).use { statement ->
                require(statement.step())
                statement.getLong(0)
            }

        private fun scalarText(connection: SQLiteConnection, sql: String): String =
            connection.prepare(sql).use { statement ->
                require(statement.step())
                statement.getText(0)
            }

        private fun validateDescriptor(descriptor: OfflineContentDescriptor) {
            require(descriptor.editionId == EDITION_ID)
            require(descriptor.sourceArchiveSha256.matches(Regex("[a-f0-9]{64}")))
            require(descriptor.databaseSha256.matches(Regex("[a-f0-9]{64}")))
            require(descriptor.databaseBytes in 1..MAX_DATABASE_BYTES)
            require(descriptor.schemaVersion == EXPECTED_SCHEMA_VERSION)
            require(descriptor.parserVersion.isNotBlank())
            require(descriptor.bookCount == 66)
            require(descriptor.chapterCount > 0 && descriptor.verseCount > 0)
            require(descriptor.ftsCount == descriptor.verseCount)
            require(descriptor.ftsTokenizer == EXPECTED_TOKENIZER)
            require(descriptor.tokenizerQualification == BASELINE_TOKENIZER_STATUS)
        }

        private fun writeDescriptor(file: File, descriptor: OfflineContentDescriptor) {
            val values = linkedMapOf(
                "editionId" to descriptor.editionId,
                "sourceArchiveSha256" to descriptor.sourceArchiveSha256,
                "databaseSha256" to descriptor.databaseSha256,
                "databaseBytes" to descriptor.databaseBytes.toString(),
                "schemaVersion" to descriptor.schemaVersion,
                "parserVersion" to descriptor.parserVersion,
                "bookCount" to descriptor.bookCount.toString(),
                "chapterCount" to descriptor.chapterCount.toString(),
                "verseCount" to descriptor.verseCount.toString(),
                "ftsCount" to descriptor.ftsCount.toString(),
                "ftsTokenizer" to descriptor.ftsTokenizer,
                "tokenizerQualification" to descriptor.tokenizerQualification,
            )
            FileOutputStream(file).use { output ->
                values.forEach { (key, value) ->
                    output.write("$key=$value\n".toByteArray(Charsets.UTF_8))
                }
                output.fd.sync()
            }
        }

        private fun readDescriptor(file: File): OfflineContentDescriptor {
            val values = Properties().apply { FileInputStream(file).use(::load) }
            fun required(key: String) = requireNotNull(values.getProperty(key)) {
                "Offline content descriptor is incomplete"
            }
            return OfflineContentDescriptor(
                editionId = required("editionId"),
                sourceArchiveSha256 = required("sourceArchiveSha256"),
                databaseSha256 = required("databaseSha256"),
                databaseBytes = required("databaseBytes").toLong(),
                schemaVersion = required("schemaVersion"),
                parserVersion = required("parserVersion"),
                bookCount = required("bookCount").toInt(),
                chapterCount = required("chapterCount").toInt(),
                verseCount = required("verseCount").toInt(),
                ftsCount = required("ftsCount").toInt(),
                ftsTokenizer = required("ftsTokenizer"),
                tokenizerQualification = required("tokenizerQualification"),
            )
        }

        private fun sha256(file: File): String {
            val digest = MessageDigest.getInstance("SHA-256")
            FileInputStream(file).use { input ->
                val buffer = ByteArray(64 * 1024)
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    digest.update(buffer, 0, count)
                }
            }
            return digest.digest().joinToString("") { "%02x".format(it) }
        }

        private fun moveAtomically(source: File, destination: File) {
            Files.move(source.toPath(), destination.toPath(), StandardCopyOption.ATOMIC_MOVE)
        }
    }
}

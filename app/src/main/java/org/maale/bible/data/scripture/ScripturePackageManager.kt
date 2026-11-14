package org.maale.bible.data.scripture

import android.content.Context
import android.content.pm.ApplicationInfo
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import org.maale.bible.data.catalog.CanonicalBookCatalog
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.util.UUID

data class ScripturePackageMetadata(
    val editionId: String,
    val language: String,
    val license: String,
    val attribution: String,
    val bookCount: Int,
    val chapterCount: Int,
    val verseCount: Int,
    val packageSha256: String
)

data class ScriptureVerse(
    val label: String,
    val ordinal: Int,
    val text: String
)

data class ScriptureHeading(
    val ordinal: Int,
    val marker: String,
    val text: String
)

data class ScriptureChapter(
    val bookId: String,
    val chapterNumber: Int,
    val headings: List<ScriptureHeading>,
    val verses: List<ScriptureVerse>
)

/**
 * Installs and reads the separately generated Maale scripture SQLite package.
 *
 * The current importer output is explicitly unsigned and not release-eligible.
 * Therefore package import and reading are restricted to debuggable builds.
 * The database is copied into app-private storage and opened read-only; it is
 * never merged into the encrypted personal-study database.
 */
class ScripturePackageManager(context: Context) {
    private val appContext = context.applicationContext
    private val packageDirectory = File(appContext.filesDir, "scripture")
    private val installedPackage = File(packageDirectory, "maale-mdytwf-2008.sqlite")

    fun isDebuggableBuild(): Boolean =
        (appContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    fun installFromUri(uri: Uri): ScripturePackageMetadata {
        check(isDebuggableBuild()) { "Scripture review packages can only be imported in debug builds." }
        check(packageDirectory.exists() || packageDirectory.mkdirs()) { "Cannot create private scripture directory." }

        val temporary = File(packageDirectory, "candidate-${UUID.randomUUID()}.sqlite.tmp")
        val backup = File(packageDirectory, "maale-mdytwf-2008.sqlite.backup")
        try {
            val input = appContext.contentResolver.openInputStream(uri)
                ?: throw IOException("Selected package could not be opened.")
            input.use { source ->
                FileOutputStream(temporary).use { target ->
                    val buffer = ByteArray(COPY_BUFFER_BYTES)
                    var total = 0L
                    while (true) {
                        val read = source.read(buffer)
                        if (read < 0) break
                        total += read
                        require(total <= MAX_PACKAGE_BYTES) { "Scripture package exceeds the allowed size." }
                        target.write(buffer, 0, read)
                    }
                    target.fd.sync()
                    require(total > 0L) { "Selected package is empty." }
                }
            }

            val metadata = validatePackage(temporary)
            if (backup.exists() && !backup.delete()) throw IOException("Cannot clear previous package backup.")
            if (installedPackage.exists() && !installedPackage.renameTo(backup)) {
                throw IOException("Cannot safely replace the installed package.")
            }
            if (!temporary.renameTo(installedPackage)) {
                if (backup.exists()) backup.renameTo(installedPackage)
                throw IOException("Cannot install the validated package.")
            }
            installedPackage.setReadable(true, true)
            installedPackage.setWritable(false, true)
            installedPackage.setReadOnly()
            backup.delete()
            return metadata.copy(packageSha256 = sha256(installedPackage))
        } finally {
            if (temporary.exists()) temporary.delete()
        }
    }

    fun openInstalled(): ScripturePackageReader? {
        if (!isDebuggableBuild() || !installedPackage.isFile) return null
        val metadata = validatePackage(installedPackage)
        val database = SQLiteDatabase.openDatabase(
            installedPackage.absolutePath,
            null,
            SQLiteDatabase.OPEN_READONLY or SQLiteDatabase.NO_LOCALIZED_COLLATORS
        )
        return ScripturePackageReader(database, metadata.copy(packageSha256 = sha256(installedPackage)))
    }

    private fun validatePackage(file: File): ScripturePackageMetadata {
        require(file.isFile) { "Scripture package file is missing." }
        require(file.length() in MIN_PACKAGE_BYTES..MAX_PACKAGE_BYTES) { "Scripture package size is invalid." }
        FileInputStream(file).use { input ->
            val signature = ByteArray(SQLITE_HEADER.size)
            require(input.read(signature) == signature.size && signature.contentEquals(SQLITE_HEADER)) {
                "Selected file is not a SQLite package."
            }
        }

        val database = SQLiteDatabase.openDatabase(
            file.absolutePath,
            null,
            SQLiteDatabase.OPEN_READONLY or SQLiteDatabase.NO_LOCALIZED_COLLATORS
        )
        try {
            require(pragmaInt(database, "PRAGMA application_id") == APPLICATION_ID) { "SQLite application ID mismatch." }
            require(pragmaInt(database, "PRAGMA user_version") == SCHEMA_VERSION) { "Unsupported scripture package schema." }
            require(pragmaString(database, "PRAGMA integrity_check") == "ok") { "Scripture package integrity check failed." }
            require(database.rawQuery("PRAGMA foreign_key_check", null).use { !it.moveToFirst() }) {
                "Scripture package has broken references."
            }

            val expectedTables = setOf("books", "chapters", "headings", "package_metadata", "verses")
            val actualTables = mutableSetOf<String>()
            database.rawQuery(
                "SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%' ORDER BY name",
                null
            ).use { cursor ->
                while (cursor.moveToNext()) actualTables += cursor.getString(0)
            }
            require(actualTables == expectedTables) { "Scripture package table set is invalid." }
            require(scalarInt(database, "SELECT COUNT(*) FROM sqlite_master WHERE type IN ('trigger', 'view')") == 0) {
                "Unexpected database objects are present."
            }

            val metadata = mutableMapOf<String, String>()
            database.query("package_metadata", arrayOf("key", "value"), null, null, null, null, null).use { cursor ->
                while (cursor.moveToNext()) metadata[cursor.getString(0)] = cursor.getString(1)
            }
            require(metadata["package_format"] == PACKAGE_FORMAT) { "Unsupported scripture package format." }
            require(metadata["schema_version"] == SCHEMA_VERSION.toString()) { "Scripture package metadata version mismatch." }
            require(metadata["edition_id"] == EDITION_ID) { "Unexpected Bible edition." }
            require(metadata["language"] == LANGUAGE) { "Unexpected scripture language." }
            require(metadata["source_archive_sha256"] == SOURCE_ARCHIVE_SHA256) { "Source archive hash does not match the approved edition." }
            require(metadata["source_archive_bytes"] == SOURCE_ARCHIVE_BYTES.toString()) { "Source archive size does not match the approved edition." }
            require(metadata["license"] == LICENSE) { "Scripture package licence metadata is missing." }
            require(metadata["attribution"] == ATTRIBUTION) { "Scripture package attribution does not match the approved source." }
            require(metadata["signer_trust"] == "UNDEFINED_IN_ISOLATED_KEYRING") { "Unexpected signer-trust state." }
            require(metadata["release_eligible"] == "false") { "This importer output is not eligible for release." }
            require(metadata["content_is_immutable"] == "true") { "Scripture package must be immutable." }

            val expectedBookIds = CanonicalBookCatalog.books.map { it.usfmId }
            val actualBookIds = mutableListOf<String>()
            database.rawQuery(
                "SELECT book_id FROM books WHERE edition_id = ? ORDER BY ordinal",
                arrayOf(EDITION_ID)
            ).use { cursor ->
                while (cursor.moveToNext()) actualBookIds += cursor.getString(0)
            }
            require(actualBookIds == expectedBookIds) { "Scripture package book inventory or order is invalid." }

            val bookCount = scalarInt(database, "SELECT COUNT(*) FROM books WHERE edition_id = '$EDITION_ID'")
            val chapterCount = scalarInt(database, "SELECT COUNT(*) FROM chapters WHERE edition_id = '$EDITION_ID'")
            val verseCount = scalarInt(database, "SELECT COUNT(*) FROM verses WHERE edition_id = '$EDITION_ID'")
            require(bookCount == EXPECTED_BOOK_COUNT) { "Scripture package book count is invalid." }
            require(chapterCount == EXPECTED_CHAPTER_COUNT) { "Scripture package chapter count is invalid." }
            require(verseCount == EXPECTED_VERSE_COUNT) { "Scripture package verse count is invalid." }

            return ScripturePackageMetadata(
                editionId = EDITION_ID,
                language = LANGUAGE,
                license = LICENSE,
                attribution = ATTRIBUTION,
                bookCount = bookCount,
                chapterCount = chapterCount,
                verseCount = verseCount,
                packageSha256 = sha256(file)
            )
        } finally {
            database.close()
        }
    }

    private fun pragmaInt(database: SQLiteDatabase, pragma: String): Int =
        database.rawQuery(pragma, null).use { cursor ->
            require(cursor.moveToFirst()) { "SQLite pragma returned no result." }
            cursor.getInt(0)
        }

    private fun pragmaString(database: SQLiteDatabase, pragma: String): String =
        database.rawQuery(pragma, null).use { cursor ->
            require(cursor.moveToFirst()) { "SQLite pragma returned no result." }
            cursor.getString(0)
        }

    private fun scalarInt(database: SQLiteDatabase, query: String): Int =
        database.rawQuery(query, null).use { cursor ->
            require(cursor.moveToFirst()) { "SQLite count query returned no result." }
            cursor.getInt(0)
        }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(COPY_BUFFER_BYTES)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    companion object {
        const val PACKAGE_FORMAT = "MAALE-SCRIPTURE-SQLITE-1"
        const val EDITION_ID = "MDYTWF-2008-MAALE-FULL"
        const val LANGUAGE = "mdy"
        const val LICENSE = "CC BY-SA 4.0"
        const val ATTRIBUTION =
            "Maale Bible (ጌኤዦ ማፃኣፖ) © 2008 The Word for the World International and the Bible Society of Ethiopia."
        const val SOURCE_ARCHIVE_SHA256 = "4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11"
        const val SOURCE_ARCHIVE_BYTES = 1_809_863L
        const val EXPECTED_BOOK_COUNT = 66
        const val EXPECTED_CHAPTER_COUNT = 1_189
        const val EXPECTED_VERSE_COUNT = 30_506
        const val APPLICATION_ID = 1_296_189_762
        const val SCHEMA_VERSION = 1
        const val MAX_PACKAGE_BYTES = 64L * 1024L * 1024L
        const val MIN_PACKAGE_BYTES = 1_024L
        private const val COPY_BUFFER_BYTES = 32 * 1024
        private val SQLITE_HEADER = "SQLite format 3\u0000".toByteArray(Charsets.US_ASCII)
    }
}

class ScripturePackageReader internal constructor(
    private val database: SQLiteDatabase,
    val metadata: ScripturePackageMetadata
) : Closeable {
    fun chapterCount(bookId: String): Int {
        require(CanonicalBookCatalog.find(bookId) != null) { "Unknown Bible book." }
        return database.rawQuery(
            "SELECT COUNT(*) FROM chapters WHERE edition_id = ? AND book_id = ?",
            arrayOf(ScripturePackageManager.EDITION_ID, bookId)
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
    }

    fun loadChapter(bookId: String, chapterNumber: Int): ScriptureChapter {
        require(CanonicalBookCatalog.find(bookId) != null) { "Unknown Bible book." }
        require(chapterNumber > 0) { "Chapter number must be positive." }

        val headings = mutableListOf<ScriptureHeading>()
        database.rawQuery(
            "SELECT ordinal, marker, text FROM headings WHERE edition_id = ? AND book_id = ? AND chapter_number = ? ORDER BY ordinal",
            arrayOf(ScripturePackageManager.EDITION_ID, bookId, chapterNumber.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                headings += ScriptureHeading(
                    ordinal = cursor.getInt(0),
                    marker = cursor.getString(1),
                    text = cursor.getString(2)
                )
            }
        }

        val verses = mutableListOf<ScriptureVerse>()
        database.rawQuery(
            "SELECT verse_label, ordinal, text FROM verses WHERE edition_id = ? AND book_id = ? AND chapter_number = ? ORDER BY ordinal",
            arrayOf(ScripturePackageManager.EDITION_ID, bookId, chapterNumber.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                verses += ScriptureVerse(
                    label = cursor.getString(0),
                    ordinal = cursor.getInt(1),
                    text = cursor.getString(2)
                )
            }
        }
        return ScriptureChapter(bookId, chapterNumber, headings, verses)
    }

    override fun close() {
        if (database.isOpen) database.close()
    }
}

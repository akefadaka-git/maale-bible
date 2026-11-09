package org.maale.bible.data.source

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.zip.ZipInputStream

/** Expected source identity from the Owner-approved metadata manifest. */
data class ExpectedUsfmFile(
    val canonicalOrder: Int,
    val bookId: String,
    val path: String,
    val byteCount: Long,
    val sha256: String
)

data class SourceArchiveExpectation(
    val archiveByteCount: Long,
    val archiveSha256: String,
    val files: List<ExpectedUsfmFile>
)

data class VerifiedUsfmFile(
    val canonicalOrder: Int,
    val bookId: String,
    val path: String,
    val byteCount: Long,
    val sha256: String,
    /** Verified source bytes; callers must not persist them before the content-storage gate. */
    val bytes: ByteArray
)

data class VerifiedSourceArchive(
    val archiveSha256: String,
    val files: List<VerifiedUsfmFile>
)

data class ParsedVerifiedUsfmFile(
    val source: VerifiedUsfmFile,
    val parsed: ParsedUsfmBook
)

data class ParsedVerifiedSourceArchive(
    val archiveSha256: String,
    val files: List<ParsedVerifiedUsfmFile>
)

class SourceArchiveVerificationException(message: String) : IllegalArgumentException(message)

/**
 * Verifies an archive without extracting paths to disk. All expected USFM payloads
 * are checked against the frozen metadata manifest before any parser is invoked.
 */
object VerifiedUsfmArchive {
    private const val MAX_ARCHIVE_BYTES = 16L * 1024 * 1024
    private const val MAX_ENTRY_BYTES = 2L * 1024 * 1024
    private const val MAX_TOTAL_EXPANDED_BYTES = 12L * 1024 * 1024
    private const val MAX_ENTRIES = 128

    fun verify(input: InputStream, expected: SourceArchiveExpectation): VerifiedSourceArchive {
        validateExpectation(expected)
        val archive = readBounded(input, MAX_ARCHIVE_BYTES, "Archive exceeds size limit")
        require(archive.size.toLong() == expected.archiveByteCount) {
            "Archive byte count mismatch"
        }
        val archiveHash = sha256(archive)
        require(archiveHash.equals(expected.archiveSha256, ignoreCase = true)) {
            "Archive SHA-256 mismatch"
        }

        val expectedByPath = expected.files.associateBy { it.path }
        val seen = HashSet<String>()
        val verified = ArrayList<VerifiedUsfmFile>()
        var entryCount = 0
        var totalExpanded = 0L

        ZipInputStream(ByteArrayInputStream(archive)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                entryCount++
                require(entryCount <= MAX_ENTRIES) { "Archive contains too many entries" }
                val name = entry.name
                require(isSafeEntryPath(name)) { "Unsafe archive entry path" }
                require(seen.add(name)) { "Duplicate archive entry path: $name" }

                if (entry.isDirectory) {
                    zip.closeEntry()
                    continue
                }

                val expectedFile = expectedByPath[name]
                if (name.endsWith(".usfm", ignoreCase = true)) {
                    require(expectedFile != null) { "Unexpected USFM file: $name" }
                }

                val bytes = readBounded(zip, MAX_ENTRY_BYTES, "Archive entry exceeds size limit: $name")
                totalExpanded += bytes.size
                require(totalExpanded <= MAX_TOTAL_EXPANDED_BYTES) {
                    "Archive expanded size exceeds limit"
                }
                if (expectedFile != null) {
                    require(bytes.size.toLong() == expectedFile.byteCount) {
                        "File byte count mismatch: $name"
                    }
                    val hash = sha256(bytes)
                    require(hash.equals(expectedFile.sha256, ignoreCase = true)) {
                        "File SHA-256 mismatch: $name"
                    }
                    verified += VerifiedUsfmFile(
                        expectedFile.canonicalOrder,
                        expectedFile.bookId,
                        name,
                        bytes.size.toLong(),
                        hash,
                        bytes
                    )
                }
                zip.closeEntry()
            }
        }

        val missing = expectedByPath.keys - seen
        require(missing.isEmpty()) { "Missing expected USFM files: ${missing.sorted().joinToString()}" }
        require(verified.size == expected.files.size) { "Not all expected USFM files were verified" }
        return VerifiedSourceArchive(archiveHash, verified.sortedBy { it.canonicalOrder })
    }

    fun verifyAndParse(input: InputStream, expected: SourceArchiveExpectation): ParsedVerifiedSourceArchive {
        val verified = verify(input, expected)
        val parsed = verified.files.map { file ->
            val decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
            val sourceText = try {
                decoder.decode(ByteBuffer.wrap(file.bytes)).toString()
            } catch (error: Exception) {
                throw SourceArchiveVerificationException("Invalid UTF-8 in ${file.path}")
            }
            ParsedVerifiedUsfmFile(file, StrictUsfmParser.parse(sourceText, file.bookId))
        }
        require(parsed.size == 66) { "All 66 source books must parse successfully" }
        return ParsedVerifiedSourceArchive(verified.archiveSha256, parsed)
    }

    private fun validateExpectation(expected: SourceArchiveExpectation) {
        require(expected.archiveByteCount in 1..MAX_ARCHIVE_BYTES) { "Invalid expected archive size" }
        require(expected.archiveSha256.matches(Regex("[0-9a-fA-F]{64}"))) {
            "Invalid archive SHA-256"
        }
        require(expected.files.size == 66) { "Expected exactly 66 source books" }
        require(expected.files.map { it.canonicalOrder } == (1..66).toList()) {
            "Manifest order must be contiguous from 1 through 66"
        }
        require(expected.files.map { it.bookId }.toSet().size == 66) { "Duplicate book IDs in manifest" }
        require(expected.files.map { it.path }.toSet().size == 66) { "Duplicate paths in manifest" }
        expected.files.forEach {
            require(it.bookId.matches(Regex("[A-Z0-9]{3}"))) { "Invalid USFM book ID" }
            require(it.path.endsWith(".usfm")) { "Expected USFM path" }
            require(isSafeEntryPath(it.path)) { "Unsafe expected path" }
            require(it.byteCount in 1..MAX_ENTRY_BYTES) { "Invalid expected file size" }
            require(it.sha256.matches(Regex("[0-9a-fA-F]{64}"))) { "Invalid file SHA-256" }
        }
    }

    private fun isSafeEntryPath(path: String): Boolean {
        if (path.isBlank() || path.startsWith("/") || path.startsWith("\\") ||
            path.contains('\\') || path.contains('\u0000') || path.contains(':')
        ) return false
        val normalized = if (path.endsWith("/")) path.dropLast(1) else path
        if (normalized.isBlank()) return false
        val parts = normalized.split('/')
        return parts.none { it.isBlank() || it == "." || it == ".." }
    }

    private fun readBounded(input: InputStream, maxBytes: Long, error: String): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        var total = 0L
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            total += count
            require(total <= maxBytes) { error }
            output.write(buffer, 0, count)
        }
        return output.toByteArray()
    }

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
}

data class SourceVerseReference(
    val bookId: String,
    val chapter: Int,
    /** Exact source token, including segment suffix and/or range. */
    val sourceVerse: String,
    val startVerse: Int,
    val startSegment: String?,
    val endVerse: Int?,
    val endSegment: String?
)

data class UsfmRecord(
    val marker: String,
    /** Unmodified content between this marker and the next marker. */
    val payload: String,
    val chapter: Int?,
    val verse: SourceVerseReference?
)

data class ParsedUsfmBook(
    val bookId: String,
    val records: List<UsfmRecord>,
    val chapters: Int,
    val verseMarkers: Int
)

class StrictUsfmParseException(message: String) : IllegalArgumentException(message)

/**
 * Strict source-coordinate parser. It validates book identity, chapter continuity,
 * verse syntax/order/ranges and marker lexical boundaries while retaining all
 * non-coordinate markers and their raw payloads (headings, poetry, notes, styles).
 * It intentionally performs no cross-edition mapping, text normalization or repair.
 */
object StrictUsfmParser {
    private val markerName = Regex("[A-Za-z][A-Za-z0-9-]*")
    private val verseNumber = Regex("([1-9][0-9]*)([a-z]?)")

    fun parse(source: String, expectedBookId: String): ParsedUsfmBook {
        require(expectedBookId.matches(Regex("[A-Z0-9]{3}"))) { "Invalid expected book ID" }
        if (source.isEmpty()) throw StrictUsfmParseException("Empty USFM source")
        if (source.startsWith('\uFEFF')) throw StrictUsfmParseException("Unexpected UTF-8 BOM; source manifest requires strict UTF-8 without BOM")
        if (source.contains('\u0000')) throw StrictUsfmParseException("NUL character in USFM source")

        val tokens = tokenize(source)
        if (tokens.isEmpty() || tokens.first().marker != "id") {
            throw StrictUsfmParseException("First USFM marker must be \\id")
        }

        var bookId: String? = null
        var chapter: Int? = null
        var previousChapter = 0
        var previousVerseEnd: Pair<Int, String>? = null
        var currentVerse: SourceVerseReference? = null
        var verseCount = 0
        val records = ArrayList<UsfmRecord>(tokens.size)

        tokens.forEachIndexed { index, token ->
            when (token.marker) {
                "id" -> {
                    if (index != 0 || bookId != null) throw StrictUsfmParseException("Duplicate or misplaced \\id marker")
                    val id = token.payload.trim().split(Regex("\\s+"), limit = 2).firstOrNull().orEmpty()
                    if (!id.matches(Regex("[A-Z0-9]{3}"))) throw StrictUsfmParseException("Malformed \\id book identifier")
                    if (id != expectedBookId) throw StrictUsfmParseException("Source book ID $id does not match expected $expectedBookId")
                    bookId = id
                    records += UsfmRecord(token.marker, token.payload, null, null)
                }
                "c" -> {
                    val raw = token.payload.trim().split(Regex("\\s+"), limit = 2).firstOrNull().orEmpty()
                    val next = raw.toIntOrNull()
                        ?: throw StrictUsfmParseException("Malformed chapter number")
                    if (next != previousChapter + 1) {
                        throw StrictUsfmParseException("Chapter sequence must be contiguous; expected ${previousChapter + 1}, found $next")
                    }
                    if (previousChapter > 0 && previousVerseEnd == null) {
                        throw StrictUsfmParseException("Chapter $previousChapter contains no verse markers")
                    }
                    chapter = next
                    previousChapter = next
                    previousVerseEnd = null
                    currentVerse = null
                    records += UsfmRecord(token.marker, token.payload, next, null)
                }
                "v" -> {
                    val currentChapter = chapter
                        ?: throw StrictUsfmParseException("Verse marker encountered before first chapter")
                    val raw = token.payload.trim().split(Regex("\\s+"), limit = 2).firstOrNull().orEmpty()
                    val parsed = parseVerse(raw)
                    val startKey = parsed.first to parsed.second
                    val endKey = (parsed.third ?: parsed.first) to (parsed.fourth ?: parsed.second)
                    val prior = previousVerseEnd
                    if (prior != null && (
                            startKey.first < prior.first ||
                            (startKey.first == prior.first &&
                                (prior.second.isEmpty() || startKey.second <= prior.second))
                        )
                    ) {
                        throw StrictUsfmParseException("Verse markers are duplicated, overlapping or out of order")
                    }
                    if (compareVerse(endKey, startKey) < 0) {
                        throw StrictUsfmParseException("Verse range ends before it starts")
                    }
                    previousVerseEnd = endKey
                    verseCount++
                    val ref = SourceVerseReference(
                        expectedBookId, currentChapter, raw,
                        parsed.first, parsed.second.ifEmpty { null },
                        parsed.third, parsed.fourth?.ifEmpty { null }
                    )
                    currentVerse = ref
                    records += UsfmRecord(token.marker, token.payload, currentChapter, ref)
                }
                else -> records += UsfmRecord(token.marker, token.payload, chapter, currentVerse)
            }
        }

        if (bookId == null) throw StrictUsfmParseException("Missing book identity")
        if (previousChapter == 0) throw StrictUsfmParseException("No chapter markers found")
        if (previousVerseEnd == null) throw StrictUsfmParseException("Final chapter contains no verse markers")
        return ParsedUsfmBook(expectedBookId, records, previousChapter, verseCount)
    }

    private data class Token(val marker: String, val payload: String)

    private fun tokenize(source: String): List<Token> {
        val tokens = ArrayList<Token>()
        var cursor = 0
        while (cursor < source.length) {
            val slash = source.indexOf('\\', cursor)
            if (slash < 0) break
            if (slash > cursor) {
                val interveningText = source.substring(cursor, slash)
                if (tokens.isEmpty()) {
                    if (interveningText.isNotBlank()) {
                        throw StrictUsfmParseException("Unexpected text before the first \\id marker")
                    }
                } else if (interveningText.isNotEmpty()) {
                    tokens += Token("#text", interveningText)
                }
            }
            if (slash + 1 >= source.length) throw StrictUsfmParseException("Trailing backslash")
            val next = source[slash + 1]
            if (!next.isLetter() && next != '+' && next != '*') {
                // USFM 3 escaped literal characters are retained in the preceding payload.
                if (next in listOf('\\', '|', '~', '/', '\'', '"')) {
                    cursor = slash + 2
                    continue
                }
                throw StrictUsfmParseException("Malformed marker escape at offset $slash")
            }
            if (slash > cursor) {
                // The preceding token's payload is closed at this marker.
            }
            var nameStart = slash + 1
            if (source[nameStart] == '*') {
                tokens += Token("*", "")
                cursor = nameStart + 1
                continue
            }
            if (source[nameStart] == '+') nameStart++
            var end = nameStart
            while (end < source.length && (source[end].isLetterOrDigit() || source[end] == '-')) end++
            val rawName = source.substring(nameStart, end)
            if (!markerName.matches(rawName)) throw StrictUsfmParseException("Malformed marker at offset $slash")
            val marker = (if (source[slash + 1] == '+') "+" else "") + rawName
            if (end < source.length && source[end] == '*') {
                tokens += Token("/$marker", "")
                cursor = end + 1
            } else {
                if (end < source.length && !source[end].isWhitespace() && source[end] != '\\') {
                    throw StrictUsfmParseException("Marker must be followed by whitespace or another marker")
                }
                var payloadStart = end
                if (payloadStart < source.length && source[payloadStart].isWhitespace()) payloadStart++
                var nextMarker = payloadStart
                while (nextMarker < source.length) {
                    if (source[nextMarker] == '\\' && nextMarker + 1 < source.length) {
                        val ch = source[nextMarker + 1]
                        if (ch.isLetter() || ch == '+' || ch == '*') break
                        if (ch in listOf('\\', '|', '~', '/', '\'', '"')) {
                            nextMarker += 2
                            continue
                        }
                    }
                    nextMarker++
                }
                tokens += Token(marker, source.substring(payloadStart, nextMarker))
                cursor = nextMarker
            }
        }
        return tokens
    }

    /** Returns start number/segment and optional end number/segment. */
    private fun parseVerse(raw: String): Quad {
        val parts = raw.split('-', limit = 2)
        val start = verseNumber.matchEntire(parts[0])
            ?: throw StrictUsfmParseException("Malformed source verse coordinate: $raw")
        val end = if (parts.size == 2) verseNumber.matchEntire(parts[1])
            ?: throw StrictUsfmParseException("Malformed source verse range: $raw") else null
        return Quad(
            start.groupValues[1].toInt(), start.groupValues[2],
            end?.groupValues?.get(1)?.toInt(), end?.groupValues?.get(2)
        )
    }

    private data class Quad(val first: Int, val second: String, val third: Int?, val fourth: String?)

    private fun compareVerse(left: Pair<Int, String>, right: Pair<Int, String>): Int {
        val number = left.first.compareTo(right.first)
        return if (number != 0) number else left.second.compareTo(right.second)
    }
}

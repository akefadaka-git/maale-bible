package org.maale.bible.data

/** Frozen identity registry for maale-bible:canon:66-v1. Names are canonical English labels, not localized UI strings. */
data class CanonicalBook(val order: Int, val usfmId: String, val canonicalName: String, val testament: String)

object CanonicalCanonRegistry {
    const val CANON_ID = "maale-bible:canon:66-v1"
    const val VERSION = 1
    val books: List<CanonicalBook> = listOf(
        CanonicalBook(1, "GEN", "Genesis", "OT"),
        CanonicalBook(2, "EXO", "Exodus", "OT"),
        CanonicalBook(3, "LEV", "Leviticus", "OT"),
        CanonicalBook(4, "NUM", "Numbers", "OT"),
        CanonicalBook(5, "DEU", "Deuteronomy", "OT"),
        CanonicalBook(6, "JOS", "Joshua", "OT"),
        CanonicalBook(7, "JDG", "Judges", "OT"),
        CanonicalBook(8, "RUT", "Ruth", "OT"),
        CanonicalBook(9, "1SA", "1 Samuel", "OT"),
        CanonicalBook(10, "2SA", "2 Samuel", "OT"),
        CanonicalBook(11, "1KI", "1 Kings", "OT"),
        CanonicalBook(12, "2KI", "2 Kings", "OT"),
        CanonicalBook(13, "1CH", "1 Chronicles", "OT"),
        CanonicalBook(14, "2CH", "2 Chronicles", "OT"),
        CanonicalBook(15, "EZR", "Ezra", "OT"),
        CanonicalBook(16, "NEH", "Nehemiah", "OT"),
        CanonicalBook(17, "EST", "Esther", "OT"),
        CanonicalBook(18, "JOB", "Job", "OT"),
        CanonicalBook(19, "PSA", "Psalms", "OT"),
        CanonicalBook(20, "PRO", "Proverbs", "OT"),
        CanonicalBook(21, "ECC", "Ecclesiastes", "OT"),
        CanonicalBook(22, "SNG", "Song of Songs", "OT"),
        CanonicalBook(23, "ISA", "Isaiah", "OT"),
        CanonicalBook(24, "JER", "Jeremiah", "OT"),
        CanonicalBook(25, "LAM", "Lamentations", "OT"),
        CanonicalBook(26, "EZK", "Ezekiel", "OT"),
        CanonicalBook(27, "DAN", "Daniel", "OT"),
        CanonicalBook(28, "HOS", "Hosea", "OT"),
        CanonicalBook(29, "JOL", "Joel", "OT"),
        CanonicalBook(30, "AMO", "Amos", "OT"),
        CanonicalBook(31, "OBA", "Obadiah", "OT"),
        CanonicalBook(32, "JON", "Jonah", "OT"),
        CanonicalBook(33, "MIC", "Micah", "OT"),
        CanonicalBook(34, "NAM", "Nahum", "OT"),
        CanonicalBook(35, "HAB", "Habakkuk", "OT"),
        CanonicalBook(36, "ZEP", "Zephaniah", "OT"),
        CanonicalBook(37, "HAG", "Haggai", "OT"),
        CanonicalBook(38, "ZEC", "Zechariah", "OT"),
        CanonicalBook(39, "MAL", "Malachi", "OT"),
        CanonicalBook(40, "MAT", "Matthew", "NT"),
        CanonicalBook(41, "MRK", "Mark", "NT"),
        CanonicalBook(42, "LUK", "Luke", "NT"),
        CanonicalBook(43, "JHN", "John", "NT"),
        CanonicalBook(44, "ACT", "Acts", "NT"),
        CanonicalBook(45, "ROM", "Romans", "NT"),
        CanonicalBook(46, "1CO", "1 Corinthians", "NT"),
        CanonicalBook(47, "2CO", "2 Corinthians", "NT"),
        CanonicalBook(48, "GAL", "Galatians", "NT"),
        CanonicalBook(49, "EPH", "Ephesians", "NT"),
        CanonicalBook(50, "PHP", "Philippians", "NT"),
        CanonicalBook(51, "COL", "Colossians", "NT"),
        CanonicalBook(52, "1TH", "1 Thessalonians", "NT"),
        CanonicalBook(53, "2TH", "2 Thessalonians", "NT"),
        CanonicalBook(54, "1TI", "1 Timothy", "NT"),
        CanonicalBook(55, "2TI", "2 Timothy", "NT"),
        CanonicalBook(56, "TIT", "Titus", "NT"),
        CanonicalBook(57, "PHM", "Philemon", "NT"),
        CanonicalBook(58, "HEB", "Hebrews", "NT"),
        CanonicalBook(59, "JAS", "James", "NT"),
        CanonicalBook(60, "1PE", "1 Peter", "NT"),
        CanonicalBook(61, "2PE", "2 Peter", "NT"),
        CanonicalBook(62, "1JN", "1 John", "NT"),
        CanonicalBook(63, "2JN", "2 John", "NT"),
        CanonicalBook(64, "3JN", "3 John", "NT"),
        CanonicalBook(65, "JUD", "Jude", "NT"),
        CanonicalBook(66, "REV", "Revelation", "NT")
    )

    init {
        require(books.size == 66)
        require(books.map { it.usfmId }.toSet().size == 66)
        require(books.map { it.order } == (1..66).toList())
        require(books.count { it.testament == "OT" } == 39)
        require(books.count { it.testament == "NT" } == 27)
    }

    fun contains(usfmId: String): Boolean =
        books.any { it.usfmId == usfmId.uppercase() }
}

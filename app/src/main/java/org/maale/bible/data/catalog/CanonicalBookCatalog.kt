package org.maale.bible.data.catalog

import org.maale.bible.domain.model.Book

/** Metadata-only catalogue for the Owner-approved 2008 MDYTWF / mdyeth edition. */
object CanonicalBookCatalog {
    val books = listOf(
        Book(1,"GEN","Genesis"), Book(2,"EXO","Exodus"), Book(3,"LEV","Leviticus"),
        Book(4,"NUM","Numbers"), Book(5,"DEU","Deuteronomy"), Book(6,"JOS","Joshua"),
        Book(7,"JDG","Judges"), Book(8,"RUT","Ruth"), Book(9,"1SA","1 Samuel"),
        Book(10,"2SA","2 Samuel"), Book(11,"1KI","1 Kings"), Book(12,"2KI","2 Kings"),
        Book(13,"1CH","1 Chronicles"), Book(14,"2CH","2 Chronicles"), Book(15,"EZR","Ezra"),
        Book(16,"NEH","Nehemiah"), Book(17,"EST","Esther"), Book(18,"JOB","Job"),
        Book(19,"PSA","Psalms"), Book(20,"PRO","Proverbs"), Book(21,"ECC","Ecclesiastes"),
        Book(22,"SNG","Song of Songs"), Book(23,"ISA","Isaiah"), Book(24,"JER","Jeremiah"),
        Book(25,"LAM","Lamentations"), Book(26,"EZK","Ezekiel"), Book(27,"DAN","Daniel"),
        Book(28,"HOS","Hosea"), Book(29,"JOL","Joel"), Book(30,"AMO","Amos"),
        Book(31,"OBA","Obadiah"), Book(32,"JON","Jonah"), Book(33,"MIC","Micah"),
        Book(34,"NAM","Nahum"), Book(35,"HAB","Habakkuk"), Book(36,"ZEP","Zephaniah"),
        Book(37,"HAG","Haggai"), Book(38,"ZEC","Zechariah"), Book(39,"MAL","Malachi"),
        Book(40,"MAT","Matthew"), Book(41,"MRK","Mark"), Book(42,"LUK","Luke"),
        Book(43,"JHN","John"), Book(44,"ACT","Acts"), Book(45,"ROM","Romans"),
        Book(46,"1CO","1 Corinthians"), Book(47,"2CO","2 Corinthians"), Book(48,"GAL","Galatians"),
        Book(49,"EPH","Ephesians"), Book(50,"PHP","Philippians"), Book(51,"COL","Colossians"),
        Book(52,"1TH","1 Thessalonians"), Book(53,"2TH","2 Thessalonians"), Book(54,"1TI","1 Timothy"),
        Book(55,"2TI","2 Timothy"), Book(56,"TIT","Titus"), Book(57,"PHM","Philemon"),
        Book(58,"HEB","Hebrews"), Book(59,"JAS","James"), Book(60,"1PE","1 Peter"),
        Book(61,"2PE","2 Peter"), Book(62,"1JN","1 John"), Book(63,"2JN","2 John"),
        Book(64,"3JN","3 John"), Book(65,"JUD","Jude"), Book(66,"REV","Revelation")
    )

    private val byId = books.associateBy(Book::usfmId)

    fun find(usfmId: String): Book? = byId[usfmId]

    /**
     * Searches catalogue metadata only. It never searches or returns scripture text.
     * Results retain the canonical order from the approved book inventory.
     */
    fun search(query: String): List<Book> {
        val normalized = query.trim()
        if (normalized.isEmpty()) return books
        return books.filter { book ->
            book.englishName.contains(normalized, ignoreCase = true) ||
                book.usfmId.contains(normalized, ignoreCase = true)
        }
    }
}

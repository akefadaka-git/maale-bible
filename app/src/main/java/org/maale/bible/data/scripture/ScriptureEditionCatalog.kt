package org.maale.bible.data.scripture

/**
 * Catalogue of candidate Bible editions. A catalogue entry is not a grant of rights and does
 * not imply that a text package is present. Import is blocked until edition-specific source,
 * licence, attribution, canon and package-integrity evidence has been approved.
 */
enum class ScriptureEditionStatus {
    PRIMARY_SOURCE,
    CATALOG_ONLY_RIGHTS_UNVERIFIED,
    SOURCE_AND_RIGHTS_REVIEW_REQUIRED
}

data class ScriptureEdition(
    val id: String,
    val languageTag: String,
    val languageName: String,
    val title: String,
    val publisherOrTradition: String,
    val publicationYear: String?,
    val status: ScriptureEditionStatus,
    /** Catalogue-level completeness candidate only; not a verified package or canon assertion. */
    val fullBible: Boolean,
    val notes: String
)

object ScriptureEditionCatalog {
    const val PRIMARY_MAALE_ID = "MDYTWF-2008-MAALE-FULL"

    val editions: List<ScriptureEdition> = listOf(
        ScriptureEdition(
            id = PRIMARY_MAALE_ID,
            languageTag = "mdy",
            languageName = "Maale",
            title = "Maale Bible (2008)",
            publisherOrTradition = "The Word for the World International and Bible Society of Ethiopia",
            publicationYear = "2008",
            status = ScriptureEditionStatus.PRIMARY_SOURCE,
            fullBible = true,
            notes = "Approved source identity; package installation and release eligibility are checked separately."
        ),

        // Amharic catalogue: four identifiable edition families. The 1987/2005 entry is one
        // revised edition lineage, not two independently selectable texts.
        amharic("AMH-HAILE-SELASSIE-1962", "New Haile Selassie I Bible", "Bible Committee / British and Foreign Bible Society", "1962",
            "Historical Amharic edition; confirm the exact print/revision and rights before import."),
        amharic("AMH-UBS-1987-2005", "UBS Amharic Version (1987; revised 2005)", "Bible Society of Ethiopia / United Bible Societies", "1987/2005",
            "Revised edition lineage; confirm exact 2005 text, canon and digital distribution rights."),
        amharic("AMH-NASV-2001", "New Amharic Standard Version (NASV)", "Biblica", "2001",
            "Confirm exact edition identity and obtain digital-use permission."),
        amharic("AMH-MILLENNIUM", "Millennium Amharic Bible (identity unverified)", "Publisher not established", null,
            "Confirm publication record, full canon and rights-holder authorization before import."),

        // English catalogue: 42 distinct translation/version candidates. Availability and
        // licensing are deliberately not inferred from familiarity or public-domain claims.
        english("ENG-ASV-1901", "American Standard Version", "1901"),
        english("ENG-AMP-2015", "Amplified Bible", "2015"),
        english("ENG-AMP-CLASSIC", "Amplified Bible Classic Edition", null),
        english("ENG-BBE", "Bible in Basic English", null),
        english("ENG-BISHOPS", "Bishops' Bible", "1568"),
        english("ENG-CJB", "Complete Jewish Bible", "1998"),
        english("ENG-CSB", "Christian Standard Bible", "2017"),
        english("ENG-CEV", "Contemporary English Version", "1995"),
        english("ENG-DARBY", "Darby Translation", "1890"),
        english("ENG-DRB", "Douay-Rheims Bible", "1899"),
        english("ENG-ERV", "English Revised Version", "1885"),
        english("ENG-ESV", "English Standard Version", "2001"),
        english("ENG-GENEVA", "Geneva Bible", "1599"),
        english("ENG-GNT", "Good News Translation", "1976"),
        english("ENG-GW", "God's Word Translation", "1995"),
        english("ENG-HCSB", "Holman Christian Standard Bible", "2004"),
        english("ENG-ISV", "International Standard Version", null),
        english("ENG-JB", "Jerusalem Bible", "1966"),
        english("ENG-KJV", "King James Version", "1611"),
        english("ENG-LSB", "Legacy Standard Bible", "2021"),
        english("ENG-MEB", "Modern English Bible", null),
        english("ENG-MKJV", "Modern King James Version", "1962"),
        english("ENG-MOUNCE", "Mounce Reverse-Interlinear New Testament", null, fullBible = false),
        english("ENG-NASB-1995", "New American Standard Bible", "1995"),
        english("ENG-NASB-2020", "New American Standard Bible", "2020"),
        english("ENG-NCB", "New Catholic Bible", "2019"),
        english("ENG-NET", "New English Translation", "2005"),
        english("ENG-NIRV", "New International Reader's Version", "1996"),
        english("ENG-NIV", "New International Version", "1978"),
        english("ENG-NKJV", "New King James Version", "1982"),
        english("ENG-NLT", "New Living Translation", "1996"),
        english("ENG-NRSV", "New Revised Standard Version", "1989"),
        english("ENG-NRSVue", "New Revised Standard Version Updated Edition", "2021"),
        english("ENG-REB", "Revised English Bible", "1989"),
        english("ENG-RSV", "Revised Standard Version", "1952"),
        english("ENG-TLB", "The Living Bible", "1971"),
        english("ENG-TPT", "The Passion Translation", "2017"),
        english("ENG-VOICE", "The Voice", "2012"),
        english("ENG-WEB", "World English Bible", null),
        english("ENG-WEY", "Weymouth New Testament", "1903", fullBible = false),
        english("ENG-WYC", "Wycliffe Bible", "1382"),
        english("ENG-LEB", "Lexham English Bible", "2012"),
        english("ENG-NEB", "New English Bible", "1970"),
        english("ENG-OEB", "Open English Bible", "2010–present", fullBible = false),
        english("ENG-KJ21", "21st Century King James Version", "1994"),
        english("ENG-RV", "Revised Version", "1881 (New Testament); 1885 (complete Bible)"),
        english("ENG-YLT", "Young's Literal Translation", "1862")
    )

    val amharicEditions: List<ScriptureEdition> get() = editions.filter { it.languageTag == "am" }
    val englishEditions: List<ScriptureEdition> get() = editions.filter { it.languageTag == "en" }

    fun find(editionId: String): ScriptureEdition? = editions.firstOrNull { it.id == editionId }

    private fun amharic(id: String, title: String, publisher: String, year: String?, notes: String) =
        ScriptureEdition(id, "am", "Amharic", title, publisher, year,
            ScriptureEditionStatus.SOURCE_AND_RIGHTS_REVIEW_REQUIRED, true, notes)

    private fun english(id: String, title: String, year: String?, fullBible: Boolean = true) =
        ScriptureEdition(id, "en", "English", title, "Edition-specific publisher / rights holder to verify", year,
            ScriptureEditionStatus.CATALOG_ONLY_RIGHTS_UNVERIFIED, fullBible,
            "Catalogue candidate only. Verify source edition, completeness, licence, attribution and digital distribution rights before import.")
}

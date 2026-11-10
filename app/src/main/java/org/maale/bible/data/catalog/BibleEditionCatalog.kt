package org.maale.bible.data.catalog

enum class EditionReadiness {
    READY_FOR_PACKAGE_INTEGRATION,
    PARTIAL_TEXT,
    EDITORIAL_REVIEW_REQUIRED,
    RIGHTS_REVIEW_REQUIRED
}

data class BibleEditionSource(
    val id: String,
    val title: String,
    val languageCode: String,
    val languageName: String,
    val abbreviation: String,
    val license: String,
    val rightsSummary: String,
    val sourceUrl: String,
    val readiness: EditionReadiness,
    val fullBible: Boolean,
    val attribution: String,
)

/**
 * Curated source register, not a blanket grant to ingest every Bible hosted
 * by a third-party catalog. Each edition keeps its own license and readiness.
 */
object BibleEditionCatalog {
    val editions = listOf(
        BibleEditionSource(
            id = "eng-kjv2006",
            title = "King James (Authorized) Version",
            languageCode = "en",
            languageName = "English",
            abbreviation = "KJV",
            license = "Public domain (outside the United Kingdom)",
            rightsSummary = "UK print/import restrictions apply; retain source and edition notices.",
            sourceUrl = "https://ebible.org/find/show.php?id=eng-kjv2006",
            readiness = EditionReadiness.READY_FOR_PACKAGE_INTEGRATION,
            fullBible = true,
            attribution = "Text source: eBible.org / CrossWire Bible Society.",
        ),
        BibleEditionSource(
            id = "eng-asv",
            title = "American Standard Version (1901)",
            languageCode = "en",
            languageName = "English",
            abbreviation = "ASV",
            license = "Public domain",
            rightsSummary = "Source identifies this 1901 edition as public domain.",
            sourceUrl = "https://ebible.org/find/show.php?id=eng-asv",
            readiness = EditionReadiness.READY_FOR_PACKAGE_INTEGRATION,
            fullBible = true,
            attribution = "Text source: eBible.org.",
        ),
        BibleEditionSource(
            id = "eng-web",
            title = "World English Bible Classic",
            languageCode = "en",
            languageName = "English",
            abbreviation = "WEB",
            license = "Public domain; translation name is a trademark",
            rightsSummary = "Text may be redistributed; preserve trademark distinction and source notices.",
            sourceUrl = "https://ebible.org/find/show.php?id=eng-web",
            readiness = EditionReadiness.READY_FOR_PACKAGE_INTEGRATION,
            fullBible = true,
            attribution = "World English Bible, courtesy of eBible.org.",
        ),
        BibleEditionSource(
            id = "eng-bsb",
            title = "Berean Standard Bible",
            languageCode = "en",
            languageName = "English",
            abbreviation = "BSB",
            license = "Public domain dedication, 30 April 2023",
            rightsSummary = "Official terms permit free reproduction, integration and adaptation.",
            sourceUrl = "https://berean.bible/downloads.htm",
            readiness = EditionReadiness.READY_FOR_PACKAGE_INTEGRATION,
            fullBible = true,
            attribution = "The Holy Bible, Berean Standard Bible (BSB), Berean Bible Translation Committee.",
        ),
        BibleEditionSource(
            id = "eng-oeb",
            title = "Open English Bible",
            languageCode = "en",
            languageName = "English",
            abbreviation = "OEB",
            license = "CC0",
            rightsSummary = "Current 2025.6 release is partial: New Testament, Psalms and selected Old Testament books.",
            sourceUrl = "https://openenglishbible.org/",
            readiness = EditionReadiness.PARTIAL_TEXT,
            fullBible = false,
            attribution = "Open English Bible; CC0.",
        ),
        BibleEditionSource(
            id = "eng-fbv",
            title = "Free Bible Version",
            languageCode = "en",
            languageName = "English",
            abbreviation = "FBV",
            license = "CC BY-SA 4.0",
            rightsSummary = "Attribution and ShareAlike conditions apply; preserve notices and license.",
            sourceUrl = "https://open.bible/bibles/english-freebible-text-bible",
            readiness = EditionReadiness.READY_FOR_PACKAGE_INTEGRATION,
            fullBible = true,
            attribution = "Free Bible Version, copyright Dr. Jonathan Gallagher; CC BY-SA 4.0.",
        ),
        BibleEditionSource(
            id = "amh-open-word",
            title = "Open Word Bible — Amharic",
            languageCode = "am",
            languageName = "Amharic",
            abbreviation = "OWB-AM",
            license = "CC0",
            rightsSummary = "The project labels its current Amharic text as AI-assisted draft content pending named reviewer approval.",
            sourceUrl = "https://openwordbible.org/download/",
            readiness = EditionReadiness.EDITORIAL_REVIEW_REQUIRED,
            fullBible = true,
            attribution = "Open Word Bible; CC0. Do not present draft text as an approved Bible translation.",
        ),
        BibleEditionSource(
            id = "amh-ubs-2003-nt",
            title = "Revised Amharic Bible (2003) — New Testament",
            languageCode = "am",
            languageName = "Amharic",
            abbreviation = "AMH",
            license = "Copyright © 1962, 2003 United Bible Societies; permission conditions apply",
            rightsSummary = "Source permits non-commercial use with full notice; commercial use requires written permission. Not treated as an open/free license.",
            sourceUrl = "https://ebible.org/bible/details.php?id=amh",
            readiness = EditionReadiness.RIGHTS_REVIEW_REQUIRED,
            fullBible = false,
            attribution = "Copyright © 1962, 2003 United Bible Societies; used by eBible.org with permission.",
        ),
    )

    fun eligibleForPackage(languageCode: String? = null): List<BibleEditionSource> =
        editions.filter {
            it.readiness == EditionReadiness.READY_FOR_PACKAGE_INTEGRATION ||
                it.readiness == EditionReadiness.PARTIAL_TEXT
        }.filter { languageCode == null || it.languageCode == languageCode }

    fun awaitingReview(languageCode: String? = null): List<BibleEditionSource> =
        editions.filter {
            it.readiness == EditionReadiness.EDITORIAL_REVIEW_REQUIRED ||
                it.readiness == EditionReadiness.RIGHTS_REVIEW_REQUIRED
        }.filter { languageCode == null || it.languageCode == languageCode }
}

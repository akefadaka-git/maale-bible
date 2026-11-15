package org.maale.bible.data.scripture

/**
 * Evidence-backed source and rights register. Publication identity is not a licence.
 * A package is importable only after exact source, rights, archive hash, package hash,
 * signer and canon have been verified for that edition.
 */
data class ScriptureEditionSourceRecord(
    val editionId: String,
    val sourceTitle: String?,
    val publisherOrRightsHolder: String?,
    val publicationDetails: String?,
    val canonScope: String?,
    val sourceUrl: String?,
    val sourceArchiveSha256: String?,
    val license: String?,
    val attribution: String?,
    val distributionPermission: String,
    val sourceVerification: String,
    val packageFileName: String?,
    val packageSha256: String?,
    val signerFingerprint: String?,
    val releaseEligible: Boolean
)

object ScriptureEditionSourceRegistry {
    private const val PENDING = "PENDING_WRITTEN_AUTHORIZATION_AND_SOURCE_INTAKE"
    private const val UNVERIFIED = "CATALOGUE_CANDIDATE_NOT_YET_PRIMARY_SOURCE_VERIFIED"

    val records: List<ScriptureEditionSourceRecord> = listOf(
        ScriptureEditionSourceRecord(
            editionId = ScriptureEditionCatalog.PRIMARY_MAALE_ID,
            sourceTitle = "Maale Bible (MDYTWF / mdyeth, 2008)",
            publisherOrRightsHolder = "The Word for the World International; Bible Society of Ethiopia",
            publicationDetails = "2008 full Bible; archive mdyeth_usfm.zip",
            canonScope = "66-book Protestant canon; source archive contains 66 USFM books",
            sourceUrl = null,
            sourceArchiveSha256 = "4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11",
            license = "CC BY-SA 4.0",
            attribution = "Maale Bible (ጌኤዦ ማፃኣፖ) © 2008 The Word for the World International and the Bible Society of Ethiopia.",
            distributionPermission = "USER_SUPPLIED_WRITTEN_PERMISSION_AND_CC_BY_SA_4_0_EVIDENCE",
            sourceVerification = "ARCHIVE_HASH_AND_66_USFM_HASHES_REPORTED_VALID; SOURCE_ARCHIVE_NOT_CURRENTLY_AVAILABLE_FOR_PACKAGE_BUILD",
            packageFileName = "maale-mdytwf-2008.sqlite",
            packageSha256 = null,
            signerFingerprint = "54D714D76956DA6673DC1EEA5F62009D93505F26",
            releaseEligible = false
        ),
        ScriptureEditionSourceRecord(
            editionId = "AMH-HAILE-SELASSIE-1962",
            sourceTitle = "New Haile Selassie I Bible",
            publisherOrRightsHolder = "British and Foreign Bible Society; Bible Society of Ethiopia / United Bible Societies",
            publicationDetails = "1961 revision; 1962 reprint without Apocrypha",
            canonScope = "Full Protestant canon (66 books), per Digital Bible Society catalogue",
            sourceUrl = "https://arc.dbs.org/data/scripture/AMHNHS/",
            sourceArchiveSha256 = null, license = null, attribution = null,
            distributionPermission = PENDING,
            sourceVerification = "IDENTITY_AND_SCOPE_CORROBORATED; EXACT_DIGITAL_EDITION_AND_RIGHTS_NOT_GRANTED",
            packageFileName = null, packageSha256 = null, signerFingerprint = null, releaseEligible = false
        ),
        ScriptureEditionSourceRecord(
            editionId = "AMH-UBS-1987-2005",
            sourceTitle = "UBS Amharic Version",
            publisherOrRightsHolder = "Bible Society of Ethiopia / United Bible Societies",
            publicationDetails = "First published 1987; revised edition reported 2005; verify exact title page and revision",
            canonScope = "Reported 66-book Protestant canon; primary publisher confirmation pending",
            sourceUrl = "https://ethiopiangospelmusic.net/src/bible-amharic/",
            sourceArchiveSha256 = null, license = null, attribution = null,
            distributionPermission = PENDING,
            sourceVerification = "SECONDARY_BIBLIOGRAPHIC_CORROBORATION_ONLY",
            packageFileName = null, packageSha256 = null, signerFingerprint = null, releaseEligible = false
        ),
        ScriptureEditionSourceRecord(
            editionId = "AMH-NASV-2001",
            sourceTitle = "New Amharic Standard Version (NASV)",
            publisherOrRightsHolder = "Publisher attribution not verified; Biblica appears only in secondary catalogue data",
            publicationDetails = "2001; exact edition/revision to be confirmed from title and copyright pages",
            canonScope = "Reported complete Bible; verify actual source package and canon",
            sourceUrl = "https://dev.find.bible/languages/amh/",
            sourceArchiveSha256 = null, license = null, attribution = null,
            distributionPermission = PENDING,
            sourceVerification = "TITLE_AND_YEAR_CATALOGUED; PRIMARY_PUBLISHER_CONFIRMATION_PENDING",
            packageFileName = null, packageSha256 = null, signerFingerprint = null, releaseEligible = false
        ),
        ScriptureEditionSourceRecord(
            editionId = "AMH-MILLENNIUM",
            sourceTitle = "Millennium Amharic Bible",
            publisherOrRightsHolder = null,
            publicationDetails = "Exact bibliographic identity, publication year and edition not established",
            canonScope = null, sourceUrl = null, sourceArchiveSha256 = null,
            license = null, attribution = null, distributionPermission = PENDING,
            sourceVerification = "IDENTITY_UNRESOLVED_DO_NOT_PRESENT_AS_VERIFIED_EDITION",
            packageFileName = null, packageSha256 = null, signerFingerprint = null, releaseEligible = false
        ),
        ) + ScriptureEditionCatalog.editions.filter { it.languageTag == "en" }.map { edition ->
            val pending = ScriptureEditionSourceRecord(
                editionId = edition.id,
                sourceTitle = edition.title,
                publisherOrRightsHolder = null,
                publicationDetails = edition.publicationYear ?: "Publication date not verified",
                canonScope = if (edition.fullBible) "Full-Bible candidate; exact canon/package not independently validated" else "Partial edition",
                sourceUrl = null, sourceArchiveSha256 = null, license = null, attribution = null,
                distributionPermission = PENDING, sourceVerification = UNVERIFIED,
                packageFileName = null, packageSha256 = null, signerFingerprint = null, releaseEligible = false
            )
            when (edition.id) {
                "ENG-ESV" -> pending.copy(
                    publisherOrRightsHolder = "Crossway",
                    publicationDetails = "First published 2001; cited text edition 2016",
                    sourceUrl = "https://www.esv.org/resources/esv-global-study-bible/copyright-page/"
                )
                "ENG-NIV" -> pending.copy(
                    publisherOrRightsHolder = "Biblica, Inc.",
                    publicationDetails = "Copyright notices list 1973, 1978, 1984 and 2011 editions",
                    sourceUrl = "https://partners.biblica.com/?bible-translation=new-international-version"
                )
                "ENG-NLT" -> pending.copy(
                    publisherOrRightsHolder = "Tyndale House Ministries",
                    publicationDetails = "First published 1996; second edition 2004; text updates through 2015",
                    sourceUrl = "https://sites.tyndale.com/nlt/translation-process/"
                )
                "ENG-NASB-1995" -> pending.copy(
                    publisherOrRightsHolder = "The Lockman Foundation",
                    publicationDetails = "NASB update published 1995; lineage completed 1971, updated 1977 and 1995",
                    sourceUrl = "https://www.lockman.org/new-american-standard-bible-nasb/"
                )
                "ENG-NASB-2020" -> pending.copy(
                    publisherOrRightsHolder = "The Lockman Foundation",
                    publicationDetails = "2020 update of the NASB 1995",
                    sourceUrl = "https://www.lockman.org/new-american-standard-bible-nasb/"
                )
                "ENG-NEB" -> pending.copy(
                    publisherOrRightsHolder = "Cambridge University Press and Oxford University Press",
                    publicationDetails = "New Testament 1961; complete Bible 1970",
                    sourceUrl = "https://www.cambridge.org/gb/files/5315/4504/7862/Bibles_Catalogue_UK_2019_web.pdf"
                )
                "ENG-OEB" -> pending.copy(
                    publisherOrRightsHolder = "Russell Allen / Open English Bible project",
                    publicationDetails = "Project begun 2010; release 2025.6 includes New Testament, Psalms and selected Old Testament books only",
                    canonScope = "PARTIAL: New Testament, Psalms and selected Old Testament books; not a complete Bible",
                    sourceUrl = "https://openenglishbible.org/",
                    license = "CC0 1.0",
                    distributionPermission = "PUBLIC_DOMAIN_CC0",
                    sourceVerification = "OFFICIAL_PROJECT_PAGE_CONFIRMS_CC0_AND_PARTIAL_2025_6_RELEASE"
                )
                else -> pending
            }
        }
    )

    fun find(editionId: String): ScriptureEditionSourceRecord? = records.firstOrNull { it.editionId == editionId }

    fun canImport(editionId: String): Boolean = find(editionId)?.let {
        it.distributionPermission == "AUTHORIZED_FOR_DIGITAL_DISTRIBUTION" &&
            it.sourceVerification == "VERIFIED" &&
            !it.sourceArchiveSha256.isNullOrBlank() &&
            !it.license.isNullOrBlank()
    } == true
}

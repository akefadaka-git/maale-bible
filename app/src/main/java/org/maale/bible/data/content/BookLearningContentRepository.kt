package org.maale.bible.data.content

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import org.json.JSONArray
import org.json.JSONObject

data class BookLearningStructureSection(
    val range: String,
    val heading: String,
    val summary: String
)

data class BookInductiveStudy(
    val id: String,
    val range: String,
    val title: String,
    val observation: List<String>,
    val interpretation: List<String>,
    val application: List<String>,
    val prayerPrompt: String
)

data class BookChapterGuide(
    val chapter: Int,
    val title: String,
    val summary: String,
    val observation: List<String>,
    val interpretation: List<String>,
    val application: List<String>,
    val references: List<String>,
    val literaryContext: String,
    val crossReferences: List<String>,
    val discussionPrompts: List<String>,
    val review: String
)

data class BookLearningCommentaryUnit(
    val range: String,
    val title: String,
    val note: String
)

data class BookQuizQuestion(
    val id: String,
    val type: String,
    val difficulty: String,
    val prompt: String,
    val options: List<String>,
    val correctOptionId: String?,
    val correctAnswer: Boolean?,
    val explanation: String,
    val explanationLocale: String,
    val references: List<String>
)

data class BookLearningIntroduction(
    val title: String,
    val summary: String,
    val authorship: String,
    val setting: String,
    val audienceAndPurpose: String,
    val dateAndComposition: String,
    val genre: String,
    val theologicalContribution: String,
    val canonicalConnections: List<String>,
    val structure: List<BookLearningStructureSection>,
    val themes: List<String>,
    val keyPeople: List<String>,
    val keyPassages: List<String>,
    val interpretiveCare: List<String>
)

internal data class BookLearningLocaleResolution(
    val contentLocale: String,
    val requiresFallbackNotice: Boolean
)

internal fun resolveBookLearningLocale(deviceLanguage: String, hasAmharic: Boolean = true): BookLearningLocaleResolution =
    when {
        deviceLanguage == "en" -> BookLearningLocaleResolution("en", false)
        deviceLanguage == "am" && hasAmharic -> BookLearningLocaleResolution("am", false)
        else -> BookLearningLocaleResolution("en", true)
    }

data class BookLearningContent(
    val bookId: String,
    val locale: String,
    val localeStatus: String,
    val publicationState: String,
    val introduction: BookLearningIntroduction,
    val inductiveStudies: List<BookInductiveStudy>,
    val chapterGuides: List<BookChapterGuide>,
    val quizQuestions: List<BookQuizQuestion>,
    val commentaryUnits: List<BookLearningCommentaryUnit>,
    val sourceCount: Int
) {
    val isApproved: Boolean get() = isApprovedPublicationState(publicationState)
}

internal fun isApprovedPublicationState(publicationState: String): Boolean =
    publicationState == "approved_for_release"

/**
 * Draft learning packages may be rendered only in debug builds for internal editorial review.
 * Release builds remain fail-closed until each package is explicitly approved.
 */
internal fun canDisplayBookLearningContent(publicationState: String, allowDraftPreview: Boolean): Boolean =
    isApprovedPublicationState(publicationState) ||
        (allowDraftPreview && publicationState == "draft_not_for_release")

object BookLearningContentRepository {
    /**
     * Resolve the installed target application's manifest flags from PackageManager.
     * A configuration-wrapped LocalContext may expose wrapper metadata that is not
     * reliable for the debug-preview gate; package metadata is authoritative.
     */
    private fun isDebuggableApplication(context: Context): Boolean {
        val flags = try {
            @Suppress("DEPRECATION")
            context.packageManager.getApplicationInfo(context.packageName, 0).flags
        } catch (_: PackageManager.NameNotFoundException) {
            0
        }
        return flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    }

    fun load(context: Context, bookId: String, allowDraftPreview: Boolean = isDebuggableApplication(context)): BookLearningContent? {
        val assetPath = "content/books/$bookId.json"
        val root = try {
            context.assets.open(assetPath).bufferedReader(Charsets.UTF_8).use { JSONObject(it.readText()) }
        } catch (_: Exception) {
            try {
                val collection = context.assets.open("content/books/remaining_books.json")
                    .bufferedReader(Charsets.UTF_8).use { JSONObject(it.readText()) }
                collection.optJSONObject("books")?.optJSONObject(bookId) ?: return null
            } catch (_: Exception) {
                return null
            }
        }
        val publicationState = root.optString("publication_state", "draft_not_for_release")
        // Release builds fail closed. Debug builds may preview explicitly marked drafts for review.
        if (!canDisplayBookLearningContent(publicationState, allowDraftPreview)) return null

        val availableLocales = root.optJSONObject("locales") ?: return null
        val localeResolution = resolveBookLearningLocale(
            context.resources.configuration.locales[0].language,
            hasAmharic = availableLocales.has("am")
        )
        val locale = localeResolution.contentLocale
        val localeContent = availableLocales.optJSONObject(locale) ?: return null
        val introductionJson = localeContent.optJSONObject("introduction") ?: return null
        val structure = introductionJson.optJSONArray("structure").toObjectList { item ->
            BookLearningStructureSection(
                range = item.optString("range"),
                heading = item.optString("heading"),
                summary = item.optString("summary")
            )
        }
        val studies = localeContent.optJSONArray("inductive_studies").toObjectList { item ->
            BookInductiveStudy(
                id = item.optString("id"),
                range = item.optString("range"),
                title = item.optString("title"),
                observation = item.optJSONArray("observation").toStringList(),
                interpretation = item.optJSONArray("interpretation").toStringList(),
                application = item.optJSONArray("application").toStringList(),
                prayerPrompt = item.optString("prayer_prompt")
            )
        }
        val chapterGuides = localeContent.optJSONArray("chapter_guides").toObjectList { item ->
            BookChapterGuide(
                chapter = item.optInt("chapter"),
                title = item.optString("title"),
                summary = item.optString("summary"),
                observation = item.optJSONArray("observation").toStringList(),
                interpretation = item.optJSONArray("interpretation").toStringList(),
                application = item.optJSONArray("application").toStringList(),
                references = item.optJSONArray("references").toStringList(),
                literaryContext = item.optString("literary_context"),
                crossReferences = item.optJSONArray("cross_references").toStringList(),
                discussionPrompts = item.optJSONArray("discussion_prompts").toStringList(),
                review = item.optString("review")
            )
        }
        val commentaryUnits = localeContent.optJSONObject("commentary")?.optJSONArray("units").toObjectList { item ->
            BookLearningCommentaryUnit(
                range = item.optString("range"),
                title = item.optString("title"),
                note = item.optString("note")
            )
        }
        val questions = localeContent.optJSONArray("quiz").toObjectList { item ->
            val correctAnswer = if (item.has("correct_answer") && !item.isNull("correct_answer")) {
                item.optBoolean("correct_answer")
            } else null
            BookQuizQuestion(
                id = item.optString("id"),
                type = item.optString("type"),
                difficulty = item.optString("difficulty"),
                prompt = item.optString("prompt"),
                options = item.optJSONArray("options").toStringList(),
                correctOptionId = item.optString("correct_option_id").takeIf { it.isNotBlank() },
                correctAnswer = correctAnswer,
                explanation = item.optString("explanation"),
                explanationLocale = item.optString("explanation_locale", locale).ifBlank { locale },
                references = item.optJSONArray("references").toStringList()
            )
        }
        val intro = BookLearningIntroduction(
            title = introductionJson.optString("title"),
            summary = introductionJson.optString("summary"),
            authorship = introductionJson.optString("authorship"),
            setting = introductionJson.optString("setting"),
            audienceAndPurpose = introductionJson.optString("audience_and_purpose"),
            dateAndComposition = introductionJson.optString("date_and_composition"),
            genre = introductionJson.optString("genre"),
            theologicalContribution = introductionJson.optString("theological_contribution"),
            canonicalConnections = introductionJson.optJSONArray("canonical_connections").toStringList(),
            structure = structure,
            themes = introductionJson.optJSONArray("themes").toStringList(),
            keyPeople = introductionJson.optJSONArray("key_people").toStringList(),
            keyPassages = introductionJson.optJSONArray("key_passages").toStringList(),
            interpretiveCare = introductionJson.optJSONArray("interpretive_care").toStringList()
        )
        return BookLearningContent(
            bookId = bookId,
            locale = locale,
            localeStatus = if (localeResolution.requiresFallbackNotice) "english_fallback_review_required" else localeContent.optString("locale_status", "draft"),
            publicationState = publicationState,
            introduction = intro,
            inductiveStudies = studies,
            chapterGuides = chapterGuides,
            quizQuestions = questions,
            commentaryUnits = commentaryUnits,
            sourceCount = root.optJSONArray("sources")?.length() ?: 0
        )
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return (0 until length()).mapNotNull { index -> optString(index).takeIf { it.isNotBlank() } }
    }

    private fun <T> JSONArray?.toObjectList(mapper: (JSONObject) -> T): List<T> {
        if (this == null) return emptyList()
        return (0 until length()).mapNotNull { index -> optJSONObject(index)?.let(mapper) }
    }
}

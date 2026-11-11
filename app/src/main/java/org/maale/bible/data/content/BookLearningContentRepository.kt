package org.maale.bible.data.content

import android.content.Context
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
    val application: List<String>
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
    val references: List<String>
)

data class BookLearningIntroduction(
    val title: String,
    val summary: String,
    val authorship: String,
    val setting: String,
    val audienceAndPurpose: String,
    val structure: List<BookLearningStructureSection>,
    val themes: List<String>,
    val keyPeople: List<String>,
    val keyPassages: List<String>,
    val interpretiveCare: List<String>
)

data class BookLearningContent(
    val bookId: String,
    val locale: String,
    val publicationState: String,
    val introduction: BookLearningIntroduction,
    val inductiveStudies: List<BookInductiveStudy>,
    val quizQuestions: List<BookQuizQuestion>,
    val sourceCount: Int
) {
    val isApproved: Boolean get() = publicationState == "approved_for_release"
}

object BookLearningContentRepository {
    fun load(context: Context, bookId: String): BookLearningContent? {
        val assetPath = "content/books/$bookId.json"
        val root = try {
            context.assets.open(assetPath).bufferedReader(Charsets.UTF_8).use { JSONObject(it.readText()) }
        } catch (_: Exception) {
            return null
        }
        val locale = if (context.resources.configuration.locales[0].language == "am") "am" else "en"
        val localeContent = root.optJSONObject("locales")?.optJSONObject(locale) ?: return null
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
                application = item.optJSONArray("application").toStringList()
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
                references = item.optJSONArray("references").toStringList()
            )
        }
        val intro = BookLearningIntroduction(
            title = introductionJson.optString("title"),
            summary = introductionJson.optString("summary"),
            authorship = introductionJson.optString("authorship"),
            setting = introductionJson.optString("setting"),
            audienceAndPurpose = introductionJson.optString("audience_and_purpose"),
            structure = structure,
            themes = introductionJson.optJSONArray("themes").toStringList(),
            keyPeople = introductionJson.optJSONArray("key_people").toStringList(),
            keyPassages = introductionJson.optJSONArray("key_passages").toStringList(),
            interpretiveCare = introductionJson.optJSONArray("interpretive_care").toStringList()
        )
        return BookLearningContent(
            bookId = bookId,
            locale = locale,
            publicationState = root.optString("publication_state", "draft_not_for_release"),
            introduction = intro,
            inductiveStudies = studies,
            quizQuestions = questions,
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

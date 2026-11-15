package org.maale.bible

import android.util.Log
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.setContent
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.maale.bible.data.content.BookLearningContentRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.maale.bible.MainActivity
import org.maale.bible.ui.AppLanguage
import org.maale.bible.data.repository.StudyDatabase
import org.maale.bible.data.scripture.ScriptureEditionCatalog
import org.maale.bible.data.scripture.ScriptureEditionSelectionStore

@RunWith(AndroidJUnit4::class)
class CoreStudyNavigationInstrumentedTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun waitForTag(tag: String) {
        try {
            compose.waitUntil(5_000) { compose.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }
        } catch (failure: Throwable) {
            compose.onRoot(useUnmergedTree = true).printToLog("NAV_DIAGNOSTIC_$tag")
            throw failure
        }
    }

    @Before fun selectEnglishForEnglishAssertions() {
        ScriptureEditionSelectionStore.save(compose.activity, ScriptureEditionCatalog.PRIMARY_MAALE_ID)
        AppLanguage.save(compose.activity, AppLanguage.ENGLISH)
        assertEquals("English test locale must be persisted before activity recreation", AppLanguage.ENGLISH, AppLanguage.load(compose.activity))
        compose.activityRule.scenario.recreate()
        compose.waitForIdle()
        assertEquals("Recreated activity must retain English test locale", AppLanguage.ENGLISH, AppLanguage.load(compose.activity))
        assertEquals("Recreated activity resources must use English", "en", compose.activity.resources.configuration.locales[0].language)
        val repository = StudyDatabase(compose.activity.applicationContext)
        repository.getBookmarks().forEach { repository.removeBookmark(it.id) }
        repository.close()
    }

    @Test fun bookReaderBookmarkAndStudyHubFlowIsConnected() {
        compose.onNodeWithTag("navigation_books").performClick()
        waitForTag("books_screen")
        compose.onNodeWithTag("books_list").performScrollToNode(hasTestTag("book_item_GEN"))
        compose.onNodeWithTag("book_item_GEN").performClick()
        waitForTag("book_learning_screen")
        compose.onNodeWithTag("book_learning_screen").performScrollToNode(hasTestTag("learning_open_reader_button"))
        compose.onNodeWithTag("learning_open_reader_button").assertIsDisplayed().performClick()
        waitForTag("reader_screen")
        compose.onNodeWithTag("reader_current_position").assertIsDisplayed()
        compose.onNodeWithTag("reader_screen")
            .performScrollToNode(hasTestTag("scripture_package_status"))
        compose.onNodeWithTag("scripture_package_status").assertIsDisplayed()
        compose.onNodeWithText("Selected Bible edition is not installed").assertExists()
        compose.onNodeWithTag("reader_screen")
            .performScrollToNode(hasTestTag("reader_add_bookmark_button"))
        compose.onNodeWithTag("reader_add_bookmark_button")
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
        compose.waitForIdle()

        // Query through a second repository instance so the test distinguishes a failed write
        // from a Compose state/semantics refresh or localized-text assertion failure.
        val verificationRepository = StudyDatabase(compose.activity.applicationContext)
        val persistedBookmarks = try {
            verificationRepository.getBookmarks("GEN")
        } finally {
            verificationRepository.close()
        }
        val expectedReaderCount = compose.activity.getString(R.string.reader_bookmarks, persistedBookmarks.size)
        Log.i(
            "R6_DIAGNOSTIC",
            "stage=reader; persistedCount=${persistedBookmarks.size}; bookIds=${persistedBookmarks.map { it.bookId }}; " +
                "savedLanguage=${AppLanguage.load(compose.activity).tag}; activityLocales=${compose.activity.resources.configuration.locales.toLanguageTags()}; " +
                "expectedResourceText=$expectedReaderCount"
        )
        assertEquals("R6 database check: exactly one Genesis bookmark must persist after the reader action", 1, persistedBookmarks.size)

        try {
            compose.onNodeWithText("Bookmarks (1)").assertExists()
        } catch (failure: Throwable) {
            compose.onRoot(useUnmergedTree = true).printToLog("R6_READER_AFTER_BOOKMARK")
            Log.e("R6_DIAGNOSTIC", "Reader bookmark count is persisted but expected Compose text was not found", failure)
            throw failure
        }

        compose.onNodeWithTag("navigation_more").performClick()
        waitForTag("more_screen")
        compose.onNodeWithTag("more_study_tab").performClick()
        waitForTag("study_hub_list")
        compose.onNodeWithTag("study_hub_list").performScrollToNode(hasText("Bookmarks (1)"))
        compose.onNodeWithTag("study_bookmarks_count").assertIsDisplayed()
        compose.onNodeWithText("Bookmarks (1)").assertIsDisplayed()
        compose.onNodeWithTag("study_hub_list").performScrollToNode(hasText("Genesis"))
        compose.onNodeWithText("Genesis").assertIsDisplayed()
    }

    @Test fun bookLearningCentrePreviewsDraftIntroductionAndQuizInDebugBuild() {
        val debugContent = BookLearningContentRepository.load(compose.activity, "GEN")
        assertNotNull("Genesis learning package must load in the debug test build", debugContent)
        assertEquals("Genesis content must follow the English test locale", "en", debugContent?.locale)
        assertEquals("Genesis quiz must expose its first question", "GEN-Q01", debugContent?.quizQuestions?.firstOrNull()?.id)
        assertEquals("Genesis quiz must load 110 questions", 110, debugContent?.quizQuestions?.size)
        assertEquals("Genesis quiz must load four multiple-select questions", 4, debugContent?.quizQuestions?.count { it.type == "multiple_select" })
        assertEquals("Genesis quiz must load four short-answer questions", 4, debugContent?.quizQuestions?.count { it.type == "short_answer" })
        assertEquals("Genesis chapter guides must expose chapter one", 1, debugContent?.chapterGuides?.firstOrNull()?.chapter)
        assertEquals("Genesis commentary units must load for the selected locale", 6, debugContent?.commentaryUnits?.size)
        assertEquals("Genesis introduction must expose composition context", true, debugContent?.introduction?.dateAndComposition?.isNotBlank())
        assertEquals("Genesis introduction must expose genre", true, debugContent?.introduction?.genre?.isNotBlank())
        assertEquals("Genesis introduction must expose canonical connections", true, debugContent?.introduction?.canonicalConnections?.isNotEmpty())
        assertEquals("Genesis chapter guide must expose literary context", true, debugContent?.chapterGuides?.firstOrNull()?.literaryContext?.isNotBlank())
        assertEquals("Genesis inductive studies must expose prayer prompts", true, debugContent?.inductiveStudies?.all { it.prayerPrompt.isNotBlank() })
        compose.onNodeWithTag("navigation_books").performClick()
        waitForTag("books_screen")
        compose.onNodeWithTag("books_list").performScrollToNode(hasTestTag("book_item_GEN"))
        compose.onNodeWithTag("book_item_GEN").performClick()
        waitForTag("book_learning_screen")
        compose.onNodeWithText("INTERNAL DRAFT — not approved for release. English editorial review and Amharic language review are still required.").assertExists()
        compose.onNodeWithText("Genesis — beginnings, humanity and the family of Israel").assertExists()
        compose.onNodeWithText("Date and composition").assertExists()
        assertEquals("Commentary notes must be rendered in the overview", true, compose.onAllNodesWithText("Commentary and theological notes").fetchSemanticsNodes().isNotEmpty())
        compose.onNodeWithTag("book_learning_screen").performScrollToNode(hasTestTag("learning_tab_quiz"))
        compose.onNodeWithTag("learning_tab_inductive").performClick()
        compose.waitForIdle()
        assertEquals("Chapter context label must render", true, compose.onAllNodesWithText("Literary context").fetchSemanticsNodes().isNotEmpty())
        assertEquals("Prayer prompt label must render", true, compose.onAllNodesWithText("Prayer prompt").fetchSemanticsNodes().isNotEmpty())
        compose.onNodeWithTag("book_learning_screen").performScrollToNode(hasTestTag("learning_tab_quiz"))
        compose.onNodeWithTag("learning_tab_quiz").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("According to Genesis 1, what does the opening chapter present God doing?").assertExists()
    }

    @Test fun multipleSelectQuestionRendersAndReturnsSelectedOptionIds() {
        var selected = ""
        val question = org.maale.bible.data.content.BookQuizQuestion(
            id = "test-multi", type = "multiple_select", difficulty = "medium", prompt = "Select all valid details",
            options = listOf("First", "Second", "Third", "Fourth"), correctOptionId = null, correctOptionIds = listOf("A", "C"), correctAnswer = null,
            explanation = "The first and third details are supported.", explanationLocale = "en", references = listOf("GEN 9:8-17")
        )
        compose.setContent {
            org.maale.bible.ui.QuizQuestionCard(
                question = question, selectedAnswer = selected, submitted = false,
                showExplanationFallback = false, onSelect = { selected = it }, onSubmit = {}
            )
        }
        compose.onNodeWithText("Select every correct option").assertExists()
        compose.onNodeWithTag("learning_quiz_option_A").performClick()
        assertEquals("A", selected)
    }

    @Test fun shortAnswerQuestionAcceptsTypedText() {
        var typed = ""
        val question = org.maale.bible.data.content.BookQuizQuestion(
            id = "test-short", type = "short_answer", difficulty = "medium", prompt = "Name the second son",
            options = emptyList(), correctOptionId = null, acceptedAnswers = listOf("Ephraim"), correctAnswer = null,
            explanation = "Ephraim is the second son.", explanationLocale = "en", references = listOf("GEN 41:50-52")
        )
        compose.setContent {
            org.maale.bible.ui.QuizQuestionCard(
                question = question, selectedAnswer = typed, submitted = false,
                showExplanationFallback = false, onSelect = { typed = it }, onSubmit = {}
            )
        }
        compose.onNodeWithTag("learning_quiz_short_answer").performTextInput("Ephraim")
        assertEquals("Ephraim", typed)
    }

    @Test fun quizSupportsAnswerLockingProgressResultsAndRetry() {
        compose.onNodeWithTag("navigation_books").performClick()
        waitForTag("books_screen")
        compose.onNodeWithTag("books_list").performScrollToNode(hasTestTag("book_item_GEN"))
        compose.onNodeWithTag("book_item_GEN").performClick()
        waitForTag("book_learning_screen")
        compose.onNodeWithTag("book_learning_screen").performScrollToNode(hasTestTag("learning_tab_quiz"))
        compose.onNodeWithTag("learning_tab_quiz").performClick()
        compose.waitForIdle()
        compose.onNodeWithTag("book_learning_screen").performScrollToNode(hasTestTag("learning_quiz_question_GEN-Q01"))

        repeat(8) {
            val trueFalseAvailable = compose.onAllNodesWithTag("learning_quiz_option_true").fetchSemanticsNodes().isNotEmpty()
            if (trueFalseAvailable) {
                compose.onNodeWithTag("learning_quiz_option_true").performClick()
            } else {
                compose.onNodeWithTag("learning_quiz_option_A").performClick()
            }
            compose.onNodeWithTag("learning_quiz_submit").performClick()
            compose.onNodeWithTag("book_learning_screen").performScrollToNode(hasTestTag("learning_quiz_next"))
            compose.onNodeWithTag("learning_quiz_next").performClick()
            compose.waitForIdle()
        }

        compose.onNodeWithTag("book_learning_screen").performScrollToNode(hasTestTag("learning_quiz_results"))
        compose.onNodeWithTag("learning_quiz_results").assertIsDisplayed()
        compose.onNodeWithTag("learning_quiz_retry").performClick()
        compose.onNodeWithTag("book_learning_screen").performScrollToNode(hasTestTag("learning_quiz_question_GEN-Q01"))
        compose.onNodeWithTag("learning_quiz_question_GEN-Q01").assertIsDisplayed()
    }

    @Test fun genesisChapterGuidesRenderFromFirstThroughFiftiethChapter() {
        compose.onNodeWithTag("navigation_books").performClick()
        waitForTag("books_screen")
        compose.onNodeWithTag("books_list").performScrollToNode(hasTestTag("book_item_GEN"))
        compose.onNodeWithTag("book_item_GEN").performClick()
        waitForTag("book_learning_screen")
        compose.onNodeWithTag("book_learning_screen").performScrollToNode(hasTestTag("learning_tab_inductive"))
        compose.onNodeWithTag("learning_tab_inductive").performClick()
        compose.waitForIdle()
        compose.onNodeWithTag("book_learning_screen").performScrollToNode(hasTestTag("learning_chapter_guide_1"))
        compose.onNodeWithTag("learning_chapter_guide_1").assertIsDisplayed()
        compose.onNodeWithTag("book_learning_screen").performScrollToNode(hasTestTag("learning_chapter_guide_50"))
        compose.onNodeWithTag("learning_chapter_guide_50").assertIsDisplayed()
    }

    @Test fun catalogueSearchFindsBooksWithoutScriptureTextSearch() {
        compose.onNodeWithText("Search").performClick()
        compose.onNodeWithTag("catalogue_search_input").performTextInput("Genesis")
        compose.onNodeWithTag("catalogue_result_GEN").assertExists()
    }



    @Test fun editionSelectionPersistsAcrossActivityRecreationWithoutMaaleFallback() {
        try {
            compose.onNodeWithTag("navigation_books").performClick()
            waitForTag("books_screen")
            compose.onNodeWithTag("books_list").performScrollToNode(hasTestTag("book_item_GEN"))
            compose.onNodeWithTag("book_item_GEN").performClick()
            waitForTag("book_learning_screen")
            compose.onNodeWithTag("book_learning_screen")
                .performScrollToNode(hasTestTag("learning_open_reader_button"))
            compose.onNodeWithTag("learning_open_reader_button").performClick()
            waitForTag("reader_screen")

            compose.onNodeWithTag("reader_edition_selector").performClick()
            compose.onNodeWithTag("reader_edition_selector_search").performTextInput("NIV")
            compose.onNodeWithTag("reader_edition_ENG-NIV").assertIsDisplayed().assertHasClickAction().performClick()
            compose.waitForIdle()
            val selectedAfterTap = ScriptureEditionSelectionStore.load(compose.activity.applicationContext)
            Log.i("EDITION_SELECTION_DIAGNOSTIC", "selectedAfterTap=$selectedAfterTap; expected=ENG-NIV")
            if (selectedAfterTap != "ENG-NIV") {
                compose.onRoot(useUnmergedTree = true).printToLog("EDITION_SELECTION_AFTER_TAP")
            }
            assertEquals("Selecting NIV must synchronously persist the shared edition preference", "ENG-NIV", selectedAfterTap)
            compose.onNodeWithTag("reader_edition_selector").assert(hasText("English: New International Version"))

            compose.activityRule.scenario.recreate()
            compose.waitForIdle()
            assertEquals("ENG-NIV", ScriptureEditionSelectionStore.load(compose.activity.applicationContext))
            compose.onNodeWithTag("reader_edition_selector").assert(hasText("English: New International Version"))
            compose.onNodeWithTag("reader_screen").performScrollToNode(hasTestTag("scripture_package_status"))
            compose.onNodeWithTag("scripture_package_status").assertIsDisplayed()
        } finally {
            ScriptureEditionSelectionStore.save(
                compose.activity.applicationContext,
                ScriptureEditionCatalog.PRIMARY_MAALE_ID
            )
            compose.activityRule.scenario.recreate()
            compose.waitForIdle()
        }
    }

    @Test fun notificationMenuHasAccessibleActionAndHonestEmptyState() {
        compose.onNodeWithTag("notifications_button").assertHasClickAction().performClick()
        compose.onNodeWithText("You are all caught up. No new notifications.").assertIsDisplayed()
    }

    @Test fun profileMenuClearlyIdentifiesLocalGuestMode() {
        compose.onNodeWithTag("profile_button").assertHasClickAction().performClick()
        compose.onNodeWithText("Guest reader · local-only").assertIsDisplayed()
        compose.onNodeWithTag("profile_language_item").assertIsDisplayed()
    }

    @Test fun studyDashboardRendersCalendarChartAndMapDisclosureAndScrolls() {
        compose.onNodeWithTag("navigation_more").performClick()
        waitForTag("more_screen")
        compose.onNodeWithTag("more_study_tab").performClick()
        waitForTag("study_hub_list")
        compose.onNodeWithTag("study_hub_list").performScrollToNode(hasTestTag("study_week_context"))
        compose.onNodeWithTag("study_week_context").assertIsDisplayed()
        compose.onNodeWithTag("study_hub_list").performScrollToNode(hasTestTag("study_insights_chart"))
        compose.onNodeWithTag("study_insights_chart").assertIsDisplayed()
        compose.onNodeWithTag("study_hub_list").performScrollToNode(hasTestTag("study_places_preview"))
        compose.onNodeWithTag("study_places_preview").assertIsDisplayed()
        compose.onNodeWithTag("study_hub_list").performScrollToNode(hasTestTag("study_annotations_count"))
        compose.onNodeWithTag("study_annotations_count").assertIsDisplayed()
    }

    @Test fun compactNavigationIsVisibleAndInteractive() {
        compose.onNodeWithTag("navigation_compact").assertIsDisplayed()
        compose.onNodeWithTag("navigation_books").performClick()
        waitForTag("books_screen")
        compose.onNodeWithTag("books_list").performScrollToNode(hasTestTag("book_item_GEN"))
        compose.onNodeWithTag("book_item_GEN").assertIsDisplayed()
    }

    @Test fun settingsExposeAccessibilityAndPortableBackupControls() {
        compose.onNodeWithTag("navigation_more").performClick()
        waitForTag("more_screen")
        compose.onNodeWithTag("more_settings_tab").performClick()
        waitForTag("settings_screen")
        compose.onNodeWithTag("settings_accessibility_section").assertIsDisplayed()
        compose.onNodeWithText("Create encrypted backup").assertExists()
        compose.onNodeWithText("Restore backup").assertExists()
    }
}

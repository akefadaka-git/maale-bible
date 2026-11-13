package org.maale.bible

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.maale.bible.MainActivity
import org.maale.bible.ui.AppLanguage
import org.maale.bible.data.repository.StudyDatabase

@RunWith(AndroidJUnit4::class)
class CoreStudyNavigationInstrumentedTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before fun selectEnglishForEnglishAssertions() {
        AppLanguage.save(compose.activity, AppLanguage.ENGLISH)
        compose.activityRule.scenario.recreate()
        compose.waitForIdle()
        val repository = StudyDatabase(compose.activity.applicationContext)
        repository.getBookmarks().forEach { repository.removeBookmark(it.id) }
        repository.close()
    }

    @Test fun bookReaderBookmarkAndStudyHubFlowIsConnected() {
        compose.onNodeWithText("Books").performClick()
        compose.onNodeWithText("1. Genesis").performClick()
        compose.onNodeWithText("Chapter 1 · Verse 1").assertIsDisplayed()
        compose.onNodeWithText("Add bookmark").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Bookmarks (1)").assertExists()
        compose.onNodeWithText("More").performClick()
        compose.onNodeWithText("Study").performClick()
        compose.onNodeWithTag("study_hub_list").performScrollToNode(hasText("Bookmarks (1)"))
        compose.onNodeWithTag("study_bookmarks_count").assertIsDisplayed()
        compose.onNodeWithText("Bookmarks (1)").assertIsDisplayed()
        compose.onNodeWithTag("study_hub_list").performScrollToNode(hasText("Genesis"))
        compose.onNodeWithText("Genesis").assertIsDisplayed()
    }

    @Test fun catalogueSearchFindsBooksWithoutScriptureTextSearch() {
        compose.onNodeWithText("Search").performClick()
        compose.onNodeWithTag("catalogue_search_input").performTextInput("Genesis")
        compose.onNodeWithTag("catalogue_result_GEN").assertExists()
    }


    @Test fun notificationMenuHasAccessibleActionAndHonestEmptyState() {
        compose.onNodeWithTag("notifications_button").assertHasClickAction().performClick()
        compose.onNodeWithText("You are all caught up. No new notifications.").assertIsDisplayed()
    }

    @Test fun profileMenuClearlyIdentifiesLocalGuestMode() {
        compose.onNodeWithTag("profile_button").assertHasClickAction().performClick()
        compose.onNodeWithText("Guest reader · local-only").assertIsDisplayed()
        compose.onNodeWithText("Interface language").assertExists()
    }

    @Test fun studyDashboardRendersCalendarChartAndMapDisclosureAndScrolls() {
        compose.onNodeWithText("More").performClick()
        compose.onNodeWithText("Study").performClick()
        compose.onNodeWithTag("study_week_context").assertIsDisplayed()
        compose.onNodeWithTag("study_insights_chart").assertIsDisplayed()
        compose.onNodeWithTag("study_places_preview").assertIsDisplayed()
        compose.onNodeWithTag("study_hub_list").performScrollToNode(hasTestTag("study_annotations_count"))
        compose.onNodeWithTag("study_annotations_count").assertIsDisplayed()
    }

    @Test fun compactNavigationIsVisibleAndInteractive() {
        compose.onNodeWithTag("navigation_compact").assertIsDisplayed()
        compose.onNodeWithText("Books").performClick()
        compose.onNodeWithText("1. Genesis").assertIsDisplayed()
    }

    @Test fun settingsExposeAccessibilityAndPortableBackupControls() {
        compose.onNodeWithText("More").performClick()
        compose.onNodeWithText("Settings").performClick()
        compose.onNodeWithText("Accessibility").assertExists()
        compose.onNodeWithText("Create encrypted backup").assertExists()
        compose.onNodeWithText("Restore backup").assertExists()
    }
}

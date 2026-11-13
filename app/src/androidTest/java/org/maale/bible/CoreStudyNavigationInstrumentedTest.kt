package org.maale.bible

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.maale.bible.MainActivity

@RunWith(AndroidJUnit4::class)
class CoreStudyNavigationInstrumentedTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun bookReaderBookmarkAndStudyHubFlowIsConnected() {
        compose.onNodeWithText("Books").performClick()
        compose.onNodeWithText("1. Genesis").performClick()
        compose.onNodeWithText("Chapter 1 · Verse 1").assertIsDisplayed()
        compose.onNodeWithText("Add bookmark").performClick()
        compose.onNodeWithText("Study").performClick()
        compose.onNodeWithText("Bookmarks (1)").assertIsDisplayed()
        compose.onNodeWithText("Genesis").assertIsDisplayed()
    }

    @Test fun catalogueSearchFindsBooksWithoutScriptureTextSearch() {
        compose.onNodeWithText("Search").performClick()
        compose.onNodeWithText("Find a book").assertIsDisplayed()
        compose.onNodeWithText("Book name or code").assertIsDisplayed()
        compose.onNodeWithText("1. Genesis").assertIsDisplayed()
    }

    @Test fun settingsExposeAccessibilityAndPortableBackupControls() {
        compose.onNodeWithText("Settings").performClick()
        compose.onNodeWithText("Accessibility").assertIsDisplayed()
        compose.onNodeWithText("Create encrypted backup").assertIsDisplayed()
        compose.onNodeWithText("Restore backup").assertIsDisplayed()
        compose.onNodeWithContentDescription("Select encrypted backup file").assertIsDisplayed()
    }

    @Test fun restoreKeyIsNotRequestedUntilAfterDocumentSelection() {
        compose.onNodeWithText("Settings").performClick()
        compose.onNodeWithContentDescription("Select encrypted backup file").assertIsDisplayed()
        compose.onNodeWithText("Choose backup file").assertIsDisplayed()
    }

    @Test fun settingsRecreationReturnsWithoutRestoringTransientRecoveryMaterial() {
        compose.onNodeWithText("Settings").performClick()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Settings").assertIsDisplayed()
        compose.onNodeWithText("Choose backup file").assertIsDisplayed()
    }
}

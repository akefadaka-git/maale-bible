package org.maale.bible

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
        compose.onNodeWithText("Bookmarks (1)").assertExists()
        compose.onNodeWithText("Genesis").assertExists()
    }

    @Test fun catalogueSearchFindsBooksWithoutScriptureTextSearch() {
        compose.onNodeWithText("Search").performClick()
        compose.onNodeWithText("Find a book").assertExists()
        compose.onNodeWithText("Book name or code").assertExists()
        compose.onNodeWithText("1. Genesis").assertExists()
    }

    @Test fun settingsExposeAccessibilityAndPortableBackupControls() {
        compose.onNodeWithText("Settings").performClick()
        compose.onNodeWithText("Accessibility").assertExists()
        compose.onNodeWithText("Create encrypted backup").assertExists()
        compose.onNodeWithText("Restore backup").assertExists()
    }
}

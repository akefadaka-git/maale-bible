package org.maale.bible

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MaaleBibleNavigationInstrumentedTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test fun homeBooksCatalogueSearchAndReaderNavigationWork() {
        compose.onNodeWithText("Maale Bible").assertExists()
        compose.onNodeWithText("Browse 66 books").performClick()
        compose.onNodeWithText("Books of the Maale Bible").assertExists()
        compose.onNodeWithText("1. Genesis").performClick()
        compose.onNodeWithText("Reader coordinates — scripture content is not included").assertExists()
        compose.onNodeWithText("Back to books").performClick()
        compose.onNodeWithText("Search").performClick()
        compose.onNodeWithText("Search the catalogue").assertExists()
        compose.onNodeWithText("Book name or code").performTextInput("Genesis")
        compose.onNodeWithText("1 books found").assertExists()
        compose.onNodeWithText("1. Genesis").performClick()
        compose.onNodeWithText("Reader coordinates — scripture content is not included").assertExists()
    }

    @Test fun searchEmptyStateIsExplicitAndAccessible() {
        compose.onNodeWithText("Search").performClick()
        compose.onNodeWithText("Book name or code").performTextInput("not-a-book")
        compose.onNodeWithText("No books match “not-a-book”").assertExists()
        compose.onNodeWithText("Try an English book name or a three-character USFM code.").assertExists()
    }
}

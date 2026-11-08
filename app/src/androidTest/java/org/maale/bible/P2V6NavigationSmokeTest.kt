package org.maale.bible

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.maale.bible.MainActivity

class P2V6NavigationSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test fun homeBooksReaderBackAndSearchDestinationsRemainReachable() {
        composeRule.onNodeWithText("ጌኤዦ ማፃኣፖ").assertIsDisplayed()
        composeRule.onNodeWithText("Browse 66 books").performClick()
        composeRule.onNodeWithText("Books of the Maale Bible").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Open Genesis").performClick()
        composeRule.onNodeWithText("Genesis").assertIsDisplayed()
        composeRule.onNodeWithText("Back to books").performClick()
        composeRule.onNodeWithText("Books of the Maale Bible").assertIsDisplayed()
        composeRule.onAllNodesWithText("Search").last().performClick()
        composeRule.onNodeWithText("Offline search becomes available when the separately approved local scripture dataset is integrated.")
            .assertExists()
    }
}

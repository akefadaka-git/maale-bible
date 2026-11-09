package org.maale.bible

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class P2V6NavigationSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeBooksReaderBackAndSearchDestinationsRemainReachable() {
        composeRule.onNodeWithText("ጌኤዦ ማፃኣፖ").assertIsDisplayed()

        // Match the current HomeScreen CTA rather than the retired "Browse 66 books" label.
        composeRule.onNodeWithText("Explore the Bible").performClick()
        composeRule.onNodeWithText("Books of the Maale Bible").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("Open Genesis").performClick()
        composeRule.onNodeWithText("Genesis").assertIsDisplayed()

        // ReaderPlaceholderScreen currently exposes its back action as the arrow icon.
        composeRule.onNodeWithText("←").performClick()
        composeRule.onNodeWithText("Books of the Maale Bible").assertIsDisplayed()

        // The current Search destination presents the catalogue-search capability state.
        composeRule.onNodeWithText("Search").performClick()
        composeRule.onNodeWithText("Search with purpose.").assertIsDisplayed()
    }
}

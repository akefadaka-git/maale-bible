package org.maale.bible

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class P2V6NavigationSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun resetLanguagePreference() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("maale_bible_preferences", Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    @Test
    fun languageSelectionAppliesAmharicAndEnglishInterfaceResources() {
        composeRule.onNodeWithTag("language_selector").performClick()
        composeRule.onNodeWithTag("language_maale").performClick()
        composeRule.onNodeWithTag("language_selector").performClick()
        composeRule.onNodeWithTag("language_amharic").performClick()
        composeRule.onNodeWithText("የማዓሌ መጽሐፍ ቅዱስ").assertIsDisplayed()
        composeRule.onNodeWithText("መነሻ").assertIsDisplayed()

        composeRule.onNodeWithTag("language_selector").performClick()
        composeRule.onNodeWithTag("language_english").performClick()
        composeRule.onNodeWithText("Maale Bible").assertIsDisplayed()
        composeRule.onNodeWithText("Home").assertIsDisplayed()
    }

    @Test
    fun accountEntryDoesNotBlockGuestReadingOrClaimAuthentication() {
        composeRule.onNodeWithTag("language_selector").performClick()
        composeRule.onNodeWithTag("language_english").performClick()
        composeRule.onNodeWithText("Account").performClick()
        composeRule.onNodeWithText("Your account").assertIsDisplayed()
        composeRule.onNodeWithText("Account service not connected").assertIsDisplayed()
        composeRule.onNodeWithText("Continue reading as guest").performClick()
        composeRule.onNodeWithText("Explore the Bible").assertIsDisplayed()
    }

    @Test
    fun homeBooksReaderBackAndSearchDestinationsRemainReachable() {
        composeRule.onNodeWithTag("language_selector").performClick()
        composeRule.onNodeWithTag("language_english").performClick()
        composeRule.onNodeWithText("ጌኤዦ ማፃኣፖ").assertIsDisplayed()
        composeRule.onNodeWithText("Explore the Bible").performClick()
        composeRule.onNodeWithText("Books of the Maale Bible").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("Open Genesis").performClick()

        // The reader intentionally displays "Genesis" in both its top bar and identity block.
        // Assert the unique canonical identity instead of an ambiguous generic text selector.
        composeRule.onNodeWithText("CANONICAL BOOK ID  •  GEN").assertIsDisplayed()
        composeRule.onNodeWithText(
            "The verified Maale scripture text is not available in this build yet."
        ).assertIsDisplayed()

        composeRule.onNodeWithText("←").performClick()
        composeRule.onNodeWithText("Books of the Maale Bible").assertIsDisplayed()

        composeRule.onNodeWithText("Search").performClick()
        composeRule.onNodeWithText("Search with purpose.").assertIsDisplayed()
    }
}

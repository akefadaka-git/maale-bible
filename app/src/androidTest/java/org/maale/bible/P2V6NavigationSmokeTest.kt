package org.maale.bible

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
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
        composeRule.onNodeWithText("Interface language").performClick()
        composeRule.onNodeWithText("Amharic").performClick()
        composeRule.onNodeWithText("የማዓሌ መጽሐፍ ቅዱስ").assertIsDisplayed()
        composeRule.onNodeWithText("መነሻ").assertIsDisplayed()

        composeRule.onNodeWithText("የመተግበሪያ ቋንቋ").performClick()
        composeRule.onNodeWithText("እንግሊዝኛ").performClick()
        composeRule.onNodeWithText("Maale Bible").assertIsDisplayed()
        composeRule.onNodeWithText("Home").assertIsDisplayed()
    }

    @Test
    fun homeBooksReaderBackAndSearchDestinationsRemainReachable() {
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

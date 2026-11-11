package org.maale.bible

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
        composeRule.onNodeWithText("የማሌ መጽሐፍ ቅዱስ").assertIsDisplayed()
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
        composeRule.onNodeWithText("Continue reading as guest").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Explore the Bible").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun homeBooksReaderBackAndSearchDestinationsRemainReachable() {
        composeRule.onNodeWithTag("language_selector").performClick()
        composeRule.onNodeWithTag("language_english").performClick()
        composeRule.onNodeWithText("ጌኤዦ ማፃኣፖ").assertIsDisplayed()
        composeRule.onNodeWithText("Explore the Bible").performClick()
        composeRule.onNodeWithText("Books of the Maale Bible").assertIsDisplayed()
        composeRule.onNodeWithText("Old Testament").assertIsDisplayed()
        composeRule.onNodeWithText("New Testament").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("Open Genesis").performClick()

        composeRule.onNodeWithText("Bible book learning centre").assertIsDisplayed()
        composeRule.onNodeWithText("Introduction").assertIsDisplayed()
        composeRule.onNodeWithText("Inductive study").assertIsDisplayed()
        composeRule.onNodeWithText("Quiz").assertIsDisplayed()

        composeRule.onNodeWithText("Open this book in the Bible reader").performScrollTo().performClick()
        composeRule.onNodeWithText("Canonical book ID • GEN").assertIsDisplayed()
        composeRule.onNodeWithText(
            "Verified Maale scripture text is not available in this build yet."
        ).assertIsDisplayed()

        composeRule.onNodeWithText("←").performClick()
        composeRule.onNodeWithText("‹  All books").performClick()
        composeRule.onNodeWithText("Books of the Maale Bible").assertIsDisplayed()

        composeRule.onNodeWithText("Search").performClick()
        composeRule.onNodeWithText("Search with purpose.").assertIsDisplayed()
    }
}

    @Test
    fun bookLearningCentreUsesAmharicAndEnglishResources() {
        composeRule.onNodeWithTag("language_selector").performClick()
        composeRule.onNodeWithTag("language_amharic").performClick()
        composeRule.onNodeWithText("መጻሕፍት").performClick()
        composeRule.onNodeWithContentDescription("Open Genesis").performClick()
        composeRule.onNodeWithText("የመጽሐፍ ቅዱስ መጽሐፍ የመማሪያ ማዕከል").assertIsDisplayed()
        composeRule.onNodeWithText("መግቢያ").assertIsDisplayed()

        composeRule.onNodeWithTag("language_selector").performClick()
        composeRule.onNodeWithTag("language_english").performClick()
        composeRule.onNodeWithText("Bible book learning centre").assertIsDisplayed()
        composeRule.onNodeWithText("Introduction").assertIsDisplayed()
    }

package org.maale.bible

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class P2V6NavigationSmokeTest {
    private fun mark(step: String) = Log.i("P2V6NavigationSmokeTest", step)

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

    @Test(timeout = 120_000)
    fun homeBooksReaderBackAndSearchDestinationsRemainReachable() {
        mark("nav-01-open-language-selector")
        composeRule.onNodeWithTag("language_selector").performClick()
        mark("nav-02-select-english")
        composeRule.onNodeWithTag("language_english").performClick()
        mark("nav-03-assert-home-title")
        composeRule.onNodeWithText("ጌኤዦ ማፃኣፖ").assertIsDisplayed()
        mark("nav-04-open-books")
        composeRule.onNodeWithText("Explore the Bible").performClick()
        mark("nav-05-assert-books-title")
        composeRule.onNodeWithText("Books of the Maale Bible").assertIsDisplayed()
        mark("nav-06-assert-old-testament")
        composeRule.onNodeWithText("Old Testament").assertIsDisplayed()
        mark("nav-07-assert-new-testament")
        composeRule.onNodeWithText("New Testament").assertIsDisplayed()

        mark("nav-08-scroll-genesis-into-view")
        composeRule.onNodeWithTag("books_list").performScrollToNode(hasContentDescription("Open Genesis"))
        mark("nav-09-open-genesis")
        composeRule.onNodeWithContentDescription("Open Genesis").performClick()

        mark("nav-10-assert-learning-centre")
        composeRule.onNodeWithText("Bible book learning centre").assertIsDisplayed()
        composeRule.onNodeWithText("Introduction").assertIsDisplayed()
        composeRule.onNodeWithText("Inductive study").assertIsDisplayed()
        composeRule.onNodeWithText("Quiz").assertIsDisplayed()

        mark("nav-11-open-reader")
        composeRule.onNodeWithText("Open this book in the Bible reader").performScrollTo().performClick()
        mark("nav-12-assert-reader-id")
        composeRule.onNodeWithText("Canonical book ID • GEN").assertIsDisplayed()
        composeRule.onNodeWithText(
            "Verified Maale scripture text is not available in this build yet."
        ).performScrollTo().assertIsDisplayed()

        mark("nav-13-reader-back")
        composeRule.onNodeWithText("←").performClick()
        mark("nav-14-learning-back-to-books")
        composeRule.onNodeWithText("‹  All books").performClick()
        composeRule.onNodeWithText("Books of the Maale Bible").assertIsDisplayed()

        mark("nav-15-open-search")
        composeRule.onNodeWithText("Search").performClick()
        mark("nav-16-assert-search")
        composeRule.onNodeWithText("Search with purpose.").assertIsDisplayed()
    }

    @Test
    fun bookLearningCentreUsesAmharicAndEnglishResources() {
        composeRule.onNodeWithTag("language_selector").performClick()
        composeRule.onNodeWithTag("language_amharic").performClick()
        composeRule.onNodeWithText("መጻሕፍት").performClick()
        composeRule.onNodeWithContentDescription("Open Genesis").performClick()
        composeRule.onNodeWithText("የመጽሐፍ ቅዱስ መጽሐፍ የመማሪያ ማዕከል").assertIsDisplayed()
        composeRule.onNodeWithText("መግቢያ").assertIsDisplayed()
        composeRule.onNodeWithText("የውስጥ ረቂቅ ነው — ለህትመት አልጸደቀም። የእንግሊዝኛ አርትዖትና የአማርኛ ቋንቋ ግምገማ ገና ያስፈልጋሉ።").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("ፈተና").performClick()
        composeRule.onNodeWithText("በዘፍጥረት 1 መሠረት የመጀመሪያው ምዕራፍ እግዚአብሔር ምን እንዳደረገ ያቀርባል?").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("ሰማይንና ምድርን መፍጠርና ሥርዓት ማስያዝ").performScrollTo().performClick()
        composeRule.onNodeWithText("መልሱን አረጋግጥ").performScrollTo().performClick()
        composeRule.onNodeWithText("ትክክል").assertIsDisplayed()

        composeRule.onNodeWithTag("language_selector").performClick()
        composeRule.onNodeWithTag("language_english").performClick()
        composeRule.onNodeWithText("Bible book learning centre").assertIsDisplayed()
        composeRule.onNodeWithText("Introduction").assertIsDisplayed()
        composeRule.onNodeWithText("Quiz").performClick()
        composeRule.onNodeWithText("According to Genesis 1, what does the opening chapter present God doing?").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Creating and ordering the heavens and the earth").performScrollTo().performClick()
        composeRule.onNodeWithText("Check answer").performScrollTo().performClick()
        composeRule.onNodeWithText("Correct").assertIsDisplayed()
    }

}

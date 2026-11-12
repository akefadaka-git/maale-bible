package org.maale.bible

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class P2V6NavigationSmokeTest {
    private fun mark(step: String) = Log.i("P2V6NavigationSmokeTest", step)

    private fun awaitText(text: String, timeoutMillis: Long = 10_000L) {
        composeRule.waitUntil(timeoutMillis) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun selectEnglish() {
        composeRule.onNodeWithTag("language_selector").performClick()
        composeRule.onNodeWithTag("language_english").performClick()
    }

    private fun openGenesisLearningCentre() {
        awaitText("Explore the Bible")
        composeRule.onNodeWithText("Explore the Bible").performClick()
        awaitText("Books of the Maale Bible")
        composeRule.onNodeWithTag("books_search").performTextInput("GEN")
        composeRule.onNodeWithContentDescription("Open Genesis").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("book_card_GEN").performClick()
        awaitText("Bible book learning centre")
    }

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

    @Test(timeout = 60_000)
    fun homeBooksAndGenesisLearningCentreAreReachable() {
        mark("nav-01-select-english")
        selectEnglish()
        mark("nav-02-assert-home")
        awaitText("ጌኤዦ ማፃኣፖ")
        composeRule.onNodeWithText("ጌኤዦ ማፃኣፖ").assertIsDisplayed()
        mark("nav-03-open-books")
        openGenesisLearningCentre()
        mark("nav-04-assert-testament-navigation")
        awaitText("Old Testament")
        composeRule.onNodeWithText("Old Testament").assertIsDisplayed()
        composeRule.onNodeWithText("New Testament").assertIsDisplayed()
        composeRule.onNodeWithText("Introduction").assertIsDisplayed()
        composeRule.onNodeWithText("Inductive study").assertIsDisplayed()
        composeRule.onNodeWithText("Quiz").assertIsDisplayed()
    }

    @Test(timeout = 60_000)
    fun genesisLearningCentreReaderAndBackNavigationAreReachable() {
        mark("reader-01-select-english")
        selectEnglish()
        mark("reader-02-open-learning-centre")
        openGenesisLearningCentre()
        mark("reader-03-open-reader")
        composeRule.onNodeWithText("Open this book in the Bible reader").performScrollTo().performClick()
        mark("reader-04-await-canonical-reader")
        awaitText("Canonical book ID • GEN")
        composeRule.onNodeWithText("Canonical book ID • GEN").assertIsDisplayed()
        composeRule.onNodeWithText(
            "Verified Maale scripture text is not available in this build yet."
        ).performScrollTo().assertIsDisplayed()
        mark("reader-05-reader-back")
        composeRule.onNodeWithText("←").performClick()
        mark("reader-06-learning-back-to-books")
        composeRule.onNodeWithText("‹  All books").performClick()
        awaitText("Books of the Maale Bible")
        composeRule.onNodeWithText("Books of the Maale Bible").assertIsDisplayed()
    }

    @Test(timeout = 60_000)
    fun searchDestinationRemainsReachableAfterBooksNavigation() {
        mark("search-01-select-english")
        selectEnglish()
        mark("search-02-open-books")
        composeRule.onNodeWithText("Explore the Bible").performClick()
        awaitText("Books of the Maale Bible")
        mark("search-03-open-search")
        composeRule.onNodeWithText("Search").performClick()
        mark("search-04-await-search-screen")
        awaitText("Search with purpose.")
        composeRule.onNodeWithText("Search with purpose.").assertIsDisplayed()
    }

    @Test
    fun bookLearningCentreUsesAmharicAndEnglishResources() {
        composeRule.onNodeWithTag("language_selector").performClick()
        composeRule.onNodeWithTag("language_amharic").performClick()
        composeRule.onNodeWithText("መጻሕፍት").performClick()
        composeRule.onNodeWithTag("books_list").assertIsDisplayed()
        composeRule.onNodeWithTag("books_search").performTextInput("GEN")
        composeRule.onNodeWithContentDescription("Open Genesis").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("book_card_GEN").performClick()
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

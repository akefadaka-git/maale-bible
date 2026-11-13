package org.maale.bible

import android.content.Context
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.maale.bible.ui.AppLanguage

@RunWith(AndroidJUnit4::class)
class AppLanguageInstrumentedTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before fun clearSavedLanguageToExerciseFirstLaunchDefault() {
        compose.activity.getSharedPreferences("maale_bible_preferences", Context.MODE_PRIVATE)
            .edit().remove("ui_language").commit()
        compose.activityRule.scenario.recreate()
        compose.waitForIdle()
    }

    @Test fun maaleIsDefaultAndLocaleSelectionPersistsAcrossActivityRecreation() {
        assertEquals(AppLanguage.MAALE, AppLanguage.load(compose.activity))
        compose.onNodeWithText("ካራ").assertIsDisplayed()

        compose.onNodeWithTag("language_selector").assertHasClickAction().performClick()
        compose.onNodeWithTag("language_amharic").performClick()
        compose.onNodeWithText("መነሻ").assertIsDisplayed()

        compose.activityRule.scenario.recreate()
        compose.waitForIdle()
        compose.onNodeWithText("መነሻ").assertIsDisplayed()

        compose.onNodeWithTag("language_selector").performClick()
        compose.onNodeWithTag("language_english").performClick()
        compose.onNodeWithText("Home").assertIsDisplayed()
    }
}

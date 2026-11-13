package org.maale.bible

import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MaaleBibleSettingsInstrumentedTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test fun settingsExposePrivacyAccessibilityAndPersistentThemeControl() {
        compose.onAllNodesWithText("Settings").onFirst().performClick()
        compose.onNodeWithText("Accessibility").assertExists()
        compose.onNodeWithText("Language").assertExists()
        compose.onNodeWithText("Privacy and offline use").assertExists()
        val themeSwitch = compose.onNodeWithContentDescription("Enable dark theme")
        themeSwitch.assertIsOff()
        themeSwitch.performClick()
        compose.onNodeWithContentDescription("Enable dark theme").assertIsOn()
    }
}

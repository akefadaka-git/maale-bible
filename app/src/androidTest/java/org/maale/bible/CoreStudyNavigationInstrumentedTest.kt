package org.maale.bible

import android.content.Intent
import android.content.IntentFilter
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CoreStudyNavigationInstrumentedTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun bookReaderBookmarkAndStudyHubFlowIsConnected() {
        compose.onNodeWithText("Books").performClick()
        compose.onNodeWithText("1. Genesis").performClick()
        compose.onNodeWithText("Chapter 1 · Verse 1").assertIsDisplayed()
        compose.onNodeWithText("Add bookmark").performClick()
        compose.onNodeWithText("Study").performClick()
        compose.onNodeWithText("Bookmarks (1)").assertIsDisplayed()
        compose.onNodeWithText("Genesis").assertIsDisplayed()
    }

    @Test fun catalogueSearchFindsBooksWithoutScriptureTextSearch() {
        compose.onNodeWithText("Search").performClick()
        compose.onNodeWithText("Find a book").assertIsDisplayed()
        compose.onNodeWithText("Book name or code").assertIsDisplayed()
        compose.onNodeWithText("1. Genesis").assertIsDisplayed()
    }

    @Test fun settingsExposeAccessibilityAndPortableBackupControls() {
        openSettings()
        compose.onNodeWithText("Accessibility").assertIsDisplayed()
        compose.onNodeWithText("Create encrypted backup").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Restore backup").performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription("Select encrypted backup file")
            .performScrollTo().assertIsDisplayed()
    }

    @Test fun restoreKeyIsNotRequestedUntilAfterDocumentSelection() {
        openSettings()
        compose.onNodeWithText("Choose backup file").performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithText("Recovery key").fetchSemanticsNodes().let {
            assert(it.isEmpty()) { "Restore recovery key must remain hidden before a document is selected." }
        }
    }

    @Test fun createDocumentPickerCancellationIsDeliveredAfterActivityRecreation() {
        openSettings()
        assertPickerCancellationSurvivesRecreation(
            action = Intent.ACTION_CREATE_DOCUMENT,
            launchPicker = {
                compose.onNodeWithText("Create encrypted backup").performScrollTo().performClick()
            },
            expectedStatus = "Backup export cancelled."
        )
    }

    @Test fun openDocumentPickerCancellationIsDeliveredAfterActivityRecreation() {
        openSettings()
        assertPickerCancellationSurvivesRecreation(
            action = Intent.ACTION_OPEN_DOCUMENT,
            launchPicker = {
                compose.onNodeWithContentDescription("Select encrypted backup file")
                    .performScrollTo().performClick()
            },
            expectedStatus = "Backup selection cancelled."
        )
    }

    @Test fun settingsRecreationDoesNotRestoreTransientRecoveryMaterial() {
        openSettings()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Choose backup file").performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithText("Recovery key").fetchSemanticsNodes().let {
            assert(it.isEmpty()) { "No recovery key may be restored from saved instance state." }
        }
    }

    private fun openSettings() {
        compose.onNodeWithText("Settings").performClick()
        compose.onNodeWithText("Accessibility").assertIsDisplayed()
    }

    private fun assertPickerCancellationSurvivesRecreation(
        action: String,
        launchPicker: () -> Unit,
        expectedStatus: String
    ) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val monitor = instrumentation.addMonitor(IntentFilter(action), null, false)
        try {
            launchPicker()
            val picker = instrumentation.waitForMonitorWithTimeout(monitor, 10_000)
            assert(picker != null) { "The system document picker for $action was not launched." }

            // Recreate the caller while the external picker owns the foreground. The
            // cancellation result must be delivered to the newly registered launcher.
            compose.activityRule.scenario.recreate()
            instrumentation.uiAutomation.executeShellCommand("input keyevent KEYCODE_BACK").close()
            compose.waitUntil(timeoutMillis = 10_000) {
                compose.onAllNodesWithText(expectedStatus).fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText(expectedStatus).assertIsDisplayed()
        } finally {
            instrumentation.removeMonitor(monitor)
        }
    }
}

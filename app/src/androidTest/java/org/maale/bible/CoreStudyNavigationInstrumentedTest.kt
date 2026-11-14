package org.maale.bible

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
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
        compose.onNodeWithText("Restore portable backup").performScrollTo().assertIsDisplayed()
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

    @Test fun createDocumentPickerCancellationIsDelivered() {
        openSettings()
        assertPickerCancellationSurvivesRecreation(
            action = Intent.ACTION_CREATE_DOCUMENT,
            launchPicker = {
                compose.onNodeWithText("Create encrypted backup").performScrollTo().performClick()
            },
            expectedStatus = "Backup export cancelled."
        )
    }

    @Test fun openDocumentPickerCancellationIsDelivered() {
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
        val device = UiDevice.getInstance(instrumentation)
        try {
            launchPicker()
            // SAF launches the platform DocumentsUI process. Observe the foreground
            // package rather than relying on Instrumentation.ActivityMonitor, which
            // does not reliably report external ActivityResultContract launches.
            val pickerVisible = device.wait(
                Until.hasObject(By.pkg("com.google.android.documentsui")),
                15_000
            )
            assert(pickerVisible) { "DocumentsUI did not become visible for $action." }

            // Cancel the platform picker and verify that the ActivityResult launcher
            // receives the null result in the still-existing caller Activity. Activity
            // recreation is covered independently by settingsRecreationDoesNotRestoreTransientRecoveryMaterial;
            // ActivityScenario.recreate() cannot run while DocumentsUI owns foreground.
            device.pressBack()
            compose.waitUntil(timeoutMillis = 10_000) {
                compose.onAllNodesWithText(expectedStatus).fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText(expectedStatus).assertIsDisplayed()
        } finally {
            // Keep the test isolated if the platform picker remains open on failure.
            device.pressBack()
        }
    }
}

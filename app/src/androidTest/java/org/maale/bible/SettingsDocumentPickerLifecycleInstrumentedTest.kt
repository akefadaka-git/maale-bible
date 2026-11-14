package org.maale.bible

import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.maale.bible.ui.PendingDocumentPickerState

@RunWith(AndroidJUnit4::class)
class SettingsDocumentPickerLifecycleInstrumentedTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun pendingExportAndRestoreKeysSurviveActivityRecreationAndAreConsumedOnce() {
        val before = ViewModelProvider(compose.activity)
            .get(PendingDocumentPickerState::class.java)
        before.storeExportKey("export-key-test-only")
        before.storeRestoreKey("restore-key-test-only")

        compose.activityRule.scenario.recreate()
        compose.waitForIdle()

        val after = ViewModelProvider(compose.activity)
            .get(PendingDocumentPickerState::class.java)
        assertSame("Activity recreation must retain the Activity-scoped ViewModel", before, after)
        assertEquals("export-key-test-only", after.consumeExportKey())
        assertNull("Export key must be consumed exactly once", after.consumeExportKey())
        assertEquals("restore-key-test-only", after.consumeRestoreKey())
        assertNull("Restore key must be consumed exactly once", after.consumeRestoreKey())
    }

    @Test fun freshPickerStateStartsWithoutPendingSecrets() {
        val fresh = PendingDocumentPickerState()
        assertNull(fresh.consumeExportKey())
        assertNull(fresh.consumeRestoreKey())
    }
}

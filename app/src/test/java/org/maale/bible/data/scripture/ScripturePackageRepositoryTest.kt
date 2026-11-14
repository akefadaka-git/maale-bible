package org.maale.bible.data.scripture

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScripturePackageRepositoryTest {
    @Test
    fun releaseBuildRequiresExplicitPackageEligibility() {
        assertTrue(canDisplayScripturePackage(releaseEligible = true, allowDebugPreview = false))
        assertFalse(canDisplayScripturePackage(releaseEligible = false, allowDebugPreview = false))
    }

    @Test
    fun debugBuildCanPreviewNonReleaseCandidateWithoutChangingItsMetadata() {
        assertTrue(canDisplayScripturePackage(releaseEligible = false, allowDebugPreview = true))
    }
}

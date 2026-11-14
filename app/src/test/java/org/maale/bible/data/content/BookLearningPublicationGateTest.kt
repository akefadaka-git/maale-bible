package org.maale.bible.data.content

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BookLearningPublicationGateTest {
    @Test
    fun onlyExplicitReleaseApprovalPasses() {
        assertTrue(isApprovedPublicationState("approved_for_release"))
        assertFalse(isApprovedPublicationState("draft_not_for_release"))
        assertFalse(isApprovedPublicationState("pending_review"))
        assertFalse(isApprovedPublicationState(""))
        assertFalse(isApprovedPublicationState("approved"))
    }
}

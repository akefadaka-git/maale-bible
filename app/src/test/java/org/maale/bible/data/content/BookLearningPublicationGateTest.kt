package org.maale.bible.data.content

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BookLearningPublicationGateTest {
    @Test
    fun onlyExplicitReleaseApprovalIsApproved() {
        assertTrue(isApprovedPublicationState("approved_for_release"))
        assertFalse(isApprovedPublicationState("draft_not_for_release"))
        assertFalse(isApprovedPublicationState("pending_review"))
        assertFalse(isApprovedPublicationState(""))
        assertFalse(isApprovedPublicationState("approved"))
    }

    @Test
    fun draftPreviewIsAvailableOnlyWhenExplicitlyEnabledForDebugReview() {
        assertTrue(canDisplayBookLearningContent("approved_for_release", allowDraftPreview = false))
        assertFalse(canDisplayBookLearningContent("draft_not_for_release", allowDraftPreview = false))
        assertTrue(canDisplayBookLearningContent("draft_not_for_release", allowDraftPreview = true))
        assertFalse(canDisplayBookLearningContent("pending_review", allowDraftPreview = true))
        assertFalse(canDisplayBookLearningContent("", allowDraftPreview = true))
    }
}

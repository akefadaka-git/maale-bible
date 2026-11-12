package org.maale.bible

import org.junit.Assert.assertEquals
import org.junit.Test
import org.maale.bible.domain.model.*

class ReadingPlanScheduleTest {
    @Test fun scheduleUsesPersistedStartDateAndMarksDueOverdueAndCompletedDays() {
        val plan = ReadingPlan("p1", "Seven days", 20L, 7, setOf(1, 3), 100L)
        val schedule = plan.schedule(todayEpochDay = 22L)
        assertEquals(PlanDayStatus.COMPLETED, schedule[0].status)
        assertEquals(PlanDayStatus.OVERDUE, schedule[1].status)
        assertEquals(PlanDayStatus.COMPLETED, schedule[2].status)
        assertEquals(PlanDayStatus.DUE, schedule[3].status)
        assertEquals(PlanDayStatus.UPCOMING, schedule[4].status)
        assertEquals(20L, schedule[0].dueEpochDay)
    }

    @Test fun completionIsIdempotentAndBounded() {
        val plan = ReadingPlan("p1", "Plan", 20L, 7, emptySet(), 100L)
        assertEquals(1, plan.withDayCompleted(1, 200L).withDayCompleted(1, 300L).completedDays.size)
        assertEquals(100, plan.copy(completedDays = (1..7).toSet()).completionPercent())
        try {
            plan.withDayCompleted(8, 200L)
            throw AssertionError("Expected invalid day to be rejected")
        } catch (_: IllegalArgumentException) { }
    }
}

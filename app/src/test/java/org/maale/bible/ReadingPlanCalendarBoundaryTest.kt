package org.maale.bible

import org.junit.Assert.assertEquals
import org.junit.Test
import org.maale.bible.domain.model.PlanDayStatus
import org.maale.bible.domain.model.ReadingPlan
import org.maale.bible.domain.model.schedule
import java.time.LocalDate

class ReadingPlanCalendarBoundaryTest {
    @Test fun leapDayAndMonthBoundaryRemainConsecutiveEpochDays() {
        val plan = ReadingPlan("leap", "Leap boundary", LocalDate.of(2024, 2, 28).toEpochDay(), 4, emptySet(), 1L)
        val days = plan.schedule(LocalDate.of(2024, 2, 29).toEpochDay())
        assertEquals(LocalDate.of(2024, 2, 28), days[0].dueDate)
        assertEquals(LocalDate.of(2024, 2, 29), days[1].dueDate)
        assertEquals(LocalDate.of(2024, 3, 1), days[2].dueDate)
        assertEquals(PlanDayStatus.OVERDUE, days[0].status)
        assertEquals(PlanDayStatus.DUE, days[1].status)
        assertEquals(PlanDayStatus.UPCOMING, days[2].status)
    }

    @Test fun yearBoundaryRemainsConsecutiveAcrossDecemberAndJanuary() {
        val plan = ReadingPlan("year", "Year boundary", LocalDate.of(2025, 12, 31).toEpochDay(), 3, emptySet(), 1L)
        val days = plan.schedule(LocalDate.of(2026, 1, 1).toEpochDay())
        assertEquals(LocalDate.of(2025, 12, 31), days[0].dueDate)
        assertEquals(LocalDate.of(2026, 1, 1), days[1].dueDate)
        assertEquals(LocalDate.of(2026, 1, 2), days[2].dueDate)
        assertEquals(PlanDayStatus.OVERDUE, days[0].status)
        assertEquals(PlanDayStatus.DUE, days[1].status)
        assertEquals(PlanDayStatus.UPCOMING, days[2].status)
    }
}

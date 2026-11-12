package org.maale.bible.domain.model

import java.time.LocalDate

enum class PlanDayStatus { UPCOMING, DUE, OVERDUE, COMPLETED }

data class ScheduledPlanDay(
    val dayNumber: Int,
    val dueEpochDay: Long,
    val status: PlanDayStatus
) {
    val dueDate: LocalDate get() = LocalDate.ofEpochDay(dueEpochDay)
}

/** Calendar schedule is derived deterministically from the persisted start date and duration. */
fun ReadingPlan.schedule(todayEpochDay: Long): List<ScheduledPlanDay> =
    (1..durationDays).map { day ->
        val due = Math.addExact(startDateEpochDay, (day - 1).toLong())
        val status = when {
            day in completedDays -> PlanDayStatus.COMPLETED
            todayEpochDay < due -> PlanDayStatus.UPCOMING
            todayEpochDay == due -> PlanDayStatus.DUE
            else -> PlanDayStatus.OVERDUE
        }
        ScheduledPlanDay(day, due, status)
    }

fun ReadingPlan.completionPercent(): Int =
    ((completedDays.size * 100.0) / durationDays).toInt()

fun ReadingPlan.withDayCompleted(dayNumber: Int, updatedAtEpochMs: Long): ReadingPlan {
    require(dayNumber in 1..durationDays) { "Day is outside this plan" }
    return copy(completedDays = completedDays + dayNumber, updatedAtEpochMs = updatedAtEpochMs)
}

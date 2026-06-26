package com.ion.daily_tracking.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single activity the user wants to track.
 *
 * Three flavours are supported, distinguished by [repeating] and [carryOver]:
 *  - repeating == true               -> a daily habit (e.g. "Gym 18:00"). [epochDay] is null; shows every day.
 *  - repeating == false, carryOver == false -> a one-off task tied to the single calendar date in [epochDay].
 *  - repeating == false, carryOver == true  -> an "until done" task: anchored at [epochDay] (its start
 *      date) and re-shown on every following day until it is ticked off, after which it disappears.
 *
 * Times are stored as "minutes from midnight" (0..1439) so they are timezone-agnostic and
 * trivial to compare/sort. -1 means "no specific time".
 */
@Entity(tableName = "schedule_items")
data class ScheduleItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    val startMinute: Int = -1,
    val endMinute: Int = -1,
    val repeating: Boolean = false,
    val epochDay: Long? = null,
    val reminderEnabled: Boolean = false,
    val carryOver: Boolean = false,
) {
    val hasTime: Boolean get() = startMinute in 0..1439
}

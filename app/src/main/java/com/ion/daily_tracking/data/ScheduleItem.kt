package com.ion.daily_tracking.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single activity the user wants to track.
 *
 * Two flavours are supported, distinguished by [repeating]:
 *  - repeating == true  -> a daily habit (e.g. "Gym 18:00"). [epochDay] is null; it shows up every day.
 *  - repeating == false -> a one-off task tied to a specific calendar date held in [epochDay].
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
) {
    val hasTime: Boolean get() = startMinute in 0..1439
}

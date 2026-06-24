package com.ion.daily_tracking.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * One row = "[itemId] was ticked off on [epochDay]".
 *
 * Tracking completion per day (instead of a single boolean on the item) is what lets a daily
 * repeating habit be done today but pending again tomorrow, while a one-off task simply has one
 * completion row for its own date.
 */
@Entity(
    tableName = "completions",
    primaryKeys = ["itemId", "epochDay"],
    foreignKeys = [
        ForeignKey(
            entity = ScheduleItem::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("itemId")],
)
data class Completion(
    val itemId: Long,
    val epochDay: Long,
)

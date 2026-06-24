package com.ion.daily_tracking.data

/** A completed activity at a point in time — used to build the history log on the profile screen. */
data class HistoryEntry(
    val title: String,
    val epochDay: Long,
    val startMinute: Int,
    val repeating: Boolean,
)

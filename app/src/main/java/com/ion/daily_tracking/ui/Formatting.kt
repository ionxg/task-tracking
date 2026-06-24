package com.ion.daily_tracking.ui

import com.ion.daily_tracking.data.ScheduleItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dayFormatter = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault())

fun formatMinute(minute: Int): String {
    if (minute < 0) return ""
    val h = minute / 60
    val m = minute % 60
    return String.format(Locale.getDefault(), "%02d:%02d", h, m)
}

/** A compact "08:00 – 09:30" / "08:00" / "" label for an item's timeframe. */
fun ScheduleItem.timeframeLabel(): String = when {
    !hasTime -> ""
    endMinute in 0..1439 && endMinute != startMinute ->
        "${formatMinute(startMinute)} – ${formatMinute(endMinute)}"
    else -> formatMinute(startMinute)
}

fun formatDayHeading(epochDay: Long): String =
    LocalDate.ofEpochDay(epochDay).format(dayFormatter)

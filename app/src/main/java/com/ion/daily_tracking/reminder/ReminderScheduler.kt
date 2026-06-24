package com.ion.daily_tracking.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.ion.daily_tracking.data.ScheduleItem
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Arms / cancels local alarms that fire a [ReminderReceiver] at an item's start time.
 *
 * Uses inexact, doze-friendly alarms ([AlarmManager.setAndAllowWhileIdle]) so the app needs no
 * special "exact alarm" permission. A reminder may be delivered a few minutes late while the phone
 * is in deep doze — acceptable for activity reminders and a fair trade for zero permission friction.
 */
class ReminderScheduler(private val context: Context) {

    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun reschedule(item: ScheduleItem) {
        cancel(item)
        if (!item.reminderEnabled || !item.hasTime) return
        val triggerAt = nextTriggerMillis(item) ?: return
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            pendingIntent(item, create = true),
        )
    }

    fun cancel(item: ScheduleItem) {
        pendingIntent(item, create = false)?.let { alarmManager.cancel(it) }
    }

    /** Next moment this item should fire, or null if it's a one-off whose time has already passed. */
    private fun nextTriggerMillis(item: ScheduleItem): Long? {
        val time = LocalTime.of(item.startMinute / 60, item.startMinute % 60)
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.now()

        val dateTime = if (item.repeating) {
            val today = LocalDate.now().atTime(time)
            if (today.isAfter(now)) today else today.plusDays(1)
        } else {
            val day = item.epochDay ?: return null
            val at = LocalDate.ofEpochDay(day).atTime(time)
            if (at.isAfter(now)) at else return null
        }
        return dateTime.atZone(zone).toInstant().toEpochMilli()
    }

    private fun pendingIntent(item: ScheduleItem, create: Boolean): PendingIntent? {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_ITEM_ID, item.id)
            putExtra(ReminderReceiver.EXTRA_TITLE, item.title)
            putExtra(ReminderReceiver.EXTRA_NOTES, item.notes)
            putExtra(ReminderReceiver.EXTRA_REPEATING, item.repeating)
        }
        var flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        if (!create) flags = flags or PendingIntent.FLAG_NO_CREATE
        return PendingIntent.getBroadcast(context, item.id.toInt(), intent, flags)
    }
}

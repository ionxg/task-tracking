package com.ion.daily_tracking

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class DailyTrackingApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Activity reminders",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Reminders for scheduled activities"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "activity_reminders"
    }
}

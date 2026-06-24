package com.ion.daily_tracking.reminder

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ion.daily_tracking.DailyTrackingApp
import com.ion.daily_tracking.MainActivity
import com.ion.daily_tracking.R
import com.ion.daily_tracking.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Posts the reminder notification when an alarm fires, then re-arms daily habits for the next day. */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val itemId = intent.getLongExtra(EXTRA_ITEM_ID, 0L)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Activity"
        val notes = intent.getStringExtra(EXTRA_NOTES).orEmpty()
        val repeating = intent.getBooleanExtra(EXTRA_REPEATING, false)

        postNotification(context, itemId, title, notes)

        // A daily habit needs the next day's alarm armed once this one has fired.
        if (repeating) reArmNextDay(context, itemId)
    }

    private fun reArmNextDay(context: Context, itemId: Long) {
        val appContext = context.applicationContext
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val item = AppDatabase.get(appContext).scheduleDao().getItem(itemId)
                if (item != null) ReminderScheduler(appContext).reschedule(item)
            } finally {
                pending.finish()
            }
        }
    }

    private fun postNotification(context: Context, itemId: Long, title: String, notes: String) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) return

        val tapIntent = PendingIntent.getActivity(
            context,
            itemId.toInt(),
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, DailyTrackingApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(if (notes.isBlank()) "It's time for this activity." else notes)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(tapIntent)
            .build()

        NotificationManagerCompat.from(context).notify(itemId.toInt(), notification)
    }

    companion object {
        const val EXTRA_ITEM_ID = "extra_item_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_NOTES = "extra_notes"
        const val EXTRA_REPEATING = "extra_repeating"
    }
}

package com.ion.daily_tracking.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ion.daily_tracking.data.ScheduleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Alarms are cleared on reboot, so re-arm every reminder once the device finishes booting. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val appContext = context.applicationContext
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ScheduleRepository.from(appContext).rescheduleAllReminders()
            } finally {
                pending.finish()
            }
        }
    }
}

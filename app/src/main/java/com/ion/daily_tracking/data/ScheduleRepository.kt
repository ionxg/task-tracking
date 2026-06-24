package com.ion.daily_tracking.data

import android.content.Context
import com.ion.daily_tracking.reminder.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** An item paired with whether it is ticked off for the day currently being viewed. */
data class TrackedItem(
    val item: ScheduleItem,
    val done: Boolean,
)

class ScheduleRepository(
    private val dao: ScheduleDao,
    private val reminders: ReminderScheduler,
) {

    /** Combines items + completions for [day] into a single observable list for the UI. */
    fun itemsForDay(day: Long): Flow<List<TrackedItem>> =
        combine(
            dao.observeItemsForDay(day),
            dao.observeCompletionsForDay(day),
        ) { items, completedIds ->
            val done = completedIds.toHashSet()
            items.map { TrackedItem(it, it.id in done) }
        }

    suspend fun getItem(id: Long): ScheduleItem? = dao.getItem(id)

    suspend fun save(item: ScheduleItem): ScheduleItem {
        val saved = if (item.id == 0L) {
            item.copy(id = dao.insert(item))
        } else {
            dao.update(item)
            item
        }
        reminders.reschedule(saved)
        return saved
    }

    suspend fun delete(item: ScheduleItem) {
        reminders.cancel(item)
        dao.delete(item)
    }

    suspend fun setDone(item: ScheduleItem, day: Long, done: Boolean) {
        if (done) dao.addCompletion(Completion(item.id, day))
        else dao.removeCompletion(item.id, day)
    }

    /** Re-arm every reminder — called after a reboot. */
    suspend fun rescheduleAllReminders() {
        dao.getReminderItems().forEach { reminders.reschedule(it) }
    }

    companion object {
        fun from(context: Context): ScheduleRepository {
            val dao = AppDatabase.get(context).scheduleDao()
            return ScheduleRepository(dao, ReminderScheduler(context.applicationContext))
        }
    }
}

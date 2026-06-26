package com.ion.daily_tracking.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    /**
     * Items relevant to a given day:
     *  - every repeating habit,
     *  - one-off tasks dated exactly that day,
     *  - "until done" tasks that started on or before that day and have not yet been completed on an
     *    earlier day (so they keep rolling forward until ticked off, then drop away the next day).
     */
    @Query(
        """
        SELECT * FROM schedule_items
        WHERE repeating = 1
           OR (carryOver = 0 AND epochDay = :day)
           OR (carryOver = 1 AND epochDay <= :day
               AND NOT EXISTS (
                   SELECT 1 FROM completions c
                   WHERE c.itemId = schedule_items.id AND c.epochDay < :day
               ))
        ORDER BY (startMinute < 0), startMinute, title
        """
    )
    fun observeItemsForDay(day: Long): Flow<List<ScheduleItem>>

    /** Item ids that have been ticked off on the given day. */
    @Query("SELECT itemId FROM completions WHERE epochDay = :day")
    fun observeCompletionsForDay(day: Long): Flow<List<Long>>

    /** Every completed activity, newest first — the profile history log. */
    @Query(
        """
        SELECT i.title AS title, c.epochDay AS epochDay, i.startMinute AS startMinute,
               i.repeating AS repeating
        FROM completions c
        INNER JOIN schedule_items i ON i.id = c.itemId
        ORDER BY c.epochDay DESC, i.startMinute
        """
    )
    fun observeHistory(): Flow<List<HistoryEntry>>

    /** Distinct days on which anything was completed, newest first — used for streaks. */
    @Query("SELECT DISTINCT epochDay FROM completions ORDER BY epochDay DESC")
    fun observeCompletionDays(): Flow<List<Long>>

    /** Total number of completions ever recorded. */
    @Query("SELECT COUNT(*) FROM completions")
    fun observeTotalCompletions(): Flow<Int>

    @Query("SELECT * FROM schedule_items WHERE id = :id")
    suspend fun getItem(id: Long): ScheduleItem?

    /** All items that want a reminder — used to (re)arm alarms, e.g. after reboot. */
    @Query("SELECT * FROM schedule_items WHERE reminderEnabled = 1")
    suspend fun getReminderItems(): List<ScheduleItem>

    @Insert
    suspend fun insert(item: ScheduleItem): Long

    @Update
    suspend fun update(item: ScheduleItem)

    @Delete
    suspend fun delete(item: ScheduleItem)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addCompletion(completion: Completion)

    @Query("DELETE FROM completions WHERE itemId = :itemId AND epochDay = :day")
    suspend fun removeCompletion(itemId: Long, day: Long)
}

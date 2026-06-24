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

    /** Items relevant to a given day: every repeating habit plus one-off tasks dated that day. */
    @Query(
        """
        SELECT * FROM schedule_items
        WHERE repeating = 1 OR epochDay = :day
        ORDER BY (startMinute < 0), startMinute, title
        """
    )
    fun observeItemsForDay(day: Long): Flow<List<ScheduleItem>>

    /** Item ids that have been ticked off on the given day. */
    @Query("SELECT itemId FROM completions WHERE epochDay = :day")
    fun observeCompletionsForDay(day: Long): Flow<List<Long>>

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

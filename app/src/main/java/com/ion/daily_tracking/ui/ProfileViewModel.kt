package com.ion.daily_tracking.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ion.daily_tracking.data.HistoryEntry
import com.ion.daily_tracking.data.ScheduleRepository
import com.ion.daily_tracking.data.UserPrefs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class ProfileUiState(
    val name: String = "Friend",
    val todayDone: Int = 0,
    val todayTotal: Int = 0,
    val streakDays: Int = 0,
    val totalCompleted: Int = 0,
    val history: List<HistoryEntry> = emptyList(),
) {
    val todayProgress: Float get() = if (todayTotal == 0) 0f else todayDone.toFloat() / todayTotal
}

class ProfileViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ScheduleRepository.from(app)
    private val prefs = UserPrefs(app)

    val state: StateFlow<ProfileUiState> =
        combine(
            prefs.displayName,
            repo.itemsForDay(LocalDate.now().toEpochDay()),
            repo.completionDays(),
            repo.totalCompletions(),
            repo.history(),
        ) { name, today, days, total, history ->
            ProfileUiState(
                name = name,
                todayDone = today.count { it.done },
                todayTotal = today.size,
                streakDays = currentStreak(days),
                totalCompleted = total,
                history = history,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun setName(name: String) = prefs.setDisplayName(name)

    /**
     * Consecutive days (ending today or yesterday) on which at least one activity was completed.
     * [days] is distinct completion days, newest first.
     */
    private fun currentStreak(days: List<Long>): Int {
        if (days.isEmpty()) return 0
        val today = LocalDate.now().toEpochDay()
        // Allow the streak to still "count" if nothing is done yet today but yesterday was.
        var expected = if (days.first() == today) today else today - 1
        if (days.first() != expected) return 0
        var streak = 0
        for (day in days) {
            if (day == expected) {
                streak++
                expected--
            } else if (day < expected) {
                break
            }
        }
        return streak
    }
}

package com.ion.daily_tracking.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ion.daily_tracking.data.ScheduleItem
import com.ion.daily_tracking.data.ScheduleRepository
import com.ion.daily_tracking.data.TrackedItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ScheduleRepository.from(app)

    private val _selectedDay = MutableStateFlow(LocalDate.now().toEpochDay())
    val selectedDay: StateFlow<Long> = _selectedDay

    val items: StateFlow<List<TrackedItem>> =
        _selectedDay
            .flatMapLatest { repo.itemsForDay(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun showDay(epochDay: Long) {
        _selectedDay.value = epochDay
    }

    fun goToToday() = showDay(LocalDate.now().toEpochDay())

    fun shiftDay(deltaDays: Long) {
        _selectedDay.value = _selectedDay.value + deltaDays
    }

    fun toggleDone(tracked: TrackedItem) {
        viewModelScope.launch {
            repo.setDone(tracked.item, _selectedDay.value, !tracked.done)
        }
    }

    fun delete(item: ScheduleItem) {
        viewModelScope.launch { repo.delete(item) }
    }

    suspend fun load(id: Long): ScheduleItem? = repo.getItem(id)

    fun save(item: ScheduleItem) {
        viewModelScope.launch { repo.save(item) }
    }
}

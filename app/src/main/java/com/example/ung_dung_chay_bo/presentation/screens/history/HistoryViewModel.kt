package com.example.ung_dung_chay_bo.presentation.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ung_dung_chay_bo.data.local.entity.ActivityRecordEntity
import com.example.ung_dung_chay_bo.domain.model.SportType
import com.example.ung_dung_chay_bo.domain.repository.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val activities: List<ActivityRecordEntity> = emptyList(),
    val filteredActivities: List<ActivityRecordEntity> = emptyList(),
    val selectedSportFilter: SportType? = null,
    val totalDistanceKm: Double = 0.0,
    val totalActivitiesCount: Int = 0,
    val totalDurationSeconds: Long = 0L,
    val isLoading: Boolean = false
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow<SportType?>(null)

    val uiState: StateFlow<HistoryUiState> = combine(
        activityRepository.getAllActivities(),
        _selectedFilter
    ) { allList, filter ->
        val filtered = if (filter == null) {
            allList
        } else {
            allList.filter { it.sportType.equals(filter.name, ignoreCase = true) }
        }

        val totalDist = allList.sumOf { it.distanceKm }
        val totalSec = allList.sumOf { it.durationSeconds }

        HistoryUiState(
            activities = allList,
            filteredActivities = filtered,
            selectedSportFilter = filter,
            totalDistanceKm = totalDist,
            totalActivitiesCount = allList.size,
            totalDurationSeconds = totalSec,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState(isLoading = true)
    )

    fun setFilter(sport: SportType?) {
        _selectedFilter.value = sport
    }

    fun deleteActivity(id: Long) {
        viewModelScope.launch {
            activityRepository.deleteActivity(id)
        }
    }
}

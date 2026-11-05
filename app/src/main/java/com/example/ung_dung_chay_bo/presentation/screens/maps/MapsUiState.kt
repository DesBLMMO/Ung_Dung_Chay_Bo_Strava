package com.example.ung_dung_chay_bo.presentation.screens.maps

import com.example.ung_dung_chay_bo.domain.model.RouteDifficulty
import com.example.ung_dung_chay_bo.domain.model.SportType
import com.example.ung_dung_chay_bo.domain.model.SuggestedRoute
import com.example.ung_dung_chay_bo.domain.model.SurfaceType
import com.google.maps.android.compose.MapType

enum class DistanceFilter(val title: String, val minKm: Double, val maxKm: Double) {
    ALL("Tất cả", 0.0, 1000.0),
    SHORT("< 5 km", 0.0, 5.0),
    MEDIUM("5 - 10 km", 5.0, 10.0),
    LONG("> 10 km", 10.0, 1000.0)
}

data class MapsUiState(
    val selectedSport: SportType = SportType.TRAIL_RUN,
    val searchQuery: String = "",
    val activeDistanceFilter: DistanceFilter = DistanceFilter.ALL,
    val activeDifficultyFilter: RouteDifficulty? = null,
    val activeSurfaceFilter: SurfaceType? = null,
    val isSavedOnly: Boolean = false,
    val routes: List<SuggestedRoute> = emptyList(),
    val selectedRoute: SuggestedRoute? = null,
    val mapType: MapType = MapType.NORMAL,
    val is3DMode: Boolean = false,
    val userLocation: Pair<Double, Double> = Pair(21.0025, 105.8185),
    val isSportDropdownExpanded: Boolean = false,
    val isFilterSheetVisible: Boolean = false,
    val isSavedRoutesSheetVisible: Boolean = false
)

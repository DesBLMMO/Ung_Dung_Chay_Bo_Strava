package com.example.ung_dung_chay_bo.presentation.screens.maps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ung_dung_chay_bo.core.utils.RoadSnapService
import com.example.ung_dung_chay_bo.domain.model.RouteDifficulty
import com.example.ung_dung_chay_bo.domain.model.SportType
import com.example.ung_dung_chay_bo.domain.model.SuggestedRoute
import com.example.ung_dung_chay_bo.domain.model.SurfaceType
import com.google.maps.android.compose.MapType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapsViewModel @Inject constructor() : ViewModel() {

    private val masterRoutes = createCatalog()
    private val _uiState = MutableStateFlow(MapsUiState())
    val uiState: StateFlow<MapsUiState> = _uiState.asStateFlow()

    init {
        updateFilteredRoutes()
        _uiState.value.selectedRoute?.let { snapRouteToRoads(it) }
    }

    private fun updateFilteredRoutes() {
        _uiState.update { current ->
            val filtered = masterRoutes.filter { route ->
                val matchesSport = route.sportType == current.selectedSport
                val matchesSearch = current.searchQuery.isBlank() ||
                        route.title.contains(current.searchQuery, ignoreCase = true) ||
                        route.areaName.contains(current.searchQuery, ignoreCase = true)
                val matchesDistance = route.distanceKm >= current.activeDistanceFilter.minKm &&
                        route.distanceKm < current.activeDistanceFilter.maxKm
                val matchesDifficulty = current.activeDifficultyFilter == null || route.difficulty == current.activeDifficultyFilter
                val matchesSurface = current.activeSurfaceFilter == null || route.surfaceType == current.activeSurfaceFilter
                val matchesSaved = !current.isSavedOnly || route.isSaved

                matchesSport && matchesSearch && matchesDistance && matchesDifficulty && matchesSurface && matchesSaved
            }

            val sortedList = if (current.isPopularOnly) {
                filtered.sortedByDescending { it.runnersCount }
            } else {
                filtered.sortedWith(
                    compareByDescending<SuggestedRoute> { it.isPersonalized }
                        .thenByDescending { it.runnersCount }
                )
            }

            val nextSelected = if (sortedList.any { it.id == current.selectedRoute?.id }) {
                current.selectedRoute
            } else {
                sortedList.firstOrNull()
            }

            current.copy(
                routes = sortedList,
                selectedRoute = nextSelected
            )
        }

        _uiState.value.selectedRoute?.let { route ->
            snapRouteToRoads(route)
        }
    }

    fun selectSport(sport: SportType) {
        _uiState.update { it.copy(selectedSport = sport, isSportDropdownExpanded = false) }
        updateFilteredRoutes()
    }

    fun setSportDropdownExpanded(expanded: Boolean) {
        _uiState.update { it.copy(isSportDropdownExpanded = expanded) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        updateFilteredRoutes()
    }

    fun selectRoute(route: SuggestedRoute) {
        _uiState.update { it.copy(selectedRoute = route) }
        snapRouteToRoads(route)
    }

    private fun snapRouteToRoads(route: SuggestedRoute) {
        viewModelScope.launch {
            val snapped = RoadSnapService.snapToRealRoads(
                routeId = route.id,
                waypoints = route.routePoints,
                sportType = route.sportType
            )
            if (snapped.size > route.routePoints.size) {
                val updatedRoute = route.copy(routePoints = snapped)
                val idx = masterRoutes.indexOfFirst { it.id == route.id }
                if (idx != -1) {
                    masterRoutes[idx] = updatedRoute
                }
                _uiState.update { current ->
                    val updatedList = current.routes.map {
                        if (it.id == route.id) updatedRoute else it
                    }
                    val updatedSelected = if (current.selectedRoute?.id == route.id) updatedRoute else current.selectedRoute
                    current.copy(
                        routes = updatedList,
                        selectedRoute = updatedSelected
                    )
                }
            }
        }
    }

    fun toggleSaveRoute(routeId: String) {
        val index = masterRoutes.indexOfFirst { it.id == routeId }
        if (index != -1) {
            val old = masterRoutes[index]
            masterRoutes[index] = old.copy(isSaved = !old.isSaved)
        }
        updateFilteredRoutes()
    }

    fun setDistanceFilter(filter: DistanceFilter) {
        _uiState.update { it.copy(activeDistanceFilter = filter) }
        updateFilteredRoutes()
    }

    fun setDifficultyFilter(difficulty: RouteDifficulty?) {
        _uiState.update { it.copy(activeDifficultyFilter = difficulty) }
        updateFilteredRoutes()
    }

    fun setSurfaceFilter(surface: SurfaceType?) {
        _uiState.update { it.copy(activeSurfaceFilter = surface) }
        updateFilteredRoutes()
    }

    fun toggleSavedOnly() {
        _uiState.update { it.copy(isSavedOnly = !it.isSavedOnly) }
        updateFilteredRoutes()
    }

    fun togglePopularOnly() {
        _uiState.update { it.copy(isPopularOnly = !it.isPopularOnly) }
        updateFilteredRoutes()
    }

    fun cycleMapType() {
        _uiState.update { current ->
            val next = when (current.mapType) {
                MapType.NORMAL -> MapType.TERRAIN
                MapType.TERRAIN -> MapType.SATELLITE
                MapType.SATELLITE -> MapType.NORMAL
                else -> MapType.NORMAL
            }
            current.copy(mapType = next)
        }
    }

    fun toggle3DMode() {
        _uiState.update { it.copy(is3DMode = !it.is3DMode) }
    }

    fun searchInCurrentArea() {
        updateFilteredRoutes()
    }

    fun addUserCreatedRoute(
        title: String,
        sportType: SportType,
        distanceKm: Double,
        durationMinutes: Int,
        areaName: String,
        difficulty: RouteDifficulty,
        surfaceType: SurfaceType
    ) {
        val userLat = _uiState.value.userLocation.first
        val userLng = _uiState.value.userLocation.second

        val radiusKm = (distanceKm / 6.28).coerceIn(0.4, 2.5)
        val deg = radiusKm / 111.0

        val waypoints = listOf(
            userLat to userLng,
            userLat + deg * 0.8 to userLng + deg * 0.6,
            userLat + deg * 1.1 to userLng - deg * 0.3,
            userLat + deg * 0.3 to userLng - deg * 0.7,
            userLat to userLng
        )

        val newRoute = SuggestedRoute(
            id = "custom_${System.currentTimeMillis()}",
            title = title,
            sportType = sportType,
            distanceKm = distanceKm,
            durationMinutes = durationMinutes,
            elevationGainM = 25,
            difficulty = difficulty,
            surfaceType = surfaceType,
            startLocationName = "Vị trí của bạn ($areaName)",
            areaName = areaName,
            isPersonalized = true,
            isSaved = true,
            startPoint = Pair(userLat, userLng),
            routesInAreaCount = 1,
            imageUrl = when (sportType) {
                SportType.RUN -> "https://images.unsplash.com/photo-1571008887538-b36bb32f4571?w=400"
                SportType.WALK -> "https://images.unsplash.com/photo-1517649763962-0c623266ddc0?w=400"
                SportType.RIDE -> "https://images.unsplash.com/photo-1485965120184-e220f721d03e?w=400"
                SportType.TRAIL_RUN -> "https://images.unsplash.com/photo-1551632811-561732d1e306?w=400"
            },
            routePoints = waypoints
        )

        masterRoutes.add(0, newRoute)
        _uiState.update { it.copy(selectedSport = sportType, selectedRoute = newRoute) }
        updateFilteredRoutes()

        snapRouteToRoads(newRoute)
    }

    private fun createCatalog(): MutableList<SuggestedRoute> {
        val userLat = 21.0025
        val userLng = 105.8185

        return mutableListOf(
            SuggestedRoute(
                id = "trail_1",
                title = "Phố Khương Trung - Ngách 29/78...",
                sportType = SportType.TRAIL_RUN,
                distanceKm = 6.88,
                durationMinutes = 50,
                elevationGainM = 45,
                difficulty = RouteDifficulty.EASY,
                surfaceType = SurfaceType.MIXED,
                startLocationName = "Vị trí hiện tại",
                areaName = "Khương Trung",
                isPersonalized = true,
                isSaved = true,
                startPoint = Pair(userLat, userLng),
                routesInAreaCount = 1,
                runnersCount = 380,
                imageUrl = "https://images.unsplash.com/photo-1544717305-2782549b5136?w=400",
                routePoints = listOf(
                    21.0028 to 105.8188,
                    21.0000 to 105.8175,
                    20.9960 to 105.8178,
                    20.9895 to 105.8162,
                    20.9930 to 105.8190,
                    20.9985 to 105.8218,
                    21.0020 to 105.8210,
                    21.0028 to 105.8188
                )
            ),
            SuggestedRoute(
                id = "trail_2",
                title = "Đồi Hàm Lợn Sóc Sơn - Chinh phục Đỉnh Độc Tôn",
                sportType = SportType.TRAIL_RUN,
                distanceKm = 11.20,
                durationMinutes = 105,
                elevationGainM = 420,
                difficulty = RouteDifficulty.HARD,
                surfaceType = SurfaceType.TRAIL,
                startLocationName = "Hồ Hàm Lợn, Sóc Sơn",
                areaName = "Sóc Sơn",
                isPersonalized = false,
                isSaved = false,
                startPoint = Pair(21.3125, 105.8050),
                routesInAreaCount = 5,
                runnersCount = 890,
                imageUrl = "https://images.unsplash.com/photo-1551632811-561732d1e306?w=400",
                routePoints = listOf(
                    21.3125 to 105.8050,
                    21.3160 to 105.8080,
                    21.3210 to 105.8115,
                    21.3250 to 105.8090,
                    21.3190 to 105.8030,
                    21.3125 to 105.8050
                )
            ),
            SuggestedRoute(
                id = "trail_3",
                title = "Vườn Quốc Gia Ba Vì - Rừng Thông Cổ Thụ & Đỉnh Mây",
                sportType = SportType.TRAIL_RUN,
                distanceKm = 8.50,
                durationMinutes = 80,
                elevationGainM = 310,
                difficulty = RouteDifficulty.MODERATE,
                surfaceType = SurfaceType.TRAIL,
                startLocationName = "Cốt 400 Vườn Quốc Gia Ba Vì",
                areaName = "Ba Vì",
                isPersonalized = true,
                isSaved = false,
                startPoint = Pair(21.0780, 105.3620),
                routesInAreaCount = 3,
                runnersCount = 650,
                imageUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=400",
                routePoints = listOf(
                    21.0780 to 105.3620,
                    21.0820 to 105.3650,
                    21.0860 to 105.3690,
                    21.0830 to 105.3720,
                    21.0790 to 105.3660,
                    21.0780 to 105.3620
                )
            ),
            SuggestedRoute(
                id = "trail_4",
                title = "Cung đường XLT825 - Dốc đê Sông Hồng ngắm hoàng hôn",
                sportType = SportType.TRAIL_RUN,
                distanceKm = 9.40,
                durationMinutes = 68,
                elevationGainM = 85,
                difficulty = RouteDifficulty.MODERATE,
                surfaceType = SurfaceType.MIXED,
                startLocationName = "Chân cầu Vĩnh Tuy",
                areaName = "XLT825",
                isPersonalized = false,
                isSaved = false,
                startPoint = Pair(20.9980, 105.8750),
                routesInAreaCount = 19,
                runnersCount = 1420,
                imageUrl = "https://images.unsplash.com/photo-1502680390469-be75c86b636f?w=400",
                routePoints = listOf(
                    20.9980 to 105.8750,
                    21.0020 to 105.8810,
                    21.0080 to 105.8850,
                    21.0120 to 105.8820,
                    21.0060 to 105.8770,
                    20.9980 to 105.8750
                )
            ),
            SuggestedRoute(
                id = "trail_5",
                title = "Rừng thông Đồng Đò - Cung đường mòn quanh hồ nước xanh",
                sportType = SportType.TRAIL_RUN,
                distanceKm = 7.80,
                durationMinutes = 58,
                elevationGainM = 150,
                difficulty = RouteDifficulty.MODERATE,
                surfaceType = SurfaceType.TRAIL,
                startLocationName = "Đập Hồ Đồng Đò, Sóc Sơn",
                areaName = "Đồng Đò",
                isPersonalized = true,
                isSaved = false,
                startPoint = Pair(21.3200, 105.7950),
                routesInAreaCount = 4,
                runnersCount = 980,
                imageUrl = "https://images.unsplash.com/photo-1448375240586-882707db888b?w=400",
                routePoints = listOf(
                    21.3200 to 105.7950,
                    21.3240 to 105.7980,
                    21.3270 to 105.8020,
                    21.3230 to 105.8050,
                    21.3200 to 105.7950
                )
            ),

            SuggestedRoute(
                id = "run_1",
                title = "Vòng quanh Hồ Tây lộng gió - Cung đường huyền thoại",
                sportType = SportType.RUN,
                distanceKm = 15.60,
                durationMinutes = 85,
                elevationGainM = 25,
                difficulty = RouteDifficulty.MODERATE,
                surfaceType = SurfaceType.PAVED,
                startLocationName = "Đường Thanh Niên, Hà Nội",
                areaName = "Hồ Tây",
                isPersonalized = true,
                isSaved = true,
                startPoint = Pair(21.0435, 105.8360),
                routesInAreaCount = 17,
                runnersCount = 3850,
                imageUrl = "https://images.unsplash.com/photo-1571008887538-b36bb32f4571?w=400",
                routePoints = listOf(
                    21.0435 to 105.8360,
                    21.0410 to 105.8280,
                    21.0430 to 105.8190,
                    21.0530 to 105.8140,
                    21.0660 to 105.8130,
                    21.0740 to 105.8190,
                    21.0730 to 105.8260,
                    21.0640 to 105.8280,
                    21.0550 to 105.8310,
                    21.0480 to 105.8340,
                    21.0435 to 105.8360
                )
            ),
            SuggestedRoute(
                id = "run_2",
                title = "Hồ Hoàn Kiếm - Phố đi bộ sáng sớm bình yên",
                sportType = SportType.RUN,
                distanceKm = 5.20,
                durationMinutes = 28,
                elevationGainM = 10,
                difficulty = RouteDifficulty.EASY,
                surfaceType = SurfaceType.PAVED,
                startLocationName = "Tượng đài Lý Thái Tổ",
                areaName = "Hoàn Kiếm",
                isPersonalized = true,
                isSaved = false,
                startPoint = Pair(21.0285, 105.8542),
                routesInAreaCount = 14,
                runnersCount = 4720,
                imageUrl = "https://images.unsplash.com/photo-1476480862126-209bfaa8edc8?w=400",
                routePoints = listOf(
                    21.0285 to 105.8542,
                    21.0263 to 105.8525,
                    21.0290 to 105.8508,
                    21.0315 to 105.8530,
                    21.0285 to 105.8542
                )
            ),
            SuggestedRoute(
                id = "run_3",
                title = "Công viên Gamuda City - Đô thị xanh Nam Hà Nội",
                sportType = SportType.RUN,
                distanceKm = 7.50,
                durationMinutes = 42,
                elevationGainM = 15,
                difficulty = RouteDifficulty.EASY,
                surfaceType = SurfaceType.PARK,
                startLocationName = "Cổng chính Gamuda Gardens",
                areaName = "Gamuda",
                isPersonalized = false,
                isSaved = false,
                startPoint = Pair(20.9700, 105.8600),
                routesInAreaCount = 12,
                runnersCount = 1260,
                imageUrl = "https://images.unsplash.com/photo-1486218119243-13883505764c?w=400",
                routePoints = listOf(
                    20.9700 to 105.8600,
                    20.9740 to 105.8650,
                    20.9770 to 105.8620,
                    20.9730 to 105.8580,
                    20.9700 to 105.8600
                )
            ),
            SuggestedRoute(
                id = "run_4",
                title = "Công viên Thống Nhất - Vòng hồ Bảy Mẫu bóng mát",
                sportType = SportType.RUN,
                distanceKm = 4.30,
                durationMinutes = 24,
                elevationGainM = 8,
                difficulty = RouteDifficulty.EASY,
                surfaceType = SurfaceType.PARK,
                startLocationName = "Cổng Trần Nhân Tông",
                areaName = "Thống Nhất",
                isPersonalized = true,
                isSaved = false,
                startPoint = Pair(21.0150, 105.8450),
                routesInAreaCount = 8,
                runnersCount = 2190,
                imageUrl = "https://images.unsplash.com/photo-1502680390469-be75c86b636f?w=400",
                routePoints = listOf(
                    21.0150 to 105.8450,
                    21.0180 to 105.8470,
                    21.0200 to 105.8440,
                    21.0170 to 105.8410,
                    21.0150 to 105.8450
                )
            ),
            SuggestedRoute(
                id = "run_5",
                title = "Đại lộ Ecopark - Cung đường marathon giữa hai hàng cọ",
                sportType = SportType.RUN,
                distanceKm = 10.50,
                durationMinutes = 58,
                elevationGainM = 12,
                difficulty = RouteDifficulty.EASY,
                surfaceType = SurfaceType.PAVED,
                startLocationName = "Khu đô thị Ecopark",
                areaName = "Ecopark",
                isPersonalized = false,
                isSaved = false,
                startPoint = Pair(20.9650, 105.9250),
                routesInAreaCount = 6,
                runnersCount = 890,
                imageUrl = "https://images.unsplash.com/photo-1530549387789-4c1017266635?w=400",
                routePoints = listOf(
                    20.9650 to 105.9250,
                    20.9700 to 105.9310,
                    20.9760 to 105.9380,
                    20.9720 to 105.9420,
                    20.9650 to 105.9250
                )
            ),

            SuggestedRoute(
                id = "walk_1",
                title = "Đi bộ ban đêm ngắm phố cổ & Cầu Long Biên",
                sportType = SportType.WALK,
                distanceKm = 6.00,
                durationMinutes = 70,
                elevationGainM = 20,
                difficulty = RouteDifficulty.EASY,
                surfaceType = SurfaceType.PAVED,
                startLocationName = "Chợ Đồng Xuân, Hoàn Kiếm",
                areaName = "Đi bộ ban đêm",
                isPersonalized = true,
                isSaved = true,
                startPoint = Pair(21.0360, 105.8490),
                routesInAreaCount = 8,
                runnersCount = 3120,
                imageUrl = "https://images.unsplash.com/photo-1517649763962-0c623266ddc0?w=400",
                routePoints = listOf(
                    21.0360 to 105.8490,
                    21.0390 to 105.8540,
                    21.0430 to 105.8590,
                    21.0400 to 105.8630,
                    21.0360 to 105.8490
                )
            ),
            SuggestedRoute(
                id = "walk_2",
                title = "Tản bộ ven hồ Linh Đàm & Khu bán đảo xanh",
                sportType = SportType.WALK,
                distanceKm = 4.20,
                durationMinutes = 52,
                elevationGainM = 10,
                difficulty = RouteDifficulty.EASY,
                surfaceType = SurfaceType.PARK,
                startLocationName = "Công viên Bán Đảo Linh Đàm",
                areaName = "Vân Điển",
                isPersonalized = false,
                isSaved = false,
                startPoint = Pair(20.9650, 105.8280),
                routesInAreaCount = 2,
                runnersCount = 670,
                imageUrl = "https://images.unsplash.com/photo-1470246973918-29a93221c455?w=400",
                routePoints = listOf(
                    20.9650 to 105.8280,
                    20.9680 to 105.8320,
                    20.9710 to 105.8300,
                    20.9670 to 105.8250,
                    20.9650 to 105.8280
                )
            ),
            SuggestedRoute(
                id = "walk_3",
                title = "Lối tản bộ thư giãn quanh Hồ Trúc Bạch",
                sportType = SportType.WALK,
                distanceKm = 3.50,
                durationMinutes = 44,
                elevationGainM = 5,
                difficulty = RouteDifficulty.EASY,
                surfaceType = SurfaceType.PAVED,
                startLocationName = "Bán đảo Trúc Bạch",
                areaName = "Trúc Bạch",
                isPersonalized = true,
                isSaved = false,
                startPoint = Pair(21.0480, 105.8380),
                routesInAreaCount = 5,
                runnersCount = 1480,
                imageUrl = "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=400",
                routePoints = listOf(
                    21.0480 to 105.8380,
                    21.0520 to 105.8410,
                    21.0540 to 105.8370,
                    21.0500 to 105.8340,
                    21.0480 to 105.8380
                )
            ),
            SuggestedRoute(
                id = "walk_4",
                title = "Vườn hoa Bách Thảo - Không gian xanh tĩnh lặng",
                sportType = SportType.WALK,
                distanceKm = 2.80,
                durationMinutes = 38,
                elevationGainM = 15,
                difficulty = RouteDifficulty.EASY,
                surfaceType = SurfaceType.PARK,
                startLocationName = "Đường Hoàng Hoa Thám",
                areaName = "Bách Thảo",
                isPersonalized = false,
                isSaved = false,
                startPoint = Pair(21.0410, 105.8290),
                routesInAreaCount = 3,
                runnersCount = 820,
                imageUrl = "https://images.unsplash.com/photo-1473448912268-2022ce9509d8?w=400",
                routePoints = listOf(
                    21.0410 to 105.8290,
                    21.0440 to 105.8320,
                    21.0420 to 105.8350,
                    21.0390 to 105.8310,
                    21.0410 to 105.8290
                )
            ),

            SuggestedRoute(
                id = "ride_1",
                title = "Cung đường Cầu Nhật Tân - Sông Hồng đón gió",
                sportType = SportType.RIDE,
                distanceKm = 24.50,
                durationMinutes = 70,
                elevationGainM = 60,
                difficulty = RouteDifficulty.MODERATE,
                surfaceType = SurfaceType.PAVED,
                startLocationName = "Võ Chí Công, Tây Hồ",
                areaName = "Hồ Tây",
                isPersonalized = true,
                isSaved = false,
                startPoint = Pair(21.0750, 105.8150),
                routesInAreaCount = 9,
                runnersCount = 2350,
                imageUrl = "https://images.unsplash.com/photo-1485965120184-e220f721d03e?w=400",
                routePoints = listOf(
                    21.0750 to 105.8150,
                    21.0950 to 105.8200,
                    21.1150 to 105.8250,
                    21.1050 to 105.8350,
                    21.0850 to 105.8280,
                    21.0750 to 105.8150
                )
            ),
            SuggestedRoute(
                id = "ride_2",
                title = "Đại lộ Thăng Long - Chinh phục Hòa Lạc",
                sportType = SportType.RIDE,
                distanceKm = 35.00,
                durationMinutes = 95,
                elevationGainM = 90,
                difficulty = RouteDifficulty.HARD,
                surfaceType = SurfaceType.PAVED,
                startLocationName = "Trung tâm Hội nghị Quốc Gia",
                areaName = "Thường Tín",
                isPersonalized = false,
                isSaved = false,
                startPoint = Pair(21.0050, 105.7850),
                routesInAreaCount = 4,
                runnersCount = 1180,
                imageUrl = "https://images.unsplash.com/photo-1544197150-b99a580bb7a8?w=400",
                routePoints = listOf(
                    21.0050 to 105.7850,
                    21.0020 to 105.7500,
                    20.9980 to 105.7100,
                    20.9950 to 105.6700,
                    21.0050 to 105.7850
                )
            ),
            SuggestedRoute(
                id = "ride_3",
                title = "Đê Bát Tràng - Cung đường làng gốm ven sông",
                sportType = SportType.RIDE,
                distanceKm = 21.00,
                durationMinutes = 62,
                elevationGainM = 35,
                difficulty = RouteDifficulty.EASY,
                surfaceType = SurfaceType.PAVED,
                startLocationName = "Chân Cầu Vĩnh Tuy, Long Biên",
                areaName = "Bát Tràng",
                isPersonalized = true,
                isSaved = false,
                startPoint = Pair(21.0020, 105.8820),
                routesInAreaCount = 7,
                runnersCount = 1620,
                imageUrl = "https://images.unsplash.com/photo-1511994298241-608e28f14fde?w=400",
                routePoints = listOf(
                    21.0020 to 105.8820,
                    20.9850 to 105.8950,
                    20.9680 to 105.9120,
                    20.9750 to 105.9180,
                    21.0020 to 105.8820
                )
            ),
            SuggestedRoute(
                id = "ride_4",
                title = "Thử thách Dốc Đền Gióng Sóc Sơn",
                sportType = SportType.RIDE,
                distanceKm = 38.00,
                durationMinutes = 110,
                elevationGainM = 280,
                difficulty = RouteDifficulty.HARD,
                surfaceType = SurfaceType.PAVED,
                startLocationName = "Khu đô thị Ciputra",
                areaName = "Sóc Sơn",
                isPersonalized = false,
                isSaved = false,
                startPoint = Pair(21.0850, 105.8000),
                routesInAreaCount = 5,
                runnersCount = 740,
                imageUrl = "https://images.unsplash.com/photo-1507035895480-2b3156c31fc8?w=400",
                routePoints = listOf(
                    21.0850 to 105.8000,
                    21.1400 to 105.8150,
                    21.2200 to 105.8250,
                    21.2800 to 105.8300,
                    21.0850 to 105.8000
                )
            )
        )
    }
}

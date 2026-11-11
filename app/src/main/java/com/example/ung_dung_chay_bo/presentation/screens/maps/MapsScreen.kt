package com.example.ung_dung_chay_bo.presentation.screens.maps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ung_dung_chay_bo.core.theme.*
import com.example.ung_dung_chay_bo.domain.model.RouteDifficulty
import com.example.ung_dung_chay_bo.domain.model.SportType
import com.example.ung_dung_chay_bo.domain.model.SuggestedRoute
import com.example.ung_dung_chay_bo.domain.model.SurfaceType
import com.example.ung_dung_chay_bo.presentation.screens.maps.components.MapTilerLayerStyle
import com.example.ung_dung_chay_bo.presentation.screens.maps.components.MapTilerView

@Composable
fun MapsScreen(
    onStartRouteClick: (SuggestedRoute) -> Unit,
    viewModel: MapsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isSheetExpanded by remember { mutableStateOf(false) }
    var showCreateRouteDialog by remember { mutableStateOf(false) }

    var currentMapStyle by remember { mutableStateOf(MapTilerLayerStyle.OUTDOOR) }
    var is3DMode by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        MapTilerView(
            routes = uiState.routes,
            selectedRoute = uiState.selectedRoute,
            userLocation = uiState.userLocation,
            layerStyle = currentMapStyle,
            is3D = is3DMode,
            onSelectRoute = { viewModel.selectRoute(it) },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            SportAndSearchTopBox(
                selectedSport = uiState.selectedSport,
                isDropdownExpanded = uiState.isSportDropdownExpanded,
                onDropdownToggle = { viewModel.setSportDropdownExpanded(it) },
                onSportSelect = { viewModel.selectSport(it) },
                searchQuery = uiState.searchQuery,
                onSearchChange = { viewModel.onSearchQueryChange(it) },
                isSavedOnly = uiState.isSavedOnly,
                onToggleSavedOnly = { viewModel.toggleSavedOnly() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            FilterChipsRow(
                uiState = uiState,
                onDistanceSelect = { viewModel.setDistanceFilter(it) },
                onDifficultySelect = { viewModel.setDifficultyFilter(it) },
                onSurfaceSelect = { viewModel.setSurfaceFilter(it) },
                onPopularToggle = { viewModel.togglePopularOnly() }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                SearchHerePillButton(onClick = { viewModel.searchInCurrentArea() })
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MapLayerButton(
                currentStyle = currentMapStyle,
                onClick = {
                    currentMapStyle = when (currentMapStyle) {
                        MapTilerLayerStyle.OUTDOOR -> MapTilerLayerStyle.STREETS
                        MapTilerLayerStyle.STREETS -> MapTilerLayerStyle.SATELLITE
                        MapTilerLayerStyle.SATELLITE -> MapTilerLayerStyle.OUTDOOR
                    }
                }
            )

            Map3DButton(
                is3D = is3DMode,
                onClick = { is3DMode = !is3DMode }
            )

            MapLocationButton(
                onClick = {
                    val defaultRoute = uiState.routes.firstOrNull { it.areaName == "Khương Trung" } ?: uiState.routes.firstOrNull()
                    defaultRoute?.let { viewModel.selectRoute(it) }
                }
            )

            MapPencilButton(onClick = { showCreateRouteDialog = true })
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            SuggestedRouteBottomCard(
                selectedRoute = uiState.selectedRoute,
                routes = uiState.routes,
                isExpanded = isSheetExpanded,
                onToggleExpand = { isSheetExpanded = !isSheetExpanded },
                onOpenCreateRoute = { showCreateRouteDialog = true },
                onSelectRoute = { viewModel.selectRoute(it) },
                onToggleSave = { viewModel.toggleSaveRoute(it) },
                onStartRouteClick = onStartRouteClick
            )
        }

        if (showCreateRouteDialog) {
            CreateCustomRouteDialog(
                defaultSport = uiState.selectedSport,
                onDismiss = { showCreateRouteDialog = false },
                onSave = { title, sport, dist, dur, area, diff, surface ->
                    viewModel.addUserCreatedRoute(
                        title = title,
                        sportType = sport,
                        distanceKm = dist,
                        durationMinutes = dur,
                        areaName = area,
                        difficulty = diff,
                        surfaceType = surface
                    )
                    showCreateRouteDialog = false
                }
            )
        }
    }
}

@Composable
private fun SportAndSearchTopBox(
    selectedSport: SportType,
    isDropdownExpanded: Boolean,
    onDropdownToggle: (Boolean) -> Unit,
    onSportSelect: (SportType) -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    isSavedOnly: Boolean,
    onToggleSavedOnly: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        color = PureWhite,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onDropdownToggle(true) }
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = getSportIcon(selectedSport),
                        contentDescription = selectedSport.title,
                        tint = SportOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Chọn môn",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { onDropdownToggle(false) },
                    modifier = Modifier.background(PureWhite)
                ) {
                    SportType.entries.forEach { sport ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = getSportIcon(sport),
                                        contentDescription = null,
                                        tint = if (sport == selectedSport) SportOrange else TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = sport.title,
                                        fontWeight = if (sport == selectedSport) FontWeight.Bold else FontWeight.Normal,
                                        color = if (sport == selectedSport) SportOrange else TextPrimary
                                    )
                                }
                            },
                            onClick = { onSportSelect(sport) }
                        )
                    }
                }
            }

            VerticalDivider(
                modifier = Modifier
                    .height(24.dp)
                    .padding(horizontal = 6.dp),
                color = LightBorder
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (searchQuery.isEmpty()) {
                    Text(
                        text = when (selectedSport) {
                            SportType.TRAIL_RUN -> "Chạy địa hình ngắm cảnh"
                            SportType.RUN -> "Khám phá cung đường chạy"
                            SportType.RIDE -> "Lộ trình đạp xe thoáng đãng"
                            SportType.WALK -> "Đi bộ thư giãn & ngắm phố"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = SportOrange
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                )
            }

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToggleSavedOnly() },
                color = if (isSavedOnly) SportOrangeSoft else Color(0xFFF3F4F6),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isSavedOnly) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Đã lưu",
                        tint = if (isSavedOnly) SportOrange else TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Đã lưu",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSavedOnly) SportOrange else TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChipsRow(
    uiState: MapsUiState,
    onDistanceSelect: (DistanceFilter) -> Unit,
    onDifficultySelect: (RouteDifficulty?) -> Unit,
    onSurfaceSelect: (SurfaceType?) -> Unit,
    onPopularToggle: () -> Unit
) {
    var isDistanceMenuOpen by remember { mutableStateOf(false) }
    var isDifficultyMenuOpen by remember { mutableStateOf(false) }
    var isSurfaceMenuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Chip lọc Lộ trình phổ biến nhất (nhiều người chạy)
        FilterChipItem(
            title = if (uiState.isPopularOnly) "Nhiều người chạy nhất" else "Phổ biến",
            isSelected = uiState.isPopularOnly,
            onClick = onPopularToggle
        )

        Surface(
            modifier = Modifier.shadow(2.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = PureWhite,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, SportOrange)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lộ trình",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SportOrange
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = SportOrange,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Box {
            FilterChipItem(
                title = if (uiState.activeDistanceFilter == DistanceFilter.ALL) "Độ dài" else uiState.activeDistanceFilter.title,
                isSelected = uiState.activeDistanceFilter != DistanceFilter.ALL,
                onClick = { isDistanceMenuOpen = true }
            )
            DropdownMenu(
                expanded = isDistanceMenuOpen,
                onDismissRequest = { isDistanceMenuOpen = false },
                modifier = Modifier.background(PureWhite)
            ) {
                DistanceFilter.entries.forEach { dist ->
                    DropdownMenuItem(
                        text = { Text(dist.title, color = TextPrimary) },
                        onClick = {
                            onDistanceSelect(dist)
                            isDistanceMenuOpen = false
                        }
                    )
                }
            }
        }

        FilterChipItem(
            title = "Độ cao",
            isSelected = false,
            onClick = { }
        )

        Box {
            FilterChipItem(
                title = uiState.activeSurfaceFilter?.title ?: "Bề mặt",
                isSelected = uiState.activeSurfaceFilter != null,
                onClick = { isSurfaceMenuOpen = true }
            )
            DropdownMenu(
                expanded = isSurfaceMenuOpen,
                onDismissRequest = { isSurfaceMenuOpen = false },
                modifier = Modifier.background(PureWhite)
            ) {
                DropdownMenuItem(
                    text = { Text("Tất cả", color = TextPrimary) },
                    onClick = {
                        onSurfaceSelect(null)
                        isSurfaceMenuOpen = false
                    }
                )
                SurfaceType.entries.filter { it != SurfaceType.ALL }.forEach { surface ->
                    DropdownMenuItem(
                        text = { Text(surface.title, color = TextPrimary) },
                        onClick = {
                            onSurfaceSelect(surface)
                            isSurfaceMenuOpen = false
                        }
                    )
                }
            }
        }

        Box {
            FilterChipItem(
                title = uiState.activeDifficultyFilter?.title ?: "Khó khăn",
                isSelected = uiState.activeDifficultyFilter != null,
                onClick = { isDifficultyMenuOpen = true }
            )
            DropdownMenu(
                expanded = isDifficultyMenuOpen,
                onDismissRequest = { isDifficultyMenuOpen = false },
                modifier = Modifier.background(PureWhite)
            ) {
                DropdownMenuItem(
                    text = { Text("Tất cả", color = TextPrimary) },
                    onClick = {
                        onDifficultySelect(null)
                        isDifficultyMenuOpen = false
                    }
                )
                RouteDifficulty.entries.forEach { diff ->
                    DropdownMenuItem(
                        text = { Text(diff.title, color = TextPrimary) },
                        onClick = {
                            onDifficultySelect(diff)
                            isDifficultyMenuOpen = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChipItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .shadow(2.dp, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) SportOrangeSoft else PureWhite,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) SportOrange else LightBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) SportOrange else TextPrimary
            )
        }
    }
}

@Composable
private fun SearchHerePillButton(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .shadow(6.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .clickable { onClick() },
        color = SportOrange,
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tìm kiếm tại đây",
                color = PureWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MapLayerButton(currentStyle: MapTilerLayerStyle, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(PureWhite)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Layers,
            contentDescription = "Đổi loại bản đồ",
            tint = SportOrange,
            modifier = Modifier.size(24.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-2).dp, y = 2.dp)
                .size(15.dp)
                .clip(CircleShape)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (currentStyle) {
                    MapTilerLayerStyle.OUTDOOR -> "O"
                    MapTilerLayerStyle.STREETS -> "S"
                    MapTilerLayerStyle.SATELLITE -> "V"
                },
                color = PureWhite,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun Map3DButton(is3D: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(if (is3D) SportOrangeSoft else PureWhite)
            .border(if (is3D) 1.5.dp else 0.dp, SportOrange, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "3D",
            fontWeight = FontWeight.Black,
            fontSize = 13.sp,
            color = if (is3D) SportOrange else TextPrimary
        )
    }
}

@Composable
private fun MapLocationButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(PureWhite)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.MyLocation,
            contentDescription = "Vị trí hiện tại",
            tint = TextPrimary,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun MapPencilButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(PureWhite)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Tạo lộ trình",
            tint = TextPrimary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SuggestedRouteBottomCard(
    selectedRoute: SuggestedRoute?,
    routes: List<SuggestedRoute>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onOpenCreateRoute: () -> Unit,
    onSelectRoute: (SuggestedRoute) -> Unit,
    onToggleSave: (String) -> Unit,
    onStartRouteClick: (SuggestedRoute) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        color = PureWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFCBD5E1))
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lộ trình gợi ý (${routes.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                TextButton(
                    onClick = onOpenCreateRoute,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = SportOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Tạo lộ trình",
                        color = SportOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            if (selectedRoute != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getSportIcon(selectedRoute.sportType),
                            contentDescription = null,
                            tint = SportOrange.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(selectedRoute.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = selectedRoute.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedRoute.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onToggleSave(selectedRoute.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (selectedRoute.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = "Lưu lộ trình",
                                    tint = if (selectedRoute.isSaved) SportOrange else TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = when (selectedRoute.difficulty) {
                                    RouteDifficulty.EASY -> Color(0xFFDCFCE7)
                                    RouteDifficulty.MODERATE -> Color(0xFFFEF9C3)
                                    RouteDifficulty.HARD -> Color(0xFFFEE2E2)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = getSportIcon(selectedRoute.sportType),
                                        contentDescription = null,
                                        tint = when (selectedRoute.difficulty) {
                                            RouteDifficulty.EASY -> SuccessGreen
                                            RouteDifficulty.MODERATE -> PauseYellow
                                            RouteDifficulty.HARD -> ErrorRed
                                        },
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = selectedRoute.difficulty.title,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (selectedRoute.difficulty) {
                                            RouteDifficulty.EASY -> SuccessGreen
                                            RouteDifficulty.MODERATE -> Color(0xFFB45309)
                                            RouteDifficulty.HARD -> ErrorRed
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = if (selectedRoute.runnersCount > 0) {
                                    "%.2f km · %s · %s".format(
                                        selectedRoute.distanceKm,
                                        selectedRoute.durationFormatted,
                                        selectedRoute.runnersCountFormatted
                                    )
                                } else {
                                    "%.2f km · %s".format(
                                        selectedRoute.distanceKm,
                                        selectedRoute.durationFormatted
                                    )
                                },
                                fontSize = 12.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.NearMe,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = selectedRoute.startLocationName,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        if (selectedRoute.isPersonalized) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = SportOrange,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "Được thiết kế riêng cho bạn",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SportOrange
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { onStartRouteClick(selectedRoute) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(44.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SportOrange)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Bắt đầu với lộ trình này",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PureWhite
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    HorizontalDivider(color = LightBorder, thickness = 0.5.dp)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tự tin khám phá",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${routes.size} lộ trình",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onOpenCreateRoute() },
                                color = SportOrangeSoft,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.2.dp, SportOrange.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(SportOrange),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Tạo mới",
                                            tint = PureWhite,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Tạo lộ trình cho riêng bạn",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = SportOrange
                                        )
                                        Text(
                                            text = "Tự đặt cự ly, thời gian và địa điểm tập luyện",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = SportOrange,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        items(routes) { route ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onSelectRoute(route) },
                                color = if (selectedRoute?.id == route.id) SportOrangeSoft else Color(0xFFF8F9FA),
                                shape = RoundedCornerShape(12.dp),
                                border = if (selectedRoute?.id == route.id) BorderStroke(1.dp, SportOrange) else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = getSportIcon(route.sportType),
                                        contentDescription = null,
                                        tint = SportOrange,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = route.title,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = TextPrimary,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = if (route.runnersCount > 0) {
                                                "${route.areaName} • ${route.distanceKm} km • ${route.runnersCountFormatted}"
                                            } else {
                                                "${route.areaName} • ${route.distanceKm} km • ${route.durationFormatted}"
                                            },
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateCustomRouteDialog(
    defaultSport: SportType,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        sportType: SportType,
        distanceKm: Double,
        durationMinutes: Int,
        areaName: String,
        difficulty: RouteDifficulty,
        surfaceType: SurfaceType
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedSport by remember { mutableStateOf(defaultSport) }
    var distanceStr by remember { mutableStateOf("5.0") }
    var durationStr by remember { mutableStateOf("30") }
    var areaName by remember { mutableStateOf("Hà Nội") }
    var selectedDifficulty by remember { mutableStateOf(RouteDifficulty.EASY) }
    var selectedSurface by remember { mutableStateOf(SurfaceType.PAVED) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = PureWhite,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SportOrangeSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditRoad,
                                contentDescription = null,
                                tint = SportOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Tạo lộ trình mới",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "TÊN LỘ TRÌNH *",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    placeholder = { Text("Vd: Chạy quanh Hồ Tây buổi sáng", fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SportOrange,
                        unfocusedBorderColor = LightBorder
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "MÔN THỂ THAO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SportType.entries.forEach { sport ->
                        val isSelected = sport == selectedSport
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedSport = sport },
                            color = if (isSelected) SportOrange else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = getSportIcon(sport),
                                    contentDescription = sport.title,
                                    tint = if (isSelected) PureWhite else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = sport.title,
                                    fontSize = 9.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) PureWhite else TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CỰ LY (KM) *",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = distanceStr,
                            onValueChange = { distanceStr = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SportOrange,
                                unfocusedBorderColor = LightBorder
                            )
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "THỜI GIAN (PHÚT)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = durationStr,
                            onValueChange = { durationStr = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SportOrange,
                                unfocusedBorderColor = LightBorder
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "KHU VỰC / QUẬN HUYỆN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = areaName,
                    onValueChange = { areaName = it },
                    placeholder = { Text("Vd: Ba Đình, Cầu Giấy...", fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SportOrange,
                        unfocusedBorderColor = LightBorder
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "MỨC ĐỘ THỬ THÁCH",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RouteDifficulty.entries.forEach { diff ->
                        val isSelected = diff == selectedDifficulty
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedDifficulty = diff },
                            color = if (isSelected) SportOrangeSoft else Color(0xFFF8F9FA),
                            shape = RoundedCornerShape(10.dp),
                            border = if (isSelected) BorderStroke(1.2.dp, SportOrange) else BorderStroke(0.8.dp, LightBorder)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = diff.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) SportOrange else TextPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "MẶT ĐƯỜNG",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(SurfaceType.PAVED, SurfaceType.PARK, SurfaceType.TRAIL, SurfaceType.MIXED).forEach { surface ->
                        val isSelected = surface == selectedSurface
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedSurface = surface },
                            color = if (isSelected) SportOrangeSoft else Color(0xFFF8F9FA),
                            shape = RoundedCornerShape(8.dp),
                            border = if (isSelected) BorderStroke(1.dp, SportOrange) else BorderStroke(0.5.dp, LightBorder)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = surface.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) SportOrange else TextPrimary
                                )
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = ErrorRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, LightBorder)
                    ) {
                        Text(text = "Hủy", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            val dist = distanceStr.toDoubleOrNull()
                            val dur = durationStr.toIntOrNull() ?: 30
                            if (title.isBlank()) {
                                errorMessage = "Vui lòng nhập tên lộ trình"
                                return@Button
                            }
                            if (dist == null || dist <= 0.0) {
                                errorMessage = "Vui lòng nhập cự ly hợp lệ (> 0 km)"
                                return@Button
                            }
                            onSave(
                                title.trim(),
                                selectedSport,
                                dist,
                                dur,
                                areaName.ifBlank { "Hà Nội" },
                                selectedDifficulty,
                                selectedSurface
                            )
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SportOrange),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Lưu lộ trình",
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

private fun getSportIcon(sport: SportType): ImageVector {
    return when (sport) {
        SportType.RUN -> Icons.AutoMirrored.Filled.DirectionsRun
        SportType.WALK -> Icons.AutoMirrored.Filled.DirectionsWalk
        SportType.RIDE -> Icons.AutoMirrored.Filled.DirectionsBike
        SportType.TRAIL_RUN -> Icons.Default.Landscape
    }
}

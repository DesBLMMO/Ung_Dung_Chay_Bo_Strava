package com.example.ung_dung_chay_bo.presentation.screens.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ung_dung_chay_bo.core.theme.*
import com.example.ung_dung_chay_bo.core.utils.GpsUtils
import com.example.ung_dung_chay_bo.data.local.entity.ActivityRecordEntity
import com.example.ung_dung_chay_bo.domain.model.SportType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onStartRunClick: () -> Unit,
    onActivityClick: (activityId: Long) -> Unit,
    onBackClick: (() -> Unit)? = null,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Lịch sử hoạt động",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Quay lại",
                                tint = TextPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PureWhite)
            )
        },
        containerColor = LightBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Thẻ tổng quan thành tích (Overview Header Card)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, LightBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "TỔNG THÀNH TÍCH TÍCH LŨY",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            HistoryStatItem(
                                label = "Tổng cự ly",
                                value = "%.1f".format(uiState.totalDistanceKm),
                                unit = "km",
                                modifier = Modifier.weight(1f)
                            )
                            HistoryStatItem(
                                label = "Số buổi tập",
                                value = "${uiState.totalActivitiesCount}",
                                unit = "buổi",
                                modifier = Modifier.weight(1f)
                            )
                            HistoryStatItem(
                                label = "Tổng thời gian",
                                value = formatHoursMinutes(uiState.totalDurationSeconds),
                                unit = "",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 2. Bộ lọc môn thể thao (Sport Filters)
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedSportFilter == null,
                            onClick = { viewModel.setFilter(null) },
                            label = { Text("Tất cả (${uiState.activities.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SportOrange,
                                selectedLabelColor = PureWhite
                            )
                        )
                    }
                    items(SportType.entries) { sport ->
                        val count = uiState.activities.count { it.sportType.equals(sport.name, ignoreCase = true) }
                        FilterChip(
                            selected = uiState.selectedSportFilter == sport,
                            onClick = { viewModel.setFilter(sport) },
                            label = { Text("${sport.displayName} ($count)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = when (sport) {
                                        SportType.RIDE -> Icons.AutoMirrored.Filled.DirectionsBike
                                        SportType.WALK -> Icons.AutoMirrored.Filled.DirectionsWalk
                                        else -> Icons.AutoMirrored.Filled.DirectionsRun
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SportOrange,
                                selectedLabelColor = PureWhite
                            )
                        )
                    }
                }
            }

            // 3. Danh sách hoạt động hoặc Trạng thái trống
            if (uiState.filteredActivities.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        border = BorderStroke(1.dp, LightBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(SportOrangeSoft, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsRun,
                                    contentDescription = null,
                                    tint = SportOrange,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Chưa có hoạt động nào",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Bắt đầu buổi tập đầu tiên để ghi lại lộ trình GPS, tốc độ và phân tích chi tiết!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = onStartRunClick,
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SportOrange,
                                    contentColor = PureWhite
                                )
                            ) {
                                Text(
                                    text = "Bắt đầu ngay",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                items(
                    items = uiState.filteredActivities,
                    key = { it.id }
                ) { activity ->
                    ActivityListItemCard(
                        activity = activity,
                        onClick = { onActivityClick(activity.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityListItemCard(
    activity: ActivityRecordEntity,
    onClick: () -> Unit
) {
    val sport = runCatching { SportType.valueOf(activity.sportType) }.getOrDefault(SportType.RUN)
    val date = Date(activity.timestamp)
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy · HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(0.5.dp, LightBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(SportOrangeSoft, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (sport) {
                            SportType.RIDE -> Icons.AutoMirrored.Filled.DirectionsBike
                            SportType.WALK -> Icons.AutoMirrored.Filled.DirectionsWalk
                            else -> Icons.AutoMirrored.Filled.DirectionsRun
                        },
                        contentDescription = null,
                        tint = SportOrange,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activity.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = dateFormatter.format(date),
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = LightBorder, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Khoảng cách", fontSize = 11.sp, color = TextMuted)
                    Text(
                        text = "%.2f km".format(activity.distanceKm),
                        fontSize = 16.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }

                Column {
                    Text(text = "Thời gian", fontSize = 11.sp, color = TextMuted)
                    Text(
                        text = GpsUtils.formatDuration(activity.durationSeconds),
                        fontSize = 16.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }

                Column {
                    Text(text = "Pace TB", fontSize = 11.sp, color = TextMuted)
                    Text(
                        text = activity.avgPace,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }

                Column {
                    Text(text = "Calo", fontSize = 11.sp, color = TextMuted)
                    Text(
                        text = "${activity.calories} kcal",
                        fontSize = 16.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryStatItem(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            if (unit.isNotBlank()) {
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = unit,
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

private fun formatHoursMinutes(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

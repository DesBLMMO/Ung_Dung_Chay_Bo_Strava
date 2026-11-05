package com.example.ung_dung_chay_bo.presentation.screens.profile

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ung_dung_chay_bo.core.theme.ErrorRed
import com.example.ung_dung_chay_bo.core.theme.PureWhite
import com.example.ung_dung_chay_bo.core.theme.SportOrange
import com.example.ung_dung_chay_bo.domain.model.RunningGoal
import com.example.ung_dung_chay_bo.domain.model.SportType
import com.example.ung_dung_chay_bo.domain.model.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogoutSuccess: () -> Unit,
    onActivityClick: (activityId: Long) -> Unit = {},
    onViewAllHistoryClick: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profile = uiState.profile

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }
    var showSportSelector by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            onLogoutSuccess()
        }
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            currentProfile = profile ?: UserProfile(),
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, height, weight, goal ->
                viewModel.updateProfile(name, height, weight, goal)
                showEditProfileDialog = false
            }
        )
    }

    if (showGoalDialog) {
        EditGoalDialog(
            currentGoalKm = uiState.weeklyGoalKm,
            onDismiss = { showGoalDialog = false },
            onSave = { newGoal ->
                viewModel.updateWeeklyGoal(newGoal)
                showGoalDialog = false
            }
        )
    }

    if (showSettingsMenu) {
        ProfileSettingsSheet(
            onDismiss = { showSettingsMenu = false },
            onLogout = {
                showSettingsMenu = false
                viewModel.logout()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
    ) {
        Surface(
            color = PureWhite,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color(0xFFE5E7EB), CircleShape)
                        .clickable {  },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Thêm hoạt động",
                        tint = Color(0xFF1F2937),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color(0xFFE5E7EB), CircleShape)
                            .clickable {  },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = "Tìm kiếm",
                            tint = Color(0xFF1F2937),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color(0xFFE5E7EB), CircleShape)
                            .clickable { showSettingsMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Cài đặt tài khoản",
                            tint = Color(0xFF1F2937),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Surface(
                    color = PureWhite,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE5E7EB)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(42.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = profile?.displayName?.ifBlank { "Dũng Trần" } ?: "Dũng Trần",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${uiState.activitiesCount} hoạt động",
                                    fontSize = 13.5.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "${uiState.followersCount} người theo dõi  •  ${uiState.followingCount} đang theo dõi",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1F2937)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showEditProfileDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                shape = RoundedCornerShape(20.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1D5DB)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1F2937)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Chỉnh sửa hồ sơ",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "Theo dõi hành trình chạy bộ của tôi trên Strava: https://strava.app.link/runner")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Chia sẻ hồ sơ"))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                shape = RoundedCornerShape(20.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1D5DB)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1F2937)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Chia sẻ hồ sơ",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            item {
                Surface(
                    color = PureWhite,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TabRow(
                        selectedTabIndex = uiState.selectedSubTab.ordinal,
                        containerColor = PureWhite,
                        contentColor = SportOrange,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[uiState.selectedSubTab.ordinal]),
                                color = SportOrange,
                                height = 3.dp
                            )
                        },
                        divider = { HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.8.dp) }
                    ) {
                        ProfileSubTab.entries.forEach { tab ->
                            val isSelected = uiState.selectedSubTab == tab
                            Tab(
                                selected = isSelected,
                                onClick = { viewModel.selectSubTab(tab) },
                                icon = {
                                    val icon = when (tab) {
                                        ProfileSubTab.PROGRESS -> Icons.Outlined.Assessment
                                        ProfileSubTab.ACTIVITIES -> Icons.Default.ShowChart
                                        ProfileSubTab.MORE -> Icons.Outlined.Menu
                                    }
                                    Icon(
                                        icon,
                                        contentDescription = tab.title,
                                        tint = if (isSelected) SportOrange else Color(0xFF6B7280),
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                text = {
                                    Text(
                                        text = tab.title,
                                        fontSize = 12.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) SportOrange else Color(0xFF6B7280)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            when (uiState.selectedSubTab) {
                ProfileSubTab.PROGRESS -> {
                    item {
                        Spacer(modifier = Modifier.height(14.dp))
                        WeeklyStatsCard(
                            selectedSport = uiState.selectedSport,
                            weeklyDistanceKm = uiState.weeklyDistanceKm,
                            weeklyDurationMinutes = uiState.weeklyDurationMinutes,
                            weeklyElevationGainM = uiState.weeklyElevationGainM,
                            twelveWeeksProgress = uiState.twelveWeeksProgress,
                            onToggleSport = {
                                val nextSport = if (uiState.selectedSport == SportType.RUN) SportType.RIDE else SportType.RUN
                                viewModel.selectSport(nextSport)
                            },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(14.dp))
                        StreakCard(
                            streakWeeks = uiState.streakWeeks,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(14.dp))
                        WeeklyGoalCard(
                            goal = profile?.goal ?: RunningGoal.STAMINA,
                            targetKm = uiState.weeklyGoalKm,
                            currentKm = uiState.weeklyDistanceKm,
                            onEditGoalClick = { showGoalDialog = true },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                ProfileSubTab.ACTIVITIES -> {
                    item {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Lịch sử hoạt động gần đây",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                            TextButton(onClick = onViewAllHistoryClick) {
                                Text(
                                    text = "Xem tất cả",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SportOrange
                                )
                            }
                        }
                    }

                    if (uiState.recentActivities.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                                        contentDescription = null,
                                        tint = SportOrange,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Chưa có hoạt động nào được lưu",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF111827)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Các buổi chạy hoặc đạp xe bạn hoàn thành sẽ được đồng bộ và lưu tại đây.",
                                        fontSize = 13.sp,
                                        color = Color(0xFF6B7280),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(uiState.recentActivities) { activity ->
                            ActivityHistoryCard(
                                activity = activity,
                                onClick = { onActivityClick(activity.recordId) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                ProfileSubTab.MORE -> {
                    item {
                        Spacer(modifier = Modifier.height(14.dp))
                        MoreSettingsTabContent(
                            profile = profile,
                            onEditProfile = { showEditProfileDialog = true },
                            onLogout = { showSettingsMenu = true },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyStatsCard(
    selectedSport: SportType,
    weeklyDistanceKm: Double,
    weeklyDurationMinutes: Int,
    weeklyElevationGainM: Int,
    twelveWeeksProgress: List<Double>,
    onToggleSport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(18.dp)),
        color = PureWhite,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, SportOrange.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                    .clickable { onToggleSport() }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val sportIcon = if (selectedSport == SportType.RUN) {
                        Icons.AutoMirrored.Filled.DirectionsRun
                    } else {
                        Icons.AutoMirrored.Filled.DirectionsBike
                    }
                    Icon(
                        imageVector = sportIcon,
                        contentDescription = null,
                        tint = SportOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = selectedSport.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SportOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Tuần này",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Quãng đường", fontSize = 12.sp, color = Color(0xFF6B7280))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "%.1f km".format(weeklyDistanceKm),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                }

                Column {
                    Text("Thời gian", fontSize = 12.sp, color = Color(0xFF6B7280))
                    Spacer(modifier = Modifier.height(2.dp))
                    val hours = weeklyDurationMinutes / 60
                    val mins = weeklyDurationMinutes % 60
                    val timeStr = if (hours > 0) "${hours}g ${mins}p" else "${mins}phút"
                    Text(
                        text = timeStr,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                }

                Column {
                    Text("Độ cao tăng", fontSize = 12.sp, color = Color(0xFF6B7280))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$weeklyElevationGainM m",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "12 tuần qua",
                fontSize = 13.sp,
                color = Color(0xFF6B7280),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(10.dp))

            StravaLineChart(
                dataPoints = twelveWeeksProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            )
        }
    }
}

@Composable
private fun StravaLineChart(
    dataPoints: List<Double>,
    modifier: Modifier = Modifier
) {
    val maxVal = (dataPoints.maxOrNull() ?: 10.0).coerceAtLeast(6.0)

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val chartRightPadding = 48.dp.toPx()
            val chartBottomPadding = 24.dp.toPx()

            val chartWidth = width - chartRightPadding
            val chartHeight = height - chartBottomPadding

            val gridLevels = listOf(0.0, maxVal / 2, maxVal)
            gridLevels.forEach { level ->
                val y = chartHeight - (level / maxVal * chartHeight).toFloat()
                drawLine(
                    color = Color(0xFFE5E7EB),
                    start = Offset(0f, y),
                    end = Offset(chartWidth, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            drawLine(
                color = Color(0xFF111827),
                start = Offset(chartWidth, 0f),
                end = Offset(chartWidth, chartHeight),
                strokeWidth = 2.dp.toPx()
            )

            if (dataPoints.size >= 2) {
                val stepX = chartWidth / (dataPoints.size - 1)
                val path = Path()

                dataPoints.forEachIndexed { index, value ->
                    val x = index * stepX
                    val y = chartHeight - (value / maxVal * chartHeight).toFloat()
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = SportOrange,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )

                dataPoints.forEachIndexed { index, value ->
                    val x = index * stepX
                    val y = chartHeight - (value / maxVal * chartHeight).toFloat()
                    drawCircle(
                        color = PureWhite,
                        radius = 4.5.dp.toPx(),
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = SportOrange,
                        radius = 3.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .fillMaxHeight()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Text("%.0f km".format(maxVal), fontSize = 10.sp, color = Color(0xFF6B7280))
            Text("%.0f km".format(maxVal / 2), fontSize = 10.sp, color = Color(0xFF6B7280))
            Text("0 km", fontSize = 10.sp, color = Color(0xFF6B7280))
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(end = 48.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text("THG 8", fontSize = 10.5.sp, color = Color(0xFF9CA3AF))
            Text("THG 9", fontSize = 10.5.sp, color = Color(0xFF9CA3AF))
            Text("THG 10", fontSize = 10.5.sp, color = Color(0xFF9CA3AF))
        }
    }
}

@Composable
private fun StreakCard(
    streakWeeks: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(18.dp)),
        color = PureWhite,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chuỗi liên tiếp",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Tháng này",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280),
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(if (streakWeeks > 0) Color(0xFFFFECE5) else Color(0xFFF3F4F6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = if (streakWeeks > 0) SportOrange else Color(0xFF9CA3AF),
                            modifier = Modifier.size(42.dp)
                        )
                        Text(
                            text = "$streakWeeks",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = PureWhite,
                            modifier = Modifier.offset(y = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Tuần",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val rows = 4
                    val cols = 7
                    for (r in 0 until rows) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (c in 0 until cols) {
                                val isHighlighted = (r == 1 && c == 3) || (r == 2 && c == 1) || (r == 3 && c == 4)
                                val isCurrent = (r == 2 && c == 4)
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isHighlighted -> SportOrange
                                                isCurrent -> Color.Transparent
                                                else -> Color(0xFFE5E7EB)
                                            }
                                        )
                                        .border(
                                            if (isCurrent) 1.5.dp else 0.dp,
                                            if (isCurrent) Color(0xFF1F2937) else Color.Transparent,
                                            CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyGoalCard(
    goal: RunningGoal,
    targetKm: Double,
    currentKm: Double,
    onEditGoalClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = (currentKm / targetKm).coerceIn(0.0, 1.0).toFloat()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(18.dp)),
        color = PureWhite,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Mục tiêu rèn luyện",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = goal.title,
                        fontSize = 12.5.sp,
                        color = Color(0xFF6B7280)
                    )
                }

                TextButton(onClick = onEditGoalClick) {
                    Text("Thay đổi", color = SportOrange, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "%.1f / %.0f km".format(currentKm, targetKm),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF111827)
                )

                Text(
                    text = "%.0f%% hoàn thành".format(progress * 100),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = SportOrange
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp)
                    .clip(CircleShape),
                color = SportOrange,
                trackColor = Color(0xFFFFECE5)
            )
        }
    }
}

@Composable
private fun ActivityHistoryCard(
    activity: UserActivityItem,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = PureWhite,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val icon = if (activity.sportType == SportType.RUN) {
                        Icons.AutoMirrored.Filled.DirectionsRun
                    } else {
                        Icons.AutoMirrored.Filled.DirectionsBike
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF0EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = SportOrange, modifier = Modifier.size(20.dp))
                    }

                    Column {
                        Text(
                            text = activity.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Text(activity.dateFormatted, fontSize = 11.5.sp, color = Color(0xFF6B7280))
                    }
                }

                Surface(
                    color = Color(0xFFF3F4F6),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "%.2f km".format(activity.distanceKm),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Thời gian", fontSize = 11.sp, color = Color(0xFF6B7280))
                    Text(activity.durationFormatted, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Tốc độ", fontSize = 11.sp, color = Color(0xFF6B7280))
                    Text(activity.paceOrSpeed, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Độ cao", fontSize = 11.sp, color = Color(0xFF6B7280))
                    Text("${activity.elevationGainM} m", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                }
            }
        }
    }
}

@Composable
private fun MoreSettingsTabContent(
    profile: UserProfile?,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = PureWhite,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Thông tin thể chất", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF111827))
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Chiều cao", color = Color(0xFF6B7280), fontSize = 13.sp)
                    Text("${profile?.heightCm ?: 175} cm", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Cân nặng", color = Color(0xFF6B7280), fontSize = 13.sp)
                    Text("${profile?.weightKg ?: 68} kg", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Chỉ số BMI ước tính", color = Color(0xFF6B7280), fontSize = 13.sp)
                    val h = (profile?.heightCm ?: 175) / 100.0
                    val w = (profile?.weightKg ?: 68).toDouble()
                    val bmi = w / (h * h)
                    Text("%.1f (Bình thường)".format(bmi), fontWeight = FontWeight.Bold, color = SportOrange, fontSize = 13.sp)
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onLogout() },
            color = PureWhite,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = ErrorRed)
                Text("Cài đặt tài khoản & Đăng xuất", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
            }
        }
    }
}

@Composable
private fun EditProfileDialog(
    currentProfile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (displayName: String, heightCm: Int, weightKg: Int, goal: RunningGoal) -> Unit
) {
    var name by remember { mutableStateOf(currentProfile.displayName.ifBlank { "Dũng Trần" }) }
    var height by remember { mutableStateOf(currentProfile.heightCm.toString()) }
    var weight by remember { mutableStateOf(currentProfile.weightKg.toString()) }
    var selectedGoal by remember { mutableStateOf(currentProfile.goal) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PureWhite,
        shape = RoundedCornerShape(22.dp),
        title = {
            Text(
                "Chỉnh sửa hồ sơ",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Họ và tên") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        label = { Text("Chiều cao (cm)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Cân nặng (kg)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Mục tiêu chính:", fontSize = 12.sp, color = Color(0xFF6B7280))
                RunningGoal.entries.forEach { goal ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedGoal = goal }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedGoal == goal,
                            onClick = { selectedGoal = goal },
                            colors = RadioButtonDefaults.colors(selectedColor = SportOrange)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(goal.title, fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val h = height.toIntOrNull() ?: currentProfile.heightCm
                    val w = weight.toIntOrNull() ?: currentProfile.weightKg
                    onSave(name, h, w, selectedGoal)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SportOrange),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Lưu thay đổi", color = PureWhite, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = Color(0xFF6B7280))
            }
        }
    )
}

@Composable
private fun EditGoalDialog(
    currentGoalKm: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var goalText by remember { mutableStateOf("%.0f".format(currentGoalKm)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PureWhite,
        shape = RoundedCornerShape(22.dp),
        title = {
            Text(
                "Mục tiêu cự ly tuần",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Đặt số km bạn muốn hoàn thành mỗi tuần:", fontSize = 13.sp, color = Color(0xFF6B7280))
                OutlinedTextField(
                    value = goalText,
                    onValueChange = { goalText = it },
                    label = { Text("Số km / tuần") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val km = goalText.toDoubleOrNull() ?: currentGoalKm
                    onSave(km)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SportOrange),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cập nhật", color = PureWhite, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = Color(0xFF6B7280))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileSettingsSheet(
    onDismiss: () -> Unit,
    onLogout: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = PureWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 12.dp)
        ) {
            Text("Cài đặt tài khoản", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLogout() },
                color = Color(0xFFFFEBEE),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = ErrorRed)
                    Text("Đăng xuất tài khoản", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

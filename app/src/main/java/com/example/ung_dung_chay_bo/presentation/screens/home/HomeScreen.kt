package com.example.ung_dung_chay_bo.presentation.screens.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ung_dung_chay_bo.core.theme.LightBackground
import com.example.ung_dung_chay_bo.core.theme.LightBorder
import com.example.ung_dung_chay_bo.core.theme.PureWhite
import com.example.ung_dung_chay_bo.core.theme.SportOrange
import com.example.ung_dung_chay_bo.core.theme.SportOrangeSoft
import com.example.ung_dung_chay_bo.core.theme.SuccessGreen
import com.example.ung_dung_chay_bo.core.theme.TextMuted
import com.example.ung_dung_chay_bo.core.theme.TextPrimary
import com.example.ung_dung_chay_bo.core.theme.TextSecondary
import com.example.ung_dung_chay_bo.domain.model.AppNotification
import com.example.ung_dung_chay_bo.domain.model.CommunityChecklistState
import com.example.ung_dung_chay_bo.domain.model.FriendPost
import com.example.ung_dung_chay_bo.domain.model.SportType
import com.example.ung_dung_chay_bo.domain.model.SuggestedUser
import com.example.ung_dung_chay_bo.presentation.screens.chat.DirectMessageSheet
import com.example.ung_dung_chay_bo.presentation.screens.device.DeviceConnectionDialog
import com.example.ung_dung_chay_bo.presentation.screens.search.SearchFriendsAndClubsSheet
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onStartRunClick: () -> Unit,
    onNavigateToCreatePost: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        viewModel.onHealthConnectPermissionsResult(grantedPermissions)
    }

    if (uiState.isDeviceDialogVisible) {
        DeviceConnectionDialog(
            currentConnectedDeviceName = uiState.checklistState.connectedDeviceName,
            healthConnectStatus = uiState.healthConnectStatus,
            isHealthConnectLinked = uiState.isHealthConnectLinked,
            healthSummary = uiState.healthSummary,
            onRequestHealthConnect = {
                permissionLauncher.launch(viewModel.healthConnectPermissions)
            },
            onInstallHealthConnect = {
                context.startActivity(viewModel.getHealthConnectInstallIntent())
            },
            onSyncHealthConnect = viewModel::syncHealthConnectData,
            onDisconnectHealthConnect = viewModel::disconnectHealthConnect,
            onDismiss = viewModel::closeDeviceDialog,
            onDeviceConnected = viewModel::onDeviceConnected,
            onSkipDevice = viewModel::onSkipDevice
        )
    }

    if (uiState.activeDialog == HomeDialogType.SEARCH_FRIENDS) {
        SearchFriendsAndClubsSheet(
            suggestedUsers = uiState.suggestedUsers,
            onToggleFollow = viewModel::onToggleFollow,
            onDismiss = viewModel::closeDialog
        )
    }

    if (uiState.activeDialog == HomeDialogType.MESSAGES) {
        DirectMessageSheet(
            onDismiss = viewModel::closeDialog
        )
    }

    if (uiState.activeDialog == HomeDialogType.NOTIFICATIONS) {
        NotificationSheet(
            notifications = uiState.notifications,
            onDismiss = viewModel::closeDialog
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        HomeTopBar(
            onAddPostClick = onNavigateToCreatePost,
            onSearchClick = { viewModel.openDialog(HomeDialogType.SEARCH_FRIENDS) },
            onMessagesClick = { viewModel.openDialog(HomeDialogType.MESSAGES) },
            onNotificationsClick = { viewModel.openDialog(HomeDialogType.NOTIFICATIONS) }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            MultiChoiceDashboardCarousel(onStartRunClick = onStartRunClick)

            if (uiState.checklistState.shouldShowBanner) {
                Spacer(modifier = Modifier.height(20.dp))
                CommunityOnboardingSection(
                    state = uiState.checklistState,
                    onStartRunClick = onStartRunClick,
                    onFollowClick = { viewModel.openDialog(HomeDialogType.SEARCH_FRIENDS) },
                    onConnectDeviceClick = viewModel::openDeviceDialog,
                    onSkipDeviceClick = viewModel::onSkipDevice,
                    onDismissBanner = viewModel::dismissCommunityBanner
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SuggestedFollowSection(
                users = uiState.suggestedUsers,
                onToggleFollow = viewModel::onToggleFollow
            )

            Spacer(modifier = Modifier.height(24.dp))

            FriendFeedSection(
                posts = uiState.friendPosts,
                onToggleKudos = viewModel::onToggleKudos
            )
        }
    }
}

@Composable
private fun HomeTopBar(
    onAddPostClick: () -> Unit,
    onSearchClick: () -> Unit,
    onMessagesClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = PureWhite,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TopBarIconButton(
                    icon = Icons.Default.Add,
                    contentDescription = "Thêm bài viết mới",
                    onClick = onAddPostClick
                )
                TopBarIconButton(
                    icon = Icons.Default.Search,
                    contentDescription = "Tìm kiếm bạn bè, nhóm",
                    onClick = onSearchClick
                )
            }

            Text(
                text = "STRAVA",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
                color = TextPrimary
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TopBarIconButton(
                    icon = Icons.Default.ChatBubbleOutline,
                    contentDescription = "Tin nhắn",
                    onClick = onMessagesClick
                )

                Box {
                    TopBarIconButton(
                        icon = Icons.Default.NotificationsNone,
                        contentDescription = "Thông báo",
                        onClick = onNotificationsClick
                    )
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(SportOrange)
                            .align(Alignment.TopEnd)
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBarIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .border(1.dp, LightBorder, CircleShape)
            .background(PureWhite)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = TextPrimary,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun MultiChoiceDashboardCarousel(
    onStartRunClick: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp
        ) { page ->
            when (page) {
                0 -> CardStreakPage(onStartRunClick = onStartRunClick)
                1 -> CardGoalPage(onStartRunClick = onStartRunClick)
                2 -> CardWeeklySummaryPage(onStartRunClick = onStartRunClick)
                3 -> CardQuickWorkoutPage(onStartRunClick = onStartRunClick)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(4) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (isSelected) 7.dp else 5.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) SportOrange else Color(0xFFD1D5DB))
                        .clickable {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                )
            }
        }
    }
}

@Composable
private fun CardStreakPage(onStartRunClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Chuỗi liên tiếp của bạn", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = "Xem lịch", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SportOrange)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(54.dp).clip(CircleShape).background(Color(0xFFF3F4F6)), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = null, tint = SportOrange, modifier = Modifier.size(18.dp))
                        Text(text = "0", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = "Tuần", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = TextSecondary)
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(text = "Ghi lại hoạt động để bắt đầu chuỗi liên tiếp của bạn.", style = MaterialTheme.typography.bodyMedium, color = TextPrimary, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = onStartRunClick,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SportOrange, contentColor = PureWhite),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(text = "Ghi ngay", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CardGoalPage(onStartRunClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Mục tiêu gợi ý cho bạn", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = "Chỉnh sửa", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SportOrange)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(54.dp).clip(CircleShape).background(SportOrangeSoft), contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Default.TrackChanges, contentDescription = null, tint = SportOrange, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Mục tiêu: 10.0 km / tuần", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(progress = { 0.35f }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = SportOrange, trackColor = Color(0xFFE5E7EB))
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(text = "Đã hoàn thành 3.5 / 10.0 km (35%)", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = onStartRunClick,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SportOrange, contentColor = PureWhite),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(text = "Chạy tiếp", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CardWeeklySummaryPage(onStartRunClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Tổng kết tuần này", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = "Chi tiết", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SportOrange)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "3.5 km", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = TextPrimary)
                    Text(text = "QUÃNG ĐƯỜNG", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                }
                VerticalDivider(modifier = Modifier.height(30.dp), color = LightBorder)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "22 phút", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = TextPrimary)
                    Text(text = "THỜI GIAN", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                }
                VerticalDivider(modifier = Modifier.height(30.dp), color = LightBorder)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "1 buổi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = TextPrimary)
                    Text(text = "SỐ BUỔI", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onStartRunClick,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SportOrange, contentColor = PureWhite),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = "Thêm buổi", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CardQuickWorkoutPage(onStartRunClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Luyện tập tức thì", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.width(6.dp))
                Surface(shape = RoundedCornerShape(6.dp), color = SportOrangeSoft) {
                    Text(text = "GỢI Ý", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = SportOrange, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
            Text(text = "Đổi bài", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SportOrange)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(54.dp).clip(CircleShape).background(Color(0xFFFEF3C7)), contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(26.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Chạy phục hồi nhẹ nhàng 3.0 km", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "Pace 6:30 • Giúp thư giãn cơ bắp và đốt cháy 210 kcal", style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = onStartRunClick,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SportOrange, contentColor = PureWhite),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(text = "Tập ngay", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CommunityOnboardingSection(
    state: CommunityChecklistState,
    onStartRunClick: () -> Unit,
    onFollowClick: () -> Unit,
    onConnectDeviceClick: () -> Unit,
    onSkipDeviceClick: () -> Unit,
    onDismissBanner: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .width(56.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(SportOrange)
            )

            IconButton(
                onClick = onDismissBanner,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Ẩn", tint = TextMuted, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Chào mừng bạn đến với cộng đồng lớn nhất thế giới!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            lineHeight = 28.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dưới đây là cách bắt đầu sử dụng Strava:",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Text(
                text = "${state.completedCount}/4",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (state.completedCount > 0) SportOrange else TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { state.completedCount / 4f },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(2.5.dp)),
            color = SportOrange,
            trackColor = Color(0xFFE5E7EB)
        )

        Spacer(modifier = Modifier.height(14.dp))

        ChecklistCard(
            icon = Icons.AutoMirrored.Filled.DirectionsRun,
            title = "Tải lên hoạt động đầu tiên của bạn",
            subtitle = if (state.isFirstRunUploaded) "Đã hoàn thành! Hoạt động đầu tiên đã ghi lại." else "Bạn có thể ghi ngay trong ứng dụng.",
            isCompleted = state.isFirstRunUploaded,
            onClick = onStartRunClick
        )

        Spacer(modifier = Modifier.height(10.dp))

        ChecklistCard(
            icon = Icons.Default.Group,
            title = "Theo dõi 3 người (${state.followedUsersCount}/3)",
            subtitle = if (state.followedUsersCount >= 3) "Tuyệt vời! Bạn đã kết nối với các runner." else "Tìm bạn bè và những gương mặt nổi bật để theo dõi.",
            isCompleted = state.followedUsersCount >= 3,
            onClick = onFollowClick
        )

        Spacer(modifier = Modifier.height(10.dp))

        ChecklistCard(
            icon = Icons.Default.Person,
            title = "Thêm ảnh hồ sơ",
            subtitle = if (state.isProfilePhotoAdded) "Hồ sơ của bạn đã có ảnh nhận diện." else "Giúp mọi người nhận ra bạn trên đường chạy.",
            isCompleted = state.isProfilePhotoAdded,
            onClick = { }
        )

        Spacer(modifier = Modifier.height(10.dp))

        ChecklistCard(
            icon = Icons.Default.Watch,
            title = "Kết nối thiết bị",
            subtitle = if (state.isDeviceConnected) {
                if (!state.connectedDeviceName.isNullOrBlank()) "Đã kết nối: ${state.connectedDeviceName}" else "Đã hoàn tất bước thiết bị."
            } else {
                "Đồng bộ đồng hồ hoặc thiết bị theo dõi thể lực để dễ dàng tải lên Strava."
            },
            isCompleted = state.isDeviceConnected,
            showExtraButtons = !state.isDeviceConnected,
            onConnectClick = onConnectDeviceClick,
            onSkipClick = onSkipDeviceClick,
            onClick = onConnectDeviceClick
        )
    }
}

@Composable
private fun ChecklistCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isCompleted: Boolean = false,
    showExtraButtons: Boolean = false,
    onConnectClick: () -> Unit = {},
    onSkipClick: () -> Unit = {},
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isCompleted) Color(0xFFD1FAE5) else LightBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(if (isCompleted) SuccessGreen else PureWhite)
                        .border(1.5.dp, if (isCompleted) SuccessGreen else Color(0xFFD1D5DB), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = PureWhite, modifier = Modifier.size(15.dp))
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isCompleted) Color(0xFFECFDF5) else SportOrangeSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isCompleted) SuccessGreen else SportOrange,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCompleted) SuccessGreen else TextSecondary,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(14.dp)
                )
            }

            if (showExtraButtons) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onConnectClick,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SportOrange,
                            contentColor = PureWhite
                        ),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp)
                    ) {
                        Text(text = "Kết nối", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onSkipClick,
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(text = "Tôi không có thiết bị", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestedFollowSection(
    users: List<SuggestedUser>,
    onToggleFollow: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Nên theo dõi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Xem tất cả",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = SportOrange
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(users) { user ->
                Card(
                    modifier = Modifier.width(170.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(SportOrangeSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = SportOrange,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = user.role,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { onToggleFollow(user.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(34.dp),
                            shape = RoundedCornerShape(17.dp),
                            colors = if (user.isFollowing) {
                                ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF3F4F6),
                                    contentColor = TextPrimary
                                )
                            } else {
                                ButtonDefaults.buttonColors(
                                    containerColor = SportOrange,
                                    contentColor = PureWhite
                                )
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = if (user.isFollowing) "Đang theo dõi" else "Theo dõi",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendFeedSection(
    posts: List<FriendPost>,
    onToggleKudos: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Bảng tin bạn bè",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Cập nhật các hoạt động thể thao mới nhất từ cộng đồng",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
        )

        posts.forEach { post ->
            FriendPostCard(post = post, onToggleKudos = onToggleKudos)
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
private fun FriendPostCard(
    post: FriendPost,
    onToggleKudos: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SportOrangeSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (post.sportType) {
                            SportType.RUN -> Icons.AutoMirrored.Filled.DirectionsRun
                            SportType.WALK -> Icons.AutoMirrored.Filled.DirectionsWalk
                            SportType.RIDE -> Icons.AutoMirrored.Filled.DirectionsBike
                            SportType.TRAIL_RUN -> Icons.Default.Landscape
                        },
                        contentDescription = null,
                        tint = SportOrange,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.authorName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFF3F4F6)
                        ) {
                            Text(
                                text = post.sportType.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.postedTimeAgo,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "Công khai",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            if (post.caption.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = post.caption,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FeedMetric(title = "Khoảng cách", value = String.format("%.2f km", post.distanceKm))
                FeedMetric(title = "Tốc độ (Pace)", value = post.avgPace)
                FeedMetric(title = "Thời gian", value = post.duration)
                FeedMetric(title = "Calo", value = "${post.caloriesBurned} kcal")
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (!post.mediaUri.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = post.mediaUri,
                        contentDescription = "Ảnh hoạt động",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF3F4F6))
                        .border(1.dp, LightBorder, RoundedCornerShape(12.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        val w = size.width
                        val h = size.height

                        if (post.routePoints.isNotEmpty()) {
                            val path = Path().apply {
                                val first = post.routePoints.first()
                                moveTo(first.first * w, first.second * h)
                                for (i in 1 until post.routePoints.size) {
                                    val pt = post.routePoints[i]
                                    lineTo(pt.first * w, pt.second * h)
                                }
                            }

                            drawPath(
                                path = path,
                                color = SportOrange.copy(alpha = 0.2f),
                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )

                            drawPath(
                                path = path,
                                color = SportOrange,
                                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PureWhite.copy(alpha = 0.85f),
                        modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                    ) {
                        Text(
                            text = "GPS Lộ trình",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${post.kudosCount} Kudos",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${post.commentCount} bình luận",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = LightBorder)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onToggleKudos(post.id) }.padding(6.dp)
                ) {
                    Icon(
                        imageVector = if (post.isKudosGiven) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = "Thả Kudos",
                        tint = if (post.isKudosGiven) SportOrange else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Kudos",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (post.isKudosGiven) SportOrange else TextSecondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { }.padding(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Bình luận",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Bình luận", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = TextSecondary)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { }.padding(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Chia sẻ", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Chia sẻ", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun FeedMetric(title: String, value: String) {
    Column {
        Text(text = title, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = TextPrimary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationSheet(
    notifications: List<AppNotification>,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = PureWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Thông báo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Đóng", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.heightIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications) { notif ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SportOrangeSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = SportOrange)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${notif.senderName} ${notif.actionText}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                            Text(notif.timeAgo, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

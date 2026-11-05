package com.example.ung_dung_chay_bo.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ung_dung_chay_bo.core.theme.LightBorder
import com.example.ung_dung_chay_bo.core.theme.PureWhite
import com.example.ung_dung_chay_bo.core.theme.SportOrange
import com.example.ung_dung_chay_bo.core.theme.TextMuted
import com.example.ung_dung_chay_bo.core.theme.TextPrimary
import com.example.ung_dung_chay_bo.core.theme.TextSecondary
import com.example.ung_dung_chay_bo.presentation.screens.community.CommunityScreen
import com.example.ung_dung_chay_bo.presentation.screens.history.HistoryScreen
import com.example.ung_dung_chay_bo.presentation.screens.home.HomeScreen
import com.example.ung_dung_chay_bo.presentation.screens.home.HomeViewModel
import com.example.ung_dung_chay_bo.presentation.screens.maps.MapsScreen
import com.example.ung_dung_chay_bo.presentation.screens.profile.ProfileScreen

enum class StravaTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Trang chủ", Icons.Filled.Home, Icons.Outlined.Home),
    MAPS("Bản đồ", Icons.Filled.Map, Icons.Outlined.Map),
    RECORD("Ghi lại", Icons.Filled.FiberManualRecord, Icons.Filled.FiberManualRecord),
    GROUPS("Nhóm", Icons.Filled.Group, Icons.Outlined.Group),
    YOU("Bạn", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun MainScreen(
    onStartTrackingClick: () -> Unit,
    onNavigateToCreatePost: () -> Unit,
    onLogoutSuccess: () -> Unit,
    homeViewModel: HomeViewModel
) {
    var selectedTab by remember { mutableStateOf(StravaTab.HOME) }

    Scaffold(
        containerColor = PureWhite,
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = PureWhite,
                shadowElevation = 12.dp,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, LightBorder)
            ) {
                NavigationBar(
                    containerColor = PureWhite,
                    contentColor = TextPrimary,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .height(64.dp)
                ) {
                    NavigationBarItem(
                        selected = selectedTab == StravaTab.HOME,
                        onClick = { selectedTab = StravaTab.HOME },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == StravaTab.HOME) StravaTab.HOME.selectedIcon else StravaTab.HOME.unselectedIcon,
                                contentDescription = StravaTab.HOME.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = StravaTab.HOME.title,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == StravaTab.HOME) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = navigationBarColors()
                    )

                    NavigationBarItem(
                        selected = selectedTab == StravaTab.MAPS,
                        onClick = { selectedTab = StravaTab.MAPS },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == StravaTab.MAPS) StravaTab.MAPS.selectedIcon else StravaTab.MAPS.unselectedIcon,
                                contentDescription = StravaTab.MAPS.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = StravaTab.MAPS.title,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == StravaTab.MAPS) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = navigationBarColors()
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { onStartTrackingClick() }
                                .padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .border(2.5.dp, TextPrimary, CircleShape)
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .background(SportOrange)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Ghi lại",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    NavigationBarItem(
                        selected = selectedTab == StravaTab.GROUPS,
                        onClick = { selectedTab = StravaTab.GROUPS },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == StravaTab.GROUPS) StravaTab.GROUPS.selectedIcon else StravaTab.GROUPS.unselectedIcon,
                                contentDescription = StravaTab.GROUPS.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = StravaTab.GROUPS.title,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == StravaTab.GROUPS) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = navigationBarColors()
                    )

                    NavigationBarItem(
                        selected = selectedTab == StravaTab.YOU,
                        onClick = { selectedTab = StravaTab.YOU },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == StravaTab.YOU) StravaTab.YOU.selectedIcon else StravaTab.YOU.unselectedIcon,
                                contentDescription = StravaTab.YOU.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = StravaTab.YOU.title,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == StravaTab.YOU) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = navigationBarColors()
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                StravaTab.HOME -> HomeScreen(
                    onStartRunClick = onStartTrackingClick,
                    onNavigateToCreatePost = onNavigateToCreatePost,
                    viewModel = homeViewModel
                )
                StravaTab.MAPS -> MapsScreen(
                    onStartRouteClick = { _ ->
                        onStartTrackingClick()
                    }
                )
                StravaTab.RECORD -> onStartTrackingClick()
                StravaTab.GROUPS -> CommunityScreen(onNavigateToCreatePost = onNavigateToCreatePost)
                StravaTab.YOU -> ProfileScreen(onLogoutSuccess = onLogoutSuccess)
            }
        }
    }
}

@Composable
private fun navigationBarColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = SportOrange,
    selectedTextColor = SportOrange,
    indicatorColor = PureWhite,
    unselectedIconColor = Color(0xFF6B7280),
    unselectedTextColor = Color(0xFF6B7280)
)

package com.example.ung_dung_chay_bo.presentation.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
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
import com.example.ung_dung_chay_bo.core.theme.SportOrangeSoft
import com.example.ung_dung_chay_bo.core.theme.TextMuted
import com.example.ung_dung_chay_bo.core.theme.TextPrimary
import com.example.ung_dung_chay_bo.core.theme.TextSecondary
import com.example.ung_dung_chay_bo.domain.model.RunningClub
import com.example.ung_dung_chay_bo.domain.model.SportType
import com.example.ung_dung_chay_bo.domain.model.SuggestedUser

enum class SearchCategoryTab(val title: String) {
    FRIENDS("Bạn bè"),
    CLUBS("Câu lạc bộ")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFriendsAndClubsSheet(
    suggestedUsers: List<SuggestedUser>,
    onToggleFollow: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(SearchCategoryTab.FRIENDS) }
    var friendSearchQuery by remember { mutableStateOf("") }
    var clubSearchQuery by remember { mutableStateOf("") }
    var showContactSyncMessage by remember { mutableStateOf<String?>(null) }

    var clubs by remember {
        mutableStateOf(
            listOf(
                RunningClub("club_1", "Hà Nội Runners Club", "Hà Nội", 1850, SportType.RUN, "Cộng đồng chạy bộ lớn nhất thủ đô, sinh hoạt đều đặn thứ 7 & Chủ nhật.", isJoined = false),
                RunningClub("club_2", "Sài Gòn Trail Runners", "TP. Hồ Chí Minh", 1240, SportType.TRAIL_RUN, "Dành cho những người đam mê cung đường mòn và chinh phục Ultra Trail.", isJoined = true),
                RunningClub("club_3", "Đà Nẵng Coastal Pacer", "Đà Nẵng", 890, SportType.RUN, "Chạy dọc bãi biển Mỹ Khê đón bình minh mỗi sáng 5:00.", isJoined = false),
                RunningClub("club_4", "Vietnam Cycling & Tri Club", "Toàn quốc", 2100, SportType.RIDE, "CLB Đạp xe đường trường & 3 môn phối hợp Triathlon bơi-đạp-chạy.", isJoined = false)
            )
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = PureWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Khám phá cộng đồng",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Đóng", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            PrimaryTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = PureWhite,
                contentColor = SportOrange,
                divider = { HorizontalDivider(color = LightBorder) }
            ) {
                SearchCategoryTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                text = tab.title,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 15.sp
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            showContactSyncMessage?.let { msg ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SportOrangeSoft,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SportOrange.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = SportOrange,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { showContactSyncMessage = null },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = SportOrange, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (selectedTab == SearchCategoryTab.FRIENDS) {
                OutlinedTextField(
                    value = friendSearchQuery,
                    onValueChange = { friendSearchQuery = it },
                    placeholder = { Text("Tìm theo tên hoặc @username...", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                    trailingIcon = {
                        if (friendSearchQuery.isNotBlank()) {
                            IconButton(onClick = { friendSearchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Xóa", tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SportOrange,
                        unfocusedBorderColor = LightBorder,
                        focusedContainerColor = PureWhite,
                        unfocusedContainerColor = PureWhite
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (friendSearchQuery.isBlank()) {
                        item {
                            Text(
                                text = "Kết bạn thông qua liên hệ",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            ContactActionCard(
                                icon = Icons.Default.ContactPhone,
                                title = "Tìm qua Danh bạ điện thoại",
                                subtitle = "Xem bạn bè trong danh bạ đang chạy bộ trên app",
                                onClick = {
                                    showContactSyncMessage = "Đang đồng bộ danh bạ: Đã tìm thấy 4 người bạn của bạn đang hoạt động!"
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            ContactActionCard(
                                icon = Icons.Default.Share,
                                title = "Mời bạn bè qua liên kết",
                                subtitle = "Gửi link mời qua Zalo, Messenger để cùng tập luyện",
                                onClick = {
                                    showContactSyncMessage = "Đã sao chép liên kết mời tải app vào khay nhớ tạm!"
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Đề xuất cho bạn",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }

                    val filteredUsers = suggestedUsers.filter {
                        friendSearchQuery.isBlank() ||
                                it.name.contains(friendSearchQuery, ignoreCase = true) ||
                                it.username.contains(friendSearchQuery, ignoreCase = true)
                    }

                    if (filteredUsers.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Không tìm thấy người dùng phù hợp.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    } else {
                        items(filteredUsers) { user ->
                            FriendItemRow(
                                user = user,
                                onToggleFollow = { onToggleFollow(user.id) }
                            )
                        }
                    }
                }
            }

            if (selectedTab == SearchCategoryTab.CLUBS) {
                OutlinedTextField(
                    value = clubSearchQuery,
                    onValueChange = { clubSearchQuery = it },
                    placeholder = { Text("Tìm CLB theo tên hoặc khu vực (Hà Nội, Sài Gòn...)", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                    trailingIcon = {
                        if (clubSearchQuery.isNotBlank()) {
                            IconButton(onClick = { clubSearchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Xóa", tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SportOrange,
                        unfocusedBorderColor = LightBorder,
                        focusedContainerColor = PureWhite,
                        unfocusedContainerColor = PureWhite
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = SportOrangeSoft,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SportOrange.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showContactSyncMessage = "Tính năng 'Tạo CLB mới' đã sẵn sàng! Bạn có thể đặt tên và mời các thành viên tham gia."
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(SportOrange),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = PureWhite, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Tạo Câu Lạc Bộ Mới",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SportOrange
                                    )
                                    Text(
                                        text = "Tạo không gian sinh hoạt riêng cho nhóm chạy của bạn",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Câu lạc bộ nổi bật",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    val filteredClubs = clubs.filter {
                        clubSearchQuery.isBlank() ||
                                it.name.contains(clubSearchQuery, ignoreCase = true) ||
                                it.location.contains(clubSearchQuery, ignoreCase = true)
                    }

                    if (filteredClubs.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Không tìm thấy câu lạc bộ phù hợp.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    } else {
                        items(filteredClubs) { club ->
                            ClubItemRow(
                                club = club,
                                onToggleJoin = {
                                    clubs = clubs.map {
                                        if (it.id == club.id) it.copy(isJoined = !it.isJoined) else it
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ContactActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = SportOrange, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 11.sp)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun FriendItemRow(
    user: SuggestedUser,
    onToggleFollow: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = PureWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SportOrangeSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = SportOrange, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(
                    text = "${user.role} • ${user.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = onToggleFollow,
                shape = RoundedCornerShape(16.dp),
                colors = if (user.isFollowing) {
                    ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6), contentColor = TextPrimary)
                } else {
                    ButtonDefaults.buttonColors(containerColor = SportOrange, contentColor = PureWhite)
                },
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
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

@Composable
private fun ClubItemRow(
    club: RunningClub,
    onToggleJoin: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = PureWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (club.isJoined) Color(0xFFECFDF5) else SportOrangeSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = if (club.isJoined) Color(0xFF10B981) else SportOrange,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = club.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(
                    text = "${club.location} • ${club.memberCount} thành viên",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                if (club.description.isNotBlank()) {
                    Text(
                        text = club.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }

            Button(
                onClick = onToggleJoin,
                shape = RoundedCornerShape(16.dp),
                colors = if (club.isJoined) {
                    ButtonDefaults.buttonColors(containerColor = Color(0xFFECFDF5), contentColor = Color(0xFF065F46))
                } else {
                    ButtonDefaults.buttonColors(containerColor = SportOrange, contentColor = PureWhite)
                },
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (club.isJoined) "Đã tham gia" else "Tham gia",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

package com.example.ung_dung_chay_bo.presentation.screens.community

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ung_dung_chay_bo.core.theme.PureWhite
import com.example.ung_dung_chay_bo.core.theme.SportOrange
import com.example.ung_dung_chay_bo.domain.model.Challenge
import com.example.ung_dung_chay_bo.domain.model.Club
import com.example.ung_dung_chay_bo.domain.model.ClubPost
import com.example.ung_dung_chay_bo.domain.model.CommunityEvent
import com.example.ung_dung_chay_bo.domain.model.CommunityTab
import com.example.ung_dung_chay_bo.domain.model.SportType

@Composable
fun CommunityScreen(
    onNavigateToCreatePost: () -> Unit = {},
    viewModel: CommunityViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var showSearchField by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearUserMessage()
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearUserMessage()
        }
    }

    if (uiState.selectedClubForDetail != null) {
        BackHandler {
            viewModel.closeClubDetail()
        }
        ClubDetailView(
            club = uiState.selectedClubForDetail!!,
            posts = uiState.selectedClubPosts,
            clubChallenges = uiState.challenges.filter { it.clubId == uiState.selectedClubForDetail!!.id },
            onBack = { viewModel.closeClubDetail() },
            onToggleJoin = { viewModel.toggleJoinClub(it) },
            onOpenCreatePost = { viewModel.openCreatePostDialog() },
            onOpenCreateChallenge = { viewModel.openCreateChallengeDialog() },
            onToggleJoinChallenge = { viewModel.toggleJoinChallenge(it) }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
        ) {
            Surface(
                color = PureWhite,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Nhóm & Cộng đồng",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { showSearchField = !showSearchField }) {
                                Icon(
                                    Icons.Outlined.Search,
                                    contentDescription = "Tìm kiếm",
                                    tint = Color(0xFF1F2937),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            IconButton(onClick = onNavigateToCreatePost) {
                                Icon(
                                    Icons.Default.AddCircleOutline,
                                    contentDescription = "Tạo bài viết",
                                    tint = SportOrange,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = showSearchField) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            placeholder = { Text("Tìm thử thách, câu lạc bộ, sự kiện...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SportOrange,
                                unfocusedBorderColor = Color(0xFFE5E7EB)
                            )
                        )
                    }

                    TabRow(
                        selectedTabIndex = uiState.selectedTab.ordinal,
                        containerColor = PureWhite,
                        contentColor = SportOrange,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab.ordinal]),
                                color = SportOrange,
                                height = 3.dp
                            )
                        },
                        divider = { HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.8.dp) }
                    ) {
                        CommunityTab.entries.forEach { tab ->
                            val isSelected = uiState.selectedTab == tab
                            Tab(
                                selected = isSelected,
                                onClick = { viewModel.selectTab(tab) },
                                text = {
                                    Text(
                                        text = tab.title,
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) SportOrange else Color(0xFF6B7280)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            when (uiState.selectedTab) {
                CommunityTab.CHALLENGES_EVENTS -> ChallengesAndEventsTabContent(
                    challenges = uiState.challenges,
                    events = uiState.events,
                    onToggleJoinChallenge = { viewModel.toggleJoinChallenge(it) },
                    onToggleGoingEvent = { viewModel.toggleEventGoing(it) }
                )
                CommunityTab.CLUBS -> ClubsTabContent(
                    clubs = uiState.clubs,
                    onToggleJoinClub = { viewModel.toggleJoinClub(it) },
                    onOpenClubDetail = { viewModel.openClubDetail(it) },
                    onCreateClubClick = { viewModel.openCreateClubDialog() }
                )
            }
        }
    }

    if (uiState.showCreateClubDialog) {
        CreateClubDialog(
            isSubmitting = uiState.isSubmitting,
            onDismiss = { viewModel.closeCreateClubDialog() },
            onSubmit = { name, desc, category, location, sportType, avatarUrl, bannerUrl ->
                viewModel.createClub(name, desc, category, location, sportType, avatarUrl, bannerUrl)
            }
        )
    }

    if (uiState.showCreatePostDialog && uiState.selectedClubForDetail != null) {
        ShareWorkoutToClubDialog(
            clubName = uiState.selectedClubForDetail!!.name,
            isLeader = uiState.selectedClubForDetail!!.isLeader,
            isSubmitting = uiState.isSubmitting,
            onDismiss = { viewModel.closeCreatePostDialog() },
            onSubmit = { content, isAnnouncement, dist, dur, pace ->
                viewModel.sharePostToClub(
                    clubId = uiState.selectedClubForDetail!!.id,
                    content = content,
                    isAnnouncement = isAnnouncement,
                    distanceKm = dist,
                    durationFormatted = dur,
                    pace = pace
                )
            }
        )
    }

    if (uiState.showCreateChallengeDialog && uiState.selectedClubForDetail != null) {
        CreateClubChallengeDialog(
            clubName = uiState.selectedClubForDetail!!.name,
            isSubmitting = uiState.isSubmitting,
            onDismiss = { viewModel.closeCreateChallengeDialog() },
            onSubmit = { title, desc, targetKm, daysLeft, sportType, badgeName ->
                viewModel.createClubChallenge(
                    clubId = uiState.selectedClubForDetail!!.id,
                    clubName = uiState.selectedClubForDetail!!.name,
                    title = title,
                    description = desc,
                    targetKm = targetKm,
                    daysLeft = daysLeft,
                    sportType = sportType,
                    badgeName = badgeName
                )
            }
        )
    }
}

@Composable
private fun ChallengesAndEventsTabContent(
    challenges: List<Challenge>,
    events: List<CommunityEvent>,
    onToggleJoinChallenge: (String) -> Unit,
    onToggleGoingEvent: (String) -> Unit
) {
    var filterType by remember { mutableStateOf(0) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(18.dp)),
                color = PureWhite,
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF0EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.EmojiEvents,
                            contentDescription = null,
                            tint = SportOrange,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Thử thách & Sự kiện cộng đồng",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "Gồm các thử thách do ứng dụng phát động và các sự kiện chạy bộ sôi nổi do các câu lạc bộ tổ chức!",
                            fontSize = 12.5.sp,
                            color = Color(0xFF6B7280),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    0 to "Tất cả (${challenges.size + events.size})",
                    1 to "Thử thách (${challenges.size})",
                    2 to "Sự kiện (${events.size})"
                ).forEach { (idx, label) ->
                    val isSelected = filterType == idx
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { filterType = idx },
                        color = if (isSelected) SportOrange else PureWhite,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) SportOrange else Color(0xFFE5E7EB)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) PureWhite else Color(0xFF4B5563),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        if (filterType == 0 || filterType == 1) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "THỬ THÁCH NỔI BẬT (APP & CLB)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4B5563)
                    )
                }
            }

            items(challenges) { challenge ->
                ChallengeCard(
                    challenge = challenge,
                    onToggleJoin = { onToggleJoinChallenge(challenge.id) }
                )
            }
        }

        if (filterType == 0 || filterType == 2) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SỰ KIỆN DO CÁC CLB TỔ CHỨC",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4B5563)
                    )
                }
            }

            items(events) { event ->
                EventCard(
                    event = event,
                    onToggleGoing = { onToggleGoingEvent(event.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun ClubsTabContent(
    clubs: List<Club>,
    onToggleJoinClub: (String) -> Unit,
    onOpenClubDetail: (Club) -> Unit,
    onCreateClubClick: () -> Unit
) {
    var clubFilter by remember { mutableStateOf(0) }

    val filteredClubs = remember(clubs, clubFilter) {
        when (clubFilter) {
            1 -> clubs.filter { it.isLeader }
            2 -> clubs.filter { it.isJoined }
            else -> clubs
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(18.dp))
                    .clickable { onCreateClubClick() },
                color = PureWhite,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFFFCCBA))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF0EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.GroupAdd,
                            contentDescription = null,
                            tint = SportOrange,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tạo câu lạc bộ của bạn",
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Lập đội nhóm riêng, đăng bài viết và chia sẻ hành trình chạy cùng các thành viên!",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280),
                            lineHeight = 16.sp
                        )
                    }

                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = SportOrange,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    0 to "Tất cả (${clubs.size})",
                    1 to "CLB của tôi (${clubs.count { it.isLeader }})",
                    2 to "Đã tham gia (${clubs.count { it.isJoined }})"
                ).forEach { (idx, title) ->
                    val isSelected = clubFilter == idx
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { clubFilter = idx },
                        color = if (isSelected) SportOrange else PureWhite,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) SportOrange else Color(0xFFE5E7EB)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = title,
                            fontSize = 12.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) PureWhite else Color(0xFF4B5563),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        if (filteredClubs.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = PureWhite
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Outlined.Groups,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (clubFilter == 1) "Bạn chưa tham gia CLB nào" else "Chưa có câu lạc bộ nào",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937),
                            fontSize = 14.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Hãy bấm vào 'Tạo câu lạc bộ của bạn' ở trên để bắt đầu ngay!",
                            fontSize = 12.5.sp,
                            color = Color(0xFF6B7280),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        } else {
            items(filteredClubs) { club ->
                ClubCard(
                    club = club,
                    onToggleJoin = { onToggleJoinClub(club.id) },
                    onClick = { onOpenClubDetail(club) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun ClubCard(
    club: Club,
    onToggleJoin: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(5.dp, RoundedCornerShape(18.dp))
            .clickable { onClick() },
        color = PureWhite,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = club.avatarUrl.ifBlank { "https://images.unsplash.com/photo-1571008887538-b36bb32f4571?auto=format&fit=crop&w=300&q=80" },
                        contentDescription = club.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, Color(0xFFE5E7EB), CircleShape)
                    )

                    Column {
                        Text(
                            text = club.name,
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "${club.category} • ${club.location}",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }

                Button(
                    onClick = onToggleJoin,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (club.isJoined) Color(0xFFE5E7EB) else SportOrange,
                        contentColor = if (club.isJoined) Color(0xFF1F2937) else PureWhite
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (club.isJoined) "Đã tham gia" else "Tham gia",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9FAFB))
                    .padding(horizontal = 16.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%,d".format(club.memberCount),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Text("Thành viên", fontSize = 10.5.sp, color = Color(0xFF6B7280))
                }

                Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color(0xFFE5E7EB)))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%,.0f km".format(club.weeklyDistanceKm),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SportOrange
                    )
                    Text("Tổng km tuần này", fontSize = 10.5.sp, color = Color(0xFF6B7280))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClubDetailView(
    club: Club,
    posts: List<ClubPost>,
    clubChallenges: List<Challenge>,
    onBack: () -> Unit,
    onToggleJoin: (String) -> Unit,
    onOpenCreatePost: () -> Unit,
    onOpenCreateChallenge: () -> Unit,
    onToggleJoinChallenge: (String) -> Unit
) {
    var subTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        club.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PureWhite)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FA))
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 30.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                        AsyncImage(
                            model = club.bannerUrl.ifBlank { "https://images.unsplash.com/photo-1552674605-db6ffd4facb5?auto=format&fit=crop&w=800&q=80" },
                            contentDescription = club.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC000000))))
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomStart)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            AsyncImage(
                                model = club.avatarUrl.ifBlank { "https://images.unsplash.com/photo-1571008887538-b36bb32f4571?auto=format&fit=crop&w=300&q=80" },
                                contentDescription = club.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, PureWhite, CircleShape)
                            )

                            Column {
                                Text(
                                    text = club.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                                Text(
                                    text = "${club.category} • ${club.location}",
                                    fontSize = 12.sp,
                                    color = Color(0xFFE5E7EB)
                                )
                            }
                        }
                    }
                }

                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = PureWhite,
                        shadowElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                                    Text(
                                        text = "${club.category} • ${club.location}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1F2937),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "%,d thành viên".format(club.memberCount),
                                        fontSize = 12.sp,
                                        color = Color(0xFF6B7280)
                                    )
                                }

                                Button(
                                    onClick = { onToggleJoin(club.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (club.isJoined) Color(0xFFE5E7EB) else SportOrange,
                                        contentColor = if (club.isJoined) Color(0xFF1F2937) else PureWhite
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(if (club.isJoined) "Đã tham gia" else "Tham gia nhóm", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                }
                            }

                            if (club.isJoined || club.isLeader) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = onOpenCreatePost,
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SportOrange),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Đăng bài chia sẻ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = onOpenCreateChallenge,
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.2.dp, SportOrange),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SportOrange),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Icon(Icons.Outlined.EmojiEvents, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Tạo thử thách", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                if (club.isJoined || club.isLeader) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clickable { onOpenCreatePost() },
                            shape = RoundedCornerShape(14.dp),
                            color = PureWhite,
                            shadowElevation = 2.dp,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCCBA))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFF0EB)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.DirectionsRun,
                                        contentDescription = null,
                                        tint = SportOrange,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Text(
                                    text = "Chia sẻ hành trình chạy bộ hoặc bài viết với CLB...",
                                    fontSize = 13.sp,
                                    color = Color(0xFF6B7280),
                                    modifier = Modifier.weight(1f)
                                )

                                Surface(
                                    color = SportOrange,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Đăng bài",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PureWhite,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    TabRow(
                        selectedTabIndex = subTab,
                        containerColor = PureWhite,
                        contentColor = SportOrange,
                        modifier = Modifier.padding(horizontal = 16.dp).clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = subTab == 0,
                            onClick = { subTab = 0 },
                            text = { Text("Bảng tin (${posts.size})", fontWeight = if (subTab == 0) FontWeight.Bold else FontWeight.Normal) }
                        )
                        Tab(
                            selected = subTab == 1,
                            onClick = { subTab = 1 },
                            text = { Text("Thử thách (${clubChallenges.size})", fontWeight = if (subTab == 1) FontWeight.Bold else FontWeight.Normal) }
                        )
                        Tab(
                            selected = subTab == 2,
                            onClick = { subTab = 2 },
                            text = { Text("Giới thiệu", fontWeight = if (subTab == 2) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                when (subTab) {
                    0 -> {
                        if (posts.isEmpty()) {
                            item {
                                Surface(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    color = PureWhite
                                ) {
                                    Column(
                                        modifier = Modifier.padding(26.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Outlined.Campaign, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Chưa có bài viết nào trong nhóm", fontWeight = FontWeight.Bold, color = Color(0xFF374151))
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Hãy là người đầu tiên chia sẻ hành trình chạy bộ của bạn nhé!", fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                            }
                        } else {
                            items(posts) { post ->
                                ClubPostItemCard(post = post)
                            }
                        }
                    }

                    1 -> {
                        if (clubChallenges.isEmpty()) {
                            item {
                                Surface(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    color = PureWhite
                                ) {
                                    Column(
                                        modifier = Modifier.padding(26.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Outlined.EmojiEvents, contentDescription = null, tint = SportOrange, modifier = Modifier.size(40.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("CLB chưa có thử thách nào", fontWeight = FontWeight.Bold, color = Color(0xFF374151))
                                        if (club.isLeader) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = onOpenCreateChallenge,
                                                colors = ButtonDefaults.buttonColors(containerColor = SportOrange),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("Tạo thử thách ngay", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            items(clubChallenges) { challenge ->
                                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    ChallengeCard(
                                        challenge = challenge,
                                        onToggleJoin = { onToggleJoinChallenge(challenge.id) }
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                shape = RoundedCornerShape(14.dp),
                                color = PureWhite,
                                shadowElevation = 2.dp
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Giới thiệu câu lạc bộ", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = club.description.ifBlank { "Câu lạc bộ chưa có mô tả chi tiết." },
                                        fontSize = 13.sp,
                                        color = Color(0xFF4B5563),
                                        lineHeight = 18.sp
                                    )

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF3F4F6))

                                    Text("Thông tin chung", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("• Địa điểm hoạt động: ${club.location}", fontSize = 12.5.sp, color = Color(0xFF4B5563))
                                    Text("• Bộ môn: ${club.category}", fontSize = 12.5.sp, color = Color(0xFF4B5563))
                                    Text("• Thành viên: ${club.memberCount} người", fontSize = 12.5.sp, color = Color(0xFF4B5563))
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
private fun ClubPostItemCard(post: ClubPost) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        color = PureWhite,
        shadowElevation = 2.dp,
        border = if (post.isAnnouncement) androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFFFD4C2)) else null
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFE5E7EB)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(post.authorName, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Color(0xFF111827))
                    Text(post.timeAgo, fontSize = 11.sp, color = Color(0xFF9CA3AF))
                }

                if (post.isAnnouncement) {
                    Surface(color = Color(0xFFFFECE5), shape = RoundedCornerShape(6.dp)) {
                        Text(
                            "THÔNG BÁO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SportOrange,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(post.content, fontSize = 13.5.sp, color = Color(0xFF1F2937), lineHeight = 19.sp)

            if (post.distanceKm != null && post.distanceKm > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFF9FAFB),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("QUÃNG ĐƯỜNG", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
                            Text("%.2f km".format(post.distanceKm), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SportOrange, fontFamily = FontFamily.Monospace)
                        }

                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFFE5E7EB)))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("THỜI GIAN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
                            Text(post.durationFormatted ?: "--:--", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827), fontFamily = FontFamily.Monospace)
                        }

                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFFE5E7EB)))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("PACE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
                            Text(post.pace ?: "--:--", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827), fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.ThumbUpOffAlt, contentDescription = null, tint = SportOrange, modifier = Modifier.size(16.dp))
                    Text("${maxOf(1, post.kudosCount)} Kudos", fontSize = 11.5.sp, color = Color(0xFF4B5563))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(15.dp))
                    Text("${post.commentsCount} bình luận", fontSize = 11.5.sp, color = Color(0xFF4B5563))
                }
            }
        }
    }
}

@Composable
private fun ShareWorkoutToClubDialog(
    clubName: String,
    isLeader: Boolean,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (content: String, isAnnouncement: Boolean, dist: Double?, dur: String?, pace: String?) -> Unit
) {
    var content by remember { mutableStateOf("") }
    var attachStats by remember { mutableStateOf(false) }
    var distanceText by remember { mutableStateOf("5.0") }
    var durationText by remember { mutableStateOf("00:27:30") }
    var paceText by remember { mutableStateOf("05:30 /km") }
    var isOfficialAnnouncement by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            color = PureWhite
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.DirectionsRun, contentDescription = null, tint = SportOrange, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Chia sẻ với CLB", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng")
                    }
                }

                Text("Đăng bài viết hoặc chia sẻ buổi chạy vào $clubName:", fontSize = 12.5.sp, color = Color(0xFF6B7280))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("Hôm nay buổi chạy của bạn thế nào? Chia sẻ cảm xúc, hành trình chạy cùng mọi người...") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { attachStats = !attachStats },
                    color = if (attachStats) Color(0xFFFFECE5) else Color(0xFFF9FAFB),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (attachStats) SportOrange else Color(0xFFE5E7EB)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.Speed, contentDescription = null, tint = if (attachStats) SportOrange else Color(0xFF6B7280), modifier = Modifier.size(18.dp))
                            Text("Đính kèm số liệu buổi chạy (km, thời gian)", fontSize = 12.5.sp, fontWeight = FontWeight.Medium, color = if (attachStats) SportOrange else Color(0xFF1F2937))
                        }
                        Checkbox(checked = attachStats, onCheckedChange = { attachStats = it })
                    }
                }

                if (attachStats) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = distanceText,
                            onValueChange = { distanceText = it },
                            label = { Text("Cự ly (km)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = durationText,
                            onValueChange = { durationText = it },
                            label = { Text("Thời gian") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    OutlinedTextField(
                        value = paceText,
                        onValueChange = { paceText = it },
                        label = { Text("Pace trung bình") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isOfficialAnnouncement, onCheckedChange = { isOfficialAnnouncement = it })
                    Text("Đăng dưới dạng Thông báo câu lạc bộ", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                }

                Button(
                    onClick = {
                        val dist = if (attachStats) distanceText.toDoubleOrNull() else null
                        val dur = if (attachStats) durationText else null
                        val pace = if (attachStats) paceText else null
                        onSubmit(content, isOfficialAnnouncement, dist, dur, pace)
                    },
                    enabled = content.isNotBlank() && !isSubmitting,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SportOrange)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = PureWhite, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("CHIA SẺ VÀO NHÓM", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateClubDialog(
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (name: String, desc: String, category: String, location: String, sportType: SportType, avatarUrl: String, bannerUrl: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("Hà Nội, Việt Nam") }
    var selectedSport by remember { mutableStateOf(SportType.RUN) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            color = PureWhite
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tạo câu lạc bộ mới",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng")
                    }
                }

                Text(
                    text = "Tạo không gian kết nối, chia sẻ lịch chạy và thử thách cùng các thành viên.",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên câu lạc bộ *") },
                    placeholder = { Text("Ví dụ: HRC Hồ Tây, Running Team...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Địa điểm sinh hoạt") },
                    placeholder = { Text("Ví dụ: Hồ Tây, Hà Nội") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Bộ môn hoạt động", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        SportType.RUN to "Chạy bộ",
                        SportType.RIDE to "Đạp xe",
                        SportType.TRAIL_RUN to "Trail",
                        SportType.WALK to "Đi bộ"
                    ).forEach { (sport, label) ->
                        val isSelected = selectedSport == sport
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedSport = sport },
                            color = if (isSelected) Color(0xFFFFECE5) else Color(0xFFF9FAFB),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) SportOrange else Color(0xFFE5E7EB)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) SportOrange else Color(0xFF4B5563),
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Mô tả câu lạc bộ") },
                    placeholder = { Text("Mục tiêu, thời gian sinh hoạt chung...") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = {
                        val categoryName = when (selectedSport) {
                            SportType.RUN -> "Chạy bộ"
                            SportType.RIDE -> "Đạp xe"
                            SportType.TRAIL_RUN -> "Chạy Trail"
                            SportType.WALK -> "Đi bộ"
                        }
                        onSubmit(name, description, categoryName, location, selectedSport, "", "")
                    },
                    enabled = name.isNotBlank() && !isSubmitting,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SportOrange)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = PureWhite, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("TẠO CÂU LẠC BỘ", fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateClubChallengeDialog(
    clubName: String,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (title: String, desc: String, targetKm: Double, daysLeft: Int, sportType: SportType, badgeName: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var targetKmText by remember { mutableStateOf("30") }
    var daysLeftText by remember { mutableStateOf("14") }
    var badgeName by remember { mutableStateOf("") }
    var selectedSport by remember { mutableStateOf(SportType.RUN) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.92f).wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            color = PureWhite
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.EmojiEvents, contentDescription = null, tint = SportOrange, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tạo thử thách mới", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng")
                    }
                }

                Text("Thử thách sẽ xuất hiện trong CLB $clubName và phần Thử thách & Sự kiện.", fontSize = 12.sp, color = Color(0xFF6B7280))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tên thử thách *") },
                    placeholder = { Text("Ví dụ: Chinh phục 50km Tháng 11...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = targetKmText,
                        onValueChange = { targetKmText = it },
                        label = { Text("Mục tiêu (km) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = daysLeftText,
                        onValueChange = { daysLeftText = it },
                        label = { Text("Thời hạn (ngày) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = badgeName,
                    onValueChange = { badgeName = it },
                    label = { Text("Tên huy hiệu hoàn thành") },
                    placeholder = { Text("Ví dụ: Huy hiệu Finisher 50K") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Mô tả & thể lệ thử thách") },
                    placeholder = { Text("Hoàn thành cự ly quy định để nhận huy hiệu...") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = {
                        val targetKm = targetKmText.toDoubleOrNull() ?: 30.0
                        val days = daysLeftText.toIntOrNull() ?: 14
                        onSubmit(title, desc, targetKm, days, selectedSport, badgeName)
                    },
                    enabled = title.isNotBlank() && !isSubmitting,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SportOrange)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = PureWhite, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("TẠO VÀ PHÁT ĐỘNG THỬ THÁCH", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengeCard(
    challenge: Challenge,
    onToggleJoin: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp)),
        color = PureWhite,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color(0xFFE5E7EB))
            ) {
                AsyncImage(
                    model = challenge.bannerUrl.ifBlank { "https://images.unsplash.com/photo-1544197150-b99a580bb7a8?auto=format&fit=crop&w=800&q=80" },
                    contentDescription = challenge.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xAA000000))
                            )
                        )
                )

                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart),
                    color = Color(0xCC000000),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Còn ${challenge.daysLeft} ngày",
                        color = PureWhite,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (!challenge.clubName.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.TopEnd),
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "CLB: ${challenge.clubName}",
                            color = Color(0xFFB45309),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = challenge.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = challenge.description,
                    fontSize = 13.sp,
                    color = Color(0xFF4B5563),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (challenge.isJoined) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tiến độ của bạn",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = challenge.progressText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = SportOrange
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { challenge.progressPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = SportOrange,
                        trackColor = Color(0xFFFFECE5)
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.PeopleOutline,
                            contentDescription = null,
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "%,d người tham gia".format(challenge.participantsCount),
                            fontSize = 12.5.sp,
                            color = Color(0xFF6B7280),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = onToggleJoin,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (challenge.isJoined) Color(0xFFE5E7EB) else SportOrange,
                            contentColor = if (challenge.isJoined) Color(0xFF1F2937) else PureWhite
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        if (challenge.isJoined) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Đang tham gia", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text("Tham gia", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventCard(
    event: CommunityEvent,
    onToggleGoing: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp)),
        color = PureWhite,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                AsyncImage(
                    model = event.imageUrl.ifBlank { "https://images.unsplash.com/photo-1530549387789-4c1017266635?auto=format&fit=crop&w=800&q=80" },
                    contentDescription = event.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(listOf(Color.Transparent, Color(0x99000000)))
                        )
                )

                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart),
                    color = Color(0xDD000000),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = event.dateTimeFormatted,
                        color = PureWhite,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopEnd),
                    color = SportOrange,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${event.distanceKm} km",
                        color = PureWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = event.title,
                    fontSize = 16.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = SportOrange, modifier = Modifier.size(16.dp))
                    Text(
                        text = event.location,
                        fontSize = 12.5.sp,
                        color = Color(0xFF4B5563),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Groups, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(16.dp))
                    Text(
                        text = "Tổ chức bởi CLB: ${event.clubName}",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "%,d người sẽ tham gia".format(event.participantsCount),
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280),
                        fontWeight = FontWeight.Medium
                    )

                    Button(
                        onClick = onToggleGoing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (event.isGoing) Color(0xFFE5E7EB) else SportOrange,
                            contentColor = if (event.isGoing) Color(0xFF1F2937) else PureWhite
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        if (event.isGoing) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sẽ tham gia", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text("Đăng ký tham gia", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

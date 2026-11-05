package com.example.ung_dung_chay_bo.presentation.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ung_dung_chay_bo.core.theme.LightBorder
import com.example.ung_dung_chay_bo.core.theme.PureWhite
import com.example.ung_dung_chay_bo.core.theme.SportOrange
import com.example.ung_dung_chay_bo.core.theme.SportOrangeSoft
import com.example.ung_dung_chay_bo.core.theme.SuccessGreen
import com.example.ung_dung_chay_bo.core.theme.TextMuted
import com.example.ung_dung_chay_bo.core.theme.TextPrimary
import com.example.ung_dung_chay_bo.core.theme.TextSecondary
import com.example.ung_dung_chay_bo.domain.model.ChatConversation
import com.example.ung_dung_chay_bo.domain.model.ChatDetailMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectMessageSheet(
    onDismiss: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissError()
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.closeConversation()
            onDismiss()
        },
        containerColor = PureWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = PureWhite
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(padding)
                    .padding(bottom = 12.dp)
            ) {
                if (uiState.activeConversationId == null) {
                    ConversationListHeader(onDismiss = onDismiss)
                    Spacer(modifier = Modifier.height(10.dp))

                    when {
                        uiState.isLoadingConversations -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = SportOrange)
                            }
                        }
                        uiState.conversations.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("💬", fontSize = 40.sp)
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Chưa có tin nhắn nào",
                                        color = TextSecondary,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        "Theo dõi bạn bè để bắt đầu nhắn tin!",
                                        color = TextMuted,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 440.dp)
                                    .padding(horizontal = 20.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(uiState.conversations) { conv ->
                                    ConversationItemRow(
                                        conversation = conv,
                                        onClick = { viewModel.openConversation(conv.id) }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    val conv = uiState.activeConversation
                    if (conv != null) {
                        ActiveChatView(
                            conversation = conv,
                            messages = uiState.messages,
                            inputText = uiState.inputText,
                            isSending = uiState.isSending,
                            isLoadingMessages = uiState.isLoadingMessages,
                            onBackToList = { viewModel.closeConversation() },
                            onInputChange = viewModel::onInputChange,
                            onSend = viewModel::sendMessage
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationListHeader(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Tin nhắn",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Đóng", tint = TextSecondary)
        }
    }
}

@Composable
private fun ConversationItemRow(
    conversation: ChatConversation,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF8F9FA))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SportOrangeSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = SportOrange,
                    modifier = Modifier.size(26.dp)
                )
            }
            if (conversation.isOnline) {
                Box(
                    modifier = Modifier
                        .size(13.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(PureWhite),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = conversation.friendName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = TextPrimary
            )
            if (conversation.lastMessage.isNotBlank()) {
                Text(
                    text = conversation.lastMessage,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    maxLines = 1
                )
            }
        }

        if (conversation.timeAgo.isNotBlank()) {
            Text(
                text = conversation.timeAgo,
                fontSize = 11.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun ActiveChatView(
    conversation: ChatConversation,
    messages: List<ChatDetailMessage>,
    inputText: String,
    isSending: Boolean,
    isLoadingMessages: Boolean,
    onBackToList: () -> Unit,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackToList) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = TextPrimary)
            }
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SportOrangeSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = SportOrange, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(conversation.friendName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                Text(
                    if (conversation.isOnline) "Đang hoạt động" else "Ngoại tuyến",
                    fontSize = 12.sp,
                    color = if (conversation.isOnline) SuccessGreen else TextMuted
                )
            }
        }

        HorizontalDivider(color = LightBorder, thickness = 0.8.dp)

        if (isLoadingMessages) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .heightIn(min = 200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SportOrange)
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .heightIn(min = 200.dp, max = 340.dp)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubble(message = msg)
                }
            }
        }

        HorizontalDivider(color = LightBorder, thickness = 0.5.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                placeholder = { Text("Nhắn tin…", color = TextMuted, fontSize = 14.sp) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp),
                shape = RoundedCornerShape(22.dp),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SportOrange,
                    unfocusedBorderColor = LightBorder,
                    cursorColor = SportOrange,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSend,
                enabled = inputText.isNotBlank() && !isSending,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (inputText.isNotBlank()) SportOrange else Color(0xFFE0E0E0))
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = PureWhite,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Gửi",
                        tint = if (inputText.isNotBlank()) PureWhite else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatDetailMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (message.isFromMe) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 260.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (message.isFromMe) 18.dp else 4.dp,
                            bottomEnd = if (message.isFromMe) 4.dp else 18.dp
                        )
                    )
                    .background(
                        if (message.isFromMe) SportOrange else Color(0xFFF0F0F0)
                    )
                    .border(
                        width = if (message.isFromMe) 0.dp else 0.5.dp,
                        color = if (message.isFromMe) Color.Transparent else LightBorder,
                        shape = RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (message.isFromMe) 18.dp else 4.dp,
                            bottomEnd = if (message.isFromMe) 4.dp else 18.dp
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            ) {
                Text(
                    text = message.text,
                    color = if (message.isFromMe) PureWhite else TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = message.time,
                fontSize = 10.sp,
                color = TextMuted,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

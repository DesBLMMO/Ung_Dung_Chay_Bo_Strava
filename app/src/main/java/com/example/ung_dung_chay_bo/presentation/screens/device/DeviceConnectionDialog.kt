package com.example.ung_dung_chay_bo.presentation.screens.device

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ung_dung_chay_bo.core.theme.LightBorder
import com.example.ung_dung_chay_bo.core.theme.PureWhite
import com.example.ung_dung_chay_bo.core.theme.SportOrange
import com.example.ung_dung_chay_bo.core.theme.SportOrangeSoft
import com.example.ung_dung_chay_bo.core.theme.SuccessGreen
import com.example.ung_dung_chay_bo.core.theme.TextMuted
import com.example.ung_dung_chay_bo.core.theme.TextPrimary
import com.example.ung_dung_chay_bo.core.theme.TextSecondary
import com.example.ung_dung_chay_bo.domain.model.DeviceBrand
import com.example.ung_dung_chay_bo.domain.model.FitnessDevice
import com.example.ung_dung_chay_bo.domain.model.HealthConnectStatus
import com.example.ung_dung_chay_bo.domain.model.HealthSummary
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceConnectionDialog(
    currentConnectedDeviceName: String?,
    healthConnectStatus: HealthConnectStatus = HealthConnectStatus.AVAILABLE,
    isHealthConnectLinked: Boolean = false,
    healthSummary: HealthSummary? = null,
    onRequestHealthConnect: () -> Unit = {},
    onInstallHealthConnect: () -> Unit = {},
    onSyncHealthConnect: () -> Unit = {},
    onDisconnectHealthConnect: () -> Unit = {},
    onDismiss: () -> Unit,
    onDeviceConnected: (String) -> Unit,
    onSkipDevice: () -> Unit
) {
    var isSyncing by remember { mutableStateOf(false) }
    var selectedDevice by remember { mutableStateOf(currentConnectedDeviceName) }

    val availableDevices = remember {
        listOf(
            FitnessDevice("1", "Garmin Forerunner / Fenix", DeviceBrand.GARMIN, batteryPercent = 92),
            FitnessDevice("2", "Coros Pace 3 / Apex", DeviceBrand.COROS, batteryPercent = 78),
            FitnessDevice("3", "Samsung Galaxy Watch / Wear OS", DeviceBrand.APPLE_WATCH, batteryPercent = 65),
            FitnessDevice("4", "Polar Grit X / Dây đo H10", DeviceBrand.POLAR, batteryPercent = 95),
            FitnessDevice("5", "Suunto Race / Vertical", DeviceBrand.SUUNTO, batteryPercent = 88)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "SyncTransition")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SyncAngle"
    )

    LaunchedEffect(isSyncing) {
        if (isSyncing) {
            onSyncHealthConnect()
            delay(1200)
            isSyncing = false
        }
    }

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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Kết nối thiết bị & cảm biến",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Đồng bộ qua Android Health Connect & Đồng hồ GPS",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Đóng", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Featured Card: Google Health Connect
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isHealthConnectLinked) Color(0xFFF0FDF4) else Color(0xFFFFF7ED),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isHealthConnectLinked) SuccessGreen.copy(alpha = 0.6f) else SportOrange.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
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
                                .background(if (isHealthConnectLinked) SuccessGreen else SportOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isHealthConnectLinked) Icons.Default.HealthAndSafety else Icons.Default.Favorite,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Google Health Connect",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isHealthConnectLinked) SuccessGreen else Color(0xFFEA580C)
                                ) {
                                    Text(
                                        text = if (isHealthConnectLinked) "ĐÃ KẾT NỐI" else "KHUYÊN DÙNG",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PureWhite,
                                        fontSize = 9.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Cầu nối nhận nhịp tim, bước chân & GPS từ Garmin, Samsung, Coros, Zepp...",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isHealthConnectLinked) {
                        // Health Data Summary Cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HealthStatChip(
                                title = "Bước chân",
                                value = "${healthSummary?.stepsToday ?: 0}",
                                unit = "bước",
                                modifier = Modifier.weight(1f)
                            )
                            HealthStatChip(
                                title = "Nhịp tim",
                                value = "${healthSummary?.averageHeartRate ?: "--"}",
                                unit = "bpm",
                                modifier = Modifier.weight(1f)
                            )
                            HealthStatChip(
                                title = "Tiêu hao",
                                value = "${healthSummary?.caloriesToday?.toInt() ?: 0}",
                                unit = "kcal",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { isSyncing = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .rotate(if (isSyncing) angle else 0f),
                                    tint = SuccessGreen
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Đồng bộ lại", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            TextButton(
                                onClick = {
                                    onDisconnectHealthConnect()
                                    selectedDevice = null
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Ngắt kết nối", color = Color(0xFFDC2626), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // Connection trigger based on availability status
                        when (healthConnectStatus) {
                            HealthConnectStatus.UPDATE_OR_INSTALL_REQUIRED -> {
                                Button(
                                    onClick = onInstallHealthConnect,
                                    modifier = Modifier.fillMaxWidth().height(42.dp),
                                    shape = RoundedCornerShape(21.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Cài đặt Health Connect từ Google Play", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                            HealthConnectStatus.AVAILABLE -> {
                                Button(
                                    onClick = {
                                        onRequestHealthConnect()
                                        onDeviceConnected("Google Health Connect")
                                    },
                                    modifier = Modifier.fillMaxWidth().height(42.dp),
                                    shape = RoundedCornerShape(21.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SportOrange)
                                ) {
                                    Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Cấp quyền & Kết nối Health Connect", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                            HealthConnectStatus.NOT_SUPPORTED -> {
                                Text(
                                    text = "Thiết bị này không hỗ trợ Health Connect (Yêu cầu Android 8.0+)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Brand Device selection guide
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dòng đồng hồ bạn đang đeo",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = "Bật chia sẻ dữ liệu trong app của hãng sang Health Connect để tự đồng bộ",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.heightIn(max = 210.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableDevices) { device ->
                    val isSelected = selectedDevice == device.name || (isHealthConnectLinked && selectedDevice == "Google Health Connect")

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) SportOrangeSoft else PureWhite,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) SportOrange else LightBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedDevice = device.name
                                onDeviceConnected(device.name)
                                if (!isHealthConnectLinked && healthConnectStatus == HealthConnectStatus.AVAILABLE) {
                                    onRequestHealthConnect()
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) SportOrange else Color(0xFFF3F4F6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Watch,
                                    contentDescription = null,
                                    tint = if (isSelected) PureWhite else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = device.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "${device.brand.brandName} • Hỗ trợ đồng bộ tự động",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = SportOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        selectedDevice = device.name
                                        onDeviceConnected(device.name)
                                        if (!isHealthConnectLinked && healthConnectStatus == HealthConnectStatus.AVAILABLE) {
                                            onRequestHealthConnect()
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SportOrange),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Text("Chọn", style = MaterialTheme.typography.labelSmall, color = SportOrange, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = {
                    onSkipDevice()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(22.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
            ) {
                Text(
                    text = "Để sau (Chỉ dùng GPS điện thoại)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun HealthStatChip(
    title: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = PureWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1FAE5)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
            Text(text = unit, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
        }
    }
}

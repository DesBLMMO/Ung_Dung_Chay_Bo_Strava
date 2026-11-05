package com.example.ung_dung_chay_bo.presentation.screens.device

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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceConnectionDialog(
    currentConnectedDeviceName: String?,
    onDismiss: () -> Unit,
    onDeviceConnected: (String) -> Unit,
    onSkipDevice: () -> Unit
) {
    var isScanning by remember { mutableStateOf(false) }
    var selectedDevice by remember { mutableStateOf(currentConnectedDeviceName) }

    val availableDevices = remember {
        listOf(
            FitnessDevice("1", "Garmin Forerunner 265", DeviceBrand.GARMIN, batteryPercent = 92),
            FitnessDevice("2", "Coros Pace 3 (White)", DeviceBrand.COROS, batteryPercent = 78),
            FitnessDevice("3", "Apple Watch Series 9", DeviceBrand.APPLE_WATCH, batteryPercent = 65),
            FitnessDevice("4", "Polar H10 Heart Rate Sensor", DeviceBrand.POLAR, batteryPercent = 95),
            FitnessDevice("5", "Suunto Race Titanium", DeviceBrand.SUUNTO, batteryPercent = 88)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "RadarTransition")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarAngle"
    )

    LaunchedEffect(isScanning) {
        if (isScanning) {
            delay(2500)
            isScanning = false
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
                        text = "Đồng bộ tự động với đồng hồ GPS & nhịp tim",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Đóng", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!selectedDevice.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFECFDF5),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(SuccessGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = PureWhite, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Đang kết nối: $selectedDevice",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                            Text(
                                text = "Dữ liệu buổi chạy sẽ tự động đồng bộ lên ứng dụng",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF047857)
                            )
                        }
                        TextButton(
                            onClick = {
                                selectedDevice = null
                                onDeviceConnected("")
                            }
                        ) {
                            Text("Ngắt", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = { isScanning = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isScanning) Color(0xFF4B5563) else SportOrange,
                    contentColor = PureWhite
                )
            ) {
                if (isScanning) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(angle)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đang quét thiết bị xung quanh...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(imageVector = Icons.Default.BluetoothSearching, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Quét thiết bị Bluetooth / Đồng hồ", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Các dòng đồng hồ & cảm biến được hỗ trợ",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.heightIn(max = 260.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableDevices) { device ->
                    val isConnected = selectedDevice == device.name

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isConnected) SportOrangeSoft else PureWhite,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isConnected) 1.5.dp else 1.dp,
                            color = if (isConnected) SportOrange else LightBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedDevice = device.name
                                onDeviceConnected(device.name)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isConnected) SportOrange else Color(0xFFF3F4F6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Watch,
                                    contentDescription = null,
                                    tint = if (isConnected) PureWhite else TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = device.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${device.brand.brandName} • Pin ${device.batteryPercent}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            if (isConnected) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SportOrange
                                ) {
                                    Text(
                                        text = "Đã chọn",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = PureWhite,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        selectedDevice = device.name
                                        onDeviceConnected(device.name)
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SportOrange),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                                ) {
                                    Text("Kết nối", style = MaterialTheme.typography.labelSmall, color = SportOrange, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    onSkipDevice()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(23.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
            ) {
                Text(
                    text = "Tôi không có thiết bị (Bỏ qua bước này)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

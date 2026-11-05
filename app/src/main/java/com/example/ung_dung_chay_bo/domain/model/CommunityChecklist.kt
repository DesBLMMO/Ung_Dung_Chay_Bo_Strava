package com.example.ung_dung_chay_bo.domain.model

data class CommunityChecklistState(
    val isFirstRunUploaded: Boolean = false,
    val followedUsersCount: Int = 0,
    val isProfilePhotoAdded: Boolean = false,
    val isDeviceConnected: Boolean = false,
    val connectedDeviceName: String? = null,
    val isDismissed: Boolean = false
) {
    val completedCount: Int
        get() {
            var count = 0
            if (isFirstRunUploaded) count++
            if (followedUsersCount >= 3) count++
            if (isProfilePhotoAdded) count++
            if (isDeviceConnected) count++
            return count
        }

    val isAllCompleted: Boolean
        get() = completedCount >= 4

    val shouldShowBanner: Boolean
        get() = !isDismissed && !isAllCompleted
}

enum class DeviceBrand(val brandName: String, val category: String) {
    GARMIN("Garmin", "Đồng hồ GPS chuyên nghiệp"),
    COROS("Coros", "Đồng hồ chạy bộ & Trail"),
    APPLE_WATCH("Apple Watch / Wear OS", "Đồng hồ thông minh"),
    POLAR("Polar", "Đồng hồ & Cảm biến nhịp tim"),
    SUUNTO("Suunto", "Đồng hồ thể thao dã ngoại"),
    BLUETOOTH_SENSOR("Cảm biến Bluetooth BLE", "Dây đo nhịp tim ngực / Vòng đo bước")
}

data class FitnessDevice(
    val id: String,
    val name: String,
    val brand: DeviceBrand,
    val isConnected: Boolean = false,
    val batteryPercent: Int = 85,
    val lastSyncTime: String = "Vừa xong"
)

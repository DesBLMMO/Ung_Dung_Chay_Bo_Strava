package com.example.ung_dung_chay_bo.domain.model

data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val goal: RunningGoal = RunningGoal.STAMINA,
    val gender: Gender = Gender.MALE,
    val heightCm: Int = 170,
    val weightKg: Int = 65,
    val level: ExperienceLevel = ExperienceLevel.BEGINNER,
    val weeklyFrequency: Int = 3,
    val isOnboardingCompleted: Boolean = false
)

enum class RunningGoal(val title: String, val subtitle: String) {
    WEIGHT_LOSS("Giảm cân & Đốt mỡ", "Đốt cháy calo với các buổi chạy bền nhịp độ vừa phải"),
    STAMINA("Nâng cao thể lực & Sức bền", "Tăng cường sức khỏe tim mạch và độ bền dẻo dai"),
    RACE_TRAINING("Chinh phục cự ly (5K/10K/21K)", "Luyện tập có lộ trình để nâng cao tốc độ và cự ly"),
    DAILY_HABIT("Duy trì lối sống lành mạnh", "Chạy bộ thư giãn mỗi ngày để giữ vóc dáng và tinh thần sảng khoái")
}

enum class Gender(val title: String) {
    MALE("Nam"),
    FEMALE("Nữ"),
    OTHER("Khác")
}

enum class ExperienceLevel(val title: String, val desc: String) {
    BEGINNER("Mới bắt đầu", "Chưa từng hoặc ít khi chạy bộ liên tục"),
    INTERMEDIATE("Đã có kinh nghiệm", "Thường xuyên chạy 3 - 5 km mỗi tuần"),
    ADVANCED("Chuyên nghiệp / Bán chuyên", "Đã tham gia các giải chạy cự ly 10K trở lên")
}

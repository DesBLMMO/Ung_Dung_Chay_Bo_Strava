package com.example.ung_dung_chay_bo.presentation.navigation

sealed class Routes(val route: String) {
    data object Login : Routes("login")
    data object Register : Routes("register")
    data object Onboarding : Routes("onboarding")

    data object Main : Routes("main")

    data object Tracking : Routes("tracking")
    data object SaveActivity : Routes("save_activity")
    data object History : Routes("history")

    data object ActivityDetail : Routes("activity_detail/{activityId}") {
        fun createRoute(activityId: Long): String = "activity_detail/$activityId"
    }

    data object CreatePost : Routes("create_post")
}

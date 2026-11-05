package com.example.ung_dung_chay_bo.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import com.example.ung_dung_chay_bo.presentation.auth.AuthViewModel
import com.example.ung_dung_chay_bo.presentation.auth.LoginScreen
import com.example.ung_dung_chay_bo.presentation.auth.RegisterScreen
import com.example.ung_dung_chay_bo.presentation.main.MainScreen
import com.example.ung_dung_chay_bo.presentation.onboarding.OnboardingScreen
import com.example.ung_dung_chay_bo.presentation.screens.activity_detail.ActivityDetailScreen
import com.example.ung_dung_chay_bo.presentation.screens.history.HistoryScreen
import com.example.ung_dung_chay_bo.presentation.screens.home.HomeViewModel
import com.example.ung_dung_chay_bo.presentation.screens.post.CreatePostScreen
import com.example.ung_dung_chay_bo.presentation.screens.save_activity.SaveActivityScreen
import com.example.ung_dung_chay_bo.presentation.screens.tracking.TrackingScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()

    val startDestination = if (authViewModel.isUserLoggedIn) {
        Routes.Main.route
    } else {
        Routes.Login.route
    }

    val homeViewModel: HomeViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Routes.Register.route)
                },
                onLoginSuccess = {
                    coroutineScope.launch {
                        val isDone = authViewModel.checkIsOnboardingCompleted()
                        val targetRoute = if (isDone) Routes.Main.route else Routes.Onboarding.route
                        navController.navigate(targetRoute) {
                            popUpTo(Routes.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Routes.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Routes.Onboarding.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Onboarding.route) {
            OnboardingScreen(
                onOnboardingComplete = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Main.route) {
            MainScreen(
                onStartTrackingClick = {
                    navController.navigate(Routes.Tracking.route)
                },
                onNavigateToCreatePost = {
                    navController.navigate(Routes.CreatePost.route)
                },
                onLogoutSuccess = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onActivityClick = { activityId ->
                    navController.navigate(Routes.ActivityDetail.createRoute(activityId))
                },
                onNavigateToHistory = {
                    navController.navigate(Routes.History.route)
                },
                homeViewModel = homeViewModel
            )
        }

        composable(Routes.History.route) {
            HistoryScreen(
                onStartRunClick = {
                    navController.navigate(Routes.Tracking.route)
                },
                onActivityClick = { activityId ->
                    navController.navigate(Routes.ActivityDetail.createRoute(activityId))
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.ActivityDetail.route,
            arguments = listOf(
                navArgument("activityId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) {
            ActivityDetailScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.CreatePost.route) {
            CreatePostScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onPostCreated = { newPost ->
                    homeViewModel.addNewPost(newPost)
                }
            )
        }

        composable(Routes.Tracking.route) {
            TrackingScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onFinishSession = { sessionId ->
                    navController.navigate(Routes.SaveActivity.route)
                }
            )
        }

        composable(Routes.SaveActivity.route) {
            SaveActivityScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSaveSuccess = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.Main.route) { inclusive = false }
                    }
                }
            )
        }
    }
}

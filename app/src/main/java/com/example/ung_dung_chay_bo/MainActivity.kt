package com.example.ung_dung_chay_bo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.ung_dung_chay_bo.core.theme.UngDungChayBoTheme
import com.example.ung_dung_chay_bo.presentation.navigation.NavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UngDungChayBoTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}

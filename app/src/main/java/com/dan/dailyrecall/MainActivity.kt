package com.dan.dailyrecall

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dan.dailyrecall.ui.AppNav
import com.dan.dailyrecall.ui.DailyRecallTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DailyRecallTheme {
                AppNav()
            }
        }
    }
}

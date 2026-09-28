package com.dan.dailyrecall.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.time.LocalDate

@Composable
fun AppNav() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "timeline") {
        composable("timeline") {
            TimelineScreen(
                onAdd = { nav.navigate("editor/${LocalDate.now().minusDays(1)}") },
                onOpen = { date -> nav.navigate("editor/$date") },
            )
        }
        composable("editor/{date}") { backStackEntry ->
            val date = LocalDate.parse(backStackEntry.arguments!!.getString("date")!!)
            EditorScreen(initialDate = date, onClose = { nav.popBackStack() })
        }
    }
}

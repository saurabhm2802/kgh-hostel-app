package com.kgh.hostel.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

data class BottomDest(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

val bottomDestinations = listOf(
    BottomDest(Screen.Dashboard.route, "Home", Icons.Default.Home),
    BottomDest(Screen.Students.route, "Students", Icons.Default.Groups),
    BottomDest(Screen.Attendance.route, "Attendance", Icons.Default.FactCheck),
    BottomDest(Screen.Rooms.route, "Rooms", Icons.Default.MeetingRoom),
    BottomDest(Screen.More.route, "More", Icons.Default.MoreHoriz)
)

@Composable
fun KGHBottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    NavigationBar {
        bottomDestinations.forEach { dest ->
            val selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(dest.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(dest.icon, contentDescription = dest.label) },
                label = { Text(dest.label) }
            )
        }
    }
}

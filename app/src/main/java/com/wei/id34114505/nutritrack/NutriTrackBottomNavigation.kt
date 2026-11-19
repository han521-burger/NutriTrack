// file: com/wei/id34114505/nutritrack/NutriTrackBottomNavigation.kt

package com.wei.id34114505.nutritrack

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.wei.id34114505.nutritrack.data.SessionManager
import com.wei.id34114505.nutritrack.ui.theme.NutriTrackTheme

class NutriTrackBottomNavigation : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is logged in
        if (!SessionManager.isLoggedIn()) {
            // Navigate to login screen
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContent {
            NutriTrackTheme {
                BottomNavigationApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationApp() {
    val navController = rememberNavController()

    // Define navigation items
    val items = listOf(
        BottomNavItem("home", "Home", Icons.Filled.Home),
        BottomNavItem("insights", "Insights", Icons.Filled.Info),
        BottomNavItem("nutricoach", "NutriCoach", Icons.Filled.Person),
        BottomNavItem("settings", "Settings", Icons.Filled.Settings)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    patientName = SessionManager.getPatientName() ?: "User",
                    foodScore = 0, // Replace with actual score from ViewModel
                    onEditClick = { /* Handle click */ },
                    onInsightsClick = { navController.navigate("insights") },
                    onNutriCoachClick = { navController.navigate("nutricoach") }, // Add this
                    onSettingsClick = { navController.navigate("settings") }      // Add this
                )
            }
            composable("insights") {
                // Your InsightsScreen composable
                Text("Insights Screen")
            }
            composable("nutricoach") {
                // Your NutriCoachScreen composable
                Text("NutriCoach Screen")
            }
            composable("settings") {
                // Your SettingsScreen composable
                Text("Settings Screen")
            }
        }
    }
}

// Helper class for bottom navigation items
data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)
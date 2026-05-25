package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ServerListScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.*
import com.example.viewmodel.NovaVpnViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                NovaVpnAppRoot()
            }
        }
    }
}

@Composable
fun NovaVpnAppRoot() {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("nova_vpn_prefs", Context.MODE_PRIVATE) }
    
    // Check if onboarding completed
    var showOnboarding by remember {
        mutableStateOf(!sharedPrefs.getBoolean("onboarding_completed", false))
    }

    if (showOnboarding) {
        OnboardingScreen(onFinished = { showOnboarding = false })
    } else {
        NovaVpnNavigationShell()
    }
}

@Composable
fun NovaVpnNavigationShell() {
    val navController = rememberNavController()
    val viewModel: NovaVpnViewModel = viewModel()

    val navigationItems = listOf(
        NavigationItem("home", "Shell Core", Icons.Default.Home),
        NavigationItem("servers", "Registry", Icons.Default.Dns),
        NavigationItem("settings", "Properties", Icons.Default.Settings)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            NavigationBar(
                modifier = Modifier.testTag("app_navigation_bar"),
                containerColor = CyberDarkGray,
                contentColor = Color.White
            ) {
                navigationItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                        label = { Text(text = item.label, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            selectedTextColor = NeonCyan,
                            indicatorColor = CyberNavy,
                            unselectedIconColor = CyberGray,
                            unselectedTextColor = CyberGray
                        ),
                        modifier = Modifier.testTag("nav_tab_${item.route}")
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
                HomeScreen(viewModel = viewModel, onNavigateToServers = {
                    navController.navigate("servers")
                })
            }
            composable("servers") {
                ServerListScreen(viewModel = viewModel, onBack = {
                    navController.navigateUp()
                })
            }
            composable("settings") {
                SettingsScreen(viewModel = viewModel)
            }
        }
    }
}

data class NavigationItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

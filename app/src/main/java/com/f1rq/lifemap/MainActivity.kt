package com.f1rq.lifemap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import com.f1rq.lifemap.screens.ListView
import com.f1rq.lifemap.screens.MapView
import com.f1rq.lifemap.screens.SettingsScreen
import com.f1rq.lifemap.screens.NotificationsScreen
import com.f1rq.lifemap.ui.theme.LifeMapTheme
import com.f1rq.lifemap.ui.theme.ActiveNavColor
import com.f1rq.lifemap.ui.theme.InactiveNavColor
import com.f1rq.lifemap.components.TopBar
import com.f1rq.lifemap.components.NavBar
import com.f1rq.lifemap.screens.settingsScreens.SettingsNotificationsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            LifeMapTheme {
                val navController = rememberNavController()

                val view = LocalView.current
                val darkTheme = !isSystemInDarkTheme()
                SideEffect {
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                    val insetsController = WindowCompat.getInsetsController(window, view)
                    insetsController.isAppearanceLightStatusBars = true
                    insetsController.isAppearanceLightNavigationBars = true
                }

                Scaffold(
                    topBar = {
                          TopBar(
                              onSettingsButtonClick = { navController.navigate("settings")},
                              onNotificationsButtonClick = { navController.navigate("notifications")}
                          )
                    },
                    bottomBar = {
                        val navBackStackEntry = navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry.value?.destination?.route

                        NavBar(
                            onMapViewClicked = { navController.navigate("mapview") },
                            onListViewClicked = { navController.navigate("listview") },
                            mapViewBackgroundColor = if (currentRoute == "mapview") ActiveNavColor else InactiveNavColor,
                            listViewBackgroundColor = if (currentRoute == "listview") ActiveNavColor else InactiveNavColor
                        )
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "mapview",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("mapview") { MapView(Modifier) }
                        composable("listview") { ListView(Modifier) }
                        composable("settings") { SettingsScreen(navController = navController, Modifier)}
                        composable("notifications") { NotificationsScreen(navController = navController, Modifier)}
                        composable("settings_notifications") { SettingsNotificationsScreen(navController = navController, Modifier) }
                    }
                }
            }
        }
    }
}

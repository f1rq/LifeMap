package com.f1rq.lifemap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.f1rq.lifemap.components.NavBar
import com.f1rq.lifemap.components.SuccessMessage
import com.f1rq.lifemap.components.TopBar
import com.f1rq.lifemap.screens.ListView
import com.f1rq.lifemap.screens.MapView
import com.f1rq.lifemap.screens.NotificationsScreen
import com.f1rq.lifemap.screens.SettingsScreen
import com.f1rq.lifemap.screens.settingsScreens.SettingsNotificationsScreen
import com.f1rq.lifemap.ui.theme.ActiveNavColor
import com.f1rq.lifemap.ui.theme.InactiveNavColor
import com.f1rq.lifemap.ui.theme.LifeMapTheme
import com.f1rq.lifemap.ui.viewmodel.EventViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            LifeMapTheme {
                val navController = rememberNavController()
                val viewModel: EventViewModel = koinViewModel()

                val view = LocalView.current
                val darkTheme = !isSystemInDarkTheme()
                SideEffect {
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                    val insetsController = WindowCompat.getInsetsController(window, view)
                    insetsController.isAppearanceLightStatusBars = true
                    insetsController.isAppearanceLightNavigationBars = true
                }

                val navBackStackEntry = navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry.value?.destination?.route

                val routesWithBars = listOf("mapview", "listview")
                val showBars = currentRoute in routesWithBars

                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        topBar = {
                            if (showBars) {
                                TopBar(
                                    onSettingsButtonClick = { navController.navigate("settings")},
                                    onNotificationsButtonClick = { navController.navigate("notifications")}
                                )
                            }
                        },
                        bottomBar = {
                            if (showBars) {
                                NavBar(
                                    onMapViewClicked = { navController.navigate("mapview") },
                                    onListViewClicked = { navController.navigate("listview") },
                                    mapViewBackgroundColor = if (currentRoute == "mapview") ActiveNavColor else InactiveNavColor,
                                    listViewBackgroundColor = if (currentRoute == "listview") ActiveNavColor else InactiveNavColor
                                )
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = "mapview",
                            modifier = Modifier.padding(innerPadding),
                            enterTransition = {
                                val from = this.initialState.destination.route
                                val to = this.targetState.destination.route
                                if (from == "mapview" && to == "listview") {
                                    androidx.compose.animation.slideInHorizontally(
                                        initialOffsetX = { it },
                                        animationSpec = tween(300)
                                    )
                                } else if (from == "listview" && to == "mapview") {
                                    androidx.compose.animation.slideInHorizontally(
                                        initialOffsetX = { -it },
                                        animationSpec = tween(300)
                                    )
                                } else {
                                    scaleIn(
                                        initialScale = 0.9f,
                                        animationSpec = tween(200)
                                    ) + fadeIn(
                                        animationSpec = tween(200)
                                    )
                                }
                            },
                            exitTransition = {
                                val from = this.initialState.destination.route
                                val to = this.targetState.destination.route
                                if (from == "mapview" && to == "listview") {
                                    androidx.compose.animation.slideOutHorizontally(
                                        targetOffsetX = { -it },
                                        animationSpec = tween(200)
                                    )
                                } else if (from == "listview" && to == "mapview") {
                                    androidx.compose.animation.slideOutHorizontally(
                                        targetOffsetX = { it },
                                        animationSpec = tween(200)
                                    )
                                } else {
                                    scaleOut(
                                        targetScale = 0.9f,
                                        animationSpec = tween(200)
                                    ) + fadeOut(
                                        animationSpec = tween(200)
                                    )
                                }
                            }
                        ) {
                            composable("mapview") {
                                MapView(
                                    navController = navController,
                                    Modifier,
                                    viewModel = viewModel
                                )
                            }
                            composable("listview") {
                                ListView(
                                    Modifier,
                                    viewModel = viewModel
                                )
                            }
                            composable("settings") {
                                SettingsScreen(
                                    navController = navController,
                                    Modifier
                                )
                            }
                            composable("notifications") {
                                NotificationsScreen(
                                    navController = navController,
                                    Modifier
                                )
                            }
                            composable("settings_notifications") {
                                SettingsNotificationsScreen(
                                    navController = navController,
                                    Modifier
                                )
                            }
                        }
                    }
                    SuccessMessage(
                        viewModel = viewModel,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                            .zIndex(999f)
                    )
                }
            }
        }
    }
}
package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.db.AppDatabase
import com.example.data.db.FocusPetRepository
import com.example.data.db.VitalityManager
import com.example.ui.navigation.Screen
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FocusPetViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize dependencies manually for simplicity & reliability (Constructor Injection)
        val database = AppDatabase.getInstance(applicationContext)
        val repository = FocusPetRepository(database.focusPetDao())
        val vitalityManager = VitalityManager(repository)

        // Inject dependencies into our ViewModel Factory
        val viewModel: FocusPetViewModel by viewModels {
            FocusPetViewModel.Factory(repository, vitalityManager, applicationContext)
        }

        // Request POST_NOTIFICATIONS runtime permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        setContent {
            MyApplicationTheme {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScaffold(viewModel: FocusPetViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Define standard bottom bar screens
    val tabScreens = listOf(Screen.Home.route, Screen.Stats.route, Screen.Settings.route)
    val showBottomBar = currentRoute in tabScreens

    val petState by viewModel.petState.collectAsStateWithLifecycle()
    val allSessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val secondsRemaining by viewModel.secondsRemaining.collectAsStateWithLifecycle()
    val totalSeconds by viewModel.totalSeconds.collectAsStateWithLifecycle()
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF0A0A0F),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color(0xFF0F0F15),
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    // 1. Home tab
                    NavigationBarItem(
                        selected = currentRoute == Screen.Home.route,
                        onClick = {
                            if (currentRoute != Screen.Home.route) {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentRoute == Screen.Home.route) Icons.Default.Home else Icons.Outlined.Home,
                                contentDescription = stringResource(R.string.nav_home)
                            )
                        },
                        label = { Text(text = stringResource(R.string.nav_home)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            unselectedIconColor = Color(0x66FFFFFF),
                            unselectedTextColor = Color(0x66FFFFFF),
                            indicatorColor = Color(0xFF6C63FF)
                        ),
                        modifier = Modifier.testTag("nav_home_tab")
                    )

                    // 2. Stats tab
                    NavigationBarItem(
                        selected = currentRoute == Screen.Stats.route,
                        onClick = {
                            if (currentRoute != Screen.Stats.route) {
                                navController.navigate(Screen.Stats.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentRoute == Screen.Stats.route) Icons.Default.BarChart else Icons.Outlined.BarChart,
                                contentDescription = stringResource(R.string.nav_stats)
                            )
                        },
                        label = { Text(text = stringResource(R.string.nav_stats)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            unselectedIconColor = Color(0x66FFFFFF),
                            unselectedTextColor = Color(0x66FFFFFF),
                            indicatorColor = Color(0xFF6C63FF)
                        ),
                        modifier = Modifier.testTag("nav_stats_tab")
                    )

                    // 3. Settings tab
                    NavigationBarItem(
                        selected = currentRoute == Screen.Settings.route,
                        onClick = {
                            if (currentRoute != Screen.Settings.route) {
                                navController.navigate(Screen.Settings.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentRoute == Screen.Settings.route) Icons.Default.Settings else Icons.Outlined.Settings,
                                contentDescription = stringResource(R.string.nav_settings)
                            )
                        },
                        label = { Text(text = stringResource(R.string.nav_settings)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            unselectedIconColor = Color(0x66FFFFFF),
                            unselectedTextColor = Color(0x66FFFFFF),
                            indicatorColor = Color(0xFF6C63FF)
                        ),
                        modifier = Modifier.testTag("nav_settings_tab")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Home Screen
            composable(Screen.Home.route) {
                HomeScreen(
                    petState = petState,
                    onBeginSession = { minutes ->
                        viewModel.startSession(minutes)
                        navController.navigate(Screen.ActiveFocus.route)
                    },
                    onUpdateFocusDuration = { mins ->
                        viewModel.updateFocusDuration(mins)
                    }
                )
            }

            // Immersive Active Focus Screen
            composable(Screen.ActiveFocus.route) {
                ActiveFocusScreen(
                    secondsRemaining = secondsRemaining,
                    totalSeconds = totalSeconds,
                    timerState = timerState,
                    onPause = { viewModel.pauseSession() },
                    onResume = { viewModel.resumeSession() },
                    onAbandon = { viewModel.abandonSession() },
                    onNavigateComplete = {
                        navController.navigate(Screen.SessionComplete.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    },
                    onNavigateFailed = {
                        navController.navigate(Screen.SessionFailed.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    }
                )
            }

            // Session Completed Screen
            composable(Screen.SessionComplete.route) {
                SessionCompleteScreen(
                    petState = petState,
                    onNextSession = {
                        viewModel.resetTimerServiceState()
                        val currentDur = petState?.focusDurationMinutes ?: 25
                        viewModel.startSession(currentDur)
                        navController.navigate(Screen.ActiveFocus.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    },
                    onGoHome = {
                        viewModel.resetTimerServiceState()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            // Session Failed Screen
            composable(Screen.SessionFailed.route) {
                SessionFailedScreen(
                    petState = petState,
                    onTryAgain = {
                        viewModel.resetTimerServiceState()
                        val currentDur = petState?.focusDurationMinutes ?: 25
                        viewModel.startSession(currentDur)
                        navController.navigate(Screen.ActiveFocus.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    },
                    onGoHome = {
                        viewModel.resetTimerServiceState()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            // Stats Screen
            composable(Screen.Stats.route) {
                StatsScreen(
                    petState = petState,
                    allSessions = allSessions
                )
            }

            // Settings Screen
            composable(Screen.Settings.route) {
                SettingsScreen(
                    petState = petState,
                    onUpdateFocusDuration = { viewModel.updateFocusDuration(it) },
                    onUpdateBreakDuration = { viewModel.updateBreakDuration(it) },
                    onUpdateNotifications = { viewModel.updateNotificationsEnabled(it) },
                    onUpdateDetectUnlock = { viewModel.updateDetectUnlock(it) },
                    onResetCreature = { viewModel.resetCreature() }
                )
            }
        }
    }
}

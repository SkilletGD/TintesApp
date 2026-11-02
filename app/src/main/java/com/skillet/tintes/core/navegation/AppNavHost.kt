package com.skillet.tintes.core.navegation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.*
import com.skillet.tintes.core.theme.ThemePreferences
import com.skillet.tintes.feature.resurtido.presentation.ResurtidoScreen
import com.skillet.tintes.feature.scanner.presentation.ScannerScreen
import com.skillet.tintes.ui.theme.BrandOrange
import org.koin.compose.koinInject

@Composable
fun AppNavHost(
    sucursalId: String,
    themePreferences: ThemePreferences = koinInject()
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier.border(width = 1.dp, color = MaterialTheme.colorScheme.outline)
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    NavigationBarItem(
                        selected = currentRoute == Screen.Scanner.route,
                        onClick = {
                            if (currentRoute != Screen.Scanner.route) {
                                navController.navigate(Screen.Scanner.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        label = {
                            Text(
                                text = "Ventas (Scan)",
                                fontWeight = if (currentRoute == Screen.Scanner.route) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        },
                        icon = {
                            Text(
                                text = "📷",
                                fontSize = 18.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandOrange,
                            selectedTextColor = BrandOrange,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Resurtido.route,
                        onClick = {
                            if (currentRoute != Screen.Resurtido.route) {
                                navController.navigate(Screen.Resurtido.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        label = {
                            Text(
                                text = "Recepción",
                                fontWeight = if (currentRoute == Screen.Resurtido.route) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        },
                        icon = {
                            Text(
                                text = "📦",
                                fontSize = 18.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandOrange,
                            selectedTextColor = BrandOrange,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Scanner.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Scanner.route) {
                ScannerScreen(
                    sucursalId = sucursalId,
                    themePreferences = themePreferences
                )
            }
            composable(Screen.Resurtido.route) {
                ResurtidoScreen(
                    sucursalId = sucursalId,
                    themePreferences = themePreferences
                )
            }
        }
    }
}
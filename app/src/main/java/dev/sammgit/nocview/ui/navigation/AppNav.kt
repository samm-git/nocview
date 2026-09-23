package dev.sammgit.nocview.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.sammgit.nocview.ui.MainViewModel
import dev.sammgit.nocview.ui.hostdetail.HostDetailScreen
import dev.sammgit.nocview.ui.hosts.HostsScreen
import dev.sammgit.nocview.ui.problems.ProblemsScreen
import dev.sammgit.nocview.ui.servicedetail.ServiceDetailScreen
import dev.sammgit.nocview.ui.services.ServicesScreen
import dev.sammgit.nocview.ui.settings.SettingsScreen
import kotlinx.coroutines.delay

private object Routes {
    const val HOSTS = "hosts"
    const val SERVICES = "services"
    const val PROBLEMS = "problems"
    const val SETTINGS = "settings"
    const val HOST_DETAIL = "host/{hostName}"
    const val SERVICE_DETAIL = "service/{hostName}/{serviceDescription}"
    const val ARG_HOST_NAME = "hostName"
    const val ARG_SERVICE_DESCRIPTION = "serviceDescription"

    fun hostDetail(name: String): String = "host/${Uri.encode(name)}"

    fun serviceDetail(hostName: String, description: String): String =
        "service/${Uri.encode(hostName)}/${Uri.encode(description)}"
}

private data class BottomItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val bottomItems = listOf(
    BottomItem(Routes.HOSTS, "Hosts", Icons.AutoMirrored.Filled.List),
    BottomItem(Routes.SERVICES, "Services", Icons.Filled.Build),
    BottomItem(Routes.PROBLEMS, "Problems", Icons.Filled.Warning),
    BottomItem(Routes.SETTINGS, "Settings", Icons.Filled.Settings),
)

@Composable
fun AppNav(mainViewModel: MainViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = bottomItems.any { it.route == currentRoute }

    val settings by mainViewModel.settings.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        mainViewModel.refresh()
    }

    LaunchedEffect(settings.autoRefreshSeconds) {
        val interval = settings.autoRefreshSeconds
        if (interval > 0) {
            while (true) {
                delay(interval * 1000L)
                mainViewModel.refresh()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOSTS,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.HOSTS) {
                HostsScreen(
                    snackbarHostState = snackbarHostState,
                    onHostClick = { name -> navController.navigate(Routes.hostDetail(name)) },
                    onServiceFilterClick = {
                        navController.navigate(Routes.SERVICES) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable(Routes.SERVICES) {
                ServicesScreen(
                    snackbarHostState = snackbarHostState,
                    onServiceClick = { host, description ->
                        navController.navigate(Routes.serviceDetail(host, description))
                    },
                    onHostFilterClick = {
                        navController.navigate(Routes.HOSTS) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable(Routes.PROBLEMS) {
                ProblemsScreen(
                    snackbarHostState = snackbarHostState,
                    onHostClick = { name -> navController.navigate(Routes.hostDetail(name)) },
                    onServiceClick = { host, description ->
                        navController.navigate(Routes.serviceDetail(host, description))
                    },
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen()
            }
            composable(
                route = Routes.HOST_DETAIL,
                arguments = listOf(navArgument(Routes.ARG_HOST_NAME) { type = NavType.StringType }),
            ) {
                HostDetailScreen(
                    snackbarHostState = snackbarHostState,
                    onBack = { navController.popBackStack() },
                    onServiceClick = { host, description ->
                        navController.navigate(Routes.serviceDetail(host, description))
                    },
                )
            }
            composable(
                route = Routes.SERVICE_DETAIL,
                arguments = listOf(
                    navArgument(Routes.ARG_HOST_NAME) { type = NavType.StringType },
                    navArgument(Routes.ARG_SERVICE_DESCRIPTION) { type = NavType.StringType },
                ),
            ) {
                ServiceDetailScreen(
                    snackbarHostState = snackbarHostState,
                    onBack = { navController.popBackStack() },
                    onHostClick = { name -> navController.navigate(Routes.hostDetail(name)) },
                )
            }
        }
    }
}

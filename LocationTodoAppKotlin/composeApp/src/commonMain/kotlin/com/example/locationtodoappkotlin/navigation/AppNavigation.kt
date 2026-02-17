package com.example.locationtodoappkotlin.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.locationtodoappkotlin.presentation.alltasks.AllTasksScreen
import com.example.locationtodoappkotlin.presentation.alltasks.AllTasksViewModel
import com.example.locationtodoappkotlin.presentation.availabletasks.AvailableTasksScreen
import com.example.locationtodoappkotlin.presentation.availabletasks.AvailableTasksViewModel
import com.example.locationtodoappkotlin.presentation.createtask.CreateTaskScreen
import com.example.locationtodoappkotlin.presentation.createtask.CreateTaskViewModel
import com.example.locationtodoappkotlin.presentation.edittask.EditTaskScreen
import com.example.locationtodoappkotlin.presentation.edittask.EditTaskViewModel
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
object AllTasksRoute

@Serializable
object AvailableTasksRoute

@Serializable
object CreateTaskRoute

@Serializable
data class EditTaskRoute(val taskId: String)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.let { dest ->
        dest.hasRoute<AllTasksRoute>() || dest.hasRoute<AvailableTasksRoute>()
    } ?: true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentDestination?.hasRoute<AllTasksRoute>() == true,
                        onClick = {
                            navController.navigate(AllTasksRoute) {
                                popUpTo(AllTasksRoute) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                        label = { Text("All Tasks") }
                    )
                    NavigationBarItem(
                        selected = currentDestination?.hasRoute<AvailableTasksRoute>() == true,
                        onClick = {
                            navController.navigate(AvailableTasksRoute) {
                                popUpTo(AllTasksRoute) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        label = { Text("Available") }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AllTasksRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<AllTasksRoute> {
                val viewModel = koinViewModel<AllTasksViewModel>()
                AllTasksScreen(
                    viewModel = viewModel,
                    onCreateTask = { navController.navigate(CreateTaskRoute) },
                    onEditTask = { taskId -> navController.navigate(EditTaskRoute(taskId)) }
                )
            }
            composable<AvailableTasksRoute> {
                val viewModel = koinViewModel<AvailableTasksViewModel>()
                AvailableTasksScreen(
                    viewModel = viewModel,
                    onEditTask = { taskId -> navController.navigate(EditTaskRoute(taskId)) }
                )
            }
            composable<CreateTaskRoute> {
                val viewModel = koinViewModel<CreateTaskViewModel>()
                CreateTaskScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<EditTaskRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<EditTaskRoute>()
                val viewModel = koinViewModel<EditTaskViewModel> { parametersOf(route.taskId) }
                EditTaskScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

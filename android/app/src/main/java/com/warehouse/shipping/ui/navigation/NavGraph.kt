package com.warehouse.shipping.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.warehouse.shipping.ui.inventory.InventoryEditScreen
import com.warehouse.shipping.ui.batch.BatchListScreen
import com.warehouse.shipping.ui.batch.BatchDetailScreen
import com.warehouse.shipping.ui.batch.BoxDetailScreen
import com.warehouse.shipping.ui.settings.DataManageScreen

sealed class Screen(val route: String) {
    object InventoryList : Screen("inventory_list")
    object InventoryEdit : Screen("inventory_edit/{id}") {
        fun createRoute(id: String) = "inventory_edit/$id"
    }
    object BatchList : Screen("batch_list")
    object BatchDetail : Screen("batch_detail/{id}") {
        fun createRoute(id: String) = "batch_detail/$id"
    }
    object BoxDetail : Screen("box_detail/{id}") {
        fun createRoute(id: String) = "box_detail/$id"
    }
    object Settings : Screen("settings")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.BatchList.route
    ) {
        composable(Screen.InventoryList.route) {
            // InventoryListScreen(navController, viewModel)
        }
        composable(Screen.InventoryEdit.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            InventoryEditScreen(navController, id)
        }
        composable(Screen.BatchList.route) {
            BatchListScreen(navController, emptyList())
        }
        composable(Screen.BatchDetail.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            BatchDetailScreen(navController, id ?: "", null, emptyList())
        }
        composable(Screen.BoxDetail.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            BoxDetailScreen(navController, id ?: "", null, emptyList())
        }
        composable(Screen.Settings.route) {
            DataManageScreen(navController)
        }
    }
}

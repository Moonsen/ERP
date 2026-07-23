package com.warehouse.shipping.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.warehouse.shipping.ui.inventory.InventoryListScreen
import com.warehouse.shipping.ui.inventory.InventoryListViewModel
import com.warehouse.shipping.ui.inventory.InventoryEditScreen
import com.warehouse.shipping.ui.inventory.InventoryEditViewModel
import com.warehouse.shipping.ui.batch.BatchListScreen
import com.warehouse.shipping.ui.batch.BatchDetailScreen
import com.warehouse.shipping.ui.batch.BoxDetailScreen
import com.warehouse.shipping.ui.batch.viewmodel.BatchViewModel
import com.warehouse.shipping.ui.settings.DataManageScreen
import com.warehouse.shipping.ui.settings.SettingsViewModel
import com.warehouse.shipping.ui.scan.BarcodeScannerScreen

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
    object Scanner : Screen("scanner")
    object Settings : Screen("settings")
}

@Composable
fun NavGraph(
    navController: NavHostController, 
    factory: ViewModelProvider.Factory,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.BatchList.route,
        modifier = modifier
    ) {
        composable(Screen.InventoryList.route) {
            val vm: InventoryListViewModel = viewModel(factory = factory)
            InventoryListScreen(navController, vm)
        }
        composable(Screen.InventoryEdit.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            val vm: InventoryEditViewModel = viewModel(factory = factory)
            InventoryEditScreen(navController, vm, id)
        }
        composable(Screen.BatchList.route) {
            val vm: BatchViewModel = viewModel(factory = factory)
            BatchListScreen(navController, vm)
        }
        composable(Screen.BatchDetail.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            val vm: BatchViewModel = viewModel(factory = factory)
            BatchDetailScreen(navController, id ?: "", vm)
        }
        composable(Screen.BoxDetail.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            val vm: BatchViewModel = viewModel(factory = factory)
            BoxDetailScreen(navController, id ?: "", vm)
        }
        composable(Screen.Scanner.route) {
            BarcodeScannerScreen(navController) { barcode ->
                navController.previousBackStackEntry?.savedStateHandle?.set("scan_result", barcode)
                navController.popBackStack()
            }
        }
        composable(Screen.Settings.route) {
            val vm: SettingsViewModel = viewModel(factory = factory)
            DataManageScreen(navController, vm)
        }
    }
}

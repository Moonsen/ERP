package com.warehouse.shipping

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.warehouse.shipping.data.local.AppDatabase
import com.warehouse.shipping.data.repository.WarehouseRepository
import com.warehouse.shipping.ui.navigation.NavGraph
import com.warehouse.shipping.ui.navigation.Screen
import com.warehouse.shipping.ui.inventory.*
import com.warehouse.shipping.ui.batch.viewmodel.BatchViewModel
import com.warehouse.shipping.ui.settings.SettingsViewModel

import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private lateinit var db: AppDatabase
    private lateinit var repository: WarehouseRepository

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "warehouse.db")
            .fallbackToDestructiveMigration()
            .build()
        repository = WarehouseRepository(db)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
        
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(InventoryListViewModel::class.java) -> InventoryListViewModel(repository) as T
                    modelClass.isAssignableFrom(InventoryEditViewModel::class.java) -> InventoryEditViewModel(repository) as T
                    modelClass.isAssignableFrom(BatchViewModel::class.java) -> BatchViewModel(repository) as T
                    modelClass.isAssignableFrom(SettingsViewModel::class.java) -> SettingsViewModel(repository, db, applicationContext) as T
                    else -> throw IllegalArgumentException("Unknown ViewModel")
                }
            }
        }

        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                // 定义需要显示底部导航栏的页面
                val topLevelScreens = listOf(
                    Screen.BatchList,
                    Screen.InventoryList,
                    Screen.Settings
                )

                Scaffold(
                    bottomBar = {
                        // 只在主页面显示底部导航，详情页不显示以留出空间
                        if (currentDestination?.route in topLevelScreens.map { it.route }) {
                            NavigationBar {
                                topLevelScreens.forEach { screen ->
                                    NavigationBarItem(
                                        icon = { 
                                            Icon(
                                                when(screen) {
                                                    Screen.BatchList -> Icons.Default.LocalShipping
                                                    Screen.InventoryList -> Icons.Default.Inventory
                                                    else -> Icons.Default.CloudSync
                                                }, 
                                                contentDescription = null
                                            ) 
                                        },
                                        label = { 
                                            Text(when(screen) {
                                                Screen.BatchList -> "批次"
                                                Screen.InventoryList -> "产品库"
                                                else -> "管理"
                                            })
                                        },
                                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                        onClick = {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavGraph(
                        navController = navController, 
                        factory = factory,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

package com.warehouse.shipping

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

// 自定义精美色调
private val PrimaryBlue = Color(0xFF2563EB)
private val BackgroundGray = Color(0xFFF8FAFC)
private val SurfaceWhite = Color(0xFFFFFFFF)

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
            val customColorScheme = lightColorScheme(
                primary = PrimaryBlue,
                onPrimary = Color.White,
                background = BackgroundGray,
                surface = SurfaceWhite,
                onSurface = Color(0xFF1E293B),
                primaryContainer = Color(0xFFDBEAFE),
                onPrimaryContainer = PrimaryBlue
            )

            MaterialTheme(colorScheme = customColorScheme) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val topLevelScreens = listOf(
                    Screen.BatchList,
                    Screen.InventoryList,
                    Screen.Settings
                )

                Scaffold(
                    containerColor = BackgroundGray,
                    bottomBar = {
                        if (currentDestination?.route in topLevelScreens.map { it.route }) {
                            NavigationBar(
                                containerColor = SurfaceWhite,
                                tonalElevation = 8.dp
                            ) {
                                topLevelScreens.forEach { screen ->
                                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                                    NavigationBarItem(
                                        icon = { 
                                            Icon(
                                                when(screen) {
                                                    Screen.BatchList -> Icons.Default.LocalShipping
                                                    Screen.InventoryList -> Icons.Default.Inventory
                                                    else -> Icons.Default.CloudSync
                                                }, 
                                                contentDescription = null,
                                                tint = if (isSelected) PrimaryBlue else Color(0xFF94A3B8)
                                            ) 
                                        },
                                        label = { 
                                            Text(
                                                when(screen) {
                                                    Screen.BatchList -> "发货批次"
                                                    Screen.InventoryList -> "产品库"
                                                    else -> "多端同步"
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isSelected) PrimaryBlue else Color(0xFF94A3B8)
                                            )
                                        },
                                        selected = isSelected,
                                        onClick = {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            indicatorColor = Color(0xFFDBEAFE)
                                        )
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

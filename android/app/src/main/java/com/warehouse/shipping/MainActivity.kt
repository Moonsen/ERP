package com.warehouse.shipping

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.warehouse.shipping.data.local.AppDatabase
import com.warehouse.shipping.data.repository.WarehouseRepository
import com.warehouse.shipping.ui.navigation.NavGraph
import com.warehouse.shipping.ui.inventory.*
import com.warehouse.shipping.ui.batch.viewmodel.BatchViewModel

import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request Camera Permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
        
        // Manual Factory (In real app use Hilt)
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(InventoryListViewModel::class.java) -> InventoryListViewModel(repository) as T
                    modelClass.isAssignableFrom(InventoryEditViewModel::class.java) -> InventoryEditViewModel(repository) as T
                    modelClass.isAssignableFrom(BatchViewModel::class.java) -> BatchViewModel(repository) as T
                    else -> throw IllegalArgumentException("Unknown ViewModel")
                }
            }
        }

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController, factory = factory)
                }
            }
        }
    }
}

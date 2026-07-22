package com.warehouse.shipping.ui.batch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.warehouse.shipping.data.local.entity.BoxEntity
import com.warehouse.shipping.data.local.entity.BoxProductEntity
import com.warehouse.shipping.ui.product.ProductPickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoxDetailScreen(
    navController: NavController,
    boxId: String,
    box: BoxEntity?,
    products: List<BoxProductEntity>
) {
    var showPickerDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (box != null) "第 ${box.box_number} 箱" else "箱子详情") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showPickerDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (box != null) {
                Card(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("箱子规格: ${box.length_cm}x${box.width_cm}x${box.height_cm}cm")
                        Text("整箱重量: ${box.weight_kg}kg")
                    }
                }
            }

            LazyColumn {
                items(products) { product ->
                    ListItem(
                        headlineContent = { Text(product.name) },
                        supportingContent = { Text("数量: ${product.quantity} | ${product.weight_g}g") },
                        trailingContent = { Text("${product.length_cm}x${product.width_cm}x${product.height_cm}") }
                    )
                }
            }
        }

        if (showPickerDialog) {
            ProductPickerDialog(
                inventory = emptyList(), // Should be passed from ViewModel
                onDismiss = { showPickerDialog = false },
                onProductSelected = { selected, qty ->
                    // Save logic
                    showPickerDialog = false
                },
                onManualInput = { name, barcode, l, w, h, weight, qty ->
                    // Save logic
                    showPickerDialog = false
                }
            )
        }
    }
}

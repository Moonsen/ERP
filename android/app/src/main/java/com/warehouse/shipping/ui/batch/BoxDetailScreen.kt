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
import com.warehouse.shipping.ui.batch.viewmodel.BatchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoxDetailScreen(
    navController: NavController,
    boxId: String,
    viewModel: BatchViewModel
) {
    var box by remember { mutableStateOf<BoxEntity?>(null) }
    val products by viewModel.getProducts(boxId).collectAsState(initial = emptyList())
    val inventory by viewModel.allInventory.collectAsState(initial = emptyList())

    LaunchedEffect(boxId) {
        box = viewModel.getBox(boxId)
    }

    var showPickerDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (box != null) "第 ${box?.box_number} 箱" else "箱子详情") },
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
            box?.let { b ->
                Card(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("箱子规格: ${b.length_cm}x${b.width_cm}x${b.height_cm}cm")
                        Text("整箱重量: ${b.weight_kg}kg")
                    }
                }
            }

            LazyColumn {
                items(products) { item ->
                    ListItem(
                        headlineContent = { Text(item.name) },
                        supportingContent = { Text("数量: ${item.quantity} | ${item.weight_g}g") },
                        trailingContent = { Text("${item.length_cm}x${item.width_cm}x${item.height_cm}") }
                    )
                }
            }
        }

        if (showPickerDialog) {
            ProductPickerDialog(
                inventory = inventory,
                onDismiss = { showPickerDialog = false },
                onProductSelected = { selected, qty ->
                    viewModel.addProduct(boxId, selected, selected.name, selected.barcode, 
                        selected.length_cm, selected.width_cm, selected.height_cm, selected.weight_g, qty)
                    showPickerDialog = false
                },
                onManualInput = { name, barcode, l, w, h, weight, qty ->
                    viewModel.addProduct(boxId, null, name, barcode, l, w, h, weight, qty)
                    showPickerDialog = false
                }
            )
        }
    }
}

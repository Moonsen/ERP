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

import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoxDetailScreen(
    navController: NavController,
    boxId: String,
    viewModel: BatchViewModel
) {
    var currentBox by remember { mutableStateOf<BoxEntity?>(null) }
    val products by viewModel.getProducts(boxId).collectAsState(initial = emptyList())
    val inventory by viewModel.allInventory.collectAsState(initial = emptyList())

    LaunchedEffect(boxId) {
        currentBox = viewModel.getBox(boxId)
    }

    var showPickerDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<BoxProductEntity?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    
    // Edit Fields
    var eName by remember { mutableStateOf("") }
    var eQty by remember { mutableStateOf("1") }
    var eL by remember { mutableStateOf("") }
    var eW by remember { mutableStateOf("") }
    var eH by remember { mutableStateOf("") }
    var eWeight by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    val b = currentBox
                    Text(if (b != null) "第 ${b.box_number} 箱" else "箱子详情") 
                },
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
            currentBox?.let { b ->
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
                        supportingContent = { 
                            ClickToCopyText(
                                text = "数量: ${item.quantity} | ${item.weight_g}g"
                            )
                        },
                        trailingContent = { 
                            Row {
                                IconButton(onClick = {
                                    editingProduct = item
                                    eName = item.name
                                    eQty = item.quantity.toString()
                                    eL = item.length_cm.toString()
                                    eW = item.width_cm.toString()
                                    eH = item.height_cm.toString()
                                    eWeight = item.weight_g.toString()
                                    showEditDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = { viewModel.deleteBoxProduct(item.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    )
                }
            }
        }

        if (showEditDialog && editingProduct != null) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("编辑产品") },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        OutlinedTextField(value = eName, onValueChange = { eName = it }, label = { Text("名称") })
                        OutlinedTextField(value = eQty, onValueChange = { eQty = it }, label = { Text("数量") })
                        Row {
                            OutlinedTextField(value = eL, onValueChange = { eL = it }, label = { Text("长") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = eW, onValueChange = { eW = it }, label = { Text("宽") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = eH, onValueChange = { eH = it }, label = { Text("高") }, modifier = Modifier.weight(1f))
                        }
                        OutlinedTextField(value = eWeight, onValueChange = { eWeight = it }, label = { Text("重量(g)") })
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.updateBoxProduct(editingProduct!!.copy(
                            name = eName,
                            quantity = eQty.toIntOrNull() ?: 1,
                            length_cm = eL.toDoubleOrNull() ?: 0.0,
                            width_cm = eW.toDoubleOrNull() ?: 0.0,
                            height_cm = eH.toDoubleOrNull() ?: 0.0,
                            weight_g = eWeight.toDoubleOrNull() ?: 0.0
                        ))
                        showEditDialog = false
                    }) { Text("确定") }
                },
                dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("取消") } }
            )
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

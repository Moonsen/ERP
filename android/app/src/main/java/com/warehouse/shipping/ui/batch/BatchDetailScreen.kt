package com.warehouse.shipping.ui.batch

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.warehouse.shipping.data.local.entity.BatchEntity
import com.warehouse.shipping.data.local.entity.BoxEntity
import com.warehouse.shipping.ui.batch.viewmodel.BatchViewModel
import com.warehouse.shipping.ui.components.ClickToCopyText
import com.warehouse.shipping.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchDetailScreen(
    navController: NavController,
    batchId: String,
    viewModel: BatchViewModel
) {
    val batches by viewModel.batches.collectAsState(initial = emptyList())
    val batch = batches.find { b -> b.id == batchId }
    val boxes by viewModel.getBoxes(batchId).collectAsState(initial = emptyList())
    
    var showBoxDialog by remember { mutableStateOf(false) }
    var editingBox by remember { mutableStateOf<BoxEntity?>(null) }
    
    var length by remember { mutableStateOf("50.0") }
    var width by remember { mutableStateOf("40.0") }
    var height by remember { mutableStateOf("30.0") }
    var weight by remember { mutableStateOf("0.0") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(batch?.name ?: "批次详情") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                editingBox = null
                length = "50.0"; width = "40.0"; height = "30.0"; weight = "0.0"
                showBoxDialog = true 
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Box")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            val totalWeight = boxes.sumOf { it.weight_kg }
            val totalVolumeCm = boxes.sumOf { it.length_cm * it.width_cm * it.height_cm }
            val totalVolumeM3 = totalVolumeCm / 1000000.0

            Card(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (batch != null) {
                        Text("批次: ${batch.name}", style = MaterialTheme.typography.titleMedium)
                        Text("目的地: ${batch.destination ?: "未设置"}", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("总重量", style = MaterialTheme.typography.labelSmall)
                            Text("${String.format("%.2f", totalWeight)} kg", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("总体积 (cm³)", style = MaterialTheme.typography.labelSmall)
                            Text(String.format("%.2f", totalVolumeCm), color = MaterialTheme.colorScheme.primary)
                        }
                        Column {
                            Text("总体积 (m³)", style = MaterialTheme.typography.labelSmall)
                            Text(String.format("%.2f", totalVolumeM3), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            LazyColumn {
                items(boxes) { item ->
                    ListItem(
                        headlineContent = { Text("第 ${item.box_number} 箱") },
                        supportingContent = { 
                            ClickToCopyText(
                                text = "${item.length_cm}x${item.width_cm}x${item.height_cm}cm | ${item.weight_kg}kg"
                            )
                        },
                        trailingContent = {
                            Row {
                                IconButton(onClick = {
                                    editingBox = item
                                    length = item.length_cm.toString()
                                    width = item.width_cm.toString()
                                    height = item.height_cm.toString()
                                    weight = item.weight_kg.toString()
                                    showBoxDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = { viewModel.deleteBox(item.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        },
                        modifier = Modifier.clickable { 
                            navController.navigate(Screen.BoxDetail.createRoute(item.id))
                        }
                    )
                }
            }
        }

        if (showBoxDialog) {
            AlertDialog(
                onDismissRequest = { showBoxDialog = false },
                title = { Text(if (editingBox == null) "添加箱子" else "编辑箱子") },
                text = {
                    Column {
                        Row {
                            OutlinedTextField(value = length, onValueChange = { length = it }, label = { Text("长") }, modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(4.dp))
                            OutlinedTextField(value = width, onValueChange = { width = it }, label = { Text("宽") }, modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(4.dp))
                            OutlinedTextField(value = height, onValueChange = { height = it }, label = { Text("高") }, modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("重量(kg)") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val l = length.toDoubleOrNull() ?: 0.0
                        val w = width.toDoubleOrNull() ?: 0.0
                        val h = height.toDoubleOrNull() ?: 0.0
                        val wg = weight.toDoubleOrNull() ?: 0.0
                        
                        if (editingBox == null) {
                            viewModel.addBox(batchId, l, w, h, wg)
                        } else {
                            viewModel.updateBox(editingBox!!.copy(length_cm = l, width_cm = w, height_cm = h, weight_kg = wg))
                        }
                        showBoxDialog = false
                    }) { Text("确定") }
                },
                dismissButton = {
                    TextButton(onClick = { showBoxDialog = false }) { Text("取消") }
                }
            )
        }
    }
}

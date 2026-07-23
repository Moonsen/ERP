package com.warehouse.shipping.ui.batch

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.warehouse.shipping.data.local.entity.BatchEntity
import com.warehouse.shipping.ui.navigation.Screen
import com.warehouse.shipping.ui.batch.viewmodel.BatchViewModel

import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchListScreen(
    navController: NavController,
    viewModel: BatchViewModel
) {
    val batches by viewModel.batches.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var editingBatch by remember { mutableStateOf<BatchEntity?>(null) }
    var batchName by remember { mutableStateOf("") }
    var batchDest by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("发货批次") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                editingBatch = null
                batchName = ""
                batchDest = ""
                showAddDialog = true 
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(batches) { item ->
                ListItem(
                    headlineContent = { Text(item.name) },
                    supportingContent = { Text(item.destination ?: "无目的地") },
                    trailingContent = { 
                        Row {
                            IconButton(onClick = { 
                                editingBatch = item
                                batchName = item.name
                                batchDest = item.destination ?: ""
                                showAddDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { viewModel.deleteBatch(item.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    },
                    modifier = Modifier.clickable { 
                        navController.navigate(Screen.BatchDetail.createRoute(item.id))
                    }
                )
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text(if (editingBatch == null) "新建批次" else "编辑批次") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = batchName,
                            onValueChange = { batchName = it },
                            label = { Text("批次名称") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = batchDest,
                            onValueChange = { batchDest = it },
                            label = { Text("目的地") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (editingBatch == null) {
                            viewModel.createBatch(batchName, batchDest)
                        } else {
                            viewModel.updateBatch(editingBatch!!.copy(name = batchName, destination = batchDest))
                        }
                        showAddDialog = false
                    }) { Text("确定") }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("取消") }
                }
            )
        }
    }
}

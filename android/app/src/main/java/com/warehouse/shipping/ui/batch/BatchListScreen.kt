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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchListScreen(
    navController: NavController,
    viewModel: BatchViewModel
) {
    val batches by viewModel.batches.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var newBatchName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("发货批次") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(batches) { batch ->
                ListItem(
                    headlineContent = { Text(batch.name) },
                    supportingContent = { Text(batch.destination ?: "无目的地") },
                    trailingContent = { Text(batch.created_at.take(10)) },
                    modifier = Modifier.clickable { 
                        navController.navigate(Screen.BatchDetail.createRoute(batch.id))
                    }
                )
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("新建批次") },
                text = {
                    OutlinedTextField(
                        value = newBatchName,
                        onValueChange = { newBatchName = it },
                        label = { Text("批次名称") }
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.createBatch(newBatchName, null)
                        showAddDialog = false
                    }) { Text("确定") }
                }
            )
        }
    }
}

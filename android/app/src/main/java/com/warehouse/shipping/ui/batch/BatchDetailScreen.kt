package com.warehouse.shipping.ui.batch

import androidx.compose.foundation.clickable
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
import com.warehouse.shipping.data.local.entity.BatchEntity
import com.warehouse.shipping.data.local.entity.BoxEntity
import com.warehouse.shipping.ui.navigation.Screen
import com.warehouse.shipping.ui.batch.viewmodel.BatchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchDetailScreen(
    navController: NavController,
    batchId: String,
    viewModel: BatchViewModel
) {
    val batches by viewModel.batches.collectAsState(initial = emptyList())
    val batch = batches.find { it.id == batchId }
    val boxes by viewModel.getBoxes(batchId).collectAsState(initial = emptyList())
    
    var showAddBoxDialog by remember { mutableStateOf(false) }

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
            FloatingActionButton(onClick = { showAddBoxDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Box")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (batch != null) {
                Card(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("目的地: ${batch.destination ?: "未设置"}")
                        Text("备注: ${batch.remark ?: "无"}")
                    }
                }
            }

            LazyColumn {
                items(boxes) { box ->
                    ListItem(
                        headlineContent = { Text("第 ${box.box_number} 箱") },
                        supportingContent = { Text("${box.length_cm}x${box.width_cm}x${box.height_cm}cm | ${box.weight_kg}kg") },
                        modifier = Modifier.clickable { 
                            navController.navigate(Screen.BoxDetail.createRoute(box.id))
                        }
                    )
                }
            }
        }

        if (showAddBoxDialog) {
            // Simplified Add Box Dialog
            AlertDialog(
                onDismissRequest = { showAddBoxDialog = false },
                title = { Text("添加箱子") },
                text = { Text("确认添加一个新的空箱？规格默认 50x40x30cm") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.addBox(batchId, 50.0, 40.0, 30.0, 0.0)
                        showAddBoxDialog = false
                    }) { Text("添加") }
                }
            )
        }
    }
}

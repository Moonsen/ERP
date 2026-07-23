package com.warehouse.shipping.ui.batch

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = { Text(batch?.name ?: "批次详情", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    editingBox = null
                    length = "50.0"; width = "40.0"; height = "30.0"; weight = "0.0"
                    showBoxDialog = true 
                },
                containerColor = Color(0xFF2563EB),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Box")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // 汇总卡片
            val totalWeight = boxes.sumOf { it.weight_kg }
            val totalVolumeCm = boxes.sumOf { it.length_cm * it.width_cm * it.height_cm }
            val totalVolumeM3 = totalVolumeCm / 1000000.0

            Card(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = Color(0xFFDBEAFE), shape = RoundedCornerShape(8.dp)) {
                            Icon(Icons.Default.Scale, contentDescription = null, modifier = Modifier.padding(6.dp).size(20.dp), tint = Color(0xFF2563EB))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("发货汇总信息", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("${boxes.size} 个箱子", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatItem("总重量", "${String.format("%.2f", totalWeight)}kg", Color(0xFFF59E0B))
                        StatItem("体积 (cm³)", String.format("%.0f", totalVolumeCm), Color(0xFF10B981))
                        StatItem("体积 (m³)", String.format("%.3f", totalVolumeM3), Color(0xFFEF4444))
                    }
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Text(
                        "箱子明细列表",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF64748B)
                    )
                }
                items(boxes) { item ->
                    BoxCard(item, onEdit = {
                        editingBox = item
                        length = item.length_cm.toString()
                        width = item.width_cm.toString()
                        height = item.height_cm.toString()
                        weight = item.weight_kg.toString()
                        showBoxDialog = true
                    }, onDelete = { viewModel.deleteBox(item.id) }) {
                        navController.navigate(Screen.BoxDetail.createRoute(item.id))
                    }
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
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(value = width, onValueChange = { width = it }, label = { Text("宽") }, modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(value = height, onValueChange = { height = it }, label = { Text("高") }, modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("整箱重量 (kg)") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    Button(onClick = {
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
                }
            )
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.ExtraBold, color = color)
    }
}

@Composable
fun BoxCard(box: BoxEntity, onEdit: () -> Unit, onDelete: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(color = Color(0xFFF1F5F9), shape = RoundedCornerShape(8.dp)) {
                Text(
                    "#${box.box_number}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Straighten, null, Modifier.size(12.dp), Color(0xFF64748B))
                    Spacer(modifier = Modifier.width(4.dp))
                    ClickToCopyText(
                        text = "${box.length_cm}x${box.width_cm}x${box.height_cm}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF1E293B))
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Scale, null, Modifier.size(12.dp), Color(0xFF64748B))
                    Spacer(modifier = Modifier.width(4.dp))
                    ClickToCopyText(
                        text = "${box.weight_kg} kg",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                    )
                }
            }
            Row {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = Color(0xFF94A3B8)) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444)) }
            }
        }
    }
}

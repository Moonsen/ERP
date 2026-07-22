package com.warehouse.shipping.ui.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryEditScreen(
    navController: NavController,
    id: String?
) {
    // In a real app, use a ViewModel to load/save
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var length by remember { mutableStateOf("") }
    var width by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (id == "new") "新建产品" else "编辑产品") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Save logic */ navController.popBackStack() }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("产品名称 *") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("产品编码") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = barcode, onValueChange = { barcode = it }, label = { Text("条形码") }, modifier = Modifier.fillMaxWidth())
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("尺寸与重量", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = length, onValueChange = { length = it }, label = { Text("长(cm)") }, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(value = width, onValueChange = { width = it }, label = { Text("宽(cm)") }, modifier = Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = height, onValueChange = { height = it }, label = { Text("高(cm)") }, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("重量(g)") }, modifier = Modifier.weight(1f))
            }
        }
    }
}

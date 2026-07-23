package com.warehouse.shipping.ui.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.warehouse.shipping.ui.navigation.Screen
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

@Serializable
data class CustomSpec(val key: String, val value: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryEditScreen(
    navController: NavController,
    viewModel: InventoryEditViewModel,
    id: String?
) {
    val product by viewModel.product.collectAsState()
    
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var length by remember { mutableStateOf("") }
    var width by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    
    var customSpecs by remember { mutableStateOf(mutableListOf<CustomSpec>()) }

    // Listen for scan result
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val scanResult by savedStateHandle?.getStateFlow<String?>("scan_result", null)?.collectAsState() ?: remember { mutableStateOf(null) }

    var lastTarget by remember { mutableStateOf("") } // Track which field we are scanning for

    LaunchedEffect(scanResult) {
        scanResult?.let { result ->
            if (lastTarget == "barcode") {
                barcode = result
            } else if (lastTarget == "code") {
                code = result
            }
            savedStateHandle?.set("scan_result", null)
        }
    }

    LaunchedEffect(id) {
        viewModel.loadProduct(id)
    }

    LaunchedEffect(product) {
        product?.let {
            name = it.name
            code = it.product_code ?: ""
            barcode = it.barcode ?: ""
            length = it.length_cm.toString()
            width = it.width_cm.toString()
            height = it.height_cm.toString()
            weight = it.weight_g.toString()
            
            try {
                it.custom_specs?.let { json ->
                    customSpecs = Json.decodeFromString<List<CustomSpec>>(json).toMutableList()
                }
            } catch (e: Exception) {
                customSpecs = mutableListOf()
            }
        }
    }

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
                    IconButton(onClick = { 
                        val specsJson = if (customSpecs.isEmpty()) null else Json.encodeToString(customSpecs)
                        viewModel.saveProduct(
                            id, name, code, barcode,
                            length.toDoubleOrNull() ?: 0.0,
                            width.toDoubleOrNull() ?: 0.0,
                            height.toDoubleOrNull() ?: 0.0,
                            weight.toDoubleOrNull() ?: 0.0,
                            specsJson
                        ) {
                            navController.popBackStack()
                        }
                    }) {
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
            OutlinedTextField(
                value = name, 
                onValueChange = { name = it }, 
                label = { Text("产品名称 *") }, 
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = code, 
                onValueChange = { code = it }, 
                label = { Text("产品编码") }, 
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { 
                        lastTarget = "code"
                        navController.navigate(Screen.Scanner.route) 
                    }) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = "Scan Code")
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = barcode, 
                onValueChange = { barcode = it }, 
                label = { Text("条形码") }, 
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { 
                        lastTarget = "barcode"
                        navController.navigate(Screen.Scanner.route) 
                    }) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = "Scan Barcode")
                    }
                }
            )
            
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

            Spacer(modifier = Modifier.height(24.dp))
            Text("自定义规格", style = MaterialTheme.typography.titleMedium)
            
            customSpecs.forEachIndexed { index, spec ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = spec.key, 
                        onValueChange = { newKey -> 
                            val newList = customSpecs.toMutableList()
                            newList[index] = newList[index].copy(key = newKey)
                            customSpecs = newList
                        }, 
                        label = { Text("名称") }, 
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = spec.value, 
                        onValueChange = { newVal -> 
                            val newList = customSpecs.toMutableList()
                            newList[index] = newList[index].copy(value = newVal)
                            customSpecs = newList
                        }, 
                        label = { Text("数值") }, 
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { 
                        val newList = customSpecs.toMutableList()
                        newList.removeAt(index)
                        customSpecs = newList
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Button(
                onClick = { 
                    val newList = customSpecs.toMutableList()
                    newList.add(CustomSpec("", ""))
                    customSpecs = newList
                }, 
                modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
            ) {
                Text("+ 添加规格")
            }
        }
    }
}

package com.warehouse.shipping.ui.product

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.warehouse.shipping.data.local.entity.ProductInventoryEntity

@Composable
fun ProductPickerDialog(
    inventory: List<ProductInventoryEntity>,
    onDismiss: () -> Unit,
    onProductSelected: (ProductInventoryEntity, Int) -> Unit,
    onManualInput: (String, String?, Double, Double, Double, Double, Int) -> Unit,
    onScanRequest: () -> Unit = {} // Added Scan Request
) {
    var activeTab by remember { mutableStateOf(0) } // 0: Select, 1: Manual
    var searchQuery by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<ProductInventoryEntity?>(null) }
    var quantity by remember { mutableStateOf("1") }

    // Manual Input States
    var mName by remember { mutableStateOf("") }
    var mBarcode by remember { mutableStateOf("") }
    var mLength by remember { mutableStateOf("") }
    var mWidth by remember { mutableStateOf("") }
    var mHeight by remember { mutableStateOf("") }
    var mWeightG by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                TabRow(selectedTabIndex = activeTab) {
                    Tab(selected = activeTab == 0, onClick = { activeTab = 0 }) {
                        Text("从库选择", modifier = Modifier.padding(12.dp))
                    }
                    Tab(selected = activeTab == 1, onClick = { activeTab = 1 }) {
                        Text("手动输入", modifier = Modifier.padding(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (activeTab == 0) {
                    // Select Mode
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("搜索产品...") },
                        trailingIcon = { 
                            IconButton(onClick = onScanRequest) { 
                                Icon(Icons.Default.PhotoCamera, contentDescription = "Scan") 
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(inventory.filter { it.name.contains(searchQuery, true) }) { product ->
                            ListItem(
                                headlineContent = { Text(product.name) },
                                modifier = Modifier.clickable { selectedProduct = product }
                            )
                        }
                    }
                    if (selectedProduct != null) {
                        Text("已选: ${selectedProduct!!.name}", color = MaterialTheme.colorScheme.primary)
                        OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("数量") })
                        Button(onClick = { onProductSelected(selectedProduct!!, quantity.toIntOrNull() ?: 1) }, modifier = Modifier.fillMaxWidth()) {
                            Text("确认添加")
                        }
                    }
                } else {
                    // Manual Mode
                    Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                        OutlinedTextField(value = mName, onValueChange = { mName = it }, label = { Text("名称 *") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = mBarcode, onValueChange = { mBarcode = it }, label = { Text("条形码") }, modifier = Modifier.fillMaxWidth())
                        Row {
                            OutlinedTextField(value = mLength, onValueChange = { mLength = it }, label = { Text("长") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = mWidth, onValueChange = { mWidth = it }, label = { Text("宽") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = mHeight, onValueChange = { mHeight = it }, label = { Text("高") }, modifier = Modifier.weight(1f))
                        }
                        OutlinedTextField(value = mWeightG, onValueChange = { mWeightG = it }, label = { Text("重量(g)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("数量") }, modifier = Modifier.fillMaxWidth())
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { 
                            onManualInput(mName, mBarcode, mLength.toDoubleOrNull() ?: 0.0, 
                                mWidth.toDoubleOrNull() ?: 0.0, mHeight.toDoubleOrNull() ?: 0.0, 
                                mWeightG.toDoubleOrNull() ?: 0.0, quantity.toIntOrNull() ?: 1)
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("添加并存入产品库")
                        }
                    }
                }
            }
        }
    }
}

package com.warehouse.shipping.ui.inventory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.warehouse.shipping.data.local.entity.ProductInventoryEntity
import com.warehouse.shipping.ui.navigation.Screen
import com.warehouse.shipping.ui.components.ClickToCopyText
import androidx.compose.ui.res.painterResource
import com.warehouse.shipping.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryListScreen(
    navController: NavController,
    viewModel: InventoryListViewModel
) {
    val products by viewModel.products.collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }

    val scanResult = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<String>("scan_result")
        ?.observeAsState()

    LaunchedEffect(scanResult?.value) {
        scanResult?.value?.let { result ->
            searchQuery = result
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("scan_result")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_logo),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("产品库")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.InventoryEdit.createRoute("new")) }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("搜索产品名称/编码/条码") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { navController.navigate(Screen.Scanner.route) }) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = "Scan")
                    }
                }
            )

            LazyColumn {
                items(products.filter { 
                    it.name.contains(searchQuery, true) || 
                    it.barcode?.contains(searchQuery) == true ||
                    it.product_code?.contains(searchQuery) == true
                }) { product ->
                    ProductItem(product) {
                        navController.navigate(Screen.InventoryEdit.createRoute(product.id))
                    }
                }
            }
        }
    }
}

@Composable
fun ProductItem(product: ProductInventoryEntity, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(product.name) },
        supportingContent = { 
            ClickToCopyText(
                text = "${product.length_cm}x${product.width_cm}x${product.height_cm}cm | ${product.weight_g}g"
            )
        },
        overlineContent = { product.product_code?.let { Text(it) } },
        trailingContent = { product.barcode?.let { Text(it) } },
        modifier = Modifier.clickable { onClick() }
    )
}

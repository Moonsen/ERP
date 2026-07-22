package com.warehouse.shipping.ui.inventory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.warehouse.shipping.data.local.entity.ProductInventoryEntity
import com.warehouse.shipping.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryListScreen(
    navController: NavController,
    viewModel: InventoryListViewModel // Assume ViewModel is provided
) {
    val products by viewModel.products.collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("产品库") })
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
                placeholder = { Text("搜索产品...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            LazyColumn {
                items(products.filter { it.name.contains(searchQuery, ignoreCase = true) || it.barcode?.contains(searchQuery) == true }) { product ->
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
            Text("${product.length_cm}x${product.width_cm}x${product.height_cm}cm | ${product.weight_g}g") 
        },
        overlineContent = { product.product_code?.let { Text(it) } },
        trailingContent = { product.barcode?.let { Text(it) } },
        modifier = Modifier.clickable { onClick() }
    )
}

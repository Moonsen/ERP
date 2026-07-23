package com.warehouse.shipping.ui.inventory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.warehouse.shipping.R
import com.warehouse.shipping.data.local.entity.ProductInventoryEntity
import com.warehouse.shipping.ui.components.ClickToCopyText
import com.warehouse.shipping.ui.navigation.Screen

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                product.product_code?.let {
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoTag(icon = Icons.Default.Straighten, text = "${product.length_cm}x${product.width_cm}x${product.height_cm} cm")
                Spacer(modifier = Modifier.width(16.dp))
                InfoTag(icon = Icons.Default.Scale, text = "${product.weight_g} g")
            }

            if (!product.barcode.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "SN: ${product.barcode}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

@Composable
fun InfoTag(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = Color(0xFF2563EB)
        )
        Spacer(modifier = Modifier.width(4.dp))
        ClickToCopyText(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
        )
    }
}

package com.warehouse.shipping.ui.scan

import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerScreen(navController: NavController, onBarcodeScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("扫描条形码") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Camera Preview placeholder
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        // In a real app, initialize CameraX here
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Scanning Overlay
            Text(
                "将条形码置于框内",
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Simulation button for dev
            Button(
                onClick = { onBarcodeScanned("123456789") },
                modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp)
            ) {
                Text("模拟扫描")
            }
        }
    }
}

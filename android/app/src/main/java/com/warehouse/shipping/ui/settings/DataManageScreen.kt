package com.warehouse.shipping.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManageScreen(navController: NavController, viewModel: SettingsViewModel) {
    val scope = rememberCoroutineScope()
    val syncStatus by viewModel.syncStatus.collectAsState()
    
    var showRestoreConfirm by remember { mutableStateOf(false) }
    
    var url by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                viewModel.importBackup(it)
            }
        }
    }

    LaunchedEffect(Unit) {
        url = viewModel.getSavedConfig("webdav_url") ?: ""
        username = viewModel.getSavedConfig("webdav_user") ?: ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("数据管理") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("坚果云同步配置 (WebDAV)", style = MaterialTheme.typography.titleMedium)
                    if (syncStatus.isNotEmpty()) {
                        Text(syncStatus, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { Text("WebDAV 服务器地址") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("账号 (Email)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("应用密码") },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { 
                                scope.launch {
                                    viewModel.saveConfig("webdav_url", url)
                                    viewModel.saveConfig("webdav_user", username)
                                    viewModel.saveConfig("webdav_pass", password)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("保存配置")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = { viewModel.performSync(url, username, password) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("立即同步")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).padding(horizontal = 8.dp))
            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("本地备份与恢复", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { 
                            scope.launch {
                                viewModel.exportBackup()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("导出备份 (.json)")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showRestoreConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("恢复备份")
                    }
                }
            }
        }

        if (showRestoreConfirm) {
            AlertDialog(
                onDismissRequest = { showRestoreConfirm = false },
                title = { Text("确认恢复数据？") },
                text = { Text("恢复操作将清空当前所有本地数据，并替换为备份文件中的内容。") },
                confirmButton = {
                    TextButton(
                        onClick = { 
                            showRestoreConfirm = false 
                            filePicker.launch("application/json")
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("选择备份文件并恢复")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRestoreConfirm = false }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

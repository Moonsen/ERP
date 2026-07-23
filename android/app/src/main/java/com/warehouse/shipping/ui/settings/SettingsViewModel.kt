package com.warehouse.shipping.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warehouse.shipping.data.repository.WarehouseRepository
import com.warehouse.shipping.sync.SyncEngine
import com.warehouse.shipping.data.local.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: WarehouseRepository,
    private val db: AppDatabase,
    private val context: android.content.Context
) : ViewModel() {
    private val syncEngine = SyncEngine(db)
    private val backupManager = com.warehouse.shipping.util.BackupManager(db, context)
    
    private val _syncStatus = MutableStateFlow("")
    val syncStatus: StateFlow<String> = _syncStatus

    fun performSync(url: String, user: String, pass: String) {
        viewModelScope.launch {
            _syncStatus.value = "同步中..."
            val result = syncEngine.performSync(url, user, pass)
            if (result.isSuccess) {
                _syncStatus.value = "同步成功"
                repository.saveConfig("webdav_url", url)
                repository.saveConfig("webdav_user", user)
                repository.saveConfig("webdav_pass", pass)
            } else {
                _syncStatus.value = "失败: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun saveConfig(key: String, value: String) {
        viewModelScope.launch {
            repository.saveConfig(key, value)
        }
    }

    suspend fun exportBackup() = backupManager.exportBackup()
    suspend fun importBackup(uri: android.net.Uri) = backupManager.importBackup(uri)

    suspend fun getSavedConfig(key: String) = repository.getConfig(key)
}

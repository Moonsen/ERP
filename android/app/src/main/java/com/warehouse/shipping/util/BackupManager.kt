package com.warehouse.shipping.util

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.warehouse.shipping.data.local.AppDatabase
import com.warehouse.shipping.sync.SyncData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BackupManager(private val db: AppDatabase, private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true; encodeDefaults = true }

    suspend fun exportBackup(): File? = withContext(Dispatchers.IO) {
        try {
            val syncData = SyncData(
                batches = db.batchDao().getDirty("1970"),
                boxes = db.boxDao().getDirty("1970"),
                productInventory = db.productInventoryDao().getDirty("1970"),
                boxProducts = db.boxProductDao().getDirty("1970"),
                lastSyncTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            )
            
            val jsonString = json.encodeToString(syncData)
            val fileName = "warehouse-backup-${System.currentTimeMillis()}.json"
            val file = File(context.cacheDir, fileName)
            file.writeText(jsonString)
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun importBackup(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            if (jsonString != null) {
                val data = json.decodeFromString<SyncData>(jsonString)
                db.withTransaction {
                    // Physical delete all business data
                    db.boxProductDao().deleteAll()
                    db.productInventoryDao().deleteAll()
                    db.boxDao().deleteAll()
                    db.batchDao().deleteAll()

                    // Batch insert everything
                    for (batch in data.batches) {
                        db.batchDao().insert(batch)
                    }
                    for (box in data.boxes) {
                        db.boxDao().insert(box)
                    }
                    for (inv in data.productInventory) {
                        db.productInventoryDao().insert(inv)
                    }
                    for (prod in data.boxProducts) {
                        db.boxProductDao().insert(prod)
                    }
                }
                true
            } else false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

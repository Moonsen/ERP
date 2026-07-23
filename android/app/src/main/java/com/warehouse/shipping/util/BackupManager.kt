package com.warehouse.shipping.util

import android.content.Context
import android.net.Uri
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
                db.runInTransaction {
                    // Physical delete all business data as per spec 5.3
                    // We don't clear the 'config' table to keep WebDAV settings
                    db.query("DELETE FROM box_product", null)
                    db.query("DELETE FROM product_inventory", null)
                    db.query("DELETE FROM box", null)
                    db.query("DELETE FROM batch", null)

                    // Batch insert everything
                    data.batches.forEach { db.batchDao().insert(it) }
                    data.boxes.forEach { db.boxDao().insert(it) }
                    data.productInventory.forEach { db.productInventoryDao().insert(it) }
                    data.boxProducts.forEach { db.boxProductDao().insert(it) }
                }
                true
            } else false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

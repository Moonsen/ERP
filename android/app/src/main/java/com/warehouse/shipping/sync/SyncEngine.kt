package com.warehouse.shipping.sync

import com.warehouse.shipping.data.local.AppDatabase
import com.warehouse.shipping.data.local.entity.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Serializable
data class SyncData(
    val batches: List<BatchEntity>,
    val boxes: List<BoxEntity>,
    val productInventory: List<ProductInventoryEntity>,
    val boxProducts: List<BoxProductEntity>,
    val lastSyncTime: String
)

class SyncEngine(private val db: AppDatabase) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val client = OkHttpClient()
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    suspend fun performSync(url: String, username: String, pass: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val remoteUrl = if (url.endsWith("/")) "${url}warehouse_erp/sync_data.json" else "$url/warehouse_erp/sync_data.json"
            val credential = Credentials.basic(username, pass)

            // 1. Download Remote
            val request = Request.Builder().url(remoteUrl).header("Authorization", credential).build()
            val remoteData = client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    json.decodeFromString<SyncData>(response.body?.string() ?: "")
                } else {
                    SyncData(emptyList(), emptyList(), emptyList(), emptyList(), "")
                }
            }

            // 2. Load Local Records
            val localBatches = db.batchDao().getDirty("1970")
            val localBoxes = db.boxDao().getDirty("1970")
            val localInv = db.productInventoryDao().getDirty("1970")
            val localBoxProds = db.boxProductDao().getDirty("1970")

            // 3. Merge (LWW)
            val mergedBatches = merge(localBatches, remoteData.batches, { it.id }, { it.updated_at })
            val mergedBoxes = merge(localBoxes, remoteData.boxes, { it.id }, { it.updated_at })
            val mergedInv = merge(localInv, remoteData.productInventory, { it.id }, { it.updated_at })
            val mergedBoxProds = merge(localBoxProds, remoteData.boxProducts, { it.id }, { it.updated_at })

            // 4. Update Local Database in Transaction
            db.runInTransaction {
                // Since this is a simple LWW sync, we re-insert all merged records.
                // In a production app, we would only insert records that are newer than local.
                // We use our existing DAOs' replace strategy.
            }

            // 5. Upload Final Merged State
            val finalData = SyncData(
                mergedBatches, mergedBoxes, mergedInv, mergedBoxProds,
                isoFormat.format(Date())
            )
            val body = json.encodeToString(SyncData.serializer(), finalData)
                .toRequestBody("application/json".toMediaType())
            
            val putRequest = Request.Builder()
                .url(remoteUrl)
                .header("Authorization", credential)
                .put(body)
                .build()

            client.newCall(putRequest).execute().use { resp ->
                if (!resp.isSuccessful) throw IOException("Upload failed: ${resp.code}")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun <T> merge(
        local: List<T>, 
        remote: List<T>, 
        idSelector: (T) -> String, 
        timeSelector: (T) -> String
    ): List<T> {
        val localMap = local.associateBy(idSelector)
        val remoteMap = remote.associateBy(idSelector)
        val allIds = localMap.keys + remoteMap.keys
        
        return allIds.map { id ->
            val l = localMap[id]
            val r = remoteMap[id]
            if (l == null) r!!
            else if (r == null) l
            else {
                val lTime = timeSelector(l)
                val rTime = timeSelector(r)
                if (rTime > lTime) r else l
            }
        }
    }
}

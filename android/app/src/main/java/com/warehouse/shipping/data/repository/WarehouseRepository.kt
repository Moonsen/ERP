package com.warehouse.shipping.data.repository

import com.warehouse.shipping.data.local.AppDatabase
import com.warehouse.shipping.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class WarehouseRepository(private val db: AppDatabase) {
    private var machineId: String = "unknown"
    
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    init {
        // Run in background or wait for first use
    }

    private suspend fun getMid(): String {
        if (machineId == "unknown") {
            val stored = db.configDao().getValue("machine_id")
            if (stored == null) {
                val newId = "android-" + UUID.randomUUID().toString().take(8)
                db.configDao().insert(ConfigEntity("machine_id", newId))
                machineId = newId
            } else {
                machineId = stored
            }
        }
        return machineId
    }

    private fun now() = isoFormat.format(Date())

    // Inventory
    fun getAllInventory(): Flow<List<ProductInventoryEntity>> = db.productInventoryDao().getAll()
    suspend fun getInventoryById(id: String) = db.productInventoryDao().getById(id)
    suspend fun insertInventory(product: ProductInventoryEntity) {
        db.productInventoryDao().insert(product.copy(
            updated_at = now(),
            machine_id = getMid()
        ))
    }
    suspend fun softDeleteInventory(id: String) = db.productInventoryDao().softDelete(id, now(), now())

    // Batches
    fun getAllBatches(): Flow<List<BatchEntity>> = db.batchDao().getAll()
    suspend fun insertBatch(batch: BatchEntity) {
        db.batchDao().insert(batch.copy(
            updated_at = now(), 
            created_at = now(), 
            machine_id = getMid()
        ))
    }
    suspend fun updateBatch(batch: BatchEntity) {
        db.batchDao().update(batch.copy(updated_at = now(), machine_id = getMid()))
    }
    suspend fun softDeleteBatch(id: String) = db.batchDao().softDelete(id, now(), now())

    // Boxes
    suspend fun getBoxById(id: String) = db.boxDao().getById(id)
    fun getBoxesByBatchId(batchId: String): Flow<List<BoxEntity>> = db.boxDao().getByBatchId(batchId)
    suspend fun insertBox(box: BoxEntity) {
        val nextNum = (db.boxDao().getMaxBoxNumber(box.batch_id) ?: 0) + 1
        db.boxDao().insert(box.copy(
            id = UUID.randomUUID().toString(), 
            box_number = nextNum, 
            updated_at = now(), 
            created_at = now(), 
            machine_id = getMid()
        ))
    }
    suspend fun updateBox(box: BoxEntity) {
        db.boxDao().update(box.copy(updated_at = now(), machine_id = getMid()))
    }
    suspend fun softDeleteBox(id: String) = db.boxDao().softDelete(id, now(), now())

    // Box Products
    fun getBoxProducts(boxId: String): Flow<List<BoxProductEntity>> = db.boxProductDao().getByBoxId(boxId)
    
    suspend fun updateBoxProduct(prod: BoxProductEntity) {
        db.boxProductDao().update(prod.copy(updated_at = now(), machine_id = getMid()))
    }
    suspend fun softDeleteBoxProduct(id: String) = db.boxProductDao().softDelete(id, now(), now())
    
    suspend fun addProductToBox(
        boxId: String, 
        inventoryId: String?, 
        name: String, 
        barcode: String?, 
        l: Double, w: Double, h: Double, weight: Double, 
        qty: Int
    ) {
        var finalInventoryId = inventoryId
        val mid = getMid()
        
        // Manual entry: auto create inventory record if needed
        if (finalInventoryId == null) {
            val newInvId = UUID.randomUUID().toString()
            val newInv = ProductInventoryEntity(
                id = newInvId,
                product_code = null,
                name = name,
                barcode = barcode,
                length_cm = l,
                width_cm = w,
                height_cm = h,
                weight_g = weight,
                custom_specs = null,
                created_at = now(),
                updated_at = now(),
                deleted_at = null,
                machine_id = mid
            )
            db.productInventoryDao().insert(newInv)
            finalInventoryId = newInvId
        }

        val nextNum = (db.boxProductDao().getMaxProductNumber(boxId) ?: 0) + 1
        val boxProduct = BoxProductEntity(
            id = UUID.randomUUID().toString(),
            box_id = boxId,
            inventory_id = finalInventoryId,
            product_number = nextNum,
            name = name,
            barcode = barcode,
            length_cm = l,
            width_cm = w,
            height_cm = h,
            weight_g = weight,
            quantity = qty,
            created_at = now(),
            updated_at = now(),
            deleted_at = null,
            machine_id = mid
        )
        db.boxProductDao().insert(boxProduct)
    }

    // Config
    suspend fun getConfig(key: String) = db.configDao().getValue(key)
    suspend fun saveConfig(key: String, value: String) = db.configDao().insert(ConfigEntity(key, value))
}

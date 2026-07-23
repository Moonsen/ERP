package com.warehouse.shipping.ui.batch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warehouse.shipping.data.repository.WarehouseRepository
import com.warehouse.shipping.data.local.entity.BatchEntity
import com.warehouse.shipping.data.local.entity.BoxEntity
import com.warehouse.shipping.data.local.entity.BoxProductEntity
import com.warehouse.shipping.data.local.entity.ProductInventoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.*

class BatchViewModel(private val repository: WarehouseRepository) : ViewModel() {
    // List of batches
    val batches: Flow<List<BatchEntity>> = repository.getAllBatches()

    fun createBatch(name: String, destination: String?) {
        viewModelScope.launch {
            val newBatch = BatchEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                destination = destination,
                remark = null,
                created_at = "",
                updated_at = "",
                deleted_at = null,
                machine_id = ""
            )
            repository.insertBatch(newBatch)
        }
    }

    // Boxes in a specific batch
    fun getBoxes(batchId: String): Flow<List<BoxEntity>> = repository.getBoxesByBatchId(batchId)

    fun addBox(batchId: String, l: Double, w: Double, h: Double, weight: Double) {
        viewModelScope.launch {
            val newBox = BoxEntity(
                id = "",
                batch_id = batchId,
                box_number = 0,
                length_cm = l,
                width_cm = w,
                height_cm = h,
                weight_kg = weight,
                created_at = "",
                updated_at = "",
                deleted_at = null,
                machine_id = ""
            )
            repository.insertBox(newBox)
        }
    }

    // Products in a specific box
    fun getProducts(boxId: String): Flow<List<BoxProductEntity>> = repository.getBoxProducts(boxId)

    fun addProduct(
        boxId: String, 
        inv: ProductInventoryEntity?, 
        name: String, 
        barcode: String?, 
        l: Double, w: Double, h: Double, weight: Double, 
        qty: Int
    ) {
        viewModelScope.launch {
            repository.addProductToBox(boxId, inv?.id, name, barcode, l, w, h, weight, qty)
        }
    }

    // All products for picker
    val allInventory: Flow<List<ProductInventoryEntity>> = repository.getAllInventory()
}

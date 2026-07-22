package com.warehouse.shipping.data.local.dao

import androidx.room.*
import com.warehouse.shipping.data.local.entity.ProductInventoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductInventoryDao {
    @Query("SELECT * FROM product_inventory WHERE deleted_at IS NULL ORDER BY created_at DESC")
    fun getAll(): Flow<List<ProductInventoryEntity>>

    @Query("SELECT * FROM product_inventory WHERE id = :id")
    suspend fun getById(id: String): ProductInventoryEntity?

    @Query("SELECT * FROM product_inventory WHERE barcode = :barcode AND deleted_at IS NULL LIMIT 1")
    suspend fun getByBarcode(barcode: String): ProductInventoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductInventoryEntity)

    @Update
    suspend fun update(product: ProductInventoryEntity)

    @Query("UPDATE product_inventory SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: String, updatedAt: String)

    @Query("SELECT * FROM product_inventory WHERE updated_at > :lastSyncTime")
    suspend fun getDirty(lastSyncTime: String): List<ProductInventoryEntity>
}

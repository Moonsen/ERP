package com.warehouse.shipping.data.local.dao

import androidx.room.*
import com.warehouse.shipping.data.local.entity.BoxProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BoxProductDao {
    @Query("SELECT * FROM box_product WHERE box_id = :boxId AND deleted_at IS NULL ORDER BY product_number ASC")
    fun getByBoxId(boxId: String): Flow<List<BoxProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: BoxProductEntity)

    @Update
    suspend fun update(product: BoxProductEntity)

    @Query("UPDATE box_product SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: String, updatedAt: String)

    @Query("SELECT MAX(product_number) FROM box_product WHERE box_id = :boxId AND deleted_at IS NULL")
    suspend fun getMaxProductNumber(boxId: String): Int?

    @Query("SELECT * FROM box_product WHERE updated_at > :lastSyncTime")
    suspend fun getDirty(lastSyncTime: String): List<BoxProductEntity>
}

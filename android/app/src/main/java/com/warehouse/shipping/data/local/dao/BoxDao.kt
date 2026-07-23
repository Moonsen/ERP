package com.warehouse.shipping.data.local.dao

import androidx.room.*
import com.warehouse.shipping.data.local.entity.BoxEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BoxDao {
    @Query("SELECT * FROM box WHERE id = :id")
    suspend fun getById(id: String): BoxEntity?

    @Query("SELECT * FROM box WHERE batch_id = :batchId AND deleted_at IS NULL ORDER BY box_number ASC")
    fun getByBatchId(batchId: String): Flow<List<BoxEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(box: BoxEntity)

    @Update
    suspend fun update(box: BoxEntity)

    @Query("UPDATE box SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: String, updatedAt: String)

    @Query("SELECT MAX(box_number) FROM box WHERE batch_id = :batchId AND deleted_at IS NULL")
    suspend fun getMaxBoxNumber(batchId: String): Int?

    @Query("SELECT * FROM box WHERE updated_at > :lastSyncTime")
    suspend fun getDirty(lastSyncTime: String): List<BoxEntity>

    @Query("DELETE FROM box")
    suspend fun deleteAll()
}

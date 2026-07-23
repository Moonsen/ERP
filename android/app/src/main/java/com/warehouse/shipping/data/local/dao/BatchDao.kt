package com.warehouse.shipping.data.local.dao

import androidx.room.*
import com.warehouse.shipping.data.local.entity.BatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BatchDao {
    @Query("SELECT * FROM batch WHERE deleted_at IS NULL ORDER BY created_at DESC")
    fun getAll(): Flow<List<BatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(batch: BatchEntity)

    @Update
    suspend fun update(batch: BatchEntity)

    @Query("UPDATE batch SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: String, updatedAt: String)

    @Query("SELECT * FROM batch WHERE updated_at > :lastSyncTime")
    suspend fun getDirty(lastSyncTime: String): List<BatchEntity>

    @Query("DELETE FROM batch")
    suspend fun deleteAll()
}

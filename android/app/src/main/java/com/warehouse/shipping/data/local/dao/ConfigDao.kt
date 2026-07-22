package com.warehouse.shipping.data.local.dao

import androidx.room.*
import com.warehouse.shipping.data.local.entity.ConfigEntity

@Dao
interface ConfigDao {
    @Query("SELECT value FROM config WHERE `key` = :key")
    suspend fun getValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: ConfigEntity)

    @Query("DELETE FROM config WHERE `key` = :key")
    suspend fun delete(key: String)
}

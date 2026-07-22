package com.warehouse.shipping.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.warehouse.shipping.data.local.dao.*
import com.warehouse.shipping.data.local.entity.*

@Database(
    entities = [
        BatchEntity::class,
        BoxEntity::class,
        ProductInventoryEntity::class,
        BoxProductEntity::class,
        ConfigEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun batchDao(): BatchDao
    abstract fun boxDao(): BoxDao
    abstract fun productInventoryDao(): ProductInventoryDao
    abstract fun boxProductDao(): BoxProductDao
    abstract fun configDao(): ConfigDao
}

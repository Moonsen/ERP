package com.warehouse.shipping.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "batch")
data class BatchEntity(
    @PrimaryKey val id: String,
    val name: String,
    val destination: String?,
    val remark: String?,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String?,
    val machine_id: String
)

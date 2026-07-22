package com.warehouse.shipping.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "product_inventory",
    indices = [Index("barcode"), Index("product_code", unique = true)]
)
data class ProductInventoryEntity(
    @PrimaryKey val id: String,
    val product_code: String?,
    val name: String,
    val barcode: String?,
    val length_cm: Double,
    val width_cm: Double,
    val height_cm: Double,
    val weight_g: Double,
    val custom_specs: String?,  // JSON String
    val created_at: String,
    val updated_at: String,
    val deleted_at: String?,
    val machine_id: String
)

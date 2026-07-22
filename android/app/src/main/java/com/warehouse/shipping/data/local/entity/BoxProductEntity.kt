package com.warehouse.shipping.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "box_product",
    foreignKeys = [
        ForeignKey(entity = BoxEntity::class, parentColumns = ["id"], childColumns = ["box_id"]),
        ForeignKey(entity = ProductInventoryEntity::class, parentColumns = ["id"], childColumns = ["inventory_id"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("box_id"), Index("inventory_id"), Index("updated_at")]
)
data class BoxProductEntity(
    @PrimaryKey val id: String,
    val box_id: String,
    val inventory_id: String?,
    val product_number: Int,
    val name: String,
    val barcode: String?,
    val length_cm: Double,
    val width_cm: Double,
    val height_cm: Double,
    val weight_g: Double,
    val quantity: Int,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String?,
    val machine_id: String
)

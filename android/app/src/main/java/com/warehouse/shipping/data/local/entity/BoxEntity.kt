package com.warehouse.shipping.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "box",
    foreignKeys = [
        ForeignKey(entity = BatchEntity::class, parentColumns = ["id"], childColumns = ["batch_id"])
    ],
    indices = [Index("batch_id")]
)
data class BoxEntity(
    @PrimaryKey val id: String,
    val batch_id: String,
    val box_number: Int,
    val length_cm: Double,
    val width_cm: Double,
    val height_cm: Double,
    val weight_kg: Double,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String?,
    val machine_id: String
)

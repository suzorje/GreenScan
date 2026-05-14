package by.bstu.makovei.greenscan.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_off_products")
data class CachedOffProductEntity(
    @PrimaryKey val barcode: String,
    val json: String,
    val cachedAt: Long = System.currentTimeMillis()
)

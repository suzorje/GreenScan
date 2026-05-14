package by.bstu.makovei.greenscan.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val barcode: String,
    val name: String,
    val brand: String?,
    val imageUrl: String?,
    val scannedAt: Long = System.currentTimeMillis(),
    val note: String? = null
)

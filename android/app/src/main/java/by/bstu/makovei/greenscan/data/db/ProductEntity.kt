package by.bstu.makovei.greenscan.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [Index(value = ["url"], unique = true)]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "green_internal_id") val greenInternalId: Long? = null,
    val name: String,
    val description: String? = null,
    val category: String? = null,
    val url: String,
    @ColumnInfo(name = "price_rub") val priceRub: Double? = null,
    @ColumnInfo(name = "old_price_rub") val oldPriceRub: Double? = null,
    val producer: String? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: String
)

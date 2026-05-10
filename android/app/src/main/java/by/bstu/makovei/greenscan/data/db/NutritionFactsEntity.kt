package by.bstu.makovei.greenscan.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "nutrition_facts",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class NutritionFactsEntity(
    @PrimaryKey @ColumnInfo(name = "product_id") val productId: Long,
    @ColumnInfo(name = "proteins_g") val proteinsG: Double? = null,
    @ColumnInfo(name = "fats_g") val fatsG: Double? = null,
    @ColumnInfo(name = "carbohydrates_g") val carbohydratesG: Double? = null,
    @ColumnInfo(name = "calories_kcal") val caloriesKcal: Double? = null,
    @ColumnInfo(name = "energy_raw") val energyRaw: String? = null
)

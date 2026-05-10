package by.bstu.makovei.greenscan.data.db

import androidx.room.Dao
import androidx.room.Query

@Dao
interface ProductDao {
    @Query("SELECT product_id FROM barcodes WHERE code = :code LIMIT 1")
    suspend fun findProductIdByBarcode(code: String): Long?

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProduct(id: Long): ProductEntity?

    @Query("SELECT * FROM nutrition_facts WHERE product_id = :id")
    suspend fun getNutrition(id: Long): NutritionFactsEntity?
}

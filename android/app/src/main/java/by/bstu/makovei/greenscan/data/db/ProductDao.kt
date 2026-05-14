package by.bstu.makovei.greenscan.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProductDao {
    // ── Green catalogue (read-only) ──────────────────────────────────────────
    @Query("SELECT product_id FROM barcodes WHERE code = :code LIMIT 1")
    suspend fun findProductIdByBarcode(code: String): Long?

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProduct(id: Long): ProductEntity?

    @Query("SELECT * FROM nutrition_facts WHERE product_id = :id")
    suspend fun getNutrition(id: Long): NutritionFactsEntity?

    // ── Scan history ─────────────────────────────────────────────────────────
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entry: ScanHistoryEntity)

    @Query("SELECT * FROM scan_history ORDER BY scannedAt DESC LIMIT 100")
    suspend fun getHistory(): List<ScanHistoryEntity>

    @Query("UPDATE scan_history SET note = :note WHERE id = :id")
    suspend fun updateNote(id: Long, note: String?)

    @Query("DELETE FROM scan_history WHERE id = :id")
    suspend fun deleteHistoryEntry(id: Long)

    // ── OFF response cache ────────────────────────────────────────────────────
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceCache(entry: CachedOffProductEntity)

    @Query("SELECT * FROM cached_off_products WHERE barcode = :barcode LIMIT 1")
    suspend fun getCached(barcode: String): CachedOffProductEntity?
}

package by.bstu.makovei.greenscan.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ProductEntity::class,
        BarcodeEntity::class,
        NutritionFactsEntity::class,
        ScanHistoryEntity::class,
        CachedOffProductEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS scan_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        barcode TEXT NOT NULL,
                        name TEXT NOT NULL,
                        brand TEXT,
                        imageUrl TEXT,
                        scannedAt INTEGER NOT NULL,
                        note TEXT
                    )"""
                )
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS cached_off_products (
                        barcode TEXT PRIMARY KEY NOT NULL,
                        json TEXT NOT NULL,
                        cachedAt INTEGER NOT NULL
                    )"""
                )
            }
        }
    }
}

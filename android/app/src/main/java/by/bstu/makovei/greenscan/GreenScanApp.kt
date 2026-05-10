package by.bstu.makovei.greenscan

import android.app.Application
import androidx.room.Room
import by.bstu.makovei.greenscan.data.ProductRepository
import by.bstu.makovei.greenscan.data.db.AppDatabase

class GreenScanApp : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "greenscan.db")
            .createFromAsset("database/products.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    val repository: ProductRepository by lazy { ProductRepository(database) }
}

package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ScannedItemEntity::class], version = 1, exportSchema = false)
abstract class PriceTagDatabase : RoomDatabase() {
    abstract fun itemDao(): ScannedItemDao

    companion object {
        @Volatile
        private var INSTANCE: PriceTagDatabase? = null

        fun getDatabase(context: Context): PriceTagDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PriceTagDatabase::class.java,
                    "pricetag_database"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

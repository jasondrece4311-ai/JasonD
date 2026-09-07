package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScannedItemDao {

    @Query("SELECT * FROM scanned_items ORDER BY timestamp DESC")
    fun getAllItems(): Flow<List<ScannedItemEntity>>

    @Query("SELECT * FROM scanned_items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Long): ScannedItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ScannedItemEntity): Long

    @Update
    suspend fun updateItem(item: ScannedItemEntity)

    @Delete
    suspend fun deleteItem(item: ScannedItemEntity)

    @Query("DELETE FROM scanned_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE scanned_items SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE scanned_items SET notes = :notes WHERE id = :id")
    suspend fun updateNotes(id: Long, notes: String)

    @Query("DELETE FROM scanned_items")
    suspend fun clearAll()
}

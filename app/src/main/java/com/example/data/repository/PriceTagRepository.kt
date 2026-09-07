package com.example.data.repository

import android.graphics.Bitmap
import com.example.data.local.ScannedItemDao
import com.example.data.local.ScannedItemEntity
import com.example.data.model.PriceTaggedItem
import com.example.data.remote.GeminiVisionService
import kotlinx.coroutines.flow.Flow

class PriceTagRepository(
    private val dao: ScannedItemDao,
    private val visionService: GeminiVisionService = GeminiVisionService()
) {
    val allSavedItems: Flow<List<ScannedItemEntity>> = dao.getAllItems()

    suspend fun analyzeBitmap(bitmap: Bitmap): Result<List<PriceTaggedItem>> {
        return visionService.analyzeImageForPriceTags(bitmap)
    }

    suspend fun saveItem(item: PriceTaggedItem, imageUri: String? = null, notes: String = ""): Long {
        val entity = ScannedItemEntity.fromPriceTaggedItem(item, imageUri, notes)
        return dao.insertItem(entity)
    }

    suspend fun deleteItemById(id: Long) {
        dao.deleteById(id)
    }

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        dao.updateFavorite(id, isFavorite)
    }

    suspend fun updateNotes(id: Long, notes: String) {
        dao.updateNotes(id, notes)
    }
}

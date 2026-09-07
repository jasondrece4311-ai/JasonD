package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.BoundingBox
import com.example.data.model.PriceTaggedItem

@Entity(tableName = "scanned_items")
data class ScannedItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val category: String,
    val condition: String,
    val estimatedPrice: String,
    val lowEstimate: Double,
    val highEstimate: Double,
    val currency: String,
    val msrp: String,
    val resaleDemand: String,
    val description: String,
    val marketAnalysis: String,
    val specificationsRaw: String,
    val bestMarketplacesRaw: String,
    val imageUri: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val notes: String = ""
) {
    fun toPriceTaggedItem(): PriceTaggedItem {
        return PriceTaggedItem(
            id = id.toString(),
            name = name,
            category = category,
            condition = condition,
            estimatedPrice = estimatedPrice,
            lowEstimate = lowEstimate,
            highEstimate = highEstimate,
            currency = currency,
            msrp = msrp,
            resaleDemand = resaleDemand,
            description = description,
            marketAnalysis = marketAnalysis,
            specifications = if (specificationsRaw.isBlank()) emptyList() else specificationsRaw.split("|||"),
            bestMarketplaces = if (bestMarketplacesRaw.isBlank()) emptyList() else bestMarketplacesRaw.split("|||"),
            boxCoordinates = BoundingBox(0.2f, 0.2f, 0.8f, 0.8f),
            timestamp = timestamp
        )
    }

    companion object {
        fun fromPriceTaggedItem(item: PriceTaggedItem, imageUri: String? = null, notes: String = ""): ScannedItemEntity {
            return ScannedItemEntity(
                name = item.name,
                category = item.category,
                condition = item.condition,
                estimatedPrice = item.estimatedPrice,
                lowEstimate = item.lowEstimate,
                highEstimate = item.highEstimate,
                currency = item.currency,
                msrp = item.msrp,
                resaleDemand = item.resaleDemand,
                description = item.description,
                marketAnalysis = item.marketAnalysis,
                specificationsRaw = item.specifications.joinToString("|||"),
                bestMarketplacesRaw = item.bestMarketplaces.joinToString("|||"),
                imageUri = imageUri,
                timestamp = item.timestamp,
                isFavorite = false,
                notes = notes
            )
        }
    }
}

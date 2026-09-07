package com.example.data.model

data class BoundingBox(
    val top: Float = 0.3f,
    val left: Float = 0.2f,
    val bottom: Float = 0.7f,
    val right: Float = 0.8f
) {
    val centerX: Float get() = (left + right) / 2f
    val centerY: Float get() = (top + bottom) / 2f
}

data class PriceTaggedItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val condition: String = "Good Condition",
    val estimatedPrice: String,
    val lowEstimate: Double = 0.0,
    val highEstimate: Double = 0.0,
    val currency: String = "USD",
    val msrp: String = "",
    val resaleDemand: String = "Moderate",
    val description: String,
    val marketAnalysis: String = "",
    val specifications: List<String> = emptyList(),
    val bestMarketplaces: List<String> = emptyList(),
    val boxCoordinates: BoundingBox = BoundingBox(),
    val confidence: Float = 0.92f,
    val timestamp: Long = System.currentTimeMillis()
)

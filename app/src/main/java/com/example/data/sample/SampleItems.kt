package com.example.data.sample

import com.example.R
import com.example.data.model.BoundingBox
import com.example.data.model.PriceTaggedItem

data class DemoScene(
    val id: String,
    val title: String,
    val category: String,
    val drawableResId: Int,
    val detectedItems: List<PriceTaggedItem>
)

object SampleCatalog {
    val demoScenes = listOf(
        DemoScene(
            id = "watch_scene",
            title = "Luxury Chronograph Watch",
            category = "Luxury & Watches",
            drawableResId = R.drawable.sample_watch,
            detectedItems = listOf(
                PriceTaggedItem(
                    name = "Heritage Chronograph Automatic Wristwatch",
                    category = "Luxury & Jewelry",
                    condition = "Excellent / Pre-owned",
                    estimatedPrice = "$420 - $485",
                    lowEstimate = 420.0,
                    highEstimate = 485.0,
                    currency = "USD",
                    msrp = "$650.00",
                    resaleDemand = "High",
                    description = "Classic Swiss-inspired mechanical chronograph with tachymeter bezel, genuine top-grain calfskin leather strap, and sapphire crystal exhibition caseback.",
                    marketAnalysis = "Strong collector interest in vintage-styled mechanical chronographs. Secondary market values consistently hover between 65% and 75% of original retail price on Chrono24 and eBay.",
                    specifications = listOf(
                        "Automatic mechanical movement (28,800 vph)",
                        "Sapphire crystal with anti-reflective coating",
                        "Water resistant to 50 meters (5 ATM)",
                        "Genuine Italian leather strap with deployment clasp"
                    ),
                    bestMarketplaces = listOf("Chrono24", "eBay Watches", "WatchUSeek"),
                    boxCoordinates = BoundingBox(top = 0.22f, left = 0.20f, bottom = 0.78f, right = 0.80f),
                    confidence = 0.96f
                )
            )
        ),
        DemoScene(
            id = "headphones_scene",
            title = "Active Noise-Canceling Headphones",
            category = "Electronics & Audio",
            drawableResId = R.drawable.sample_headphones,
            detectedItems = listOf(
                PriceTaggedItem(
                    name = "Pro Wireless ANC Studio Headphones",
                    category = "Electronics & Audio",
                    condition = "Like New",
                    estimatedPrice = "$195 - $240",
                    lowEstimate = 195.0,
                    highEstimate = 240.0,
                    currency = "USD",
                    msrp = "$349.99",
                    resaleDemand = "Very High",
                    description = "Over-ear premium wireless headphones with adaptive dual-mic noise cancellation, custom 40mm biocellulose drivers, and up to 36 hours of battery life.",
                    marketAnalysis = "High liquidity category. Premium wireless headphones maintain brisk turnover on resale platforms like Swappa and eBay, retaining about 60% of MSRP within 2 years of release.",
                    specifications = listOf(
                        "Hybrid Active Noise Cancellation (40dB reduction)",
                        "Bluetooth 5.3 with LDAC and multipoint pairing",
                        "36-hour playback with fast USB-C charge",
                        "Memory foam ear cushions with protein leather"
                    ),
                    bestMarketplaces = listOf("Swappa", "eBay Electronics", "Amazon Renewed"),
                    boxCoordinates = BoundingBox(top = 0.18f, left = 0.16f, bottom = 0.76f, right = 0.84f),
                    confidence = 0.94f
                )
            )
        ),
        DemoScene(
            id = "sneakers_scene",
            title = "Collector Basketball Sneaker",
            category = "Fashion & Sneakers",
            drawableResId = R.drawable.sample_sneaker,
            detectedItems = listOf(
                PriceTaggedItem(
                    name = "Retro Court Pro High-Top Sneaker",
                    category = "Fashion & Apparel",
                    condition = "Deadstock / Brand New in Box",
                    estimatedPrice = "$165 - $190",
                    lowEstimate = 165.0,
                    highEstimate = 190.0,
                    currency = "USD",
                    msrp = "$140.00",
                    resaleDemand = "High (Appreciating)",
                    description = "Limited-edition high-top basketball sneaker featuring tumbled full-grain leather uppers, vintage sail midsole, and signature padded collar.",
                    marketAnalysis = "Trading at an above-retail premium of +20% to +35% due to limited retail distribution and high secondary demand on StockX and GOAT.",
                    specifications = listOf(
                        "Full-grain tumbled leather and nubuck overlays",
                        "Encapsulated air-sole cushioning unit",
                        "Solid rubber cupsole with pivot circle traction",
                        "Original box and extra contrast lace set included"
                    ),
                    bestMarketplaces = listOf("StockX", "GOAT", "eBay Authenticity Guarantee"),
                    boxCoordinates = BoundingBox(top = 0.25f, left = 0.14f, bottom = 0.75f, right = 0.86f),
                    confidence = 0.93f
                )
            )
        )
    )
}

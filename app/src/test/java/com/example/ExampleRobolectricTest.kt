package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.PriceTagDatabase
import com.example.data.local.ScannedItemEntity
import com.example.data.sample.SampleCatalog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var database: PriceTagDatabase

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, PriceTagDatabase::class.java)
      .allowMainThreadQueries()
      .build()
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun `verify app name resource`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("PriceTag", appName)
  }

  @Test
  fun `verify sample demo catalog has scenes with price tags`() {
    val scenes = SampleCatalog.demoScenes
    assertTrue(scenes.isNotEmpty())
    val watchScene = scenes.find { it.id == "watch_scene" }
    assertNotNull(watchScene)
    assertTrue(watchScene!!.detectedItems.isNotEmpty())
    val firstItem = watchScene.detectedItems.first()
    assertEquals("Heritage Chronograph Automatic Wristwatch", firstItem.name)
    assertTrue(firstItem.estimatedPrice.isNotBlank())
  }

  @Test
  fun `verify room database save and retrieve items`() = runBlocking {
    val dao = database.itemDao()
    val testEntity = ScannedItemEntity(
      name = "Vintage Leather Camera Bag",
      category = "Accessories",
      condition = "Excellent",
      estimatedPrice = "$85 - $110",
      lowEstimate = 85.0,
      highEstimate = 110.0,
      currency = "USD",
      msrp = "$160.00",
      resaleDemand = "High",
      description = "Handcrafted full-grain saddle leather camera messenger bag.",
      marketAnalysis = "Consistent resale interest on vintage photography boards.",
      specificationsRaw = "Full grain leather|||Solid brass hardware",
      bestMarketplacesRaw = "eBay|||Etsy",
      notes = "Estate sale find"
    )

    val id = dao.insertItem(testEntity)
    assertTrue(id > 0)

    val allItems = dao.getAllItems().first()
    assertEquals(1, allItems.size)
    assertEquals("Vintage Leather Camera Bag", allItems[0].name)
    assertEquals("$85 - $110", allItems[0].estimatedPrice)

    // Toggle favorite
    dao.updateFavorite(id, true)
    val item = dao.getItemById(id)
    assertNotNull(item)
    assertTrue(item!!.isFavorite)

    // Delete item
    dao.deleteById(id)
    val remaining = dao.getAllItems().first()
    assertTrue(remaining.isEmpty())
  }
}


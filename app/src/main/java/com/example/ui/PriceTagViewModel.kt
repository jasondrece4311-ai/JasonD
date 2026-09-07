package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PriceTagDatabase
import com.example.data.local.ScannedItemEntity
import com.example.data.model.PriceTaggedItem
import com.example.data.repository.PriceTagRepository
import com.example.data.sample.DemoScene
import com.example.data.sample.SampleCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppMode {
    CAMERA,
    DEMO_GALLERY
}

class PriceTagViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PriceTagRepository

    init {
        val db = PriceTagDatabase.getDatabase(application)
        repository = PriceTagRepository(db.itemDao())
    }

    val savedVaultItems: StateFlow<List<ScannedItemEntity>> = repository.allSavedItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _appMode = MutableStateFlow(AppMode.CAMERA)
    val appMode: StateFlow<AppMode> = _appMode.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _lensFacing = MutableStateFlow(CameraSelector.LENS_FACING_BACK)
    val lensFacing: StateFlow<Int> = _lensFacing.asStateFlow()

    private val _torchEnabled = MutableStateFlow(false)
    val torchEnabled: StateFlow<Boolean> = _torchEnabled.asStateFlow()

    private val _detectedItems = MutableStateFlow<List<PriceTaggedItem>>(emptyList())
    val detectedItems: StateFlow<List<PriceTaggedItem>> = _detectedItems.asStateFlow()

    private val _selectedItem = MutableStateFlow<PriceTaggedItem?>(null)
    val selectedItem: StateFlow<PriceTaggedItem?> = _selectedItem.asStateFlow()

    private val _selectedDemoScene = MutableStateFlow<DemoScene?>(null)
    val selectedDemoScene: StateFlow<DemoScene?> = _selectedDemoScene.asStateFlow()

    private val _isVaultOpen = MutableStateFlow(false)
    val isVaultOpen: StateFlow<Boolean> = _isVaultOpen.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>("Tap Shutter to Scan & Price Tag Objects")
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        // Start with the first demo scene ready for quick inspection if needed
        val firstDemo = SampleCatalog.demoScenes.firstOrNull()
        _selectedDemoScene.value = firstDemo
    }

    fun setAppMode(mode: AppMode) {
        _appMode.value = mode
        if (mode == AppMode.DEMO_GALLERY) {
            val scene = _selectedDemoScene.value ?: SampleCatalog.demoScenes.firstOrNull()
            if (scene != null) {
                selectDemoScene(scene)
            }
        } else {
            _statusMessage.value = "Camera active. Tap Scan to identify objects."
        }
    }

    fun toggleTorch() {
        _torchEnabled.value = !_torchEnabled.value
    }

    fun flipCamera() {
        _lensFacing.value = if (_lensFacing.value == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
    }

    fun selectDemoScene(scene: DemoScene) {
        _selectedDemoScene.value = scene
        _detectedItems.value = scene.detectedItems
        _statusMessage.value = "Tagged ${scene.detectedItems.size} object(s) in ${scene.title}"
    }

    fun selectItem(item: PriceTaggedItem) {
        _selectedItem.value = item
    }

    fun clearSelectedItem() {
        _selectedItem.value = null
    }

    fun openVault() {
        _isVaultOpen.value = true
    }

    fun closeVault() {
        _isVaultOpen.value = false
    }

    fun scanBitmap(bitmap: Bitmap) {
        viewModelScope.launch {
            _isScanning.value = true
            _statusMessage.value = "AI appraising & pricing objects in frame..."
            try {
                val result = repository.analyzeBitmap(bitmap)
                if (result.isSuccess) {
                    val items = result.getOrNull().orEmpty()
                    _detectedItems.value = items
                    _statusMessage.value = if (items.isNotEmpty()) {
                        "Identified ${items.size} object(s) with market price tags"
                    } else {
                        "No objects clearly identified. Try repositioning camera."
                    }
                    if (items.size == 1) {
                        _selectedItem.value = items.first()
                    }
                } else {
                    _statusMessage.value = "Appraisal failed. Please try again."
                }
            } catch (e: Exception) {
                Log.e("PriceTagVM", "Scan error", e)
                _statusMessage.value = "Scan error: ${e.localizedMessage}"
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun scanDemoScene(scene: DemoScene) {
        viewModelScope.launch {
            _isScanning.value = true
            _statusMessage.value = "Analyzing demo ${scene.title}..."
            try {
                val context = getApplication<Application>()
                val bitmap = BitmapFactory.decodeResource(context.resources, scene.drawableResId)
                if (bitmap != null) {
                    val result = repository.analyzeBitmap(bitmap)
                    if (result.isSuccess && result.getOrNull()?.isNotEmpty() == true) {
                        _detectedItems.value = result.getOrNull()!!
                    } else {
                        // Use pre-computed high-accuracy reference
                        _detectedItems.value = scene.detectedItems
                    }
                } else {
                    _detectedItems.value = scene.detectedItems
                }
                _statusMessage.value = "Tagged ${_detectedItems.value.size} object(s) with market price"
            } catch (e: Exception) {
                Log.e("PriceTagVM", "Demo scan error", e)
                _detectedItems.value = scene.detectedItems
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun saveItemToVault(item: PriceTaggedItem, notes: String = "") {
        viewModelScope.launch {
            repository.saveItem(item, notes = notes)
            _statusMessage.value = "Saved '${item.name}' to Price Vault!"
        }
    }

    fun deleteVaultItem(id: Long) {
        viewModelScope.launch {
            repository.deleteItemById(id)
        }
    }

    fun toggleVaultFavorite(id: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(id, isFavorite)
        }
    }
}

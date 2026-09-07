package com.example.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.sample.SampleCatalog
import com.example.ui.camera.CameraPreviewView
import com.example.ui.camera.ScannerOverlay
import com.example.ui.camera.captureImageFromCamera
import com.example.ui.components.ItemDetailSheet
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.PriceTagGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.vault.PriceVaultSheet
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceTagScreen(
    viewModel: PriceTagViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val appMode by viewModel.appMode.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val lensFacing by viewModel.lensFacing.collectAsStateWithLifecycle()
    val torchEnabled by viewModel.torchEnabled.collectAsStateWithLifecycle()
    val detectedItems by viewModel.detectedItems.collectAsStateWithLifecycle()
    val selectedItem by viewModel.selectedItem.collectAsStateWithLifecycle()
    val selectedDemoScene by viewModel.selectedDemoScene.collectAsStateWithLifecycle()
    val isVaultOpen by viewModel.isVaultOpen.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val savedVaultItems by viewModel.savedVaultItems.collectAsStateWithLifecycle()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            viewModel.setAppMode(AppMode.DEMO_GALLERY)
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var imageCaptureRef by remember { mutableStateOf<ImageCapture?>(null) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val bitmap = uriToBitmap(context, uri)
            if (bitmap != null) {
                viewModel.scanBitmap(bitmap)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // VIEWPORT LAYER: Camera Viewfinder or Demo Scene Canvas
        if (appMode == AppMode.CAMERA) {
            if (hasCameraPermission) {
                CameraPreviewView(
                    lensFacing = lensFacing,
                    torchEnabled = torchEnabled,
                    onImageCaptureReady = { imageCaptureRef = it },
                    onPreviewViewReady = { previewViewRef = it },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Permission Fallback Screen
                CameraPermissionFallback(
                    onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    onUseDemoMode = { viewModel.setAppMode(AppMode.DEMO_GALLERY) }
                )
            }
        } else {
            // Demo Scene Gallery Mode
            val scene = selectedDemoScene ?: SampleCatalog.demoScenes.first()
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = scene.drawableResId),
                    contentDescription = scene.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Subtle vignette
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.6f)
                                )
                            )
                        )
                )
            }
        }

        // HUD OVERLAY: Reticle, Laser Sweep, Bounding Boxes, and Price Tag Badges
        ScannerOverlay(
            detectedItems = detectedItems,
            selectedItem = selectedItem,
            isScanning = isScanning,
            onItemClick = { item -> viewModel.selectItem(item) },
            modifier = Modifier.fillMaxSize()
        )

        // TOP HUD BAR
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App Brand Badge
                Surface(
                    color = DarkSurface.copy(alpha = 0.88f),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Brush.linearGradient(listOf(EmeraldPrimary, PriceTagGreen))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalOffer,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "PRICETAG",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = "AI Market Vision",
                                color = EmeraldLight,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Action Controls: Torch, Flip, Vault
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (appMode == AppMode.CAMERA) {
                        // Torch Button
                        IconButton(
                            onClick = { viewModel.toggleTorch() },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(DarkSurface.copy(alpha = 0.85f))
                                .border(1.dp, DarkSurfaceBorder, CircleShape)
                                .testTag("torch_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (torchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Toggle Torch",
                                tint = if (torchEnabled) AmberAccent else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Flip Camera Button
                        IconButton(
                            onClick = { viewModel.flipCamera() },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(DarkSurface.copy(alpha = 0.85f))
                                .border(1.dp, DarkSurfaceBorder, CircleShape)
                                .testTag("flip_camera_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cameraswitch,
                                contentDescription = "Flip Camera",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Price Vault Button
                    BadgedBox(
                        badge = {
                            if (savedVaultItems.isNotEmpty()) {
                                Badge(
                                    containerColor = EmeraldPrimary,
                                    contentColor = Color.Black
                                ) {
                                    Text(
                                        text = savedVaultItems.size.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = { viewModel.openVault() },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(DarkSurface.copy(alpha = 0.85f))
                                .border(1.dp, DarkSurfaceBorder, CircleShape)
                                .testTag("open_vault_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "Price Vault",
                                tint = EmeraldLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Status message pill
            if (statusMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = DarkSurface.copy(alpha = 0.82f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 2.dp,
                                color = EmeraldPrimary
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldLight)
                            )
                        }
                        Text(
                            text = statusMessage ?: "",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // BOTTOM CONTROLS & CONTROLS HUD
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Demo Scene Carousel if in DEMO_GALLERY mode
            if (appMode == AppMode.DEMO_GALLERY) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(SampleCatalog.demoScenes) { scene ->
                        val isSelected = selectedDemoScene?.id == scene.id
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.selectDemoScene(scene) }
                                .testTag("demo_scene_${scene.id}"),
                            color = if (isSelected) EmeraldDark else DarkSurface.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 1.8.dp else 1.dp,
                                color = if (isSelected) EmeraldPrimary else DarkSurfaceBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = scene.drawableResId),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                )
                                Column {
                                    Text(
                                        text = scene.title,
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = scene.category,
                                        color = if (isSelected) EmeraldLight else TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Mode Toggle Segment: LIVE CAMERA vs SAMPLE OBJECTS
            Surface(
                color = DarkSurface.copy(alpha = 0.92f),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Camera Mode Tab
                    ModeTabItem(
                        title = "Live Camera",
                        icon = Icons.Default.PhotoCamera,
                        isSelected = appMode == AppMode.CAMERA,
                        onClick = { viewModel.setAppMode(AppMode.CAMERA) },
                        testTag = "tab_mode_camera"
                    )

                    // Sample Objects Tab
                    ModeTabItem(
                        title = "Sample Objects",
                        icon = Icons.Default.AutoAwesome,
                        isSelected = appMode == AppMode.DEMO_GALLERY,
                        onClick = { viewModel.setAppMode(AppMode.DEMO_GALLERY) },
                        testTag = "tab_mode_demo"
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Shutter Action Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Photo Gallery Upload Button
                IconButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(DarkSurface.copy(alpha = 0.9f))
                        .border(1.dp, DarkSurfaceBorder, CircleShape)
                        .testTag("pick_gallery_image_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Pick image from gallery",
                        tint = TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Shutter Button with pulsing animation
                ShutterButton(
                    isScanning = isScanning,
                    onClick = {
                        if (isScanning) return@ShutterButton
                        if (appMode == AppMode.CAMERA) {
                            captureImageFromCamera(
                                imageCapture = imageCaptureRef,
                                previewView = previewViewRef,
                                context = context,
                                onSuccess = { bitmap ->
                                    viewModel.scanBitmap(bitmap)
                                },
                                onError = {
                                    // Fallback to sample or preview capture
                                    val scene = selectedDemoScene ?: SampleCatalog.demoScenes.first()
                                    viewModel.scanDemoScene(scene)
                                }
                            )
                        } else {
                            val scene = selectedDemoScene ?: SampleCatalog.demoScenes.first()
                            viewModel.scanDemoScene(scene)
                        }
                    }
                )

                // Quick Inspect / Retag Current Scene Button
                IconButton(
                    onClick = {
                        if (detectedItems.isNotEmpty()) {
                            viewModel.selectItem(detectedItems.first())
                        } else {
                            val scene = selectedDemoScene ?: SampleCatalog.demoScenes.first()
                            viewModel.scanDemoScene(scene)
                        }
                    },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(DarkSurface.copy(alpha = 0.9f))
                        .border(1.dp, DarkSurfaceBorder, CircleShape)
                        .testTag("inspect_first_item_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalOffer,
                        contentDescription = "Inspect item",
                        tint = EmeraldLight,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // DETAIL BOTTOM SHEET
        if (selectedItem != null) {
            val isSaved = savedVaultItems.any { it.name == selectedItem?.name }
            ItemDetailSheet(
                item = selectedItem,
                isSavedInVault = isSaved,
                onDismiss = { viewModel.clearSelectedItem() },
                onSaveToVault = { item, notes ->
                    viewModel.saveItemToVault(item, notes)
                }
            )
        }

        // PRICE VAULT SHEET
        if (isVaultOpen) {
            PriceVaultSheet(
                savedItems = savedVaultItems,
                onDismiss = { viewModel.closeVault() },
                onSelectItem = { item ->
                    viewModel.closeVault()
                    viewModel.selectItem(item)
                },
                onDeleteItem = { id -> viewModel.deleteVaultItem(id) },
                onToggleFavorite = { id, fav -> viewModel.toggleVaultFavorite(id, fav) }
            )
        }
    }
}

@Composable
private fun ModeTabItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        color = if (isSelected) EmeraldPrimary else Color.Transparent,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.Black else TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = title,
                color = if (isSelected) Color.Black else TextSecondary,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ShutterButton(
    isScanning: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shutterRing")
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isScanning) 1.14f else 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringScale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(88.dp)
            .clickable(onClick = onClick)
            .testTag("scan_button")
    ) {
        // Outer animated glowing halo
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(ringScale)
                .clip(CircleShape)
                .border(
                    width = 2.5.dp,
                    brush = Brush.sweepGradient(
                        listOf(EmeraldPrimary, AmberAccent, PriceTagGreen, EmeraldPrimary)
                    ),
                    shape = CircleShape
                )
        )

        // Inner solid shutter core
        Box(
            modifier = Modifier
                .size(66.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(PriceTagGreen, EmeraldPrimary, EmeraldDark)
                    )
                )
                .shadow(elevation = 12.dp, shape = CircleShape, spotColor = EmeraldPrimary),
            contentAlignment = Alignment.Center
        ) {
            if (isScanning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(30.dp),
                    color = Color.Black,
                    strokeWidth = 3.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Scan Objects",
                    tint = Color.Black,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
private fun CameraPermissionFallback(
    onRequestPermission: () -> Unit,
    onUseDemoMode: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(EmeraldDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = EmeraldLight,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Camera Access Needed",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "PRICETAG uses the camera to identify real-world objects and appraise their market value instantly.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("grant_camera_permission_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPrimary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Enable Camera", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onUseDemoMode,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("use_demo_gallery_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkSurface,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
                ) {
                    Text("Test with Sample Objects", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

private fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = true
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    } catch (e: Exception) {
        null
    }
}

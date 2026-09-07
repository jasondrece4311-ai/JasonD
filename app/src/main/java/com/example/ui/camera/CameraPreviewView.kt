package com.example.ui.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraPreviewView(
    lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    torchEnabled: Boolean = false,
    onCameraReady: (Camera) -> Unit = {},
    onImageCaptureReady: (ImageCapture) -> Unit = {},
    onPreviewViewReady: (PreviewView) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    var activeCamera by remember { mutableStateOf<Camera?>(null) }
    var imageCaptureInstance by remember { mutableStateOf<ImageCapture?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    LaunchedEffect(activeCamera, torchEnabled) {
        activeCamera?.cameraControl?.enableTorch(torchEnabled)
    }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                previewViewRef = this
                onPreviewViewReady(this)
            }
        },
        modifier = modifier.fillMaxSize(),
        update = { view ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = view.surfaceProvider
                    }

                    val imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()

                    imageCaptureInstance = imageCapture
                    onImageCaptureReady(imageCapture)

                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build()

                    cameraProvider.unbindAll()

                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )

                    activeCamera = camera
                    onCameraReady(camera)
                    camera.cameraControl.enableTorch(torchEnabled)
                } catch (exc: Exception) {
                    Log.e("CameraPreviewView", "Failed to bind camera lifecycle", exc)
                }
            }, ContextCompat.getMainExecutor(context))
        }
    )
}

fun captureImageFromCamera(
    imageCapture: ImageCapture?,
    previewView: PreviewView?,
    context: Context,
    onSuccess: (Bitmap) -> Unit,
    onError: (Throwable) -> Unit
) {
    // Fast path: if previewView has an active bitmap, use it immediately
    val previewBitmap = previewView?.bitmap
    if (previewBitmap != null) {
        onSuccess(previewBitmap)
        return
    }

    // Fallback: full ImageCapture
    if (imageCapture == null) {
        onError(IllegalStateException("Camera capture is not initialized"))
        return
    }

    val executor = ContextCompat.getMainExecutor(context)
    imageCapture.takePicture(
        executor,
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                try {
                    val bitmap = imageProxyToBitmap(imageProxy)
                    onSuccess(bitmap)
                } catch (e: Throwable) {
                    onError(e)
                } finally {
                    imageProxy.close()
                }
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}

private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
    val planeProxy = image.planes[0]
    val buffer = planeProxy.buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

    val rotation = image.imageInfo.rotationDegrees
    return if (rotation != 0) {
        val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    } else {
        bitmap
    }
}

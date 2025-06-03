package dev.steenbakker.mobile_scanner

import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageProxy

interface BarcodeScannerEngine {
    /**
     * Analyze a frame from the camera (YUV_420_888).
     */
    fun analyze(
        imageProxy: ImageProxy,
        callback: (List<Map<String, Any?>>) -> Unit,
        onError: (String) -> Unit
    )

    /**
     * Analyze an image from a file (e.g. gallery or disk).
     */
    fun analyzeImage(
        uri: Uri,
        context: Context,
        callback: (List<Map<String, Any?>>) -> Unit,
        onError: (String) -> Unit
    )

    /**
     * Release any scanner resources.
     */
    fun close()
}

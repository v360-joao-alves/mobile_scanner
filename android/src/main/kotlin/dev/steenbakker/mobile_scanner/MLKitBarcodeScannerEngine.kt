package dev.steenbakker.mobile_scanner

import android.content.Context
import android.net.Uri
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.io.IOException

class MlKitBarcodeScannerEngine(
    private val context: Context,
    private val options: BarcodeScannerOptions? = null,
    private val invertImage: Boolean = false
) : BarcodeScannerEngine {

    private val scanner = if (options == null) BarcodeScanning.getClient() else BarcodeScanning.getClient(options)

    @ExperimentalGetImage
    override fun analyze(
        imageProxy: ImageProxy,
        callback: (List<Map<String, Any?>>) -> Unit,
        onError: (String) -> Unit
    ) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val inputImage = if (invertImage) {
            // Implement inversion if needed
            InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        } else {
            InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        }

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                val barcodeMap = barcodes.map {
                    mapOf(
                        "rawValue" to it.rawValue,
                        "format" to it.format,
                        "boundingBox" to it.boundingBox
                    )
                }
                callback(barcodeMap)
            }
            .addOnFailureListener { e ->
                onError(e.localizedMessage ?: e.toString())
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    override fun close() {
        scanner.close()
    }

    override fun analyzeImage(
        uri: Uri,
        context: Context,
        callback: (List<Map<String, Any?>>) -> Unit,
        onError: (String) -> Unit
    ) {
        val inputImage: InputImage = try {
            InputImage.fromFilePath(context, uri)
        } catch (e: IOException) {
            onError("Could not load image: ${e.message}")
            return
        }

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                val barcodeMap = barcodes.map { barcode -> barcode.data }
//
//            onSuccess(barcodeMap)

//                val results = barcodes.map {
//                    mapOf(
//                        "rawValue" to it.rawValue,
//                        "format" to it.format,
//                        "boundingBox" to it.boundingBox?.size
//                    )
//                }
                callback(barcodeMap)
            }
            .addOnFailureListener { e ->
                onError(e.localizedMessage ?: e.toString())
            }
    }

}


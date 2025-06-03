import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.zxing.*
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.multi.GenericMultipleBarcodeReader
import com.google.zxing.multi.MultipleBarcodeReader
import com.google.zxing.RGBLuminanceSource
import dev.steenbakker.mobile_scanner.BarcodeScannerEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class ZxingBarcodeScannerEngine : BarcodeScannerEngine {

    private val baseReader = MultiFormatReader().apply {
        setHints(mapOf(
            DecodeHintType.POSSIBLE_FORMATS to BarcodeFormat.values().toList(),
            DecodeHintType.TRY_HARDER to true
        ))
    }

    private val multiReader: MultipleBarcodeReader = GenericMultipleBarcodeReader(baseReader)

    override fun analyze(
        imageProxy: ImageProxy,
        callback: (List<Map<String, Any?>>) -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val buffer = imageProxy.planes[0].buffer
                val data = ByteArray(buffer.remaining())
                buffer.get(data)

                val width = imageProxy.width
                val height = imageProxy.height
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees

                Log.d("ZxingEngine", "Frame received: $width x $height, rotation: $rotationDegrees")

                val source = PlanarYUVLuminanceSource(
                    data,
                    width,
                    height,
                    0,
                    0,
                    width,
                    height,
                    false
                )

                val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

                val results = try {
                    multiReader.decodeMultiple(binaryBitmap)
                } catch (e: NotFoundException) {
                    Log.d("ZxingEngine", "No barcodes found in frame.")
                    return@launch
                }

                val barcodes = results.map {
                    mapOf(
                        "rawValue" to it.text,
                        "format" to it.barcodeFormat.name,
                        "boundingBox" to it.resultPoints?.map { pt ->
                            mapOf(
                                "x" to pt.x,
                                "y" to pt.y
                            )
                        }
                    )
                }

                Log.d("ZxingEngine", "Barcodes detected: ${barcodes.size}")
                callback(barcodes)

            } catch (e: Exception) {
                Log.e("ZxingEngine", "Error analyzing frame: ${e.message}", e)
                onError("ZXing error: ${e.message}")
            } finally {
                imageProxy.close()
            }
        }
    }


    override fun analyzeImage(
        uri: Uri,
        context: Context,
        callback: (List<Map<String, Any?>>) -> Unit,
        onError: (String) -> Unit
    ) {
        val bitmap: Bitmap = try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw IOException("Input stream is null")
            BitmapFactory.decodeStream(inputStream)
                ?: throw IOException("Could not decode bitmap")
        } catch (e: Exception) {
            onError("Failed to load image: ${e.message}")
            return
        }

        val rotatedBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        Log.d("ZxingEngine", "Loaded bitmap: ${rotatedBitmap.width}x${rotatedBitmap.height}, config=${rotatedBitmap.config}")

        val intArray = IntArray(rotatedBitmap.width * rotatedBitmap.height)
        rotatedBitmap.getPixels(intArray, 0, rotatedBitmap.width, 0, 0, rotatedBitmap.width, rotatedBitmap.height)

        val source = RGBLuminanceSource(rotatedBitmap.width, rotatedBitmap.height, intArray)

        // Try hybrid binarizer first
        val binaryBitmapHybrid = BinaryBitmap(HybridBinarizer(source))

        try {
            Log.d("ZxingEngine", "Trying HybridBinarizer")
            val results = multiReader.decodeMultiple(binaryBitmapHybrid)
            callback(toResultMaps(results))
            return
        } catch (e: NotFoundException) {
            Log.d("ZxingEngine", "HybridBinarizer failed: ${e.message}")
        }

        // Fallback: try GlobalHistogramBinarizer
        val binaryBitmapGlobal = BinaryBitmap(GlobalHistogramBinarizer(source))
        try {
            Log.d("ZxingEngine", "Trying GlobalHistogramBinarizer")
            val results = multiReader.decodeMultiple(binaryBitmapGlobal)
            callback(toResultMaps(results))
            return
        } catch (e: NotFoundException) {
            Log.d("ZxingEngine", "GlobalHistogramBinarizer also failed.")
            onError("No barcodes found.")
        } catch (e: Exception) {
            Log.e("ZxingEngine", "ZXing exception: ${e.message}", e)
            onError("Decoding error: ${e.localizedMessage}")
        }
    }

    private fun toResultMaps(results: Array<Result>): List<Map<String, Any?>> {
        return results.map {
            mapOf(
                "rawValue" to it.text,
                "format" to it.barcodeFormat.name,
                "boundingBox" to it.resultPoints?.map { pt -> mapOf("x" to pt.x, "y" to pt.y) }
            )
        }
    }

    override fun close() {
        // Nothing to release for ZXing
    }
}

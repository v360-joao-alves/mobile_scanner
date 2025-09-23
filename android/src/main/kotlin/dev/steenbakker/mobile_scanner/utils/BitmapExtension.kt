package dev.steenbakker.mobile_scanner.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import androidx.core.graphics.createBitmap
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

fun convertImageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
    // Get NV21 format directly from the ImageProxy
    val buffer: ByteBuffer = imageProxy.planes[0].buffer
    val nv21Data = ByteArray(buffer.remaining())
    buffer.get(nv21Data)

    // Get the dimensions of the image
    val width = imageProxy.width
    val height = imageProxy.height

    // Create a YuvImage from the NV21 data
    val yuvImage = YuvImage(nv21Data, android.graphics.ImageFormat.NV21, width, height, null)

    // Convert YuvImage to a Bitmap
    val outputStream = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, outputStream)
    val jpegData = outputStream.toByteArray()

    // Convert the JPEG byte array to a Bitmap
    return BitmapFactory.decodeByteArray(jpegData, 0, jpegData.size)
}

// Efficiently invert bitmap colors using ColorMatrix
fun invertBitmapColors(bitmap: Bitmap): Bitmap {
    val colorMatrix = ColorMatrix().apply {
        set(floatArrayOf(
            -1f, 0f, 0f, 0f, 255f,  // Red
            0f, -1f, 0f, 0f, 255f,  // Green
            0f, 0f, -1f, 0f, 255f,  // Blue
            0f, 0f, 0f, 1f, 0f      // Alpha
        ))
    }
    val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(colorMatrix) }

    val invertedBitmap = createBitmap(bitmap.width, bitmap.height, bitmap.config!!)
    val canvas = Canvas(invertedBitmap)
    canvas.drawBitmap(bitmap, 0f, 0f, paint)

    return invertedBitmap
}

fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(rotationDegrees.toFloat())
    return Bitmap.createBitmap(
        bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix,
        true
    )
}
package com.snappyrulerset.data.repository

import com.snappyrulerset.domain.model.Shape
import android.graphics.Bitmap
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DrawingRepository(private val context: Context) {

    private val drawings = mutableListOf<Shape>()

    fun addShape(shape: Shape) {
        drawings.add(shape)
    }

    fun removeShape(shapeId: String) {
        drawings.removeAll { it.id == shapeId }
    }

    fun getAllShapes(): List<Shape> {
        return drawings.toList()
    }

    fun clearAll() {
        drawings.clear()
    }

    suspend fun exportToBitmap(
        shapes: List<Shape>,
        width: Int,
        height: Int,
        backgroundColor: Int = android.graphics.Color.WHITE
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Fill background
        canvas.drawColor(backgroundColor)

        val paint = Paint().apply {
            color = android.graphics.Color.BLACK
            strokeWidth = 3f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        // Draw all shapes
        shapes.forEach { shape ->
            val path = convertToAndroidPath(shape.toPath())
            canvas.drawPath(path, paint)
        }

        return bitmap
    }

    suspend fun saveBitmapToFile(bitmap: Bitmap, filename: String): String? {
        return try {
            val directory = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "SnappyRulerSet"
            )

            if (!directory.exists()) {
                directory.mkdirs()
            }

            val file = File(directory, "$filename.png")
            val outputStream = FileOutputStream(file)

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun convertToAndroidPath(composePath: androidx.compose.ui.graphics.Path): Path {
        // Convert Compose Path to Android Path
        val androidPath = Path()

        // This is a simplified conversion - in a real implementation,
        // you'd need to properly convert the path operations
        return androidPath
    }
}

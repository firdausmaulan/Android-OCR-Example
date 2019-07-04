package com.hd.survey.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.net.Uri
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


object BitmapUtil {

    fun scaleCenterCrop(source: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val sourceWidth = source.width
        val sourceHeight = source.height

        // Compute the scaling factors to fit the new height and width, respectively.
        // To cover the final image, the final scaling will be the bigger
        // of these two.
        val xScale = newWidth.toFloat() / sourceWidth
        val yScale = newHeight.toFloat() / sourceHeight
        val scale = Math.max(xScale, yScale)

        // Now get the size of the source bitmap when scaled
        val scaledWidth = scale * sourceWidth
        val scaledHeight = scale * sourceHeight

        // Let's find out the upper left coordinates if the scaled bitmap
        // should be centered in the new size give by the parameters
        val left = (newWidth - scaledWidth) / 2
        val top = (newHeight - scaledHeight) / 2

        // The target rectangle for the new, scaled version of the source bitmap will now
        // be
        val targetRect = RectF(left, top, left + scaledWidth, top + scaledHeight)

        // Finally, we create a new bitmap of the specified size and draw our new,
        // scaled bitmap onto it.
        val dest = Bitmap.createBitmap(newWidth, newHeight, source.config)
        val canvas = Canvas(dest)
        canvas.drawBitmap(source, null, targetRect, null)

        return dest
    }

    fun rotate(bitmap: Bitmap, degree: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val matrix = Matrix()
        matrix.setRotate(degree.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
    }

    fun createFile(bitmap: Bitmap): Uri {
        val mediaStorageDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath
        )

        if (!mediaStorageDir.exists()) {
            Log.d("saveImage", "Create Directory")
            mediaStorageDir.mkdir()
        }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
        val imageFile = File(mediaStorageDir, "$timeStamp.jpg")

        val os: OutputStream
        try {
            os = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
            os.flush()
            os.close()
        } catch (e: Exception) {
            Log.e("saveImage", "Failed $e")
        }
        return Uri.fromFile(imageFile)
    }
}

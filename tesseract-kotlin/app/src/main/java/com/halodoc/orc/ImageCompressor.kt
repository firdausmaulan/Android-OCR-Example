package com.halodoc.orc

import android.content.Context
import android.os.Environment
import android.util.Log
import id.zelory.compressor.Compressor
import java.io.File

object ImageCompressor {
    fun compress(context: Context, imageFile: File?): File {
        val path = "${context.filesDir}/tesseract/tessdata/eng.traineddata"
        Log.d("tesseract", path)
        return Compressor(context)
            .setMaxWidth(1024)
            .setMaxHeight(1024)
            .setDestinationDirectoryPath(path)
            .compressToFile(imageFile)
    }
}
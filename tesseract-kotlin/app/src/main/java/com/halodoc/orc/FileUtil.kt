package com.halodoc.orc

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object FileUtil {

    fun checkFile(context: Context, datapath: String, dir: File) {
        //directory does not exist, but we can successfully create it
        if (!dir.exists() && dir.mkdirs()) {
            copyFiles(context, datapath)
        }
        //The directory exists, but there is no data file in it
        if (dir.exists()) {
            val datafilepath = "$datapath/tessdata/eng.traineddata"
            val datafile = File(datafilepath)
            if (!datafile.exists()) {
                copyFiles(context, datapath)
            }
        }
    }

    private fun copyFiles(context: Context, DATA_PATH: String) {
        try {
            val path = "tessdata"
            val fileList = context.assets.list(path)

            for (fileName in fileList!!) {

                // open file within the assets folder
                // if it is not already there copy it to the sdcard
                val pathToDataFile = "$DATA_PATH$path/$fileName"
                if (!File(pathToDataFile).exists()) {

                    val inputStream = context.assets.open("$path/$fileName")

                    val out = FileOutputStream(pathToDataFile)

                    // Transfer bytes from in to out
                    val buf = ByteArray(1024)
                    var len: Int
                    len = inputStream.read(buf)
                    while (len > 0) {
                        out.write(buf, 0, len)
                        len = inputStream.read(buf)
                    }
                    inputStream.close()
                    out.close()

                    Log.d("copyFiles", "Copied " + fileName + "to tessdata")
                }
            }
        } catch (e: IOException) {
            Log.e("copyFiles", "Unable to copy files to tessdata $e")
        }

    }
}
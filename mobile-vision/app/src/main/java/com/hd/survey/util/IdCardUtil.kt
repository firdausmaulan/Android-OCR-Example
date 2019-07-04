package com.hd.survey.util

import android.util.Log

object IdCardUtil {
    fun getId(text: String): String {
        var id = ""
        var counter = 0
        val listNumber = arrayListOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        val arrayText = text.split("\n".toRegex())
        if (arrayText.size > 2) {
            for (i in 0 until arrayText.size) {
                id = ""
                counter = 0
                val nextLine = arrayText[i]
                for (j in 0 until nextLine.length) {
                    if (listNumber.contains(nextLine[j])) {
                        id += nextLine[j]
                        counter++
                        if (counter == 16) break
                    }
                }
                if (counter == 16) break
            }
            Log.d("id", id)
        }
        return id
    }
}
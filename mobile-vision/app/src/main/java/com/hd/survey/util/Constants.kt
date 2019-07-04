package com.hd.survey.util

import android.Manifest

object Constants {
    val PERMISSIONS = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    )

    const val REQUEST_PERMISSIONS = 110
    const val REQUEST_CAMERA = 111
}

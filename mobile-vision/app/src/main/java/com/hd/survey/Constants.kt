package com.halodoc.orc

import android.Manifest

object Constants {
    val INITIAL_PERMS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )

    const val INITIAL_REQUEST = 110
}

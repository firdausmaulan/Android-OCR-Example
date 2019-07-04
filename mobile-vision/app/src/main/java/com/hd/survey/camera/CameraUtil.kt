package com.hd.survey.camera

import android.hardware.Camera

@Suppress("deprecation")
object CameraUtil {
    fun setParam(camera: Camera?) {
        val param = camera?.parameters

        val arrayCameraSize = param?.supportedPictureSizes
        var cameraSize = arrayCameraSize?.get(0)
        arrayCameraSize?.let {
            for (i in arrayCameraSize.indices) {
                if (arrayCameraSize[i].width > cameraSize!!.width) {
                    cameraSize = arrayCameraSize[i]
                }
            }
        }
        param?.setPictureSize(cameraSize!!.width, cameraSize!!.height)

        param?.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE

        camera?.parameters = param
    }
}

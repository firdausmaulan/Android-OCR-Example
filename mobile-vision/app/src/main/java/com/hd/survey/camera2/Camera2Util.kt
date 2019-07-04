package com.hd.survey.camera2

import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraDevice
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView

@SuppressLint("NewApi")
class Camera2Util(context: Camera2Activity) {

    internal val ORIENTATIONS = SparseIntArray()

    init {
        ORIENTATIONS.append(Surface.ROTATION_0, 90)
        ORIENTATIONS.append(Surface.ROTATION_90, 0)
        ORIENTATIONS.append(Surface.ROTATION_180, 270)
        ORIENTATIONS.append(Surface.ROTATION_270, 180)
    }

    val textureListener: TextureView.SurfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            //open your camera here
            context.openCamera()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            // Transform you image captured size according to the surface width and height
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }

    val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            //This is called when the camera is open
            context.cameraDevice = camera
            context.createCameraPreview()
        }

        override fun onDisconnected(camera: CameraDevice) {
            context.cameraDevice?.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            context.cameraDevice?.close()
            context.cameraDevice = null
        }
    }
}

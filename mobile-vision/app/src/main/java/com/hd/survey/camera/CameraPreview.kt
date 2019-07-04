package com.hd.survey.camera

import android.content.Context
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View

import java.io.IOException

@Suppress("deprecation")
class CameraPreview(context: Context, private var mCamera: Camera?, width: Int, height: Int)
    : SurfaceView(context), SurfaceHolder.Callback {

    private val mHolder: SurfaceHolder = holder

    init {
        mHolder.addCallback(this)
        mHolder.setFixedSize(width, height)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        try {
            // create the surface and start camera preview
            if (mCamera == null) {
                mCamera?.setPreviewDisplay(holder)
                mCamera?.startPreview()
            }
        } catch (e: IOException) {
            Log.d(View.VIEW_LOG_TAG, "Error setting camera preview: " + e.message)
        }

    }

    fun refreshCamera(camera: Camera?) {
        if (mHolder.surface == null) {
            // preview surface does not exist
            return
        }
        // stop preview before making changes
        try {
            mCamera?.stopPreview()
        } catch (e: Exception) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings
        setCamera(camera)
        try {
            mCamera?.setPreviewDisplay(mHolder)
            mCamera?.startPreview()
        } catch (e: Exception) {
            Log.d(View.VIEW_LOG_TAG, "Error starting camera preview: " + e.message)
        }

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        refreshCamera(mCamera)
    }

    fun setCamera(camera: Camera?) {
        //method to set a camera instance
        mCamera = camera
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {}
}
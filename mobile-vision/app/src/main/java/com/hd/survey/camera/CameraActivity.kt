package com.hd.survey.camera

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.hardware.Camera
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager
import com.hd.survey.util.BitmapUtil
import com.hd.survey.R
import kotlinx.android.synthetic.main.activity_camera.*

@Suppress("deprecation")
class CameraActivity : AppCompatActivity() {

    private var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null
    private var mPicture: Camera.PictureCallback? = null
    private lateinit var mBitmap: Bitmap
    private var mWidth = 0
    private var mHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_camera)

        openCamera()
        getWidthScreen()

        mPreview = CameraPreview(this, mCamera, mWidth, mHeight)
        cameraPreview.addView(mPreview)

        CameraUtil.setParam(mCamera)
        mCamera?.startPreview()

        btnCapture.setOnClickListener { mCamera?.takePicture(null, null, mPicture) }
    }

    private fun getWidthScreen() {
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        mWidth = size.x
        mHeight = size.y
    }

    private fun openCamera() {
        mCamera = Camera.open()
        mCamera?.setDisplayOrientation(90)
        mPicture = getPictureCallback()
        CameraUtil.setParam(mCamera)
        if (mPreview != null) mPreview?.refreshCamera(mCamera)
    }

    private fun releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera?.stopPreview()
            mCamera?.setPreviewCallback(null)
            mCamera?.release()
            mCamera = null
        }
    }

    private fun getPictureCallback(): Camera.PictureCallback {
        return Camera.PictureCallback { data, camera -> DecodeTask().execute(data) }
    }

    override fun onResume() {
        super.onResume()
        if (mCamera == null) openCamera()
    }

    override fun onPause() {
        super.onPause()
        releaseCamera()
    }

    private inner class DecodeTask : AsyncTask<ByteArray, Void, Uri>() {

        private var progressDialog: ProgressDialog? = null

        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog = ProgressDialog(this@CameraActivity)
            progressDialog?.setMessage("Loading")
            progressDialog?.setCancelable(false)
            progressDialog?.show()
        }

        override fun doInBackground(vararg bytes: ByteArray): Uri {
            mBitmap = BitmapFactory.decodeByteArray(bytes[0], 0, bytes[0].size)
            mBitmap = BitmapUtil.rotate(mBitmap, 90)
            mBitmap = BitmapUtil.scaleCenterCrop(mBitmap, mWidth, mWidth)
            return BitmapUtil.createFile(mBitmap)
        }

        override fun onPostExecute(uri: Uri) {
            super.onPostExecute(uri)
            progressDialog?.dismiss()
            val returnIntent = Intent()
            returnIntent.data = uri
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
    }
}

package com.hd.survey.camera2

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.Size
import android.view.Surface
import android.widget.Toast
import com.hd.survey.R
import com.hd.survey.util.BitmapUtil
import com.hd.survey.util.Constants
import kotlinx.android.synthetic.main.activity_camera_two.*
import java.util.*

@SuppressLint("NewApi")
class Camera2Activity : AppCompatActivity() {

    private val TAG = "Camera2Activity"
    var cameraDevice: CameraDevice? = null
    private lateinit var cameraCaptureSessions: CameraCaptureSession
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private lateinit var imageDimension: Size
    private var mBackgroundHandler: Handler? = null
    private var mBackgroundThread: HandlerThread? = null
    private lateinit var camera2Util: Camera2Util

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_two)

        camera2Util = Camera2Util(this)

        textureView.surfaceTextureListener = camera2Util.textureListener

        btnCapture.setOnClickListener {
            btnCapture.isEnabled = false
            takePhoto()
        }
    }

    fun openCamera() {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        Log.e(TAG, "is camera open")
        try {
            val cameraId = manager.cameraIdList[0]
            val characteristics = manager.getCameraCharacteristics(cameraId)
            val map = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
            )
            imageDimension = map.getOutputSizes(SurfaceTexture::class.java)[0]
            // Add permission for camera and let user grant the permission
            val permissionCamera = Manifest.permission.CAMERA
            val granted = PackageManager.PERMISSION_GRANTED
            if (ActivityCompat.checkSelfPermission(this, permissionCamera) != granted) {
                ActivityCompat.requestPermissions(
                        this, arrayOf(permissionCamera), Constants.REQUEST_PERMISSIONS
                )
                return
            }
            manager.openCamera(cameraId, camera2Util.stateCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

        Log.e(TAG, "openCamera X")
    }

    fun createCameraPreview() {
        try {
            val texture = textureView?.surfaceTexture
            texture?.setDefaultBufferSize(imageDimension.width, imageDimension.height)
            val surface = Surface(texture)
            captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder.addTarget(surface)
            cameraDevice?.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession
                    updatePreview()
                }

                override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                    Toast.makeText(this@Camera2Activity, "Configuration change", Toast.LENGTH_SHORT).show()
                }
            }, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    protected fun updatePreview() {
        if (null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return")
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        try {
            cameraCaptureSessions.setRepeatingRequest(
                    captureRequestBuilder.build(), null, mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "onResume")
        startBackgroundThread()
        if (textureView?.isAvailable == true) {
            openCamera()
        } else {
            textureView?.surfaceTextureListener = camera2Util.textureListener
        }
    }

    override fun onPause() {
        Log.e(TAG, "onPause")
        stopBackgroundThread()
        super.onPause()
    }

    protected fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("Camera Background")
        mBackgroundThread?.start()
        mBackgroundHandler = Handler(mBackgroundThread?.looper)
    }

    protected fun stopBackgroundThread() {
        mBackgroundThread?.quitSafely()
        try {
            mBackgroundThread?.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }

    protected fun takePhoto() {
        if (null == cameraDevice) {
            Log.e(TAG, "cameraDevice is null")
            return
        }
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val characteristics = manager.getCameraCharacteristics(cameraDevice?.id)
            var jpegSizes: Array<Size>? = null
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)?.getOutputSizes(ImageFormat.JPEG)
            }
            var width = 640
            var height = 480
            if (!jpegSizes.isNullOrEmpty()) {
                width = jpegSizes[0].width
                height = jpegSizes[0].height
            }
            val reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)
            val outputSurfaces = ArrayList<Surface>(2)
            outputSurfaces.add(reader.surface)
            outputSurfaces.add(Surface(textureView?.surfaceTexture))
            val captureBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder?.addTarget(reader.surface)
            captureBuilder?.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
            // Orientation
            val rotation = windowManager.defaultDisplay.rotation
            captureBuilder?.set(CaptureRequest.JPEG_ORIENTATION, camera2Util.ORIENTATIONS.get(rotation))
            var uri: Uri? = null
            val readerListener = object : ImageReader.OnImageAvailableListener {
                override fun onImageAvailable(reader: ImageReader) {
                    var image: Image? = null
                    image = reader.acquireLatestImage()
                    val buffer = image?.planes?.get(0)?.buffer
                    val bytes = ByteArray(buffer?.capacity()!!)
                    buffer.get(bytes)
                    save(bytes)
                    image?.close()
                }

                private fun save(bytes: ByteArray) {
                    var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    bitmap = BitmapUtil.scaleCenterCrop(bitmap, width, width)
                    uri = BitmapUtil.createFile(bitmap)
                }
            }
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler)

            val captureListener = object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(session: CameraCaptureSession,
                                                request: CaptureRequest,
                                                result: TotalCaptureResult) {
                    super.onCaptureCompleted(session, request, result)
                    val returnIntent = Intent()
                    returnIntent.data = uri
                    setResult(Activity.RESULT_OK, returnIntent)
                    finish()
                }
            }
            cameraDevice?.createCaptureSession(outputSurfaces, object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    try {
                        session.capture(captureBuilder?.build(), captureListener, mBackgroundHandler)
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }

                }

                override fun onConfigureFailed(session: CameraCaptureSession) {}
            }, mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }
}
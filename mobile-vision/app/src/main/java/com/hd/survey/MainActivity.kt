package com.hd.survey

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.hd.survey.camera.CameraActivity
import com.hd.survey.camera2.Camera2Activity
import com.hd.survey.util.Constants
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(
                this, Constants.PERMISSIONS, Constants.REQUEST_PERMISSIONS
        )

        FirebaseApp.initializeApp(this)

        btnTakePhoto.setOnClickListener {
            val intent: Intent
            if (Build.VERSION.SDK_INT < 21) {
                intent = Intent(this, CameraActivity::class.java)
            } else {
                intent = Intent(this, Camera2Activity::class.java)
            }
            startActivityForResult(intent, Constants.REQUEST_CAMERA)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.REQUEST_CAMERA) {
                val uri = data?.data
                Log.d("uri", uri.toString())
                uri?.let {
                    loadImage(uri)
                    readOCR(uri)
                }
            }
        }
    }

    private fun loadImage(uri: Uri) {
        Glide.with(this)
                .load(uri)
                .into(ivOCR)
    }

    private fun readOCR(uri: Uri) {
        tvResult.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        try {
            val image = FirebaseVisionImage.fromFilePath(this, uri)
            val detector = FirebaseVision.getInstance().onDeviceTextRecognizer
            detector.processImage(image)
                    .addOnSuccessListener { firebaseVisionText ->
                        //IdCardUtil.getId(firebaseVisionText.text.toString())
                        tvResult.text = firebaseVisionText.text
                        tvResult.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                    }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

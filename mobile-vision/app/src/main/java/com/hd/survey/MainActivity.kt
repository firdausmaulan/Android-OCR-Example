package com.hd.survey

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.bumptech.glide.Glide
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.halodoc.orc.Constants
import com.halodoc.orc.Constants.INITIAL_PERMS
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_main.*
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(this, INITIAL_PERMS, Constants.INITIAL_REQUEST)

        FirebaseApp.initializeApp(this)

        btnTakePhoto.setOnClickListener {
            EasyImage.openCameraForImage(this, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        EasyImage.handleActivityResult(requestCode, resultCode, data, this, object : DefaultCallback() {
            override fun onImagesPicked(files: MutableList<File>, source: EasyImage.ImageSource?, type: Int) {
                CropImage.activity(Uri.fromFile(files[0])).start(this@MainActivity)
            }
        })
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val result = CropImage.getActivityResult(data)
                loadImage(result.uri)
                readOCR(result.uri)
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
                    tvResult.text = firebaseVisionText.text
                    tvResult.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

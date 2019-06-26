package com.hd.ocr;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.util.List;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class MainActivity extends AppCompatActivity {

    private ImageView ivOCR;
    private TextView tvResult;
    private Button btnTakePhoto;
    private ProgressBar progressBar;

    private File imageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initPermission();

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EasyImage.openCameraForImage(MainActivity.this, 0);
            }
        });
    }

    private void initPermission() {
        String[] INITIAL_PERMS = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        };

        ActivityCompat.requestPermissions(this, INITIAL_PERMS, 110);
    }

    private void initView() {
        ivOCR = findViewById(R.id.ivOCR);
        tvResult = findViewById(R.id.tvResult);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        progressBar = findViewById(R.id.progressBar);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagesPicked(List<File> files, EasyImage.ImageSource source, int type) {
                CropImage.activity(Uri.fromFile(files.get(0))).start(MainActivity.this);
            }
        });
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                imageFile = new File(result.getUri().getPath());
                loadImage(imageFile);
                new ConvertTask().execute(imageFile);
            }
        }
    }

    private void loadImage(File imageFile) {
        Glide.with(this).load(imageFile).into(ivOCR);
    }


    private class ConvertTask extends AsyncTask<File, Void, String> {
        TessBaseAPI tesseract = new TessBaseAPI();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            String datapath = getFilesDir() + "/tesseract/";
            FileUtil fileUtil = new FileUtil();
            fileUtil.checkFile(
                    MainActivity.this,
                    datapath,
                    new File(datapath + "tessdata/")
            );
            tesseract.init(datapath, "eng");
            tvResult.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(File... files) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4; // 1 - means max size. 4 - means maxsize/4 size. Don't use value <4, because you need more memory in the heap to store your data.
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getPath(), options);
            tesseract.setImage(bitmap);
            String result = tesseract.getUTF8Text();
            tesseract.end();
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            tvResult.setText(result);
            tvResult.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }
}

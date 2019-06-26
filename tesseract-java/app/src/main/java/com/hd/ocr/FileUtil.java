package com.hd.ocr;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {

    void checkFile(Context context, String datapath, File dir) {
        //directory does not exist, but we can successfully create it
        if (!dir.exists() && dir.mkdirs()) {
            copyFiles(context, datapath);
        }
        //The directory exists, but there is no data file in it
        if (dir.exists()) {
            String datafilepath = datapath + "/tessdata/eng.traineddata";
            File datafile = new File(datafilepath);
            if (!datafile.exists()) {
                copyFiles(context, datapath);
            }
        }
    }

    /*private void copyFiles(Context context, String datapath) {
        try {
            //location we want the file to be at
            String filepath = datapath + "/tessdata/eng.traineddata";

            //get access to AssetManager
            AssetManager assetManager = context.getAssets();

            //open byte streams for reading/writing
            InputStream instream = assetManager.open("tessdata/eng.traineddata");
            OutputStream outstream = new FileOutputStream(filepath);

            //copy the file to the location specified by filepath
            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }
            outstream.flush();
            outstream.close();
            instream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/


    private void copyFiles(Context context, String DATA_PATH) {
        try {
            String path = "tessdata";
            String fileList[] = context.getAssets().list(path);

            for (String fileName : fileList) {

                // open file within the assets folder
                // if it is not already there copy it to the sdcard
                String pathToDataFile = DATA_PATH + path + "/" + fileName;
                if (!(new File(pathToDataFile)).exists()) {

                    InputStream in = context.getAssets().open(path + "/" + fileName);

                    OutputStream out = new FileOutputStream(pathToDataFile);

                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;

                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();

                    Log.d("copyFiles", "Copied " + fileName + "to tessdata");
                }
            }
        } catch (IOException e) {
            Log.e("copyFiles", "Unable to copy files to tessdata " + e.toString());
        }
    }

}

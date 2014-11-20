package com.avgtechie.videoencoderdecoder;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by ashish on 10/26/14.
 */
public class FileUtil {
    private static final String TAG = "FileUtil";
    private static FileUtil fileUtil;

    private String fileName = "video_recording_2.mp4";
    private String golf_tiger_woods = "slack_for_ios_upload.mp4";
    private String slider = "gen-sliders.mp4";
    private String youtube = "youtube_tigerwoods.mp4";

    public static FileUtil getInstance() {
        if (fileUtil == null) {
            fileUtil = new FileUtil();
        }
        return fileUtil;
    }

    public File getMemeDirPath() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
    }


    public File getMemeFilePath() {
        File memeDir = getMemeDirPath();
        File videoFile = new File(memeDir, youtube);
        return videoFile;
    }

    public void copyFileFromAssets(Context context) {
        InputStream in = null;
        OutputStream fout = null;
        int count = 0;

        try {
            in = context.getAssets().open(youtube);
            fout = new FileOutputStream(getMemeFilePath(youtube));

            byte data[] = new byte[1024];
            while ((count = in.read(data, 0, 1024)) != -1) {
                fout.write(data, 0, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                }
            }
        }
    }


    public File getMemeFilePath(String fileName) {
        File memeDir = getMemeDirPath();
        File videoFile = new File(memeDir, fileName);
        return videoFile;
    }

    public void saveBitmapToCameraRoll(Bitmap bitmap, Context context, String fileName) {
        File memeImageFile = createMemeFileIfNotExist(fileName);
        OutputStream fOutputStream = null;
        try {
            fOutputStream = new FileOutputStream(memeImageFile);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOutputStream);

            fOutputStream.flush();
            fOutputStream.close();
            Log.d(TAG, "Saving : " + memeImageFile.getAbsolutePath());
            MediaStore.Images.Media.insertImage(context.getContentResolver(), memeImageFile.getAbsolutePath(), memeImageFile.getName(), memeImageFile.getName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Save Failed", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public File getOutputFile(Context context) {
        File outputFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "camera-test.mp4");
        return outputFile;
    }

    public File createMemeFileIfNotExist(String fileName) {
        File destDir = getMemeDirPath();
        if (destDir != null && !destDir.exists()) {
            destDir.mkdirs();
        }

        File memeFile = getMemeFilePath(fileName);
        if (!memeFile.exists()) {
            try {
                memeFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return memeFile;

    }

}

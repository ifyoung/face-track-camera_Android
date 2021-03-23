package face.camera.beans.base.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * description:
 * Created by aserbao on 2018/5/14.
 */


public class FileUtils {


    public static final String LOG_TAG = "FileUtils";
    public static final File externalStorageDirectory = Environment.getExternalStorageDirectory();
    public static String packageFilesDirectory = null;
    public static String storagePath = null;
    private static String mDefaultFolder = "DCIM/ZP";

    public static void setDefaultFolder(String defaultFolder) {
        mDefaultFolder = defaultFolder;
    }

    public static String getPath() {
        return getPath(null);
    }

    public static String getPath(Context context) {

        if(storagePath == null) {
            storagePath = externalStorageDirectory.getAbsolutePath() + "/" + mDefaultFolder;
            File file = new File(storagePath);
            if(!file.exists()) {
                if(!file.mkdirs()) {
                    storagePath = getPathInPackage(context, true);
                }
            }
        }

        return storagePath;
    }

    public static String getPathInPackage(Context context, boolean grantPermissions) {

        if(context == null || packageFilesDirectory != null)
            return packageFilesDirectory;

        //手机不存在sdcard, 需要使用 data/data/name.of.package/files 目录
        String path = context.getFilesDir() + "/" + mDefaultFolder;
        File file = new File(path);

        if(!file.exists()) {
            if(!file.mkdirs()) {
                Log.e(LOG_TAG, "Create package dir of CGE failed!");
                return null;
            }

            if(grantPermissions) {

                //设置隐藏目录权限.
                if (file.setExecutable(true, false)) {
                    Log.i(LOG_TAG, "Package folder is executable");
                }

                if (file.setReadable(true, false)) {
                    Log.i(LOG_TAG, "Package folder is readable");
                }

                if (file.setWritable(true, false)) {
                    Log.i(LOG_TAG, "Package folder is writable");
                }
            }
        }

        packageFilesDirectory = path;
        return packageFilesDirectory;
    }

    public static void saveTextContent(String text, String filename) {
        Log.i(LOG_TAG, "Saving text : " + filename);

        try {
            FileOutputStream fileout = new FileOutputStream(filename);
            fileout.write(text.getBytes());
            fileout.flush();
            fileout.close();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error: " + e.getMessage());
        }
    }

    public static String getTextContent(String filename) {
        Log.i(LOG_TAG, "Reading text : " + filename);

        if(filename == null) {
            return null;
        }

        String content = "";
        byte[] buffer = new byte[256]; //Create cache for reading.

        try {

            FileInputStream filein = new FileInputStream(filename);
            int len;

            while(true) {
                len = filein.read(buffer);

                if(len <= 0)
                    break;

                content += new String(buffer, 0, len);
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error: " + e.getMessage());
            return null;
        }

        return content;
    }

    public static File getStorageMp4(String s) {
        File file;
//        String parent = Environment.getExternalStorageDirectory().getAbsolutePath() + "/aserbao";
//        String parent = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/ZP";
        String parent = getPath();
        File file1 = new File(parent);
        if (!file1.exists()) {
            file1.mkdir();
        }
        String append = "ZP" + s;

        file = new File(parent, append + ".mp4");

//        return file.getPath();
        return file;
    }


    public static String saveBitmap(Bitmap bmp) {
        String path = getPath();
        long currentTime = System.currentTimeMillis();
        String filename = path + "/" + currentTime + ".jpg";
        return saveBitmap(bmp, filename);
    }

    public static String saveBitmap(Bitmap bmp, String filename) {

        Log.i(LOG_TAG, "saving Bitmap : " + filename);

        try {
            FileOutputStream fileout = new FileOutputStream(filename);
            BufferedOutputStream bufferOutStream = new BufferedOutputStream(fileout);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, bufferOutStream);
            bufferOutStream.flush();
            bufferOutStream.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Err when saving bitmap...");
            e.printStackTrace();
            return null;
        }

        Log.i(LOG_TAG, "Bitmap " + filename + " saved!");
        return filename;
    }

}

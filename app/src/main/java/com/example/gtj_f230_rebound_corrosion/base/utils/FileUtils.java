package com.example.gtj_f230_rebound_corrosion.base.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.example.gtj_f230_rebound_corrosion.StaticConstant;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FileUtils {
    /**
     * 保存文件到本地
     */
    public static String saveDataFile(String fileDir, String fileName, byte[] data) {
        String path = getFilePath(fileDir, fileName);
        String filePath = null;
        try {
            assert path != null;
            OutputStream fos = new FileOutputStream(path);
            fos.write(data);
            fos.flush();
            fos.close();
            filePath = path;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filePath;
    }

    private static String getFilePath(String fileDir, String fileName) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dir = new File(StaticConstant.dataFilePathExport + fileDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(StaticConstant.dataFilePathExport + fileDir + fileName);
            if (file.exists()) {
                file.delete();
            }
            return file.getPath();
        }
        return null;
    }

    public static boolean copyFile(String strSrcFile, String strDestFile) {
        try {
            FileInputStream is = new FileInputStream(strSrcFile);
            if (is.available() <= 100) {
                return false;
            }
            int len;
            byte[] buffer = new byte[1024];
            FileOutputStream fos = new FileOutputStream(strDestFile);
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.flush();
            fos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 转换成JPG格式图片
     *
     * @param pngFilePath png或者bmp照片
     * @param jpgFilePath jpg照片
     */
    public static boolean convertToJpg(String pngFilePath, String jpgFilePath, int quality) {
        Bitmap bitmap = BitmapFactory.decodeFile(pngFilePath);
        boolean flag;
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(jpgFilePath));
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos)) {
                bos.flush();
            }
            bos.close();
            flag = true;
        } catch (IOException e) {
            e.printStackTrace();
            flag = false;
        } finally {
            bitmap.recycle();
        }
        return flag;
    }

    public static boolean saveBitmap(String strImagePath, Bitmap bitmap) {
        boolean flag;
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(strImagePath));
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)) {
                bos.flush();
            }
            bos.close();
            flag = true;
        } catch (IOException e) {
            e.printStackTrace();
            flag = false;
        }
        return flag;
    }
}

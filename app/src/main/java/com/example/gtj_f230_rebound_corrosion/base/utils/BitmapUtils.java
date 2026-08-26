package com.example.gtj_f230_rebound_corrosion.base.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.widget.ScrollView;

import androidx.core.content.FileProvider;

import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.orhanobut.logger.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BitmapUtils {

    public static byte[] compressBitmap(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    public static Bitmap zoomImg(Bitmap bm, int newWidth) {
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
//        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleWidth);
        // 得到新的图片
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
    }

    public static void clearFiles(String fileName) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dir = new File(fileName);
            if (dir.exists()) {
                DeleteFileUtils.deleteDirectory(fileName);
            }
        }
    }

    public static Bitmap screenshotView(View view) {
        view.setBackgroundColor(Color.parseColor("#FFFFFF"));
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    /**
     * 保存图片到本地
     *
     * @param context：context
     * @param data：流
     * @return 本地图片地址
     */
    public static String saveBitmapFile(Context context, String fileName, byte[] data) {
        String path = getFilePath(fileName);
        assert path != null;
        Uri imageFileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileProvider", new File(path));
        String filePath = null;
        try {
            OutputStream imageFileOS = context.getContentResolver().openOutputStream(imageFileUri);
            if (imageFileOS != null) {
                imageFileOS.write(data);
                imageFileOS.flush();
                imageFileOS.close();
                filePath = path;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filePath;
    }

    private static String getFilePath(String fileName) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dir = new File(StaticConstant.dataFilePathCache);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(StaticConstant.dataFilePathCache + fileName + ".jpg");
            if (file.exists()) {
                file.delete();
            }
            return StaticConstant.dataFilePathCache + fileName + ".jpg";
        }
        return null;
    }

    public static void clearChartFile() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dir = new File(StaticConstant.dataFilePathCache);
            if (dir.exists()) {
                DeleteFileUtils.deleteDirectory(StaticConstant.dataFilePathCache);
            }
        }
    }

    /**
     * 获取scrollview的截屏
     */
    public static Bitmap scrollViewScreenShot(ScrollView scrollView) {
        int h = 0;
        Bitmap bitmap;
        for (int i = 0; i < scrollView.getChildCount(); i++) {
            h += scrollView.getChildAt(i).getHeight();
            scrollView.getChildAt(i).setBackgroundColor(Color.parseColor("#ffffff"));
        }
        bitmap = Bitmap.createBitmap(scrollView.getWidth(), h, Bitmap.Config.RGB_565);
        final Canvas canvas = new Canvas(bitmap);
        scrollView.draw(canvas);
        return bitmap;
    }

    public static Bitmap convertViewToBitmap(View tempView) {
        Bitmap bitmap = Bitmap.createBitmap(tempView.getWidth(),
                tempView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        tempView.draw(canvas);
        return bitmap;
    }

    /**
     * 将图片转换成Base64编码
     */
    public static String Bitmap2StrByBase64_f230(String imagePath) {
        //将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        InputStream in;
        byte[] data = null;
        //读取图片字节数组
        try {
            in = new FileInputStream(imagePath);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Base64.encode(data);
    }

    public static void checkExists(String strDir) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dir = new File(strDir);
            if (!dir.exists()) {
                Logger.e("checkExists=" + dir.mkdirs());
            } else {
                Logger.e("else-checkExists=true");
            }
        }
    }

    /**
     * 将Bitmap转换成Base64字符串
     */
    public static String Bitmap2StrByBase64(String imagePath) {
        try {
            FileInputStream fis = new FileInputStream(imagePath);
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);//参数100表示不压缩
            byte[] bytes = bos.toByteArray();
            return android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT).trim();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 图片旋转
     *
     * @param tmpBitmap
     * @param degrees
     * @return
     */
    public static Bitmap rotateToDegrees(Bitmap tmpBitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.setRotate(degrees);
        return Bitmap.createBitmap(tmpBitmap, 0, 0, tmpBitmap.getWidth(), tmpBitmap.getHeight(), matrix,
                true);
    }
}

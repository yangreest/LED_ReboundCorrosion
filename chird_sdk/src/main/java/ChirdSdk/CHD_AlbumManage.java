package ChirdSdk;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.MediaStore.Video.Thumbnails;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;

import ChirdSdk.Apis.chd_wmp_apis;
import androidx.core.content.FileProvider;

public class CHD_AlbumManage {

    private chd_wmp_apis mAlbumManage = new chd_wmp_apis();

    public CHD_AlbumManage() {

    }

    /**
     * 保存图片文件微缩图
     *
     * @param url    照片存储路径
     * @param width  照片宽
     * @param height 照片高度
     * @param bitmap 录像微缩图
     * @return null
     */
    public int savePictureThumbnail(String url, int width, int height,
                                    Bitmap bitmap) {
        if (url == null)
            return -1;

        return mAlbumManage.CHD_AlbumManage_SavePictureThumbnail(url, width,
                height, bitmap);
    }

    /**
     * 获取照片实际分辨率
     *
     * @return 照片实际分辨率 失败返回空
     */
    public String getPictrureResolu(String url) {
        String sresolu = "";
        int width = mAlbumManage.CHD_AlbumManage_GetPictureWidth(url);
        int height = mAlbumManage.CHD_AlbumManage_GetPictureHeight(url);
        if (width <= 0 || height <= 0) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            Bitmap bitmap = BitmapFactory.decodeFile(url, options);
            options.inJustDecodeBounds = false;
            int beWidth = options.outWidth / width;
            int beHeight = options.outHeight / height;
            int be = beWidth < beHeight ? beWidth : beHeight;
            options.inSampleSize = be <= 0 ? 1 : be;
            bitmap = BitmapFactory.decodeFile(url, options);
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
            if (bitmap != null) {
                width = bitmap.getWidth();
                height = bitmap.getHeight();
            }
        }

        sresolu = "" + (width > 0 ? width : "") + "x"
                + (height > 0 ? height : "");

        return sresolu;
    }

    /**
     * 获取照片微缩图
     *
     * @return 照片微缩图 失败返回null
     */
    public Bitmap getPictureThumbnail(String url) {
        int width = 64;
        int height = 48;
        Bitmap bitmap = null;
        bitmap = mAlbumManage.CHD_AlbumManage_GetPictureThumbnail(url);
        if (bitmap == null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            bitmap = BitmapFactory.decodeFile(url, options);
            options.inJustDecodeBounds = false;
            int beWidth = options.outWidth / width;
            int beHeight = options.outHeight / height;
            int be = beWidth < beHeight ? beWidth : beHeight;
            options.inSampleSize = be <= 0 ? 1 : be;
            bitmap = BitmapFactory.decodeFile(url, options);
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
//		Log.v("test", "bitmap:" + bitmap.getWidth() + "x" + bitmap.getHeight());
        return bitmap;
    }

    /**
     * 保存录像文件微缩图
     *
     * @param url    录像文件存储路径
     * @param time   录像时间
     * @param bitmap 录像微缩图
     * @return null
     */
    public int saveVideoThumbnail(String url, int time, Bitmap bitmap) {
        if (url == null)
            return -1;

        return mAlbumManage.CHD_AlbumManage_SaveVideoThumbnail(url, time,
                bitmap);
    }

    /**
     * 获取录像时间
     *
     * @return 录像时间，失败录像时间为"0"
     */
    public String getVideoTime(String url) {
        int time = mAlbumManage.CHD_AlbumManage_GetVideoTime(url);

        return getStringTime(time);
    }

    /**
     * 获取录像微缩图
     *
     * @return 录像微缩图 失败返回null
     */
    public Bitmap getVideoThumbnail(String url) {
        Bitmap bitmap = null;
        bitmap = mAlbumManage.CHD_AlbumManage_GetVideoThumbnail(url);
        if (bitmap == null) {
            bitmap = ThumbnailUtils.createVideoThumbnail(url,
                    Thumbnails.MICRO_KIND);
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, 64, 48,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }

        return bitmap;
    }

    /**
     * 删除照片或文件
     *
     * @return null
     */
    public void deleteFile(String url) {

        mAlbumManage.CHD_AlbumManage_DeleteFile(url);
    }

    /**
     * 调用系统相册播放视频
     */
    public void playVideoFile(Context context, String url, String authorities) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        File file = new File(url);

        Uri uri;
        if (Build.VERSION.SDK_INT > 23) {
            uri = FileProvider.getUriForFile(context, authorities, file);
        } else {
            uri = Uri.fromFile(file);
        }

        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        // 文件读权限
        if (Build.VERSION.SDK_INT > 23) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        intent.setDataAndType(uri, "video/*");
        context.getApplicationContext().startActivity(intent);
    }

    /**
     * 调用系统相册显示图片
     */
    public void showPictureFile(Context context, String url, String authorities) {

        if (TextUtils.isEmpty(url)) {
            return;
        }
        File file = new File(url);

        Uri uri;
        if (Build.VERSION.SDK_INT > 23) {
            uri = FileProvider.getUriForFile(context, authorities, file);
        } else {
            uri = Uri.fromFile(file);
        }

        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        // 文件读权限
        if (Build.VERSION.SDK_INT > 23) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        intent.setDataAndType(uri, "image/*");
        context.getApplicationContext().startActivity(intent);
    }

    /**
     * 拷贝图片到系统相册
     *
     * @param url 图片路径
     */
    public int copyImageToSystemAlbum(Context context, String url) {
        if (url == null) {
            return -1;
        }

        File file = new File(url);
        if (!file.exists()) {
            return -2;
        }

        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    url, url, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        /** 最后通知图库更新 */
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://*")));

        Log.v("test", "-->>>>copyImageToSystemAlbum");


        return 0;
    }

    /**
     * 拷贝视频到系统相册
     *
     * @param url 视频路径
     */
    public int copyVideoToSystemAblium(Context context, String url) {
        if (url == null) {
            return -1;
        }

        File file = new File(url);
        if (!file.exists()) {
            return -2;
        }

        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(new File(url)));
        context.sendBroadcast(intent);

        Log.v("test", "-->>>>copyVideoToSystemAblium");

        return 0;
    }

    /**
     * 分享图片
     *
     * @param url 图片路径
     */
    public int sharePicture(Context context, String url, String authorities) {

        if (url == null) {
            return -1;
        }

        File file = new File(url);
        if (!file.exists()) {
            return -2;
        }

        Uri uuri;
        if (Build.VERSION.SDK_INT > 23) {
            uuri = FileProvider.getUriForFile(context, authorities, file);
        } else {
            uuri = Uri.fromFile(file);
        }

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uuri);
        shareIntent.setType("image/*");
        context.getApplicationContext().startActivity(shareIntent);

        return 0;
    }

    /**
     * 分享视频
     *
     * @param url 视频路径
     */
    public int shareVideo(Context context, String url, String authorities) {
        if (url == null) {
            return -1;
        }

        File file = new File(url);
        if (!file.exists()) {
            return -2;
        }

        Uri uuri;
        if (Build.VERSION.SDK_INT > 23) {
            uuri = FileProvider.getUriForFile(context, authorities, file);
        } else {
            uuri = Uri.fromFile(file);
        }

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uuri);
        shareIntent.setType("video/*");
        context.getApplicationContext().startActivity(shareIntent);

        return 0;
    }

    /**
     * 获取录像时间字符串
     *
     * @param timer 整形的时间
     * @return 整理后按照时分秒命名的字符串
     */
    private String getStringTime(int timer) {
        String Stimer;
        if ((timer / 60) >= 10) {
            Stimer = String.valueOf(timer / 60) + ":";
        } else {
            Stimer = "0" + String.valueOf(timer / 60) + ":";
        }
        if ((timer % 60) >= 10) {
            Stimer += String.valueOf(timer % 60);
        } else {
            Stimer += "0" + String.valueOf(timer % 60);
        }

        return Stimer;
    }

}

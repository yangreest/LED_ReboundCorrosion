package ChirdSdk.ChirdCoder;

import android.graphics.Bitmap;

public class nativeCoder {
    /**
     * 视频队列相关 13个函数
     */
    public native long chird_video_list_create();

    public native int chird_video_list_destory(long handle);

    public native int chird_video_list_malloc(long handle, st_DecInfo stDecInfo);

    public native int chird_video_list_free(long handle, st_DecInfo stDecInfo);

    public native int chird_video_list_add(long handle, st_DecInfo stDecInfo);

    public native int chird_video_list_get(long handle, st_DecInfo stDecInfo, Bitmap bitmap);

    public native int chird_video_list_clean(long handle);

    public native int chird_video_list_openMultiPointFreezeFile(long handle, String fileName);

    public native int chird_video_list_forceCloseMultiPointFreeze(long handle);

    public native int chird_video_list_setFreezeFlag(long handle, int flag, String fileName);

    public native int chird_video_list_getFreezeFrameBitmap(long handle, st_DecInfo stDecInfo, Bitmap bitmap);

    public native int chird_video_list_getPreVideoBitmap(long handle, st_DecInfo stDecInfo, Bitmap bitmap);

    public native int chird_video_list_getNextVideoBitmap(long handle, st_DecInfo stDecInfo, Bitmap bitmap);

    /**
     * 视频解码显示相关 13个函数
     */
    public static final int CODE_FMT_H264 = 28;
    public static final int CODE_FMT_MJPEG = 8;

    public static final int CODE_PIXEL_FMT_YUYV420 = 0;
    public static final int CODE_PIXEL_FMT_YUYV422 = 1;
    public static final int CODE_PIXEL_FMT_RGB24 = 2;
    public static final int CODE_PIXEL_FMT_BGR24 = 3;
    public static final int CODE_PIXEL_FMT_GRAY8 = 8;
    public static final int CODE_PIXEL_FMT_YUV420SP = 25;
    public static final int CODE_PIXEL_FMT_RGBA8888 = 28;
    public static final int CODE_PIXEL_FMT_BGRA8888 = 30;
    public static final int CODE_PIXEL_FMT_RGB565 = 44;

    public native long chird_videomen_malloc(int length);

    public native long chird_videomen_realloc(long handle, int length);

    public native int chird_videomen_free(long handle);

    public native int chird_videomen_copy(long srchandle, int lenght, long desthandle);

    public native int chird_videomen_copytobitmap(long handle, int width, int height, Bitmap bitmap);

    public native int chird_videomen_copytoarray(long handle, int length, byte[] dest);

    public native long chird_vdec_create(int srcfmt, int destfmt);

    public native int chird_vdec_process(long handle, int width, int height, int datalen, byte[] data, Bitmap bitmap);

    public native int chird_vdec_processbyaddress(long handle, int width, int height, int datalen, long pdata, Bitmap bitmap);

    public native int chird_vdec_processaddress(long handle, int width, int height, int datalen, long srcdata, long destdata);

    public native int chird_vdec_destory(long handle);

    public native int chird_vdec_bitmapcopy(Bitmap src, Bitmap dest);

    public native int chird_vdec_copybitmaptobytearray(Bitmap src, byte[] dest);

    public native Bitmap chird_vdec_processtobitmap(long handle, int width, int height, int datalen, long pdata);


    /**
     * 视频格式转换相关 3个函数
     */
    public native int chird_sws_process(int srcPixel, byte[] data, int destPixel, Bitmap bitmap, int width, int height);

    public native int chird_sws_processbyaddress(int srcPixel, long pdata, int destPixel, Bitmap bitmap, int width, int height);

    public native int chird_sws_processaddress(int srcPixel, long psrcdata, int destPixel, long pdestdata, int width, int height);


    /**
     * 音频解码相关 4个函数
     */
    public static final int CODE_FMT_G711U = 0xAA00;
    public static final int CODE_FMT_G711A = 0xAA01;
    public static final int CODE_FMT_G726 = 0xA726;

    public native long chird_adec_create(int srcfmt, int rate);

    public native int chird_adec_process(long handle, byte[] srcdata, int srclen, byte[] destdata);

    public native int chird_adec_destory(long handle);

    /**
     * 录像相关 4个函数
     */
    public static final int MIXER_TYPE_VIDEO = 0;
    public static final int MIXER_TYPE_AUDIO = 1;

    public native long chird_mixer_create(String fileName, int width, int height, int fps, int srcvideofmt, int srcaudiofmt);

    public native int chird_mixer_process(long handle, int type, byte[] pdata, int datalen, int timerstamp);

    public native int chird_mixer_processbyaddress(long handle, int type, long pdata, int datalen, int timerstamp);

    public native int chird_mixer_destory(long handle);

    /**
     * RGB录像相关 11个函数
     */

    public native long chird_rgbrecord_create(String fileName, int width, int height);

    public native int chird_rgbrecord_write(long handle, int width, int height, int len, long pdata);

    public native int chird_rgbrecord_destory(long handle);


    public native long chird_rgbrecord_openfile(String fileName, st_RGBInfo strgb);

    public native int chird_rgbrecord_tail(long handle, st_RGBInfo strgb, Bitmap bitmap);

    public native int chird_rgbrecord_head(long handle, st_RGBInfo strgb, Bitmap bitmap);

    public native int chird_rgbrecord_next(long handle, st_RGBInfo strgb, Bitmap bitmap);

    public native int chird_rgbrecord_last(long handle, st_RGBInfo strgb, Bitmap bitmap);

    public native int chird_rgbrecord_close(long handle);


    static {

        System.loadLibrary("nativeCoder");
    }
}

package ChirdSdk.ChirdCoder;

public class st_DecInfo {

    public static final int CHD_FMT_YUYV = 0X01;
    public static final int CHD_FMT_MJPEG = 0x02;
    public static final int CHD_FMT_H264 = 0x03;

    public static final int VIDEO_DECODE_TYPE_RGB24 = 2;
    public static final int VIDEO_DECODE_TYPE_GRAY8 = 8;

    public int format = 0;
    public int width = 0;
    public int height = 0;
    public int datalen = 0;
    public int decType = 0;
    public long pDataAddress = 0;

    public long videoInfoAddress = 0;

}

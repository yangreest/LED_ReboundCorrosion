package ChirdSdk.Apis;

public class st_VideoParamInfo {

    public static final int CHD_FMT_YUYV = 0X01;
    public static final int CHD_FMT_MJPEG = 0x02;
    public static final int CHD_FMT_H264 = 0x03;

    public int format = 0;
    public int width = 0;
    public int height = 0;
    public int fps = 0;
    public int maxfps = 0;

    // OSD 不需要设置的参数必须设置为-1
    public int osd_index = 0;
    public int osd_enable = 0;
    public int osd_x = 0;
    public int osd_y = 0;

    // TOSD 不需要设置的参数必须设置为-1
    public int tosd_enable = 0;
    public int tosd_x = 0;
    public int tosd_y = 0;


    // motion detection
    public int md_enable;
    public int md_sensity;


    // device video mirror
    public int horizontal = 0;
    public int vertical = 0;

    public String GetFormatString() {
        switch (format) {
            case CHD_FMT_YUYV:
                return "YUYV";
            case CHD_FMT_MJPEG:
                return "JPEG";
            case CHD_FMT_H264:
                return "H264";
            default:
                return "other";
        }
    }


    public String GetResoluString() {
        return String.valueOf(width) + "x" + String.valueOf(height);
    }
}

package ChirdSdk.ChirdCoder;

public class st_VideoDecFrame implements Cloneable {

    public static final int CHD_FMT_RGB565 = 0X00;
    public static final int CHD_FMT_YUYV = 0X01;
    public static final int CHD_FMT_MJPEG = 0x02;
    public static final int CHD_FMT_H264 = 0x03;

    public int format = 0;
    public int width = 0;
    public int height = 0;
    public int fps = 0;
    public int BPS = 0;
    public int timestamp = 0;
    public int datalen = 0;

    public int maxLength = 0;
    public long pDataAddress = 0;

    /**
     * 时间挫，用于录像回放
     */
    public int year = 0;
    public int month = 0;
    public int day = 0;
    public int hour = 0;
    public int min = 0;
    public int sec = 0;

    public void copyDecFrame(int fmt, int w, int h, int len, int time) {
        format = fmt;
        width = w;
        height = h;
        datalen = len;
        timestamp = time;
    }

    /* Object Deep Level Copy */
    public Object clone() {
        st_VideoDecFrame bankAccount = null;
        try {
            bankAccount = (st_VideoDecFrame) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return bankAccount;
    }

}

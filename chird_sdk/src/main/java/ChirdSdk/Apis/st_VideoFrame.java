package ChirdSdk.Apis;

public class st_VideoFrame implements Cloneable {

    public static final int CHD_FMT_YUYV = 0X01;
    public static final int CHD_FMT_MJPEG = 0x02;
    public static final int CHD_FMT_H264 = 0x03;

    public int bexist = 0;
    public int format = 0;
    public int width = 0;
    public int height = 0;
    public int fps = 0;
    public int BPS = 0;
    public int timestamp = 0;
    public int datalen = 0;

    public int sequence = 0;
    public int iflag = 0;
    public int gop = 0;
    public int queueNum = 0;

    public long Address = 0;
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

    public void setTimestamp(int y, int mon, int d, int h, int m, int s) {
        year = y;
        month = mon;
        day = d;
        hour = h;
        min = m;
        sec = s;
    }

    /* Object Deep Level Copy */
    public Object clone() {
        st_VideoFrame bankAccount = null;
        try {
            bankAccount = (st_VideoFrame) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return bankAccount;
    }

}

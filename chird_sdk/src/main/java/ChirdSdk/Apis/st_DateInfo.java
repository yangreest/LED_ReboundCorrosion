package ChirdSdk.Apis;

public class st_DateInfo {

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

}

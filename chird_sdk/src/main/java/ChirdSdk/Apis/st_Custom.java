package ChirdSdk.Apis;

import android.graphics.Bitmap;

public class st_Custom {

    // control cmd
    public int ctrl = 0;

    public int[] value = new int[100];

    public int transtype;

    public int postion = 0;
    public int index = 0;
    public int outidx = 0;

    public float score = 0;

    public int outidx2;
    public float score2;

    public long timestamp = 0;

    public int flag = 0;
    public int totalnumber;
    public int splitnumber;
    public int format = 0;
    public int width = 0;
    public int height = 0;
    public int[] reserved = new int[10];

    // data frame
    public long Address = 0;
    public long pDataAddress = 0;
    public int datalen = 0;
    public Bitmap bitmap;

    public byte[] pData;

    public void CreateByteData(int length) {

        pData = new byte[length];
    }

}

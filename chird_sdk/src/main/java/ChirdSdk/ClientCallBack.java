package ChirdSdk;

import android.graphics.Bitmap;

import ChirdSdk.Apis.st_DateInfo;

public interface ClientCallBack {
    // 改变参数状态的类型
    public static final int PARAMCHANGE_TYPE_VIDEO_ABILITY = 0x00; // 相机性能
    public static final int PARAMCHANGE_TYPE_VIDEO_PARAME = 0x01; // 视频格式、分辨率、帧率
    public static final int PARAMCHANGE_TYPE_VIDEO_CTRL = 0x02; // 相机控制参数
    public static final int PARAMCHANGE_TYPE_AUDIO_PARAM = 0x03; // 音频参数
    public static final int PARAMCHANGE_TYPE_SERIAL_PARAM = 0x04; // 串口参数
    public static final int PARAMCHANGE_TYPE_GPIO_STATUS = 0x05; // GPIO方向或状态
    public static final int PARAMCHANGE_TYPE_VIDEO_ALLCTRL = 0x06; // 视频所有控制参数
    public static final int PARAMCHANGE_TYPE_VIDEO_ALL = 0X07;
    public static final int PARAMCHANGE_TYPE_VIDEO_H26X = 0X08;
    public static final int PARAMCHANGE_TYPE_NETCHN_NUM = 0X09;
    public static final int PARAMCHANGE_TYPE_VIDEO_CROP = 0X10;
    public static final int PARAMCHANGE_TYPE_SIGNAL_SNAP = 0X20; // 设备拍照按键信息
    public static final int PARAMCHANGE_TYPE_SIGNAL_RECORD = 0X21; // 设备录像按键信号
    public static final int PARAMCHANGE_TYPE_SIGNAL_FREEZE = 0X22; // 设备定格按键信号
    public static final int PARAMCHANGE_TYPE_ALARM_PARM = 0X23; // 报警参数变动

    /**
     * 设备状态改变回调函数
     *
     * @param changeType 改变的参数类型，见上面定义
     * @return null
     */
    void paramChangeCallBack(int changeType);

    /**
     * 设备断开连接回调函数
     *
     * @return null
     */
    void disConnectCallBack();

    /**
     * 拍照完成回调函数
     *
     * @param url    照片文件存储路径
     * @param bitmap 照片的微缩图
     * @param width  存储照片的宽度
     * @param height 存储照片的高度
     * @return null
     */
    void snapBitmapCallBack(String url, Bitmap bitmap, int width, int height);

    /**
     * 录像时间回调函数
     *
     * @param times 录像时间
     * @return null
     */
    void recordTimeCountCallBack(String times);

    /**
     * 录像结束返回结束时的微缩图回调函数
     *
     * @param url    录像文件存储路径
     * @param bitmap 录像微缩图
     * @return null
     */
    void recordStopBitmapCallBack(String url, Bitmap bitmap);

    /* 视频参数（格式、分辨率、帧率） */
    public static int VIDEO_FORMAT_YUYV = 0X01;
    public static int VIDEO_FORMAT_MJPEG = 0X02;
    public static int VIDEO_FORMAT_H264 = 0X03;
    public static int VIDEO_FORMAT_YUV420SP = 0X04;

    /**
     * 当前视频流回调函数
     *
     * @param bitmap 当前帧视频
     * @return null
     */
    void videoStreamBitmapCallBack(Bitmap bitmap);

    /**
     * 解码成 RGB565的视频数据，默认不回调，需要回调请调用 setCallbackVideoRGB565Data(true)函数开启
     *
     * @param format  视频格式
     * @param width   视频宽度
     * @param height  视频高度
     * @param datalen 数据长度
     * @param data    RGB565数据
     * @return null
     * //     * @see 不建议开启 ，如需开启，函数内不可执行复杂计算，否则会影响视频的解码，导致视频预览延时
     */
    void videoStreamDataCallBack(int format, int width, int height,
                                 int datalen, byte[] data);

    /**
     * 串口接收回调函数
     *
     * @param datalen 串口数据长度
     * @param data    串口数据
     * @return null
     */
    void serialDataCallBack(int datalen, byte[] data);

    /**
     * 音频接收回调函数(音频采集播放已内部做掉，默认不回调)
     *
     * @param datalen 音频数据长度
     * @param data    PCM数据
     * @return null
     */
    void audioDataCallBack(int datalen, byte[] data);

    /**
     * SD卡录像回调函数
     *
     * @param date   当前帧录像的时间
     * @param bitmap 回放视频帧
     * @return null
     */
    void playbackStreamBitmapCallBack(st_DateInfo date, Bitmap bitmap);

}

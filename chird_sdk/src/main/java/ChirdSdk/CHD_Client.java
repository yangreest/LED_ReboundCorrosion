package ChirdSdk;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.Semaphore;

import ChirdSdk.Apis.chd_wmp_apis;
import ChirdSdk.Apis.st_AlarmParam;
import ChirdSdk.Apis.st_AudioFrame;
import ChirdSdk.Apis.st_Custom;
import ChirdSdk.Apis.st_DateInfo;
import ChirdSdk.Apis.st_GpioInfo;
import ChirdSdk.Apis.st_StaStatus;
import ChirdSdk.Apis.st_StorageInfo;
import ChirdSdk.Apis.st_VideoAbilityInfo;
import ChirdSdk.Apis.st_VideoCtrlInfo;
import ChirdSdk.Apis.st_VideoFrame;
import ChirdSdk.Apis.st_VideoParamInfo;
import ChirdSdk.Apis.st_WirelessInfo;
import ChirdSdk.ChirdCoder.nativeCoder;
import ChirdSdk.ChirdCoder.st_VideoDecFrame;

public class CHD_Client {
    /**
     * 错误返回值
     */
    public static final int RET_ERROR_SUCCESS = 0;
    public static final int RET_ERROR_RET_FAILED = -1;
    public static final int RET_ERROR_RET_TIMEOUT = -2;
    public static final int RET_ERROR_RET_NET_TIMEOUT = -4;
    public static final int RET_ERROR_RET_DEVICE_OFFLINE = -6;
    public static final int RET_ERROR_RET_INVALID_HANDLE = -7;
    public static final int RET_ERROR_SESSIONID = -8;
    public static final int RET_ERROR_PROTOCOL = -9;
    public static final int RET_ERROR_NET_BIND = -10;
    public static final int RET_ERROR_RET_INVALID_ADDRESS = -11;
    public static final int RET_ERROR_RET_DEVICE_NOT_ONLINE = -12;
    public static final int RET_ERROR_PASSWD = -13;
    public static final int RET_ERROR_INVALID_PARAMETER = -15;
    public static final int RET_ERROR_PARAMETER_LENGHTH_OVERFLOW = -14;
    public static final int RET_ERROR_INITIALIZED_FAIL = -16;

    // 复用格式化对象，避免频繁创建
    private static final DecimalFormat SIZE_FORMAT = new DecimalFormat("#.00");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SS");

    private static final int SIGNAL_SNAP = 0X04;
    private static final int SIGNAL_DISCONNECT = 0X01;
    private static final int SIGNAL_RECORD_TIME = 0X02;
    private static final int SIGNAL_RECORD_STOP = 0X03;

    private static final int SIGNAL_PARAM_CHANGE = 0X05;

    /* global param */
    private long mHandle = 0;
    private long mSessionID = 0;
    private boolean isConnect = false;
    private nativeCoder mCoder = new nativeCoder();
    private chd_wmp_apis mClient = new chd_wmp_apis();
    private Semaphore videoDecSemp = new Semaphore(0);
    private Semaphore displaySemp = new Semaphore(0);

    private long mVideoJhandle, mVideoHhandle;
    private st_VideoDecFrame mDecFrame = new st_VideoDecFrame();

    private Handler mSignalHandler = null;
    private ClientCallBack mClientCallBack = null;

    private String mAddress = "";

    /**
     * 录像回放
     */
    private Boolean isPlayback = false;
    private Boolean isPlaybackAudio = false;
    private Bitmap sBitmap = null;
    private Semaphore playbackDecSemp = new Semaphore(0);
    private Semaphore playbackShowSemp = new Semaphore(0);
    private st_DateInfo playbackDate = new st_DateInfo();
    private Queues playbackQueue = new Queues();

    /**
     * video
     */
    private int mBitmapRotate = 0; // bitmap 旋转
    private int mBitmapVmirror = 0;// bitmap 垂直镜像
    private int mBitmapHmirror = 0;// bitmap 水平镜像

    private boolean isCallbackRGBDate = false;
    private Bitmap vBitmap = null;
    public boolean isOpenVideoStream = false;
    private int videoFps = 0, videoBps = 0;
    private Queues videoQueue = new Queues();

    /* snap record storage path */
    private String mStoragePath = null;
    private String mStorageFilePrefix = null;

    /**
     * snap shot
     */
    private int mSnapCount = 0;
    private String mSnapFileName = null;
    private String mSnapUrl = null;

    /**
     * record
     */
    private int mRecordTimes = 0;
    private boolean isRecord = false;
    private String mRecordUrl = null;
    private String mRecordFileName = null;
    private boolean isSupportRecord = false;
    private Handler mRecordTimeHandler = null;
    private Runnable RecordTimeRunnable = null;
    private int mRecordStatue = RECORD_STATUE_NONE;
    private static final int RECORD_STATUE_NONE = 0;
    private static final int RECORD_STATUE_START = 1;
    private static final int RECORD_STATUE_WRITEDATA = 2;
    private static final int RECORD_STATUE_STOP = 3;
    private Bitmap mRBitmap = null;
    private Boolean isGetRBitmap = false;

    private Boolean mSavePRThumbFlag = true;
    /**
     * 保存拍照录像微缩图配置文件标志位
     */
    private int mRBThumbWidth = 64;
    private int mRBThumbHeight = 64;

    /**
     * audio
     */
    private boolean isOpenAudio = false;
    private boolean isAudioTalk = false;
    private ChirdAudioUnit audioUnit = null;

    /**
     * serial
     */
    private boolean isOpenSerial = false;

    /**
     * gpio
     */
    private boolean bGpioget = false;
    private st_GpioInfo mGpioInfo = new st_GpioInfo();

    public CHD_Client() {

        mDecFrame.maxLength = 1;
        vBitmap = Bitmap.createBitmap(640, 480, Config.RGB_565);
        mDecFrame.pDataAddress = mCoder.chird_videomen_malloc(mDecFrame.maxLength);
        mVideoJhandle = mCoder.chird_vdec_create(mCoder.CODE_FMT_MJPEG, mCoder.CODE_PIXEL_FMT_RGB565);
        mVideoHhandle = mCoder.chird_vdec_create(mCoder.CODE_FMT_H264, mCoder.CODE_PIXEL_FMT_RGB565);

        /** 录像时间统计定时器 */
        mRecordTimeHandler = new Handler();
        RecordTimeRunnable = new Runnable() {
            public void run() {
                _chird_send_message(SIGNAL_RECORD_TIME, ++mRecordTimes, 0, null);
                mRecordTimeHandler.postDelayed(this, 1000);
            }
        };/* end RecordTimeRunnable */

        /** 消息通知 */
        mSignalHandler = new Handler() {
            public void handleMessage(Message msg) {

                if (mClientCallBack != null) {
                    switch (msg.what) {
                        case SIGNAL_DISCONNECT:
                            mClientCallBack.disConnectCallBack();
                        case SIGNAL_PARAM_CHANGE:
                            mClientCallBack.paramChangeCallBack(msg.arg1);
                            break;
                        case SIGNAL_RECORD_STOP:

                            /* record thumbnail Bitmap CallBack */
                            if (mRBitmap != null) {
                                mClientCallBack.recordStopBitmapCallBack((String) msg.obj, mRBitmap);

                                if (mSavePRThumbFlag) {
                                    mClient.CHD_AlbumManage_SaveVideoThumbnail((String) msg.obj, mRecordTimes, mRBitmap);
                                }
                            }

                            mRecordTimes = 0;
                            break;
                        case SIGNAL_RECORD_TIME:
                            mClientCallBack.recordTimeCountCallBack(getRecordTimerString(msg.arg1));
                            break;
                        case SIGNAL_SNAP:
                            /* snap thumbnail Bitmap CallBack */
                            Bitmap bitmap = null;
                            synchronized (this) {
                                if (vBitmap != null) {
                                    bitmap = GenerateThumbnail(vBitmap, mRBThumbWidth, mRBThumbHeight);
                                }
                            }
                            if (bitmap != null) {
                                mClientCallBack.snapBitmapCallBack((String) msg.obj, bitmap, msg.arg1, msg.arg2);

                                if (mSavePRThumbFlag) {
                                    mClient.CHD_AlbumManage_SavePictureThumbnail((String) msg.obj, msg.arg1, msg.arg2,
                                            bitmap);
                                }
                            }

                            break;
                    }
                }
            }
        }; /* end mSignalHandler */

        /** 音频采集与播放类 */
        audioUnit = new ChirdAudioUnit(new ChirdAudioUnit.callBack() {

            @Override
            public void recordCallBack(byte[] data, int length) {
                // TODO Auto-generated method stub
                int rn = mClient.CHD_WMP_Audio_SendData(mHandle, st_AudioFrame.G726, data, length);
                // Log.v("test", "send audio rn = " + rn);
            }
        });

    }

    /**
     * 用于线程间消息发送
     */
    private void _chird_send_message(int signal, int arg1, int arg2, String sarg) {
        Message message = new Message();
        message.what = signal;
        message.arg1 = arg1;
        message.arg2 = arg2;
        message.obj = sarg;
        mSignalHandler.sendMessage(message);
    }

    /**
     * 设置回调函数 (必须调用)
     *
     * @param null
     * @return null
     */
    public void setClientCallBack(ClientCallBack callback) {
        mClientCallBack = callback;
    }

    /**
     * 获取SDK版本号
     *
     * @param null
     * @return string:sdk版本号
     */
    public String getSdkVersion() {

        int version = mClient.CHD_WMP_GetSdkVersion();
        return Long.toString(version >> 16) + "." + Long.toString((version & 0x0000ff00) >> 8) + "."
                + Long.toString(version & 0x000000ff);
    }

    /**
     * 设置是否回调视频解码后的RGB565数据给应用层使用
     *
     * @param null
     * @return null
     */
    public void setCallbackVideoRGB565Data(boolean iscallback) {
        isCallbackRGBDate = iscallback;
    }

    /**
     * 设备是否连接
     *
     * @param null
     * @return true(已连接) false(未连接)
     */
    public boolean isConnect() {
        return isConnect;
    }

    /**
     * 连接设备
     *
     * @param address 设备地址
     * @param passwd  连接密码
     * @return 见错误返回值
     */
        public int connectDevice(String address, String passwd) {
    
            if (isConnect) {
                Log.w("CHD_Client", "[connectDevice] already connected, skip");
                return RET_ERROR_SUCCESS;
            }
    
            Log.i("CHD_Client", "[connectDevice] connecting... address=" + address);
            mHandle = mClient.CHD_WMP_ConnectDevice(address.trim(), passwd.trim());
            Log.i("CHD_Client", "[connectDevice] handle=" + mHandle + " (handle<0 means failed)");
            if (mHandle < 0) {
                Log.e("CHD_Client", "[connectDevice] FAILED! error=" + mHandle);
                return (int) mHandle;
            }
    
            mSessionID++;
            isConnect = true;
            mBitmapRotate = 0;
            mBitmapVmirror = 0;
            mBitmapHmirror = 0;
            isPlayback = false;
            mAddress = address.trim();
            
            Log.i("CHD_Client", "[connectDevice] SUCCESS! sessionId=" + mSessionID);
            Log.i("CHD_Client", "[connectDevice] starting threads...");
            
            /** 视音频、串口、GPIO、参数变动监听线程 */
            _chird_date_poll_thread pollthread = new _chird_date_poll_thread();
    
            /** 视频解码线程 */
            _chird_video_decoder_thread videodecthread = new _chird_video_decoder_thread();
    
            /** 视频显示线程 */
            _chird_video_show_thread videoshowthread = new _chird_video_show_thread();
    
            /** 录像回放解码线程 */
            _chird_playback_decoder_thread playbackdecthread = new _chird_playback_decoder_thread();
    
            /** 录像回放显示线程 */
            _chird_playback_show_thread playbackshowthread = new _chird_playback_show_thread();
    
            pollthread.start();
            videodecthread.start();
            videoshowthread.start();
    
            playbackdecthread.start();
            playbackshowthread.start();
            
            Log.i("CHD_Client", "[connectDevice] all threads started");
    
            return RET_ERROR_SUCCESS;
        }

    /**
     * 断开设备连接
     *
     * @param null
     * @return 见错误返回值
     */
    public int disconnectDevice() {
        Log.i("CHD_Client", "[disconnectDevice] disconnecting... isConnect=" + isConnect);
        if (!isConnect) {
            Log.w("CHD_Client", "[disconnectDevice] already disconnected");
            return RET_ERROR_SUCCESS;
        }
        mClient.CHD_WMP_Disconnect(mHandle);
        mHandle = 0;
        isConnect = false;
        isRecord = false;
        isOpenAudio = false;
        isAudioTalk = false;
        isOpenVideoStream = false;
        Log.i("CHD_Client", "[disconnectDevice] SUCCESS");

        return RET_ERROR_SUCCESS;
    }

    /**
     * 是否打开视频流
     *
     * @param null
     * @return true(已打开) false(未打开)
     */
    public boolean isOpenVideoStream() {
        if (!isConnect) {
            return false;
        }
        return isOpenVideoStream;
    }

    /**
     * 打开视频流
     *
     * @param null
     * @return 见错误返回值
     */
    public int openVideoStream() {
        if (!isConnect) {
            Log.e("CHD_Client", "[openVideoStream] FAILED! not connected");
            return -1;
        }

        Log.i("CHD_Client", "[openVideoStream] opening... handle=" + mHandle);
        int ret = mClient.CHD_WMP_Video_Begin(mHandle);
        Log.i("CHD_Client", "[openVideoStream] ret=" + ret + " (ret<0 means failed)");
        if (ret < 0) {
            Log.e("CHD_Client", "[openVideoStream] FAILED! error=" + ret);
            return ret;
        }

        mSnapCount = 0;
        isOpenVideoStream = true;
        mRecordStatue = RECORD_STATUE_NONE;
        Log.i("CHD_Client", "[openVideoStream] SUCCESS! isOpenVideoStream=" + isOpenVideoStream);

        return 0;
    }

    /**
     * 关闭视频流
     *
     * @param null
     * @return 见错误返回值
     */
    public int closeVideoStream() {
        if (!isConnect) {
            Log.e("CHD_Client", "[closeVideoStream] FAILED! not connected");
            return -1;
        }

        Log.i("CHD_Client", "[closeVideoStream] closing... handle=" + mHandle);
        int ret = mClient.CHD_WMP_Video_End(mHandle);
        Log.i("CHD_Client", "[closeVideoStream] ret=" + ret);
        if (ret < 0) {
            Log.e("CHD_Client", "[closeVideoStream] FAILED! error=" + ret);
            return ret;
        }

        /* if in the record, you must stop record */
        if (isRecord) {
            stopRecord();
        }

        isOpenVideoStream = false;
        Log.i("CHD_Client", "[closeVideoStream] SUCCESS! isOpenVideoStream=" + isOpenVideoStream);
        return 0;
    }

    /**
     * 设置拍照和录像存储路径-路径最后必须带'/'(未指定拍照录像名称则默认以当前毫秒时间命名)
     * <p>
     * 注意：初始化要提前设置路径，否则按键拍照录像将不会生成文件
     *
     * @param path 拍照录像存储路径
     * @return null
     */
    public void setStoragePath(String path) {
        mStoragePath = path;
    }

    /**
     * 设置拍照录像文件文件名命名前缀
     *
     * @param prefix 拍照录像文件名前缀
     * @return null
     */
    public void setStorageFilePrefix(String prefix) {
        mStorageFilePrefix = prefix;
    }

    /**
     * 拍照：优先从设备中传一张JPEG图上来(照片名称为上面设置的拍照路径+当前时间命名)，如果设备获取照片失败才从视频流里面拿(传入的filename 命名)
     *
     * @param filename 照片文件名称,可输入null,默认自动以时间命名照片名称
     * @return -1:失败 0:成功，调函数中会返回拍照微缩图
     */
    public int snapShot(String filename) {
        if (!isConnect) {
            return -1;
        }

        String pname = getDeviceProductName();
        if (pname.contains("CHD-A")) {
            mSnapCount++;
            return 0;
        }

        mSnapFileName = filename;
        if (mClient.CHD_WMP_Video_GetFormat(mHandle) == VIDEO_FORMAT_YUYV) {
            mSnapCount++;
            return 0;
        }

        if (mClient.CHD_WMP_Video_SnapShot(mHandle) < 0) {
            mSnapCount++;
        }

        return 0;
    }

    /**
     * 按照分辨率拍照(必须是相机支持的分辨率,必须设定拍照存储路径：setStoragePath()函数，拍照成功自动以时间命名存储到次路径下)
     *
     * @param width  宽
     * @param height 高
     * @return 见错误返回值 (成功回调函数中会返回拍照微缩图)
     */
    public int snapShotResolu(int width, int height) {
        if (!isConnect) {
            return -1;
        }

        if (mClient.CHD_WMP_Video_GetFormat(mHandle) == VIDEO_FORMAT_YUYV) {
            return -1;
        }

        return mClient.CHD_WMP_Video_SnapShotResolu(mHandle, width, height);
    }

    /**
     * 拍照从视频流里面取图片(必须设定拍照存储路径：setStoragePath()函数，拍照成功自动以时间命名存储到次路径下)
     *
     * @param null
     * @return null
     */
    public int snapShotFromVideoStream() {
        if (!isConnect) {
            return -1;
        }

        mSnapCount++;

        return 0;
    }

    /**
     * 最大分辨率拍照(必须设定拍照存储路径：setSnapSavePath()函数，拍照成功自动以时间命名存储到次路径下)
     *
     * @param null
     * @return 见错误返回值 (成功回调函数中会返回拍照微缩图)
     */
    public int snapShotMaxResolu() {
        if (!isConnect) {
            return -1;
        }

        if (mClient.CHD_WMP_Video_GetFormat(mHandle) == VIDEO_FORMAT_YUYV) {
            return -1;
        }

        int width = 0, height = 0;
        st_VideoAbilityInfo abi = new st_VideoAbilityInfo();
        mClient.CHD_WMP_Video_GetAbility(mHandle, abi);

        for (int i = 0; i < abi.ResoluNum; i++) {
            if (abi.width[i] >= width) {
                width = abi.width[i];
                height = height >= abi.height[i] ? height : abi.height[i];
            }
        }

        return mClient.CHD_WMP_Video_SnapShotResolu(mHandle, width, height);
    }

    /**
     * 是否正在录像
     *
     * @param null
     * @return false(未录像) true(正在录像)
     */
    public boolean isRecord() {
        isRecord = isConnect == false ? false : isRecord;

        return isRecord;
    }

    /**
     * 开始录像
     *
     * @param filename 录像文件名称(不需要带录像文件名后缀，自动根据格式录像生产文件后缀) - 可输入null 生产以时间命名的录像文件名
     * @return 见返回值
     */
    public int startRecord(String filename) {
        if (isRecord) {
            return 0;
        }
        if (!isConnect || !isOpenVideoStream || !isSupportRecord) {
            return -1;
        }

        isRecord = true;
        mRecordTimes = 0;
        mRecordFileName = filename;
        mRecordStatue = RECORD_STATUE_START;

        /* h264 video stream record I must be the first frame */
        int format = mClient.CHD_WMP_Video_GetFormat(mHandle);
        if (format == VIDEO_FORMAT_H264) {
            setVideoH264ForceI();
        }

        /* open record timer */
        mRecordTimeHandler.postDelayed(RecordTimeRunnable, 1000);
        if (mClientCallBack != null) {
            mClientCallBack.recordTimeCountCallBack("00:00");
        }

        return 0;
    }

    /**
     * 停止录像
     *
     * @param null
     * @return 见返回值(录像结束会在回掉中返回结束时的录像微缩图)
     */
    public int stopRecord() {
        if (!isRecord || !isConnect) {
            return 0;
        }

        isRecord = false;
        mRecordStatue = RECORD_STATUE_STOP;

        return 0;
    }

    public static final int NET_TRANSMODE_TCP = 1; // TCP 连接
    public static final int NET_TRANSMODE_P2P = 2; // P2P 直连
    public static final int NET_TRANSMODE_RLY = 3; // 服务器转发

    /**
     * 获取网络传输模式
     *
     * @param null
     * @return -1:获取失败 >0:见传输模式
     */
    public int getTransMode() {
        if (!isConnect) {
            return -1;
        }
        return mClient.CHD_WMP_GetTransMode(mHandle);
    }

    /**
     * 检测设备是否在线(仅检测远程网络设备) 注:设备不在线的情况下本函数检测时间需要2s，远程设备掉线状态更新5分钟
     *
     * @param did :设备远程 DID 码
     * @return < 0: 设备不在线 > 0: 设备在线
     */
    public int getDeviceOnlineStatus(String did) {
        if (!isConnect) {
            return -1;
        }
        return mClient.CHD_WMP_DeviceIsOnline(did);
    }

    /**
     * 获取连接地址
     *
     * @param null
     * @return ""(获取失败)
     */
    public String getConnectAddress() {
        if (!isConnect) {
            return "";
        }

        return mAddress;
    }

    /**
     * 获取连接密码
     *
     * @param null
     * @return null(获取失败)
     */
    public String getConnectPasswd() {
        if (!isConnect)
            return null;

        return mClient.CHD_WMP_GetEncrypt(mHandle);
    }

    /**
     * 修改设备连接密码
     *
     * @param passwd 设备连接密码
     * @return 见错误返回值
     */
    public int setConnectPasswd(String passwd) {
        if (!isConnect || passwd == null) {
            return -1;
        }
        return mClient.CHD_WMP_SetEncrypt(mHandle, passwd);
    }

    /**
     * 获取设备版本号
     *
     * @param null
     * @return null:获取失败 string:成功返回设备版本号
     */
    public String getDeviceVersion() {
        if (!isConnect) {
            return null;
        }

        int version = mClient.CHD_WMP_GetVersion(mHandle);

        return Long.toString(version >> 16) + "." + Long.toString((version & 0x0000ff00) >> 8) + "."
                + Long.toString(version & 0x000000ff);
    }

    /**
     * 获取设备版本号
     *
     * @param null
     * @return null:获取失败 string:成功返回设备版本号
     */
    public double getDeviceDigitalVersions() {
        double version = 0;
        if (!isConnect) {
            return version;
        }

        int ver = mClient.CHD_WMP_GetVersion(mHandle);

        version = (ver >> 16) + ((ver & 0x0000ff00) >> 8) / 10.0 + (ver & 0x000000ff) / 100.0;

        return version;
    }

    /**
     * 设备固件在线升级(森森定制专用)
     *
     * @param url : 固件存放服务器路径
     * @return null:获取失败 string:成功返回设备版本号
     */
    public int SenSenFirmwareUpdata(String url) {
        if (!isConnect) {
            return -1;
        }

        return mClient.CHD_WMP_Device_SenSenUpdata(mHandle, url);
    }

    /**
     * 获取设备 ID 号
     *
     * @param null
     * @return 见错误返回值
     */
    public int getDeviceId() {
        if (!isConnect)
            return -1;
        return mClient.CHD_WMP_Device_GetId(mHandle);
    }

    /**
     * 设置设备 ID 号
     *
     * @param id 设备 ID 号
     * @return 见错误返回值
     */
    public int setDeviceId(int id) {
        if (!isConnect)
            return -1;
        return mClient.CHD_WMP_Device_SetId(mHandle, id);
    }

    /**
     * 获取设备型号
     *
     * @param null
     * @return null:失败 string:设备型号
     */
    public String getDeviceProductName() {
        if (!isConnect) {
            return null;
        }

        return mClient.CHD_WMP_Device_GetProductName(mHandle);
    }

    /**
     * 获取设备名称
     *
     * @param null
     * @return null:失败 string:设备别名
     */
    public String getDeviceAlias() {
        if (!isConnect) {
            return null;
        }

        return mClient.CHD_WMP_Device_GetAlias(mHandle);
    }

    /**
     * 设置设备名称
     *
     * @param alias 设备名称
     * @return 见错误返回值
     */
    public int setDeviceAlias(String alias) {
        if (!isConnect) {
            return -1;
        }
        return mClient.CHD_WMP_Device_SetAlias(mHandle, alias);
    }

    /**
     * 获取设备远程DID
     *
     * @param null
     * @return null:失败 string:设备远程连接DID
     */
    public String getDeviceDID() {
        if (!isConnect) {
            return null;
        }

        return mClient.CHD_WMP_Device_GetDID(mHandle);
    }

    /**
     * 设备重启
     *
     * @param null
     * @return 见错误返回值
     */
    public int rebootDevice() {
        if (!isConnect)
            return -1;
        int ret = mClient.CHD_WMP_Device_Reboot(mHandle);
        if (ret < 0) {
            return ret;
        }

        disconnectDevice();

        return 0;
    }

    /**
     * 设备复位
     *
     * @param null
     * @return 见错误返回值
     */
    public int resetDevice() {
        if (!isConnect) {
            return -1;
        }
        return mClient.CHD_WMP_Device_Reset(mHandle);
    }

    /**
     * 获取设备的 Mac 地址
     *
     * @param null
     * @return null(失败) string(设备 mac 地址)
     */
    public String getMacAddress() {
        if (!isConnect) {
            return null;
        }

        return mClient.CHD_WMP_GetMac(mHandle);
    }

    /**
     * 获取设备中的公司名称字符串
     *
     * @param null
     * @return null(失败) string(公司名称字符串)
     */
    public String getCompany() {
        if (!isConnect)
            return null;

        return mClient.CHD_WMP_GetCompany(mHandle);
    }

    public static final int DEVICE_WIRELESS_MODE_AP = 1; // 设备发出 WIFI
    public static final int DEVICE_WIRELESS_MODE_STA = 2; // 设备连接到路由器

    /**
     * 获取当前设备网络连接类型
     *
     * @param null
     * @return < 0(见错误返回值) >=0(设备网络连接模式)
     */
    public int getDeviceWirelessMode() {
        if (!isConnect)
            return -1;
        return mClient.CHD_WMP_Wireless_GetNetType(mHandle);
    }

    /**
     * 获取设备当前的wifi名称
     *
     * @param null
     * @return null(获取失败) string(设备wifi名称)
     */
    public String getApName() {
        if (!isConnect) {
            return null;
        }
        st_WirelessInfo param = new st_WirelessInfo();
        int ret = mClient.CHD_WMP_Wireless_GetApInfo(mHandle, param);
        if (ret < 0) {
            return null;
        }

        return param.apssid;
    }

    /**
     * 获取设备当前的 WIFI 连接密码
     *
     * @param null
     * @return null(获取失败) string(设备wifi连接密码)
     */
    public String getApPasswd() {
        if (!isConnect) {
            return null;
        }
        st_WirelessInfo param = new st_WirelessInfo();

        int ret = mClient.CHD_WMP_Wireless_GetApInfo(mHandle, param);
        if (ret < 0) {
            return null;
        }

        return param.apkey;
    }

    /**
     * 设置设备当前的wifi名称(不管当前是AP模式还是STA模式，只要设置了，设备就会变成AP模式)
     *
     * @param name   设备wifi名称
     * @param passwd 设备wifi密码
     * @return 见错误返回值
     */
    public int setApInfo(String name, String passwd) {
        if (!isConnect || name == null || passwd == null) {
            return -1;
        }
        st_WirelessInfo param = new st_WirelessInfo();
        param.apssid = name;
        param.apkey = passwd;

        return mClient.CHD_WMP_Wireless_SetApInfo(mHandle, param);
    }

    /**
     * 获取设备连接路由器的帐号
     *
     * @param null
     * @return null(获取失败) string(路由器的帐号)
     */
    public String getStaName() {
        if (!isConnect) {
            return null;
        }
        st_WirelessInfo param = new st_WirelessInfo();

        int ret = mClient.CHD_WMP_Wireless_GetStaInfo(mHandle, param);
        if (ret < 0) {
            return null;
        }

        return param.stassid;
    }

    /**
     * 获取设备连接路由器的密码
     *
     * @param null
     * @return null(获取失败) string(路由器的密码)
     */
    public String getStaPasswd() {
        if (!isConnect) {
            return null;
        }
        st_WirelessInfo param = new st_WirelessInfo();

        int ret = mClient.CHD_WMP_Wireless_GetStaInfo(mHandle, param);
        if (ret < 0) {
            return null;
        }

        return param.stakey;
    }

    /**
     * 获取设备连接路由器状态(仅支持版本6.0以上的设备)
     *
     * @param stStatus 连接路由器状态(sta 使能、sta 连接路由器的状态、被分配到的 IP 地址、连接路由器的帐号和密码)
     * @return null(获取失败) string(路由器的密码)
     */
    public int getStaStatus(st_StaStatus stStatus) {
        if (!isConnect) {
            return -1;
        }

        if (stStatus == null) {
            return -2;
        }

        return mClient.CHD_WMP_Wireless_GetStaStatus(mHandle, stStatus);
    }

    /**
     * 设置设备连接路由器(不管当前是 AP 模式还是 STA 模式，只要设置了，设备就会变成连接路由器)
     *
     * @param name   路由器帐号
     * @param passwd 路由器密码
     * @return 见错误返回值
     */
    public int setStaInfo(String name, String passwd) {
        if (!isConnect) {
            return -1;
        }
        st_WirelessInfo param = new st_WirelessInfo();

        param.stassid = name;
        param.stakey = passwd;

        return mClient.CHD_WMP_Wireless_SetStaInfo(mHandle, param);
    }

    /**
     * 获取设备相机支持的视频格式个数
     *
     * @param null
     * @return < 0(见错误返回值)
     */
    public int Video_getAbiFormatNum() {
        if (!isConnect) {
            return -1;
        }
        st_VideoAbilityInfo abi = new st_VideoAbilityInfo();
        int ret = mClient.CHD_WMP_Video_GetAbility(mHandle, abi);
        if (ret < 0) {
            return ret;
        }

        return abi.FormatNum;
    }

    /**
     * 获取相机所支持的第n个视频格式(YUYV MJPEG H264等)
     *
     * @param null
     * @return null（失败） string(视频格式)
     */
    public String Video_getAbiFormat(int Cnt) {
        if (!isConnect) {
            return null;
        }
        st_VideoAbilityInfo abi = new st_VideoAbilityInfo();
        int ret = mClient.CHD_WMP_Video_GetAbility(mHandle, abi);
        if (ret < 0 || abi.FormatNum < Cnt) {
            return null;
        }

        return abi.GetFormatString(abi.format[Cnt]);
    }

    /**
     * 获取相机所支持的第n个视频格式(YUYV MJPEG H264等)
     *
     * @param null
     * @return null（失败） int(视频格式)
     */
    public int Video_getAbiFormatInt(int Cnt) {
        if (!isConnect) {
            return -1;
        }
        st_VideoAbilityInfo abi = new st_VideoAbilityInfo();
        int ret = mClient.CHD_WMP_Video_GetAbility(mHandle, abi);
        if (ret < 0 || abi.FormatNum < Cnt) {
            return -1;
        }

        return abi.format[Cnt];
    }

    /**
     * 获取设备相机当前格式下所支持的分辨率个数
     *
     * @param null
     * @return < 0(见错误返回值)
     */
    public int Video_getAbiResoluNum() {
        if (!isConnect) {
            return -1;
        }
        st_VideoAbilityInfo abi = new st_VideoAbilityInfo();
        int ret = mClient.CHD_WMP_Video_GetAbility(mHandle, abi);
        if (ret < 0) {
            return ret;
        }

        return abi.ResoluNum;
    }

    /**
     * 获取相机当前格式下所支持的第n个视频分辨率
     *
     * @param null
     * @return null（失败） string(分辨率)
     */
    public String Video_getAbiResolu(int Cnt) {
        if (!isConnect)
            return null;
        st_VideoAbilityInfo abi = new st_VideoAbilityInfo();
        int ret = mClient.CHD_WMP_Video_GetAbility(mHandle, abi);
        if (ret < 0 || abi.ResoluNum < Cnt)
            return null;

        return abi.GetResoluString(abi.width[Cnt], abi.height[Cnt]);
    }

    /* 视频参数（格式、分辨率、帧率） */
    public static int VIDEO_FORMAT_YUYV = 0X01;
    public static int VIDEO_FORMAT_MJPEG = 0X02;
    public static int VIDEO_FORMAT_H264 = 0X03;
    public static int VIDEO_FORMAT_YUV420SP = 0X04;
    public static int VIDEO_FORMAT_RGB565 = 44;

    /**
     * 获取当前相机所设置的格式
     *
     * @param null
     * @return < 0(见错误返回值) >= 0(见上面的视频格式定义)
     */
    public int Video_getFormat() {
        if (!isConnect) {
            return -1;
        }

        return mClient.CHD_WMP_Video_GetFormat(mHandle);
    }

    /**
     * 获取当前相机所设置的格式String
     *
     * @param null
     * @return ""(失败) String(视频格式)
     */
    public String Video_getStringFormat() {
        if (!isConnect) {
            return "";
        }

        st_VideoAbilityInfo abi = new st_VideoAbilityInfo();
        return abi.GetFormatString(mClient.CHD_WMP_Video_GetFormat(mHandle));
    }

    /**
     * 获取当前相机所设置的视频分辨率
     *
     * @param null
     * @return ""(获取失败) String(视频分辨率)
     */
    public String Video_getResolu() {
        if (!isConnect) {
            return "";
        }

        st_VideoParamInfo param = new st_VideoParamInfo();
        int ret = mClient.CHD_WMP_Video_GetResolu(mHandle, param);
        if (ret < 0) {
            return "";
        }

        st_VideoAbilityInfo abi = new st_VideoAbilityInfo();
        return abi.GetResoluString(param.width, param.height);
    }

    /**
     * 获取当前相机所设置的视频宽度
     *
     * @param null
     * @return < 0(见错误返回值) >0(视频宽)
     */
    public int Video_getResoluWidth() {
        if (!isConnect) {
            return -1;
        }

        st_VideoParamInfo param = new st_VideoParamInfo();
        int ret = mClient.CHD_WMP_Video_GetResolu(mHandle, param);
        if (ret < 0) {
            return ret;
        }

        return param.width;
    }

    /**
     * 获取当前相机所设置的视频高度
     *
     * @param null
     * @return < 0(见错误返回值) >0(视频高)
     */
    public int Video_getResoluHeight() {
        if (!isConnect) {
            return -1;
        }

        st_VideoParamInfo param = new st_VideoParamInfo();
        int ret = mClient.CHD_WMP_Video_GetResolu(mHandle, param);
        if (ret < 0) {
            return ret;
        }

        return param.height;
    }

    /**
     * 获取当前相机所设置的视频采集帧率
     *
     * @param null
     * @return < 0(见错误返回值) >0(视频采集帧率)
     */
    public int Video_getFps() {
        if (!isConnect) {
            return -1;
        }
        return mClient.CHD_WMP_Video_GetFPS(mHandle);
    }

    /**
     * 获取当前相机所支持设置的最大帧率
     *
     * @param null
     * @return < 0(见错误返回值) >0(最大帧率)
     */
    public int Video_getMaxFps() {
        if (!isConnect) {
            return -1;
        }
        int maxfps = -1;
        st_VideoParamInfo param = new st_VideoParamInfo();
        int ret = mClient.CHD_WMP_Video_GetParam(mHandle, param);
        if (ret >= 0) {
            maxfps = param.maxfps;
        }
        return maxfps;
    }

    /**
     * 设置相机视频格式
     *
     * @param format 相机视频格式(必须是相机所支持的)
     * @return < 0(见错误返回值)
     */
    public int Video_setFormat(int format) {
        if (!isConnect) {
            return -1;
        }
        return mClient.CHD_WMP_Video_SetFormat(mHandle, format);
    }

    /**
     * 设置相机视频分辨率
     *
     * @param format 相机视频分辨率(必须是相机所支持的)
     * @return < 0(见错误返回值)
     */
    public int Video_setResolu(int width, int height) {
        if (!isConnect) {
            return -1;
        }
        return mClient.CHD_WMP_Video_SetResolu(mHandle, width, height);
    }

    /**
     * 设置相机视频采集帧率
     *
     * @param format 相机视频采集帧率(必须是相机所支持的)
     * @return < 0(见错误返回值)
     */
    public int Video_setFps(int fps) {
        if (!isConnect) {
            return -1;
        }
        return mClient.CHD_WMP_Video_SetFPS(mHandle, fps);
    }

    /* camera control param type */
    public static int VIDEO_CTRL_TYPE_BRIGHTNESS = 0; // 亮度
    public static int VIDEO_CTRL_TYPE_CONTRAST = 1; // 对比度
    public static int VIDEO_CTRL_TYPE_SATURATION = 2; // 饱和度
    public static int VIDEO_CTRL_TYPE_HUE = 3; // 色调
    public static int VIDEO_CTRL_TYPE_WHITE_BALANCE = 4; // 白平衡
    public static int VIDEO_CTRL_TYPE_GAMMA = 5; // 伽马
    public static int VIDEO_CTRL_TYPE_GAIN = 6; // 增益
    public static int VIDEO_CTRL_TYPE_SHARPNESS = 7; // 清晰度
    public static int VIDEO_CTRL_TYPE_BACKLIGH = 8; // 背光补偿
    public static int VIDEO_CTRL_TYPE_EXPOSURE = 9; // 曝光值

    /**
     * 获取相机相应控制参数是否支持自动设置
     *
     * @param CtrlType 相机控制参数，见上面定义
     * @return false(不支持自动控制) true(支持自动控制)
     */
    public boolean VCtrl_isSupportAutoCtrl(int CtrlType) {
        if (!isConnect) {
            return false;
        }

        st_VideoCtrlInfo vctrl = new st_VideoCtrlInfo();
        int ret = mClient.CHD_WMP_Video_GetVideoCtrl(mHandle, CtrlType, vctrl);
        if (ret < 0) {
            return false;
        }

        return vctrl.auto_valid == 1 ? true : false;
    }

    /**
     * 获取相机相应控制参数是否当前是否开启自动控制
     *
     * @param CtrlType 相机控制参数，见上面定义
     * @return false(手动控制) true(自动控制)
     */
    public boolean VCtrl_isAutoCtrl(int CtrlType) {
        if (!isConnect) {
            return false;
        }
        st_VideoCtrlInfo vctrl = new st_VideoCtrlInfo();
        int ret = mClient.CHD_WMP_Video_GetVideoCtrl(mHandle, CtrlType, vctrl);
        if (ret < 0) {
            return false;
        }

        return (vctrl.auto_valid == 1 && vctrl.autoval == 1) ? true : false;
    }

    /**
     * 设置相机相应控制参数自动控制
     *
     * @param CtrlType 相机控制参数，见上面定义
     * @return 见错误返回值
     */
    public int VCtrl_setAutoCtrl(int CtrlType, int Auto) {
        if (!isConnect) {
            return -1;
        }

        st_VideoCtrlInfo vctrl = new st_VideoCtrlInfo();
        int ret = mClient.CHD_WMP_Video_GetVideoCtrl(mHandle, CtrlType, vctrl);

        if (ret < 0 || vctrl.auto_valid == 0) {
            Log.v("test", "CHD_WMP_Video_GetVideoCtrl ret = " + ret + " auto_valid:" + vctrl.auto_valid);
            return -1;
        }

        vctrl.autoval = Auto;
        return mClient.CHD_WMP_Video_SetVideoCtrl(mHandle, CtrlType, vctrl);
    }

    /**
     * 获取相机相应控制参数是否支持手动调节参数值
     *
     * @param CtrlType 相机控制参数，见上面定义
     * @return false(不支持手动调节参数值) true(支持手动调节参数值)
     */
    public boolean VCtrl_isSupportValueCtrl(int CtrlType) {
        if (!isConnect) {
            return false;
        }
        st_VideoCtrlInfo vctrl = new st_VideoCtrlInfo();
        int ret = mClient.CHD_WMP_Video_GetVideoCtrl(mHandle, CtrlType, vctrl);
        if (ret < 0) {
            return false;
        }

        return vctrl.val_valid == 1 ? true : false;
    }

    /**
     * 获取相机相应控制参数手动调节最大设置值
     *
     * @param CtrlType 相机控制参数，见上面定义
     * @return <0(见错误定义码) >= 0(最大设置值)
     */
    public int VCtrl_getMaxValue(int CtrlType) {
        if (!isConnect) {
            return -1;
        }
        st_VideoCtrlInfo vctrl = new st_VideoCtrlInfo();
        int ret = mClient.CHD_WMP_Video_GetVideoCtrl(mHandle, CtrlType, vctrl);
        if (ret < 0) {
            return ret;
        }

        return vctrl.maxval;
    }

    /**
     * 获取相机相应控制参数手动调节最小设置值
     *
     * @param CtrlType 相机控制参数，见上面定义
     * @return 最小设置值
     */
    public int VCtrl_getMinValue(int CtrlType) {
        if (!isConnect) {
            return -1;
        }
        st_VideoCtrlInfo vctrl = new st_VideoCtrlInfo();
        int ret = mClient.CHD_WMP_Video_GetVideoCtrl(mHandle, CtrlType, vctrl);
        if (ret < 0) {
            return -1;
        }

        return vctrl.minval;
    }

    /**
     * 获取设备 OSD 使能(客户定制，对一般设备暂不开放)
     *
     * @param void
     * @return < 0 见错误返回值 >= 0 OSD使能
     */
    public int Video_getOSDEnable() {
        if (!isConnect) {
            return -1;
        }

        st_VideoParamInfo osdparam = new st_VideoParamInfo();
        int ret = mClient.CHD_WMP_Video_GetOSD(mHandle, osdparam);
        if (ret >= 0) {
            ret = osdparam.osd_enable;
        }

        return ret;
    }

    /**
     * 设置设备 OSD 使能(客户定制，对一般设备暂不开放)
     *
     * @param enable :设备 OSD 使能:0( 不显示 OSD) 1(显示 OSD)
     * @return 见错误返回值
     */
    public int Video_setOSDEnable(int enable) {
        if (!isConnect) {
            return -1;
        }

        st_VideoParamInfo osdparam = new st_VideoParamInfo();
        osdparam.osd_enable = enable > 0 ? 1 : 0;
        osdparam.osd_index = 0;
        osdparam.osd_x = -1;
        osdparam.osd_y = -1;

        return mClient.CHD_WMP_Video_SetOSD(mHandle, osdparam);
    }

    /**
     * 获取设备 TOSD(时间水印) 使能(客户定制，对一般设备暂不开放)
     *
     * @param void
     * @return < 0 见错误返回值 >= 0 TOSD使能
     */
    public int Video_getTOSDEnable() {
        if (!isConnect) {
            return -1;
        }

        st_VideoParamInfo osdparam = new st_VideoParamInfo();
        int ret = mClient.CHD_WMP_Video_GetTOSD(mHandle, osdparam);
        if (ret >= 0) {
            ret = osdparam.tosd_enable;
        }

        return ret;
    }

    /**
     * 设置设备 TOSD(时间水印) 使能(客户定制，对一般设备暂不开放)
     *
     * @param enable :设备时间水印显示 0( 不显示 OSD) 1(显示 OSD)
     * @return 见错误返回值
     */
    public int Video_setTOSDEnable(int enable) {
        if (!isConnect) {
            return -1;
        }

        st_VideoParamInfo osdparam = new st_VideoParamInfo();
        osdparam.tosd_enable = enable > 0 ? 1 : 0;
        osdparam.tosd_x = -1;
        osdparam.tosd_y = -1;

        return mClient.CHD_WMP_Video_SetTOSD(mHandle, osdparam);
    }

    /**
     * 获取相机相应控制参数当前设置值
     *
     * @param CtrlType 相机控制参数，见上面定义
     * @return 当前设置值
     */
    public int VCtrl_getCurValue(int CtrlType) {
        if (!isConnect) {
            return -1;
        }
        st_VideoCtrlInfo vctrl = new st_VideoCtrlInfo();
        int ret = mClient.CHD_WMP_Video_GetVideoCtrl(mHandle, CtrlType, vctrl);
        if (ret < 0) {
            return -1;
        }

        return vctrl.curval;
    }

    /**
     * 调节相机相应控制参数当前值(必须手动控制模式下才能设置)
     *
     * @param CtrlType 相机控制参数，见上面定义
     * @param Value    控制值
     * @return 见错误返回值
     */
    public int VCtrl_setCtrlValue(int CtrlType, int Value) {
        if (!isConnect) {
            return -1;
        }

        st_VideoCtrlInfo vctrl = new st_VideoCtrlInfo();
        int ret = mClient.CHD_WMP_Video_GetVideoCtrl(mHandle, CtrlType, vctrl);

        if (ret < 0 || vctrl.val_valid == 0) {
            return -1;
        }

        if (Value > vctrl.maxval) {
            Value = vctrl.maxval;
        } else if (Value < vctrl.minval) {
            Value = vctrl.minval;
        }

        vctrl.curval = Value;
        return mClient.CHD_WMP_Video_SetVideoCtrl(mHandle, CtrlType, vctrl);
    }

    /**
     * 获取相机所有控制参数复位
     *
     * @param null
     * @return 见错误返回值
     */
    public int VCtrl_Reset() {
        if (!isConnect) {
            return -1;
        }
        return mClient.CHD_WMP_Video_ResetVCtrl(mHandle);
    }

    /**
     * 设置设备视频旋转(必须设备支持情况下才有效)
     *
     * @param angle 视频旋转角度
     * @return 见错误返回值
     */
    public int setDeviceRotation(int angle) {
        if (!isConnect) {
            return -1;
        }

        return mClient.CHD_WMP_Video_SetVideoRotation(mHandle, angle);
    }

    /**
     * 设置设备视频水平镜像(必须设备支持情况下才有效)
     *
     * @param null
     * @return 见错误返回值
     */
    public int setDeviceHorizontalMirror() {
        if (!isConnect) {
            return -1;
        }

        st_VideoParamInfo mirror = new st_VideoParamInfo();
        int ret = mClient.CHD_WMP_Video_GetMirror(mHandle, mirror);
        if (ret < 0) {
            return ret;
        }

        mirror.horizontal = mirror.horizontal == 0 ? 1 : 0;

        return mClient.CHD_WMP_Video_SetMirror(mHandle, mirror);
    }

    /**
     * 设置设备视频垂直镜像(必须设备支持情况下才有效)
     *
     * @param null
     * @return 见错误返回值
     */
    public int setDeviceVerticalMirror() {
        if (!isConnect) {
            return -1;
        }

        st_VideoParamInfo mirror = new st_VideoParamInfo();

        int rn = mClient.CHD_WMP_Video_GetMirror(mHandle, mirror);
        if (rn < 0) {
            return rn;
        }

        mirror.vertical = mirror.vertical == 0 ? 1 : 0;

        return mClient.CHD_WMP_Video_SetMirror(mHandle, mirror);
    }

    /**
     * 设置设备视频镜像复位不管当前是水平镜像还是垂直镜像，都直接复位显示原始视频(必须设备支持情况下才有效)
     *
     * @param null
     * @return 见错误返回值
     */
    public int resetDeviceMirror() {
        if (!isConnect) {
            return -1;
        }

        st_VideoParamInfo mirror = new st_VideoParamInfo();

        mirror.vertical = 0;
        mirror.horizontal = 0;

        return mClient.CHD_WMP_Video_SetMirror(mHandle, mirror);
    }

    /**
     * 获取设备镜头翻转状态(必须设备支持情况下才有效,其实就是水平垂直镜像的组合)
     *
     * @param null
     * @return < 0 见错误返回值 0(正常) 1（翻转）
     */
    public int getDeviceLensFlip() {
        if (!isConnect) {
            return -1;
        }

        st_VideoParamInfo mirror = new st_VideoParamInfo();
        int rn = mClient.CHD_WMP_Video_GetMirror(mHandle, mirror);
        if (rn < 0) {
            return rn;
        }

        return (mirror.vertical == 0 || mirror.horizontal == 0) ? 0 : 1;
    }

    /**
     * 设置设备镜头翻转状态(必须设备支持情况下才有效,其实就是水平垂直镜像的组合)
     *
     * @param flip :镜头翻转 0(正常) 1（翻转）
     * @return 见错误返回值
     */
    public int setDeviceLensFlip(int flip) {
        if (!isConnect) {
            return -1;
        }

        st_VideoParamInfo mirror = new st_VideoParamInfo();
        mirror.vertical = (byte) (flip == 0 ? 0 : 1);
        mirror.horizontal = (byte) (flip == 0 ? 0 : 1);

        return mClient.CHD_WMP_Video_SetMirror(mHandle, mirror);
    }

    /**
     * 设置视频帧bitmap旋转角度
     *
     * @param rotate 解码后生成的bitmap旋转角度
     * @return null
     */
    public void setVideoBitmapRotate(int rotate) {
        mBitmapRotate = rotate;
    }

    /**
     * 设置视频帧bitmap是否进行软件水平镜像
     *
     * @param flag 0(不镜像) 1(镜像)
     * @return null
     */
    public void setVideoBitmapHorMirror(int flag) {
        mBitmapHmirror = flag;
    }

    /**
     * 设置视频帧bitmap是否进行软件垂直镜像
     *
     * @param flag 0(不镜像) 1(镜像)
     * @return null
     */
    public void setVideoBitmapVerMirror(int flag) {
        mBitmapVmirror = flag;
    }

    /**
     * 设置视频帧bitmap复位(不管是旋转了还是镜像了，都全部复位)
     *
     * @param void
     * @return null
     */
    public void resetVideoBitmap() {
        mBitmapRotate = 0;
        mBitmapHmirror = 0;
        mBitmapVmirror = 0;
    }

    /**
     * 获取设备H264视频传输I帧间隔(相机当前格式必须为H264，不支持H264的相机无效)
     *
     * @param null
     * @return <0(见错误返回值) >=0(I帧间隔，间隔越大码流越小，一般是20~240之间)
     */
    public int getVideoH264StreamGop() {
        if (!isConnect) {
            return -1;
        }

        return mClient.CHD_WMP_Video_GetH264KeyInter(mHandle);
    }

    /**
     * 设置设备H264视频传输I帧间隔(相机当前格式必须为H264，不支持H264的相机无效)
     *
     * @param value 视频I帧间隔(20~240),间隔越大码流越小
     * @return <0(见错误返回值)
     */
    public int setVideoH264StreamGop(int value) {
        if (!isConnect) {
            return -1;
        }
        return mClient.CHD_WMP_Video_SetH264KeyInter(mHandle, value);
    }

    /**
     * 获取设备H264视频传输画质(相机当前格式必须为H264，不支持H264的相机无效)
     *
     * @param null
     * @return <0(见错误返回值) >=0(画质越大码流越大，视频清晰度越好，但相应码流也越大)
     */
    public int getVideoH264StreamQpValue() {
        if (!isConnect) {
            return -1;
        }
        return mClient.CHD_WMP_Video_GetH264QpValue(mHandle);
    }

    /**
     * 设置设备H264视频传输画质(相机当前格式必须为H264，不支持H264的相机无效)
     *
     * @param value 画质(1~100)，画质越大码流越大，视频清晰度越好，但相应码流也越大
     * @return <0(见错误返回值) >=0(画质越大码流越大，视频清晰度越好，但相应码流也越大)
     */
    public int setVideoH264StreamQpValue(int value) {
        if (!isConnect) {
            return -1;
        }

        return mClient.CHD_WMP_Video_SetH264QpValue(mHandle, value);
    }

    /**
     * 强制申请I帧(相机当前格式必须为H264，不支持H264的相机无效)
     *
     * @param null
     * @return 见错误返回值
     */
    private int setVideoH264ForceI() {
        if (!isConnect) {
            return -1;
        }

        return mClient.CHD_WMP_Video_SetForceI(mHandle);
    }

    /**
     * 获取当前视频传输实际帧率(SDK接收实时统计)
     *
     * @param null
     * @return 视频传输实际帧率
     */
    public String getVideoFrameFps() {
        return String.valueOf(videoFps);
    }

    /**
     * 获取当前视频传输实际码流(SDK接收实时统计)
     *
     * @param null
     * @return 视频传输实际码流
     */
    public String getVideoFrameBps() {
        return SizeLongToString(videoBps) + "/s";
    }

    /**
     * 获取音频是否打开(设备音频->手机播放)
     *
     * @param null
     * @return false(未打开) true(已打开)
     */
    public boolean isOpenAudio() {
        if (!isConnect) {
            return false;
        }

        return isOpenAudio;
    }

    /**
     * 打开音频流(设备音频->手机播放)，CHD_Client.java内部已实现播放功能，只要打开就直接放了
     *
     * @param null
     * @return 见错误返回值
     */
    public int openAudioStream() {
        if (!isConnect) {
            return -1;
        }

        int ret = mClient.CHD_WMP_Audio_Begin(mHandle);
        if (ret < 0) {
            return ret;
        }
        isOpenAudio = true;

        return 0;
    }

    /**
     * 关闭音频流(设备音频->手机播放)
     *
     * @param null
     * @return 见错误返回值
     */
    public int closeAudioStream() {
        if (!isConnect) {
            return -1;
        }

        int rn = mClient.CHD_WMP_Audio_End(mHandle);
        if (rn < 0) {
            return rn;
        }
        isOpenAudio = false;

        return 0;
    }

    public Boolean isAudioTalk() {
        if (!isConnect) {
            return false;
        }

        return isAudioTalk;
    }

    /**
     * 打开音频对讲(手机采集音频->设备播放)
     *
     * @param null
     * @return 见错误返回值
     */
    public int startAudioTalk() {
        if (!isConnect) {
            return -1;
        }

        int rn = mClient.CHD_WMP_Audio_PlayBegin(mHandle, audioUnit.recordRate, audioUnit.recordChn,
                audioUnit.recordBits);
        if (rn < 0) {
            return rn;
        }

        audioUnit.startSampling();
        isAudioTalk = true;

        return 0;
    }

    /**
     * 关闭音频对讲(手机采集音频->设备播放)
     *
     * @param null
     * @return 见错误返回值
     */
    public int stopAudioTalk() {
        if (!isConnect) {
            return -1;
        }

        int rn = mClient.CHD_WMP_Audio_PlayEnd(mHandle);
        if (rn < 0) {
            return rn;
        }

        audioUnit.stopSampling();
        isAudioTalk = false;

        return 0;
    }

    /**
     * 获取当前是否正在录像回放
     *
     * @param null
     * @return 见错误返回值
     */
    public Boolean isPlayback() {
        if (!isConnect) {
            return false;
        }

        return isPlayback;
    }

    /**
     * 开启录像回放(回放设备SD卡中的录像)
     *
     * @param date 开始回放的时间
     * @return 见错误返回值
     */
    public int startPlayback(st_DateInfo date) {
        if (!isConnect) {
            return -1;
        }

        int ret = mClient.CHD_WMP_Storage_Begin(mHandle, date);
        if (ret >= 0) {
            isPlayback = true;
        }

        return ret;
    }

    /**
     * 停止录像回放(回放设备SD卡中的录像)
     *
     * @param null
     * @return 见错误返回值
     */
    public int stopPlayback() {
        if (!isConnect || !isPlayback) {
            return 0;
        }

        int ret = mClient.CHD_WMP_Storage_End(mHandle);
        if (ret >= 0) {
            isPlayback = false;
        }

        return ret;
    }

    /**
     * 获取SD卡录像音频播放状态
     *
     * @param null
     * @return 见错误返回值
     */
    public Boolean getPlaybackAudioPlay() {
        if (!isConnect || !isPlayback) {
            return false;
        }

        return isPlaybackAudio;
    }

    /**
     * 设置SD卡录像音频播放状态(手机是否同时播放音频)
     *
     * @param null
     * @return 见错误返回值
     */
    public void setPlaybackAudioPlay(Boolean isplayaudio) {
        isPlaybackAudio = isplayaudio;
    }

    /**
     * 获取知道月份内每天是否存在录像(回放设备SD卡中的录像)
     *
     * @param year 年
     * @param mon  月
     * @param date 当月录像状态 (0:无录像 1:存在录像)，天数从0开始
     * @return <0(见错误返回值) >0(指定月份有多少天)
     */
    public int getRecordState(int year, int mon, byte[] date) {
        if (!isConnect) {
            return -1;
        }

        return mClient.CHD_WMP_Storage_GetRecordState(mHandle, year, mon, date);
    }

    /**
     * 获取指定年月日内24小时每个小时的录像状态
     *
     * @param year 年
     * @param mon  月
     * @param date 存放24小时内每个小时的录像状态(0:无录像 1:存在录像)，天数从0开始
     * @return 见错误返回值
     * @prram day 日
     */
    public int getRecordStateForDay(int year, int mon, int day, byte[] date) {
        if (!isConnect) {
            return -1;
        }

        return mClient.CHD_WMP_Storage_GetEveryDayOfVideo(mHandle, year, mon, day, date);
    }

    /**
     * 获取设备录像参数
     *
     * @param param 录像相应参数,见 st_StorageInfo类中的record information
     * @return 见错误返回值
     */
    public int getRecordParam(st_StorageInfo param) {
        if (!isConnect) {
            return -1;
        }

        return mClient.CHD_WMP_Storage_GetRecordParam(mHandle, param);
    }

    /**
     * 设备SD卡录像分段存储，获取一段文件存储的时间(单位分钟)
     *
     * @param timemin 一段文件存储录像时间
     * @return 见错误返回值
     */
    public int setRecordSegmentTime(int timemin) {
        if (!isConnect) {
            return -1;
        }

        return mClient.CHD_WMP_Stroage_SetRecordSegmentTime(mHandle, timemin);
    }

    public static int STORAGE_RECORD_TYPE_NO = 0; // 不录像
    public static int STORAGE_RECORD_TYPE_CONTINUOUS = 1; // 连续录像
    public static int STORAGE_RECORD_TYPE_CONDITIONS = 2; // 条件录像(动态检测)

    /**
     * 设置设备SD卡录像类型
     *
     * @param type 录像类型，见上面定义
     * @return 见错误返回值
     */
    public int setRecordType(int type) {
        if (!isConnect) {
            return -1;
        }
        return mClient.CHD_WMP_Storage_SetRecordType(mHandle, type);
    }

    public static int STORAGE_MDCOND_DYNAMIC = 1; // 动时录像
    public static int STORAGE_MDCOND_STATIC = 0; // 不动时录像

    /**
     * 设置设备SD卡录像动态侦测录像的录像条件
     *
     * @param mdcond 录像条件，见上面定义
     * @return 见错误返回值
     */
    public int setRecordMDCondition(int mdcond) {
        if (!isConnect) {
            return -1;
        }
        return mClient.CHD_WMP_Storage_SetRecordMDCondition(mHandle, mdcond);
    }

    /**
     * 获取设备SD卡存储状态
     *
     * @param param sd卡录像相应参数,见 st_StorageInfo类中的sd card information
     * @return 见错误返回值
     */
    public int getSdCardInfo(st_StorageInfo info) {
        if (!isConnect) {
            return -1;
        }

        return mClient.CHD_WMP_Storage_GetSDcardSpace(mHandle, info);
    }

    /**
     * 设备SD卡格式化
     *
     * @param void
     * @return 见错误返回值
     */
    public int setSdCardFormatting() {
        if (!isConnect) {
            return -1;
        }

        return mClient.CHD_WMP_Storage_SetSDcardFormatting(mHandle);
    }

    /**
     * 获取设备移动侦测灵敏度
     *
     * @param void
     * @return < 0 见错误返回值 >= 设备移动侦测灵敏度
     */
    public int getMDSensity() {
        if (!isConnect) {
            return -1;
        }

        st_VideoParamInfo param = new st_VideoParamInfo();
        int ret = mClient.CHD_WMP_Video_GetMotionDetection(mHandle, param);
        if (ret < 0) {
            return ret;
        }
        return param.md_sensity;
    }

    /**
     * 设置设置设备移动侦测灵敏度
     *
     * @param sensity : 设备移动侦测灵敏度(0 ~ 6)
     * @return 见错误返回值
     */
    public int setMDSensity(int sensity) {
        if (!isConnect || sensity < 0) {
            return -1;
        }

        st_VideoParamInfo param = new st_VideoParamInfo();
        param.md_enable = 1;
        param.md_sensity = sensity;

        return mClient.CHD_WMP_Video_SetMotionDetection(mHandle, param);
    }

    /**
     * 获取设备报警信息
     *
     * @param param : 设备报警信息
     * @return 见错误返回值
     */
    public int getAlarmInfomation(st_AlarmParam param) {
        if (!isConnect) {
            return -1;
        }

        return mClient.CHD_WMP_Alarm_GetParam(mHandle, param);
    }

    /**
     * 设置设备报警信息
     *
     * @param param : 设备报警信息
     * @return 见错误返回值
     */
    public int setAlarmInfomation(st_AlarmParam param) {
        if (!isConnect) {
            return -1;
        }

        return mClient.CHD_WMP_Alarm_SetParam(mHandle, param);
    }

    /**
     * 获取串口是否打开
     *
     * @param null
     * @return true(已打开串口) false(未打开)
     */
    public boolean isOpenSerial() {
        if (!isConnect)
            return false;

        return isOpenSerial;
    }

    /**
     * 打开串口
     *
     * @param null
     * @return 见错误返回值
     */
    public int openSerial() {
        if (!isConnect) {
            return -1;
        }

        int ret = mClient.CHD_WMP_Serial_Begin(mHandle);
        if (ret < 0) {
            return ret;
        }

        isOpenSerial = true;

        return 0;
    }

    /**
     * 关闭串口是否打开(必须在打开的情况下才能进行串口数据传输)
     *
     * @param null
     * @return 见错误返回值
     */
    public int closeSerial() {
        if (!isConnect)
            return -1;

        int ret = mClient.CHD_WMP_Serial_End(mHandle);
        if (ret < 0)
            return ret;

        isOpenSerial = false;

        return 0;
    }

    /* 串口波特率 */
    public static int SERIAL_SPEED_BS300 = 300;
    public static int SERIAL_SPEED_BS1200 = 1200;
    public static int SERIAL_SPEED_BS2400 = 2400;
    public static int SERIAL_SPEED_BS4800 = 4800;
    public static int SERIAL_SPEED_BS9600 = 9600;
    public static int SERIAL_SPEED_BS19200 = 19200;
    public static int SERIAL_SPEED_BS38400 = 38400;
    public static int SERIAL_SPEED_BS57600 = 57600;
    public static int SERIAL_SPEED_BS115200 = 115200;
    public static int SERIAL_SPEED_BS230400 = 230400;

    /**
     * 获取当前串口波特率
     *
     * @param null
     * @return 见错误返回值
     */
    public int getSerialSpeed() {
        if (!isConnect) {
            return -1;
        }

        return mClient.CHD_WMP_Serial_GetSpeed(mHandle);
    }

    /**
     * 设置当前串口波特率
     *
     * @param value 串口波特率，只支持上面定义的波特率才
     * @return 见错误返回值
     */
    public int setSerialSpeed(int value) {
        if (!isConnect) {
            return -1;
        }

        return mClient.CHD_WMP_Serial_SetSpeed(mHandle, value);
    }

    public static int SERIAL_DATABIT_7 = 7;
    public static int SERIAL_DATABIT_8 = 8;

    /**
     * 获取当前串口数据位
     *
     * @param null
     * @return 见错误返回值
     */
    public int getSerialDataBit() {
        if (!isConnect)
            return -1;

        return mClient.CHD_WMP_Serial_GetDataBit(mHandle);
    }

    /**
     * 设置当前串口数据位
     *
     * @param value 数据位，只支持上面定义的数据位
     * @return 见错误返回值
     */
    public int setSerialDataBit(int value) {
        if (!isConnect)
            return -1;

        return mClient.CHD_WMP_Serial_SetDataBit(mHandle, value);
    }

    public static int SERIAL_STOPBIT_1 = 1;
    public static int SERIAL_STOPBIT_0 = 0;

    /**
     * 获取当前串口停止位
     *
     * @param null
     * @return 见错误返回值
     */
    public int getSerialStopBit() {
        if (!isConnect)
            return -1;

        return mClient.CHD_WMP_Serial_GetStopBit(mHandle);
    }

    /**
     * 设置当前串口停止位
     *
     * @param value 停止位，只支持上面定义的停止位
     * @return 见错误返回值
     */
    public int setSerialStopBit(int value) {
        if (!isConnect)
            return -1;

        return mClient.CHD_WMP_Serial_SetStopBit(mHandle, value);
    }

    public static int SERIAL_PARITY_EVEN = 69;
    public static int SERIAL_PARITY_NONE = 78;
    public static int SERIAL_PARITY_ODD = 79;
    public static int SERIAL_PARITY_SPACE = 83;

    /**
     * 获取当前串口校验位
     *
     * @param null
     * @return 见错误返回值
     */
    public int getSerialParityBit() {
        if (!isConnect) {
            return -1;
        }

        return mClient.CHD_WMP_Serial_GetParity(mHandle);
    }

    /**
     * 设置当前串口校验位
     *
     * @param value 校验位，只支持上面定义的校验位
     * @return 见错误返回值
     */
    public int setSerialParity(int value) {
        if (!isConnect) {
            return -1;
        }

        return mClient.CHD_WMP_Serial_SetParity(mHandle, value);
    }

    /**
     * 发送串口数据(串口必须先打开才能发送数据)
     *
     * @param data    串口数据
     * @param datalen 数据长度
     * @return 见错误返回值
     */
    public int sendSerialData(byte[] data, int datalen) {
        if (!isConnect || !isOpenSerial) {
            return -1;
        }

        return mClient.CHD_WMP_Serial_SendData(mHandle, data, datalen);
    }

    /**
     * 获取设备 GPIO 口的数量
     *
     * @param null
     * @return <0(见错误返回值) >=0(gpio口数量)
     */
    public int getGpioNum() {
        if (!isConnect) {
            return -1;
        }

        if (!bGpioget) {
            bGpioget = true;
            mClient.CHD_WMP_Gpio_GetAll(mHandle, mGpioInfo);
        }

        return mGpioInfo.number;
    }

    public static int GPIO_DIR_IN = 1;
    public static int GPIO_DIR_OUT = 0;

    /**
     * 获取相应GPIO口输入输出方向（io口状态改变会在回调函数中通知）
     *
     * @param gpio GPIO序号
     * @return 见错误返回值
     */
    public int getGpioDir(int gpio) {
        if (!isConnect) {
            return -1;
        }

        if (!bGpioget) {
            bGpioget = true;
            mClient.CHD_WMP_Gpio_GetAll(mHandle, mGpioInfo);
        }

        if (gpio > mGpioInfo.number) {
            return -1;
        }

        int ret = mClient.CHD_WMP_Gpio_GetStatus(mHandle, gpio, mGpioInfo);
        if (ret < 0) {
            return ret;
        }

        return mGpioInfo.dir[gpio];
    }

    /**
     * 设置相应GPIO口输入输出方向（io口状态改变会在回调函数中通知）
     *
     * @param gpio GPIO序号
     * @param dir  输入输出方向，见上面定义
     * @return 见错误返回值
     */
    public int setGpioDir(int gpio, int dir) {
        if (!isConnect)
            return -1;

        if (!bGpioget) {
            bGpioget = true;
            mClient.CHD_WMP_Gpio_GetAll(mHandle, mGpioInfo);
        }
        if (gpio > mGpioInfo.number)
            return -1;

        return mClient.CHD_WMP_Gpio_SetStatus(mHandle, gpio, dir, mGpioInfo.state[gpio]);
    }

    public static int GPIO_VALUE_HIGH = 1;
    public static int GPIO_VALUE_LOW = 0;

    /**
     * 获取相应GPIO口的电平状态（io口状态改变会在回调函数中通知）
     *
     * @param gpio GPIO序号
     * @return <0(见错误返回值) >= 0(gpio口电平状态)
     */
    public int getGpioState(int gpio) {
        if (!isConnect)
            return -1;

        if (!bGpioget) {
            bGpioget = true;
            mClient.CHD_WMP_Gpio_GetAll(mHandle, mGpioInfo);
        }
        if (gpio > mGpioInfo.number)
            return -1;

        int ret = mClient.CHD_WMP_Gpio_GetStatus(mHandle, gpio, mGpioInfo);
        if (ret < 0) {
            return ret;
        }

        return mGpioInfo.state[gpio];
    }

    /**
     * 设置相应GPIO口在输出方向下的的电平状态（io口状态改变会在回调函数中通知）
     *
     * @param gpio  GPIO序号
     * @param state 电平状态见上面定义
     * @return <0(见错误返回值)
     */
    public int setGpioValue(int gpio, int state) {
        if (!isConnect) {
            return -1;
        }

        if (!bGpioget) {
            bGpioget = true;
            mClient.CHD_WMP_Gpio_GetAll(mHandle, mGpioInfo);
        }
        if (gpio > mGpioInfo.number)
            return -1;

        return mClient.CHD_WMP_Gpio_SetStatus(mHandle, gpio, mGpioInfo.dir[gpio], state);
    }

    /**
     * 数据监听处理线程，监听所有数据流
     */
    class _chird_date_poll_thread extends Thread {
        int ret = 0;
        private int dataType;
        private long sessionId = 0;

        /* playback */
        private st_DateInfo stDate = new st_DateInfo();

        /* record */
        private long recordHandle = 0;
        private int recordStatue = RECORD_STATUE_NONE;
        private String recordfilename = null;

        /* picture data save */
        private boolean bNewPicFlag = false;
        private byte[] pictureBuffer;

        /* audio */
        private byte[] audioBuffer = new byte[1024 * 128];

        /* serial data process */
        private byte[] serialBuffer = new byte[1024 * 300];

        public void run() {
            sessionId = mSessionID;
            isGetRBitmap = false;
            Log.i("CHD_Client", "[PollThread] started, sessionId=" + sessionId);
            
            int pollCount = 0;
            int videoDataCount = 0;
            
            while (isConnect) {

                dataType = mClient.CHD_WMP_Poll(mHandle, 800);
                if (sessionId != mSessionID) {
                    Log.w("CHD_Client", "[PollThread] sessionId changed, exit");
                    break;
                }

                /** 设备端异常断开 */
                if (dataType == RET_ERROR_RET_DEVICE_OFFLINE || dataType == RET_ERROR_RET_INVALID_HANDLE) {
                    Log.e("CHD_Client", "[PollThread] device OFFLINE! dataType=" + dataType);
                    break;
                }
                /** 超时及其他错误处理 */
                if (dataType < RET_ERROR_SUCCESS) {
                    continue;
                }

                /** 视频流处理: 将视频包放入队列并发送信号给解码线程进行解码处理；此处也处理录像 */
                if ((dataType & mClient.CHD_DATA_YTPE_VIDEO) == mClient.CHD_DATA_YTPE_VIDEO) {
                    videoDataCount++;
                    if (videoDataCount % 30 == 0) {
                        Log.v("CHD_Client", "[PollThread] video data received, count=" + videoDataCount);
                    }
                    
                    st_VideoFrame vframe = new st_VideoFrame();
                    int ret = mClient.CHD_WMP_Video_RequestVideoDataAddress(mHandle, vframe);
                    if (ret >= 0) {
                        videoFps = vframe.fps;
                        videoBps = vframe.BPS;

                        /****************** 录像处理 *********************/
                        /* recording */
                        isSupportRecord = vframe.format == vframe.CHD_FMT_YUYV ? false : true;
                        if (mRecordStatue == RECORD_STATUE_START && recordStatue == RECORD_STATUE_NONE) {
                            recordStatue = mRecordStatue;
                        } else if (mRecordStatue == RECORD_STATUE_STOP && recordStatue != RECORD_STATUE_NONE
                                && recordStatue != RECORD_STATUE_START) {
                            recordStatue = mRecordStatue;
                        }
                        /* start record */
                        if (isSupportRecord && isRecord && mStoragePath != null
                                && recordStatue == RECORD_STATUE_START) {

                            recordfilename = mRecordFileName;
                            if (mRecordFileName == null) {
                                recordfilename = mStorageFilePrefix != null ? (mStoragePath + mStorageFilePrefix + "_")
                                        : mStoragePath;
                                recordfilename += mClient.CHD_WMP_GetTimeMs();
                            }
                            mRecordFileName = null;

                            int sfmt = vframe.format == vframe.CHD_FMT_H264 ? mCoder.CODE_FMT_H264
                                    : mCoder.CODE_FMT_MJPEG;
                            recordHandle = mCoder.chird_mixer_create(recordfilename, vframe.width, vframe.height,
                                    vframe.fps, sfmt, 0);

                            if (vframe.format == vframe.CHD_FMT_H264)
                                mClient.CHD_WMP_Video_SetForceI(mHandle);

                            if (recordHandle != 0) {
                                recordStatue = RECORD_STATUE_WRITEDATA;
                                mRecordUrl = recordfilename + (vframe.format == vframe.CHD_FMT_H264 ? ".mp4" : ".avi");
                                isGetRBitmap = true;
                            }

                        }
                        /* write video data */
                        if (isRecord && recordStatue == RECORD_STATUE_WRITEDATA) {
                            int rn = mCoder.chird_mixer_processbyaddress(recordHandle, mCoder.MIXER_TYPE_VIDEO,
                                    vframe.pDataAddress, vframe.datalen, 0);

                        }
                        /* stop record */
                        if (recordStatue == RECORD_STATUE_STOP) {

                            mCoder.chird_mixer_destory(recordHandle);
                            recordStatue = RECORD_STATUE_NONE;
                            /* close record timer */
                            mRecordTimeHandler.removeCallbacks(RecordTimeRunnable);
                            _chird_send_message(SIGNAL_RECORD_STOP, 0, 0, mRecordUrl);
                            Log.v("test", "url:" + mRecordUrl);
                        }

                        /** 放入队列通知解码线程进行解码 */
                        videoQueue.putQueue(vframe);
                        videoDecSemp.release();
                    }

                } /* end video data */

                /** 照片流处理，按键拍照及命令拍照,照片根据时间命名 */
                if ((dataType & mClient.CHD_DATA_YTPE_PICTURE) == mClient.CHD_DATA_YTPE_PICTURE) {
                    if (!bNewPicFlag) {
                        bNewPicFlag = true;
                        pictureBuffer = new byte[1024 * 1024 * 3];
                    }

                    st_VideoFrame pframe = new st_VideoFrame();
                    int ret = mClient.CHD_WMP_Video_RequestPicData(mHandle, pframe, pictureBuffer);

                    Log.v("test", "CHD_WMP_Video_RequestPicData ret = " + ret);

                    if (ret >= 0 && pframe.format == VIDEO_FORMAT_MJPEG) {
                        String filename = mSnapFileName;
                        if (mSnapFileName == null) {
                            filename = mStorageFilePrefix != null ? (mStoragePath + mStorageFilePrefix + "_")
                                    : mStoragePath;
                            filename += (mClient.CHD_WMP_GetTimeMs() + ".jpg");
                        }
                        mSnapFileName = null;

                        if (filename != null) {
                            mClient.CHD_WMP_File_Save(filename, pframe.datalen, pictureBuffer);

                            mSnapUrl = filename;
                            _chird_send_message(SIGNAL_SNAP, pframe.width, pframe.height, mSnapUrl);
                        }
                    }
                } /* end picture data */

                /** 音频流处理，传输上来的 PCM 音频数据，直接播放 */
                if ((dataType & mClient.CHD_DATA_YTPE_AUDIO) == mClient.CHD_DATA_YTPE_AUDIO) {

                    st_AudioFrame stAudioFrame = new st_AudioFrame();
                    int ret = mClient.CHD_WMP_Audio_RequestData(mHandle, stAudioFrame, audioBuffer);

                    if (ret >= 0 && stAudioFrame.EncodeType == stAudioFrame.PCM) {
                        /* 将音频数据回调出去给应用层 */
                        if (mClientCallBack != null) {
                            mClientCallBack.audioDataCallBack(stAudioFrame.datalen, audioBuffer);
                        }
                        audioUnit.playAudioData(stAudioFrame.rate, stAudioFrame.channels, stAudioFrame.bits,
                                audioBuffer, stAudioFrame.datalen);
                    }
                } /* end audio data */

                /** 串口数据处理，直接通过回调函数回调给上层进行处理 */
                if ((dataType & mClient.CHD_DATA_YTPE_SERIAL) == mClient.CHD_DATA_YTPE_SERIAL) {
                    int len = mClient.CHD_WMP_Serial_RequestData(mHandle, serialBuffer);

                    if (mClientCallBack != null && len > 0) {
                        mClientCallBack.serialDataCallBack(len, serialBuffer);
                    }
                }

                /** 设备参数变动流 */
                if ((dataType & mClient.CHD_PARAM_CHANGE) == mClient.CHD_PARAM_CHANGE) {

                    int changeType = mClient.CHD_WMP_GetParamChangeType(mHandle);
                    /* 将参数变动通知给应用层 */
                    _chird_send_message(SIGNAL_PARAM_CHANGE, changeType, 0, null);
                }

                /** SD卡录像回放视频流 */
                if ((dataType & mClient.CHD_DATA_STORAGE_VIDEO) == mClient.CHD_DATA_STORAGE_VIDEO) {
                    st_VideoFrame pbframe = new st_VideoFrame();

                    if (mClient.CHD_WMP_Storage_RequestVideoData(mHandle, pbframe, stDate) < 0) {
                        continue;
                    }

                    pbframe.setTimestamp(stDate.year, stDate.month, stDate.day, stDate.hour, stDate.min, stDate.sec);
                    playbackQueue.putQueue(pbframe);
                    playbackDecSemp.release();
                }

                /** SD卡录像回放音频流 */
                if ((dataType & mClient.CHD_DATA_STORAGE_AUDIO) == mClient.CHD_DATA_STORAGE_AUDIO) {
                    st_AudioFrame stAudioFrame = new st_AudioFrame();
                    int ret = mClient.CHD_WMP_Storage_RequestAudioData(mHandle, stAudioFrame, audioBuffer);

                    if (ret >= 0 && stAudioFrame.EncodeType == stAudioFrame.PCM && isPlaybackAudio) {
                        audioUnit.playAudioData(stAudioFrame.rate, stAudioFrame.channels, stAudioFrame.bits,
                                audioBuffer, stAudioFrame.datalen);
                    }
                } /* end storage audio */

                /** SD卡录像回放报警照片流(此处预留，还未想好怎么处理) */
                if ((dataType & mClient.CHD_DATA_STORAGE_SNAP) == mClient.CHD_DATA_STORAGE_SNAP) {

                }

                /** 自定义数据流 */
                if ((dataType & mClient.CHD_DATA_CUSTOM) == mClient.CHD_DATA_CUSTOM) {

                    st_Custom custom = new st_Custom();
                    if (mClient.CHD_WMP_Custom_RequestData(mHandle, custom) >= 0) {

//						

                        mClient.CHD_WMP_Custom_ReleaseData(mHandle, custom);
                    }

                }

            } /* end while(isConnect) */

            Log.v("test", "--------- poll thread quit " + ret + " ----------");

            /* audio uninit */
            audioUnit.audioPlayDestory();

            /* if the record is not closed, turn off the record */
            if (recordStatue == RECORD_STATUE_WRITEDATA || recordStatue == RECORD_STATUE_STOP) {
                mRecordUrl = recordfilename;
                recordStatue = RECORD_STATUE_NONE;
                _chird_send_message(SIGNAL_RECORD_STOP, 0, 0, mRecordUrl);
                mCoder.chird_mixer_destory(recordHandle);
                mRecordTimeHandler.removeCallbacks(RecordTimeRunnable);
            }

            /* unexpected exit, reclaim space */
            if (sessionId == mSessionID && isConnect) {
                mClient.CHD_WMP_Disconnect(mHandle);
            }

            cleanVideoQueue();
            videoDecSemp.release();

            playbackDecSemp.release();

            _chird_send_message(SIGNAL_DISCONNECT, 0, 0, null);

            isConnect = false;
            isRecord = false;
            isOpenAudio = false;
            isOpenVideoStream = false;
        }/* end void run() */

    }

    /**
     * 视频队列清空函数
     */
    private void cleanVideoQueue() {
        if (!isConnect) {
            return;
        }
        st_VideoFrame videoframe = null;
        while (videoQueue.getLength() > 0) {
            videoframe = videoQueue.getQueue();
            mClient.CHD_WMP_Video_ReleaseVideoDataAddress(mHandle, videoframe);
        }
    }

    /**
     * YUYV JPEG 为了高实时性只解码最新的两帧
     */
    private static final int VIDEO_DECODER_CACHE_NUMBER = 2;

    /**
     * 视频解码处理线程，解码完成后将数据拷贝到一个全局变量中，通知显示线程进行显示
     */
    class _chird_video_decoder_thread extends Thread {
        private int ret = 0;
        private long sessionId = 0;
        private int rgblen = 0;
        private int width = 0, height = 0;

        // video h264 decoder process
        private long old_times = 0;
        private long mSequence = 0;
        private int lostFrameCnt = 0;
        private boolean geIflag = false;

        // video rgb565 callback user
        private byte[] videobuffer;
        private int maxvideobufflen = 0;

        private st_VideoDecFrame stDecFrame = new st_VideoDecFrame();

        public void run() {
            sessionId = mSessionID;
            Log.i("CHD_Client", "[VideoDecoderThread] started, sessionId=" + sessionId);

            // 初始化解码函数
            if (mVideoJhandle == 0) {
                mVideoJhandle = mCoder.chird_vdec_create(mCoder.CODE_FMT_MJPEG, mCoder.CODE_PIXEL_FMT_RGB565);
            }
            if (mVideoHhandle == 0) {
                mVideoHhandle = mCoder.chird_vdec_create(mCoder.CODE_FMT_H264, mCoder.CODE_PIXEL_FMT_RGB565);
            }

            stDecFrame.maxLength = 1;
            stDecFrame.pDataAddress = mCoder.chird_videomen_malloc(stDecFrame.maxLength);

            int decodeFrameCount = 0;
            while (isConnect) {
                /** 视频队列中存在视频队列，不等待，直接进行解码 */
                if (videoQueue.getLength() <= 3) {
                    try {
                        videoDecSemp.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        continue;
                    }
                }

                if (sessionId != mSessionID) {
                    Log.w("CHD_Client", "[VideoDecoderThread] sessionId changed, exit");
                    break;
                }
                // 从视频队列中取出一帧视频
                st_VideoFrame videoframe = videoQueue.getQueue();
                if (videoframe == null) {
                    continue;
                }
                
                decodeFrameCount++;
                if (decodeFrameCount % 30 == 0) {
                    Log.v("CHD_Client", "[VideoDecoderThread] frame#" + decodeFrameCount + 
                        " format=" + videoframe.format + " size=" + videoframe.width + "x" + videoframe.height +
                        " queueLen=" + videoQueue.getLength());
                }

                /* Resolu Change, recreate bitmap to display */
                if (width != videoframe.width || height != videoframe.height) {
                    rgblen = videoframe.width * videoframe.height * 2;
                    if (stDecFrame.maxLength < rgblen) {
                        stDecFrame.maxLength = rgblen;
                        stDecFrame.pDataAddress = mCoder.chird_videomen_realloc(stDecFrame.pDataAddress,
                                stDecFrame.maxLength);
                    }

                    /* H264 video stream, must be forced to get I frame */
                    if (videoframe.format == videoframe.CHD_FMT_H264) {
                        if (videoframe.iflag != 0) {
                            geIflag = false;
                        } else {
                            geIflag = true;
                        }
                    }
                    width = videoframe.width;
                    height = videoframe.height;
                }

                stDecFrame.copyDecFrame(videoframe.format, width, height, rgblen, videoframe.timestamp);

                boolean isShowFlag = false;

                /** YUYV JPEG 数据解码 */
                if (videoframe.format != VIDEO_FORMAT_H264) {
                    if (videoQueue.getLength() < VIDEO_DECODER_CACHE_NUMBER || mSnapCount > 0 || isGetRBitmap) {
                        if (videoframe.format == videoframe.CHD_FMT_YUYV) {
                            ret = mCoder.chird_sws_processaddress(mCoder.CODE_PIXEL_FMT_YUYV422,
                                    videoframe.pDataAddress, mCoder.CODE_PIXEL_FMT_RGB565, videoframe.pDataAddress,
                                    width, height);
                        } else if (videoframe.format == videoframe.CHD_FMT_MJPEG) {
                            ret = mCoder.chird_vdec_processaddress(mVideoJhandle, width, height, videoframe.datalen,
                                    videoframe.pDataAddress, stDecFrame.pDataAddress);
                        }
                        if (ret >= 0) {
                            isShowFlag = true;
                        }
                    }
                    /** H264数据解码处理 */
                } else {
                    /** 连续丢帧10帧以上就必须获取一次I帧 */
                    if ((videoframe.sequence - mSequence) != 1) {
                        lostFrameCnt++;
                        if (lostFrameCnt >= 10) {
                            setVideoH264ForceI();
                            lostFrameCnt = 0;
                        }
                        geIflag = true;
                    }

                    if (geIflag && videoframe.iflag == 1) {
                        geIflag = false;
                    }

                    if (!geIflag) {
                        lostFrameCnt = 0;
                        mSequence = videoframe.sequence;
                        ret = mCoder.chird_vdec_processaddress(mVideoHhandle, width, height, videoframe.datalen,
                                videoframe.pDataAddress, stDecFrame.pDataAddress);

                        if (ret < 0) {
                            geIflag = true;
                        }
                    }

                    long curtime = System.currentTimeMillis();
                    if (ret >= 0 && ((curtime - old_times) > 40 || mSnapCount > 0 || isGetRBitmap)) {
                        isShowFlag = true;
                        old_times = curtime;
                    }

                }

                /** RGB565视频回调给应用层 */
                if (isCallbackRGBDate && mClientCallBack != null) {
                    if (maxvideobufflen > stDecFrame.datalen) {
                        maxvideobufflen = stDecFrame.datalen;
                        videobuffer = new byte[maxvideobufflen];
                    }
                    mCoder.chird_videomen_copytoarray(mDecFrame.pDataAddress, mDecFrame.datalen, videobuffer);
                    mClientCallBack.videoStreamDataCallBack(VIDEO_FORMAT_RGB565, videoframe.width, videoframe.height,
                            videoframe.datalen, videobuffer);
                }

                /** 将视频拷贝到全局bitmap，并通知显示线程进行显示 */
                if (isShowFlag) {
                    synchronized (this) {
                        if (mDecFrame.maxLength < stDecFrame.maxLength) {
                            mDecFrame.maxLength = stDecFrame.maxLength;
                            mDecFrame.pDataAddress = mCoder.chird_videomen_realloc(mDecFrame.pDataAddress,
                                    mDecFrame.maxLength);
                        }

                        mDecFrame.copyDecFrame(stDecFrame.format, stDecFrame.width, stDecFrame.height,
                                stDecFrame.datalen, stDecFrame.timestamp);
                        int rn = mCoder.chird_videomen_copy(stDecFrame.pDataAddress, stDecFrame.datalen,
                                mDecFrame.pDataAddress);
                        displaySemp.release();

                    }
                }

                mClient.CHD_WMP_Video_ReleaseVideoDataAddress(mHandle, videoframe);
            } /* end while (isConnect) */

            if (stDecFrame.pDataAddress != 0) {
                mCoder.chird_videomen_free(stDecFrame.pDataAddress);
            }

            displaySemp.release();
        }/* end run() */
    }/* end videoDecoderThread */

    class _chird_video_show_thread extends Thread {
        private long sessionId = 0;

        private Bitmap bitmap = null;
        private int frameCount = 0;

        public void run() {
            sessionId = mSessionID;
            Log.i("CHD_Client", "[VideoShowThread] started, sessionId=" + sessionId);
            while (isConnect) {
                /* Wait Decoding Successful Signal */
                try {
                    displaySemp.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    continue;
                }
                if (sessionId != mSessionID) {
                    Log.w("CHD_Client", "[VideoShowThread] sessionId changed, exit");
                    break;
                }

                // 将解码后的数据拷贝到bitmap
                synchronized (this) {
                    bitmap = vBitmap;
                    int rn = mCoder.chird_videomen_copytobitmap(mDecFrame.pDataAddress, mDecFrame.width,
                            mDecFrame.height, vBitmap);
                    if (rn < 0) {
                        Log.w("CHD_Client", "[VideoShowThread] copyToBitmap failed, reCreateBitmap");
                        reCreateBitmap(mDecFrame.width, mDecFrame.height);
                    }
                }

                synchronized (this) {
                    /* Display Bitmap CallBack */
                    if (mClientCallBack != null && bitmap != null) {
                        frameCount++;
                        if (frameCount % 30 == 0) { // 每30帧打印一次
                            Log.i("CHD_Client", "[VideoShowThread] frame#" + frameCount + 
                                " size=" + mDecFrame.width + "x" + mDecFrame.height);
                        }
                        
                        /** 视频旋转处理 */
                        if (mBitmapRotate % 360 != 0 || (mBitmapVmirror != 0) || (mBitmapHmirror != 0)) {
                            bitmap = bitmapRotateAndMirror(bitmap, mBitmapRotate % 360, mBitmapHmirror, mBitmapVmirror);
                        }

                        /** 从流里面获取一张照片保存 */
                        if (mSnapCount > 0 && mStoragePath != null) {
                            mSnapCount--;
                            String filename = mSnapFileName;
                            if (mSnapFileName == null) {
                                filename = mStorageFilePrefix != null ? (mStoragePath + mStorageFilePrefix + "_")
                                        : mStoragePath;
                                filename += (mClient.CHD_WMP_GetTimeMs() + ".jpg");
                            }
                            mSnapFileName = null;

                            if (filename != null) {
                                SaveBitmap(filename, bitmap);
                                mSnapUrl = filename;
                                _chird_send_message(SIGNAL_SNAP, bitmap.getWidth(), bitmap.getHeight(), mSnapUrl);
                            }
                        }

                        /* 录像处理 */
                        if (isGetRBitmap) {
                            isGetRBitmap = false;
                            mRBitmap = GenerateThumbnail(bitmap, mRBThumbWidth, mRBThumbHeight);
                        }

                        if (isConnect) {
                            mClientCallBack.videoStreamBitmapCallBack(bitmap);
                        }
                    } else {
                        if (mClientCallBack == null) {
                            Log.w("CHD_Client", "[VideoShowThread] mClientCallBack is NULL!");
                        }
                        if (bitmap == null) {
                            Log.w("CHD_Client", "[VideoShowThread] bitmap is NULL!");
                        }
                    }
                }

            } /* end while (isConnect) */
            Log.i("CHD_Client", "[VideoShowThread] exited");
        }/* end void run() */

        private int reCreateBitmap(int width, int height) {

            if (width <= 0 || height <= 0)
                return -1;

            if (vBitmap == null || vBitmap.getWidth() != width || vBitmap.getHeight() != height) {
                synchronized (this) {
                    try {
                        vBitmap = Bitmap.createBitmap(width, height, Config.RGB_565);
                    } catch (OutOfMemoryError e) {
                        return -1;
                    }
                }
            }
            return 0;
        }/* end reCreateBitmap() */

    }/* end displayThread */

    /**
     * sd卡视频回放解码处理线程，解码完成后将数据拷贝到一个全局变量中，通知显示线程进行显示
     */
    class _chird_playback_decoder_thread extends Thread {
        private int ret = 0;
        private long sessionId = 0;
        private Bitmap bitmap = null;
        private int width = 640;
        private int height = 480;
        private long jhandle, hhandle;

        public void run() {
            bitmap = Bitmap.createBitmap(width, height, Config.RGB_565);
            sBitmap = Bitmap.createBitmap(width, height, Config.RGB_565);

            jhandle = mCoder.chird_vdec_create(mCoder.CODE_FMT_MJPEG, mCoder.CODE_PIXEL_FMT_RGB565);
            hhandle = mCoder.chird_vdec_create(mCoder.CODE_FMT_H264, mCoder.CODE_PIXEL_FMT_RGB565);

            sessionId = mSessionID;
            while (isConnect) {

                if (playbackQueue.getLength() <= 5) {
                    try {
                        playbackDecSemp.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        continue;
                    }
                }

                if (sessionId != mSessionID) {
                    break;
                }

                st_VideoFrame videoframe = playbackQueue.getQueue();
                if (videoframe == null) {
                    continue;
                }

                /* decode */
                if (videoframe.width != width || videoframe.height != height) {
                    if (createNewBitmap(videoframe.width, videoframe.height) < 0) {
//						Log.v("test", "create bitmap fail... w:"
//								+ videoframe.width + "h:" + videoframe.height);
                        // continue;
                    } else {
                        width = videoframe.width;
                        height = videoframe.height;
                    }

                }

                if (videoframe.format == videoframe.CHD_FMT_YUYV) {
                    ret = mCoder.chird_sws_processbyaddress(mCoder.CODE_PIXEL_FMT_YUYV422, videoframe.pDataAddress,
                            mCoder.CODE_PIXEL_FMT_RGB565, bitmap, videoframe.width, videoframe.height);
                } else if (videoframe.format == videoframe.CHD_FMT_MJPEG) {
                    ret = mCoder.chird_vdec_processbyaddress(jhandle, videoframe.width, videoframe.height,
                            videoframe.datalen, videoframe.pDataAddress, bitmap);
                } else if (videoframe.format == videoframe.CHD_FMT_H264) {
                    ret = mCoder.chird_vdec_processbyaddress(hhandle, videoframe.width, videoframe.height,
                            videoframe.datalen, videoframe.pDataAddress, bitmap);
                }

                if (ret >= 0) {
                    synchronized (this) {
                        if (mCoder.chird_vdec_bitmapcopy(bitmap, sBitmap) >= 0) {
                            playbackDate.setTimestamp(videoframe.year, videoframe.month, videoframe.day,
                                    videoframe.hour, videoframe.min, videoframe.sec);
                            playbackShowSemp.release();
                        }
                    }
                }

                /* release storage video data */
                mClient.CHD_WMP_Storage_ReleaseVideoData(mHandle, videoframe);

            } /* end while (isConnect) */

            /** 清空所有的视频队列 */
            for (int i = 0; i < playbackQueue.getLength(); i++) {
                st_VideoFrame videoframe = playbackQueue.getQueue();
                if (videoframe != null) {
                    mClient.CHD_WMP_Storage_ReleaseVideoData(mHandle, videoframe);
                }
            }

            mCoder.chird_vdec_destory(jhandle);
            mCoder.chird_vdec_destory(hhandle);
        }/* end public void run() */

        private int createNewBitmap(int w, int h) {
            if (bitmap == null || sBitmap == null || bitmap.getWidth() != w || bitmap.getHeight() != h) {
                if (bitmap != null && !bitmap.isRecycled()) {
                    // bitmap.recycle();
                    bitmap = null;
                    System.gc();
                }
                try {
                    bitmap = Bitmap.createBitmap(w, h, Config.RGB_565);
                } catch (OutOfMemoryError e) {
                    return -1;
                }

                synchronized (this) {
                    if (sBitmap != null && !sBitmap.isRecycled()) {
                        // sBitmap.recycle();
                        sBitmap = null;
                        System.gc();
                    }
                    try {
                        sBitmap = Bitmap.createBitmap(w, h, Config.RGB_565);
                    } catch (OutOfMemoryError e) {
                        return -1;
                    }
                }
            }
            return 0;
        }/* end private int createNewBitmap(Bitmap btp, int w, int h) */

    }

    /**
     * sd卡录像回放显示处理线程
     */
    class _chird_playback_show_thread extends Thread {
        private long sessionId = 0;

        public void run() {
            sessionId = mSessionID;
            while (isConnect) {
                /* Wait Decoding Successful Signal */
                try {
                    playbackShowSemp.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    continue;
                }
                if (sessionId != mSessionID) {
                    break;
                }
                synchronized (this) {
                    /* Display Bitmap CallBack */
                    if (mClientCallBack != null && sBitmap != null && isPlayback) {
                        mClientCallBack.playbackStreamBitmapCallBack(playbackDate, sBitmap);
                    }
                }

            } /* end while (isConnect) */
        }/* end void run() */
    }/* end displayThread */

    protected void finalize() {
        if (isConnect) {
            isConnect = false;
            mClient.CHD_WMP_Disconnect(mHandle);
        }

        if (mDecFrame.pDataAddress != 0) {
            mCoder.chird_videomen_free(mDecFrame.pDataAddress);
        }

        if (mVideoJhandle != 0) {
            mCoder.chird_vdec_destory(mVideoJhandle);
        }
        if (mVideoHhandle != 0) {
            mCoder.chird_vdec_destory(mVideoHhandle);
        }

        audioUnit.Destory();
    }

    /**
     * bitmap 旋转及镜像
     *
     * @param origin           原始 bitmap
     * @param angle            旋转角度
     * @param horizontalMirror 水平镜像
     * @param verticalMirror   垂直镜像
     * @return 处理后的bitmap
     */
    private Bitmap bitmapRotateAndMirror(Bitmap origin, float angle, int hmirror, int vmirror) {
        if (origin == null) {
            return null;
        }

        hmirror = hmirror != 0 ? -1 : 1;
        vmirror = vmirror != 0 ? -1 : 1;

        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(angle);
        matrix.postScale(hmirror, vmirror);
        return Bitmap.createBitmap(origin, 0, 0, origin.getWidth(), origin.getHeight(), matrix, true);
    }

    /**
     * 存储bitmap 照片
     */
    public int SaveBitmap(String filename, Bitmap mBitmap) {
        File file = new File(filename);
        try {
            file.createNewFile();
        } catch (IOException e) {
            return -1;
        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return -1;
        }

        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }

        return 0;
    }

    /**
     * 获取录像时间字符串
     *
     * @param timer 整形的时间
     * @return 整理后按照时分秒命名的字符串
     */
    private String getRecordTimerString(int timer) {
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

    /**
     * 计算文件或码流大小，自适应单位
     *
     * @param fileS 文件的长度或者码流的长度
     * @return 返回计算好并添加单位的字符串
     */
    public String SizeLongToString(long fileS) {
        String fileSizeString;
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS - 1024.0 < 0) {
            fileSizeString = SIZE_FORMAT.format((double) fileS) + "B";
        } else if (fileS - 1048576.0 < 0) {
            fileSizeString = SIZE_FORMAT.format((double) fileS / 1024) + "KB";
        } else if (fileS - 1073741824.0 < 0) {
            fileSizeString = SIZE_FORMAT.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = SIZE_FORMAT.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    /**
     * 获取当前时间字符串
     */
    private String getTimesString() {
        String time = TIME_FORMAT.format(Calendar.getInstance().getTime());
        time = time.replace(" ", "_");
        return time.replace(":", "-");
    }

    /**
     * 创建微缩图
     *
     * @param source 原始图片
     * @param width  微缩图宽
     * @param height 微缩图高
     * @return 生成的微缩图
     */
    private Bitmap GenerateThumbnail(Bitmap source, int width, int height) {
        return ThumbnailUtils.extractThumbnail(source, width, height);
    }

}

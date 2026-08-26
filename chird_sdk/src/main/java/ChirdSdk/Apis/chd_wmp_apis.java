package ChirdSdk.Apis;

import android.graphics.Bitmap;

public class chd_wmp_apis {

    // 获取 SDK 版本号
    public native int CHD_WMP_GetSdkVersion();

    /**
     * 设备搜索相关  5个函数
     */
    public native int CHD_WMP_ScanDevice_Init(int ScanTime);

    public native int CHD_WMP_ScanDevice_InitIndex(int ScanTime, int Index);

    public native int CHD_WMP_ScanDevice_UnInit();

    public native int CHD_WMP_Scan_GetDeviceInfo(st_SearchInfo DevInfo);

    public native int CHD_WMP_Scan_GetDeviceInformation(st_SearchInfo DevInfo);


    /**
     * 一键配置相关 2个函数
     */
    public native int CHD_WMP_SmartConfig_Begin(String RouterName,
                                                String RouterPasswd, String DevId);

    public native int CHD_WMP_SmartConfig_End();


    /**
     * 连接断开函数 7个函数
     */
    public native long CHD_WMP_ConnectDeviceUser(String address, int index,
                                                 String param, String passwd);

    public native long CHD_WMP_ConnectDeviceIndex(String address, int index,
                                                  String passwd);

    public native long CHD_WMP_ConnectDevice(String address, String passwd);

    public native long CHD_WMP_ConnectSenSenDevice(String address, String passwd);

    public native int CHD_WMP_Disconnect(long handle);

    public native String CHD_WMP_GetEncrypt(long handle);

    public native int CHD_WMP_SetEncrypt(long handle, String passwd);

    /**
     * 自定义 5个函数
     */
    public native int CHD_WMP_GetCustom(long handle, st_Custom custom);

    public native int CHD_WMP_SetCustom(long handle, st_Custom custom);

    public native int CHD_WMP_Custom_RequestData(long handle, st_Custom custom);

    public native int CHD_WMP_Custom_CopyDataToByteArray(long pAddress, int length, byte[] data);

    public native int CHD_WMP_Custom_ReleaseData(long handle, st_Custom custom);


    /**
     * 数据监听 3个函数
     */
    public static final int CHD_DATA_YTPE_VIDEO = 0x0001;
    public static final int CHD_DATA_YTPE_PICTURE = 0x0002;
    public static final int CHD_DATA_YTPE_AUDIO = 0x0004;
    public static final int CHD_DATA_YTPE_SERIAL = 0x0008;

    public static final int CHD_PARAM_CHANGE = 0x0010;
    public static final int CHD_DATA_STORAGE_SNAP = 0x0020;
    public static final int CHD_DATA_STORAGE_VIDEO = 0x0040;
    public static final int CHD_DATA_STORAGE_AUDIO = 0x0080;
    public static final int CHD_DATA_CUSTOM = 0x0100;

    public native int CHD_WMP_Poll(long handle, int msec);

    public static final int CHD_PARAMCHANGETYPE_VIDEO_ABILITY = 0x00;
    public static final int CHD_PARAMCHANGETYPE_VIDEO_PARAM = 0x01;
    public static final int CHD_PARAMCHANGETYPE_VIDEO_CTRL = 0x02;
    public static final int CHD_PARAMCHANGETYPE_AUDIO_PARAM = 0x03;
    public static final int CHD_PARAMCHANGETYPE_SERIAL_PARAM = 0X04;
    public static final int CHD_PARAMCHANGETYPE_GPIO_STATE = 0X05;

    public static final int CHD_PARAMCHANGETYPE_VIDEO_ALLCTRL = 0X06;
    public static final int CHD_PARAMCHANGETYPE_VIDEO_ALL = 0X07;
    public static final int CHD_PARAMCHANGETYPE_VIDEO_H26X = 0X08;
    public static final int CHD_PARAMCHANGETYPE_NETCHN_NUM = 0X09;
    public static final int CHD_PARAMCHANGETYPE_VIDEO_CROP = 0X10;
    public static final int CHD_PARAMCHANGETYPE_SIGNAL_SNAP = 0X20;
    public static final int CHD_PARAMCHANGETYPE_SIGNAL_RECORD = 0X21;
    public static final int CHD_PARAMCHANGETYPE_SIGNAL_FREEZE = 0X22;

    public native int CHD_WMP_GetParamChangeType(long handle);

    public static final int CHD_TRANSMODE_TCP = 1;
    public static final int CHD_TRANSMODE_P2P = 2;
    public static final int CHD_TRANSMODE_RLY = 3;

    public native int CHD_WMP_GetTransMode(long handle);

    public native int CHD_WMP_DeviceIsOnline(String did);


    /**
     * 设备相关 12个函数
     */
    public native int CHD_WMP_GetVersion(long handle);

    public native int CHD_WMP_Device_GetId(long handle);

    public native int CHD_WMP_Device_SetId(long handle, int id);

    public native String CHD_WMP_Device_GetAlias(long handle);

    public native String CHD_WMP_Device_GetProductName(long handle);

    public native int CHD_WMP_Device_SetAlias(long handle, String alias);

    public native String CHD_WMP_Device_GetDID(long handle);

    public native int CHD_WMP_Device_Reboot(long handle);

    public native int CHD_WMP_Device_Reset(long handle);

    public native int CHD_WMP_Device_SenSenUpdata(long handle, String url);

    public native int CHD_WMP_GetDeviceInfo(long handle, st_DeviceInfo dev);

    public native String CHD_WMP_GetMac(long handle);

    public native String CHD_WMP_GetCompany(long handle);


    /**
     * PWM 2个函数
     */
    public native int CHD_WMP_GetPWM(long handle, st_PwmInfo pwm);

    public native int CHD_WMP_SetPWM(long handle, st_PwmInfo pwm);


    /**
     * AD 1个函数
     */
    public native int CHD_WMP_GetADValue(long handle, st_ADValueInfo advalue);


    /**
     * 时间同步相关 2个函数
     */
    public native int CHD_WMP_GetSystemTime(long handle, st_SystimeInfo stime);

    public native int CHD_WMP_SetSystemTime(long handle, st_SystimeInfo stime);


    /**
     * 无线相关 6个函数
     */
    public native int CHD_WMP_Wireless_GetNetType(long handle);

    public native int CHD_WMP_Wireless_SetStaInfo(long handle,
                                                  st_WirelessInfo param);

    public native int CHD_WMP_Wireless_GetStaInfo(long handle,
                                                  st_WirelessInfo param);

    public native int CHD_WMP_Wireless_SetApInfo(long handle,
                                                 st_WirelessInfo param);

    public native int CHD_WMP_Wireless_GetApInfo(long handle,
                                                 st_WirelessInfo param);

    public native int CHD_WMP_Wireless_GetStaStatus(long handle,
                                                    st_StaStatus status);


    /**
     * 音频相关 9个函数
     */
    public native int CHD_WMP_Audio_Begin(long handle);

    public native int CHD_WMP_Audio_End(long handle);

    public native int CHD_WMP_Audio_GetParam(long handle,
                                             st_AudioParamInfo param);

    public native int CHD_WMP_Audio_SetParam(long handle,
                                             st_AudioParamInfo param);

    public native int CHD_WMP_Audio_SetDenoise(long handle, int enable,
                                               int nmode);

    public native int CHD_WMP_Audio_RequestData(long handle,
                                                st_AudioFrame audioframe, byte[] data);

    public native int CHD_WMP_Audio_PlayBegin(long handle, int rate,
                                              int channels, int bits);

    public native int CHD_WMP_Audio_PlayEnd(long handle);

    public native int CHD_WMP_Audio_SendData(long handle, int encodeType,
                                             byte[] data, int datalen);


    /**
     * 串口相关 18个函数
     */
    public native int CHD_WMP_Serial_Begin(long handle);

    public native int CHD_WMP_Serial_End(long handle);

    public native int CHD_WMP_Serial_SendData(long handle, byte[] data,
                                              int datalen);

    public native int CHD_WMP_Serial_RequestData(long handle, byte[] data);

    public native int CHD_WMP_Serial_GetCurRxCacheSize(long handle);

    public native int CHD_WMP_Serial_GetRxTotalNum(long handle);

    public native int CHD_WMP_Serial_GetTxTotalNum(long handle);

    public native int CHD_WMP_Serial_GetParam(long handle, st_SerialInfo param);

    public native int CHD_WMP_Serial_GetSpeed(long handle);

    public native int CHD_WMP_Serial_GetDataBit(long handle);

    public native int CHD_WMP_Serial_GetStopBit(long handle);

    public native int CHD_WMP_Serial_GetParity(long handle);

    public native int CHD_WMP_Serial_GetTimeout(long handle);

    public native int CHD_WMP_Serial_SetSpeed(long handle, int speed);

    public native int CHD_WMP_Serial_SetDataBit(long handle, int databit);

    public native int CHD_WMP_Serial_SetStopBit(long handle, int stopbit);

    public native int CHD_WMP_Serial_SetParity(long handle, int parity);

    public native int CHD_WMP_Serial_SetTimeout(long handle, int timeout);

    /**
     * GPIO相关 4个函数
     */
    public native int CHD_WMP_Gpio_GetAll(long handle, st_GpioInfo param);

    public native int CHD_WMP_Gpio_SetAll(long handle, st_GpioInfo param);

    public native int CHD_WMP_Gpio_GetStatus(long handle, int gpio,
                                             st_GpioInfo param);

    public native int CHD_WMP_Gpio_SetStatus(long handle, int gpio, int dir,
                                             int state);

    /**
     * I2C相关 2个函数
     */
    public native int CHD_WMP_I2C_GetValue(long handle, st_I2CInfo data);

    public native int CHD_WMP_I2C_SetValue(long handle, st_I2CInfo data);


    /**
     * 视频云台 镜头切换相关 3个函数
     */
    public native int CHD_WMP_Video_GetDeviceIdx(long handle);

    public native int CHD_WMP_Video_SetDeviceIdx(long handle, int idx);

    public static final int CHD_VIDEO_PTZ_RESET = 0;
    public static final int CHD_VIDEO_PTZ_UP = 1;
    public static final int CHD_VIDEO_PTZ_DOWN = 2;
    public static final int CHD_VIDEO_PTZ_LEFT = 3;
    public static final int CHD_VIDEO_PTZ_RIGHT = 4;
    public static final int CHD_VIDEO_PTZ_ZOOMIN = 5;
    public static final int CHD_VIDEO_PTZ_ZOOMOUT = 6;
    public static final int CHD_VIDEO_PTZ_ZOOMIN_W = 7;
    public static final int CHD_VIDEO_PTZ_ZOOMOUT_W = 8;
    public static final int CHD_VIDEO_PTZ_ZOOMIN_H = 9;
    public static final int CHD_VIDEO_PTZ_ZOOMOUT_H = 10;
    public static final int CHD_VIDEO_PTZ_MIN = 11;

    public native int CHD_WMP_Video_SetElecPTZ(long handle, int flag);


    /**
     * 视频流相关 6个函数
     */

    public native int CHD_WMP_Video_Begin(long handle);

    public native int CHD_WMP_Video_End(long handle);

    public native int CHD_WMP_Video_RequestVideoData(long handle,
                                                     st_VideoFrame videoframe, byte[] data);

    public native int CHD_WMP_Video_RequestVideoDataAddress(long handle,
                                                            st_VideoFrame videoframe);

    public native int CHD_WMP_Video_ReleaseVideoDataAddress(long handle,
                                                            st_VideoFrame videoframe);

    public native int CHD_WMP_Video_CopyVideoDataToByteArray(long handle,
                                                             st_VideoFrame videoframe, byte[] data);


    /**
     * 视频旋转镜像相关 3个函数
     */

    public native int CHD_WMP_Video_SetVideoRotation(long handle, int rotation);

    public native int CHD_WMP_Video_GetMirror(long handle,
                                              st_VideoParamInfo param);

    public native int CHD_WMP_Video_SetMirror(long handle,
                                              st_VideoParamInfo param);


    /**
     * 设备性能、视频参数相关 8个函数
     */

    public native int CHD_WMP_Video_GetAbility(long handle,
                                               st_VideoAbilityInfo abi);

    public native int CHD_WMP_Video_GetParam(long handle,
                                             st_VideoParamInfo param);

    public native int CHD_WMP_Video_GetFormat(long handle);

    public native int CHD_WMP_Video_GetResolu(long handle,
                                              st_VideoParamInfo param);

    public native int CHD_WMP_Video_GetFPS(long handle);

    public native int CHD_WMP_Video_SetFormat(long handle, int format);

    public native int CHD_WMP_Video_SetResolu(long handle, int width, int height);

    public native int CHD_WMP_Video_SetFPS(long handle, int fps);

    /**
     * 视频控制参数相关 4个函数
     */
    public native int CHD_WMP_Video_GetVideoCtrl(long handle, int type,
                                                 st_VideoCtrlInfo vctrl);

    public native int CHD_WMP_Video_SetVideoCtrl(long handle, int type,
                                                 st_VideoCtrlInfo vctrl);

    public native int CHD_WMP_Video_ResetVCtrl(long handle);

    public native int CHD_WMP_Video_SetXuCtrl(long handle, st_VideoCtrlInfo xuCtrl);

    /**
     * 视频 OSD相关 4个函数
     */

    public native int CHD_WMP_Video_GetOSD(long handle, st_VideoParamInfo param);

    public native int CHD_WMP_Video_SetOSD(long handle, st_VideoParamInfo param);

    public native int CHD_WMP_Video_GetTOSD(long handle, st_VideoParamInfo param);

    public native int CHD_WMP_Video_SetTOSD(long handle, st_VideoParamInfo param);

    /**
     * 缓存队列相关 3个函数
     */
    public native int CHD_WMP_Video_GetCurVideoFrameNum(long handle);

    public native int CHD_WMP_Video_GetCurPictureFrameNum(long handle);

    public native int CHD_WMP_Video_GetPeerMaxFrameNum(long handle);

    public native int CHD_WMP_Video_GetLocalMaxFrameNum(long handle);

    public native int CHD_WMP_Video_SetPeerMaxFrameNum(long handle, int num);

    public native int CHD_WMP_Video_SetLocalMaxFrameNum(long handle, int num);

    /**
     * 拍照相关 3个函数
     */
    public native int CHD_WMP_Video_SnapShot(long handle);

    public native int CHD_WMP_Video_SnapShotResolu(long handle, int width,
                                                   int height);

    public native int CHD_WMP_Video_RequestPicData(long handle,
                                                   st_VideoFrame videofram, byte[] data);


    /**
     * H264设置相关 10个函数
     */
    public native int CHD_WMP_Video_GetH264KeyInter(long handle);

    public native int CHD_WMP_Video_GetH264QpValue(long handle);

    public native int CHD_WMP_Video_GetH264Stream(long handle);

    public native int CHD_WMP_Video_GetQuality(long handle);

    public native int CHD_WMP_Video_SetH264KeyInter(long handle, int cnt);

    public native int CHD_WMP_Video_SetH264QpValue(long handle, int value);

    public native int CHD_WMP_Video_SetH264Stream(long handle, int value);

    public native int CHD_WMP_Video_SetQuality(long handle, int quality);

    public native int CHD_WMP_Video_SetForceI(long handle);

    public native int CHD_WMP_Video_SetFpsLimit(long handle, int limit);


    /**
     * 录像回放相关 13个函数
     */
    public native int CHD_WMP_Storage_GetSDcardSpace(long handle,
                                                     st_StorageInfo sdcard);

    public native int CHD_WMP_Storage_SetSDcardFormatting(long handle);

    public native int CHD_WMP_Storage_GetRecordParam(long handle,
                                                     st_StorageInfo param);

    public native int CHD_WMP_Stroage_SetRecordSegmentTime(long handle,
                                                           int timemin);

    public native int CHD_WMP_Storage_SetRecordType(long handle, int type);

    public native int CHD_WMP_Storage_SetRecordMDCondition(long handle,
                                                           int mdcond);

    public native int CHD_WMP_Storage_GetRecordState(long handle, int year,
                                                     int mon, byte[] date);

    public native int CHD_WMP_Storage_GetEveryDayOfVideo(long handle, int year,
                                                         int mon, int day, byte[] date);

    public native int CHD_WMP_Storage_Begin(long handle, st_DateInfo timestamp);

    public native int CHD_WMP_Storage_End(long handle);

    public native int CHD_WMP_Storage_RequestVideoData(long handle,
                                                       st_VideoFrame videoframe, st_DateInfo timestamp);

    public native int CHD_WMP_Storage_ReleaseVideoData(long handle,
                                                       st_VideoFrame videoframe);

    public native int CHD_WMP_Storage_RequestAudioData(long handle,
                                                       st_AudioFrame audioframe, byte[] data);


    /**
     * 视频运动检测相关 2个函数
     */
    public native int CHD_WMP_Video_GetMotionDetection(long handle,
                                                       st_VideoParamInfo param);

    public native int CHD_WMP_Video_SetMotionDetection(long handle,
                                                       st_VideoParamInfo param);


    /**
     * 警报设置相关 2个函数
     */
    public native int CHD_WMP_Alarm_GetParam(long handle, st_AlarmParam param);

    public native int CHD_WMP_Alarm_SetParam(long handle, st_AlarmParam param);


    /**
     * 配置相关 2个函数
     */
    public native int CHD_WMP_Config_SetResolu(long handle, int width, int height);


    /**
     * 文件操作相关 5个函数
     */
    public native int CHD_WMP_File_Save(String filename, int datalen,
                                        byte[] data);

    public native long CHD_WMP_File_GetSize(String filename);

    public native int CHD_WMP_File_Copy(String srcfilename, String destfilename);

    public native long CHD_WMP_Folder_GetSize(String dirname);

    public native int CHD_WMP_Folder_Copy(String srcdir, String destdir);

    public native String CHD_WMP_GetTimeMs();


    /**
     * 相册管理相关函数   8 个函数
     */
    public native int CHD_AlbumManage_SavePictureThumbnail(String filename, int w, int h, Bitmap bitmap);

    public native int CHD_AlbumManage_GetPictureWidth(String filename);

    public native int CHD_AlbumManage_GetPictureHeight(String filename);

    public native Bitmap CHD_AlbumManage_GetPictureThumbnail(String filename);


    public native int CHD_AlbumManage_SaveVideoThumbnail(String filename, int times, Bitmap bitmap);

    public native int CHD_AlbumManage_GetVideoTime(String filename);

    public native Bitmap CHD_AlbumManage_GetVideoThumbnail(String filename);

    public native int CHD_AlbumManage_DeleteFile(String filename);

    static {
        System.loadLibrary("nativeChdWmp");
    }
}

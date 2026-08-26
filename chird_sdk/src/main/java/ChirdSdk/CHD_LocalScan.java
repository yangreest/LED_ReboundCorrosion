package ChirdSdk;

import ChirdSdk.Apis.chd_wmp_apis;
import ChirdSdk.Apis.st_SearchInfo;

public class CHD_LocalScan {

    private static final int LOCAL_SCAN_TIME_S = 2000; // 本地定时扫描时间，不要太频繁，以免造成无线网络堵塞

    private Boolean isInit = false;

    private int LocalScanTimes = LOCAL_SCAN_TIME_S;

    private chd_wmp_apis WMP = new chd_wmp_apis();
    private st_SearchInfo DevInfo = new st_SearchInfo();

    public CHD_LocalScan() {
        isInit = true;
        LocalScanTimes = LOCAL_SCAN_TIME_S;
        WMP.CHD_WMP_ScanDevice_InitIndex(LocalScanTimes, 1);
    }

    /**
     * 本地扫描初始化
     *
     * @param scantimems :本地扫描间隔时间
     * @return null
     */
    public void CHD_LocalScanInit(int scantimems) {
        if (isInit) {
            WMP.CHD_WMP_ScanDevice_UnInit();
        }

        isInit = true;
        LocalScanTimes = scantimems;
        WMP.CHD_WMP_ScanDevice_InitIndex(LocalScanTimes, 1);
    }

    /**
     * 获取当前本地扫描的的设备个数
     *
     * @return null
     */
    public int getLocalScanNum() {
        if (!isInit) {
            isInit = true;
            WMP.CHD_WMP_ScanDevice_InitIndex(LocalScanTimes, 1);
        }

        int ret = WMP.CHD_WMP_Scan_GetDeviceInformation(DevInfo);
        if (ret < 0) {
            isInit = false;
            WMP.CHD_WMP_ScanDevice_UnInit();
        }

        return ret;
    }

    /**
     * 获取当前本地扫描到第num个设备的ID号
     *
     * @param Num 扫描到的第num个设备
     * @return null
     */
    public int getLocalDevId(int Num) {
        if (!isInit) {
            isInit = true;
            WMP.CHD_WMP_ScanDevice_InitIndex(LocalScanTimes, 1);
        }

        int Cnt = WMP.CHD_WMP_Scan_GetDeviceInformation(DevInfo);
        if (Num >= Cnt) {
            return -1;
        }

        return DevInfo.id[Num];
    }

    /**
     * 获取当前本地扫描到第num个设备的名称(此处不是设备wifi名称)
     *
     * @param Num 扫描到的第num个设备
     * @return null
     */
    public String getLocalDevAlias(int Num) {
        if (!isInit) {
            isInit = true;
            WMP.CHD_WMP_ScanDevice_InitIndex(LocalScanTimes, 1);
        }

        if (Num >= WMP.CHD_WMP_Scan_GetDeviceInformation(DevInfo)) {
            return null;
        }

        return DevInfo.alias[Num];
    }

    /**
     * 获取当前本地扫描到第num个设备的IP地址
     *
     * @param Num 扫描到的第num个设备
     * @return null
     */
    public String getLocalDevIpAddress(int Num) {
        if (!isInit) {
            isInit = true;
            WMP.CHD_WMP_ScanDevice_InitIndex(LocalScanTimes, 1);
        }

        if (Num >= WMP.CHD_WMP_Scan_GetDeviceInformation(DevInfo)) {
            return null;
        }

        return DevInfo.address[Num];
    }

    /**
     * 获取当前本地扫描到第num个设备的DID码(只有带远程的设备，此为客户定制功能，默认不开)
     *
     * @param Num 扫描到的第num个设备
     * @return null
     */
    public String getLocalDevDid(int Num) {
        if (!isInit) {
            isInit = true;
            WMP.CHD_WMP_ScanDevice_InitIndex(LocalScanTimes, 1);
        }

        if (Num >= WMP.CHD_WMP_Scan_GetDeviceInformation(DevInfo)) {
            return null;
        }

        return DevInfo.did[Num];
    }

    /**
     * 获取当前本地扫描到第num个设备的连接密码(此为客户定制功能，默认不开)
     *
     * @param Num 扫描到的第num个设备
     * @return null
     */
    public String getLocalDevPasswd(int Num) {
        if (!isInit) {
            isInit = true;
            WMP.CHD_WMP_ScanDevice_InitIndex(LocalScanTimes, 1);
        }

        if (Num >= WMP.CHD_WMP_Scan_GetDeviceInformation(DevInfo)) {
            return null;
        }

        return DevInfo.passwd[Num];
    }

    /**
     * 获取当前本地扫描到第num个设备版本号(此为客户定制功能，默认不开)
     *
     * @param Num 扫描到的第num个设备
     * @return null
     */
    public String getLocalDevVersion(int Num) {
        if (!isInit) {
            isInit = true;
            WMP.CHD_WMP_ScanDevice_InitIndex(LocalScanTimes, 1);
        }

        if (Num >= WMP.CHD_WMP_Scan_GetDeviceInformation(DevInfo)) {
            return null;
        }

        return Long.toString(DevInfo.version[Num] >> 16) + "."
                + Long.toString((DevInfo.version[Num] & 0x0000ff00) >> 8)
                + Long.toString(DevInfo.version[Num] & 0x000000ff);
    }

    /**
     * 销毁函数，关闭 本地扫描必须调用
     *
     * @return null
     */
    public void Destory() {
        if (isInit) {
            isInit = false;
            WMP.CHD_WMP_ScanDevice_UnInit();
        }
    }

    protected void finalize() {
        if (isInit) {
            isInit = false;
            WMP.CHD_WMP_ScanDevice_UnInit();
        }
    }
}

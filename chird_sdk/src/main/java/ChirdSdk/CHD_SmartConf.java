package ChirdSdk;

import ChirdSdk.Apis.chd_wmp_apis;

public class CHD_SmartConf {
    private chd_wmp_apis WMP = new chd_wmp_apis();

    boolean isOpen;

    /**
     * 设备出厂默认 AP 模式，一键配置模式和 AP 模式为二选一功能，一键配置模式下设备不发出 WIFI，只能通过此类接口配置设备连接路由器
     *
     * @return null
     */
    public CHD_SmartConf() {
        isOpen = false;
    }

    /**
     * 一键配置是否打开
     *
     * @return false(未开启) true(开启)
     */
    public boolean isOpen() {
        return isOpen;
    }

    /**
     * 打开一键配置(不可配置连接5.8G 路由器)
     *
     * @param RouterName   路由器名称
     * @param RouterPasswd 路由器密码
     * @param DevId        设备ID(预留，可输入"0")
     * @return -1(失败) 0(成功)
     */
    public int openSmartConfig(String RouterName, String RouterPasswd,
                               String DevId) {

        int ret = WMP
                .CHD_WMP_SmartConfig_Begin(RouterName, RouterPasswd, DevId);
        if (ret < 0) {
            return ret;
        }

        isOpen = true;

        return 0;
    }

    /**
     * 关闭一键配置(不需要一键配置功能一定要调用，否则会造成无线网络堵塞)
     *
     * @return -1(失败) 0(成功)
     */
    public int closeSmartConfig() {

        int ret = WMP.CHD_WMP_SmartConfig_End();
        if (ret < 0) {
            return ret;
        }

        isOpen = false;

        return 0;
    }

    protected void finalize() {

        if (isOpen) {
            WMP.CHD_WMP_SmartConfig_End();
        }
    }
}

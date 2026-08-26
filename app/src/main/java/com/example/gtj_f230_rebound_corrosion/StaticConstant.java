package com.example.gtj_f230_rebound_corrosion;

import android.annotation.SuppressLint;

import com.blackhao.utillibrary.usbHelper.UsbHelper;
import com.blankj.utilcode.util.SPUtils;
import com.example.gtj_f230_rebound_corrosion.rebound.model.RebounderConfigBean;
import com.printer.sdk.PrinterInstance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by wd_mac on 2018/12/29.
 */
@SuppressLint("SdCardPath")
public class StaticConstant {
    public static final String APP_CORROSION = "CORROSION";  //锈蚀
    public static final String APP_THICKNESS = "THICKNESS";  //厚度检测
    public static String APP_TYPE_DEFAULT = APP_CORROSION;  //当前版本

    public static final String APP_LIERDA = "立尔达";
    public static final String APP_DIANKEYUAN = "电科院";
    public static final String APP_MINGSHENGHENGZHUO = "明生恒卓";
    public static final String APP_GTJ = "高铁建";
    public static String APP_LOGO_DEFAULT = APP_LIERDA;  //当前版本
    //发布版本
    public static final boolean isRelease = true;

    public static final boolean isLock = true;
    //中性版本
    public static final boolean isNeutral = false;
    public static final String appName = "SD-DG480";

    public static final String pwd = "123456";
    public static final String pwd1 = "123456789";
    public static UsbHelper usbHelper;

    //数据库图片文件
    public static final String dataFilePathCache = "/sdcard/cache/gtj_f230_rebound_corrosion/";
    //数据文件生成地址
    public static String dataFilePathExport = "/sdcard/App/" + appName + "/";
    public static final String dataFilePathExportUSb = appName + "/";

    //崩溃信息存储地址
    public static final String crashFilePath = "/sdcard/gtj_crash/gtj_f230_rebound_corrosion/";
    //崩溃信息是否存文件
    public static final boolean CRASH_DEBUG = true;

    //是否打印日志
    public static final boolean LOGGER_DEBUG = true;
    public static PrinterInstance printerInstance;
    //百度地图定位
    public static double mapLatitude;
    public static double mapLontitude;

    //广播ACTION
    public static final String ACTION_TESTING = "action.activity.testing";
    public static final String ACTION_DATA_CHANGE = "action.activity.data.change";
    //ActivityResult
    public static final int INT_REQUESTCODE_1 = 10001;

    public static List<String> imageModifyList = new ArrayList<>();
    public static int imageModifyIndex;

    //电杆锈蚀仪
    public static final String USR_SERVICE = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public static final String USR_CHARACTER_NOTIFY = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public static final String USR_CHARACTER_WRITE = "0000ffe1-0000-1000-8000-00805f9b34fb";

    public static final List<String> detectionType = Arrays.asList("预制梁", "预制板", "预制柱", "预制墙", "现浇梁", "现浇板", "现浇柱", "现浇墙", "预制桁架", "其他");
    public static final List<String> detectionSurface = Arrays.asList("表面", "底面", "侧面");
    public static final List<String> pouringSurface = Arrays.asList("浇筑侧面", "浇筑底面");
    private static final List<String> detectionAngle = Arrays.asList("0", "30", "45", "60", "90", "-30", "-45", "-60", "-90");
    private static final List<String> detectionAngle1 = Arrays.asList("水平 0", "向上 30", "向上 45", "向上 60", "向上 90", "向下 30", "向下 45", "向下 60", "向下 90");
    private static final List<String> detectionAngleGX = Arrays.asList("0", "90");
    private static final List<String> detectionAngleGX1 = Arrays.asList("水平 0", "向上 90");
    private static final List<String> detectionAngleGZ = Arrays.asList("0", "15", "30", "45", "60", "75", "90", "-15", "-30", "-45", "-60", "-75", "-90");
    private static final List<String> detectionAngleGZ1 = Arrays.asList("水平 0", "向上 15", "向上 30", "向上 45", "向上 60", "向上 75", "向上 90", "向下 15", "向下 30", "向下 45", "向下 60", "向下 75", "向下 90");
    public static final List<String> detectionPumping = Arrays.asList("否", "是");
    public static final List<String> detectionStandard = Arrays.asList("国家统一新版", "国家高强度4.5J", "国家高强度5.5J", "北京曲线", "河北曲线", "河南商品混凝土曲线", "山东曲线", "山东高强度曲线", "青岛曲线", "青岛高强度曲线", "辽宁曲线", "安徽低强度", "安徽高强度", "江苏泵送", "浙江卵石", "浙江碎石", "上海结构混凝土曲线", "福建卵石", "福建碎石", "福建泵送", "陕西泵送曲线", "宁夏泵送", "甘肃庆阳预拌混凝土曲线", "四川高强度", "贵州曲线", "水运工程2.207J", "铁路工程曲线", "广西泵送2023");
    private static final List<RebounderConfigBean> rebounderConfigBeanList = new ArrayList<>();
    private static final double[] dm_guizhou = {1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0, 5.5, 6.0, 7.0, 8.0, 9.0, 10.0};
    private static final double[] dm_standard = {0, 0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0, 5.5, 6.0, 6.5, 7.0, 7.5, 8.0};
    private static final double[][] waterTransport = new double[4][6];
    private static final double[][] railway = new double[2][7];

    //电杆类型
    public static final String poleType_PT = "普通混凝土电杆";
    public static final String poleType_Y = "预应力混凝土电杆Y";
    public static final String poleType_BY = "部分预应力混凝土电杆BY";
    public static final List<String> configPoleTypeList = Arrays.asList(poleType_PT, poleType_Y, poleType_BY);
    //电杆结构
    public static final String poleStructure_ZX = "锥形杆";
    public static final String poleStructure_FL = "法兰杆";
    public static final String poleStructure_DJ = "等径杆";
    public static final List<String> configpoleStructureList = Arrays.asList(poleStructure_ZX, poleStructure_FL, poleStructure_DJ);
    //
    public static List<String> configList = Arrays.asList("190mm，10米杆，I级", "190mm，12米杆，M级", "190mm，15米杆，M级", "Z Ø150×8×G×Y", "Z Ø190×10×K×Y", "Z Ø190×12×K×Y", "Z Ø190×15×K×Y", "Z Ø190×12(6+6)×K×Y", "Z Ø190×15(9+6)×K×Y", "Z Ø190×12×100×BY", "Z Ø190×12×M×BY", "Z Ø190×15×M×BY", "Z Ø230×10×O×BY", "Z Ø230×12×125×BY", "Z Ø230×15×150×BY", "Z Ø350×15×U×BY", "Z Ø190×15(9+6)×100×BY", "Z Ø190×18(9+9)×M×BY", "Z Ø310×12(6+6)×O×BY", "Z Ø310×12(6+6)×U×BY", "Z Ø230×12(6+6)×O×BY");
    //规范
    public static final List<String> poleStandardList = List.of("GB/T 4623-2014 环形混凝土电杆");
    //纵筋直径
//    public static final List<String> rebarDiameterList = Arrays.asList("7", "9", "10", "11", "14");
    public static final List<String> rebarDiameterList = Arrays.asList("6", "7", "8", "9", "10", "11", "12", "14", "16", "18");
    public static final List<String> rebarDiameterList1 = Arrays.asList("7", "8", "9", "10", "11", "12", "14", "16");  //计量
    public static final List<String> hoopingDiameterList = Arrays.asList("3", "4");

    //电杆类型
    public static final List<String> poleTypeList = Arrays.asList("普通锥形杆", "法兰式锥形杆", "等径杆");

    //数据库名称
    public static final String DB_NAME = "gtj_f230_rebound_corrosion.db";

    public static double matrixRatio = 480 / 640.0;  //屏幕占比：3/4

    public static String SP_F230_WIFI_NAME = "wifi-name";
    public static String SP_REBOUND_BLE_NAME = "rebound_ble_name";
    public static String SP_REBOUND_BLE_MAC = "rebound_ble_mac";
    public static String SP_CORROSION_BLE_NAME = "corrosion_ble_name";
    public static String SP_CORROSION_BLE_MAC = "corrosion_ble_mac";

    public static String SP_REBOUND_LAST_CONFIG = "detect_rebound_last_config";
    public static String SP_CORROSION_LAST_CONFIG = "corrosion_last_config";

    //1：中文；2：英文
    private static int languageType;

    public static boolean getLanguage() {
        if (languageType == 0) {
            //1：中文；2：英文
            languageType = SPUtils.getInstance().getInt("languageType", 1);
        }
        return languageType == 1;
    }

    public static List<String> getDetectionAngle(int standardId) {
        switch (standardId) {
            case 25:
                return detectionAngleGZ1;
            case 28:
                return detectionAngleGX1;
            default:
                return detectionAngle1;
        }
    }

    public static List<String> getDetectionAngleCompute(int standardId) {
        switch (standardId) {
            case 25:
                return detectionAngleGZ;
            case 28:
                return detectionAngleGX;
            default:
                return detectionAngle;
        }
    }

    public static List<RebounderConfigBean> getRebounderConfigBeanList() {
        if (rebounderConfigBeanList.size() == 0) {
            List<RebounderConfigBean.RConfigBean> tempList = new ArrayList<>();
            tempList.add(new RebounderConfigBean.RConfigBean(0, false, 1, false, 3, false, 0));
            rebounderConfigBeanList.add(new RebounderConfigBean("国家高强度4.5J", false, tempList));
            rebounderConfigBeanList.add(new RebounderConfigBean("国家高强度5.5J", false, tempList));
            tempList.clear();
            tempList.add(new RebounderConfigBean.RConfigBean(1, false, 1, true, 3, true, 8));
            rebounderConfigBeanList.add(new RebounderConfigBean("北京曲线", false, tempList));
            tempList.clear();
            tempList.add(new RebounderConfigBean.RConfigBean(0, true, 1, true, 3, true, 6));
            rebounderConfigBeanList.add(new RebounderConfigBean("河南商品混凝土曲线", false, tempList));
            tempList.clear();
            tempList.add(new RebounderConfigBean.RConfigBean(0, true, 1, true, 3, false, 0));
            rebounderConfigBeanList.add(new RebounderConfigBean("山东高强度曲线", false, tempList));
            rebounderConfigBeanList.add(new RebounderConfigBean("青岛高强度曲线", false, tempList));
            tempList.clear();
            tempList.add(new RebounderConfigBean.RConfigBean(1, true, 1, true, 3, false, 6));
            rebounderConfigBeanList.add(new RebounderConfigBean("辽宁曲线", false, tempList));
            tempList.clear();
            tempList.add(new RebounderConfigBean.RConfigBean(1, true, 1, false, 3, true, 6));
            rebounderConfigBeanList.add(new RebounderConfigBean("安徽低强度", false, tempList));
            tempList.clear();
            tempList.add(new RebounderConfigBean.RConfigBean(1, false, 1, false, 3, true, 6));
            rebounderConfigBeanList.add(new RebounderConfigBean("安徽高强度", false, tempList));
            tempList.clear();
            tempList.add(new RebounderConfigBean.RConfigBean(1, false, 1, false, 3, true, 6));
            rebounderConfigBeanList.add(new RebounderConfigBean("江苏泵送", false, tempList));
            tempList.clear();
            tempList.add(new RebounderConfigBean.RConfigBean(1, false, 1, false, 3, true, 6));
            rebounderConfigBeanList.add(new RebounderConfigBean("浙江卵石", false, tempList));
            tempList.clear();
            tempList.add(new RebounderConfigBean.RConfigBean(1, false, 1, false, 3, true, 8));
            rebounderConfigBeanList.add(new RebounderConfigBean("浙江碎石", false, tempList));
            tempList.clear();
            tempList.add(new RebounderConfigBean.RConfigBean(0, true, 1, true, 3, true, 6));
            rebounderConfigBeanList.add(new RebounderConfigBean("上海结构混凝土曲线", false, tempList));
            tempList.clear();
            tempList.add(new RebounderConfigBean.RConfigBean(0, true, 1, true, 3, true, 6));
            rebounderConfigBeanList.add(new RebounderConfigBean("福建卵石", false, tempList));
            rebounderConfigBeanList.add(new RebounderConfigBean("福建碎石", false, tempList));
            tempList.clear();
            tempList.add(new RebounderConfigBean.RConfigBean(1, true, 1, true, 3, true, 6));
            rebounderConfigBeanList.add(new RebounderConfigBean("福建泵送", false, tempList));
            tempList.clear();
            tempList.add(new RebounderConfigBean.RConfigBean(1, true, 1, true, 3, true, 6));
            rebounderConfigBeanList.add(new RebounderConfigBean("陕西泵送曲线", false, tempList));
            rebounderConfigBeanList.add(new RebounderConfigBean("宁夏泵送", false, tempList));
            tempList.clear();
            tempList.add(new RebounderConfigBean.RConfigBean(1, true, 1, true, 3, true, 6));
            rebounderConfigBeanList.add(new RebounderConfigBean("甘肃庆阳预拌混凝土曲线", false, tempList));
            tempList.clear();
            tempList.add(new RebounderConfigBean.RConfigBean(0, false, 1, false, 3, true, 2));
            rebounderConfigBeanList.add(new RebounderConfigBean("四川高强度", false, tempList));
            tempList.clear();
            tempList.add(new RebounderConfigBean.RConfigBean(0, true, 1, true, 3, true, 6));
            rebounderConfigBeanList.add(new RebounderConfigBean("水运工程2.207J", false, tempList));
            tempList.clear();
            tempList.add(new RebounderConfigBean.RConfigBean(1, true, 1, true, 3, false, 6));
            rebounderConfigBeanList.add(new RebounderConfigBean("广西泵送2023", false, tempList));
        }
        return rebounderConfigBeanList;
    }

    public static double[] getDSCarbonization(int standard) {
        if (standard == 25) {
            return dm_guizhou;
        } else {
            return dm_standard;
        }
    }

    public static List<String> getDetectionStandardCarbonization(int standard) {
        List<String> tempList = new ArrayList<>();
        if (standard == 25) {
            for (int i = 0; i < dm_guizhou.length; i++) {
                tempList.add(dm_guizhou[i] + "");
            }
        } else {
            for (int i = 0; i < dm_standard.length; i++) {
                tempList.add(dm_standard[i] + "");
            }
        }
        return tempList;
    }

    public static double[][] getWaterTransport() {
        if (waterTransport[0][0] == 0) {
            waterTransport[0][0] = 0.95;
            waterTransport[0][1] = 0.90;
            waterTransport[0][2] = 0.85;
            waterTransport[0][3] = 0.80;
            waterTransport[0][4] = 0.75;
            waterTransport[0][5] = 0.70;
            waterTransport[1][0] = 0.94;
            waterTransport[1][1] = 0.88;
            waterTransport[1][2] = 0.82;
            waterTransport[1][3] = 0.75;
            waterTransport[1][4] = 0.73;
            waterTransport[1][5] = 0.65;
            waterTransport[2][0] = 0.93;
            waterTransport[2][1] = 0.86;
            waterTransport[2][2] = 0.80;
            waterTransport[2][3] = 0.73;
            waterTransport[2][4] = 0.68;
            waterTransport[2][5] = 0.60;
            waterTransport[3][0] = 0.92;
            waterTransport[3][1] = 0.84;
            waterTransport[3][2] = 0.78;
            waterTransport[3][3] = 0.71;
            waterTransport[3][4] = 0.65;
            waterTransport[3][5] = 0.58;
        }
        return waterTransport;
    }

    public static double[][] getRailway() {
        if (railway[0][0] == 0) {
            railway[0][0] = 4.5;
            railway[0][1] = 4.5;
            railway[0][2] = 4.5;
            railway[0][3] = 3.0;
            railway[0][4] = 1.5;
            railway[0][5] = 0;
            railway[0][6] = 0;
            railway[1][0] = 3.0;
            railway[1][1] = 1.5;
            railway[1][2] = 0;
            railway[1][3] = 0;
            railway[1][4] = 0;
            railway[1][5] = 0;
            railway[1][6] = 0;
        }
        return railway;
    }
}

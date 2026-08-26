package com.example.gtj_f230_rebound_corrosion.base.utils.rectify;

import android.annotation.SuppressLint;
import android.content.Context;

import com.example.gtj_f230_rebound_corrosion.base.utils.StringUtils;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.rebound.model.RebounderBean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;

public class RebounderRectifyUtils {
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;


    public static void initRectifyUtils(Context context) {
        mContext = context.getApplicationContext();
    }

    public static RebounderBean computeUtils(RebounderBean rebounderBean) {
        Integer[] testingArray = new Integer[rebounderBean.testNumber.size()];
        rebounderBean.testNumber.toArray(testingArray);
        //计算16个回弹值的平均值
        double rectifyAverage = RebounderUtils.getUncorrectedAverage(rebounderBean.standardId, testingArray);
        rebounderBean.average = rectifyAverage;
        //
        if (rebounderBean.isPumping == 1) {
            switch (rebounderBean.standardId) {
                case 1:  //国家统一新版（泵送）10MPa～60 MPa
                {
                    //非水平方向平均回弹值角度修正（20-50°）
                    double value = rectifyAverage;
                    value = value < 20 ? 20 : value;
                    value = value > 50 ? 50 : value;
                    value = getAngle(value, Integer.parseInt(StaticConstant.getDetectionAngleCompute(rebounderBean.standardId).get(rebounderBean.angle - 1)));
                    rebounderBean.calAngle = value;
                    double rectifyAngle = rectifyAverage + value;  //Rm = Rmα+Raα;
                    //浇筑面(表面、底面)平均回弹值修正
                    value = getSurface(rectifyAngle, rebounderBean.surface, rebounderBean.standardId, rebounderBean.isPumping);
                    rebounderBean.calSurface = value;
                    double rectifySurface = rectifyAngle + value;
                    //混凝土强度碳化计算
                    double coefficient1, coefficient2, coefficient3;
                    coefficient1 = 0.034488;
                    coefficient2 = 1.9400;
                    coefficient3 = -0.0173;
                    double dm = rebounderBean.carbonization >= 6 ? 6 : rebounderBean.carbonization;
                    rebounderBean.carbonize = getPumpingCarbonize(rectifySurface, dm, coefficient1, coefficient2, coefficient3);
                }
                break;
                case 2:  //国家高强度4.5J（泵送）20MPa～110 MPa
                    //暂无
                    break;
                case 3:  //国家高强度5.5J（泵送）60.2MPa～79.9 MPa
                    //暂无
                    break;
                case 4:  //北京曲线（泵送）15MPa～60 MPa
                {
                    //混凝土强度碳化计算
                    double coefficient1, coefficient2, coefficient3;
                    switch (rebounderBean.surface) {
                        case 1:  //表面
                            coefficient1 = 0.462321;
                            coefficient2 = 1.2824;
                            coefficient3 = -0.0092;
                            break;
                        case 2:  //底面
                            coefficient1 = 0.030685;
                            coefficient2 = 1.9030;
                            coefficient3 = -0.0097;
                            break;
                        default:  //侧面
                            coefficient1 = 0.210595;
                            coefficient2 = 1.4863;
                            coefficient3 = -0.0062;
                            break;
                    }
                    double dm = rebounderBean.carbonization >= 8 ? 8 : rebounderBean.carbonization;
                    rebounderBean.carbonize = getPumpingCarbonize(rectifyAverage, dm, coefficient1, coefficient2, coefficient3);
                }
                break;
                case 5:  //河北曲线（泵送）10MPa～60 MPa
                {
                    //混凝土强度碳化计算
                    double coefficient1, coefficient2, coefficient3;
                    if (rebounderBean.pouringSurface == 1) {
                        //浇筑底面
                        coefficient1 = 0.020870;
                        coefficient2 = 2.011928;
                        coefficient3 = -0.014850;
                    } else {
                        //浇筑侧面
                        coefficient1 = 0.030111;
                        coefficient2 = 1.991718;
                        coefficient3 = -0.017347;
                        //非水平方向平均回弹值角度修正（20-50°）
                        double value = rectifyAverage;
                        value = value < 20 ? 20 : value;
                        value = value > 50 ? 50 : value;
                        value = getAngle(value, Integer.parseInt(StaticConstant.getDetectionAngleCompute(rebounderBean.standardId).get(rebounderBean.angle - 1)));
                        rebounderBean.calAngle = value;
                        rectifyAverage = rectifyAverage + value;  //Rm = Rmα+Raα;
                        //浇筑面(表面、底面)平均回弹值修正
                        value = getSurface(rectifyAverage, rebounderBean.surface, rebounderBean.standardId, rebounderBean.isPumping);
                        rebounderBean.calSurface = value;
                        rectifyAverage = rectifyAverage + value;
                    }
                    double dm = rebounderBean.carbonization >= 6 ? 6 : rebounderBean.carbonization;
                    rebounderBean.carbonize = getPumpingCarbonize(rectifyAverage, dm, coefficient1, coefficient2, coefficient3);
                }
                break;
                case 6:  //河南商品混凝土曲线（泵送）15MPa～80 MPa
                    //暂无
                    break;
                case 7:  //山东曲线（泵送）10MPa～60 MPa M225型修正
                {
                    //非水平方向平均回弹值角度修正（20-56°）
                    double value = rectifyAverage;
                    value = value < 20 ? 20 : value;
                    value = value > 56 ? 56 : value;
                    value = getAngle(value, Integer.parseInt(StaticConstant.getDetectionAngleCompute(rebounderBean.standardId).get(rebounderBean.angle - 1)));
                    rebounderBean.calAngle = value;
                    double rectifyAngle = rectifyAverage + value;  //Rm = Rmα+Raα;
                    //浇筑面(表面、底面)平均回弹值修正
                    value = getSurface(rectifyAngle, rebounderBean.surface, rebounderBean.standardId, rebounderBean.isPumping);
                    rebounderBean.calSurface = value;
                    double rectifySurface = rectifyAngle + value;
                    //混凝土强度碳化计算
                    double coefficient1, coefficient2, coefficient3;
                    coefficient1 = 0.01374;
                    coefficient2 = 2.19;
                    coefficient3 = -0.0153;
                    double dm = rebounderBean.carbonization >= 6 ? 6 : rebounderBean.carbonization;
                    double carbonize = getPumpingCarbonize(rectifySurface, dm, coefficient1, coefficient2, coefficient3);
                    //混凝土修正
                    if (carbonize >= 10 && carbonize < 60) {
                        if (carbonize <= 15) {
                            carbonize = carbonize * (1.5357 - 0.0357 * carbonize);
                        } else if (carbonize >= 50) {
                            carbonize = carbonize * (1.3571 - 0.0071 * carbonize);
                        }
                    }
                    rebounderBean.carbonize = carbonize;
                }
                break;
                case 8:  //山东高强度曲线（泵送）60MPa～80 MPa
                    //暂无
                    break;
                case 9:  //青岛曲线（泵送）10MPa～60 MPa M225型修正
                {
                    //非水平方向平均回弹值角度修正（20-56°）
                    double value = rectifyAverage;
                    value = value < 20 ? 20 : value;
                    value = value > 56 ? 56 : value;
                    value = getAngle(value, Integer.parseInt(StaticConstant.getDetectionAngleCompute(rebounderBean.standardId).get(rebounderBean.angle - 1)));
                    rebounderBean.calAngle = value;
                    double rectifyAngle = rectifyAverage + value;  //Rm = Rmα+Raα;
                    //浇筑面(表面、底面)平均回弹值修正
                    value = getSurface(rectifyAngle, rebounderBean.surface, rebounderBean.standardId, rebounderBean.isPumping);
                    rebounderBean.calSurface = value;
                    double rectifySurface = rectifyAngle + value;
                    //混凝土强度碳化计算
                    double coefficient1, coefficient2, coefficient3;
                    coefficient1 = 0.0104;
                    coefficient2 = 2.2804;
                    coefficient3 = -0.0242;
                    double dm = rebounderBean.carbonization >= 6 ? 6 : rebounderBean.carbonization;
                    double carbonize = getPumpingCarbonize(rectifySurface, dm, coefficient1, coefficient2, coefficient3);
                    //混凝土修正
                    if (carbonize >= 10 && carbonize < 60) {
                        if (carbonize <= 15) {
                            carbonize = carbonize * (1.5357 - 0.0357 * carbonize);
                        } else if (carbonize >= 50) {
                            carbonize = carbonize * (1.3571 - 0.0071 * carbonize);
                        }
                    }
                    rebounderBean.carbonize = carbonize;
                }
                break;
                case 10:  //青岛高强度曲线（泵送）60MPa～80 MPa
                    //暂无
                    break;
                case 11:  //辽宁曲线（泵送）10MPa～60 MPa
                {
                    //非水平方向平均回弹值角度修正（20-50°）
                    double value = rectifyAverage;
                    value = value < 20 ? 20 : value;
                    value = value > 50 ? 50 : value;
                    value = getAngle(value, Integer.parseInt(StaticConstant.getDetectionAngleCompute(rebounderBean.standardId).get(rebounderBean.angle - 1)));
                    rebounderBean.calAngle = value;
                    double rectifyAngle = rectifyAverage + value;  //Rm = Rmα+Raα;
                    //浇筑面(表面、底面)平均回弹值修正
                    value = getSurface(rectifyAngle, rebounderBean.surface, rebounderBean.standardId, rebounderBean.isPumping);
                    rebounderBean.calSurface = value;
                    double rectifySurface = rectifyAngle + value;
                    //混凝土强度碳化计算
                    double coefficient1, coefficient2, coefficient3;
                    coefficient1 = 0.049642;
                    coefficient2 = 1.833555;
                    coefficient3 = -0.01794;
                    double dm = rebounderBean.carbonization >= 6 ? 6 : rebounderBean.carbonization;
                    rebounderBean.carbonize = getPumpingCarbonize(rectifySurface, dm, coefficient1, coefficient2, coefficient3);
                }
                break;
                case 12:  //安徽低强度（泵送）10MPa～60 MPa
                {
                    //非水平方向平均回弹值角度修正（20-50°）
                    double value = rectifyAverage;
                    value = value < 20 ? 20 : value;
                    value = value > 50 ? 50 : value;
                    value = getAngle(value, Integer.parseInt(StaticConstant.getDetectionAngleCompute(rebounderBean.standardId).get(rebounderBean.angle - 1)));
                    double rectifyAngle = rectifyAverage + value;  //Rm = Rmα+Raα;
                    rebounderBean.calAngle = value;
                    //混凝土强度碳化计算
                    double coefficient1, coefficient2, coefficient3;
                    coefficient1 = 0.03622;
                    coefficient2 = 1.949;
                    coefficient3 = -0.01650;
                    double dm = rebounderBean.carbonization >= 6 ? 6 : rebounderBean.carbonization;
                    rebounderBean.carbonize = getPumpingCarbonize(rectifyAngle, dm, coefficient1, coefficient2, coefficient3);
                }
                break;
                case 13:  //安徽高强度（泵送）60MPa～90 MPa
                {
                    double dm = rebounderBean.carbonization >= 6 ? 6 : rebounderBean.carbonization;
                    rebounderBean.carbonize = getHighRm3(rectifyAverage, dm, 32.4922, 0.021, 0.051);
                }
                break;
                case 14:  //江苏泵送（泵送）10MPa～60 MPa
                {
                    //混凝土强度碳化计算
                    double coefficient1, coefficient2, coefficient3;
                    coefficient1 = 0.407680;
                    coefficient2 = 1.26881;
                    coefficient3 = -0.01272;
                    double dm = rebounderBean.carbonization >= 6 ? 6 : rebounderBean.carbonization;
                    rebounderBean.carbonize = getPumpingCarbonize(rectifyAverage, dm, coefficient1, coefficient2, coefficient3);
                }
                break;
                case 15:  //浙江卵石（泵送）10MPa～60 MPa
                {
                    //混凝土强度碳化计算
                    double dm = rebounderBean.carbonization >= 6 ? 6 : rebounderBean.carbonization;
                    rebounderBean.carbonize = getCarbonize(rectifyAverage, dm, rebounderBean.standardId);
                }
                break;
                case 16:  //浙江碎石（泵送）15MPa～80 MPa
                {
                    //混凝土强度碳化计算
                    double coefficient1, coefficient2, coefficient3, dm;
                    if (rebounderBean.carbonization >= 8) {
                        //碳化深度 8.0mm 以上取 8mm
                        dm = 8;
                        coefficient1 = 0.072028;
                        coefficient2 = 1.7639;
                        coefficient3 = -0.0113;
                    } else {
                        dm = rebounderBean.carbonization >= 6 ? 6 : rebounderBean.carbonization;
                        coefficient1 = 0.071262;
                        coefficient2 = 1.7682;
                        coefficient3 = -0.0135;
                    }
                    rebounderBean.carbonize = getPumpingCarbonize(rectifyAverage, dm, coefficient1, coefficient2, coefficient3);
                }
                break;
                case 17:  //上海结构混凝土曲线（泵送）10MPa～70 MPa
                    //暂无
                    break;
                case 18:  //福建卵石（泵送）10MPa～60 MPa
                    //暂无
                    break;
                case 19:  //福建碎石（泵送）10MPa～60 MPa
                    //暂无
                    break;
                case 20:  //福建泵送（泵送）10MPa～60 MPa
                {
                    //非水平方向平均回弹值角度修正（20-55°）
                    double value = rectifyAverage;
                    if (value >= 54 && value <= 55) {
                        value = 55;
                    }
                    value = value < 20 ? 20 : value;
                    value = value > 55 ? 55 : value;
                    value = getAngle(value, Integer.parseInt(StaticConstant.getDetectionAngleCompute(rebounderBean.standardId).get(rebounderBean.angle - 1)));
                    rebounderBean.calAngle = value;
                    double rectifyAngle = rectifyAverage + value;  //Rm = Rmα+Raα;
                    //浇筑面(表面、底面)平均回弹值修正
                    value = getSurface(rectifyAngle, rebounderBean.surface, rebounderBean.standardId, rebounderBean.isPumping);
                    rebounderBean.calSurface = value;
                    double rectifySurface = rectifyAngle + value;
                    //混凝土强度碳化计算
                    double coefficient1, coefficient2, coefficient3;
                    coefficient1 = 0.024408;
                    coefficient2 = 2.03222;
                    coefficient3 = -0.010737;
                    double dm = rebounderBean.carbonization >= 6 ? 6 : rebounderBean.carbonization;
                    rebounderBean.carbonize = getPumpingCarbonize(rectifySurface, dm, coefficient1, coefficient2, coefficient3);
                }
                break;
                case 21:  //陕西泵送曲线（泵送）10MPa～60 MPa
                {
                    //非水平方向平均回弹值角度修正（20-50°）
                    double value = rectifyAverage;
                    value = value < 20 ? 20 : value;
                    value = value > 50 ? 50 : value;
                    value = getAngle(value, Integer.parseInt(StaticConstant.getDetectionAngleCompute(rebounderBean.standardId).get(rebounderBean.angle - 1)));
                    rebounderBean.calAngle = value;
                    double rectifyAngle = rectifyAverage + value;  //Rm = Rmα+Raα;
                    //浇筑面(表面、底面)平均回弹值修正
                    value = getSurface(rectifyAngle, rebounderBean.surface, rebounderBean.standardId, rebounderBean.isPumping);
                    rebounderBean.calSurface = value;
                    double rectifySurface = rectifyAngle + value;
                    //混凝土强度碳化计算
                    double coefficient1, coefficient2, coefficient3;
                    coefficient1 = 0.080716;
                    coefficient2 = 1.70131096;
                    coefficient3 = -0.0240119;
                    double dm = rebounderBean.carbonization >= 6 ? 6 : rebounderBean.carbonization;
                    rebounderBean.carbonize = getPumpingCarbonize(rectifySurface, dm, coefficient1, coefficient2, coefficient3);
                }
                break;
                case 22:  //宁夏泵送（泵送）//TODO 暂无 现使用国家标准
                {
                    //非水平方向平均回弹值角度修正
                    double value = rectifyAverage;
                    value = value < 20 ? 20 : value;
                    value = value > 50 ? 50 : value;
                    value = getAngle(value, Integer.parseInt(StaticConstant.getDetectionAngleCompute(rebounderBean.standardId).get(rebounderBean.angle - 1)));
                    rebounderBean.calAngle = value;
                    double rectifyAngle = rectifyAverage + value;  //Rm = Rmα+Raα;
                    //浇筑面(表面、底面)平均回弹值修正
                    value = getSurface(rectifyAngle, rebounderBean.surface, 22, rebounderBean.isPumping);
                    rebounderBean.calSurface = value;
                    double rectifySurface = rectifyAngle + value;
                    //混凝土强度碳化计算
                    double coefficient1, coefficient2, coefficient3;
                    coefficient1 = 0.034488;
                    coefficient2 = 1.9400;
                    coefficient3 = -0.0173;
                    double dm = rebounderBean.carbonization >= 6 ? 6 : rebounderBean.carbonization;
                    rebounderBean.carbonize = getPumpingCarbonize(rectifySurface, dm, coefficient1, coefficient2, coefficient3);
                }
                break;
                case 23:  //甘肃庆阳预拌混凝土曲线（泵送）
                    //暂无
                    break;
                case 24:  //四川高强度 5.5J（泵送）50MPa～100 MPa （构件中 出现测区强度无法查出(如 c cu f ＜50.0MPa 或 c cu f ＞100.0MPa)时，因无法计算平均值及标准差，也只能 24 以最小值作为该强度推定值）
                    //暂无
                    break;
                case 25:  //贵州曲线（泵送）10MPa～60 MPa
                {
                    //非水平方向平均回弹值角度修正（20-50°）
                    double value = rectifyAverage;
                    value = value < 20 ? 20 : value;
                    value = value > 50 ? 50 : value;
                    value = getAngle(value, Integer.parseInt(StaticConstant.getDetectionAngleCompute(rebounderBean.standardId).get(rebounderBean.angle - 1)));
                    rebounderBean.calAngle = value;
                    double rectifyAngle = rectifyAverage + value;  //Rm = Rmα+Raα;
                    //浇筑面(表面、底面)平均回弹值修正
                    value = getSurface(rectifyAngle, rebounderBean.surface, rebounderBean.standardId, rebounderBean.isPumping);
                    rebounderBean.calSurface = value;
                    double rectifySurface = rectifyAngle + value;
                    //混凝土强度碳化计算
                    double coefficient1, coefficient2, coefficient3;
                    coefficient1 = 0.015833;
                    coefficient2 = 2.1893;
                    coefficient3 = -0.0100;
                    double dm = rebounderBean.carbonization >= 10 ? 10 : rebounderBean.carbonization;
                    rebounderBean.carbonize = getPumpingCarbonize(rectifySurface, dm, coefficient1, coefficient2, coefficient3);
                }
                break;
                case 26:  //水运工程2.207J（泵送）10MPa～60 MPa
                    //暂无
                    break;
                case 27:  //铁路工程曲线（泵送）10MPa～60 MPa
                {
                    //非水平方向平均回弹值角度修正（20-50°）
                    double value = rectifyAverage;
                    value = value < 20 ? 20 : value;
                    value = value > 50 ? 50 : value;
                    value = getAngle(value, Integer.parseInt(StaticConstant.getDetectionAngleCompute(rebounderBean.standardId).get(rebounderBean.angle - 1)));
                    rebounderBean.calAngle = value;
                    double rectifyAngle = rectifyAverage + value;  //Rm = Rmα+Raα;
                    //浇筑面(表面、底面)平均回弹值修正
                    value = getSurface(rectifyAngle, rebounderBean.surface, rebounderBean.standardId, rebounderBean.isPumping);
                    rebounderBean.calSurface = value;
                    double rectifySurface = rectifyAngle + value;
                    //混凝土强度碳化计算
                    double dm = rebounderBean.carbonization >= 6 ? 6 : rebounderBean.carbonization;
                    double conversionValue = getCarbonize(rectifySurface, dm, rebounderBean.standardId);
                    rebounderBean.carbonize = reviseRailway(conversionValue, dm);//泵送修正
                }
                break;
                case 28:  //广西泵送2023（泵送）
                {
                    double rectifyAngle = rectifyAverage;
                    //浇筑面平均回弹值修正
                    if (rebounderBean.surface == 1) {  //表面
                        double value = rectifyAverage;
                        value = value < 20 ? 20 : value;
                        value = value > 50 ? 50 : value;
                        value = getSurface(value, rebounderBean.surface, rebounderBean.standardId, rebounderBean.isPumping);
                        rebounderBean.calSurface = value;
                        double rectifySurface = rectifyAverage + value;
                        //90°板底垂直向上修正
                        if (rebounderBean.angle == 2) {
                            rectifyAngle = StringUtils.getRounding(1.1643 * rectifySurface - 12.497, 1);
                            rebounderBean.calAngle = StringUtils.getRounding(rectifyAngle - rectifySurface, 1);
                        } else {
                            rectifyAngle = rectifySurface;
                            rebounderBean.calAngle = 0;
                        }
                    } else if (rebounderBean.surface == 2) {  //底面
                        rebounderBean.calAngle = 0;
                        if (rebounderBean.angle == 1) {  //底面立起来横向水平0°修正
                            double value = getSurface(rectifyAverage, rebounderBean.surface, rebounderBean.standardId, rebounderBean.isPumping);
                            rectifyAngle = rectifyAverage + value;
                            rebounderBean.calSurface = value;
                        } else if (rebounderBean.angle == 2) {
                            //90°板底垂直向上修正
                            // y = 1.1643x - 12.497
                            rectifyAngle = StringUtils.getRounding(1.1643 * rectifyAverage - 12.497, 1);
                            rebounderBean.calSurface = StringUtils.getRounding(rectifyAngle - rectifyAverage, 1);
                        }
                    } else if (rebounderBean.surface == 3) {  //侧面
                        rebounderBean.calSurface = 0;
                        rebounderBean.calAngle = 0;
                        if (rebounderBean.angle == 2) {
                            //90°板底垂直向上修正
                            // y = 1.1643x - 12.497
                            rectifyAngle = StringUtils.getRounding(1.1643 * rectifyAverage - 12.497, 1);
                            rebounderBean.calAngle = StringUtils.getRounding(rectifyAngle - rectifyAverage, 1);
                        }
                    }
                    //混凝土强度碳化计算
                    double coefficient1, coefficient2;
                    coefficient1 = 0.0492;
                    coefficient2 = 1.8709;
                    rebounderBean.carbonize = coefficient1 * Math.pow(rectifyAngle, coefficient2);
                }
                break;
                default:
                    break;
            }
        } else {
            switch (rebounderBean.standardId) {
                case 1:  //国家统一新版（非泵送）10MPa～60 MPa
                {
                    //非水平方向平均回弹值角度修正（20-50°）
                    double value = rectifyAverage;
                    value = value < 20 ? 20 : value;
                    value = value > 50 ? 50 : value;
                    value = getAngle(value, Integer.parseInt(StaticConstant.getDetectionAngleCompute(rebounderBean.standardId).get(rebounderBean.angle - 1)));
                    rebounderBean.calAngle = value;
                    double rectifyAngle = rectifyAverage + value;  //Rm = Rmα+Raα;
                    //浇筑面(表面、底面)平均回弹值修正
                    value = getSurface(rectifyAngle, rebounderBean.surface, rebounderBean.standardId, rebounderBean.isPumping);
                    rebounderBean.calSurface = value;
                    double rectifySurface = rectifyAngle + value;
                    //混凝土强度碳化计算
                    double dm = rebounderBean.carbonization >= 6 ? 6 : rebounderBean.carbonization;
                    rebounderBean.carbonize = getCarbonize(rectifySurface, dm, rebounderBean.standardId);
                }
                break;
                case 2:  //国家高强度4.5J（不确定）20MPa～110 MPa
                    rebounderBean.carbonize = getHighRm1(rectifyAverage);
                    break;
                case 3:  //国家高强度5.5J（不确定）60.2MPa～79.9 MPa
                    rebounderBean.carbonize = getHighRm2(2.51246, rectifyAverage, 0.889);
                    break;
                case 4:  //北京曲线（非泵送）15MPa～60 MPa
                    //暂无
                    break;
                case 5:  //河北曲线（非泵送）10MPa～60 MPa
                {
                    //非水平方向平均回弹值角度修正（20-50°）
                    double value = rectifyAverage;
                    value = value < 20 ? 20 : value;
                    value = value > 50 ? 50 : value;
                    value = getAngle(value, Integer.parseInt(StaticConstant.getDetectionAngleCompute(rebounderBean.standardId).get(rebounderBean.angle - 1)));
                    rebounderBean.calAngle = value;
                    double rectifyAngle = rectifyAverage + value;  //Rm = Rmα+Raα;
                    //浇筑面(表面、底面)平均回弹值修正
                    value = getSurface(rectifyAngle, rebounderBean.surface, rebounderBean.standardId, rebounderBean.isPumping);
                    rebounderBean.calSurface = value;
                    double rectifySurface = rectifyAngle + value;
                    //混凝土强度碳化计算
                    double dm = rebounderBean.carbonization >= 6 ? 6 : rebounderBean.carbonization;
                    rebounderBean.carbonize = getCarbonize(rectifySurface, dm, rebounderBean.standardId);
                }
                break;
                case 6:  //河南商品混凝土曲线（非泵送）15MPa～80 MPa
                {
                    //非水平方向平均回弹值角度修正（20-50°）
                    double value = rectifyAverage;
                    value = value < 20 ? 20 : value;
                    value = value > 50 ? 50 : value;
                    value = getAngle(value, Integer.parseInt(StaticConstant.getDetectionAngleCompute(rebounderBean.standardId).get(rebounderBean.angle - 1)));
                    rebounderBean.calAngle = value;
                    double rectifyAngle = rectifyAverage + value;  //Rm = Rmα+Raα;
                    //浇筑面(表面、底面)平均回弹值修正
                    value = getSurface(rectifyAngle, rebounderBean.surface, rebounderBean.standardId, rebounderBean.isPumping);
                    rebounderBean.calSurface = value;
                    double rectifySurface = rectifyAngle + value;
                    //混凝土强度碳化计算
                    double dm = rebounderBean.carbonization >= 6 ? 6 : rebounderBean.carbonization;
                    rebounderBean.carbonize = getCarbonize(rectifySurface, dm, rebounderBean.standardId);
                }
                break;
                case 7:  //山东曲线（非泵送）10MPa～60 MPa M225型修正
                {
                    //非水平方向平均回弹值角度修正（20-56°）
                    double value = rectifyAverage;
                    value = value < 20 ? 20 : value;
                    value = value > 56 ? 56 : value;
                    value = getAngle(value, Integer.parseInt(StaticConstant.getDetectionAngleCompute(rebounderBean.standardId).get(rebounderBean.angle - 1)));
                    rebounderBean.calAngle = value;
                    double rectifyAngle = rectifyAverage + value;  //Rm = Rmα+Raα;
                    //浇筑面(表面、底面)平均回弹值修正
                    value = getSurface(rectifyAngle, rebounderBean.surface, rebounderBean.standardId, rebounderBean.isPumping);
                    rebounderBean.calSurface = value;
                    double rectifySurface = rectifyAngle + value;
                    //混凝土强度碳化计算
                    double coefficient1, coefficient2, coefficient3;
                    coefficient1 = 0.02216;
                    coefficient2 = 2.0492;
                    coefficient3 = -0.0204;
                    double dm = rebounderBean.carbonization >= 6 ? 6 : rebounderBean.carbonization;
                    double carbonize = getPumpingCarbonize(rectifySurface, dm, coefficient1, coefficient2, coefficient3);
                    //混凝土修正
                    if (carbonize >= 10 && carbonize < 60) {
                        if (carbonize <= 15) {
                            carbonize = carbonize * (1.5357 - 0.0357 * carbonize);
                        } else if (carbonize >= 50) {
                            carbonize = carbonize * (1.3571 - 0.0071 * carbonize);
                        }
                    }
                    rebounderBean.carbonize = carbonize;
                }
                break;
                case 8:  //山东高强度曲线（非泵送）60MPa～80 MPa
                {
                    double value = rectifyAverage;
                    value = value < 20 ? 20 : value;
                    value = value > 56 ? 56 : value;
                    value = getAngle(value, Integer.parseInt(StaticConstant.getDetectionAngleCompute(rebounderBean.standardId).get(rebounderBean.angle - 1)));
                    rebounderBean.calAngle = value;
                    double rectifyAngle = rectifyAverage + value;  //Rm = Rmα+Raα;
                    //浇筑面(表面、底面)平均回弹值修正
                    value = getSurface(rectifyAngle, rebounderBean.surface, rebounderBean.standardId, rebounderBean.isPumping);
                    rebounderBean.calSurface = value;
                    double rectifySurface = rectifyAngle + value;
                    //混凝土强度碳化计算
                    rebounderBean.carbonize = getHighRm2(3.7661, rectifySurface, 0.7717);
                }
                break;
                case 9:  //青岛曲线（非泵送）10MPa～60 MPa M225型修正
                {
                    //非水平方向平均回弹值角度修正（20-56°）
                    double value = rectifyAverage;
                    value = value < 20 ? 20 : value;
                    value = value > 56 ? 56 : value;
                    value = getAngle(value, Integer.parseInt(StaticConstant.getDetectionAngleCompute(rebounderBean.standardId).get(rebounderBean.angle - 1)));
                    rebounderBean.calAngle = value;
                    double rectifyAngle = rectifyAverage + value;  //Rm = Rmα+Raα;
                    //浇筑面(表面、底面)平均回弹值修正
                    value = getSurface(rectifyAngle, rebounderBean.surface, rebounderBean.standardId, rebounderBean.isPumping);
                    rebounderBean.calSurface = value;
                    double rectifySurface = rectifyAngle + value;
                    //混凝土强度碳化计算
                    double coefficient1, coefficient2, coefficient3;
                    coefficient1 = 0.0176;
                    coefficient2 = 2.141;
                    coefficient3 = -0.0289;
                    double dm = rebounderBean.carbonization >= 6 ? 6 : rebounderBean.carbonization;
                    double carbonize = getPumpingCarbonize(rectifySurface, dm, coefficient1, coefficient2, coefficient3);
                    //混凝土修正
                    if (carbonize >= 10 && carbonize < 60) {
                        if (carbonize <= 15) {
                            carbonize = carbonize * (1.5357 - 0.0357 * carbonize);
                        } else if (carbonize >= 50) {
                            carbonize = carbonize * (1.3571 - 0.0071 * carbonize);
                        }
                    }
                    rebounderBean.carbonize = carbonize;
                }
                break;
                case 10:  //青岛高强度曲线（非泵送）60MPa～80 MPa
                {
                    double value = rectifyAverage;
                    value = value < 20 ? 20 : value;
                    value = value > 56 ? 56 : value;
                    value = getAngle(value, Integer.parseInt(StaticConstant.getDetectionAngleCompute(rebounderBean.standardId).get(rebounderBean.angle - 1)));
                    rebounderBean.calAngle = value;
                    double rectifyAngle = rectifyAverage + value;  //Rm = Rmα+Raα;
                    //浇筑面(表面、底面)平均回弹值修正
                    value = getSurface(rectifyAngle, rebounderBean.surface, rebounderBean.standardId, rebounderBean.isPumping);
                    rebounderBean.calSurface = value;
                    double rectifySurface = rectifyAngle + value;
                    //混凝土强度碳化计算
                    rebounderBean.carbonize = getHighRm2(2.40, rectifySurface, 0.90);
                }
                break;
                case 11:  //辽宁曲线（非泵送）10MPa～60 MPa
                    //暂无
                    break;
                case 12:  //安徽低强度（非泵送）10MPa～60 MPa
                    //暂无
                    break;
                case 13:  //安徽高强度（非泵送）60MPa～90 MPa
                    //暂无
                    break;
                case 14:  //江苏泵送（非泵送）10MPa～60 MPa
                    //暂无
                    break;
                case 15:  //浙江卵石（非泵送）10MPa～60 MPa
                    //暂无
                    break;
                case 16:  //浙江碎石（非泵送）15MPa～80 MPa
                    //暂无
                    break;
                case 17:  //上海结构混凝土曲线（非泵送）10MPa～70 MPa
                {
                    //非水平方向平均回弹值角度修正（20-50°）
                    double value = rectifyAverage;
                    value = value < 20 ? 20 : value;
                    value = value > 50 ? 50 : value;
                    value = getAngle(value, Integer.parseInt(StaticConstant.getDetectionAngleCompute(rebounderBean.standardId).get(rebounderBean.angle - 1)));
                    rebounderBean.calAngle = value;
                    double rectifyAngle = rectifyAverage + value;  //Rm = Rmα+Raα;
                    //浇筑面(表面、底面)平均回弹值修正
                    value = getSurface(rectifyAngle, rebounderBean.surface, rebounderBean.standardId, rebounderBean.isPumping);
                    rebounderBean.calSurface = value;
                    double rectifySurface = rectifyAngle + value;
                    //混凝土强度碳化计算
                    double coefficient1, coefficient2, coefficient3;
                    coefficient1 = 0.00548;
                    coefficient2 = 2.449;
                    coefficient3 = -0.00836;
                    double dm = rebounderBean.carbonization >= 6 ? 6 : rebounderBean.carbonization;
                    rebounderBean.carbonize = getPumpingCarbonize(rectifySurface, dm, coefficient1, coefficient2, coefficient3);
                }
                break;
                case 18:  //福建卵石（非泵送）10MPa～60 MPa
                {
                    //非水平方向平均回弹值角度修正（20-55°）
                    double value = rectifyAverage;
                    if (value >= 54 && value <= 55) {
                        value = 55;
                    }
                    value = value < 20 ? 20 : value;
                    value = value > 55 ? 55 : value;
                    value = getAngle(value, Integer.parseInt(StaticConstant.getDetectionAngleCompute(rebounderBean.standardId).get(rebounderBean.angle - 1)));
                    rebounderBean.calAngle = value;
                    double rectifyAngle = rectifyAverage + value;  //Rm = Rmα+Raα;
                    //浇筑面(表面、底面)平均回弹值修正
                    value = getSurface(rectifyAngle, rebounderBean.surface, rebounderBean.standardId, rebounderBean.isPumping);
                    rebounderBean.calSurface = value;
                    double rectifySurface = rectifyAngle + value;
                    //混凝土强度碳化计算
                    double dm = rebounderBean.carbonization >= 6 ? 6 : rebounderBean.carbonization;
                    rebounderBean.carbonize = getCarbonizeFuJian(rectifySurface, dm, 18);
                }
                break;
                case 19:  //福建碎石（非泵送）10MPa～60 MPa
                {
                    //非水平方向平均回弹值角度修正（20-55°）
                    double value = rectifyAverage;
                    if (value >= 54 && value <= 55) {
                        value = 55;
                    }
                    value = value < 20 ? 20 : value;
                    value = value > 55 ? 55 : value;
                    value = getAngle(value, Integer.parseInt(StaticConstant.getDetectionAngleCompute(rebounderBean.standardId).get(rebounderBean.angle - 1)));
                    rebounderBean.calAngle = value;
                    double rectifyAngle = rectifyAverage + value;  //Rm = Rmα+Raα;
                    //浇筑面(表面、底面)平均回弹值修正
                    value = getSurface(rectifyAngle, rebounderBean.surface, rebounderBean.standardId, rebounderBean.isPumping);
                    rebounderBean.calSurface = value;
                    double rectifySurface = rectifyAngle + value;
                    //混凝土强度碳化计算
                    double dm = rebounderBean.carbonization >= 6 ? 6 : rebounderBean.carbonization;
                    rebounderBean.carbonize = getCarbonizeFuJian(rectifySurface, dm, 19);
                }
                break;
                case 20:  //福建泵送（非泵送）10MPa～60 MPa
                    //暂无
                    break;
                case 21:  //陕西泵送曲线（非泵送）
                    //暂无
                    break;
                case 22:  //宁夏泵送（非泵送）
                    //暂无
                    break;
                case 23:  //甘肃庆阳预拌混凝土曲线（非泵送）
                {
                    //非水平方向平均回弹值角度修正（20-50°）
                    double value = rectifyAverage;
                    value = value < 20 ? 20 : value;
                    value = value > 50 ? 50 : value;
                    value = getAngle(value, Integer.parseInt(StaticConstant.getDetectionAngleCompute(rebounderBean.standardId).get(rebounderBean.angle - 1)));
                    rebounderBean.calAngle = value;
                    double rectifyAngle = rectifyAverage + value;  //Rm = Rmα+Raα;
                    //浇筑面(表面、底面)平均回弹值修正
                    value = getSurface(rectifyAngle, rebounderBean.surface, rebounderBean.standardId, rebounderBean.isPumping);
                    rebounderBean.calSurface = value;
                    double rectifySurface = rectifyAngle + value;
                    //混凝土强度碳化计算
                    double coefficient1, coefficient2, coefficient3;
                    coefficient1 = 0.117286;
                    coefficient2 = 1.62562;
                    coefficient3 = -0.0137;
                    double dm = rebounderBean.carbonization >= 6 ? 6 : rebounderBean.carbonization;
                    rebounderBean.carbonize = getPumpingCarbonize(rectifySurface, dm, coefficient1, coefficient2, coefficient3);
                }
                break;
                case 24:  //四川高强度（非泵送）
                {
                    //混凝土强度碳化计算
                    double coefficient1, coefficient2, coefficient3;
                    coefficient1 = 0.2440;
                    coefficient2 = 1.5652;
                    coefficient3 = -0.02354;
                    double dm = rebounderBean.carbonization >= 2 ? 2 : rebounderBean.carbonization;
                    rebounderBean.carbonize = getPumpingCarbonize(rectifyAverage, dm, coefficient1, coefficient2, coefficient3);
                }
                break;
                case 25:  //贵州曲线（非泵送）10MPa～50 MPa   <=1.0dm, 1.5dm,2.0dm
                {
                    //非水平方向平均回弹值角度修正（20-50°）
                    double value = rectifyAverage;
                    value = value < 20 ? 20 : value;
                    value = value > 50 ? 50 : value;
                    value = getAngle(value, Integer.parseInt(StaticConstant.getDetectionAngleCompute(rebounderBean.standardId).get(rebounderBean.angle - 1)));
                    rebounderBean.calAngle = value;
                    double rectifyAngle = rectifyAverage + value;  //Rm = Rmα+Raα;
                    //浇筑面(表面、底面)平均回弹值修正
                    value = getSurface(rectifyAngle, rebounderBean.surface, rebounderBean.standardId, rebounderBean.isPumping);
                    rebounderBean.calSurface = value;
                    double rectifySurface = rectifyAngle + value;
                    //混凝土强度碳化计算
                    double dm = rebounderBean.carbonization >= 10 ? 10 : rebounderBean.carbonization;
                    rebounderBean.carbonize = getCarbonize(rectifySurface, dm, rebounderBean.standardId);
                }
                break;
                case 26:  //水运工程2.207J（非泵送）10MPa～60 MPa
                {
                    //非水平方向平均回弹值角度修正（20-50°）
                    double value = rectifyAverage;
                    value = value < 20 ? 20 : value;
                    value = value > 50 ? 50 : value;
                    value = getAngle(value, Integer.parseInt(StaticConstant.getDetectionAngleCompute(rebounderBean.standardId).get(rebounderBean.angle - 1)));
                    rebounderBean.calAngle = value;
                    double rectifyAngle = rectifyAverage + value;  //Rm = Rmα+Raα;
                    //浇筑面(表面、底面)平均回弹值修正
                    value = getSurface(rectifyAngle, rebounderBean.surface, rebounderBean.standardId, rebounderBean.isPumping);
                    rebounderBean.calSurface = value;
                    double rectifySurface = rectifyAngle + value;
                    //混凝土强度碳化计算
                    double dm = rebounderBean.carbonization >= 6 ? 6 : rebounderBean.carbonization;
                    rebounderBean.carbonize = getCarbonizeWaterTransport(rectifySurface, dm);
                }
                break;
                case 27:  //铁路工程曲线（非泵送）10MPa～60 MPa
                {
                    //非水平方向平均回弹值角度修正（20-50°）
                    double value = rectifyAverage;
                    value = value < 20 ? 20 : value;
                    value = value > 50 ? 50 : value;
                    value = getAngle(value, Integer.parseInt(StaticConstant.getDetectionAngleCompute(rebounderBean.standardId).get(rebounderBean.angle - 1)));
                    rebounderBean.calAngle = value;
                    double rectifyAngle = rectifyAverage + value;  //Rm = Rmα+Raα;
                    //浇筑面(表面、底面)平均回弹值修正
                    value = getSurface(rectifyAngle, rebounderBean.surface, rebounderBean.standardId, rebounderBean.isPumping);
                    rebounderBean.calSurface = value;
                    double rectifySurface = rectifyAngle + value;
                    //混凝土强度碳化计算
                    double dm = rebounderBean.carbonization >= 6 ? 6 : rebounderBean.carbonization;
                    rebounderBean.carbonize = getCarbonize(rectifySurface, dm, 27);
                }
                break;
                case 28:  //广西泵送2023（非泵送）
                    //暂无
                    break;
                default:
                    break;
            }
        }
        return rebounderBean;
    }

    /**
     * 混凝土测强曲线
     *
     * @param Rm           平均回弹值Rm
     * @param dm           平均碳化深度值dm
     * @param coefficient1 系数1
     * @param coefficient2 系数2
     * @param coefficient3 系数3
     * @return 换算值
     */
    private static double getPumpingCarbonize(double Rm, double dm, double coefficient1, double coefficient2, double coefficient3) {
        return coefficient1 * Math.pow(Rm, coefficient2) * Math.pow(10, coefficient3 * dm);
    }

    /**
     * 测区混凝土强度换算值
     *
     * @param Rm：平均回弹值Rm
     * @param dm：平均碳化深度值dm
     * @return 换算值
     */
    private static double getCarbonizeFuJian(double Rm, double dm, int standardId) {
        switch (standardId) {
            case 18:  //福建卵石
            {
                BigDecimal d1 = new BigDecimal(Math.pow(Rm, 2.3244));
                BigDecimal d4 = new BigDecimal(-0.2252).multiply(new BigDecimal(1).subtract(new BigDecimal(Math.pow(1.4841, -dm))));
                BigDecimal d2 = new BigDecimal(Math.pow(10, d4.doubleValue()));
                BigDecimal d3 = d1.multiply(d2).multiply(new BigDecimal(0.007153));
                return d3.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
            }
            case 19:  //福建碎石
            {
                BigDecimal d1 = new BigDecimal(Math.pow(Rm, 2.1890));
                BigDecimal d4 = new BigDecimal(-0.2433).multiply(new BigDecimal(1).subtract(new BigDecimal(Math.pow(1.3241, -dm))));
                BigDecimal d2 = new BigDecimal(Math.pow(10, d4.doubleValue()));
                BigDecimal d3 = d1.multiply(d2).multiply(new BigDecimal(0.013187));
                return d3.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
            }
            default:
                return 0;
        }
    }

    private static double getCarbonizeWaterTransport(double Rm, double dm) {
        double d1 = StringUtils.getRounding(0.02497 * Math.pow(Rm, 2.016), 1);
        double d2 = 1;
        if (d1 >= 40) {
            d2 = waterTransportCarbonize(StaticConstant.getWaterTransport()[3], dm);
        } else if (d1 >= 30) {
            d2 = waterTransportCarbonize(StaticConstant.getWaterTransport()[2], dm);
        } else if (d1 >= 20) {
            d2 = waterTransportCarbonize(StaticConstant.getWaterTransport()[1], dm);
        } else if (d1 >= 10) {
            d2 = waterTransportCarbonize(StaticConstant.getWaterTransport()[0], dm);
        }
        return StringUtils.getRounding(d1 * d2, 1);
    }

    private static double waterTransportCarbonize(double[] waterTransport, double dm) {
        if (dm >= 6) {
            return waterTransport[5];
        } else if (dm >= 5) {
            return RebounderUtils.getInterpolationValue1(5, waterTransport[5 - 1], 5 + 1, waterTransport[5], dm);
        } else if (dm >= 4) {
            return RebounderUtils.getInterpolationValue1(4, waterTransport[4 - 1], 4 + 1, waterTransport[4], dm);
        } else if (dm >= 3) {
            return RebounderUtils.getInterpolationValue1(3, waterTransport[3 - 1], 3 + 1, waterTransport[3], dm);
        } else if (dm >= 2) {
            return RebounderUtils.getInterpolationValue1(2, waterTransport[2 - 1], 2 + 1, waterTransport[2], dm);
        } else if (dm >= 1) {
            return RebounderUtils.getInterpolationValue1(1, waterTransport[0], 1 + 1, waterTransport[1], dm);
        } else {
            return 1;
        }
    }

    /**
     * 铁路泵送修正
     *
     * @param conversionValue 碳化换算值
     * @param dm              碳化值
     * @return 修正值
     */
    private static double reviseRailway(double conversionValue, double dm) {
        if (dm > 2) {
            return conversionValue;
        } else if (dm >= 1.5) {
            return StringUtils.getRounding(reviseRailway1(StaticConstant.getRailway()[1], conversionValue), 1);
        } else {
            return StringUtils.getRounding(reviseRailway1(StaticConstant.getRailway()[0], conversionValue), 1);
        }
    }

    private static double reviseRailway1(double[] railway, double conversionValue) {
        if (conversionValue >= 55) {
            return conversionValue;
        } else if (conversionValue >= 50) {
            return conversionValue + RebounderUtils.getInterpolationValue(50, railway[5 - 1], 55, railway[5], conversionValue);
        } else if (conversionValue >= 45) {
            return conversionValue + RebounderUtils.getInterpolationValue(45, railway[4 - 1], 50, railway[4], conversionValue);
        } else if (conversionValue >= 40) {
            return conversionValue + RebounderUtils.getInterpolationValue(40, railway[3 - 1], 45, railway[3], conversionValue);
        } else if (conversionValue >= 35) {
            return conversionValue + RebounderUtils.getInterpolationValue(35, railway[2 - 1], 40, railway[2], conversionValue);
        } else if (conversionValue >= 30) {
            return conversionValue + RebounderUtils.getInterpolationValue(30, railway[0], 35, railway[1], conversionValue);
        } else {
            return conversionValue + railway[0];
        }
    }

    private static double getHighRm1(double Rm) {
        BigDecimal d1 = new BigDecimal(Math.pow(Rm, 2)).multiply(new BigDecimal(0.0079));
        BigDecimal d2 = new BigDecimal(0.75).multiply(new BigDecimal(Rm));
        BigDecimal d3 = new BigDecimal(-7.83).add(d1).add(d2);
        return d3.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    private static double getHighRm2(double coefficient1, double Rm, double coefficient2) {
        BigDecimal d1 = new BigDecimal(Math.pow(Rm, coefficient2)).multiply(new BigDecimal(coefficient1));
        return d1.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    private static double getHighRm3(double Rm, double dm, double coefficient1, double coefficient2, double coefficient3) {
        double e = 2.718281828459045;
        BigDecimal d1 = new BigDecimal(coefficient2).multiply(new BigDecimal(Rm));
        BigDecimal d2 = new BigDecimal(coefficient3).multiply(new BigDecimal(dm));
        BigDecimal d3 = d1.subtract(d2);
        BigDecimal d4 = new BigDecimal(Math.pow(e, d3.doubleValue())).multiply(new BigDecimal(coefficient1));
        return d4.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    private static double getCarbonize(double Rm, double dm, int standardId) {
        double[][] arrRm = getCarbonizeArray(standardId);
        if (Rm < arrRm[0][0]) {
            //碳化换算值小于10MPa
            return 4.5;
        } else if (Rm > arrRm[arrRm.length - 1][0]) {
            //标准：碳化换算值大于60MPa
            //河南商品混凝土曲线-6：碳化换算值大于60MPa
            return standardId == 6 ? 99.9 : 66.6;
        } else {
            //贵州非泵送-25
            double[] DM = StaticConstant.getDSCarbonization(standardId);
            if (standardId == 25) {
                dm = dm < 1 ? 1 : dm;
            }
            for (int i = 0; i < arrRm.length; i++) {
                if (Rm <= arrRm[i][0]) {
                    if (Rm == arrRm[i][0]) {
                        double[] arrRmm = arrRm[i];
                        return StringUtils.getRounding(getRm(arrRmm, DM, dm), 1);
                    } else {
                        double[] arrRmm1 = arrRm[i - 1];
                        double[] arrRmm2 = arrRm[i];
                        double rm1 = getRm(arrRmm1, DM, dm);
                        double rm2 = getRm(arrRmm2, DM, dm);
                        return StringUtils.getRounding(RebounderUtils.getInterpolationValue(arrRmm1[0], rm1, arrRmm2[0], rm2, Rm), 1);
                    }
                }
            }
        }
        return 0;
    }

    private static double getRm(double[] arrRmm, double[] DM, double dm) {
        double rm = 0;
        for (int i = 0; i < DM.length; i++) {
            if (dm <= DM[i]) {
                if (dm == DM[i]) {
                    rm = arrRmm[i + 1];
                    break;
                } else {
                    rm = RebounderUtils.getInterpolationValue(DM[i - 1], arrRmm[i], DM[i], arrRmm[i + 1], dm);
                    break;
                }
            }
        }
        return rm;
    }

    private static double[][] getCarbonizeArray(int standardId) {
        double[][] carbonizeArray = null;
        InputStreamReader is;
        try {
            switch (standardId) {
                case 1:  //国家统一新版 10MPa～60 MPa
                case 5:  //河北曲线 10MPa～60 MPa
                    is = new InputStreamReader(mContext.getAssets().open("carbonize_national_standard.txt"));
                    carbonizeArray = getCarbonizeArray(is, 201, 14);
                    break;
                case 2:  //国家高强度4.5J 20MPa～110 MPa
                    //暂无
                    break;
                case 3:  //国家高强度5.5J 60.2MPa～79.9 MPa
                    //暂无
                    break;
                case 4:  //北京曲线 15MPa～60 MPa
                    //暂无
                    break;
                case 6:  //河南商品混凝土曲线
                    is = new InputStreamReader(mContext.getAssets().open("carbonize_hensphnt.txt"));
                    carbonizeArray = getCarbonizeArray(is, 259, 14);
                    break;
                case 7:  //山东曲线
                    break;
                case 8:  //山东高强度曲线
                    break;
                case 9:  //青岛曲线
                    break;
                case 10:  //青岛高强度曲线
                    break;
                case 11:  //辽宁曲线
                    break;
                case 12:  //安徽低强度
                    break;
                case 13:  //安徽高强度
                    break;
                case 14:  //江苏泵送
                    break;
                case 15:  //浙江卵石
                    is = new InputStreamReader(mContext.getAssets().open("carbonize_zjls.txt"));
                    carbonizeArray = getCarbonizeArray(is, 201, 14);
                    break;
                case 16:  //浙江碎石
                    break;
                case 17:  //上海结构混凝土曲线
                    break;
                case 18:  //福建卵石
                    break;
                case 19:  //福建碎石
                    break;
                case 20:  //福建泵送
                    break;
                case 21:  //陕西泵送曲线
                    break;
                case 22:  //宁夏泵送
                    break;
                case 23:  //甘肃庆阳预拌混凝土曲线
                    break;
                case 24:  //四川高强度
                    break;
                case 25:  //贵州曲线
                    is = new InputStreamReader(mContext.getAssets().open("carbonize_guizhou.TXT"));
                    carbonizeArray = getCarbonizeArray(is, 150, 16);
                    break;
                case 26:  //水运工程2.207J
                    break;
                case 27:  //铁路工程曲线
                    is = new InputStreamReader(mContext.getAssets().open("carbonize_railway.txt"));
                    carbonizeArray = getCarbonizeArray(is, 201, 14);
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return carbonizeArray;
    }

    private static double[][] getCarbonizeArray(InputStreamReader is, int first, int second) {
        BufferedReader br = new BufferedReader(is);// 读取文件
        double[][] carbonizeArray = new double[first][second];
        try {
            String line;
            String[] sp;
            int count = 0;
            while ((line = br.readLine()) != null) {
                sp = line.split(",");
                for (int i = 0; i < sp.length; i++) {
                    carbonizeArray[count][i] = Double.parseDouble(sp[i].trim());
                }
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return carbonizeArray;
    }

    /**
     * 计算非水平方向角度修正值
     *
     * @param value 平均回弹值
     * @param angle 角度
     * @return 平均回弹修正值
     */
    private static double getAngle(double value, int angle) {
        int number = (int) value;
        double y;
        if (angle == 0) {
            //角度为0，不用修正
            y = 0;
        } else if (number == value) {
            //查表
            y = getAngleValue(number, angle);
        } else {
            //浮点数按内插法求得，精确至0.1
            double x1 = Math.floor(value);
            double x2 = Math.ceil(value);
            double y1 = getAngleValue((int) x1, angle);
            double y2 = getAngleValue((int) x2, angle);
            y = RebounderUtils.getInterpolationValue(x1, y1, x2, y2, value);
        }
        return StringUtils.getRounding(y, 1);
    }

    private static double getAngleValue(int Rmα, int angle) {
        Rmα = Rmα - 20;
        switch (angle) {
            case 90:
                return RectifyAngle.angle90[Rmα];
            case 75:
                return RectifyAngle.angle75[Rmα];
            case 60:
                return RectifyAngle.angle60[Rmα];
            case 45:
                return RectifyAngle.angle45[Rmα];
            case 30:
                return RectifyAngle.angle30[Rmα];
            case 15:
                return RectifyAngle.angle15[Rmα];
            case -15:
                return RectifyAngle.angle_15[Rmα];
            case -30:
                return RectifyAngle.angle_30[Rmα];
            case -45:
                return RectifyAngle.angle_45[Rmα];
            case -60:
                return RectifyAngle.angle_60[Rmα];
            case -75:
                return RectifyAngle.angle_75[Rmα];
            case -90:
                return RectifyAngle.angle_90[Rmα];
            default:
                return 0;
        }
    }

    /**
     * 计算不同浇筑面修正值
     *
     * @param rectify    平均回弹值
     * @param surface    1：表面、2：底面、3：侧面
     * @param standardId 曲线ID
     * @return 平均回弹修正值
     */
    private static double getSurface(double rectify, int surface, int standardId, int isPumping) {
        double value = rectify;
        boolean isTop = surface == 1;
        int number = (int) value;
        double y;
        if (surface == 3) {
            //侧面不需要修正
            y = 0;
        } else if (number == value || value > 50 || value < 20) {
            //回弹值小于20或大于50时，分别按20或50查表
            value = value < 20 ? 20 : value;
            value = value > 50 ? 50 : value;
            y = getSurfaceValue((int) value, isTop, standardId, isPumping);
        } else {
            //浮点数按内插法求得，精确至0.1
            double x1 = Math.floor(value);
            double x2 = Math.ceil(value);
            double y1 = getSurfaceValue((int) x1, isTop, standardId, isPumping);
            double y2 = getSurfaceValue((int) x2, isTop, standardId, isPumping);
            y = RebounderUtils.getInterpolationValue(x1, y1, x2, y2, value);
        }
        //Rm = Rtm+Rta;
        //Rm = Rbm+Rba;
        return y;
    }

    private static double getSurfaceValue(int Rtbm, boolean isTop, int standardId, int isPumping) {
        Rtbm = Rtbm - 20;
        if (isTop) {
            switch (standardId) {
                case 7:
                case 8:
                case 9:
                case 10:
                    if (isPumping == 1) {//是否泵送 0：否；1：是；
                        return RectifySurface.shandong_top[Rtbm];
                    } else {
                        return RectifySurface.standard_top[Rtbm];
                    }
                default:
                    return RectifySurface.standard_top[Rtbm];
            }
        } else {
            switch (standardId) {
                case 7:
                case 8:
                case 9:
                case 10:
                    if (isPumping == 1) {//是否泵送 0：否；1：是；
                        return RectifySurface.shandong_bottom[Rtbm];
                    } else {
                        return RectifySurface.standard_bottom[Rtbm];
                    }
                default:
                    return RectifySurface.standard_bottom[Rtbm];
            }
        }
    }

    public static int getAngle(int id) {
        return id + 1;
    }

    public static int getSurface(int id) {
        int surfaceId;
        switch (id) {
            case 1:
                surfaceId = 1;
                break;
            case 2:
                surfaceId = 2;
                break;
            default:
                surfaceId = 3;
                break;
        }
        return surfaceId;
    }

    public static int getCurve(int id) {
        //0~10分别表示：国家、上海、江苏、山东、北京、辽宁、陕西、福建碎石、福建卵石、浙江碎石、浙江卵石
        int curveId;
        switch (id) {
            case 1:
                curveId = 17;
                break;
            case 2:
                curveId = 14;
                break;
            case 3:
                curveId = 7;
                break;
            case 4:
                curveId = 4;
                break;
            case 5:
                curveId = 11;
                break;
            case 6:
                curveId = 21;
                break;
            case 7:
                curveId = 19;
                break;
            case 8:
                curveId = 18;
                break;
            case 9:
                curveId = 16;
                break;
            case 10:
                curveId = 15;
                break;
            default:
                curveId = 1;
                break;
        }
        return curveId;
    }
}

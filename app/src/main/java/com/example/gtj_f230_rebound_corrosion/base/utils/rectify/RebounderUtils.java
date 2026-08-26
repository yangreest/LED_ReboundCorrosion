package com.example.gtj_f230_rebound_corrosion.base.utils.rectify;


import com.example.gtj_f230_rebound_corrosion.base.utils.StringUtils;
import com.example.gtj_f230_rebound_corrosion.rebound.model.RebounderBean;
import com.example.gtj_f230_rebound_corrosion.base.model.RebounderZoneBean;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class RebounderUtils {

    /**
     * 标准差、推定值等计算逻辑
     *
     * @param testingList       测区集合
     * @param rebounderZoneBean 构件对象
     */
    public static void calculate(List<RebounderBean> testingList, RebounderZoneBean rebounderZoneBean) {
        double[] carbonizeArray = new double[testingList.size()];
        for (int i = 0; i < testingList.size(); i++) {
            carbonizeArray[i] = testingList.get(i).carbonize;
        }
        switch (rebounderZoneBean.detectionStandard) {
            case 1://国家统一新版（泵送）10MPa～60 MPa
            case 5://河北曲线（泵送）10MPa～60 MPa
            case 7://山东曲线（泵送）10MPa～60 MPa M225型修正
            case 9://青岛曲线（泵送）10MPa～60 MPa M225型修正
            case 11://辽宁曲线（泵送）10MPa～60 MPa
            case 12://安徽低强度（泵送）10MPa～60 MPa
            case 14://江苏泵送（泵送）10MPa～60 MPa
            case 15://浙江卵石（泵送）10MPa～60 MPa
            case 18://福建卵石（泵送）10MPa～60 MPa
            case 19://福建碎石（泵送）10MPa～60 MPa
            case 20://福建泵送（泵送）10MPa～60 MPa
            case 21://陕西泵送曲线（泵送）10MPa～60 MPa
            case 22://宁夏泵送（泵送）//TODO 暂无 现使用国家标准
            case 23://甘肃庆阳预拌混凝土曲线（泵送）
            case 26://水运工程2.207J（泵送）10MPa～60 MPa
            case 27://铁路工程曲线（泵送）10MPa～60 MPa
            case 28://广西泵送2023（泵送）10MPa～60 MPa
                getNoResult(10, 60, carbonizeArray, rebounderZoneBean);
                break;
            case 2:////国家高强度4.5J（泵送）20MPa～110 MPa
                getNoResult(20, 110, carbonizeArray, rebounderZoneBean);
                break;
            case 3://国家高强度5.5J（泵送）60.2MPa～79.9 MPa
                getNoResult(60.2, 79.9, carbonizeArray, rebounderZoneBean);
                break;
            case 4://北京曲线（泵送）15MPa～60 MPa
                getNoResult(15, 60, carbonizeArray, rebounderZoneBean);
                break;
            case 6://河南商品混凝土曲线（泵送）15MPa～80 MPa
            case 16://浙江碎石（泵送）15MPa～80 MPa
                getNoResult(15, 80, carbonizeArray, rebounderZoneBean);
                break;
            case 8://山东高强度曲线（泵送）60MPa～80 MPa
            case 10://青岛高强度曲线（泵送）60MPa～80 MPa
                getNoResult(60, 80, carbonizeArray, rebounderZoneBean);
                break;
            case 13://安徽高强度（泵送）60MPa～90 MPa
                getNoResult(60, 90, carbonizeArray, rebounderZoneBean);
                break;
            case 17://上海结构混凝土曲线（泵送）10MPa～70 MPa
                getNoResult(10, 70, carbonizeArray, rebounderZoneBean);
                break;
            case 24://四川高强度 5.5J（泵送）50MPa～100 MPa （构件中 出现测区强度无法查出(如 c cu f ＜50.0MPa 或 c cu f ＞100.0MPa)时，因无法计算平均值及标准差，也只能 24 以最小值作为该强度推定值）
                getNoResult(50, 100, carbonizeArray, rebounderZoneBean);
                break;
            case 25://贵州曲线
                if (rebounderZoneBean.isPumping == 0) {  //非泵送 10MPa～50 MPa
                    getNoResult(10, 50, carbonizeArray, rebounderZoneBean);
                } else {  //泵送 10MPa～50 MPa
                    getNoResult(10, 60, carbonizeArray, rebounderZoneBean);
                }
                break;
        }
    }

    private static void getNoResult(double stdMin, double stdMax, double[] carbonizeArray, RebounderZoneBean rebounderZoneBean) {
        double min = getCorrectedMin(carbonizeArray);
        double max = getCorrectedMax(carbonizeArray);
        if (min < stdMin) {
            rebounderZoneBean.min = min;
            rebounderZoneBean.average = 0;
            rebounderZoneBean.presumption = 0;
            rebounderZoneBean.stdDev = 0;
            rebounderZoneBean.strMin = "<" + stdMin;
            rebounderZoneBean.strAverage = "--";
            rebounderZoneBean.strPresumption = "<" + stdMin;
            rebounderZoneBean.strStdDev = "--";
        } else if (max > stdMax) {
            rebounderZoneBean.min = min > stdMax ? max : min;
            rebounderZoneBean.average = 0;
            rebounderZoneBean.presumption = max;
            rebounderZoneBean.stdDev = 0;
            rebounderZoneBean.strMin = min > stdMax ? (">" + stdMax) : min + "";
            rebounderZoneBean.strAverage = "--";
            rebounderZoneBean.strPresumption = ">" + stdMax;
            rebounderZoneBean.strStdDev = "--";
        } else {
            getResult(carbonizeArray, rebounderZoneBean);
        }
    }

    private static void getResult(double[] carbonizeArray, RebounderZoneBean rebounderZoneBean) {
        double min = StringUtils.getRounding(getCorrectedMin(carbonizeArray), 1);
        //测区混凝土强度换算值的平均值
        double value = getCorrectedAverage(carbonizeArray);
        double average = StringUtils.getRounding(value, 2);
        //标准差、推定值
        double stdDev, presumption;
        if (carbonizeArray.length >= 10) {
            //结构或构件测区混凝土强度换算值的标准差
            if (rebounderZoneBean.detectionStandard == 26) {
                stdDev = getStDev(carbonizeArray, average);
            } else {
                stdDev = getStandardDeviation(carbonizeArray, average);
            }
            //混凝土强度推定值计算
            presumption = StringUtils.getRounding(getPresumption(carbonizeArray, average, stdDev), 1);
        } else {
            stdDev = 0;
            presumption = min;
        }
        String strMin = "--", strAverage = "--", strPresumption = "--", strStdDev = "--";
        switch (rebounderZoneBean.detectionStandard) {
            case 1:
            case 5:
            case 7:
            case 9:
            case 11:
            case 12:
            case 14:
            case 15:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 26:
            case 27:
            case 28:
                strMin = getStringNumber(10, 60, min);
                strAverage = getStringNumber(10, 60, average);
                strPresumption = getStringNumber(10, 60, presumption);
                strStdDev = getStringNumber(60, stdDev);
                break;
            case 2:
                strMin = getStringNumber(20, 110, min);
                strAverage = getStringNumber(20, 110, average);
                strPresumption = getStringNumber(20, 110, presumption);
                strStdDev = getStringNumber(110, stdDev);
                break;
            case 3:
                strMin = getStringNumber(60.2, 79.9, min);
                strAverage = getStringNumber(60.2, 79.9, average);
                strPresumption = getStringNumber(60.2, 79.9, presumption);
                strStdDev = getStringNumber(79.9, stdDev);
                break;
            case 4:
                strMin = getStringNumber(15, 60, min);
                strAverage = getStringNumber(15, 60, average);
                strPresumption = getStringNumber(15, 60, presumption);
                strStdDev = getStringNumber(60, stdDev);
                break;
            case 6:
            case 16:
                strMin = getStringNumber(15, 80, min);
                strAverage = getStringNumber(15, 80, average);
                strPresumption = getStringNumber(15, 80, presumption);
                strStdDev = getStringNumber(80, stdDev);
                break;
            case 8:
            case 10:
                strMin = getStringNumber(60, 80, min);
                strAverage = getStringNumber(60, 80, average);
                strPresumption = getStringNumber(60, 80, presumption);
                strStdDev = getStringNumber(80, stdDev);
                break;
            case 13:
                strMin = getStringNumber(60, 90, min);
                strAverage = getStringNumber(60, 90, average);
                strPresumption = getStringNumber(60, 90, presumption);
                strStdDev = getStringNumber(90, stdDev);
                break;
            case 17:
                strMin = getStringNumber(10, 70, min);
                strAverage = getStringNumber(10, 70, average);
                strPresumption = getStringNumber(10, 70, presumption);
                strStdDev = getStringNumber(70, stdDev);
                break;
            case 24:
                strMin = getStringNumber(50, 100, min);
                strAverage = getStringNumber(50, 100, average);
                strPresumption = getStringNumber(50, 100, presumption);
                strStdDev = getStringNumber(100, stdDev);
                break;
            case 25:
                if (rebounderZoneBean.isPumping == 0) {
                    strMin = getStringNumber(10, 50, min);
                    strAverage = getStringNumber(10, 50, average);
                    strPresumption = getStringNumber(10, 50, presumption);
                    strStdDev = getStringNumber(50, stdDev);
                } else {
                    strMin = getStringNumber(10, 60, min);
                    strAverage = getStringNumber(10, 60, average);
                    strPresumption = getStringNumber(10, 60, presumption);
                    strStdDev = getStringNumber(60, stdDev);
                }
                break;
        }
        rebounderZoneBean.min = min;
        rebounderZoneBean.average = average;
        rebounderZoneBean.presumption = presumption;
        rebounderZoneBean.stdDev = stdDev;
        rebounderZoneBean.strMin = strMin;
        rebounderZoneBean.strAverage = strAverage;
        rebounderZoneBean.strPresumption = strPresumption;
        rebounderZoneBean.strStdDev = strStdDev;
    }

    private static String getStringNumber(double stdMin, double stdMax, double number) {
        String strNumber = number + "";
        if (number < stdMin) {
            strNumber = "<" + stdMin;
        } else if (number > stdMax) {
            strNumber = ">" + stdMax;
        }
        return strNumber;
    }

    private static String getStringNumber(double stdMax, double number) {
        String strNumber = number + "";
        if (number > stdMax) {
            strNumber = ">" + stdMax;
        }
        return strNumber;
    }

    /**
     * 计算数组长度为16的数组平均值
     *
     * @param array:size=16
     * @return average
     */
    static double getUncorrectedAverage(int standardId, Integer[] array) {
        double sum = 0;
        switch (standardId) {
            case 28: {
                //10个回弹值中剔除1个最小值和1个最大值，剩余8个计算平均值
                Arrays.sort(array);
                for (int i = 0; i < array.length; i++) {
                    if (i >= 1 && i <= 8) {
                        sum = sum + array[i];
                    }
                }
                sum = sum / 8;
            }
            break;
            default: {
                //16个回弹值中剔除3个最小值和3个最大值，剩余10个计算平均值
                Arrays.sort(array);
                for (int i = 0; i < array.length; i++) {
                    if (i >= 3 && i <= 12) {
                        sum = sum + array[i];
                    }
                }
                sum = sum / 10;
            }
            break;
        }
        return StringUtils.getRounding(sum, 1);
    }

    /**
     * 求最小换算值
     *
     * @param array 换算值集合（单个构件：构件测区换算值集合；批量构件：所有被抽检构件测区换算值集合）
     * @return 最小换算值
     */
    private static double getCorrectedMin(double[] array) {
        Arrays.sort(array);
        return array[0];
    }

    /**
     * 求最小换算值
     *
     * @param array 换算值集合（单个构件：构件测区换算值集合；批量构件：所有被抽检构件测区换算值集合）
     * @return 最小换算值
     */
    private static double getCorrectedMax(double[] array) {
        Arrays.sort(array);
        return array[array.length - 1];
    }

    /**
     * 测区混凝土强度换算值的平均值
     *
     * @param array 换算值集合（单个构件：构件测区换算值集合；批量构件：所有被抽检构件测区换算值集合）
     * @return 换算值的平均值（需要精确至0.1MPa）
     */
    private static double getCorrectedAverage(double[] array) {
        double sum = 0;
        for (double v : array) {
            sum = sum + v;
        }
        BigDecimal bignum1 = new BigDecimal(sum);
        return bignum1.divide(new BigDecimal(array.length), 4, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 结构或构件测区混凝土强度换算值的标准差
     *
     * @param array 换算值集合（单个构件：构件测区集合；批量构件：所有被抽检构件测区集合）
     * @param Mfccu 换算值的平均值
     * @return 标准差（需要精确至0.01MPa）
     */
    private static double getStandardDeviation(double[] array, double Mfccu) {
        if (array.length == 1) {
            return 0;
        }
        double sum_fccui = 0;
        for (double v : array) {
            sum_fccui += v * v;
        }
        double n = array.length;
        double a = n * Mfccu * Mfccu;
        double result = (sum_fccui - a) / (n - 1);
        result = Math.sqrt(result);
        return StringUtils.getRounding(result, 2);
    }

    /**
     * 结构或构件测区混凝土强度换算值的标准差
     *
     * @param array 换算值集合（单个构件：构件测区集合；批量构件：所有被抽检构件测区集合）
     * @param Mfccu 换算值的平均值
     * @return 标准差（需要精确至0.01MPa）
     */
    private static double getStDev(double[] array, double Mfccu) {
        if (array.length == 1) {
            return 0;
        }
        double result = 0;
        for (double v : array) {
            //x^2+2xy+y^2
            result += v * v + Mfccu * Mfccu - 2 * v * Mfccu;
        }
        result = result / (array.length - 1);
        result = Math.sqrt(result);
        return StringUtils.getRounding(result, 2);
    }

    /**
     * 构件现龄期混凝土强度推定值
     *
     * @param array 换算值集合（单个构件：构件测区集合；批量构件：所有被抽检构件测区集合）
     * @param Mfccu 换算值的平均值
     * @param Sfccu 标准差
     * @return 推定值
     */
    private static double getPresumption(double[] array, double Mfccu, double Sfccu) {
        double min = array[0];
        for (double v : array) {
            if (min > v) {
                min = v;
            }
        }
        if (min < 10) {
            //当构件测区强度值中出现小于10Mpa时，...
            return min;
        } else if (array.length < 10) {
            //当构件测区数少于10个时，取集合最小值
            return min;
        } else {
            //当构件测区数不少于10个时或批量检测时
            return Mfccu - 1.645 * Sfccu;
        }
    }

    /**
     * 求内插数
     */
    static double getInterpolationValue(double x1, double y1, double x2, double y2, double x) {
        //K＝(y2-y1)/(x2-x1)
        double K = (y2 - y1) / (x2 - x1);
        //y-y0=k(x-x0)
        double dd = K * (x - x1) + y1;
        return StringUtils.getRounding(dd, 1);
    }

    /**
     * 求内插数
     */
    static double getInterpolationValue1(double x1, double y1, double x2, double y2, double x) {
        //K＝(y2-y1)/(x2-x1)
        double K = (y2 - y1) / (x2 - x1);
        //y-y0=k(x-x0)
        return K * (x - x1) + y1;
    }
}

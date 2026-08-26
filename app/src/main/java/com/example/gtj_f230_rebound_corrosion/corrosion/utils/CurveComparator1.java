package com.example.gtj_f230_rebound_corrosion.corrosion.utils;

import com.example.gtj_f230_rebound_corrosion.base.utils.StringUtils;

import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

public class CurveComparator1 {
    /**
     * 比较2条曲线的均方根误差（原始数据）
     *
     * @return ：均方根误差，RMSE
     * @param：standardCurve
     * @param：curve
     */
    public static double compareRMSE(int[] standardCurve, int[] curve) {
        if (standardCurve.length != curve.length) {
            return 10000;
        }
        double[] squaredErrors = new double[standardCurve.length];
        // 1. 计算误差数组
        for (int i = 0; i < standardCurve.length; i++) {
            double error = standardCurve[i] - curve[i];
            squaredErrors[i] = error * error; // 平方误差
        }
        // 2. 使用 StatUtils 计算各种指标
        // 均方误差 (MSE)
        double MSE = StringUtils.getRounding(StatUtils.mean(squaredErrors), 3);
        // 均方根误差 (RMSE)
        return StringUtils.getRounding(Math.sqrt(MSE), 3);
    }

    /**
     * 曲线相关性分析（原始数据）
     *
     * @param curve1：曲线1
     * @param curve2：曲线2
     * @return ：均方根误差|皮尔逊|斯皮尔曼|欧氏距离（DTW）
     */
    public static double[] computeCorrelationAnalysis(int[] curve1, int[] curve2) {
        //变化率：K＝(y2-y1)/(x2-x1)，K ＝ y2 / y1
        double[] K1 = new double[60];
        double[] K2 = new double[60];
        for (int i = 0; i < 60; i++) {
            //
            double t11 = curve1[i];
            double t12 = curve1[i + 1];
            K1[i] = Math.max(t11, t12) - Math.min(t11, t12);
            //
            double t21 = curve2[i];
            double t22 = curve2[i + 1];
            K2[i] = Math.max(t21, t22) - Math.min(t21, t22);
        }
        double[] result = new double[2];
        result[0] = compute2(K1, K2, 40);
        result[1] = compute2(K1, K2, 50);
        return result;
    }

    /**
     * 曲线相关性分析（原始数据）
     *
     * @param curve1：曲线1
     * @param curve2：曲线2
     * @param length：1   ~ 25
     * @return ：广角
     */
    public static double[] computeCorrelationAnalysis1(double[] curve1, double[] curve2, int length) {
        int middle = curve1.length / 2;
        double c1L = coordinatesToAngle(curve1[middle] - curve1[middle - length], length);
        double c1R = coordinatesToAngle(curve1[middle] - curve1[middle + length], length);
        //
        double c2L = coordinatesToAngle(curve2[middle] - curve2[middle - length], length);
        double c2R = coordinatesToAngle(curve2[middle] - curve2[middle + length], length);
        double[] result = new double[7];
        result[0] = StringUtils.getRounding(c1L, 5);
        result[1] = StringUtils.getRounding(c1R, 5);
        result[2] = StringUtils.getRounding(c1L + c1R, 5);
        //
        result[3] = StringUtils.getRounding(c2L, 5);
        result[4] = StringUtils.getRounding(c2R, 5);
        result[5] = StringUtils.getRounding(c2L + c2R, 5);
        result[6] = StringUtils.getRounding(Math.abs(result[2] - result[5]), 5);
        return result;
    }

    /**
     * 根据点的坐标求角度
     *
     * @param y y坐标
     * @param x x坐标
     * @return 角度值（度）
     */
    public static double coordinatesToAngle(double y, double x) {
        return Math.toDegrees(Math.atan2(y, x));
    }

    /**
     * 相关性分析
     *
     * @param curve1：数据1
     * @param curve2：数据2
     * @return ：均方根误差|皮尔逊|斯皮尔曼|欧氏距离（DTW）
     */
    private static double[] correlationAnalysis(double[] curve1, double[] curve2) {
        double[] result = new double[4];
        //1. 均方根误差
        double[] squaredErrors = new double[curve1.length];
        double[] squaredErrors1 = new double[curve1.length];
        //  计算误差数组
        for (int i = 0; i < curve1.length; i++) {
            double error = curve1[i] - curve2[i];
            squaredErrors[i] = error * error; // 平方误差
            squaredErrors1[i] = error;
        }
        //  均方误差 (MSE)
        double MSE = StringUtils.getRounding(StatUtils.mean(squaredErrors), 3);
        //  均方根误差 (RMSE)
        result[0] = StringUtils.getRounding(Math.sqrt(MSE), 3);
        //
        //2. 皮尔逊 | 斯皮尔曼
        PearsonsCorrelation pearson = new PearsonsCorrelation();
        SpearmansCorrelation spearman = new SpearmansCorrelation();
        double pearsonCorr = pearson.correlation(curve1, curve2);
        double spearmanCorr = spearman.correlation(curve1, curve2);
        result[1] = StringUtils.getRounding(pearsonCorr, 3);  //皮尔逊相关系数
        result[2] = StringUtils.getRounding(spearmanCorr, 3);  //斯皮尔曼相关系数
        //
        //3. 欧氏距离（DTW）
        EuclideanDistance euclidean = new EuclideanDistance();
        result[3] = StringUtils.getRounding(euclidean.compute(curve1, curve2), 3);
        return result;
    }

    public static double compute1(double[] curve1, double[] curve2, int size) {
        int index = (60 - size) / 2;
        double rate = 0;
        for (int i = 0; i < curve1.length; i++) {
            if (i >= index && i <= size + index - 1) {
                rate += Math.abs(curve1[i] - curve2[i]);
            }
        }
        return StringUtils.getRounding(rate, 3);
    }

    public static double compute2(double[] curve1, double[] curve2, int size) {
        int index = (60 - size) / 2;
        double rate = 0;
        for (int i = 0; i < curve1.length; i++) {
            if (i >= index && i <= size + index - 1) {
                rate += curve1[i] - curve2[i];
            }
        }
        return StringUtils.getRounding(rate, 3);
    }

}

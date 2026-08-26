package com.example.gtj_f230_rebound_corrosion.corrosion.utils;

import com.example.gtj_f230_rebound_corrosion.base.utils.StringUtils;
import com.orhanobut.logger.Logger;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ComputeCurveUtils {

    /**
     * 修正后的曲线数据
     *
     * @param xs：x
     * @param ys：y
     * @param size：数据长度
     * @return 曲线数据
     */
    public static int[] getCurveY(double[] xs, double[] ys, int size) {
        //三次样条插值
        SplineInterpolator interpolator = new SplineInterpolator();
        UnivariateFunction spline = interpolator.interpolate(xs, ys);
        double min = xs[0];
        double max = xs[xs.length - 1];
        int[] signals = new int[size];
        for (int i = 0; i < size; i++) {
            double x = StringUtils.getInterpolationValue(1, min, size, max, i + 1);
            signals[i] = (int) StringUtils.getRounding(spline.value(x), 0);
        }
        return signals;
    }

    public static int[] getCurveY2(double[] xs, double[] ys, int size) {
        //三次样条插值
        SplineInterpolator interpolator = new SplineInterpolator();
        UnivariateFunction spline = interpolator.interpolate(xs, ys);
        int min = (int) StringUtils.getRoundingMode(xs[0], BigDecimal.ROUND_CEILING, 0);
        double max = (int) StringUtils.getRoundingMode(xs[xs.length - 1], BigDecimal.ROUND_DOWN, 0);
        int[] signals = new int[size];
        for (int i = 0; i < size; i++) {
            if (i >= min && i <= max) {
                signals[i] = (int) StringUtils.getRounding(spline.value(i), 0);
            }
        }
        return signals;
    }

    public static double[] getCurveY1(double[] xs, double[] ys, int size) {
        PolynomialFunction polyFunc = new PolynomialFunction(getCoefficient(xs, ys));
        double min = xs[0];
        double max = xs[xs.length - 1];
        double[] signals = new double[size];
        for (int i = 0; i < size; i++) {
//            double x = StringUtils.getInterpolationValue(1, min, size, max, i + 1);
            signals[i] = (int) StringUtils.getRounding(polyFunc.value(i + 1), 0);
        }
        return signals;
    }

    /**
     * 直线行走距离数据
     *
     * @param R：电杆半径
     * @param r：钢筋半径
     * @param d：厚度
     * @param size：数据长度
     * @return 直线行走距离数据
     */
//    public static double[] getDistance(String poleNorm, double R, double r, double d, int size) {
//        double[] distances = new double[size];
//        for (int i = 0; i < size; i++) {
//            distances[i] = calculateDistanceC(poleNorm, R, r, d, i + 1 - 61);
//        }
//        for (int i = 0; i < size; i++) {
//            distances[i] = distances[i] + (size >> 1) + 1;
//        }
//        return distances;
//    }

    /**
     * 钢筋距小车实际距离
     *
     * @param R：电杆半径
     * @param r：钢筋半径
     * @param d：厚度
     * @param x：行走距离
     * @return 实际距离
     */
    public static double calculateDistanceC(String poleNorm, double R, double r, double d, int x) {
        double angle = calculateAngleInDegrees(R, x);
//        double C = calculateThirdSide(R - d - r, R, angle);
        double C = calculateThirdSide(R - d - 11, R, angle);
        return C;
    }

    /**
     * 行走夹角
     * ∠c = (n * 0.54 / πD) * 360
     *
     * @param R：半径
     * @param x：行走距离
     * @return 夹角
     */
    private static double calculateAngleInDegrees(double R, int x) {
        //∠c
        double x1 = Math.abs(x) * 0.54d;
        double x2 = Math.PI * R * 2;
        return x1 / x2 * 360;
    }

    /**
     * 计算三角形第三边
     * C² = a² + b² - 2ab * cos(c)
     *
     * @param a：a边
     * @param b：b边
     * @param angleDegrees：夹角
     * @return 第三边
     */
    private static double calculateThirdSide(double a, double b, double angleDegrees) {
        // 验证输入有效性
        if (a <= 0 || b <= 0) {
            Logger.e("边长必须为正数");
            return 0;
        }
        if (angleDegrees == 0) {
            return Math.abs(a - b);
        }
        if (angleDegrees <= 0 || angleDegrees >= 180) {
            Logger.e("夹角必须在0°到180°之间（不包括0和180）");
            return 0;
        }
        // 将角度转换为弧度
        double angleRadians = Math.toRadians(angleDegrees);
        // 使用余弦定理：c² = a² + b² - 2ab·cos(C)
        double cSquared = a * a + b * b - 2 * a * b * Math.cos(angleRadians);
        // 检查计算结果是否有效（应为正数）
        if (cSquared <= 0) {
            Logger.e("无法构成有效三角形，请检查输入值");
            return 0;
        }
        return Math.sqrt(cSquared);
    }

    /**
     * 拟合曲线（最小二乘法）
     *
     * @param x：xs
     * @param y：ys
     * @return 曲线系数
     */
    public static double[] getCoefficient(double[] x, double[] y) {
        WeightedObservedPoints obs = new WeightedObservedPoints();
        for (int i = 0; i < x.length; i++) {
            obs.add(x[i], y[i]);
        }
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(2);
        return fitter.fit(obs.toList());
    }

    /**
     * Z-score标准化
     */
    public static List<Double> standardizeCurve(int[] curve) {
        // 标准化
        List<Double> standardized = new ArrayList<>();
        //
//        // 计算均值
//        double sum = 0.0;
//        for (int value : curve) {
//            sum += value;
//        }
//        double mean = sum / curve.length;
//
//        // 计算标准差
//        double variance = 0.0;
//        for (int value : curve) {
//            variance += Math.pow(value - mean, 2);
//        }
//        double stdDev = Math.sqrt(variance / curve.length);
//        //
//        if (stdDev == 0) {
//            for (int value : curve) {
//                standardized.add((double) value);
//            }
//            return standardized;
//        }
//        for (int value : curve) {
//            standardized.add((value - mean) / stdDev);
//        }

//---------------------------------------------------------

        for (int value : curve) {
            standardized.add((double) value);
        }
        return standardized;
    }
}

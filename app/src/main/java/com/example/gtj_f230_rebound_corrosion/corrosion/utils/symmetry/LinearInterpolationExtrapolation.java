package com.example.gtj_f230_rebound_corrosion.corrosion.utils.symmetry;

import com.example.gtj_f230_rebound_corrosion.base.utils.StringUtils;

import java.util.Arrays;

public class LinearInterpolationExtrapolation {

    /**
     * 基础线性插值（内插值）
     *
     * @param x       已知x值数组
     * @param y       已知y值数组
     * @param targetX 目标x值
     * @return 内插的y值
     */
    public static double interpolate(double[] x, double[] y, double targetX) {
        validateInput(x, y);

        // 如果目标x在数据范围之外，转为外推
        if (targetX < x[0] || targetX > x[x.length - 1]) {
            return extrapolate(x, y, targetX);
        }

        // 查找目标x所在的区间
        int index = findIntervalIndex(x, targetX);

        if (index < 0) {
            // targetX恰好等于某个已知点
            return y[-index - 1];
        }

        // 线性插值公式
        double x0 = x[index];
        double x1 = x[index + 1];
        double y0 = y[index];
        double y1 = y[index + 1];

        return y0 + (y1 - y0) * (targetX - x0) / (x1 - x0);
    }

    /**
     * 基础线性外推
     *
     * @param x       已知x值数组
     * @param y       已知y值数组
     * @param targetX 目标x值
     * @return 外推的y值
     */
    public static double extrapolate(double[] x, double[] y, double targetX) {
        validateInput(x, y);

        // 判断是向左外推还是向右外推
        if (targetX < x[0]) {
            return extrapolateLeft(x, y, targetX);
        } else {
            return extrapolateRight(x, y, targetX);
        }
    }

    /**
     * 向左外推（使用前两个点）
     */
    private static double extrapolateLeft(double[] x, double[] y, double targetX) {
        double x1 = x[0];
        double x2 = x[1];
        double y1 = y[0];
        double y2 = y[1];

        double slope = (y2 - y1) / (x2 - x1);
        return y1 + slope * (targetX - x1);
    }

    /**
     * 向右外推（使用最后两个点）
     */
    private static double extrapolateRight(double[] x, double[] y, double targetX) {
        int n = x.length;
        double x1 = x[n - 2];
        double x2 = x[n - 1];
        double y1 = y[n - 2];
        double y2 = y[n - 1];

        double slope = (y2 - y1) / (x2 - x1);
        return y2 + slope * (targetX - x2);
    }

    /**
     * 基于线性回归的外推
     * 使用所有点进行线性拟合，然后外推
     */
    public static double extrapolateByRegression(double[] x, double[] y, double targetX) {
        validateInput(x, y);

        // 使用最小二乘法计算线性回归参数
        LinearRegressionResult result = calculateLinearRegression(x, y);

        return result.slope * targetX + result.intercept;
    }

    /**
     * 批量插值/外推
     *
     * @param x        已知x数组
     * @param y        已知y数组
     * @param targetXs 目标x数组
     * @return 对应的y值数组
     */
    public static double[] interpolateBatch(double[] x, double[] y, double[] targetXs) {
        validateInput(x, y);

        double[] results = new double[targetXs.length];
        for (int i = 0; i < targetXs.length; i++) {
            results[i] = StringUtils.getRounding(interpolate(x, y, targetXs[i]), 1);
        }
        return results;
    }

    /**
     * 带边界检查的插值
     *
     * @param x                  已知x数组
     * @param y                  已知y数组
     * @param targetX            目标x值
     * @param allowExtrapolation 是否允许外推
     * @return 插值结果
     * @throws IllegalArgumentException 如果不允许外推且targetX超出范围
     */
    public static double interpolateWithBounds(double[] x, double[] y,
                                               double targetX, boolean allowExtrapolation) {
        validateInput(x, y);

        if (!allowExtrapolation && (targetX < x[0] || targetX > x[x.length - 1])) {
            throw new IllegalArgumentException(
                    "目标值 " + targetX + " 超出数据范围 [" + x[0] + ", " + x[x.length - 1] +
                            "]，且不允许外推");
        }

        return interpolate(x, y, targetX);
    }

    /**
     * 查找目标x所在的区间索引
     *
     * @return 正数：区间起始索引；负数：恰好等于某点（绝对值为索引+1）
     */
    private static int findIntervalIndex(double[] x, double targetX) {
        // 二分查找
        int left = 0;
        int right = x.length - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;

            if (Math.abs(x[mid] - targetX) < 1e-10) {
                return -(mid + 1); // 负值表示恰好等于
            }

            if (x[mid] < targetX) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        // 返回区间的起始索引
        return Math.max(0, Math.min(right, x.length - 2));
    }

    /**
     * 计算线性回归参数
     */
    private static LinearRegressionResult calculateLinearRegression(double[] x, double[] y) {
        int n = x.length;

        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumX2 += x[i] * x[i];
        }

        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;

        return new LinearRegressionResult(slope, intercept);
    }

    /**
     * 验证输入数据
     */
    private static void validateInput(double[] x, double[] y) {
        if (x == null || y == null) {
            throw new IllegalArgumentException("输入数组不能为null");
        }
        if (x.length != y.length) {
            throw new IllegalArgumentException("x和y数组长度必须相同");
        }
        if (x.length < 2) {
            throw new IllegalArgumentException("至少需要两个数据点");
        }

        // 检查x是否已排序
        for (int i = 1; i < x.length; i++) {
            if (x[i] <= x[i - 1]) {
                throw new IllegalArgumentException("x数组必须严格递增排序");
            }
        }
    }

    /**
     * 线性回归结果封装
     */
    private static class LinearRegressionResult {
        final double slope;
        final double intercept;

        LinearRegressionResult(double slope, double intercept) {
            this.slope = slope;
            this.intercept = intercept;
        }
    }

    public static void main(String[] args) {
        // 示例数据
        double[] x = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] y = {10.0, 20.0, 30.0, 40.0, 50.0};

        // 内插示例
        double targetX1 = 2.5;
        double result1 = interpolate(x, y, targetX1);
        System.out.println("内插结果(" + targetX1 + "): " + result1);

        // 外推示例（向左）
        double targetX2 = 0.5;
        double result2 = extrapolate(x, y, targetX2);
        System.out.println("向左外推(" + targetX2 + "): " + result2);

        // 外推示例（向右）
        double targetX3 = 6.0;
        double result3 = extrapolate(x, y, targetX3);
        System.out.println("向右外推(" + targetX3 + "): " + result3);

        // 线性回归外推
        double result4 = extrapolateByRegression(x, y, targetX3);
        System.out.println("回归外推(" + targetX3 + "): " + result4);
    }
}

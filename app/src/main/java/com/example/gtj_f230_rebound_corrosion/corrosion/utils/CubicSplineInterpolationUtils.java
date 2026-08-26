package com.example.gtj_f230_rebound_corrosion.corrosion.utils;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

public class CubicSplineInterpolationUtils {

    public static void main(String[] args) {
        // 已知数据点
        double[] knownX = {1, 5, 8, 18};
        double[][] knownY = {{100, 200, 300, 400, 500, 600},    // x=1
                {105, 205, 305, 405, 505, 605},    // x=5
                {108, 208, 308, 408, 508, 608},    // x=8
                {128, 218, 328, 418, 528, 618}     // x=18
        };

        // 目标x值
        double[] targetX = {2, 3, 4, 6, 7, 9, 10, 11};

        // 使用Apache Commons Math进行三次样条插值
        double[][] result = interpolateWithApacheMath(knownX, knownY, targetX, false);

        // 输出结果
        System.out.println("Apache Commons Math 三次样条插值结果：");
        System.out.println("=================================");
        for (int i = 0; i < targetX.length; i++) {
            System.out.printf("x = %.0f: %s%n", targetX[i], formatArray(result[i]));
        }

        System.out.println("\n对比：线性插值结果（作为参考）：");
        System.out.println("=================================");
        double[][] linearResult = interpolateWithApacheMath(knownX, knownY, targetX, true);
        for (int i = 0; i < targetX.length; i++) {
            System.out.printf("x = %.0f: %s%n", targetX[i], formatArray(linearResult[i]));
        }
    }

    /**
     * 使用Apache Commons Math进行插值
     *
     * @param knownX  已知x坐标
     * @param knownY  已知y值数组
     * @param targetX 目标x坐标
     * @param linear  是否使用线性插值（false则使用三次样条）
     * @return 插值结果
     */
    public static double[][] interpolateWithApacheMath(double[] knownX, double[][] knownY, double[] targetX, boolean linear) {
        int m = knownY[0].length;  // 每个数组的长度（6）
        int targetCount = targetX.length;

        // 存储每个位置的插值函数
        PolynomialSplineFunction[] splineFunctions = new PolynomialSplineFunction[m];

        // 为每个数组位置创建插值函数
        for (int pos = 0; pos < m; pos++) {
            // 提取该位置的y值序列
            double[] yPos = new double[knownX.length];
            for (int i = 0; i < knownX.length; i++) {
                yPos[i] = knownY[i][pos];
            }

            try {
                if (linear) {
                    // 线性插值
                    LinearInterpolator interpolator = new LinearInterpolator();
                    splineFunctions[pos] = interpolator.interpolate(knownX, yPos);
                } else {
                    // 三次样条插值
                    SplineInterpolator interpolator = new SplineInterpolator();
                    splineFunctions[pos] = interpolator.interpolate(knownX, yPos);
                }
            } catch (Exception e) {
                System.err.println("位置 " + pos + " 插值失败: " + e.getMessage());
                return null;
            }
        }

        // 对每个目标x进行插值
        double[][] result = new double[targetCount][m];

        for (int i = 0; i < targetCount; i++) {
            double tx = targetX[i];

            for (int pos = 0; pos < m; pos++) {
                try {
                    result[i][pos] = splineFunctions[pos].value(tx);
                } catch (Exception e) {
                    // 如果超出插值范围，使用边界值
                    if (tx < knownX[0]) {
                        result[i][pos] = knownY[0][pos];
                    } else if (tx > knownX[knownX.length - 1]) {
                        result[i][pos] = knownY[knownX.length - 1][pos];
                    } else {
                        // 线性外推
                        result[i][pos] = linearExtrapolate(knownX, knownY, tx, pos);
                    }
                }
            }
        }

        return result;
    }

    /**
     * 简单的线性外推
     */
    private static double linearExtrapolate(double[] x, double[][] y, double targetX, int pos) {
        int n = x.length;

        // 找到最近的两个点
        if (targetX < x[0]) {
            // 左外推：使用前两个点
            double x0 = x[0], y0 = y[0][pos];
            double x1 = x[1], y1 = y[1][pos];
            double slope = (y1 - y0) / (x1 - x0);
            return y0 + slope * (targetX - x0);
        } else if (targetX > x[n - 1]) {
            // 右外推：使用最后两个点
            double x0 = x[n - 2], y0 = y[n - 2][pos];
            double x1 = x[n - 1], y1 = y[n - 1][pos];
            double slope = (y1 - y0) / (x1 - x0);
            return y1 + slope * (targetX - x1);
        } else {
            // 实际上不应该走到这里
            return 0;
        }
    }

    /**
     * 格式化数组输出，保留一位小数
     */
    private static String formatArray(double[] arr) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(String.format("%.1f", arr[i]));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 获取每个位置的详细插值信息
     */
    public static void printDetailedInfo(double[] knownX, double[][] knownY) {
        int m = knownY[0].length;

        System.out.println("\n详细分析：");
        System.out.println("================");

        for (int pos = 0; pos < m; pos++) {
            System.out.printf("\n数组位置 %d 的数据序列:%n", pos);
            for (int i = 0; i < knownX.length; i++) {
                System.out.printf("  x=%.0f: y=%.1f%n", knownX[i], knownY[i][pos]);
            }

            // 计算增长率
            System.out.println("  增长率:");
            for (int i = 1; i < knownX.length; i++) {
                double deltaY = knownY[i][pos] - knownY[i - 1][pos];
                double deltaX = knownX[i] - knownX[i - 1];
                double rate = deltaY / deltaX;
                System.out.printf("    [%.0f->%.0f]: %.3f/单位%n", knownX[i - 1], knownX[i], rate);
            }
        }
    }
}

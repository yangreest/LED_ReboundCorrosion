package com.example.gtj_f230_rebound_corrosion.corrosion.utils.lowpassfilter;

public class LowPassFilter {

    /**
     * 移动平均低通滤波
     *
     * @param data       原始数据
     * @param windowSize 窗口大小（必须为奇数）
     * @return 滤波后的数据
     */
    public static double[] movingAverageFilter(double[] data, int windowSize) {
        if (windowSize % 2 == 0) {
            throw new IllegalArgumentException("窗口大小必须为奇数");
        }

        double[] filtered = new double[data.length];
        int halfWindow = windowSize / 2;

        for (int i = 0; i < data.length; i++) {
            double sum = 0;
            int count = 0;

            // 处理边界情况
            for (int j = -halfWindow; j <= halfWindow; j++) {
                int index = i + j;
                if (index >= 0 && index < data.length) {
                    sum += data[index];
                    count++;
                }
            }

            filtered[i] = sum / count;
        }

        return filtered;
    }

    /**
     * 一阶低通滤波（指数平滑）
     *
     * @param data  原始数据
     * @param alpha 平滑系数 (0 < alpha < 1)，越小滤波越强
     * @return 滤波后的数据
     */
    public static double[] firstOrderFilter(double[] data, double alpha) {
        if (alpha <= 0 || alpha >= 1) {
            throw new IllegalArgumentException("alpha必须在0和1之间");
        }

        double[] filtered = new double[data.length];
        filtered[0] = data[0]; // 初始化第一个值

        for (int i = 1; i < data.length; i++) {
            // y[i] = alpha * x[i] + (1 - alpha) * y[i-1]
            filtered[i] = alpha * data[i] + (1 - alpha) * filtered[i - 1];
        }

        return filtered;
    }

}

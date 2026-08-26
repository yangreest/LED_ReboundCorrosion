package com.example.gtj_f230_rebound_corrosion.corrosion.utils.lowpassfilter;

public class GaussianFilter {

    /**
     * 高斯低通滤波
     *
     * @param data       原始数据
     * @param sigma      标准差，控制滤波强度
     * @param windowSize 窗口大小
     * @return 滤波后的数据
     */
    public static double[] gaussianFilter(double[] data, double sigma, int windowSize) {
        if (windowSize % 2 == 0) {
            windowSize++; // 确保窗口大小为奇数
        }

        // 生成高斯核
        double[] kernel = createGaussianKernel(sigma, windowSize);
        double[] filtered = new double[data.length];

        int halfWindow = windowSize / 2;

        for (int i = 0; i < data.length; i++) {
            double sum = 0;
            double weightSum = 0;

            for (int j = -halfWindow; j <= halfWindow; j++) {
                int index = i + j;
                if (index >= 0 && index < data.length) {
                    sum += data[index] * kernel[j + halfWindow];
                    weightSum += kernel[j + halfWindow];
                }
            }

            filtered[i] = sum / weightSum;
        }

        return filtered;
    }

    private static double[] createGaussianKernel(double sigma, int size) {
        double[] kernel = new double[size];
        double sum = 0;
        int center = size / 2;

        for (int i = 0; i < size; i++) {
            int x = i - center;
            kernel[i] = Math.exp(-(x * x) / (2 * sigma * sigma)) / (Math.sqrt(2 * Math.PI) * sigma);
            sum += kernel[i];
        }

        // 归一化
        for (int i = 0; i < size; i++) {
            kernel[i] /= sum;
        }

        return kernel;
    }
}

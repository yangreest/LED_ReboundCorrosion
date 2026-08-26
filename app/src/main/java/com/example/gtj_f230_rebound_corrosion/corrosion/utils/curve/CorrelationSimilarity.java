package com.example.gtj_f230_rebound_corrosion.corrosion.utils.curve;

import java.util.List;

public class CorrelationSimilarity {

    /**
     * 计算皮尔逊相关系数
     *
     * @return 相关系数，-1到1之间，越接近1越相似
     */
    public static double calculatePearsonCorrelation(List<Double> curve1, List<Double> curve2) {
        if (curve1.size() != curve2.size()) {
            throw new IllegalArgumentException("曲线长度必须相等");
        }

        int n = curve1.size();

        // 计算均值
        double sum1 = 0.0, sum2 = 0.0;
        for (int i = 0; i < n; i++) {
            sum1 += curve1.get(i);
            sum2 += curve2.get(i);
        }
        double mean1 = sum1 / n;
        double mean2 = sum2 / n;

        // 计算协方差和方差
        double cov = 0.0;
        double var1 = 0.0, var2 = 0.0;

        for (int i = 0; i < n; i++) {
            double diff1 = curve1.get(i) - mean1;
            double diff2 = curve2.get(i) - mean2;

            cov += diff1 * diff2;
            var1 += diff1 * diff1;
            var2 += diff2 * diff2;
        }

        if (var1 == 0 || var2 == 0) {
            return 0.0;
        }

        return cov / Math.sqrt(var1 * var2);
    }

    /**
     * 判断两条曲线是否形状相似
     */
    public static boolean isShapeSimilar(List<Double> curve1, List<Double> curve2, double threshold) {
        double correlation = calculatePearsonCorrelation(curve1, curve2);
        return correlation >= threshold; // 通常阈值设为0.8或0.9
    }
}

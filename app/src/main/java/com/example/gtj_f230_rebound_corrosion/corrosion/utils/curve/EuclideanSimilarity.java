package com.example.gtj_f230_rebound_corrosion.corrosion.utils.curve;

import java.util.List;

public class EuclideanSimilarity {

    /**
     * 计算两条曲线的欧氏距离
     */
    public static double calculateEuclideanDistance(List<Double> curve1,
                                                    List<Double> curve2) {
        if (curve1.size() != curve2.size()) {
            throw new IllegalArgumentException("曲线长度必须相等");
        }

        double sum = 0.0;
        for (int i = 0; i < curve1.size(); i++) {
            double diff = curve1.get(i) - curve2.get(i);
            sum += diff * diff;
        }

        return Math.sqrt(sum);
    }

    /**
     * 归一化欧氏距离（0-1之间）
     */
    public static double calculateNormalizedEuclidean(List<Double> curve1,
                                                      List<Double> curve2) {
        double euclideanDist = calculateEuclideanDistance(curve1, curve2);

        // 计算数据范围用于归一化
        double max1 = curve1.stream().max(Double::compare).orElse(1.0);
        double min1 = curve1.stream().min(Double::compare).orElse(0.0);
        double range1 = max1 - min1;

        double max2 = curve2.stream().max(Double::compare).orElse(1.0);
        double min2 = curve2.stream().min(Double::compare).orElse(0.0);
        double range2 = max2 - min2;

        double maxRange = Math.max(range1, range2);

        return euclideanDist / (maxRange * Math.sqrt(curve1.size()));
    }
}

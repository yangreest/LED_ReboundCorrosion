package com.example.gtj_f230_rebound_corrosion.corrosion.utils.curve;

import java.util.List;

public class CurveComparatorNew {

    public static class ComparisonResult {
        public double dtwDistance;
        public double euclideanDistance;
        public double pearsonCorrelation;
        public boolean isSimilar;

        @Override
        public String toString() {
            return String.format("DTW: %.4f, Euclidean: %.4f, Correlation: %.4f, Similar: %b", dtwDistance, euclideanDistance, pearsonCorrelation, isSimilar);
        }
    }

    /**
     * 综合判断两条曲线是否相似
     */
    public static ComparisonResult compareCurves(List<Double> curve1, List<Double> curve2, DTWConfig dtwConfig, EuclideanConfig euclidConfig, CorrelationConfig corrConfig) {

        ComparisonResult result = new ComparisonResult();

        // 1. 计算DTW距离
        result.dtwDistance = CurveSimilarity.calculateDTW(curve1, curve2);
        boolean dtwSimilar = result.dtwDistance <= dtwConfig.threshold;

        // 2. 计算欧氏距离（如果长度相同）
        boolean euclidSimilar = false;
        if (curve1.size() == curve2.size()) {
            result.euclideanDistance = EuclideanSimilarity.calculateEuclideanDistance(curve1, curve2);
            euclidSimilar = result.euclideanDistance <= euclidConfig.threshold;
        }

        // 3. 计算相关系数
        boolean corrSimilar = false;
        if (curve1.size() == curve2.size()) {
            result.pearsonCorrelation = CorrelationSimilarity.calculatePearsonCorrelation(curve1, curve2);
            corrSimilar = result.pearsonCorrelation >= corrConfig.threshold;
        }

        // 4. 综合判断
        if (curve1.size() == curve2.size()) {
            // 等长曲线，使用多种指标综合判断
            int vote = 0;
            if (dtwSimilar) vote++;
            if (euclidSimilar) vote++;
            if (corrSimilar) vote++;
            result.isSimilar = vote >= 2; // 至少2个指标通过
        } else {
            // 不等长曲线，主要依赖DTW
            result.isSimilar = dtwSimilar;
        }

        return result;
    }

    /**
     * 在曲线集合中查找与目标曲线最相似的曲线
     */
    public static int findMostSimilarCurve(List<Double> targetCurve, List<List<Double>> curveCollection) {
        if (curveCollection.isEmpty()) {
            return -1;
        }

        int mostSimilarIndex = -1;
        double minDTW = Double.MAX_VALUE;

        for (int i = 0; i < curveCollection.size(); i++) {
            List<Double> curve = curveCollection.get(i);
            double dtw = CurveSimilarity.calculateDTW(targetCurve, curve);

            if (dtw < minDTW) {
                minDTW = dtw;
                mostSimilarIndex = i;
            }
        }

        return mostSimilarIndex;
    }

    // 配置类
    public static class DTWConfig {
        public double threshold = 10.0; // 根据实际情况调整
    }

    public static class EuclideanConfig {
        public double threshold = 5.0; // 根据实际情况调整
    }

    public static class CorrelationConfig {
        public double threshold = 0.8; // 相关系数阈值
    }
}

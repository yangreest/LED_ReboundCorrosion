package com.example.gtj_f230_rebound_corrosion.corrosion.utils.curve;

import java.util.List;

public class CurveSimilarity {

    /**
     * 计算两条曲线的动态时间规整距离
     *
     * @param curve1 第一条曲线（点列表）
     * @param curve2 第二条曲线（点列表）
     * @return DTW距离，越小越相似
     */
    public static double calculateDTW(List<Double> curve1, List<Double> curve2) {
        int n = curve1.size();
        int m = curve2.size();

        // 创建距离矩阵
        double[][] dtw = new double[n + 1][m + 1];

        // 初始化
        for (int i = 0; i <= n; i++) {
            dtw[i][0] = Double.MAX_VALUE;
        }
        for (int j = 0; j <= m; j++) {
            dtw[0][j] = Double.MAX_VALUE;
        }
        dtw[0][0] = 0;

        // 计算DTW
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                double cost = Math.abs(curve1.get(i - 1) - curve2.get(j - 1));
                dtw[i][j] = cost + Math.min(dtw[i - 1][j], Math.min(dtw[i][j - 1], dtw[i - 1][j - 1]));
            }
        }

        return dtw[n][m];
    }

    /**
     * 判断两条曲线是否相似（基于DTW）
     */
    public static boolean isSimilarByDTW(List<Double> curve1, List<Double> curve2, double threshold) {
        double dtwDistance = calculateDTW(curve1, curve2);
        return dtwDistance <= threshold;
    }
}

package com.example.gtj_f230_rebound_corrosion.corrosion.utils;

public class MovingAverage {
    public static void main(String[] args) {
        double[] data = {494, 493, 505, 528, 534, 528, 529, 541, 563, 575, 562, 563, 584, 591, 578, 593, 609, 598, 598, 616, 622, 612, 608, 612, 625, 635, 624, 618, 620, 631, 638, 622, 617, 629, 635, 616, 612, 626, 630, 608, 590, 603, 607, 590, 577, 579, 591, 566, 555, 571, 558, 537, 531, 539, 526, 508, 517, 511, 489, 479, 489};

        // 设置窗口大小
        int windowSize = 5;

        // 应用移动平均
        double[] smoothed = movingAverage(data, windowSize);

        System.out.println("移动平均结果（窗口大小=" + windowSize + "）：");
        System.out.println("索引\t原始值\t\t平滑值");
        for (int i = 0; i < data.length; i++) {
            System.out.printf("%2d\t%.2f\t\t%.2f\n", i, data[i], smoothed[i]);
        }
    }

    public static double[] movingAverage(double[] data, int windowSize) {
        double[] result = new double[data.length];

        for (int i = 0; i < data.length; i++) {
            double sum = 0;
            int count = 0;

            // 计算窗口内的平均值
            for (int j = i - windowSize / 2; j <= i + windowSize / 2; j++) {
                if (j >= 0 && j < data.length) {
                    sum += data[j];
                    count++;
                }
            }
            result[i] = sum / count;
        }

        return result;
    }
}

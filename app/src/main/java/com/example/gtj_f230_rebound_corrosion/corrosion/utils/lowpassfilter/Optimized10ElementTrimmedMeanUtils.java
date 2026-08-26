package com.example.gtj_f230_rebound_corrosion.corrosion.utils.lowpassfilter;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 去掉3个最小去掉3个最大，剩余4个求平均值
 */
public class Optimized10ElementTrimmedMeanUtils {
    private static final int SIZE = 6;
    private static final int TRIM = 2;
    private static final int MIDDLE_COUNT = SIZE - 2 * TRIM;

    private final double[] window = new double[SIZE];
    private int index = 0;
    private boolean full = false;
    private final ReentrantLock lock = new ReentrantLock();

    // 缓存
    private double cachedAverage = 0.0;
    private boolean cacheValid = false;

    /**
     * 针对10个元素的特殊优化排序
     */
    private void optimizedSort4Trim(double[] arr) {
        // 网络排序算法或手动展开循环
        // 这里使用插入排序优化版（针对小数组）
        for (int i = 1; i < SIZE; i++) {
            double key = arr[i];
            int j = i - 1;

            // 内联比较和移动
            while (j >= 0 && arr[j] > key) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
    }

    /**
     * 优化的修剪平均值计算
     */
    private double calculateOptimizedTrimmedMean() {
        if (!full) return 0.0;

        // 方法1：部分选择（针对4个中间值优化）
        double[] copy = window.clone();

        // 只找到第4小的值（索引3）和第7小的值（索引6）
        // 使用手动展开的选择算法

        // 找到前3个最小值
        for (int i = 0; i < TRIM; i++) {
            int minIdx = i;
            for (int j = i + 1; j < SIZE; j++) {
                if (copy[j] < copy[minIdx]) {
                    minIdx = j;
                }
            }
            // 交换
            double temp = copy[i];
            copy[i] = copy[minIdx];
            copy[minIdx] = temp;
        }

        // 找到后3个最大值
        for (int i = SIZE - 1; i >= SIZE - TRIM; i--) {
            int maxIdx = i;
            for (int j = 0; j < i; j++) {
                if (copy[j] > copy[maxIdx]) {
                    maxIdx = j;
                }
            }
            // 交换
            double temp = copy[i];
            copy[i] = copy[maxIdx];
            copy[maxIdx] = temp;
        }

        // 计算中间4个值的和
        double sum = copy[2] + copy[3];
//        double sum = copy[3] + copy[4] + copy[5] + copy[6];
        return sum / MIDDLE_COUNT;
    }

    /**
     * 去掉3个最小去掉3个最大，剩余4个求平均值
     *
     * @param value ：信号
     * @return 中间4个平均值
     */
    public double addValue(double value) {
        lock.lock();
        try {
            window[index] = value;
            index = (index + 1) % SIZE;

            if (!full && index == 0) {
                full = true;
            }

            cacheValid = false;

            if (full) {
                cachedAverage = calculateOptimizedTrimmedMean();
                cacheValid = true;
                return cachedAverage;
            }
            return 0.0;
        } finally {
            lock.unlock();
        }
    }

    public double getTrimmedMean() {
        lock.lock();
        try {
            if (!full) return 0.0;

            if (!cacheValid) {
                cachedAverage = calculateOptimizedTrimmedMean();
                cacheValid = true;
            }
            return cachedAverage;
        } finally {
            lock.unlock();
        }
    }
}

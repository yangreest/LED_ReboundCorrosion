package com.example.gtj_f230_rebound_corrosion.corrosion.utils;

import com.example.gtj_f230_rebound_corrosion.corrosion.model.CheckDataBean;

public class SignalArrayBufferUtils {
    private int MAX_SIZE;
    private final int effectiveSpacing = 30;
    private int lastDisplacement;
    private int[] positions;
    private int[] signals;
    private int currentIndex;
    private int count;
    private final Object lock = new Object();

    public SignalArrayBufferUtils() {

    }

    public void setSize(int size) {
        MAX_SIZE = size;
        positions = new int[MAX_SIZE];
        signals = new int[MAX_SIZE];
        currentIndex = 0;
        count = 0;
    }

    /**
     * 添加位移和信号值
     */
    public CheckDataBean putAndCheck(int position, int signal) {
        synchronized (lock) {
            // 存储数据（循环缓冲区）
            positions[currentIndex] = position;
            signals[currentIndex] = signal;
            currentIndex = (currentIndex + 1) % MAX_SIZE;

            if (count < MAX_SIZE) {
                count++;
            }

            // 检查是否达到61个
            if (count == MAX_SIZE) {
                // 计算中间索引（61个元素的中间是第31个）
                int middleIndex = getAbsoluteIndex(MAX_SIZE / 2); // 索引30
                int middleSignal = signals[middleIndex];
                //判断信号值 最小不小于50
                if (middleSignal > 50) {
                    // 检查是否为最大值
                    if (isMiddleMax(middleSignal)) {
                        //判断最大信号值间隔30个数
                        int displacement = positions[middleIndex];
                        if (Math.abs(lastDisplacement - displacement) >= effectiveSpacing) {
                            lastDisplacement = displacement;
                            // 返回完整的61个数据
                            CheckDataBean checkDataBean = new CheckDataBean(displacement, middleSignal);
                            checkDataBean.curve_y_original = new double[count];
                            checkDataBean.curve_x = new int[count];
                            for (int i = 0; i < count; i++) {
                                int index = getAbsoluteIndex(i);
                                checkDataBean.curve_x[i] = positions[index];
                                checkDataBean.curve_y_original[i] = signals[index];
                            }
                            return checkDataBean;
                        }
                    }
                }
            }
            return null;
        }
    }

    /**
     * 计算绝对索引
     */
    private int getAbsoluteIndex(int relativeIndex) {
        if (count < MAX_SIZE) {
            return relativeIndex;
        }
        // 当缓冲区满时，考虑循环
        int start = currentIndex; // 当前最旧元素的位置
        return (start + relativeIndex) % MAX_SIZE;
    }

    /**
     * 检查中间值是否为最大值
     */
    private boolean isMiddleMax(int middleValue) {
        int maxValue = Integer.MIN_VALUE;

        for (int i = 0; i < count; i++) {
            int index = getAbsoluteIndex(i);
            if (signals[index] > maxValue) {
                maxValue = signals[index];
            }
        }

        return middleValue == maxValue;
    }

    /**
     * 清空缓冲区
     */
    public void clear() {
        synchronized (lock) {
            currentIndex = 0;
            count = 0;
        }
    }

    /**
     * 获取当前数据数量
     */
    public int size() {
        synchronized (lock) {
            return count;
        }
    }
}
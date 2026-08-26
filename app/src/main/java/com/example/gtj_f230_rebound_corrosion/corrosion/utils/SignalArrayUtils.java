package com.example.gtj_f230_rebound_corrosion.corrosion.utils;

public class SignalArrayUtils {
    private static final int MAX_SIZE = 1200;
    private static final int[] signals = new int[MAX_SIZE];
    private static int currentIndex;
    private static final Object lock = new Object();

    public static int[] put(int signal) {
        synchronized (lock) {
            signals[currentIndex] = signal;
            if (currentIndex < MAX_SIZE) {
                currentIndex++;
            }
            if (currentIndex == MAX_SIZE) {
                return signals;
            }
        }
        return null;
    }

    /**
     * 清空缓冲区
     */
    public static void clear() {
        synchronized (lock) {
            currentIndex = 0;
        }
    }
}

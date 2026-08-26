package com.example.gtj_f230_rebound_corrosion.corrosion.utils;

public class IntArrayUtils {
    public static int[] subarrayInt(int[] array, int start, int end) {
        if (array == null) {
            return new int[0];
        }
        if (start < 0) {
            start = 0;
        }
        if (end > array.length) {
            end = array.length;
        }
        if (start > end) {
            return new int[0];
        }

        int[] result = new int[end - start];
        System.arraycopy(array, start, result, 0, end - start);
        return result;
    }

    public static double[] subarrayDouble(double[] array, int start, int end) {
        if (array == null) {
            return new double[0];
        }
        if (start < 0) {
            start = 0;
        }
        if (end > array.length) {
            end = array.length;
        }
        if (start > end) {
            return new double[0];
        }

        double[] result = new double[end - start];
        System.arraycopy(array, start, result, 0, end - start);
        return result;
    }
}

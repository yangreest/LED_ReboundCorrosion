package com.example.gtj_f230_rebound_corrosion.corrosion.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Byte数组操作工具类
 * 提供拼接、截取、查找等常用操作
 */
public class ByteArrayUtils {

    private ByteArrayUtils() {
        // 工具类，防止实例化
    }

    // ==================== 拼接操作 ====================

    /**
     * 拼接两个byte数组
     */
    public static byte[] concat(byte[] array1, byte[] array2) {
        if (array1 == null) return array2;
        if (array2 == null) return array1;

        byte[] result = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }

    /**
     * 拼接多个byte数组
     */
    public static byte[] concatAll(byte[]... arrays) {
        if (arrays == null) return new byte[0];

        int totalLength = 0;
        for (byte[] array : arrays) {
            if (array != null) {
                totalLength += array.length;
            }
        }

        byte[] result = new byte[totalLength];
        int currentPos = 0;
        for (byte[] array : arrays) {
            if (array != null && array.length > 0) {
                System.arraycopy(array, 0, result, currentPos, array.length);
                currentPos += array.length;
            }
        }
        return result;
    }

    /**
     * 使用ByteArrayOutputStream拼接（适合动态添加）
     */
    public static byte[] concatWithStream(byte[]... arrays) {
        if (arrays == null) return new byte[0];

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            for (byte[] array : arrays) {
                if (array != null) {
                    outputStream.write(array);
                }
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            // ByteArrayOutputStream不会抛出IOException，但需要处理语法
            return new byte[0];
        }
    }

    // ==================== 截取操作 ====================

    /**
     * 截取byte数组
     */
    public static byte[] subarray(byte[] array, int start, int end) {
        if (array == null) {
            return new byte[0];
        }
        if (start < 0) {
            start = 0;
        }
        if (end > array.length) {
            end = array.length;
        }
        if (start > end) {
            return new byte[0];
        }

        byte[] result = new byte[end - start];
        System.arraycopy(array, start, result, 0, end - start);
        return result;
    }

    /**
     * 从指定位置截取到末尾
     */
    public static byte[] subarrayFrom(byte[] array, int start) {
        if (array == null) return new byte[0];
        return subarray(array, start, array.length);
    }

    /**
     * 截取指定长度
     */
    public static byte[] subarrayLength(byte[] array, int start, int length) {
        if (array == null) return new byte[0];
        return subarray(array, start, Math.min(start + length, array.length));
    }

    // ==================== 查找操作 ====================

    /**
     * 查找byte在数组中的位置
     */
    public static int indexOf(byte[] array, byte target) {
        return indexOf(array, target, 0);
    }

    /**
     * 从指定位置开始查找
     */
    public static int indexOf(byte[] array, byte target, int start) {
        if (array == null || start < 0 || start >= array.length) {
            return -1;
        }

        for (int i = start; i < array.length; i++) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 查找子数组位置
     */
    public static int indexOf(byte[] array, byte[] target) {
        return indexOf(array, target, 0);
    }

    /**
     * 从指定位置查找子数组
     */
    public static int indexOf(byte[] array, byte[] target, int start) {
        if (array == null || target == null || target.length == 0) {
            return -1;
        }
        if (start < 0) start = 0;

        outer:
        for (int i = start; i <= array.length - target.length; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    /**
     * 查找所有匹配位置
     */
    public static List<Integer> findAll(byte[] array, byte[] target) {
        List<Integer> positions = new ArrayList<>();
        if (array == null || target == null || target.length == 0) {
            return positions;
        }

        int index = 0;
        while (index < array.length) {
            int pos = indexOf(array, target, index);
            if (pos == -1) break;
            positions.add(pos);
            index = pos + 1;
        }
        return positions;
    }

    // ==================== 取出操作 ====================

    /**
     * 取出指定范围的byte（不修改原数组）
     */
    public static byte[] getRange(byte[] array, int start, int end) {
        return subarray(array, start, end);
    }

    /**
     * 取出第一个匹配的byte之前的所有数据
     */
    public static byte[] getBefore(byte[] array, byte separator) {
        int index = indexOf(array, separator);
        if (index == -1) {
            return array != null ? Arrays.copyOf(array, array.length) : new byte[0];
        }
        return subarray(array, 0, index);
    }

    /**
     * 取出第一个匹配的byte之后的所有数据
     */
    public static byte[] getAfter(byte[] array, byte separator) {
        int index = indexOf(array, separator);
        if (index == -1) {
            return new byte[0];
        }
        return subarray(array, index + 1, array.length);
    }

    /**
     * 按分隔符分割byte数组
     */
    public static List<byte[]> split(byte[] array, byte separator) {
        List<byte[]> result = new ArrayList<>();
        if (array == null || array.length == 0) {
            return result;
        }

        int start = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == separator) {
                if (i > start) {
                    result.add(subarray(array, start, i));
                }
                start = i + 1;
            }
        }

        // 添加最后一段
        if (start < array.length) {
            result.add(subarray(array, start, array.length));
        }

        return result;
    }

    // ==================== 其他工具方法 ====================

    /**
     * 比较两个byte数组是否相等
     */
    public static boolean equals(byte[] array1, byte[] array2) {
        if (array1 == array2) return true;
        if (array1 == null || array2 == null) return false;
        if (array1.length != array2.length) return false;

        for (int i = 0; i < array1.length; i++) {
            if (array1[i] != array2[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是否以指定前缀开头
     */
    public static boolean startsWith(byte[] array, byte[] prefix) {
        if (array == null || prefix == null) return false;
        if (array.length < prefix.length) return false;

        for (int i = 0; i < prefix.length; i++) {
            if (array[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是否以指定后缀结尾
     */
    public static boolean endsWith(byte[] array, byte[] suffix) {
        if (array == null || suffix == null) return false;
        if (array.length < suffix.length) return false;

        int offset = array.length - suffix.length;
        for (int i = 0; i < suffix.length; i++) {
            if (array[offset + i] != suffix[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 转换为十六进制字符串（调试用）
     */
    public static String toHexString(byte[] array) {
        if (array == null) return "null";
        if (array.length == 0) return "";

        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    /**
     * 反转byte数组
     */
    public static byte[] reverse(byte[] array) {
        if (array == null) return new byte[0];

        byte[] result = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[array.length - 1 - i];
        }
        return result;
    }
}

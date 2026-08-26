package com.example.gtj_f230_rebound_corrosion.base.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * double与byte数组转换的工具类
 */
public final class DoubleByteConverter {

    private DoubleByteConverter() {
        // 私有构造函数，防止实例化
    }

    /**
     * 将double转换为大端序byte数组
     */
    public static byte[] toBigEndianBytes(double value) {
        return toBytes(value, ByteOrder.BIG_ENDIAN);
    }

    /**
     * 将double转换为小端序byte数组
     */
    public static byte[] toLittleEndianBytes(double value) {
        return toBytes(value, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * 将double转换为指定字节序的byte数组
     */
    public static byte[] toBytes(double value, ByteOrder order) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(order);
        buffer.putDouble(value);
        return buffer.array();
    }

    /**
     * 从大端序byte数组恢复double
     */
    public static double fromBigEndianBytes(byte[] bytes) {
        return fromBytes(bytes, ByteOrder.BIG_ENDIAN);
    }

    /**
     * 从小端序byte数组恢复double
     */
    public static double fromLittleEndianBytes(byte[] bytes) {
        return fromBytes(bytes, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * 从指定字节序的byte数组恢复double
     */
    public static double fromBytes(byte[] bytes, ByteOrder order) {
        if (bytes.length < 8) {
            throw new IllegalArgumentException("Byte array must be at least 8 bytes");
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(order);
        return buffer.getDouble();
    }

    /**
     * 将double数组转换为byte数组
     */
    public static byte[] toBytes(double[] values, ByteOrder order) {
        ByteBuffer buffer = ByteBuffer.allocate(values.length * 8);
        buffer.order(order);

        for (double value : values) {
            buffer.putDouble(value);
        }

        return buffer.array();
    }

    /**
     * 从byte数组恢复double数组
     */
    public static double[] toDoubleArray(byte[] bytes, ByteOrder order) {
        if (bytes.length % 8 != 0) {
            throw new IllegalArgumentException("Byte array length must be multiple of 8");
        }

        int count = bytes.length / 8;
        double[] result = new double[count];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(order);

        for (int i = 0; i < count; i++) {
            result[i] = buffer.getDouble();
        }

        return result;
    }

    /**
     * 打印byte数组为十六进制
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    // 测试方法
    public static void main(String[] args) {
        // 测试单个double
        double testValue = 123.456;

        byte[] bigEndian = toBigEndianBytes(testValue);
        byte[] littleEndian = toLittleEndianBytes(testValue);

        System.out.println("测试值: " + testValue);
        System.out.println("大端序: " + bytesToHex(bigEndian));
        System.out.println("小端序: " + bytesToHex(littleEndian));

        double recoveredBig = fromBigEndianBytes(bigEndian);
        double recoveredLittle = fromLittleEndianBytes(littleEndian);

        System.out.println("大端序恢复: " + recoveredBig);
        System.out.println("小端序恢复: " + recoveredLittle);

        // 测试double数组
        double[] values = {1.1, 2.2, 3.3, 4.4};
        byte[] arrayBytes = toBytes(values, ByteOrder.BIG_ENDIAN);
        double[] recoveredArray = toDoubleArray(arrayBytes, ByteOrder.BIG_ENDIAN);

        System.out.println("\n数组转换:");
        System.out.print("原始: ");
        for (double v : values) System.out.print(v + " ");
        System.out.print("\n恢复: ");
        for (double v : recoveredArray) System.out.print(v + " ");
    }
}

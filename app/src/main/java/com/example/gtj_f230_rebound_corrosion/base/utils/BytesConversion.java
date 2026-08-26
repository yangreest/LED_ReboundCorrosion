package com.example.gtj_f230_rebound_corrosion.base.utils;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class BytesConversion {
    private static StringBuilder sbd = new StringBuilder();

    public static String toHexString(byte[] byteArray) {
        if (byteArray == null || byteArray.length < 1)
            throw new IllegalArgumentException("this byteArray must not be null or empty");

        final StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < byteArray.length; i++) {
            if ((byteArray[i] & 0xff) < 0x10)//0~F前面不零
                hexString.append("0");
            hexString.append(Integer.toHexString(0xFF & byteArray[i]));
        }
        return hexString.toString().toLowerCase();
    }

    public static byte[] addBytes(byte[] data1, byte[] data2) {
        byte[] data3 = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, data3, 0, data1.length);
        System.arraycopy(data2, 0, data3, data1.length, data2.length);
        return data3;
    }

    /**
     * @param number int
     * @param length byte字节
     * @return string
     */
    public static String intToBytes(int number, int length) {
        length *= 3;
        sbd.delete(0, sbd.length());
        int i = 0;
        char[] S = new char[length];
        if (number == 0) {
            for (int j = 0; j < length; j++) {
                if (j % 3 == 0) {
                    if (j != 0) {
                        sbd.append(" ");
                    }
                } else {
                    sbd.append("2");
                }
            }
            return sbd.toString();
        } else {
            while (number != 0) {
                int t = number % 16;
                if (t >= 0 && t < 10) {
                    S[i] = (char) (t + '0');
                    i++;
                } else {
                    S[i] = (char) (t + 'A' - 10);
                    i++;
                }
                number = number / 16;
            }

            for (int j = length - 1; j >= 0; j--) {
                sbd.append(S[j]);
            }
            return sbd.toString();
        }
    }

    /**
     * 整数转换byte数组
     *
     * @param num    整数
     * @param length 字节长度
     */
    public static byte[] intTobytes(int num, int length) {

        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            int movelength = (length - 1 - i) * 8;
            bytes[length - 1 - i] = (byte) ((num >> movelength) & 0xFF);
        }
        return bytes;
    }

    /**
     * 字符串转换byte数组
     *
     * @param conent  内容
     * @param charset 编码方式
     */
    public static byte[] stringTobytes(String conent, String charset) throws UnsupportedEncodingException {
        return conent.getBytes(charset);
    }

    /**
     * 字节流转16位有符号整数
     *
     * @param src    字节流
     * @param offset 起始索引
     */
    public static short readShort(byte[] src, int offset) {
        int result = 0;
        byte[] newbt = new byte[2];
        for (int i = offset; i < (offset + 2); i++) {
            newbt[i - offset] = src[i];
        }
        ByteBuffer buffer = ByteBuffer.wrap(newbt);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        return buffer.getShort();
    }

    /**
     * byte数组转换32位有符号整数
     */
    public static int readInt(byte[] src, int offset) {

        int result = 0;
        byte[] newbt = new byte[4];
        for (int i = offset; i < (offset + 4); i++) {
            newbt[i - offset] = src[i];
        }
        ByteBuffer buffer = ByteBuffer.wrap(newbt);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        return buffer.getInt();
    }

    /**
     * char转换成byte[]
     */
    public static byte[] charToByte(char c) {
        byte[] b = new byte[2];
        b[0] = (byte) ((c & 0xFF00) >> 8);
        b[1] = (byte) (c & 0xFF);
        return b;
    }

    /**
     * char转换成byte[]
     */
    public static byte[] charToByte1(char c) {
        byte[] b = new byte[2];
        b[1] = (byte) ((c & 0xFF00) >> 8);
        b[0] = (byte) (c & 0xFF);
        return b;
    }

    /**
     * 数字转换byte数组，个十百千每位一个字节，低位在前高位在后
     */
    public static byte[] numTobytes(int num) {
        byte[] numbytes = new byte[4];
        int g = num % 10;
        int s = (num % 100 - g) / 10;
        int b = (num % 1000 - (s * 10) - g) / 100;
        int q = (num - (b * 100) - (s * 10) - g) / 1000;

        numbytes[0] = (byte) g;
        numbytes[1] = (byte) s;
        numbytes[2] = (byte) b;
        numbytes[3] = (byte) q;
        return numbytes;
    }

    /**
     * 数字转换byte数组，个十百千每位一个字节，低位在前高位在后
     */
    public static byte[] numTobytesTwo(int num) {
        byte[] numbytes = new byte[2];
        int g = num % 10;
        int s = (num % 100 - g) / 10;

        numbytes[0] = (byte) g;
        numbytes[1] = (byte) s;

        return numbytes;
    }


    /**
     * byte数组转换数字，个十百千每位一个字节，低位在前高位在后
     */
    public static int bytesTonum(byte[] bytes) {
        int g = bytes[0] & 0xff;
        int s = bytes[1] & 0xff;
        int b = bytes[2] & 0xff;
        int q = bytes[3] & 0xff;

        return g + s * 10 + b * 100 + q * 1000;
    }


    /**
     * 从数据包内容中获得一个整数值(低位在前，高位在后)。
     */
    public static byte[] readByte(int num, int length) {
        byte[] result = new byte[length];

        for (int i = 0; i < length; i++) {
            int n = i * 8;
            if (n == 0) {
                result[i] = (byte) (num & 0xff);
            } else {
                result[i] = (byte) (num >> n & 0xff);
            }
        }
        return result;
    }

    /**
     * 字符串（16进制类型）转byte
     */
    private static byte[] strToBytes2(String str) {
        int size = str.length();
        String[] array = new String[size];
        for (int i = 0; i < size; i++) {
            array[i] = str.charAt(i) + "";
        }
        //对接协议：低位在前，高位在后（根据对接协议对字符串进行修改）
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < array.length / 2; i++) {
            string.insert(0, array[i * 2] + array[i * 2 + 1]);
        }
        try {
            return Hex.decodeHex(string.toString());
        } catch (DecoderException e) {
            e.printStackTrace();
            return new byte[8];
        }
    }

    /**
     * double转16进制字符串（）
     */
    private static String double2Hex(double dou) {
        return Long.toHexString(Double.doubleToLongBits(dou));
    }

    /**
     * 十六进制字符串转字节流
     */
    public static byte[] hexStrToBytes(String hexStr) {
        if (hexStr == null || hexStr.equals("")) {
            return null;
        }

        hexStr = hexStr.toLowerCase();
        int length = hexStr.length() / 2;
        char[] hexChars = hexStr.toCharArray();
        byte[] bytes = new byte[length];
        String hexDigits = "0123456789abcdef";
        for (int i = 0; i < length; i++) {
            int pos = i * 2; // 两个字符对应一个byte
            int h = hexDigits.indexOf(hexChars[pos]) << 4; // 注1
            int l = hexDigits.indexOf(hexChars[pos + 1]); // 注2
            if (h == -1 || l == -1) { // 非16进制字符
                return null;
            }
            bytes[i] = (byte) (h | l);
        }
        return bytes;
    }

    /**
     * 字节流转十六进制字符
     */
    public static String bytesToHexStr(byte[] b) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            buffer.append(hex.toUpperCase()).append(" ");
        }
        return buffer.toString();
    }

    /**
     * 字节流转十六进制字符
     */
    public static String bytesToHexStr(byte[] b, String split) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            buffer.append(hex.toUpperCase()).append(split);
        }
        return buffer.toString();
    }

    /**
     * 字节流转换文本
     */
    public static String bytesToText(byte[] bytes, int begin, int len, String chartset) {
        byte[] bts = new byte[len];
        int j = 0;
        for (int i = begin; i < begin + len; i++) {
            bts[j] = bytes[i];
            j++;
        }

        String s = null;
        try {
            s = new String(bts, chartset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return s;
    }


    /**
     * 截取byte数组
     */
    public static byte[] sub(byte[] bytes, int begin, int length) {
        if (bytes.length < begin + length) {
            return new byte[0];
        }

        byte[] result = Arrays.copyOfRange(bytes, begin, begin + length);
        return result;
    }

    /**
     * 截取String数组
     */
    public static String[] sub(String[] bytes, int begin, int length) {
        if (bytes.length < begin + length) {
            return new String[0];
        }
        String[] result = Arrays.copyOfRange(bytes, begin, begin + length);
        return result;
    }

    /**
     * 将byte数组插入到目标byte数组中
     *
     * @param bytes 目标byte数组
     * @param in    要插入的byte数组
     * @param begin 插入的起始索引（目标数组的）
     */
    public static void insert(byte[] bytes, byte[] in, int begin) {
        if (bytes.length < in.length + begin) {
            throw new RuntimeException("插入数组超出长度");
        }
        //将byte数组插入到目标数组中
        System.arraycopy(in, 0, bytes, begin, in.length);
    }

    /**
     * 16进制转2进制
     */
    public static String hexStringToByte(String hex) {
        int i = Integer.parseInt(hex, 16);
        return Integer.toBinaryString(i);
    }

    /**
     * 16进制转10进制
     */
    public static int hex2decimal(String hex) {
        return Integer.parseInt(hex, 16);
    }

    public static double hex2Double(String hex) {
        Long value = Long.parseLong(hex, 16);
        return value / 1000.0f;
    }

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}

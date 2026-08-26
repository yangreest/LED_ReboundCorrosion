package com.example.gtj_f230_rebound_corrosion.base.utils;

import com.orhanobut.logger.Logger;

public class HexUtil {

    public static byte[] getByte(String string) {
        String[] strArray = string.split(" ");
        byte[] bytes = new byte[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            bytes[i] = hexToByte(strArray[i]);
        }
        return bytes;
    }

    public static byte[] stringToBytes(String text) {
        Logger.e("xxxxx-----发送: " + text);
        int len = text.length();
        byte[] bytes = new byte[(len + 1) / 2];
        for (int i = 0; i < len; i += 2) {
            int size = Math.min(2, len - i);
            String sub = text.substring(i, i + size);
            bytes[i / 2] = (byte) Integer.parseInt(sub, 16);
        }
        return bytes;
    }

    public static String intToHex(int i) {
        StringBuffer Hex = new StringBuffer("");
        String m = "0123456789ABCDEF";
        if (i == 0)
            Hex.append(i);
        while (i != 0) {
            Hex.append(m.charAt(i % 16));
            i >>= 4;
        }
        return Hex.reverse().toString();

    }

    /**
     * Hex字符串转byte
     *
     * @param inHex 待转换的Hex字符串
     * @return 转换后的byte
     */
    private static byte hexToByte(String inHex) {
        return (byte) Integer.parseInt(inHex, 16);
    }


    /**
     * 整数转16进制字符
     */
    public static String[] intTo16String(int var, int len) {
        String[] array = new String[len];
        String string = Integer.toHexString((var & 0x0000FFFF) | 0xFFFF0000).substring(4);
        String temp1 = string.substring(0, 2);//00,48
        String temp2 = string.substring(2);
        if (len == 1) {
            array[0] = temp2;
        } else if (len == 2) {
            array[0] = temp1;
            array[1] = temp2;
        }
        return array;
    }

    public static String[] stringToArray(String temp) {
        String[] arr = new String[temp.length()];
        int i = 0;
        for (int length = temp.length() - 1; length >= 0; length--) {
            arr[i] = "0" + temp.charAt(length);
            i++;
        }
        return arr;
    }

    /**
     * 将数组插入到目标数组中
     */
    static void insert(String[] bytes, String[] in, int begin) {
        if (bytes.length < in.length + begin) {
            throw new RuntimeException("插入数组超出长度");
        }
        System.arraycopy(in, 0, bytes, begin, in.length);
    }

    public static String[] addBytes(String[] data1, String[] data2) {
        String[] data3 = new String[data1.length + data2.length];
        System.arraycopy(data1, 0, data3, 0, data1.length);
        System.arraycopy(data2, 0, data3, data1.length, data2.length);
        return data3;
    }

    public static int getNumber(double number) {
        int tempNumber;
        number = Math.abs(number);
        if (number > 65.536) {
            tempNumber = (int) (number * 100);
        } else {
            tempNumber = (int) (number * 1000);
        }
        return tempNumber;
    }

    public static String getMark0(double number) {
        String string;
        if (number >= 0) {
            if (Math.abs(number) > 65.536) {
                string = "10";
            } else {
                string = "00";
            }
        } else {
            if (Math.abs(number) > 65.536) {
                string = "11";
            } else {
                string = "01";
            }
        }
        return string;
    }

    public static String getMark1(double number) {
        String string;
        if (number >= 0) {
            string = "00";
        } else {
            string = "01";
        }
        return string;
    }
}
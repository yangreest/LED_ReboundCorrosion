package com.example.gtj_f230_rebound_corrosion.base.utils;

public class Base64 {
    private static final char[] a = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/', '='};
    private static final byte[] b = new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};

    private Base64() {
    }

    public static String encode(byte[] var0) {
        return encode(var0, var0.length);
    }

    public static String encode(byte[] var0, int var1) {
        int var2 = (var1 + 2) / 3 * 4;
        char[] var3 = new char[var2];
        int var6 = 0;

        for (int var7 = 0; var6 < var1; var7 += 4) {
            boolean var4 = false;
            boolean var5 = false;
            int var8 = 255 & var0[var6];
            var8 <<= 8;
            if (var6 + 1 < var1) {
                var8 |= 255 & var0[var6 + 1];
                var5 = true;
            }

            var8 <<= 8;
            if (var6 + 2 < var1) {
                var8 |= 255 & var0[var6 + 2];
                var4 = true;
            }

            var3[var7 + 3] = a[var4 ? var8 & 63 : 64];
            var8 >>= 6;
            var3[var7 + 2] = a[var5 ? var8 & 63 : 64];
            var8 >>= 6;
            var3[var7 + 1] = a[var8 & 63];
            var8 >>= 6;
            var3[var7 + 0] = a[var8 & 63];
            var6 += 3;
        }

        return new String(var3);
    }

    public static byte[] decode(String var0) {
        int var1 = a(var0);
        int var2 = var1 / 4 * 3;
        if (var1 % 4 == 3) {
            var2 += 2;
        }

        if (var1 % 4 == 2) {
            ++var2;
        }

        byte[] var3 = new byte[var2];
        int var4 = 0;
        int var5 = 0;
        int var6 = 0;

        for (int var8 = 0; var8 < var0.length(); ++var8) {
            char var7 = var0.charAt(var8);
            byte var9 = var7 > 255 ? -1 : b[var7];
            if (var9 >= 0) {
                var5 <<= 6;
                var4 += 6;
                var5 |= var9;
                if (var4 >= 8) {
                    var4 -= 8;
                    var3[var6++] = (byte) (var5 >> var4 & 255);
                }
            }
        }

        if (var6 != var3.length) {
            return new byte[0];
        } else {
            return var3;
        }
    }

    private static int a(String var0) {
        int var1 = var0.length();

        for (int var3 = 0; var3 < var0.length(); ++var3) {
            char var2 = var0.charAt(var3);
            if (var2 > 255 || b[var2] < 0) {
                --var1;
            }
        }

        return var1;
    }
}

package com.example.gtj_f230_rebound_corrosion.corrosion.utils.fittingcurve;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ComputeUtils {
    /**
     * 计算第三边
     *
     * @param a：第一条边
     * @param b：第二条边
     * @param angleDegrees：夹角度数
     * @return 第三条边
     */
    public static double calculateThirdSide(double a, double b, double angleDegrees) {
        // 将角度转换为弧度
        double angleRadians = Math.toRadians(angleDegrees);

        // 使用余弦定理计算第三边
        double cSquared = a * a + b * b - 2 * a * b * Math.cos(angleRadians);

        // 返回平方根
        return Math.sqrt(cSquared);
    }

    private static void getString(List<int[]> list, double[] t) {
        StringBuilder string = new StringBuilder("[" + t[0] + ", " + t[1] + "] = ");
        for (int[] ints : list) {
            string.append("[").append(ints[0]).append(", ").append(ints[1]).append("], ");
        }
        Log.e("====", "==== " + string);
    }

    /**
     * 根据数据找到匹配的空间索引
     */
    public static List<int[]> findMatchingSpaces(List<int[]> spaces, double[] input) {
        List<int[]> matchingIndices = new ArrayList<>();

        if (input == null || input.length != 2) {
            return matchingIndices;
        }

        double x = input[0];
        double y = input[1];

        // 遍历所有空间
        for (int i = 0; i < spaces.size(); i++) {
            int[] space = spaces.get(i);
            int spaceX = space[0];
            int spaceY = space[1];
            // 检查是否匹配规则
            if (isMatch(x, y, spaceX, spaceY)) {
                matchingIndices.add(space);
            }
        }
        getString(matchingIndices, input);
        return matchingIndices;
    }

    /**
     * 判断数据是否匹配空间
     */
    private static boolean isMatch(double x, double y, int spaceX, int spaceY) {
        if (spaceX == 35 && spaceY == 35) {
            if (x < 35 && y <= 45) {
                return true;
            } else if (x < 30) {
                return true;
            }
        } else {
            boolean b = x >= spaceX - 5 && x <= spaceX + 5 && y >= spaceY - 5 && y <= spaceY + 5;
            if (spaceX == 40 && spaceY == 40) {
                if (b) {
                    return true;
                } else if (x >= 30 && x <= 45 && Math.abs(y - 55) <= 10) {
                    return true;
                }
            } else if (spaceX == 45 && spaceY == 45) {
                if (b) {
                    return true;
                } else if (x >= 40 && x <= 53 && Math.abs(y - 60) <= 10) {
                    return true;
                }
            } else if (spaceX == 50 && spaceY == 50) {
                if (b) {
                    return true;
                } else if (Math.abs(x - 45) <= 5 && Math.abs(y - 57) <= 5) {
                    return true;
                } else if (Math.abs(x - 50) <= 5 && Math.abs(y - 60) <= 5) {
                    return true;
                }
            } else if (spaceX == 55 && spaceY == 55) {
                if (b) {
                    return true;
                } else if (Math.abs(x - 45) <= 5 && Math.abs(y - 57) <= 5) {
                    return true;
                } else if (Math.abs(x - 50) <= 5 && y >= 50 && y <= 84) {
                    return true;
                }
            } else if (spaceX == 60 && spaceY == 60) {
                if (b) {
                    return true;
                } else if (Math.abs(x - 56) <= 5 && Math.abs(y - 67) <= 5) {
                    return true;
                } else if (Math.abs(x - 60) <= 5 && y >= 60 && y <= 80) {
                    return true;
                }
            } else if (spaceX == 65 && spaceY == 65) {
                if (b) {
                    return true;
                } else if (x >= 58 && x <= 70 && y >= 60 && y <= 85) {
                    return true;
                }
            } else if (spaceX == 70 && spaceY == 70) {
                if (b) {
                    return true;
                } else if (x >= 55 && x <= 74 && Math.abs(y - 80) <= 5) {
                    return true;
                }
            } else if (spaceX == 75 && spaceY == 75) {
                if (b) {
                    return true;
                } else if (x >= 75 && y >= 75) {
                    return true;
                }
            } else if (spaceX == 35 && spaceY == 65) {
                if (b) {
                    return true;
                } else if (x <= 35 && y >= 60) {
                    return true;
                }
            } else if (spaceX == 40 && spaceY == 80) {
                if (b) {
                    return true;
                } else if (x >= 35 && x <= 45 && y >= 70 && y <= 120) {
                    return true;
                }
            } else if (spaceX == 50 && spaceY == 100) {
                if (b) {
                    return true;
                } else if (x >= 45 && y >= 85) {
                    return true;
                }
            }
        }
        return false;
    }
}

package com.example.gtj_f230_rebound_corrosion.corrosion.utils.fittingcurve;

import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_f230_rebound_corrosion.base.view.StyleAlertDialog2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class ValleyDetectionUtils {
    private static StringBuilder stringBuilder = new StringBuilder();

    // 峰间最小间距（采样点数），防止同一根钢筋附近重复选峰
    private static final int MIN_PEAK_DISTANCE = 60;
    // 最小峰宽（半突出度处的采样点数），过滤窄噪声尖峰，需按实际扫描采样率标定
    private static final int MIN_PEAK_WIDTH = 0;
    // 突出度门槛：峰突出度至少达到最大突出度的10%，过滤低矮毛刺峰
    private static final double MIN_PROMINENCE_RATIO = 0.06;

    // 查找最明显的4个波峰（基于高度）
    //    public static List<Integer> findTop4Peaks(double[] data) {
    //        stringBuilder.delete(0, stringBuilder.length());
    //        // 首先找到所有波峰
    //        List<Integer> allPeaks = findAllPeaks(data);
    //        // 按波峰高度排序（从高到低）
    //        List<PeakInfo> peakInfos = new ArrayList<>();
    //        for (int idx : allPeaks) {
    //            peakInfos.add(new PeakInfo(idx, data[idx]));
    //        }
    //
    //        // 按高度降序排序
    //        peakInfos.sort((a, b) -> (int) (b.value - a.value));
    //        // 取前4个最高的波峰
    //        List<Integer> top4Peaks = new ArrayList<>();
    //        int tempIndex = peakInfos.get(0).index;
    //        top4Peaks.add(tempIndex);
    //        int count = 1;
    //        for (int i = 1; i < peakInfos.size(); i++) {
    //            if (Math.abs(tempIndex - peakInfos.get(i).index) > 100) {
    //                top4Peaks.add(peakInfos.get(i).index);
    //                tempIndex = peakInfos.get(i).index;
    //                count++;
    //            }
    //            if (count == 4) {
    //                break;
    //            }
    //        }
    //        // 按索引位置排序
    //        Collections.sort(top4Peaks);
    //        stringBuilder.append("\n所有波峰：").append(allPeaks.size()).append(" ， 4个：").append(top4Peaks);
    //        return top4Peaks;
    //    }

    // 查找最明显的4个波峰（突出度 + 峰宽 + 最小间距三重判据）
    public static List<Integer> findTop4Peaks(double[] data) {
        stringBuilder.delete(0, stringBuilder.length());
        // 1. 找到所有局部波峰
        List<Integer> allPeaks = findAllPeaks(data);
        // 2. 计算每个波峰的突出度与半突出度峰宽
        List<PeakInfo> peakInfos = new ArrayList<>();
        double maxProminence = 0;
        for (int idx : allPeaks) {
            double prominence = calcProminence(data, idx);
            if (prominence > maxProminence) {
                maxProminence = prominence;
            }
            peakInfos.add(new PeakInfo(idx, data[idx], prominence, calcPeakWidth(data, idx, prominence)));
        }
        // 3. 剔除低突出度毛刺峰和窄噪声尖峰
        List<PeakInfo> validPeaks = new ArrayList<>();
        for (PeakInfo p : peakInfos) {
            if (p.prominence >= maxProminence * MIN_PROMINENCE_RATIO && p.width >= MIN_PEAK_WIDTH) {
                validPeaks.add(p);
            }
        }
        // 4. 按突出度降序排序（突出度比原始幅值更能反映峰的真实显著性）
        validPeaks.sort((a, b) -> Double.compare(b.prominence, a.prominence));
        // 5. 贪心选取：与所有已选峰的间距都必须大于最小间距
        List<Integer> top4Peaks = new ArrayList<>();
        for (PeakInfo p : validPeaks) {
            boolean farEnough = true;
            for (int selected : top4Peaks) {
                if (Math.abs(selected - p.index) <= MIN_PEAK_DISTANCE) {
                    farEnough = false;
                    break;
                }
            }
            if (farEnough) {
                top4Peaks.add(p.index);
            }
            if (top4Peaks.size() == 10) {
                break;
            }
        }


        // 6. 按索引位置排序
        Collections.sort(top4Peaks);

        // 7. 候选超过4个时，顺序滑窗筛选：依次取连续4个为一组，
        //    比较每组第2、3个点中的最低值，取最低值最低的那组作为最终4个波峰
        if (top4Peaks.size() > 4) {
            int bestStart = 0;
            double bestMin = Double.MAX_VALUE;
            for (int start = 0; start + 3 < top4Peaks.size(); start++) {
                // 该组4个点中的第2、3个点
                int p2 = top4Peaks.get(start + 1);
                int p3 = top4Peaks.get(start + 2);
                double min23 = Math.min(data[p2], data[p3]);
                if (min23 < bestMin) {
                    bestMin = min23;
                    bestStart = start;
                }
            }
            stringBuilder.append("\n候选波峰超过4个，滑窗筛选起点索引：").append(bestStart)
                    .append(" ，中间两点最低值：").append(bestMin);
            top4Peaks = new ArrayList<>(top4Peaks.subList(bestStart, bestStart + 4));
        }
        stringBuilder.append("\n所有波峰：").append(allPeaks.size())
                .append(" ，有效波峰：").append(validPeaks.size())
                .append(" ， 4个：").append(top4Peaks);
        return top4Peaks;
    }
    private static List<Integer> findAllPeaks(double[] data) {
//        List<Integer> peaks = new ArrayList<>();
//        for (int i = 1; i < data.length - 1; i++) {
//            if (data[i] > data[i - 1] && data[i] > data[i + 1]) {
//                peaks.add(i);
//            }
//        }
//        return peaks;

        List<Integer> peaks = new ArrayList<>();
        int length = data.length;
        if (length < 3) {
            return peaks;
        }

        int i = 1;
        while (i < length - 1) {
            // 寻找连续相等值的右边界（处理平顶峰）
            int j = i;
            while (j < length - 2 && data[j] == data[j + 1]) {
                j++;
            }

            // 判断该段是否为局部极大值
            if (data[i] > data[i - 1] && data[j] > data[j + 1]) {
                // 取平顶的中点作为峰值索引
                peaks.add((i + j) / 2);
            }

            // 跳到下一段
            i = j + 1;
        }

        return peaks;
   }

    // 计算波峰突出度：峰顶相对参考等高线的垂直高度
    // 参考等高线：从峰顶分别向左右两侧走到更高峰（或边界）为止，记录沿途最低点，取两侧最低点中的较高者
    private static double calcProminence(double[] data, int peakIndex) {
        double peakValue = data[peakIndex];
        // 向左搜索：遇到更高峰或到达左边界为止，记录沿途最低点
        double leftMin = peakValue;
        boolean hasHigherLeft = false;
        for (int i = peakIndex - 1; i >= 0; i--) {
            if (data[i] < leftMin) {
                leftMin = data[i];
            }
            if (data[i] > peakValue) {
                hasHigherLeft = true;
                break;
            }
        }
        // 向右搜索：同理
        double rightMin = peakValue;
        boolean hasHigherRight = false;
        for (int i = peakIndex + 1; i < data.length; i++) {
            if (data[i] < rightMin) {
                rightMin = data[i];
            }
            if (data[i] > peakValue) {
                hasHigherRight = true;
                break;
            }
        }
        double reference;
        if (hasHigherLeft || hasHigherRight) {
            reference = Math.max(leftMin, rightMin);
        } else {
            // 全局最高峰：以全曲线最小值作为参考
            reference = peakValue;
            for (double v : data) {
                if (v < reference) {
                    reference = v;
                }
            }
        }
        return peakValue - reference;
    }

    // 计算半突出度处的峰宽（采样点数），用于区分宽缓的钢筋响应峰与窄噪声尖峰
    private static int calcPeakWidth(double[] data, int peakIndex, double prominence) {
        double halfLevel = data[peakIndex] - prominence / 2.0;
        int left = peakIndex;
        // 向左扩展：保持在半高线以上且持续下降，遇到回升（另一个峰的边缘）即停止
        while (left > 0 && data[left - 1] >= halfLevel && data[left - 1] <= data[left]) {
            left--;
        }
        int right = peakIndex;
        while (right < data.length - 1 && data[right + 1] >= halfLevel && data[right + 1] <= data[right]) {
            right++;
        }
        return right - left;
    }

    // 在4个波峰之间找3个波谷
    public static List<Integer> findValleysBetweenPeaks(double[] data, List<Integer> peakIndices) {
        List<Integer> valleys = new ArrayList<>();
        if (peakIndices.size() < 4) {
            //ToastUtils.showLong("请重新竖向扫描，" + peakIndices.size() + "\n" + valleys);
            return valleys;
        }
        // 在每对相邻波峰之间找波谷
        for (int i = 0; i < peakIndices.size() - 1; i++) {
            int leftPeak = peakIndices.get(i);
            int rightPeak = peakIndices.get(i + 1);

            // 在这个区间内找波谷
            int valleyIndex = findDeepestValleyInRange(data, leftPeak, rightPeak);
            if (valleyIndex != -1) {
                valleys.add(valleyIndex);
            }
        }

        // 确保找到3个波谷
        if (valleys.size() < 3 && peakIndices.size() >= 4) {
            // 如果没找到足够波谷，使用全局最小值
            valleys = ensure3Valleys(data, peakIndices, valleys);
        }
        return valleys;
    }

    // 在指定范围内找最深的波谷
    private static int findDeepestValleyInRange(double[] data, int start, int end) {
        int deepestValleyIndex = -1;
        double deepestValue = Integer.MAX_VALUE;

        for (int i = start + 1; i < end; i++) {
            // 检查是否是波谷
            if (isValley(data, i)) {
                if (data[i] < deepestValue) {
                    deepestValue = data[i];
                    deepestValleyIndex = i;
                }
            }
        }

        // 如果没找到波谷，找区间内最小值
        if (deepestValleyIndex == -1) {
            for (int i = start + 1; i < end; i++) {
                if (data[i] < deepestValue) {
                    deepestValue = data[i];
                    deepestValleyIndex = i;
                }
            }
        }

        return deepestValleyIndex;
    }

    // 确保有3个波谷
    private static List<Integer> ensure3Valleys(double[] data, List<Integer> peaks, List<Integer> valleys) {
        List<Integer> result = new ArrayList<>(valleys);

        // 如果已经够了，直接返回
        if (result.size() >= 3) {
            return result.subList(0, 3);
        }

        // 找到所有波谷
        List<Integer> allValleys = findAllValleys(data);

        // 按位置找到在4个波峰之间的3个最合适的波谷
        if (peaks.size() >= 4 && result.size() < 3) {
            // 寻找在第一个和第二个波峰之间的波谷
            if (result.size() < 1) {
                int valley = findValleyBetween(data, peaks.get(0), peaks.get(1), allValleys);
                if (valley != -1) result.add(valley);
            }

            // 寻找在第二个和第三个波峰之间的波谷
            if (result.size() < 2) {
                int valley = findValleyBetween(data, peaks.get(1), peaks.get(2), allValleys);
                if (valley != -1) result.add(valley);
            }

            // 寻找在第三个和第四个波峰之间的波谷
            if (result.size() < 3) {
                int valley = findValleyBetween(data, peaks.get(2), peaks.get(3), allValleys);
                if (valley != -1) result.add(valley);
            }
        }

        return result;
    }

    // 在范围内找波谷
    private static int findValleyBetween(double[] data, int start, int end, List<Integer> valleys) {
        for (int valley : valleys) {
            if (valley > start && valley < end) {
                return valley;
            }
        }
        return -1;
    }

    // 判断是否为波谷
    private static boolean isValley(double[] data, int index) {
        if (index <= 0 || index >= data.length - 1) {
            return false;
        }
        return data[index] < data[index - 1] && data[index] < data[index + 1];
    }

    // 找到所有波峰


    // 找到所有波谷
    private static List<Integer> findAllValleys(double[] data) {
        List<Integer> valleys = new ArrayList<>();
        for (int i = 1; i < data.length - 1; i++) {
            if (data[i] < data[i - 1] && data[i] < data[i + 1]) {
                valleys.add(i);
            }
        }
        return valleys;
    }

    // 主算法：查找4个明显波峰和对应的3个波谷
    public static Map<String, List<Integer>> findPeaksAndValleys(double[] data) {
        Map<String, List<Integer>> result = new HashMap<>();

        // 1. 找到最明显的波峰
        List<Integer> top4Peaks = findTop4Peaks(data);

        // 2. 找到这4个波峰之间的3个波谷
        List<Integer> valleys = findValleysBetweenPeaks(data, top4Peaks);
        if (!valleys.isEmpty()) {
            result.put("peaks", top4Peaks);
            result.put("valleys", valleys);
        }
        return result;
    }
    public static Map<String, List<Integer>> findPeaksAndValleys(double[] data, int numPeaks) {
        Map<String, List<Integer>> result = new HashMap<>();
        int t = numPeaks;
        // 1. 找到曲线上的波峰
        List<Integer> top4Peaks = findTop4Peaks(data);
        if(top4Peaks.size() < 4){
            //ToastUtils.showLong("请重新竖向扫描，" + top4Peaks.size());
            return result;
        }

        List<Integer> top2Peaks = top4Peaks.subList(0, 2);

        // 2. 选取前两个波峰
        int leftPeak = top4Peaks.get(0);
        int rightPeak = top4Peaks.get(1);

        // 3. 在两个波峰之间找到最低点
        int valleyIndex = leftPeak;
        double minValue = data[leftPeak];
        for (int i = leftPeak + 1; i < rightPeak; i++) {
            if (data[i] < minValue) {
                minValue = data[i];
                valleyIndex = i;
            }
        }

        // 4. 找到最低点与两个波峰的中点
        int leftMid = (leftPeak + valleyIndex) / 2;
        int rightMid = (valleyIndex + rightPeak) / 2;

        // 5. 记录三个点：左侧中点、最低点、右侧中点
        List<Integer> valleys = new ArrayList<>();
        valleys.add(leftMid);
        valleys.add(valleyIndex);
        valleys.add(rightMid);

        // 6. 根据leftPeak和valleyIndex的距离差，找到valleyIndex关于leftPeak的镜像对称点
        //    同理找到valleyIndex关于rightPeak的镜像对称点
        //    开头数据不够则取尽可能远的起始位置，尾部数据不够则取尽可能远的末尾位置
        int mirrorLeft = leftPeak - (valleyIndex - leftPeak);
        if (mirrorLeft < 0) {
            mirrorLeft = 0;
        }
        int mirrorRight = rightPeak + (rightPeak - valleyIndex);
        if (mirrorRight > data.length - 1) {
            mirrorRight = data.length - 1;
        }

        // 7. 按从左到右顺序记录5个点：左镜像点、左中点、最低点、右中点、右镜像点
        top2Peaks.add(0, mirrorLeft);
        top2Peaks.add(mirrorRight);

        if (!valleys.isEmpty()) {
            result.put("peaks", top2Peaks);
            result.put("valleys", valleys);
        }
        return result;
    }

    // 辅助类：存储波峰信息
    static class PeakInfo {
//        int index;
//        double value;
//
//        PeakInfo(int index, double value) {
//            this.index = index;
//            this.value = value;
        int index;
        double value;
        double prominence;
        int width;

        PeakInfo(int index, double value, double prominence, int width) {
            this.index = index;
            this.value = value;
            this.prominence = prominence;
            this.width = width;
        }
    }

    // 测试主函数
    public static void main(String[] args) {
        double[] temp = {265, 69, 68, 67, 68, 67, 66, 67, 68, 67, 66, 67, 68, 67, 68, 69, 68, 67, 65, 66, 67, 66, 67, 68, 69, 70, 71, 72, 74, 73, 75, 76, 81, 86, 87, 89, 91, 92, 98, 107, 113, 117, 119, 121, 134, 146, 150, 156, 161, 167, 172, 174, 208, 248, 254, 258, 269, 281, 322, 365, 383, 392, 400, 418, 504, 593, 620, 635, 648, 678, 712, 745, 952, 1160, 1210, 1267, 1322, 1381, 1673, 1970, 2042, 2112, 2183, 2262, 2537, 2814, 2895, 2934, 2977, 3062, 3480, 3884, 3945, 4009, 4071, 4130, 4315, 4487, 4510, 4526, 4516, 4486, 4458, 4427, 4392, 4354, 4076, 3789, 3724, 3650, 3574, 3497, 3101, 2704, 2630, 2594, 2554, 2473, 2209, 1951, 1880, 1810, 1743, 1676, 1398, 1130, 1085, 1041, 998, 957, 806, 664, 639, 617, 596, 574, 523, 476, 460, 449, 440, 430, 410, 397, 400, 405, 448, 496, 513, 530, 548, 567, 682, 809, 850, 888, 926, 968, 1136, 1310, 1365, 1422, 1484, 1548, 1561, 1615, 1684, 1935, 2188, 2265, 2693, 3116, 3227, 3307, 3341, 3616, 3878, 3911, 3942, 3972, 3996, 3994, 3973, 3948, 3916, 3793, 3665, 3616, 3560, 3503, 3445, 3080, 2716, 2651, 2578, 2507, 2472, 2172, 1840, 1777, 1722, 1666, 1606, 1445, 1291, 1243, 1195, 1152, 1108, 926, 749, 721, 697, 671, 646, 557, 472, 456, 443, 434, 424, 388, 358, 356, 353, 359, 368, 373, 380, 391, 400, 409, 421, 470, 521, 540, 560, 582, 603, 729, 860, 895, 935, 975, 1018, 1178, 1342, 1394, 1448, 1504, 1561, 1566, 1619, 1679, 1899, 2123, 2189, 2256, 2325, 2388, 2695, 3002, 3058, 3112, 3164, 3190, 3333, 3490, 3513, 3536, 3558, 3575, 3589, 3585, 3565, 3544, 3520, 3507, 3494, 3292, 3080, 3029, 2972, 2913, 2855, 2610, 2394, 2360, 2300, 2242, 2178, 2114, 2051, 1909, 1699, 1572, 1518, 1464, 1438, 1416, 1189, 967, 932, 898, 863, 845, 742, 625, 601, 581, 561, 538, 527, 517, 456, 400, 392, 382, 371, 366, 345, 326, 325, 327, 328, 342, 360, 367, 377, 387, 390, 396, 407, 496, 581, 592, 615, 638, 662, 769, 881, 920, 959, 980, 1002, 1194, 1394, 1453, 1513, 1578, 1641, 1704, 1765, 1802, 2169, 2572, 2665, 2758, 2844, 2933, 3219, 3499, 3586, 3681, 3779, 3871, 4202, 4525, 4598, 4667, 4727, 4784, 4842, 4877, 4903, 5050, 5186, 5204, 5219, 5170, 5097, 5063, 5026, 4984, 4939, 4716, 4485, 4418, 4353, 4286, 4214, 4138, 4062, 3939, 3669, 3446, 3370, 3293, 3219, 3184, 2851, 2486, 2418, 2348, 2279, 2214, 2061, 1940, 1909, 1849, 1791, 1736, 1685, 1634, 1581, 1370, 1164, 1125, 1089, 1049, 1015, 862, 725, 712, 686, 661, 637, 552, 467, 455, 450, 438, 422, 405, 389, 337, 292, 287, 275, 266, 256, 220, 183, 175, 170, 164, 159, 140, 122, 117, 112, 109, 106, 103, 93, 84, 80, 76, 75, 74, 65, 55, 53, 51, 49, 46, 44, 40, 39, 37, 35, 34, 33, 32, 29, 25, 23, 22, 23, 22, 20, 19, 18, 17, 15, 14, 13, 12, 9, 8, 9, 7, 5, 6, 5, 4, 3, 4, 5, 4, 3, 2, 3, 2, 3, 2, 3, 2, 3, 2, 1, 0, 1, 2, 1, 3, 4, 5, 6, 4, 5, 8, 11, 13, 15, 16, 17, 20, 23, 24, 25, 33, 40, 41, 43, 44, 45, 44, 43, 42, 41, 42, 43, 45, 49, 51, 52, 55, 57, 60, 64, 67, 69, 72, 73, 72, 71, 70, 69, 68, 69, 70, 71, 74, 78, 79, 81, 85, 89, 92, 93, 94, 96, 98, 100, 103, 114, 127, 131, 134, 138, 142, 153, 168, 174, 176, 180, 183, 191, 199, 203, 207, 210, 213, 214, 219, 226, 229, 230, 231};

        System.out.println("=== 查找4个明显波峰和3个波谷 ===");

        Map<String, List<Integer>> result = findPeaksAndValleys(temp);
        if (result.isEmpty()) {
            return;
        }
        List<Integer> peaks = result.get("peaks");
        List<Integer> valleys = result.get("valleys");

        System.out.println("\n4个最明显的波峰:");
        for (int i = 0; i < peaks.size(); i++) {
            int idx = peaks.get(i);
            System.out.printf("\n波峰" + (i + 1) + ": 索引=" + idx + ", 值=" + temp[idx]);
        }

        System.out.println("\n对应的3个波谷:");
        for (int i = 0; i < valleys.size(); i++) {
            int idx = valleys.get(i);
            System.out.printf("\n波谷" + (i + 1) + ": 索引=" + idx + ", 值=" + temp[idx]);
        }
    }
}

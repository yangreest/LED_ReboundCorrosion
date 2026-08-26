package com.example.gtj_f230_rebound_corrosion.corrosion.utils.symmetry;

public class MirrorSymmetryAligner {

    /**
     * 使用镜像对称进行对齐
     *
     * @param reference 参考信号
     * @param target    目标信号
     * @return 对齐后的信号
     */
    public static double[] alignByMirrorSymmetry(double[] reference, double[] target) {
        int length = Math.min(reference.length, target.length);

        // 找到参考信号的对称轴
        int refMirrorPoint = findMirrorPoint(reference);
        System.out.println("参考信号镜像点: " + refMirrorPoint);

        // 找到目标信号的对称轴
        int targetMirrorPoint = findMirrorPoint(target);
        System.out.println("目标信号镜像点: " + targetMirrorPoint);

        // 计算偏移量
        int offset = targetMirrorPoint - refMirrorPoint;

        // 对齐到镜像点
        return alignToMirrorPoint(reference, target, refMirrorPoint);
    }

    /**
     * 寻找最佳镜像点（左右镜像对称性最好的点）
     */
    private static int findMirrorPoint(double[] signal) {
        int length = signal.length;
        double[] mirrorScores = new double[length];

        for (int mirrorPoint = 0; mirrorPoint < length; mirrorPoint++) {
            // 分别考虑左右两侧
            int leftLength = mirrorPoint;
            int rightLength = length - mirrorPoint - 1;
            int compareLength = Math.min(leftLength, rightLength);

            double score = 0;
            for (int i = 1; i <= compareLength; i++) {
                double left = signal[mirrorPoint - i];
                double right = signal[mirrorPoint + i];
                score += Math.abs(left - right);
            }

            mirrorScores[mirrorPoint] = compareLength > 0 ? score / compareLength : Double.MAX_VALUE;
        }

        // 找到最小得分（最对称）的点
        int bestPoint = 0;
        double bestScore = mirrorScores[0];
        for (int i = 1; i < length; i++) {
            if (mirrorScores[i] < bestScore) {
                bestScore = mirrorScores[i];
                bestPoint = i;
            }
        }

        return bestPoint;
    }

    /**
     * 基于镜像点对齐
     */
    private static double[] alignToMirrorPoint(double[] reference, double[] target, int refMirrorPoint) {
        int length = Math.min(reference.length, target.length);
        double[] aligned = new double[length];

        // 创建目标信号的镜像版本
        double[] mirroredTarget = createMirroredSignal(target, refMirrorPoint);

        // 与参考信号比较，选择更好的对齐
        double correlationOriginal = calculateCorrelation(reference, target, refMirrorPoint);
        double correlationMirrored = calculateCorrelation(reference, mirroredTarget, refMirrorPoint);

        if (correlationMirrored > correlationOriginal) {
            System.out.println("使用镜像信号对齐，相关系数: " + correlationMirrored);
            return mirroredTarget;
        } else {
            System.out.println("使用原始信号对齐，相关系数: " + correlationOriginal);
            // 简单平移对齐
            int targetMirrorPoint = findMirrorPoint(target);
            int shift = targetMirrorPoint - refMirrorPoint;
            return shiftSignal(target, -shift);
        }
    }

    /**
     * 创建以指定点为中心的镜像信号
     */
    private static double[] createMirroredSignal(double[] signal, int mirrorPoint) {
        double[] mirrored = new double[signal.length];

        // 镜像中心点
        mirrored[mirrorPoint] = signal[mirrorPoint];

        // 创建左右镜像
        int maxRadius = Math.min(mirrorPoint, signal.length - 1 - mirrorPoint);
        for (int r = 1; r <= maxRadius; r++) {
            // 取左右平均值作为镜像值
            double left = signal[mirrorPoint - r];
            double right = signal[mirrorPoint + r];
            double mirroredValue = (left + right) / 2;

            mirrored[mirrorPoint - r] = mirroredValue;
            mirrored[mirrorPoint + r] = mirroredValue;
        }

        // 处理边缘部分
        if (mirrorPoint - maxRadius > 0) {
            for (int i = 0; i < mirrorPoint - maxRadius; i++) {
                mirrored[i] = signal[i];
            }
        }

        if (mirrorPoint + maxRadius < signal.length - 1) {
            for (int i = mirrorPoint + maxRadius + 1; i < signal.length; i++) {
                mirrored[i] = signal[i];
            }
        }

        return mirrored;
    }

    /**
     * 计算以中心点为基础的相关系数
     */
    private static double calculateCorrelation(double[] signal1, double[] signal2, int center) {
        int radius = Math.min(center, Math.min(signal1.length - 1 - center, signal2.length - 1 - center));
        radius = Math.min(radius, 50); // 限制范围

        double sum1 = 0, sum2 = 0;
        double sum11 = 0, sum22 = 0, sum12 = 0;
        int count = 0;

        for (int i = center - radius; i <= center + radius; i++) {
            if (i >= 0 && i < signal1.length && i < signal2.length) {
                double x = signal1[i];
                double y = signal2[i];
                sum1 += x;
                sum2 += y;
                sum11 += x * x;
                sum22 += y * y;
                sum12 += x * y;
                count++;
            }
        }

        if (count == 0) return 0;

        double cov = sum12 - sum1 * sum2 / count;
        double var1 = sum11 - sum1 * sum1 / count;
        double var2 = sum22 - sum2 * sum2 / count;

        if (var1 <= 0 || var2 <= 0) return 0;

        return cov / Math.sqrt(var1 * var2);
    }

    private static double[] shiftSignal(double[] signal, int shift) {
        return SymmetricSignalAligner.shiftSignal(signal, shift);
    }
}

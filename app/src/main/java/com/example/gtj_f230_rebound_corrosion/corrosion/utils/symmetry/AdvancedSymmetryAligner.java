package com.example.gtj_f230_rebound_corrosion.corrosion.utils.symmetry;

import java.util.Arrays;

public class AdvancedSymmetryAligner {

    /**
     * 多策略对称对齐
     */
    public static class AlignmentResult {
        public double[] alignedSignal;
        public int referenceCenter;
        public int targetCenter;
        public double symmetryScore;
        public double correlation;

        public AlignmentResult(double[] alignedSignal, int refCenter, int targetCenter, double symmetryScore, double correlation) {
            this.alignedSignal = alignedSignal;
            this.referenceCenter = refCenter;
            this.targetCenter = targetCenter;
            this.symmetryScore = symmetryScore;
            this.correlation = correlation;
        }
    }

    /**
     * 智能对称对齐
     *
     * @param reference：参考信号
     * @param target：目标信号
     * @return 对齐后的信号
     */
    public static AlignmentResult intelligentSymmetryAlign(double[] reference, double[] target) {
        // 检测信号类型
        boolean isRefSymmetric = checkSymmetry(reference);
        boolean isTargetSymmetric = checkSymmetry(target);

        System.out.println("参考信号对称性: " + isRefSymmetric);
        System.out.println("目标信号对称性: " + isTargetSymmetric);

        if (isRefSymmetric && isTargetSymmetric) {
            // 双方都对称，使用中心对称对齐
            return centerSymmetryAlign(reference, target);
        } else if (isRefSymmetric) {
            // 只有参考信号对称，强制目标信号对称化
            return forceSymmetryAlign(reference, target);
        } else {
            // 都不对称或只有目标对称，使用传统方法
            return traditionalAlign(reference, target);
        }
    }

    /**
     * 中心对称对齐
     */
    private static AlignmentResult centerSymmetryAlign(double[] reference, double[] target) {
        int refCenter = SymmetricSignalAligner.findSymmetryCenter(reference);
        int targetCenter = SymmetricSignalAligner.findSymmetryCenter(target);

        // 使用增强的对称信号
        double[] enhancedRef = SymmetricSignalAligner.enhanceSymmetry(reference, refCenter);
        double[] enhancedTarget = SymmetricSignalAligner.enhanceSymmetry(target, targetCenter);

        // 对齐
        int shift = targetCenter - refCenter;
        double[] aligned = SymmetricSignalAligner.shiftSignal(enhancedTarget, -shift);

        // 计算得分
        double symmetryScore = calculateOverallSymmetry(aligned);
        double correlation = calculateFullCorrelation(enhancedRef, aligned);

        return new AlignmentResult(aligned, refCenter, targetCenter, symmetryScore, correlation);
    }

    /**
     * 强制对称对齐
     */
    private static AlignmentResult forceSymmetryAlign(double[] reference, double[] target) {
        int refCenter = SymmetricSignalAligner.findSymmetryCenter(reference);

        // 使目标信号强制对称化
        double[] symmetricTarget = SymmetricSignalAligner.enhanceSymmetry(target, refCenter);

        // 使用镜像对称对齐
        double[] aligned = MirrorSymmetryAligner.alignByMirrorSymmetry(reference, symmetricTarget);

        // 计算得分
        double symmetryScore = calculateOverallSymmetry(aligned);
        double correlation = calculateFullCorrelation(reference, aligned);

        return new AlignmentResult(aligned, refCenter, refCenter, symmetryScore, correlation);
    }

    /**
     * 传统对齐（作为后备）
     */
    private static AlignmentResult traditionalAlign(double[] reference, double[] target) {
        // 使用互相关对齐
        int bestShift = findBestSymmetryShift(reference, target);
        double[] aligned = SymmetricSignalAligner.shiftSignal(target, -bestShift);

        int refCenter = reference.length / 2;
        int targetCenter = refCenter + bestShift;

        double symmetryScore = calculateOverallSymmetry(aligned);
        double correlation = calculateFullCorrelation(reference, aligned);

        return new AlignmentResult(aligned, refCenter, targetCenter, symmetryScore, correlation);
    }

    /**
     * 检查信号对称性
     */
    private static boolean checkSymmetry(double[] signal) {
        int center = signal.length / 2;
        double score = SymmetricSignalAligner.calculateSymmetryScore(signal, center);

        // 阈值判断，可根据实际情况调整
        double maxValue = Arrays.stream(signal).max().orElse(1);
        double threshold = 0.2 * maxValue; // 20%的容差

        return score < threshold;
    }

    /**
     * 基于对称性寻找最佳平移
     */
    private static int findBestSymmetryShift(double[] reference, double[] target) {
        int maxShift = Math.min(reference.length, target.length) / 4;
        double bestScore = Double.MAX_VALUE;
        int bestShift = 0;

        for (int shift = -maxShift; shift <= maxShift; shift++) {
            double[] shifted = SymmetricSignalAligner.shiftSignal(target, shift);
            double score = 0;

            // 计算多个中心点的对称性
            for (int center = reference.length / 4; center < 3 * reference.length / 4; center += 10) {
                score += SymmetricSignalAligner.calculateSymmetryScore(shifted, center);
            }

            if (score < bestScore) {
                bestScore = score;
                bestShift = shift;
            }
        }

        return bestShift;
    }

    /**
     * 计算整体对称性得分
     */
    private static double calculateOverallSymmetry(double[] signal) {
        int center = signal.length / 2;
        double score = 0;

        // 测试多个可能的中心点
        int testPoints = 5;
        for (int i = 0; i < testPoints; i++) {
            int testCenter = signal.length * (i + 1) / (testPoints + 1);
            score += SymmetricSignalAligner.calculateSymmetryScore(signal, testCenter);
        }

        return score / testPoints;
    }

    /**
     * 计算全信号相关系数
     */
    private static double calculateFullCorrelation(double[] signal1, double[] signal2) {
        int length = Math.min(signal1.length, signal2.length);

        double sum1 = 0, sum2 = 0;
        double sum11 = 0, sum22 = 0, sum12 = 0;

        for (int i = 0; i < length; i++) {
            double x = signal1[i];
            double y = signal2[i];
            sum1 += x;
            sum2 += y;
            sum11 += x * x;
            sum22 += y * y;
            sum12 += x * y;
        }

        double cov = sum12 - sum1 * sum2 / length;
        double var1 = sum11 - sum1 * sum1 / length;
        double var2 = sum22 - sum2 * sum2 / length;

        if (var1 <= 0 || var2 <= 0) return 0;

        return cov / Math.sqrt(var1 * var2);
    }
}

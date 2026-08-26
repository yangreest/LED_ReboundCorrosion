package com.example.gtj_f230_rebound_corrosion.corrosion.utils.symmetry;

public class SymmetricSignalAligner {

    /**
     * 对称对齐的主方法
     *
     * @param reference     参考信号（应该是基本对称的）
     * @param target        目标信号
     * @param symmetryPoint 对称中心点索引（-1表示自动检测）
     * @return 对齐后的目标信号
     */
    public static double[] alignSymmetricSignals(double[] reference, double[] target, int symmetryPoint) {
        if (reference == null || target == null) {
            throw new IllegalArgumentException("信号不能为空");
        }

        // 自动检测参考信号的对称中心
        if (symmetryPoint < 0) {
            symmetryPoint = findSymmetryCenter(reference);
            System.out.println("检测到参考信号对称中心点: " + symmetryPoint);
        }

        // 检测目标信号的对称中心
        int targetSymmetryPoint = findSymmetryCenter(target);
        System.out.println("检测到目标信号对称中心点: " + targetSymmetryPoint);

        // 计算对称中心偏移量
        int centerShift = targetSymmetryPoint - symmetryPoint;

        // 根据对称中心进行对齐
        return centerAlignBySymmetry(reference, target, symmetryPoint, targetSymmetryPoint);
    }

    /**
     * 寻找信号的对称中心
     * 使用左右互相关方法检测最佳对称点
     */
    public static int findSymmetryCenter(double[] signal) {
        int length = signal.length;
        int maxCenter = length - 1;
        double[] symmetryScores = new double[length];

        for (int center = 0; center < length; center++) {
            symmetryScores[center] = calculateSymmetryScore(signal, center);
        }

        // 找到对称性最好的点
        int bestCenter = 0;
        double bestScore = symmetryScores[0];
        for (int i = 1; i < length; i++) {
            if (symmetryScores[i] < bestScore) {
                bestScore = symmetryScores[i];
                bestCenter = i;
            }
        }

        return bestCenter;
    }

    /**
     * 计算以某个点为中心的对称性得分（越小越对称）
     */
    public static double calculateSymmetryScore(double[] signal, int center) {
        int length = signal.length;
        double totalError = 0.0;
        int count = 0;

        // 计算左右两侧的对称误差
        int maxRadius = Math.min(center, length - 1 - center);
        for (int r = 1; r <= maxRadius; r++) {
            double left = signal[center - r];
            double right = signal[center + r];
            totalError += Math.abs(left - right);
            count++;
        }

        // 如果没有足够的对称点，返回大值
        if (count == 0) {
            return Double.MAX_VALUE;
        }

        // 归一化得分
        return totalError / count;
    }

    /**
     * 基于对称中心的对齐方法
     */
    private static double[] centerAlignBySymmetry(double[] reference, double[] target, int refCenter, int targetCenter) {
        int length = Math.min(reference.length, target.length);
        double[] aligned = new double[length];

        // 计算中心点偏移
        int centerOffset = targetCenter - refCenter;

        // 方法1：直接平移对齐
        if (Math.abs(centerOffset) < length / 4) { // 偏移不大时使用平移
            aligned = shiftSignal(target, -centerOffset);
        }
        // 方法2：使用对称性特征重新构建
        else {
            aligned = reconstructBySymmetry(reference, target, refCenter);
        }

        return aligned;
    }

    /**
     * 基于对称性的信号重建
     */
    private static double[] reconstructBySymmetry(double[] reference, double[] target, int refCenter) {
        int length = Math.min(reference.length, target.length);
        double[] result = new double[length];

        // 找到目标信号的对称中心
        int targetCenter = findSymmetryCenter(target);

        // 计算缩放因子（基于信号能量）
        double refEnergy = calculateEnergy(reference, refCenter);
        double targetEnergy = calculateEnergy(target, targetCenter);
        double scale = Math.sqrt(refEnergy / targetEnergy);

        // 基于对称性重建信号
        int maxRadius = Math.min(refCenter, length - 1 - refCenter);
        maxRadius = Math.min(maxRadius, Math.min(targetCenter, length - 1 - targetCenter));

        // 对齐中心点
        result[refCenter] = target[targetCenter] * scale;

        // 对称复制
        for (int r = 1; r <= maxRadius; r++) {
            // 参考信号左侧
            if (refCenter - r >= 0) {
                // 使用目标信号的对称平均值
                double leftVal = target[targetCenter - r];
                double rightVal = target[targetCenter + r];
                double symmetricValue = (leftVal + rightVal) / 2 * scale;

                result[refCenter - r] = symmetricValue;
            }

            // 参考信号右侧
            if (refCenter + r < length) {
                double leftVal = target[targetCenter - r];
                double rightVal = target[targetCenter + r];
                double symmetricValue = (leftVal + rightVal) / 2 * scale;

                result[refCenter + r] = symmetricValue;
            }
        }

        return result;
    }

    /**
     * 计算信号能量（以中心点为基准）
     */
    private static double calculateEnergy(double[] signal, int center) {
        int radius = Math.min(center, signal.length - 1 - center);
        double energy = 0;

        for (int i = center - radius; i <= center + radius; i++) {
            if (i >= 0 && i < signal.length) {
                energy += signal[i] * signal[i];
            }
        }

        return energy;
    }

    /**
     * 平移信号（保持对称性）
     */
    public static double[] shiftSignal(double[] signal, int shift) {
        double[] shifted = new double[signal.length];

        if (shift == 0) {
            System.arraycopy(signal, 0, shifted, 0, signal.length);
            return shifted;
        }

        if (shift > 0) {
            // 向右平移
            for (int i = shift; i < signal.length; i++) {
                int index = i - shift;
                shifted[i] = signal[index];
            }
            // 左侧对称填充
            for (int i = 0; i < shift && i < signal.length; i++) {
                int index = 2 * shift - i - 1;
                index = Math.min(index, shifted.length- 1);
                index = Math.max(index, 0);
                shifted[i] = shifted[index];
            }
        } else {
            // 向左平移
            shift = -shift;
            for (int i = 0; i < signal.length - shift; i++) {
                int index = i + shift;
                index = Math.min(index, signal.length- 1);
                index = Math.max(index, 0);
                shifted[i] = signal[index];
            }
            // 右侧对称填充
            for (int i = signal.length - shift; i < signal.length; i++) {
                int index = 2 * (signal.length - shift) - i - 1;
                index = Math.min(index, shifted.length - 1);
                index = Math.max(index, 0);
                shifted[i] = shifted[index];
            }
        }

        return shifted;
    }

    /**
     * 增强信号对称性
     */
    public static double[] enhanceSymmetry(double[] signal, int center) {
        double[] symmetric = new double[signal.length];

        // 保留中心点
        symmetric[center] = signal[center];

        // 使左右对称
        int maxRadius = Math.min(center, signal.length - 1 - center);
        for (int r = 1; r <= maxRadius; r++) {
            double left = signal[center - r];
            double right = signal[center + r];
            double average = (left + right) / 2;

            symmetric[center - r] = average;
            symmetric[center + r] = average;
        }

        return symmetric;
    }
}

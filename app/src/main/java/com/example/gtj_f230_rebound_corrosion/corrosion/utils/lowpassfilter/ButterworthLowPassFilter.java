package com.example.gtj_f230_rebound_corrosion.corrosion.utils.lowpassfilter;

/**
 * 巴特沃斯滤波
 */
public class ButterworthLowPassFilter {
    private double[] input;
    private double[] output;
    private double a0, a1, a2, b1, b2;
    private double x1, x2, y1, y2;

    /**
     * 初始化巴特沃斯低通滤波器
     *
     * @param sampleRate 采样率 (Hz)
     * @param cutoffFreq 截止频率 (Hz)
     */
    public ButterworthLowPassFilter(double sampleRate, double cutoffFreq) {
        // 计算滤波器系数
        double omega = 2.0 * Math.PI * cutoffFreq / sampleRate;
        double cosOmega = Math.cos(omega);
        double sinOmega = Math.sin(omega);
        double alpha = sinOmega / (2.0 * 0.7071); // 0.7071 = 1/√2

        a0 = 1.0 / (1.0 + alpha);
        a1 = 2.0 * cosOmega * a0;
        a2 = (1.0 - alpha) * a0;
        b1 = 2.0 * cosOmega * a0;
        b2 = (1.0 - alpha) * a0;

        // 初始化历史值
        x1 = x2 = y1 = y2 = 0.0;
    }

    /**
     * 滤波单个值
     *
     * @param input 输入值
     * @return 滤波后的值
     */
    public double filter(double input) {
        double output = a0 * input + a1 * x1 + a2 * x2 - b1 * y1 - b2 * y2;

        // 更新历史值
        x2 = x1;
        x1 = input;
        y2 = y1;
        y1 = output;

        return output;
    }

    /**
     * 滤波整个数组
     *
     * @param data 输入数组
     * @return 滤波后的数组
     */
    public double[] filterArray(double[] data) {
        double[] result = new double[data.length];

        for (int i = 0; i < data.length; i++) {
            result[i] = filter(data[i]);
        }

        return result;
    }
}

package com.example.gtj_f230_rebound_corrosion.corrosion.utils.lowpassfilter;

public class FFTFilterUtils {

    /**
     * 频域低通滤波
     *
     * @param signal     原始信号
     * @param sampleRate 采样率
     * @param cutoffFreq 截止频率
     * @return 滤波后的信号
     */
    public static double[] frequencyDomainLowPass(double[] signal, double sampleRate, double cutoffFreq) {
        int n = signal.length;

        // 执行FFT
        Complex[] fft = FFT.fft(signal);

        // 计算频率分辨率
        double freqResolution = sampleRate / n;

        // 应用低通滤波器
        int cutoffBin = (int) (cutoffFreq / freqResolution);

        for (int i = cutoffBin; i < n - cutoffBin; i++) {
            fft[i] = new Complex(0, 0);  // 高频部分置零
            fft[n - i - 1] = new Complex(0, 0);
        }

        // 执行逆FFT
        return FFT.ifft(fft);
    }
}

// 简单的FFT实现
class FFT {

    public static Complex[] fft(double[] signal) {
        int n = signal.length;
        Complex[] x = new Complex[n];

        for (int i = 0; i < n; i++) {
            x[i] = new Complex(signal[i], 0);
        }

        return fft(x);
    }

    private static Complex[] fft(Complex[] x) {
        int n = x.length;

        if (n == 1) {
            return new Complex[]{x[0]};
        }

        // 确保n是2的幂
        if (n % 2 != 0) {
            throw new IllegalArgumentException("n不是2的幂");
        }

        // 偶数索引
        Complex[] even = new Complex[n / 2];
        for (int k = 0; k < n / 2; k++) {
            even[k] = x[2 * k];
        }
        Complex[] q = fft(even);

        // 奇数索引
        Complex[] odd = even;  // 重用数组
        for (int k = 0; k < n / 2; k++) {
            odd[k] = x[2 * k + 1];
        }
        Complex[] r = fft(odd);

        // 合并
        Complex[] y = new Complex[n];
        for (int k = 0; k < n / 2; k++) {
            double kth = -2 * k * Math.PI / n;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k] = q[k].plus(wk.times(r[k]));
            y[k + n / 2] = q[k].minus(wk.times(r[k]));
        }

        return y;
    }

    public static double[] ifft(Complex[] x) {
        int n = x.length;
        Complex[] y = new Complex[n];

        // 取共轭
        for (int i = 0; i < n; i++) {
            y[i] = x[i].conjugate();
        }

        // 正向FFT
        y = fft(y);

        // 取共轭并缩放
        double[] result = new double[n];
        for (int i = 0; i < n; i++) {
            result[i] = y[i].conjugate().re() / n;
        }

        return result;
    }
}

class Complex {
    private final double re;
    private final double im;

    public Complex(double real, double imag) {
        re = real;
        im = imag;
    }

    public double re() {
        return re;
    }

    public double im() {
        return im;
    }

    public Complex plus(Complex b) {
        return new Complex(re + b.re, im + b.im);
    }

    public Complex minus(Complex b) {
        return new Complex(re - b.re, im - b.im);
    }

    public Complex times(Complex b) {
        return new Complex(re * b.re - im * b.im, re * b.im + im * b.re);
    }

    public Complex conjugate() {
        return new Complex(re, -im);
    }
}

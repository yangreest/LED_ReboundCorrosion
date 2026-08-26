package com.example.gtj_f230_rebound_corrosion.corrosion.utils.lowpassfilter;

import com.github.psambit9791.jdsp.filter.Chebyshev;

/**
 * 切比雪夫滤波
 */
public class ChebyshevUtils {
    private int order;  //滤波器阶数
    private int samplingRate;  //采样频率 (Hz)
    private Chebyshev chebyshev;

    public ChebyshevUtils(int order, int samplingRate) {
        this.order = order;
        this.samplingRate = samplingRate;
        this.chebyshev = new Chebyshev(samplingRate, 10, 2);
    }

    public double[] lowPassFilter(double[] inputSignal, double cutoffFreq) {
        return chebyshev.lowPassFilter(inputSignal, order, cutoffFreq);
    }
}

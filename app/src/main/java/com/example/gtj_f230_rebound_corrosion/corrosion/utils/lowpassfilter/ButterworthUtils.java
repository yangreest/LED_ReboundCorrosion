package com.example.gtj_f230_rebound_corrosion.corrosion.utils.lowpassfilter;

import com.github.psambit9791.jdsp.filter.Butterworth;

/**
 * 巴特沃斯滤波
 */
public class ButterworthUtils {
    private int order;  //滤波器阶数
    private int samplingRate;  //采样频率 (Hz)
    private Butterworth butterworthFilter;

    public ButterworthUtils(int order, int samplingRate) {
        this.order = order;
        this.samplingRate = samplingRate;
        this.butterworthFilter = new Butterworth(samplingRate);
    }

    public double[] lowPassFilter(double[] inputSignal, double cutoffFreq) {
        return butterworthFilter.lowPassFilter(inputSignal, order, cutoffFreq);
    }
}

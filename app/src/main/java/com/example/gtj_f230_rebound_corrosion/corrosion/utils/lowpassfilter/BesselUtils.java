package com.example.gtj_f230_rebound_corrosion.corrosion.utils.lowpassfilter;

import com.github.psambit9791.jdsp.filter.Bessel;

/**
 * 贝塞尔滤波
 */
public class BesselUtils {
    private int order;  //滤波器阶数
    private Bessel bessel;

    public BesselUtils(int order, int samplingRate) {
        this.order = order;
        this.bessel = new Bessel(samplingRate);
    }

    public double[] lowPassFilter(double[] inputSignal, double cutoffFreq) {
        return bessel.lowPassFilter(inputSignal, order, cutoffFreq);
    }
}

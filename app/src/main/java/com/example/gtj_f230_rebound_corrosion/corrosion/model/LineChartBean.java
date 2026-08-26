package com.example.gtj_f230_rebound_corrosion.corrosion.model;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.gtj_f230_rebound_corrosion.corrosion.utils.lowpassfilter.LowPassFilter;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

public class LineChartBean {
    public String name;
    public String rebarDiameter;
    public int thickness;
    public double maxSignal;
    public List<Entry> entryList;

    public LineChartBean(String name, double maxSignal, int[] signalArray) {
        this.name = name;
        this.maxSignal = maxSignal;
        this.entryList = new ArrayList<>();
        //
        if (signalArray != null && signalArray.length > 0) {
            for (int i = 0; i < signalArray.length; i++) {
                entryList.add(new Entry((i + 1), signalArray[i]));
            }
        }
        Log.e("===", "===" + signalArray.length);
    }

    public LineChartBean(String rebarDiameter, int thickness, String name, double maxSignal, double[] signalArray) {
        this.rebarDiameter = rebarDiameter;
        this.thickness = thickness;
        this.name = name;
        this.maxSignal = maxSignal;
        this.entryList = new ArrayList<>();
        //
        double[] signal = LowPassFilter.firstOrderFilter(signalArray, 0.15);

//        double[] signal = signalArray;
        //
        if (signal != null && signal.length > 0) {
            for (int i = 0; i < signal.length; i++) {
                entryList.add(new Entry((i + 1), (float) signal[i]));
            }
        }
    }

    public LineChartBean(String name, double maxSignal, List<Double> signalArray) {
        this.name = name;
        this.maxSignal = maxSignal;
        this.entryList = new ArrayList<>();
        //
        if (signalArray != null && !signalArray.isEmpty()) {
            for (int i = 0; i < signalArray.size(); i++) {
                entryList.add(new Entry((i + 1), signalArray.get(i).floatValue()));
            }
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "{" + name + "}";
    }
}

package com.example.gtj_f230_rebound_corrosion.rebound.model;

import androidx.annotation.NonNull;

public class CarbonizationBean {
    public double c1;
    public double c2;
    public double c3;
    public double cAverage;

    public CarbonizationBean(double c1, double cAverage) {
        this.c1 = c1;
        this.c2 = -1;
        this.c3 = -1;
        this.cAverage = cAverage;
    }

    @NonNull
    @Override
    public String toString() {
        return "{" + cAverage + "}";
    }
}

package com.example.gtj_f230_rebound_corrosion.corrosion.model;

import androidx.annotation.NonNull;

public class NumberBean {
    public int numberMin;
    public int numberMax;
    public int number;
    public double thickness;
    public double distance;  //最小间距

    public NumberBean(int number) {
        this.number = number;
    }

    public NumberBean(int number, double distance) {
        this.number = number;
        this.distance = distance;
    }

    public NumberBean(int numberMin, int numberMax) {
        this.numberMin = numberMin;
        this.numberMax = numberMax;
    }

    @NonNull
    @Override
    public String toString() {
        return "{" + numberMin + ", " + numberMax + "}";
    }
}

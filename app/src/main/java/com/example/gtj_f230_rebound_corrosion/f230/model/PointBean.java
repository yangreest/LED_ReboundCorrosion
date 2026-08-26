package com.example.gtj_f230_rebound_corrosion.f230.model;

import androidx.annotation.NonNull;

public class PointBean {
    public double x;
    public double y;

    public PointBean(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @NonNull
    @Override
    public String toString() {
        return "{" + x + "," + y + "}";
    }
}

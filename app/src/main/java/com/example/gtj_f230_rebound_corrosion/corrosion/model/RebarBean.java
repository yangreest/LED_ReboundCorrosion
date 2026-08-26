package com.example.gtj_f230_rebound_corrosion.corrosion.model;

import java.io.Serializable;

public class RebarBean implements Serializable {
    public int distance;  //位移，mm
    public int signalLeft;  //左信号
    public int signalRight;  //右信号
    public double thickness;  //保护层厚度，mm
    public double thickness1;  //保护层厚度，mm
    public String segment;  //部位，根部、梢部、距根部

    public RebarBean(String segment, double thickness) {
        this.segment = segment;
        this.thickness = thickness;
    }

    public RebarBean(int distance, int signalLeft, int signalRight, double thickness, String segment) {
        this.distance = distance;
        this.signalLeft = signalLeft;
        this.signalRight = signalRight;
        this.thickness = thickness;
        this.segment = segment;
    }
}

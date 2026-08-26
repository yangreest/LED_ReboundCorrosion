package com.example.gtj_f230_rebound_corrosion.corrosion.model;

public class MinMaxBean {
    public int thickness;
    public double min70;
    public double max70;
    public double min120;
    public double max120;

    public MinMaxBean(int thickness, double min70, double max70, double min120, double max120) {
        this.thickness = thickness;
        this.min70 = min70;
        this.max70 = max70;
        this.min120 = min120;
        this.max120 = max120;
    }
}

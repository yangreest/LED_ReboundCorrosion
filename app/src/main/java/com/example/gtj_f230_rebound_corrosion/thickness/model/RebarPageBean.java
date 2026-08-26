package com.example.gtj_f230_rebound_corrosion.thickness.model;

import androidx.annotation.NonNull;

import com.example.gtj_f230_rebound_corrosion.corrosion.model.RebarBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RebarPageBean implements Serializable, Comparable<RebarPageBean> {
    public String segment;
    public double distance;
    public double averageThickness;
    public double averageThickness1;
    public List<RebarBean> beanList;

    public RebarPageBean(String segment, double distance) {
        this.segment = segment;
        this.distance = distance;
        beanList = new ArrayList<>();
    }

    @Override
    public int compareTo(RebarPageBean o) {
        return (int) (this.distance * 1000 - o.distance * 1000);
    }

    @NonNull
    @Override
    public String toString() {
        return "{" + distance + ", " + segment + ", " + averageThickness + "}";
    }
}

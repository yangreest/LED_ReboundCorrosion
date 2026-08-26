package com.example.gtj_f230_rebound_corrosion.f230.model;

import androidx.annotation.NonNull;

public class ComputeWidthBean implements Comparable<ComputeWidthBean> {
    public PointBean pointBean;  //中点
    public int width;  //图像裂缝宽（像素点）

    public ComputeWidthBean(int maxYCount, PointBean pointBean) {
        this.width = maxYCount;
        this.pointBean = pointBean;
    }

    @NonNull
    @Override
    public String toString() {
        return "{" + width + "," + pointBean + "}";
    }

    @Override
    public int compareTo(ComputeWidthBean bean) {
        return this.width - bean.width;
    }
}

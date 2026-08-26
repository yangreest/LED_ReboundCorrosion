package com.example.gtj_f230_rebound_corrosion.f230.model;

import androidx.annotation.NonNull;

public class SlitWidthBean {
    public PointBean p1;  //上中点
    public PointBean p2;  //下中点
    //图像照片
    public int start;  //图像宽度起始x位置（像素点）
    public int end;  //图像宽度结束x位置（像素点）
    public int width;  //图像裂缝宽（像素点）

    @NonNull
    @Override
    public String toString() {
        return "{" + p1 + "," + p2 + "}";
    }
}

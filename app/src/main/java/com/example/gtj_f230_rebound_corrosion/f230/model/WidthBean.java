package com.example.gtj_f230_rebound_corrosion.f230.model;

import java.io.Serializable;

public class WidthBean implements Serializable {
    //检测数据
    public int id;  //测点
    public String time;  //时间
    public double width;  //缝宽
    public float widthLeftX;  //宽度左侧像素位置
    public float widthRightX;  //宽度右侧像素位置
    public float widthLeftY;  //左侧高度
    public float widthRightY;  //右侧高度
    public String imagePath;  //缝宽图片
    public float widthAngle;  //裂缝角度
    //原始数据
    public double widthRaw;  //缝宽
    public float widthLeftXRaw;  //宽度左侧像素位置
    public float widthRightXRaw;  //宽度右侧像素位置
    public float widthLeftYRaw;  //左侧高度
    public float widthRightYRaw;  //右侧高度

    public boolean isSelected;
}

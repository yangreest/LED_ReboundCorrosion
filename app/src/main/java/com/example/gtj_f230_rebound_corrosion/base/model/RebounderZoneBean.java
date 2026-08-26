package com.example.gtj_f230_rebound_corrosion.base.model;

import java.io.Serializable;

public class RebounderZoneBean implements Serializable {
    public double detectionStandardCarbonization;//标准碳化深度值
    public double b;
    public int pouringSurface;//浇筑面 0：浇筑侧面；1：浇筑底面
    public int detectionSurface;  //检测面 1：表面；2：底面；3：侧面
    public int detectionAngle;  //检测角度 1：水平；2：30°
    public int isPumping; //是否泵送 0：否；1：是；
    public int detectionStandard; //检测依据 1：国家标准；2：上海
    //
    public double min;  //测区最小换算值
    public double average;  //测区平均换算值
    public double stdDev;  //标准差
    public double presumption;  //推定值
    public String strMin;
    public String strAverage;
    public String strStdDev;
    public String strPresumption;
    //测点json（测区集合）
    public String strZoneList;
    public int zoneCount;  //测区数量
    public String startTime;  //开始时间
}

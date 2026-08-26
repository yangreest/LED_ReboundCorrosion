package com.example.gtj_f230_rebound_corrosion.base.model;


import java.io.Serializable;

public class CorrosionZoneBean implements Serializable {
    public int poleType;  //电杆类型
    public int poleStandard;  //规范

    //试验参数
    public String poleHeight;  //电杆长度，m
    public String poleHeightReinforce;  //纵筋配筋长度，m
    public String poleBottomDiameter;  //电杆根径，mm
    public String poleTopDiameter;  //电杆梢径，mm
    public String rebarDiameter;  //设计纵筋直径
    public int hoopingDiameter;  //螺旋筋直径，mm

    public String Distance; // 距离根部

    //测点json（测区集合）
    public String strZoneList;  //锈蚀数据
    public int zoneCount;

    //其他
    public String startTime;  //开始时间
}

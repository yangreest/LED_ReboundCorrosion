package com.example.gtj_f230_rebound_corrosion.rebound.model;

import androidx.annotation.NonNull;

import com.example.gtj_f230_rebound_corrosion.base.utils.StringUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.rectify.RebounderRectifyUtils;

import java.io.Serializable;
import java.util.List;

public class RebounderBean implements Serializable {
    public List<Integer> testNumber;  //测区测点集合
    public List<Integer> outliersNumber;  //测区异常测点集合
    public double average;  //测区回弹平均值
    public double carbonize;  //测区强度值
    public String strCar;  //测区强度值（页面显示使用，比如最小强度值为10MPa，carbonize的值为9，则strCar的值为<10）
    public double calSurface;  //面修正值
    public double calAngle;  //角度修正值
    //
    public int pouringSurface;//0：浇筑侧面；1：浇筑底面
    public int surface;  //检测面ID（1："表面", 2："底面", 3："侧面"）
    public int angle;  //检测角度ID 标准：（1~9："0", "30", "45", "60", "90", "-30", "-45", "-60", "-90"）；贵州：（1~9："0", "15", "30", "45", "60", "75", "90", "-15", "-30", "-45", "-60", "-75", "-90"）
    public double carbonization;  //碳化值
    public double b;  //修正b值
    public int isPumping;  //是否泵送 0：否；1：是；
    public int standardId; //检测标准依据（1~28："国家统一新版", "国家高强度4.5J", "国家高强度5.5J", "北京曲线", "河北曲线", "河南商品混凝土曲线","山东曲线", "山东高强度曲线", "青岛曲线", "青岛高强度曲线", "辽宁曲线", "安徽低强度", "安徽高强度", "江苏泵送", "浙江卵石", "浙江碎石", "上海结构混凝土曲线","福建卵石", "福建碎石", "福建泵送", "陕西泵送曲线", "宁夏泵送", "甘肃庆阳预拌混凝土曲线", "四川高强度", "贵州曲线", "水运工程2.207J", "铁路工程曲线"）
    public boolean flagCarbonization;//false:支持;true:不支持

    public boolean isSelected;//测点列表是否选中

    public RebounderBean(int pouringSurface, int surface, int angle, int isPumping, double carbonization, double b,
                         int standardId, List<Integer> testNumber, List<Integer> outliersList) {
        this.pouringSurface = pouringSurface;
        this.surface = surface;
        this.angle = angle;
        this.isPumping = isPumping;
        this.carbonization = carbonization;
        this.b = b;
        this.testNumber = testNumber;
        this.standardId = standardId;
        this.outliersNumber = outliersList;
        calculate();
    }

    @NonNull
    @Override
    public String toString() {
        return "{" + carbonization + "," + average + "}";
    }

    public void calculate() {
        //混凝土强度碳化计算
        RebounderBean bean = RebounderRectifyUtils.computeUtils(this);
        this.average = StringUtils.getRounding(bean.average, 1);
        bean.carbonize = bean.carbonize + bean.b;
        this.carbonize = StringUtils.getRounding(bean.carbonize, 1);
        this.strCar = StringUtils.getStringCar(bean);
    }
}

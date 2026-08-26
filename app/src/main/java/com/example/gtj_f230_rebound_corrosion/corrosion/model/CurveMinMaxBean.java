package com.example.gtj_f230_rebound_corrosion.corrosion.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Unique;

@Entity
public class CurveMinMaxBean {
    @Id
    public Long id;  //（序号id）
    @Unique
    public String uniqueKey;
    //
    public String rebarDiameter;  //钢筋直径
    public double thickness;  //保护层厚度
    public double signalMin;
    public double signalMax;
    @Transient
    public boolean isSelected;

    public CurveMinMaxBean(String rebarDiameter, double thickness) {
        this.rebarDiameter = rebarDiameter;
        this.thickness = thickness;
        generateUniqueKey();
    }

    public CurveMinMaxBean(String rebarDiameter, String thickness, String signalMin, String signalMax) {
        this.rebarDiameter = rebarDiameter;
        this.thickness = Double.parseDouble(thickness.trim());
        this.signalMin = Double.parseDouble(signalMin.trim());
        this.signalMax = Double.parseDouble(signalMax.trim());
        generateUniqueKey();
    }

    @Generated(hash = 1488520120)
    public CurveMinMaxBean(Long id, String uniqueKey, String rebarDiameter, double thickness, double signalMin, double signalMax) {
        this.id = id;
        this.uniqueKey = uniqueKey;
        this.rebarDiameter = rebarDiameter;
        this.thickness = thickness;
        this.signalMin = signalMin;
        this.signalMax = signalMax;
    }

    @Generated(hash = 76592435)
    public CurveMinMaxBean() {
    }

    // 生成唯一键
    public void generateUniqueKey() {
        this.uniqueKey = rebarDiameter + "_" + thickness;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUniqueKey() {
        return this.uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public String getRebarDiameter() {
        return this.rebarDiameter;
    }

    public void setRebarDiameter(String rebarDiameter) {
        this.rebarDiameter = rebarDiameter;
    }

    public double getThickness() {
        return this.thickness;
    }

    public void setThickness(double thickness) {
        this.thickness = thickness;
    }

    public double getSignalMin() {
        return this.signalMin;
    }

    public void setSignalMin(double signalMin) {
        this.signalMin = signalMin;
    }

    public double getSignalMax() {
        return this.signalMax;
    }

    public void setSignalMax(double signalMax) {
        this.signalMax = signalMax;
    }
}

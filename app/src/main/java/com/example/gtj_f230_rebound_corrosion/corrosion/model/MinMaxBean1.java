package com.example.gtj_f230_rebound_corrosion.corrosion.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class MinMaxBean1 {
    @Id
    public Long id;  //（序号id）
    //
    public String rebarDiameter;
    public int thickness;
    public double min;
    public double max;

    public MinMaxBean1(String rebarDiameter, int thickness, double min, double max) {
        this.rebarDiameter = rebarDiameter;
        this.thickness = thickness;
        this.min = min;
        this.max = max;
    }

    @Generated(hash = 1303151737)
    public MinMaxBean1(Long id, String rebarDiameter, int thickness, double min, double max) {
        this.id = id;
        this.rebarDiameter = rebarDiameter;
        this.thickness = thickness;
        this.min = min;
        this.max = max;
    }

    @Generated(hash = 1996948827)
    public MinMaxBean1() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRebarDiameter() {
        return this.rebarDiameter;
    }

    public void setRebarDiameter(String rebarDiameter) {
        this.rebarDiameter = rebarDiameter;
    }

    public int getThickness() {
        return this.thickness;
    }

    public void setThickness(int thickness) {
        this.thickness = thickness;
    }

    public double getMin() {
        return this.min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return this.max;
    }

    public void setMax(double max) {
        this.max = max;
    }
}

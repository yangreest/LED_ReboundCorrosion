package com.example.gtj_f230_rebound_corrosion.corrosion.model;

import androidx.annotation.NonNull;

import com.example.gtj_f230_rebound_corrosion.corrosion.storage.CurveDoubleConverter;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Unique;

@Entity
public class StandardCurveBean {
    @Id
    public Long id;  //（序号id）
    @Unique
    public String uniqueKey;
    //
    public String rebarDiameter;  //钢筋直径
    public String poleDiameter;  //电杆直径
    public String rebarSpacing;  //主筋间距
    public String spiralSpacing;  //螺旋筋间距
    public double thickness;  //保护层厚度
    @Convert(converter = CurveDoubleConverter.class, columnType = String.class)
    public double[] signalArray;//信号波形
    @Convert(converter = CurveDoubleConverter.class, columnType = String.class)
    public double[] signalProcessed;//信号波形
    @Convert(converter = CurveDoubleConverter.class, columnType = String.class)
    public double[] signalMovAve;  //平滑信号  movingAverage
    @Transient
    public double changeRate;  //变化率
    @Transient
    public int compareValue;
    @Transient
    public boolean isSelected;

    public StandardCurveBean(String rebarDiameter, String poleDiameter, String rebarSpacing, String spiralSpacing, double thickness, double[] signalArray) {
        this.rebarDiameter = rebarDiameter;
        this.poleDiameter = poleDiameter;
        this.rebarSpacing = rebarSpacing;
        this.spiralSpacing = spiralSpacing;
        this.thickness = thickness;
        this.signalArray = signalArray;
        generateUniqueKey();
    }


    @Generated(hash = 1814938663)
    public StandardCurveBean() {
    }


    @Generated(hash = 1397532240)
    public StandardCurveBean(Long id, String uniqueKey, String rebarDiameter, String poleDiameter, String rebarSpacing, String spiralSpacing, double thickness, double[] signalArray, double[] signalProcessed, double[] signalMovAve) {
        this.id = id;
        this.uniqueKey = uniqueKey;
        this.rebarDiameter = rebarDiameter;
        this.poleDiameter = poleDiameter;
        this.rebarSpacing = rebarSpacing;
        this.spiralSpacing = spiralSpacing;
        this.thickness = thickness;
        this.signalArray = signalArray;
        this.signalProcessed = signalProcessed;
        this.signalMovAve = signalMovAve;
    }


    // 生成唯一键
    public void generateUniqueKey() {
        //                  钢筋直径                电杆直径              主筋间距              螺旋筋间距           保护层厚度
        this.uniqueKey = rebarDiameter + "_" + poleDiameter + "_" + rebarSpacing + "_" + spiralSpacing + "_" + thickness;
    }

    @NonNull
    @Override
    public String toString() {
        return "{" + id + ", " + uniqueKey + ", " + signalArray.length + "}";
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

    public String getPoleDiameter() {
        return this.poleDiameter;
    }

    public void setPoleDiameter(String poleDiameter) {
        this.poleDiameter = poleDiameter;
    }

    public double getThickness() {
        return this.thickness;
    }

    public void setThickness(double thickness) {
        this.thickness = thickness;
    }

    public double[] getSignalMovAve() {
        return this.signalMovAve;
    }

    public void setSignalMovAve(double[] signalMovAve) {
        this.signalMovAve = signalMovAve;
    }


    public double[] getSignalArray() {
        return this.signalArray;
    }


    public void setSignalArray(double[] signalArray) {
        this.signalArray = signalArray;
    }


    public double[] getSignalProcessed() {
        return this.signalProcessed;
    }


    public void setSignalProcessed(double[] signalProcessed) {
        this.signalProcessed = signalProcessed;
    }


    public String getRebarSpacing() {
        return this.rebarSpacing;
    }


    public void setRebarSpacing(String rebarSpacing) {
        this.rebarSpacing = rebarSpacing;
    }


    public String getSpiralSpacing() {
        return this.spiralSpacing;
    }


    public void setSpiralSpacing(String spiralSpacing) {
        this.spiralSpacing = spiralSpacing;
    }
}

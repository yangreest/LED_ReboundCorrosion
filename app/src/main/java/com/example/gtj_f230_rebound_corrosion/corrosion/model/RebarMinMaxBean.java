package com.example.gtj_f230_rebound_corrosion.corrosion.model;

import com.example.gtj_f230_rebound_corrosion.corrosion.storage.CurveDoubleConverter;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Unique;

@Entity
public class RebarMinMaxBean {
    @Id
    public Long id;  //（序号id）
    @Unique
    public String uniqueKey;
    //
    public String rebarDiameter;  //主筋直径 7，8，9，10，11，12，14，16
    public String poleDiameter;  //电杆直径 250，300，350，400，450
    public String rebarSpacingMin;  //主筋间距30，40，50，60
    public String rebarSpacingMax;  //主筋间距30，40，50，60
    public String spiralSpacing;  //螺旋筋间距70，120
    @Convert(converter = CurveDoubleConverter.class, columnType = String.class)
    public double[] signalMin;
    @Convert(converter = CurveDoubleConverter.class, columnType = String.class)
    public double[] signalMax;
    @Transient
    public double thickness;
    @Transient
    public double signal;

    public RebarMinMaxBean(String rebarDiameter, String poleDiameter, String rebarSpacingMin, String rebarSpacingMax, String spiralSpacing, double[] signalMin, double[] signalMax) {
        this.rebarDiameter = rebarDiameter;
        this.poleDiameter = poleDiameter;
        this.rebarSpacingMin = rebarSpacingMin;
        this.rebarSpacingMax = rebarSpacingMax;
        this.spiralSpacing = spiralSpacing;
        this.signalMin = signalMin;
        this.signalMax = signalMax;
        generateUniqueKey();
    }

    public RebarMinMaxBean(String rebarDiameter, String poleDiameter, String rebarSpacingMin, String rebarSpacingMax, String spiralSpacing) {
        this.rebarDiameter = rebarDiameter;
        this.poleDiameter = poleDiameter;
        this.rebarSpacingMin = rebarSpacingMin;
        this.rebarSpacingMax = rebarSpacingMax;
        this.spiralSpacing = spiralSpacing;
        generateUniqueKey();
    }

    @Generated(hash = 1844697159)
    public RebarMinMaxBean() {
    }

    @Generated(hash = 199420841)
    public RebarMinMaxBean(Long id, String uniqueKey, String rebarDiameter, String poleDiameter, String rebarSpacingMin, String rebarSpacingMax, String spiralSpacing,
                           double[] signalMin, double[] signalMax) {
        this.id = id;
        this.uniqueKey = uniqueKey;
        this.rebarDiameter = rebarDiameter;
        this.poleDiameter = poleDiameter;
        this.rebarSpacingMin = rebarSpacingMin;
        this.rebarSpacingMax = rebarSpacingMax;
        this.spiralSpacing = spiralSpacing;
        this.signalMin = signalMin;
        this.signalMax = signalMax;
    }

    // 生成唯一键
    public void generateUniqueKey() {
        //                  钢筋直径                电杆直径              主筋间距                  主筋间距             螺旋筋间距70，120
        this.uniqueKey = rebarDiameter + "_" + poleDiameter + "_" + rebarSpacingMin + "_" + rebarSpacingMax + "_" + spiralSpacing;
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

    public String getSpiralSpacing() {
        return this.spiralSpacing;
    }

    public void setSpiralSpacing(String spiralSpacing) {
        this.spiralSpacing = spiralSpacing;
    }

    public double[] getSignalMin() {
        return this.signalMin;
    }

    public void setSignalMin(double[] signalMin) {
        this.signalMin = signalMin;
    }

    public double[] getSignalMax() {
        return this.signalMax;
    }

    public void setSignalMax(double[] signalMax) {
        this.signalMax = signalMax;
    }

    public String getRebarSpacingMin() {
        return this.rebarSpacingMin;
    }

    public void setRebarSpacingMin(String rebarSpacingMin) {
        this.rebarSpacingMin = rebarSpacingMin;
    }

    public String getRebarSpacingMax() {
        return this.rebarSpacingMax;
    }

    public void setRebarSpacingMax(String rebarSpacingMax) {
        this.rebarSpacingMax = rebarSpacingMax;
    }
}

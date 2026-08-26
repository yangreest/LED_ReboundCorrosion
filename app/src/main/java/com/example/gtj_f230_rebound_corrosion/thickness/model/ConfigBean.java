package com.example.gtj_f230_rebound_corrosion.thickness.model;

import androidx.annotation.NonNull;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Unique;

import java.io.Serializable;

@Entity
public class ConfigBean implements Serializable {
    private static final long serialVersionUID = 1L;
    @Unique
    @Id(autoincrement = true)
    public Long id;

    public String poleName; //电杆名称

    public String poleHeight;  //电杆长度，m
    public int poleBottomDiameter;  //电杆根径，mm
    public int poleTopDiameter;  //电杆梢径，mm
    public int rebarDiameter;  //纵筋直径，index
    public int hoopingDiameter;  //螺旋筋直径，mm

    public String poleType;    //电杆类型
    public int poleStructure;  //电杆结构
    public String designThickness;  //设计保护层厚度
    @Transient
    public boolean isSelected;
    @Transient
    public boolean isDeleteSelected;   //删除选中
    public ConfigBean(String poleType, boolean isSelected, boolean isDeleteSelected) {
        this.poleType = poleType;
        this.isSelected = isSelected;
        this.isDeleteSelected = isDeleteSelected;
    }
    @Generated(hash = 1103764345)
    public ConfigBean(Long id, String poleName, String poleHeight, int poleBottomDiameter, int poleTopDiameter,
            int rebarDiameter, int hoopingDiameter, String poleType, int poleStructure, String designThickness) {
        this.id = id;
        this.poleName = poleName;
        this.poleHeight = poleHeight;
        this.poleBottomDiameter = poleBottomDiameter;
        this.poleTopDiameter = poleTopDiameter;
        this.rebarDiameter = rebarDiameter;
        this.hoopingDiameter = hoopingDiameter;
        this.poleType = poleType;
        this.poleStructure = poleStructure;
        this.designThickness = designThickness;
    }
    @Generated(hash = 1548494737)
    public ConfigBean() {
    }
    @NonNull
    @Override
    public String toString() {
        return "{" + id + ", " + poleName + ", " + poleType + ", " + poleStructure + ", " + designThickness + "}";
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getPoleName() {
        return this.poleName;
    }
    public void setPoleName(String poleName) {
        this.poleName = poleName;
    }
    public String getPoleHeight() {
        return this.poleHeight;
    }
    public void setPoleHeight(String poleHeight) {
        this.poleHeight = poleHeight;
    }
    public int getPoleBottomDiameter() {
        return this.poleBottomDiameter;
    }
    public void setPoleBottomDiameter(int poleBottomDiameter) {
        this.poleBottomDiameter = poleBottomDiameter;
    }
    public int getPoleTopDiameter() {
        return this.poleTopDiameter;
    }
    public void setPoleTopDiameter(int poleTopDiameter) {
        this.poleTopDiameter = poleTopDiameter;
    }
    public int getRebarDiameter() {
        return this.rebarDiameter;
    }
    public void setRebarDiameter(int rebarDiameter) {
        this.rebarDiameter = rebarDiameter;
    }
    public int getHoopingDiameter() {
        return this.hoopingDiameter;
    }
    public void setHoopingDiameter(int hoopingDiameter) {
        this.hoopingDiameter = hoopingDiameter;
    }
    public String getPoleType() {
        return this.poleType;
    }
    public void setPoleType(String poleType) {
        this.poleType = poleType;
    }
    public int getPoleStructure() {
        return this.poleStructure;
    }
    public void setPoleStructure(int poleStructure) {
        this.poleStructure = poleStructure;
    }
    public String getDesignThickness() {
        return this.designThickness;
    }
    public void setDesignThickness(String designThickness) {
        this.designThickness = designThickness;
    }
}

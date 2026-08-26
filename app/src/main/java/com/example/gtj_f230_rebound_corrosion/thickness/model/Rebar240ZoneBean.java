package com.example.gtj_f230_rebound_corrosion.thickness.model;

import com.example.gtj_f230_rebound_corrosion.base.storage.RebarBeanConverter;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

import java.io.Serializable;
import java.util.List;

@Entity
public class Rebar240ZoneBean implements Serializable {
    private static final long serialVersionUID = 1L;
    @Unique
    @Id(autoincrement = true)
    public Long id;  //00001（序号id）

    //工程信息
    public String poleNo;  //电杆编号
    public int poleType;  //电杆类型
    public int poleStructure;  //电杆结构
    public int poleStandard;  //规范

    //试验参数
    public String poleHeight;  //电杆长度，m
    public String poleHeightReinforce;  //纵筋配筋长度，m
    public int poleBottomDiameter;  //电杆根径，mm
    public int poleTopDiameter;  //电杆梢径，mm
    public int rebarDiameter;  //纵筋直径，index
    public int hoopingDiameter;  //螺旋筋直径，mm
    public int poleBottomCount;  //保护层厚度表格X轴数量
    public int poleTopCount;  //梢部纵筋数量
    public String designThickness;  //设计保护层厚度
    //
    @Convert(columnType = String.class, converter = RebarBeanConverter.class)
    public List<RebarPageBean> rebarPageList;  //钢筋数据

    public String startTime;  //开始时间

    @Generated(hash = 266650118)
    public Rebar240ZoneBean(Long id, String poleNo, int poleType, int poleStructure,
            int poleStandard, String poleHeight, String poleHeightReinforce,
            int poleBottomDiameter, int poleTopDiameter, int rebarDiameter,
            int hoopingDiameter, int poleBottomCount, int poleTopCount,
            String designThickness, List<RebarPageBean> rebarPageList,
            String startTime) {
        this.id = id;
        this.poleNo = poleNo;
        this.poleType = poleType;
        this.poleStructure = poleStructure;
        this.poleStandard = poleStandard;
        this.poleHeight = poleHeight;
        this.poleHeightReinforce = poleHeightReinforce;
        this.poleBottomDiameter = poleBottomDiameter;
        this.poleTopDiameter = poleTopDiameter;
        this.rebarDiameter = rebarDiameter;
        this.hoopingDiameter = hoopingDiameter;
        this.poleBottomCount = poleBottomCount;
        this.poleTopCount = poleTopCount;
        this.designThickness = designThickness;
        this.rebarPageList = rebarPageList;
        this.startTime = startTime;
    }

    @Generated(hash = 1518412594)
    public Rebar240ZoneBean() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPoleNo() {
        return this.poleNo;
    }

    public void setPoleNo(String poleNo) {
        this.poleNo = poleNo;
    }

    public int getPoleType() {
        return this.poleType;
    }

    public void setPoleType(int poleType) {
        this.poleType = poleType;
    }

    public int getPoleStructure() {
        return this.poleStructure;
    }

    public void setPoleStructure(int poleStructure) {
        this.poleStructure = poleStructure;
    }

    public int getPoleStandard() {
        return this.poleStandard;
    }

    public void setPoleStandard(int poleStandard) {
        this.poleStandard = poleStandard;
    }

    public String getPoleHeight() {
        return this.poleHeight;
    }

    public void setPoleHeight(String poleHeight) {
        this.poleHeight = poleHeight;
    }

    public String getPoleHeightReinforce() {
        return this.poleHeightReinforce;
    }

    public void setPoleHeightReinforce(String poleHeightReinforce) {
        this.poleHeightReinforce = poleHeightReinforce;
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

    public int getPoleBottomCount() {
        return this.poleBottomCount;
    }

    public void setPoleBottomCount(int poleBottomCount) {
        this.poleBottomCount = poleBottomCount;
    }

    public int getPoleTopCount() {
        return this.poleTopCount;
    }

    public void setPoleTopCount(int poleTopCount) {
        this.poleTopCount = poleTopCount;
    }

    public String getDesignThickness() {
        return this.designThickness;
    }

    public void setDesignThickness(String designThickness) {
        this.designThickness = designThickness;
    }

    public List<RebarPageBean> getRebarPageList() {
        return this.rebarPageList;
    }

    public void setRebarPageList(List<RebarPageBean> rebarPageList) {
        this.rebarPageList = rebarPageList;
    }

    public String getStartTime() {
        return this.startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

}

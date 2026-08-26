package com.example.gtj_f230_rebound_corrosion.base.model;

import com.example.gtj_f230_rebound_corrosion.base.storage.CorrosionZoneBeanConverter;
import com.example.gtj_f230_rebound_corrosion.base.storage.F230ZoneBeanConverter;
import com.example.gtj_f230_rebound_corrosion.base.storage.ReboundZoneBeanConverter;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.io.Serializable;

@Entity
public class ZoneBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id(autoincrement = true)
    public Long id;
    public long projectId;
    public String projectName;
    public String number;
    public String remark;

    //缝宽数据
    @Convert(columnType = String.class, converter = F230ZoneBeanConverter.class)
    public F230ZoneBean f230ZoneBean;
    //回弹数据
    @Convert(columnType = String.class, converter = ReboundZoneBeanConverter.class)
    public RebounderZoneBean rebounderZoneBean;
    //锈蚀数据
    @Convert(columnType = String.class, converter = CorrosionZoneBeanConverter.class)
    public CorrosionZoneBean corrosionZoneBean;
    //保护层厚度数据id
    public long thicknessId;
    public int thicknessCount;

    @Transient
    public boolean isSelected;
    @Transient
    public boolean isSelected_open;
    @Transient
    public boolean isSelected_treat;

    @Transient
    public boolean f230Export;
    @Transient
    public boolean reboundExport;

    @Generated(hash = 663159365)
    public ZoneBean(Long id, long projectId, String projectName, String number, String remark, F230ZoneBean f230ZoneBean, RebounderZoneBean rebounderZoneBean, CorrosionZoneBean corrosionZoneBean, long thicknessId,
            int thicknessCount) {
        this.id = id;
        this.projectId = projectId;
        this.projectName = projectName;
        this.number = number;
        this.remark = remark;
        this.f230ZoneBean = f230ZoneBean;
        this.rebounderZoneBean = rebounderZoneBean;
        this.corrosionZoneBean = corrosionZoneBean;
        this.thicknessId = thicknessId;
        this.thicknessCount = thicknessCount;
    }

    @Generated(hash = 988320533)
    public ZoneBean() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getProjectId() {
        return this.projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return this.projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getNumber() {
        return this.number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getRemark() {
        return this.remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public F230ZoneBean getF230ZoneBean() {
        return this.f230ZoneBean;
    }

    public void setF230ZoneBean(F230ZoneBean f230ZoneBean) {
        this.f230ZoneBean = f230ZoneBean;
    }

    public RebounderZoneBean getRebounderZoneBean() {
        return this.rebounderZoneBean;
    }

    public void setRebounderZoneBean(RebounderZoneBean rebounderZoneBean) {
        this.rebounderZoneBean = rebounderZoneBean;
    }

    public CorrosionZoneBean getCorrosionZoneBean() {
        return this.corrosionZoneBean;
    }

    public void setCorrosionZoneBean(CorrosionZoneBean corrosionZoneBean) {
        this.corrosionZoneBean = corrosionZoneBean;
    }

    public long getThicknessId() {
        return this.thicknessId;
    }

    public void setThicknessId(long thicknessId) {
        this.thicknessId = thicknessId;
    }

    public int getThicknessCount() {
        return this.thicknessCount;
    }

    public void setThicknessCount(int thicknessCount) {
        this.thicknessCount = thicknessCount;
    }


}

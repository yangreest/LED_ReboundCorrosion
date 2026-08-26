package com.example.gtj_f230_rebound_corrosion.f230.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class WidthZoneBean implements Serializable {
    private static final long serialVersionUID = 1L;
    @Unique
    @Id(autoincrement = true)
    public Long id;  //00001（序号id）
    public String num;  //序号(H00001)
    //测点json（测区集合）
    public String strZoneList;
    public int zoneListCount;
    //
    public String remark;  //备注
    public String address;  //检测位置
    public String inspector;  //检测人
    public String imageList;  //照片
    public String startTime;  //开始时间
    //
    public double lontitude;  //经度
    public double latitude;  //纬度
    public double altitude;  //海拔
    public int upload;  // 0：未上传；1：已上传
    @Generated(hash = 1063245155)
    public WidthZoneBean(Long id, String num, String strZoneList, int zoneListCount,
            String remark, String address, String inspector, String imageList,
            String startTime, double lontitude, double latitude, double altitude,
            int upload) {
        this.id = id;
        this.num = num;
        this.strZoneList = strZoneList;
        this.zoneListCount = zoneListCount;
        this.remark = remark;
        this.address = address;
        this.inspector = inspector;
        this.imageList = imageList;
        this.startTime = startTime;
        this.lontitude = lontitude;
        this.latitude = latitude;
        this.altitude = altitude;
        this.upload = upload;
    }
    @Generated(hash = 32020746)
    public WidthZoneBean() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getNum() {
        return this.num;
    }
    public void setNum(String num) {
        this.num = num;
    }
    public String getStrZoneList() {
        return this.strZoneList;
    }
    public void setStrZoneList(String strZoneList) {
        this.strZoneList = strZoneList;
    }
    public int getZoneListCount() {
        return this.zoneListCount;
    }
    public void setZoneListCount(int zoneListCount) {
        this.zoneListCount = zoneListCount;
    }
    public String getRemark() {
        return this.remark;
    }
    public void setRemark(String remark) {
        this.remark = remark;
    }
    public String getAddress() {
        return this.address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getInspector() {
        return this.inspector;
    }
    public void setInspector(String inspector) {
        this.inspector = inspector;
    }
    public String getImageList() {
        return this.imageList;
    }
    public void setImageList(String imageList) {
        this.imageList = imageList;
    }
    public String getStartTime() {
        return this.startTime;
    }
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    public double getLontitude() {
        return this.lontitude;
    }
    public void setLontitude(double lontitude) {
        this.lontitude = lontitude;
    }
    public double getLatitude() {
        return this.latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public double getAltitude() {
        return this.altitude;
    }
    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }
    public int getUpload() {
        return this.upload;
    }
    public void setUpload(int upload) {
        this.upload = upload;
    }
}

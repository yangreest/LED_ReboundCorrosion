package com.example.gtj_f230_rebound_corrosion.base.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class ProjectBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id(autoincrement = true)
    public Long id;
    public String name;
    public String address;

    @Transient
    public boolean isSelected;
    @Transient
    public boolean isSelected_open;
    @Transient
    public boolean isSelected_treat;

    public ProjectBean(String name, String strAddress) {
        this.name = name;
        this.address = strAddress;
    }

    @Generated(hash = 1101449973)
    public ProjectBean(Long id, String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }

    @Generated(hash = 882656566)
    public ProjectBean() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

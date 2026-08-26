package com.example.gtj_f230_rebound_corrosion.rebound.model;

public class ActivityItemBean {
    public String name;
    public String describe;
    public Class<?> activity;

    public ActivityItemBean(String name, String describe, Class<?> activity) {
        this.name = name;
        this.describe = describe;
        this.activity = activity;
    }
}

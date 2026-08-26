package com.example.gtj_f230_rebound_corrosion.rebound.model;

import java.util.ArrayList;
import java.util.List;

public class RebounderConfigBean {
    public String curve;  //检测依据 1：国家标准；2：上海
    public boolean isPumping;  //是否支持多泵送
    public List<RConfigBean> rConfigBeanList = new ArrayList<>();

    public RebounderConfigBean() {
        this.curve = "###";
        this.isPumping = true;
        this.rConfigBeanList.add(new RConfigBean(0));
        this.rConfigBeanList.add(new RConfigBean(1));
    }

    public RebounderConfigBean(String curve, boolean isPumping, List<RConfigBean> rConfigBeanList) {
        this.curve = curve;
        this.isPumping = isPumping;
        this.rConfigBeanList.addAll(rConfigBeanList);
    }

    public static class RConfigBean {
        public int pumping;  //是否泵送 0：否；1：是；
        public boolean isAngle;  //是否支持多角度
        public int angle;  //检测角度 1：水平；2：30°
        public boolean isSurface;  //是否支持多面
        public int surface;  //检测面 1：表面；2：底面；3：侧面
        public boolean isCar;  //是否支持碳化
        public double maxCar;  //支持最大碳化

        public RConfigBean(int pumping) {
            this.pumping = pumping;
            this.isSurface = true;
            this.surface = 3;
            this.isAngle = true;
            this.angle = 1;
            this.isCar = true;
            this.maxCar = 6;
        }

        public RConfigBean(int pumping, boolean isAngle, int angle, boolean isSurface, int surface, boolean isCar, double maxCar) {
            this.pumping = pumping;
            this.isSurface = isSurface;
            this.surface = surface;
            this.isAngle = isAngle;
            this.angle = angle;
            this.isCar = isCar;
            this.maxCar = maxCar;
        }
    }
}

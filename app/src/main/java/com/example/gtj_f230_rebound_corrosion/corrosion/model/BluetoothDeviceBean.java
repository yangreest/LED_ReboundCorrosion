package com.example.gtj_f230_rebound_corrosion.corrosion.model;

import java.io.Serializable;

public class BluetoothDeviceBean implements Serializable {
    private static final long serialVersionUID = 1L;

    public String mac;
    public String name;
    public boolean isSelected;

    public BluetoothDeviceBean(String mac, String name) {
        this.mac = mac;
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (obj instanceof BluetoothDeviceBean) {
            BluetoothDeviceBean bean = (BluetoothDeviceBean) obj;
            if (bean.mac.equals(this.mac))
                return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mac.hashCode();
    }
}

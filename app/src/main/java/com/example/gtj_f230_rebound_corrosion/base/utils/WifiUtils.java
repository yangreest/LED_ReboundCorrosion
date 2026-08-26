package com.example.gtj_f230_rebound_corrosion.base.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * WiFi工具类
 * 提供WiFi开关、连接、断开、获取信息等功能
 */
public class WifiUtils {
    private static final String TAG = "WifiUtils";

    private static volatile WifiUtils instance;
    private final WifiManager wifiManager;

    private WifiUtils(Context context) {
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public static WifiUtils getInstance(Context context) {
        if (instance == null) {
            synchronized (WifiUtils.class) {
                if (instance == null) {
                    instance = new WifiUtils(context);
                }
            }
        }
        return instance;
    }

    // ==================== WiFi开关相关 ====================

    /**
     * WiFi是否打开
     */
    public boolean isWifiEnable() {
        return wifiManager != null && wifiManager.isWifiEnabled();
    }

    /**
     * 打开WiFi
     */
    public void openWifi() {
        if (wifiManager != null && !isWifiEnable()) {
            wifiManager.setWifiEnabled(true);
        }
    }

    /**
     * 关闭WiFi
     */
    public void closeWifi() {
        if (wifiManager != null && isWifiEnable()) {
            wifiManager.setWifiEnabled(false);
        }
    }

    /**
     * 触发WiFi扫描
     * 注意：Android 10+ 需要位置权限且位置服务开启才能获取扫描结果
     *
     * @return 是否成功触发扫描
     */
    public boolean startScan() {
        if (wifiManager == null) {
            Log.e(TAG, "WifiManager is null");
            return false;
        }
        boolean success = wifiManager.startScan();
        Log.d(TAG, "startScan result: " + success);
        return success;
    }

    /**
     * 检查位置服务是否开启
     * Android 10+ 获取WiFi扫描结果必须开启位置服务
     *
     * @param context 上下文
     * @return 位置服务是否开启
     */
    public boolean isLocationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            LocationManager locationManager = (LocationManager) context.getApplicationContext()
                    .getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null) {
                return locationManager.isLocationEnabled();
            }
        }
        // Android 9以下不需要位置服务即可扫描WiFi
        return true;
    }

    /**
     * 获取位置服务设置的Intent
     *
     * @return 跳转位置设置的Intent
     */
    public android.content.Intent getLocationSettingsIntent() {
        return new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    }

    // ==================== WiFi连接相关 ====================

    /**
     * 断开当前WiFi连接
     */
    public void disconnectWifi() {
        if (wifiManager != null) {
            wifiManager.disconnect();
        }
    }

    /**
     * 切换到指定WiFi
     *
     * @param wifiName WiFi名称
     * @param wifiPwd  WiFi密码，如果已保存过可传null
     * @return 是否切换成功
     */
    public boolean changeToWifi(String wifiName, String wifiPwd) {
        if (wifiManager == null) {
            Log.e(TAG, "WifiManager is null");
            return false;
        }

        String quotedSsid = "\"" + wifiName + "\"";
        logCurrentWifiInfo();

        @SuppressLint("MissingPermission") List<WifiConfiguration> wifiList = wifiManager.getConfiguredNetworks();
        if (wifiList != null) {
            for (WifiConfiguration config : wifiList) {
                // 找到已配置的WiFi
                if (quotedSsid.equals(config.SSID) || wifiName.equals(config.SSID)) {
                    Log.i(TAG, "Found saved wifi: " + config.SSID);
                    return enableNetwork(config.networkId);
                }
            }
        }

        // WiFi未保存，添加新配置
        WifiConfiguration newConfig = createWpa2Config(wifiName, wifiPwd);
        int newNetworkId = wifiManager.addNetwork(newConfig);
        if (newNetworkId == -1) {
            Log.e(TAG, "Failed to add network, please unsave this WiFi in system settings");
            return false;
        }
        return enableNetwork(newNetworkId);
    }

    /**
     * 有密码连接WiFi
     */
    public void connectWifiWithPassword(String ssid, String password) {
        if (wifiManager == null) return;

        wifiManager.disconnect();
        wifiManager.disableNetwork(wifiManager.getConnectionInfo().getNetworkId());
        int netId = wifiManager.addNetwork(createWifiConfig(ssid, password, true));
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
    }

    /**
     * 无密码连接WiFi
     */
    public void connectWifiWithoutPassword(String ssid) {
        if (wifiManager == null) return;

        wifiManager.disableNetwork(wifiManager.getConnectionInfo().getNetworkId());
        int netId = wifiManager.addNetwork(createWifiConfig(ssid, "", false));
        wifiManager.enableNetwork(netId, true);
    }

    // ==================== WiFi信息获取 ====================

    /**
     * 获取当前连接的WiFi名称
     */
    public String getConnectWifiSsid() {
        if (wifiManager == null) return "";

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID();
        Log.d(TAG, "Current SSID: " + ssid);
        // 移除引号
        return ssid.replaceAll("\"", "");
    }

    /**
     * 获取WiFi扫描列表
     */
    @SuppressLint("MissingPermission")
    public List<ScanResult> getWifiList() {
        List<ScanResult> resultList = new ArrayList<>();
        if (wifiManager != null && isWifiEnable()) {
            resultList.addAll(wifiManager.getScanResults());
        }
        return resultList;
    }

    /**
     * 打印当前WiFi信息
     */
    public void logCurrentWifiInfo() {
        if (wifiManager == null) return;

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        Log.i(TAG, "Current SSID: " + wifiInfo.getSSID());
        Log.i(TAG, "Network ID: " + wifiInfo.getNetworkId());
    }

    // ==================== 私有方法 ====================

    /**
     * 启用指定网络
     */
    private boolean enableNetwork(int networkId) {
        // 确保WiFi已打开
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        boolean success = wifiManager.enableNetwork(networkId, true);
        if (success) {
            Log.i(TAG, "Switch to wifi success");
        } else {
            Log.e(TAG, "Switch to wifi failed");
        }
        return success;
    }

    /**
     * 创建WPA2加密的WiFi配置
     */
    private WifiConfiguration createWpa2Config(String ssid, String password) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();

        config.SSID = "\"" + ssid + "\"";
        config.preSharedKey = "\"" + password + "\"";
        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        config.status = WifiConfiguration.Status.ENABLED;

        return config;
    }

    /**
     * 创建WiFi配置（支持有密码和无密码）
     */
    private WifiConfiguration createWifiConfig(String ssid, String password, boolean hasPassword) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + ssid + "\"";

        // 移除已存在的配置
        WifiConfiguration existingConfig = findExistingConfig(ssid);
        if (existingConfig != null) {
            wifiManager.removeNetwork(existingConfig.networkId);
        }

        if (hasPassword) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        } else {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }

        return config;
    }

    /**
     * 查找已存在的WiFi配置
     */
    @SuppressLint("MissingPermission")
    private WifiConfiguration findExistingConfig(String ssid) {
        List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
        if (configs == null) return null;

        String quotedSsid = "\"" + ssid + "\"";
        for (WifiConfiguration config : configs) {
            if (quotedSsid.equals(config.SSID)) {
                return config;
            }
        }
        return null;
    }
}

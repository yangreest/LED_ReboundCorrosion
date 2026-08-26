package com.example.gtj_f230_rebound_corrosion.f230.activity.setting;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.base.activity.BaseActivity;
import com.example.gtj_f230_rebound_corrosion.base.utils.WifiUtils;
import com.example.gtj_f230_rebound_corrosion.base.view.StyleAlertDialog;
import com.example.gtj_f230_rebound_corrosion.databinding.ActWifiListBinding;
import com.example.gtj_f230_rebound_corrosion.f230.adapter.WifiListAdapter;
import com.example.gtj_f230_rebound_corrosion.f230.model.WifiBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class WifiListActivity extends BaseActivity<ActWifiListBinding> {
    private WifiListAdapter listAdapter;

    @Override
    protected ActWifiListBinding getBinding() {
        return ActWifiListBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initLabel() {
        initBatteryReceiver(binding.navigationBar);
        initDeviceStatusExecutor(binding.navigationBar);
        binding.navigationBar.setTitleText("缝宽探头连接");
    }

    @Override
    protected void initView() {
        setRecyclerView();
        initData();
        binding.btnReturn.setOnClickListener(v -> finish());
    }

    private void initData() {
        checkWifi();
    }


    @SuppressLint("NotifyDataSetChanged")
    private void setRecyclerView() {
        binding.mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        listAdapter = new WifiListAdapter();
        binding.mRecyclerView.setAdapter(listAdapter);
        listAdapter.setEmptyView(this);
        listAdapter.setOnItemClickListener((adapter, view, position) -> {
            boolean flag = WifiUtils.getInstance(this).changeToWifi(Objects.requireNonNull(listAdapter.getItem(position)).name, "12345678");
            if (flag) {
                for (int i = 0; i < listAdapter.getItems().size(); i++) {
                    listAdapter.getItems().get(i).isSelected = false;
                }
                SPUtils.getInstance().put(StaticConstant.SP_F230_WIFI_NAME, Objects.requireNonNull(listAdapter.getItem(position)).name);
                listAdapter.getItems().get(position).isSelected = true;
                adapter.notifyDataSetChanged();
                ToastUtils.showLong("连接成功");
            }
        });
    }

    private void setRecyclerViewData() {
        // 触发WiFi扫描（Android 10+必须）
        WifiUtils.getInstance(this).startScan();
        
        String connectWifiSsid = WifiUtils.getInstance(this).getConnectWifiSsid();
        List<ScanResult> scanResultList = WifiUtils.getInstance(this).getWifiList();
        List<WifiBean> wifiBeanList = new ArrayList<>();
        for (ScanResult result : scanResultList) {
            if (result.SSID != null && (result.SSID.contains("CHD") || result.SSID.contains("WIFI"))) {
                wifiBeanList.add(new WifiBean(result.SSID));
            }
        }
        for (int i = 0; i < wifiBeanList.size(); i++) {
            if (TextUtils.equals(connectWifiSsid, wifiBeanList.get(i).name)) {
                wifiBeanList.get(i).isSelected = true;
                break;
            }
        }
        listAdapter.submitList(wifiBeanList);
    }

    private void checkWifi() {
        boolean flag = WifiUtils.getInstance(this).isWifiEnable();
        if (!flag) {
            WifiUtils.getInstance(this).openWifi();
            initWifiTimer(3000);
        } else {
            // Android 10+ 需要位置服务开启才能获取WiFi扫描结果
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (!WifiUtils.getInstance(this).isLocationEnabled(this)) {
                    showLocationServiceDialog();
                    return;
                }
            }
            initWifiTimer(0);
        }
    }

    /**
     * 显示位置服务未开启的提示对话框
     */
    private void showLocationServiceDialog() {
        StyleAlertDialog dialog = new StyleAlertDialog(this);
        dialog.setContent("获取WiFi列表需要开启位置服务，请前往设置开启位置服务。")
                .setLeftButton("取消", v -> dialog.dismiss())
                .setRightButton("去设置", v -> {
                    dialog.dismiss();
                    try {
                        startActivity(WifiUtils.getInstance(this).getLocationSettingsIntent());
                    } catch (Exception e) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .show();
    }

    private Timer wifiTimer;
    private TimerTask wifiTimerTask;
    private Handler wifiHandler;
    private int count;

    private void initWifiTimer(int delayTime) {
        wifiHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (isShow) {
                    setRecyclerViewData();
                    count++;
                    if (count >= 3) {
                        listAdapter.setEmptyRefreshView(WifiListActivity.this);
                    }
                }
            }
        };
        wifiTimer = new Timer();
        wifiTimerTask = new TimerTask() {
            @Override
            public void run() {
                wifiHandler.sendEmptyMessage(1);
            }
        };
        wifiTimer.schedule(wifiTimerTask, delayTime, 1000 * 3);
    }

    private void clearWifiTimer() {
        if (wifiTimerTask != null) {
            wifiTimerTask.cancel();
            wifiTimerTask = null;
        }
        if (wifiTimer != null) {
            wifiTimer.cancel();
            wifiTimer = null;
        }
        wifiHandler = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearWifiTimer();
    }
}

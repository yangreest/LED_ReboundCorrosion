package com.example.gtj_f230_rebound_corrosion.base.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.base.utils.CustomDensityUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.OtherUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.WifiUtils;
import com.example.gtj_f230_rebound_corrosion.base.view.ProgressiveDialog;
import com.example.gtj_f230_rebound_corrosion.base.view.TitleBar;
import com.inuker.bluetooth.library.BluetoothManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public abstract class BaseActivity<T extends ViewBinding> extends AppCompatActivity {
    //电池电量Receiver
    private BroadcastReceiver mBatteryReceiver;
    //缝宽|回弹|锈蚀 连接状态
    private Disposable deviceStatusDisposable;

    protected ProgressiveDialog progressiveDialog;
    protected ExecutorService executorService;
    protected Handler uiHandler;
    private boolean isHideKeyboard;
    protected boolean isExist;
    protected boolean isShow;

    protected T binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //隐藏状态栏和导航栏（全屏沉浸式）- 使用 WindowCompat 在 setContentView 前设置
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // API 30+ 使用 WindowCompat 设置
            getWindow().setDecorFitsSystemWindows(false);
        } else {
            // 旧版本使用过时的 API（兼容性）
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            @SuppressWarnings("deprecation") WindowManager.LayoutParams params = getWindow().getAttributes();
            params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
            getWindow().setAttributes(params);
        }
        //屏幕适配
        CustomDensityUtils.setCustomDensity(this, getApplication());
        //
        progressiveDialog = new ProgressiveDialog(this);
        progressiveDialog.setTvContent("加载中...");
        executorService = Executors.newFixedThreadPool(1);
        uiHandler = new Handler(Looper.getMainLooper());
        isExist = true;
        //
        binding = getBinding();
        setContentView(binding.getRoot());
        // API 30+ 在 setContentView 后隐藏系统栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }
        setHideKeyboard();
        initLabel();
        initView();
    }

    protected abstract T getBinding();

    protected abstract void initLabel();

    protected abstract void initView();

    @Override
    protected void onStart() {
        super.onStart();
        this.isShow = true;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        this.isShow = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.isShow = false;
    }

    /**
     * 电池电量
     */
    protected void initBatteryReceiver(TitleBar navigationBar) {
        mBatteryReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                    int status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
                    if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                        navigationBar.setivBattery(true);
                    } else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
                        navigationBar.setivBattery(false);
                    }
                }
                //
                int level = intent.getIntExtra("level", 0);  //电池剩余电量
                int scale = intent.getIntExtra("scale", 0);  //获取电池满电量数值
                navigationBar.setBatteryLevel(level, scale);
            }
        };
        registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    /**
     * 缝宽|回弹|锈蚀
     */
    public void initDeviceStatusExecutor(TitleBar navigationBar) {
        deviceStatusDisposable = Observable.interval(2, TimeUnit.SECONDS).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(aLong -> {
            if (isShow) {
                String wifiName = SPUtils.getInstance().getString(StaticConstant.SP_F230_WIFI_NAME);
                if (!TextUtils.isEmpty(wifiName)) {
                    String wifiSsid = WifiUtils.getInstance(this).getConnectWifiSsid();
                    navigationBar.setIvF230WIFI(TextUtils.equals(wifiName, wifiSsid));
                } else {
                    navigationBar.setIvF230WIFI(false);
                }
                //回弹
                String spMac_REBOUND = SPUtils.getInstance().getString(StaticConstant.SP_REBOUND_BLE_MAC);
                if (!TextUtils.isEmpty(spMac_REBOUND)) {
                    boolean connectStatus = BluetoothManager.getConnectStatus(spMac_REBOUND);
                    if (!connectStatus) {
                        BluetoothManager.connectBluetoothDevice(spMac_REBOUND, null);
                    }
                    navigationBar.setIvReboundBluetooth(connectStatus);
                } else {
                    navigationBar.setIvReboundBluetooth(false);
                }
                //锈蚀
                String spMac_CORROSION = SPUtils.getInstance().getString(StaticConstant.SP_CORROSION_BLE_MAC);
                if (!TextUtils.isEmpty(spMac_CORROSION)) {
                    boolean connectStatus = BluetoothManager.getConnectStatus(spMac_CORROSION);
                    if (!connectStatus) {
                        BluetoothManager.connectBluetoothDevice(spMac_CORROSION, null);
                    }
                    navigationBar.setIvCorBluetooth(connectStatus);
                } else {
                    navigationBar.setIvCorBluetooth(false);
                }
            }
        });
    }

    /**
     * 隐藏软键盘
     */
    protected void setHideKeyboard() {
        this.isHideKeyboard = true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isHideKeyboard && ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (OtherUtils.isShouldHideInput(v, ev)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBatteryReceiver != null) {
            unregisterReceiver(mBatteryReceiver);
        }
        if (deviceStatusDisposable != null) {
            deviceStatusDisposable.dispose();
            deviceStatusDisposable = null;
        }
        if (!executorService.isShutdown()) {
            executorService.shutdownNow();
        }
        ToastUtils.cancel();
    }
}

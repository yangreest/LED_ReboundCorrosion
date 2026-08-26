package com.example.gtj_f230_rebound_corrosion;

import android.app.Application;
import android.graphics.Color;
import android.view.Gravity;

import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.example.gtj_f230_rebound_corrosion.base.utils.CrashHandler;
import com.example.gtj_f230_rebound_corrosion.base.utils.WifiUtils;
import com.inuker.bluetooth.library.BluetoothManager;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

public class GTJApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        BluetoothManager.init(this, StaticConstant.USR_SERVICE, StaticConstant.USR_CHARACTER_NOTIFY, StaticConstant.USR_CHARACTER_WRITE);
        WifiUtils.getInstance(this);
        CrashHandler.getInstance().init(this);
        Logger.addLogAdapter(new AndroidLogAdapter());
        Utils.init(this);
        ToastUtils.getDefaultMaker().setGravity(Gravity.CENTER, 0, 0);
        ToastUtils.getDefaultMaker().setBgColor(Color.parseColor("#000000"));
        ToastUtils.getDefaultMaker().setTextColor(Color.parseColor("#FFFFFF"));
        ToastUtils.getDefaultMaker().setTextSize(24);
    }
}

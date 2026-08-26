package com.example.gtj_f230_rebound_corrosion.base.utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.example.gtj_f230_rebound_corrosion.StaticConstant;

public class ShutUtils {

    public static BroadcastReceiver registShutActivity(final Activity activity, String action) {
        BroadcastReceiver shutReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                activity.finish();
            }
        };
        activity.registerReceiver(shutReceiver, new IntentFilter(action));
        return shutReceiver;
    }

    /**
     * 检测数据改变广播
     * @param context
     * @param changeType    1缝宽检测  2回弹检测  3锈蚀检测
     */
    public static void shutDataChangeActivity(Context context, int changeType) {
        Intent intent = new Intent();
        intent.setAction(StaticConstant.ACTION_DATA_CHANGE);
        intent.putExtra("type",changeType);
        context.sendBroadcast(intent);
    }
}

package com.example.gtj_f230_rebound_corrosion.f230.activity.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Parcelable;
import android.util.Log;

public class WifiBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null
                && WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            Log.e("======", "NETWORK_STATE_CHANGED_ACTION");
            Parcelable parcelable = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (parcelable != null) {
                NetworkInfo networkInfo = (NetworkInfo) parcelable;
                NetworkInfo.State state = networkInfo.getState();
                boolean isConnect = state == NetworkInfo.State.CONNECTED;
                Log.e("======", "isConnect=" + isConnect);
            }
        }
    }
}
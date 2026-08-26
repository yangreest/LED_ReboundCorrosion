package com.inuker.bluetooth.library;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.response.SearchResponse;

import java.util.UUID;


public class BluetoothManager6 {
    private static UUID USR_SERVICE;
    private static UUID USR_CHARACTER_NOTIFY;
    private static UUID USR_CHARACTER_WRITE;

    public interface StatusResult {
        void onResult(boolean flag);
    }


    public static void init(Context context, String USR_SERVICE, String USR_CHARACTER_NOTIFY, String USR_CHARACTER_WRITE) {
        BluetoothManager6.USR_SERVICE = UUID.fromString(USR_SERVICE);
        BluetoothManager6.USR_CHARACTER_NOTIFY = UUID.fromString(USR_CHARACTER_NOTIFY);
        BluetoothManager6.USR_CHARACTER_WRITE = UUID.fromString(USR_CHARACTER_WRITE);
        BluetoothContext.set(context);
    }

    private static volatile BluetoothClient mClient;

    public static BluetoothClient getClient() {
        if (mClient == null) {
            synchronized (BluetoothManager6.class) {
                if (mClient == null) {
                    mClient = new BluetoothClient(BluetoothContext.get());
                }
            }
        }
        return mClient;
    }

    public static void searchDeviceDevice(SearchResponse mSearchResponse) {
        SearchRequest request = new SearchRequest.Builder().searchBluetoothLeDevice(3000, 2).build();
        BluetoothManager6.getClient().search(request, mSearchResponse);
    }

    public static boolean getConnectStatus(String mac) {
        return 2 == BluetoothManager6.getClient().getConnectStatus(mac);
    }

    public static void connectBluetoothDevice(final String mac, final StatusResult statusResult) {
        BluetoothManager6.getClient().connect(mac, (code, data) -> {
            if (statusResult != null) statusResult.onResult(code == Constants.REQUEST_SUCCESS);
        });
    }

    //断开设备连接
    public static void unConnect(String mac) {
        if (TextUtils.isEmpty(mac)) {
            return;
        }
        //断开设备连接
        BluetoothManager6.getClient().disconnect(mac);
    }

    public static void writeBluetooth(String mac, byte[] bytes) {
        writeBluetoothDevice(mac, bytes, code -> {
        });
    }

    public static void notifyBluetoothDevice(String mac, BleNotifyResponse bleNotifyResponse) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> BluetoothManager6.getClient().notify(mac, USR_SERVICE, USR_CHARACTER_NOTIFY, bleNotifyResponse), 500);
    }

    public static void unNotifyBluetoothDevice(String mac) {
        BluetoothManager6.getClient().unnotify(mac, USR_SERVICE, USR_CHARACTER_NOTIFY, code -> {
        });
    }

    private static void writeBluetoothDevice(String mac, byte[] bytes, BleWriteResponse bleWriteResponse) {
        BluetoothManager6.getClient().write(mac, USR_SERVICE, USR_CHARACTER_WRITE, bytes, bleWriteResponse);
    }

    public static void registerConnectStatusListener(String mac, BleConnectStatusListener listener) {
        BluetoothManager6.getClient().registerConnectStatusListener(mac, listener);
    }

    public static void unregisterConnectStatusListener(String mac, BleConnectStatusListener listener) {
        BluetoothManager6.getClient().unregisterConnectStatusListener(mac, listener);
    }
}

package com.inuker.bluetooth.library.search.le;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;

import com.inuker.bluetooth.library.search.BluetoothSearcher;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.BluetoothSearchResponse;
import com.inuker.bluetooth.library.utils.BluetoothLog;
import com.inuker.bluetooth.library.utils.BluetoothUtils;

import java.util.List;

/**
 * @author dingjikerbo
 * 
 * 更新支持 Android 12+ (API 31+) 蓝牙扫描
 * - Android 12+ 使用 BluetoothLeScanner.startScan()
 * - Android 11 及以下使用已废弃的 BluetoothAdapter.startLeScan()
 */
public class BluetoothLESearcher extends BluetoothSearcher {

    private BluetoothLeScanner mBluetoothLeScanner;

	private BluetoothLESearcher() {
		mBluetoothAdapter = BluetoothUtils.getBluetoothAdapter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        }
	}

	public static BluetoothLESearcher getInstance() {
		return BluetoothLESearcherHolder.instance;
	}

	private static class BluetoothLESearcherHolder {
		private static BluetoothLESearcher instance = new BluetoothLESearcher();
	}

	@Override
	public void startScanBluetooth(BluetoothSearchResponse response) {
		super.startScanBluetooth(response);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ (API 31+): 使用新的 BluetoothLeScanner API
            startScanModern();
        } else {
            // Android 11 及以下: 使用已废弃的 startLeScan (向后兼容)
            startScanLegacy();
        }
	}

    @TargetApi(Build.VERSION_CODES.S)
    private void startScanModern() {
        if (mBluetoothLeScanner == null) {
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        }
        
        if (mBluetoothLeScanner == null) {
            BluetoothLog.e("BluetoothLeScanner is null, cannot start scan");
            return;
        }

        try {
            // 使用低延迟模式以获得更快的扫描结果
            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            
            mBluetoothLeScanner.startScan(null, settings, mScanCallback);
            BluetoothLog.v("Started BLE scan using BluetoothLeScanner (Android 12+)");
        } catch (SecurityException e) {
            BluetoothLog.e("SecurityException during BLE scan: missing BLUETOOTH_SCAN permission");
        } catch (Exception e) {
            BluetoothLog.e(e);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @SuppressWarnings("deprecation")
    private void startScanLegacy() {
        try {
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            BluetoothLog.v("Started BLE scan using legacy startLeScan");
        } catch (Exception e) {
            BluetoothLog.e(e);
        }
    }

	@Override
	public void stopScanBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            stopScanModern();
        } else {
            stopScanLegacy();
        }
		super.stopScanBluetooth();
	}
	
    @TargetApi(Build.VERSION_CODES.S)
    private void stopScanModern() {
        if (mBluetoothLeScanner != null) {
            try {
                mBluetoothLeScanner.stopScan(mScanCallback);
                BluetoothLog.v("Stopped BLE scan using BluetoothLeScanner");
            } catch (SecurityException e) {
                BluetoothLog.e("SecurityException during stop scan: missing BLUETOOTH_SCAN permission");
            } catch (Exception e) {
                BluetoothLog.e(e);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @SuppressWarnings("deprecation")
    private void stopScanLegacy() {
        try {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        } catch (Exception e) {
            BluetoothLog.e(e);
        }
    }

	@Override
	protected void cancelScanBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            stopScanModern();
        } else {
            stopScanLegacy();
        }
		super.cancelScanBluetooth();
	}

    // Android 12+ (API 21+) 的扫描回调
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            int rssi = result.getRssi();
            byte[] scanRecord = null;
            
            if (result.getScanRecord() != null) {
                scanRecord = result.getScanRecord().getBytes();
            }
            
            notifyDeviceFounded(new SearchResult(device, rssi, scanRecord));
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                BluetoothDevice device = result.getDevice();
                int rssi = result.getRssi();
                byte[] scanRecord = null;
                
                if (result.getScanRecord() != null) {
                    scanRecord = result.getScanRecord().getBytes();
                }
                
                notifyDeviceFounded(new SearchResult(device, rssi, scanRecord));
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            BluetoothLog.e("BLE scan failed with error code: " + errorCode);
        }
    };

    // Android 11 及以下的扫描回调 (已废弃，保留向后兼容)
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @SuppressWarnings("deprecation")
	private final LeScanCallback mLeScanCallback = new LeScanCallback() {
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            notifyDeviceFounded(new SearchResult(device, rssi, scanRecord));
		}
	};
}

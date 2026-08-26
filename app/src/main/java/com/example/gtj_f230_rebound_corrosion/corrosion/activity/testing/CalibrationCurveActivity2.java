package com.example.gtj_f230_rebound_corrosion.corrosion.activity.testing;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.blankj.utilcode.util.SPUtils;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.base.activity.BaseActivity;
import com.example.gtj_f230_rebound_corrosion.base.utils.CustomMediaPlayer;
import com.example.gtj_f230_rebound_corrosion.base.utils.StringUtils;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.OptimizedDataProcessor;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.lowpassfilter.Optimized10ElementTrimmedMeanUtils;
import com.example.gtj_f230_rebound_corrosion.databinding.ActCalibrationCurveBinding;
import com.inuker.bluetooth.library.BluetoothManager;
import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;

import java.util.UUID;

/**
 * 标定存储曲线（小车一直发送位移、信号数据）
 */
@SuppressLint("SetTextI18n")
public class CalibrationCurveActivity2 extends BaseActivity<ActCalibrationCurveBinding> {
    private MediaPlayer mediaPlayer;
    private String spMac;
    private OptimizedDataProcessor dataProcessor;
    private Handler dataProcessorHandler;
    private Optimized10ElementTrimmedMeanUtils meanUtils;  //平均信号

    @Override
    protected ActCalibrationCurveBinding getBinding() {
        return ActCalibrationCurveBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initLabel() {
        binding.navigationBar.setTitleText("标定曲线");
        initBatteryReceiver(binding.navigationBar);
        initDeviceStatusExecutor(binding.navigationBar);
    }

    @Override
    protected void initView() {
        spMac = SPUtils.getInstance().getString(StaticConstant.SP_CORROSION_BLE_MAC);
        binding.btnFinish.setOnClickListener(view -> finish());
        mediaPlayer = CustomMediaPlayer.getMediaPlayer(null);
        meanUtils = new Optimized10ElementTrimmedMeanUtils();
        initBluetooth();
        initOptimizedDataProcessor();
        binding.navigationBar.getTitleText().setOnClickListener(v -> {
            if (binding.scrollView.getVisibility() == View.VISIBLE) {
                binding.scrollView.setVisibility(View.GONE);
            } else {
                binding.scrollView.setVisibility(View.VISIBLE);
            }
        });
        binding.etRebarDiameter.setText(SPUtils.getInstance().getString("strCal-RebarDiameter"));
        binding.etPoleDiameter.setText(SPUtils.getInstance().getString("strCal-PoleDiameter"));
        binding.etThickness.setText(SPUtils.getInstance().getString("strCal-Thickness"));
        binding.btnClear.setOnClickListener(v -> {
            signalMin = 10000;
            signalMax = 0;
            binding.tvMessage1.setText("位移|信号：-- , --\nMin: " + signalMin + "\nMax: " + signalMax);
        });
    }

    private void initBluetooth() {
        BluetoothManager.registerConnectStatusListener(spMac, mBleConnectStatusListener);
        if (BluetoothManager.getConnectStatus(spMac)) {
            notifyBluetoothDevice();
        }
    }

    private final BleConnectStatusListener mBleConnectStatusListener = new BleConnectStatusListener() {
        @Override
        public void onConnectStatusChanged(String mac, int status) {
            if (status == Constants.STATUS_CONNECTED) {
                notifyBluetoothDevice();
            } else if (status == Constants.STATUS_DISCONNECTED) {
                BluetoothManager.unNotifyBluetoothDevice(spMac);
            }
        }
    };

    private void notifyBluetoothDevice() {
        BluetoothManager.notifyBluetoothDevice(spMac, new BleNotifyResponse() {
            @Override
            public void onNotify(UUID service, UUID character, byte[] value) {
                if (!isShow) {
                    return;
                }
                Log.d("====", "====onNotify1: " + StringUtils.byteArraytoHex(value));
                dataProcessor.receiveDataOptimized(value);
            }

            @Override
            public void onResponse(int code) {
            }
        });
    }

    private int signalMin = 10000, signalMax;

    private void initOptimizedDataProcessor() {
        dataProcessorHandler = new Handler(Looper.getMainLooper());
        dataProcessor = new OptimizedDataProcessor((displacement, signal, frameData) -> {
            Log.d("====", "====onNotify2: " + displacement + " , " + signal + " , " + frameData);
            //
            //平均信号
            double tempSignal = meanUtils.addValue(signal);
            //
            signalMin = (int) Math.min(tempSignal, signalMin);
            signalMax = (int) Math.max(tempSignal, signalMax);
            //
            dataProcessorHandler.post(() -> binding.tvMessage1.setText("位移|信号：" + displacement + " , " + signal + "\nMin: " + signalMin + "\nMax: " + signalMax));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow();
        CustomMediaPlayer.releasePlayer(mediaPlayer);
        BluetoothManager.unNotifyBluetoothDevice(spMac);
        BluetoothManager.unregisterConnectStatusListener(spMac, mBleConnectStatusListener);
    }
}

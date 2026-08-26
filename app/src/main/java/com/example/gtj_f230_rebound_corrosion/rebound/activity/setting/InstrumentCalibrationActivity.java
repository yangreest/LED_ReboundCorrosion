package com.example.gtj_f230_rebound_corrosion.rebound.activity.setting;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.base.activity.BaseActivity;
import com.example.gtj_f230_rebound_corrosion.base.utils.BeepSoundVibrateUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.BytesConversion;
import com.example.gtj_f230_rebound_corrosion.base.utils.StringUtils;
import com.example.gtj_f230_rebound_corrosion.databinding.ActInstrumentCalibrationBinding;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.inuker.bluetooth.library.BluetoothManager;
import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressLint("SetTextI18n")
public class InstrumentCalibrationActivity extends BaseActivity<ActInstrumentCalibrationBinding> {

    private BeepSoundVibrateUtils beepSoundVibrateUtils;
    private String spMac;

    @Override
    protected ActInstrumentCalibrationBinding getBinding() {
        return ActInstrumentCalibrationBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initLabel() {
        initBatteryReceiver(binding.navigationBar);
        initDeviceStatusExecutor(binding.navigationBar);
        
        binding.navigationBar.setTitleText("仪器率定");
    }

    @Override
    protected void initView() {
        binding.btnRestart.setOnClickListener(this::onViewClicked);
        binding.btnReturn.setOnClickListener(this::onViewClicked);
        initData();
    }

    public void onViewClicked(View view) {
        if (view.getId() == R.id.btn_restart) {
            tempList.clear();
            refreshTestingList();
        } else if (view.getId() == R.id.btn_return) {
            finish();
        }
    }

    private void initData() {
        spMac = SPUtils.getInstance().getString(StaticConstant.SP_REBOUND_BLE_MAC);
        beepSoundVibrateUtils = new BeepSoundVibrateUtils(this);
        connectDevice();
    }

    private void connectDevice() {
        if (isExist) {
            BluetoothManager.getClient().registerConnectStatusListener(spMac, mBleConnectStatusListener);
            if (BluetoothManager.getConnectStatus(spMac)) {
                notifyBluetoothDevice();
            } else {
                progressiveDialog.show();
                BluetoothManager.connectBluetoothDevice(spMac, flag -> {
                });
            }
        }
    }

    private final BleConnectStatusListener mBleConnectStatusListener = new BleConnectStatusListener() {
        @Override
        public void onConnectStatusChanged(String mac, int status) {
            progressiveDialog.dismiss();
            if (status == Constants.STATUS_CONNECTED) {
                Logger.d(mac + " =STATUS_CONNECTED");
                notifyBluetoothDevice();
            } else if (status == Constants.STATUS_DISCONNECTED) {
                Logger.d(mac + "=STATUS_DISCONNECTED");
                if (binding.navigationBar != null) {
                    binding.navigationBar.postDelayed(() -> connectDevice(), 1000);
                }
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
                String string = StringUtils.byteArraytoHex(value);
                notifyRefreshData(string);
            }

            @Override
            public void onResponse(int code) {
//                if (Constants.REQUEST_SUCCESS == code) {
//                }
            }
        });
    }

    private List<Integer> tempList = new ArrayList<>();

    private String[] tempArray = new String[0];

    private void notifyRefreshData(String string) {
        //FF 29 2A FE（old）
        //上传协议：ff X1 X2 X3 fe (X1：测区数量、X2：回弹值、X3=X1+X2)
        String[] array = string.split(" ");
        if (array.length != 5) {
            return;
        }
        beepSoundVibrateUtils.playBeepSoundAndVibrate();
        if (TextUtils.equals("FF", array[0])) {  //上传
            int number1 = BytesConversion.hex2decimal(array[1]);  //测区数量
            int number2 = BytesConversion.hex2decimal(array[2]);  //回弹值
//            int number3 = BytesConversion.hex2decimal(array[3]);  //校验和X3=X1+X2
            tempList.add(number2);
        }
        refreshTestingList();
        if (tempList.size() >= 12) {
            ToastUtils.showShort("测试完成");
        }
    }

    private void refreshTestingList() {
        double average, average1 = 0, average2 = 0, average3 = 0, average4;
        if (tempList.size() >= 1) {
            binding.tv11.setText(tempList.get(0) + "");
        } else {
            binding.tv11.setText("");
        }
        if (tempList.size() >= 2) {
            binding.tv12.setText(tempList.get(1) + "");
        } else {
            binding.tv12.setText("");
        }
        if (tempList.size() >= 3) {
            binding.tv13.setText(tempList.get(2) + "");
            average1 = (tempList.get(0) + tempList.get(1) + tempList.get(2)) / 3.0;
            binding.tvAverage1.setText(StringUtils.getRounding(average1, 1) + "");
        } else {
            binding.tv13.setText("");
            binding.tvAverage1.setText("");
        }
        if (tempList.size() >= 4) {
            binding.tv21.setText(tempList.get(3) + "");
        } else {
            binding.tv21.setText("");
        }
        if (tempList.size() >= 5) {
            binding.tv22.setText(tempList.get(4) + "");
        } else {
            binding.tv22.setText("");
        }
        if (tempList.size() >= 6) {
            binding.tv23.setText(tempList.get(5) + "");
            average2 = (tempList.get(3) + tempList.get(4) + tempList.get(5)) / 3.0;
            binding.tvAverage2.setText(StringUtils.getRounding(average2, 1) + "");
        } else {
            binding.tv23.setText("");
            binding.tvAverage2.setText("");
        }
        if (tempList.size() >= 7) {
            binding.tv31.setText(tempList.get(6) + "");
        } else {
            binding.tv31.setText("");
        }
        if (tempList.size() >= 8) {
            binding.tv32.setText(tempList.get(7) + "");
        } else {
            binding.tv32.setText("");
        }
        if (tempList.size() >= 9) {
            binding.tv33.setText(tempList.get(8) + "");
            average3 = (tempList.get(6) + tempList.get(7) + tempList.get(8)) / 3.0;
            binding.tvAverage3.setText(StringUtils.getRounding(average3, 1) + "");
        } else {
            binding.tv33.setText("");
            binding.tvAverage3.setText("");
        }
        if (tempList.size() >= 10) {
            binding.tv41.setText(tempList.get(9) + "");
        } else {
            binding.tv41.setText("");
        }
        if (tempList.size() >= 11) {
            binding.tv42.setText(tempList.get(10) + "");
        } else {
            binding.tv42.setText("");
        }
        if (tempList.size() >= 12) {
            binding.tv43.setText(tempList.get(11) + "");
            average4 = (tempList.get(9) + tempList.get(10) + tempList.get(11)) / 3.0;
            binding.tvAverage4.setText(StringUtils.getRounding(average4, 1) + "");
            average = StringUtils.getRounding((average1 + average2 + average3 + average4) / 4.0, 1);
            binding.tvResultNumber.setText(average + "");
            if (average >= 80 && average <= 99) {
                binding.tvResult.setTextColor(ContextCompat.getColor(this, R.color.color_Green));
                binding.tvResult.setText("合格");
            } else {
                binding.tvResult.setTextColor(ContextCompat.getColor(this, R.color.colorRed));
                binding.tvResult.setText("不合格");
            }
        } else {
            binding.tv43.setText("");
            binding.tvAverage4.setText("");
            binding.tvResultNumber.setText("");
            binding.tvResult.setText("");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beepSoundVibrateUtils.clear();
        BluetoothManager.unNotifyBluetoothDevice(spMac);
        BluetoothManager.getClient().unregisterConnectStatusListener(spMac, null);
    }
}

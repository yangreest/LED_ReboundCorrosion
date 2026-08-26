package com.example.gtj_f230_rebound_corrosion.corrosion.activity.setting;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.base.activity.BaseActivity;
import com.example.gtj_f230_rebound_corrosion.base.utils.CustomMediaPlayer;
import com.example.gtj_f230_rebound_corrosion.base.utils.OtherUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.StringUtils;
import com.example.gtj_f230_rebound_corrosion.base.view.StyleAlertDialog;
import com.example.gtj_f230_rebound_corrosion.corrosion.activity.testing.CalibrationCurveListActivity;
import com.example.gtj_f230_rebound_corrosion.corrosion.activity.testing.CalibrationMinMaxListActivity;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.CheckDataBean;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.OptimizedDataProcessor;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.SignalArrayBufferUtils;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.lowpassfilter.Optimized10ElementTrimmedMeanUtils;
import com.example.gtj_f230_rebound_corrosion.databinding.ActCalibrationDeviceBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.inuker.bluetooth.library.BluetoothManager;
import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressLint("SetTextI18n")
public class CalibrationDeviceActivity extends BaseActivity<ActCalibrationDeviceBinding> {
    private List<Integer> calibrationStandardList = new ArrayList<>();
    private List<Integer> calibrationList = new ArrayList<>();
    private String spMac;
    private MediaPlayer mediaPlayer;
    private ScheduledExecutorService scheduledExecutorService;
    private OptimizedDataProcessor dataProcessor;
    private Handler dataProcessorHandler;
    private Optimized10ElementTrimmedMeanUtils meanUtils;  //平均信号
    private SignalArrayBufferUtils signalArrayUtils;
    private final List<CheckDataBean> checkDataList = new ArrayList<>();

    @Override
    protected ActCalibrationDeviceBinding getBinding() {
        return ActCalibrationDeviceBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initLabel() {
        binding.navigationBar.setTitleText("钢筋校准");
        initBatteryReceiver(binding.navigationBar);
        initDeviceStatusExecutor(binding.navigationBar);
        setHideKeyboard();
    }

    @Override
    protected void initView() {
        binding.btnStandard.setVisibility(StaticConstant.isRelease ? View.GONE : View.VISIBLE);
        binding.btnOk.setOnClickListener(this::onViewClicked);
        binding.btnCancel.setOnClickListener(this::onViewClicked);
        binding.btnDefault1.setOnClickListener(this::onViewClicked);
        binding.btnDefault11.setOnClickListener(this::onViewClicked);
        binding.btnStandard.setOnClickListener(this::onViewClicked);
        initCalibration();
        spMac = SPUtils.getInstance().getString(StaticConstant.SP_CORROSION_BLE_MAC);
        signalArrayUtils = new SignalArrayBufferUtils();
        signalArrayUtils.setSize(64);
        meanUtils = new Optimized10ElementTrimmedMeanUtils();
        mediaPlayer = CustomMediaPlayer.getMediaPlayer(null);
        initBluetooth();
        initExecutor();
        initOptimizedDataProcessor();
    }

    private void initCalibration() {
        String strCalibrationStandardList = SPUtils.getInstance().getString("calibration-standard-list");
        if (!TextUtils.isEmpty(strCalibrationStandardList)) {
            calibrationStandardList = new Gson().fromJson(strCalibrationStandardList, new TypeToken<List<Integer>>() {
            }.getType());
            binding.et01.setText(calibrationStandardList.get(0) + "");
            binding.et03.setText(calibrationStandardList.get(1) + "");
            binding.et06.setText(calibrationStandardList.get(2) + "");
            binding.et10.setText(calibrationStandardList.get(3) + "");
            binding.et13.setText(calibrationStandardList.get(4) + "");
            binding.et16.setText(calibrationStandardList.get(5) + "");
            binding.et20.setText(calibrationStandardList.get(6) + "");
            binding.et23.setText(calibrationStandardList.get(7) + "");
            binding.et26.setText(calibrationStandardList.get(8) + "");
            binding.et30.setText(calibrationStandardList.get(9) + "");
            binding.et33.setText(calibrationStandardList.get(10) + "");
            binding.et36.setText(calibrationStandardList.get(11) + "");
            binding.et40.setText(calibrationStandardList.get(12) + "");
        }
        String strCalibrationList = SPUtils.getInstance().getString("calibration-list");
        if (!TextUtils.isEmpty(strCalibrationList)) {
            calibrationList = new Gson().fromJson(strCalibrationList, new TypeToken<List<Integer>>() {
            }.getType());
            binding.et011.setText(calibrationList.get(0) + "");
            binding.et031.setText(calibrationList.get(1) + "");
            binding.et061.setText(calibrationList.get(2) + "");
            binding.et101.setText(calibrationList.get(3) + "");
            binding.et131.setText(calibrationList.get(4) + "");
            binding.et161.setText(calibrationList.get(5) + "");
            binding.et201.setText(calibrationList.get(6) + "");
            binding.et231.setText(calibrationList.get(7) + "");
            binding.et261.setText(calibrationList.get(8) + "");
            binding.et301.setText(calibrationList.get(9) + "");
            binding.et331.setText(calibrationList.get(10) + "");
            binding.et361.setText(calibrationList.get(11) + "");
            binding.et401.setText(calibrationList.get(12) + "");
        }
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
                dataProcessor.receiveDataOptimized(value);
                binding.tvMessage1.setText(StringUtils.ByteArraytoHex(value).trim());
            }

            @Override
            public void onResponse(int code) {
            }
        });
        ToastUtils.showLong("连接成功，请检测");
    }

    private void initExecutor() {
        Handler handlerPost = new Handler();
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleWithFixedDelay(() -> handlerPost.post(() -> timerTime++), 0, 1, TimeUnit.SECONDS);
    }

    private long timerTime;
    private boolean isCollectStop;
    private int cur_displacement;

    private void initOptimizedDataProcessor() {
        dataProcessorHandler = new Handler(Looper.getMainLooper());
        dataProcessor = new OptimizedDataProcessor((displacement, signal, frameData) -> {
            //校准信号
            int signalValue = getSignalValue(signal);
            //
            //平均信号
            int tempSignal = (int) meanUtils.addValue(signalValue);

            if (isCollectStop) {
                return;
            }
            //
            if (cur_displacement == displacement) {
                return;
            }
            cur_displacement = displacement;
            //
            //超过2秒清空数据
            {
                if (timerTime >= 2) {
                    signalArrayUtils.clear();
                }
                timerTime = 0;
            }
            //
            //记录数据|获取最大值
            CheckDataBean maxBean = signalArrayUtils.putAndCheck(displacement, tempSignal);
            //
            if (maxBean == null) {
                return;
            }
            //停止检测
            isCollectStop = true;
            CustomMediaPlayer.startSpeaking(this, mediaPlayer, "prompt");
            //清空
            if (checkDataList.size() == 3) {
                checkDataList.clear();
            }
            //添加
            checkDataList.add(maxBean);
            //显示
            final StringBuilder sbd = new StringBuilder();
            if (checkDataList.size() == 3) {
                double sum = 0;
                sum += checkDataList.get(0).y;
                sum += checkDataList.get(1).y;
                sum += checkDataList.get(2).y;
                int average = (int) StringUtils.getRounding(sum / 3, 0);
                sbd.append(average);
            } else {
                ToastUtils.showShort("已检测" + checkDataList.size() + "次");
            }
            dataProcessorHandler.post(() -> {
                binding.tvMessage2.setText("" + signal);
                binding.tvDetectionValue.setText("标定取3次平均值\n" + getY(checkDataList) + "\n" + checkDataList.size() + "次平均值：" + sbd);
                isCollectStop = false;
            });
        });
    }

    private String getY(List<CheckDataBean> checkDataList) {
        StringBuilder sbd = new StringBuilder("[ ");
        for (CheckDataBean bean : checkDataList) {
            sbd.append((int) bean.y).append(" ");
        }
        sbd.append(" ] ");
        return sbd.toString();
    }

    private int getSignalValue(int value) {
        int offset = 0;
        for (int i = 0; i < calibrationList.size(); i++) {
            if (value >= calibrationList.get(i)) {
                if (i - 1 >= 0) {
                    int indexMax = calibrationList.get(i - 1) - calibrationList.get(i);
                    int indexMin = 0;
                    int max = calibrationList.get(i - 1) - calibrationStandardList.get(i - 1);
                    int min = calibrationList.get(i) - calibrationStandardList.get(i);
                    int indexCurrent = value - calibrationList.get(i);
                    offset = (int) StringUtils.getRounding(StringUtils.getInterpolationValue(indexMax, max, indexMin, min, indexCurrent), 0);
                }
                break;
            }
        }
        return value - offset;
    }

    public void onViewClicked(View view) {
        OtherUtils.hideKeyboard(this);
        if (view.getId() == R.id.btn_default_1) {
            StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this);
            styleAlertDialog.setContent("确定更改标定为 1 : -- ？").setLeftButton("取消", v -> styleAlertDialog.dismiss()).setRightButton("确定", v -> {
                styleAlertDialog.dismiss();
                setDefault1();
            }).show();
        } else if (view.getId() == R.id.btn_default_1_1) {
            StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this);
            styleAlertDialog.setContent("确定更改标定为 1 : 1 ？").setLeftButton("取消", v -> styleAlertDialog.dismiss()).setRightButton("确定", v -> {
                styleAlertDialog.dismiss();
                setDefault();
            }).show();
        } else if (view.getId() == R.id.btn_ok) {
            save(true);
        } else if (view.getId() == R.id.btn_cancel) {
            finish();
        } else if (view.getId() == R.id.btn_standard) {
            boolean isCorrosion = TextUtils.equals(StaticConstant.APP_TYPE_DEFAULT, StaticConstant.APP_CORROSION);
            if (isCorrosion) {
                startActivity(new Intent(this, CalibrationCurveListActivity.class));
            } else {
                startActivity(new Intent(this, CalibrationMinMaxListActivity.class));
            }
        }
    }

    private void setDefault() {
        binding.et01.setText("18235");
        binding.et03.setText("11972");
        binding.et06.setText("7444");
        binding.et10.setText("4044");
        binding.et13.setText("2686");
        binding.et16.setText("1823");
        binding.et20.setText("1173");
        binding.et23.setText("844");
        binding.et26.setText("622");
        binding.et30.setText("419");
        binding.et33.setText("325");
        binding.et36.setText("249");
        binding.et40.setText("181");
        //
        binding.et011.setText("18235");
        binding.et031.setText("11972");
        binding.et061.setText("7444");
        binding.et101.setText("4044");
        binding.et131.setText("2686");
        binding.et161.setText("1823");
        binding.et201.setText("1173");
        binding.et231.setText("844");
        binding.et261.setText("622");
        binding.et301.setText("419");
        binding.et331.setText("325");
        binding.et361.setText("249");
        binding.et401.setText("181");
        save(true);
    }

    private void setDefault1() {
        binding.et01.setText("18235");
        binding.et03.setText("11972");
        binding.et06.setText("7444");
        binding.et10.setText("4044");
        binding.et13.setText("2686");
        binding.et16.setText("1823");
        binding.et20.setText("1173");
        binding.et23.setText("844");
        binding.et26.setText("622");
        binding.et30.setText("419");
        binding.et33.setText("325");
        binding.et36.setText("249");
        binding.et40.setText("181");
        //
        binding.et011.setText("");
        binding.et031.setText("");
        binding.et061.setText("");
        binding.et101.setText("");
        binding.et131.setText("");
        binding.et161.setText("");
        binding.et201.setText("");
        binding.et231.setText("");
        binding.et261.setText("");
        binding.et301.setText("");
        binding.et331.setText("");
        binding.et361.setText("");
        binding.et401.setText("");
        save(true);
    }

    private void save(boolean isShow) {
        String str01 = binding.et01.getText().toString().trim();
        String str03 = binding.et03.getText().toString().trim();
        String str06 = binding.et06.getText().toString().trim();
        //
        String str10 = binding.et10.getText().toString().trim();
        String str13 = binding.et13.getText().toString().trim();
        String str16 = binding.et16.getText().toString().trim();
        //
        String str20 = binding.et20.getText().toString().trim();
        String str23 = binding.et23.getText().toString().trim();
        String str26 = binding.et26.getText().toString().trim();
        //
        String str30 = binding.et30.getText().toString().trim();
        String str33 = binding.et33.getText().toString().trim();
        String str36 = binding.et36.getText().toString().trim();
        String str40 = binding.et40.getText().toString().trim();
        if (TextUtils.isEmpty(str01) || TextUtils.isEmpty(str03) || TextUtils.isEmpty(str06) || TextUtils.isEmpty(str10) || TextUtils.isEmpty(str13) || TextUtils.isEmpty(str16) || TextUtils.isEmpty(str20) || TextUtils.isEmpty(str23) || TextUtils.isEmpty(str26) || TextUtils.isEmpty(str30) || TextUtils.isEmpty(str33) || TextUtils.isEmpty(str36) || TextUtils.isEmpty(str40)) {
            ToastUtils.showShort("请填写完整");
            return;
        }
        String str011 = binding.et011.getText().toString().trim();
        String str031 = binding.et031.getText().toString().trim();
        String str061 = binding.et061.getText().toString().trim();
        //
        String str101 = binding.et101.getText().toString().trim();
        String str131 = binding.et131.getText().toString().trim();
        String str161 = binding.et161.getText().toString().trim();
        //
        String str201 = binding.et201.getText().toString().trim();
        String str231 = binding.et231.getText().toString().trim();
        String str261 = binding.et261.getText().toString().trim();
        //
        String str301 = binding.et301.getText().toString().trim();
        String str331 = binding.et331.getText().toString().trim();
        String str361 = binding.et361.getText().toString().trim();
        String str401 = binding.et401.getText().toString().trim();
        if (TextUtils.isEmpty(str011) || TextUtils.isEmpty(str031) || TextUtils.isEmpty(str061) || TextUtils.isEmpty(str101) || TextUtils.isEmpty(str131) || TextUtils.isEmpty(str161) || TextUtils.isEmpty(str201) || TextUtils.isEmpty(str231) || TextUtils.isEmpty(str261) || TextUtils.isEmpty(str301) || TextUtils.isEmpty(str331) || TextUtils.isEmpty(str361) || TextUtils.isEmpty(str401)) {
            ToastUtils.showShort("请填写完整");
            return;
        }
        calibrationStandardList = new ArrayList<>();
        calibrationStandardList.add(Integer.parseInt(str01));
        calibrationStandardList.add(Integer.parseInt(str03));
        calibrationStandardList.add(Integer.parseInt(str06));
        calibrationStandardList.add(Integer.parseInt(str10));
        calibrationStandardList.add(Integer.parseInt(str13));
        calibrationStandardList.add(Integer.parseInt(str16));
        calibrationStandardList.add(Integer.parseInt(str20));
        calibrationStandardList.add(Integer.parseInt(str23));
        calibrationStandardList.add(Integer.parseInt(str26));
        calibrationStandardList.add(Integer.parseInt(str30));
        calibrationStandardList.add(Integer.parseInt(str33));
        calibrationStandardList.add(Integer.parseInt(str36));
        calibrationStandardList.add(Integer.parseInt(str40));
        SPUtils.getInstance().put("calibration-standard-list", new Gson().toJson(calibrationStandardList));
        //
        calibrationList = new ArrayList<>();
        calibrationList.add(Integer.parseInt(str011));
        calibrationList.add(Integer.parseInt(str031));
        calibrationList.add(Integer.parseInt(str061));
        calibrationList.add(Integer.parseInt(str101));
        calibrationList.add(Integer.parseInt(str131));
        calibrationList.add(Integer.parseInt(str161));
        calibrationList.add(Integer.parseInt(str201));
        calibrationList.add(Integer.parseInt(str231));
        calibrationList.add(Integer.parseInt(str261));
        calibrationList.add(Integer.parseInt(str301));
        calibrationList.add(Integer.parseInt(str331));
        calibrationList.add(Integer.parseInt(str361));
        calibrationList.add(Integer.parseInt(str401));
        SPUtils.getInstance().put("calibration-list", new Gson().toJson(calibrationList));
        if (isShow) {
            ToastUtils.showShort("保存成功");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scheduledExecutorService.shutdownNow();
        CustomMediaPlayer.releasePlayer(mediaPlayer);
        BluetoothManager.unNotifyBluetoothDevice(spMac);
        BluetoothManager.unregisterConnectStatusListener(spMac, mBleConnectStatusListener);
    }
}

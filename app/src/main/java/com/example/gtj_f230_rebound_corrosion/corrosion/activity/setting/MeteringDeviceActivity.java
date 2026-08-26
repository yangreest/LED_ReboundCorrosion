package com.example.gtj_f230_rebound_corrosion.corrosion.activity.setting;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_f230_rebound_corrosion.StaConMetering;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.base.activity.BaseActivity;
import com.example.gtj_f230_rebound_corrosion.base.adapter.MeteringDeviceAdapter;
import com.example.gtj_f230_rebound_corrosion.base.model.MeteringBean;
import com.example.gtj_f230_rebound_corrosion.base.utils.CustomMediaPlayer;
import com.example.gtj_f230_rebound_corrosion.base.utils.OtherUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.SpinnerUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.StringUtils;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.CheckDataBean;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.OptimizedDataProcessor;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.SignalArrayBufferUtils;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.lowpassfilter.Optimized10ElementTrimmedMeanUtils;
import com.example.gtj_f230_rebound_corrosion.databinding.ActMeteringDeviceBinding;
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
public class MeteringDeviceActivity extends BaseActivity<ActMeteringDeviceBinding> {
    private List<Integer> standardArray;
    private String strDiameter;
    private MeteringDeviceAdapter listAdapter;
    private String spMac;
    private ScheduledExecutorService scheduledExecutorService;
    private MediaPlayer mediaPlayer;
    private OptimizedDataProcessor dataProcessor;
    private Handler dataProcessorHandler;
    private Optimized10ElementTrimmedMeanUtils meanUtils;  //平均信号
    private SignalArrayBufferUtils signalArrayUtils;

    @Override
    protected ActMeteringDeviceBinding getBinding() {
        return ActMeteringDeviceBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initLabel() {
        binding.navigationBar.setTitleText("计量");
        initBatteryReceiver(binding.navigationBar);
        initDeviceStatusExecutor(binding.navigationBar);
    }


    @Override
    protected void initView() {
        signalArrayUtils = new SignalArrayBufferUtils();
        signalArrayUtils.setSize(64);
        meanUtils = new Optimized10ElementTrimmedMeanUtils();
        mediaPlayer = CustomMediaPlayer.getMediaPlayer(null);
        //
        SpinnerUtils.attachDataSource(binding.spDiameter, StaticConstant.rebarDiameterList1, (spinner, text, position) -> {
            SPUtils.getInstance().put("spDiameter", position);
            setStandardList();
        });
        binding.spDiameter.setSelection(SPUtils.getInstance().getInt("spDiameter", 0));  //纵筋直径
        binding.btnDelete.setOnClickListener(this::onViewClicked);
        binding.btnCancel.setOnClickListener(this::onViewClicked);
        //
        spMac = SPUtils.getInstance().getString(StaticConstant.SP_CORROSION_BLE_MAC);
        calibrationSignalValue();
        initRecyclerView();
        setStandardList();
        initBluetooth();
        initExecutor();
        initOptimizedDataProcessor();
    }

    private List<Integer> calibrationStandardList = new ArrayList<>();
    private List<Integer> calibrationList = new ArrayList<>();

    private void calibrationSignalValue() {
        String strCalibrationStandardList = SPUtils.getInstance().getString("calibration-standard-list");
        calibrationStandardList = new Gson().fromJson(strCalibrationStandardList, new TypeToken<List<Integer>>() {
        }.getType());
        String strCalibrationList = SPUtils.getInstance().getString("calibration-list");
        calibrationList = new Gson().fromJson(strCalibrationList, new TypeToken<List<Integer>>() {
        }.getType());
    }

    private void initRecyclerView() {
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 5));
        listAdapter = new MeteringDeviceAdapter();
        binding.recyclerView.setAdapter(listAdapter);
        //
        String strMeteringDeviceList = SPUtils.getInstance().getString("strMeteringDeviceList");
        if (!TextUtils.isEmpty(strMeteringDeviceList)) {
            List<MeteringBean> list = new Gson().fromJson(strMeteringDeviceList, new TypeToken<List<MeteringBean>>() {
            }.getType());
            listAdapter.submitList(list);
        }
    }

    private void setStandardList() {
        strDiameter = StaticConstant.rebarDiameterList1.get(SPUtils.getInstance().getInt("spDiameter", 0));
        switch (strDiameter) {
            case "7":
                standardArray = StaConMetering.meteringList_7;
                break;
            case "8":
                standardArray = StaConMetering.meteringList_8;
                break;
            case "9":
                standardArray = StaConMetering.meteringList_9;
                break;
            case "11":
                standardArray = StaConMetering.meteringList_11;
                break;
            case "12":
                standardArray = StaConMetering.meteringList_12;
                break;
            case "14":
                standardArray = StaConMetering.meteringList_14;
                break;
            case "16":
                standardArray = StaConMetering.meteringList_16;
                break;
            default:
                standardArray = StaConMetering.meteringList_10;
                break;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void onViewClicked(View view) {
        OtherUtils.hideKeyboard(this);
        if (view.getId() == binding.btnDelete.getId()) {
            listAdapter.getItems().clear();
            listAdapter.notifyDataSetChanged();
        } else if (view.getId() == binding.btnCancel.getId()) {
            finish();
        }
    }

    private void initBluetooth() {
        BluetoothManager.registerConnectStatusListener(spMac, mBleConnectStatusListener);
        if (BluetoothManager.getConnectStatus(spMac)) {
            notifyBluetoothDevice();
        } else {

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
            dataProcessorHandler.post(() -> binding.tvValue.setText("位移|信号：" + displacement + " , " + signal));

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
            //
            dataProcessorHandler.post(() -> {
                treatSignalValue((int) maxBean.y);
                isCollectStop = false;
            });
        });
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

    /**
     * 计算厚度
     *
     * @param signalValue：最大信号
     */
    private void treatSignalValue(int signalValue) {
        double thickness = 0;
        for (int i = 0; i < standardArray.size(); i++) {
            if (signalValue >= standardArray.get(i)) {
                int max = standardArray.get(Math.max(i - 1, 0));
                int min = standardArray.get(i);
                thickness = StringUtils.getRounding(StringUtils.getInterpolationValue(max, i, min, i + 1, signalValue), 1);
                break;
            }
        }
        //
        binding.tvValue.setText("钢筋保护层厚度：" + thickness + "mm，" + signalValue);
        //
        listAdapter.getItems().add(new MeteringBean(strDiameter, signalValue, thickness));
        listAdapter.notifyItemInserted(listAdapter.getItems().size() - 1);
        binding.recyclerView.scrollToPosition(listAdapter.getItems().size() - 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scheduledExecutorService.shutdownNow();
        CustomMediaPlayer.releasePlayer(mediaPlayer);
        BluetoothManager.unNotifyBluetoothDevice(spMac);
        BluetoothManager.unregisterConnectStatusListener(spMac, mBleConnectStatusListener);
        // 保存数据
        if (listAdapter != null && !listAdapter.getItems().isEmpty()) {
            SPUtils.getInstance().put("strMeteringDeviceList", new Gson().toJson(listAdapter.getItems()));
        } else {
            SPUtils.getInstance().put("strMeteringDeviceList", "");
        }
    }
}

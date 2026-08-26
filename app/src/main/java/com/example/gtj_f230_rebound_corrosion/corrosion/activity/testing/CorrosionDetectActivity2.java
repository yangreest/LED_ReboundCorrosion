package com.example.gtj_f230_rebound_corrosion.corrosion.activity.testing;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.base.activity.BaseActivity;
import com.example.gtj_f230_rebound_corrosion.base.model.CorrosionZoneBean;
import com.example.gtj_f230_rebound_corrosion.base.model.ZoneBean;
import com.example.gtj_f230_rebound_corrosion.base.storage.GreenDaoHelper;
import com.example.gtj_f230_rebound_corrosion.base.utils.CustomMediaPlayer;
import com.example.gtj_f230_rebound_corrosion.base.utils.OptionsPickerViewUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.OtherUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.SpinnerUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.StringUtils;
import com.example.gtj_f230_rebound_corrosion.base.view.StyleAlertDialog;
import com.example.gtj_f230_rebound_corrosion.base.view.StyleAlertDialog2;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.CheckDataBean;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.CorrosionBean;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.LineChartBean;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.RebarMinMaxBean;
import com.example.gtj_f230_rebound_corrosion.corrosion.storage.CorrosionCurve;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.LineChartManager;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.OptimizedDataProcessor;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.SignalArrayBufferUtils;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.fittingcurve.ComputeUtils;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.fittingcurve.DoubleRingBufferUtils;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.fittingcurve.ValleyDetectionUtils;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.lowpassfilter.GaussianFilter;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.lowpassfilter.Optimized10ElementTrimmedMeanUtils;
import com.example.gtj_f230_rebound_corrosion.databinding.ActDetect1Binding;
import com.example.gtj_f230_rebound_corrosion.databinding.ViewGridContentBinding;
import com.example.gtj_f230_rebound_corrosion.functions.database.greenDao.db.RebarMinMaxBeanDao;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.inuker.bluetooth.library.BluetoothManager;
import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@SuppressLint({"SetTextI18n", "DefaultLocale", "SetTextI18n"})
public class CorrosionDetectActivity2 extends BaseActivity<ActDetect1Binding> {
    private LineChartManager lineChartManager;
    private MediaPlayer mediaPlayer;
    private ScheduledExecutorService executorService;
    private long timerTimeHor, timerTimeVer;
    private String spMac;
    private ZoneBean zoneBean;
    private final List<CorrosionBean> corrosionBeanList = new ArrayList<>();
    private OptimizedDataProcessor dataProcessor;
    private Handler dataProcessorHandler;
    private Optimized10ElementTrimmedMeanUtils meanUtils;  //平均信号
    private RebarMinMaxBeanDao minMaxBeanDao;
    private int horVer;  //横向，竖向
    private boolean isSaveData = true;  //是否保存数据
    private SignalArrayBufferUtils signalArrayUtilsHor;
    private int lastSignal;
    private DoubleRingBufferUtils bufferUtils;
    private static final double[] temp = {265, 69, 68, 67, 68, 67, 66, 67, 68, 67, 66, 67, 68, 67, 68, 69, 68, 67, 65, 66, 67, 66, 67, 68, 69, 70, 71, 72, 74, 73, 75, 76, 81, 86, 87, 89, 91, 92, 98, 107, 113, 117, 119, 121, 134, 146, 150, 156, 161, 167, 172, 174, 208, 248, 254, 258, 269, 281, 322, 365, 383, 392, 400, 418, 504, 593, 620, 635, 648, 678, 712, 745, 952, 1160, 1210, 1267, 1322, 1381, 1673, 1970, 2042, 2112, 2183, 2262, 2537, 2814, 2895, 2934, 2977, 3062, 3480, 3884, 3945, 4009, 4071, 4130, 4315, 4487, 4510, 4526, 4516, 4486, 4458, 4427, 4392, 4354, 4076, 3789, 3724, 3650, 3574, 3497, 3101, 2704, 2630, 2594, 2554, 2473, 2209, 1951, 1880, 1810, 1743, 1676, 1398, 1130, 1085, 1041, 998, 957, 806, 664, 639, 617, 596, 574, 523, 476, 460, 449, 440, 430, 410, 397, 400, 405, 448, 496, 513, 530, 548, 567, 682, 809, 850, 888, 926, 968, 1136, 1310, 1365, 1422, 1484, 1548, 1561, 1615, 1684, 1935, 2188, 2265, 2693, 3116, 3227, 3307, 3341, 3616, 3878, 3911, 3942, 3972, 3996, 3994, 3973, 3948, 3916, 3793, 3665, 3616, 3560, 3503, 3445, 3080, 2716, 2651, 2578, 2507, 2472, 2172, 1840, 1777, 1722, 1666, 1606, 1445, 1291, 1243, 1195, 1152, 1108, 926, 749, 721, 697, 671, 646, 557, 472, 456, 443, 434, 424, 388, 358, 356, 353, 359, 368, 373, 380, 391, 400, 409, 421, 470, 521, 540, 560, 582, 603, 729, 860, 895, 935, 975, 1018, 1178, 1342, 1394, 1448, 1504, 1561, 1566, 1619, 1679, 1899, 2123, 2189, 2256, 2325, 2388, 2695, 3002, 3058, 3112, 3164, 3190, 3333, 3490, 3513, 3536, 3558, 3575, 3589, 3585, 3565, 3544, 3520, 3507, 3494, 3292, 3080, 3029, 2972, 2913, 2855, 2610, 2394, 2360, 2300, 2242, 2178, 2114, 2051, 1909, 1699, 1572, 1518, 1464, 1438, 1416, 1189, 967, 932, 898, 863, 845, 742, 625, 601, 581, 561, 538, 527, 517, 456, 400, 392, 382, 371, 366, 345, 326, 325, 327, 328, 342, 360, 367, 377, 387, 390, 396, 407, 496, 581, 592, 615, 638, 662, 769, 881, 920, 959, 980, 1002, 1194, 1394, 1453, 1513, 1578, 1641, 1704, 1765, 1802, 2169, 2572, 2665, 2758, 2844, 2933, 3219, 3499, 3586, 3681, 3779, 3871, 4202, 4525, 4598, 4667, 4727, 4784, 4842, 4877, 4903, 5050, 5186, 5204, 5219, 5170, 5097, 5063, 5026, 4984, 4939, 4716, 4485, 4418, 4353, 4286, 4214, 4138, 4062, 3939, 3669, 3446, 3370, 3293, 3219, 3184, 2851, 2486, 2418, 2348, 2279, 2214, 2061, 1940, 1909, 1849, 1791, 1736, 1685, 1634, 1581, 1370, 1164, 1125, 1089, 1049, 1015, 862, 725, 712, 686, 661, 637, 552, 467, 455, 450, 438, 422, 405, 389, 337, 292, 287, 275, 266, 256, 220, 183, 175, 170, 164, 159, 140, 122, 117, 112, 109, 106, 103, 93, 84, 80, 76, 75, 74, 65, 55, 53, 51, 49, 46, 44, 40, 39, 37, 35, 34, 33, 32, 29, 25, 23, 22, 23, 22, 20, 19, 18, 17, 15, 14, 13, 12, 9, 8, 9, 7, 5, 6, 5, 4, 3, 4, 5, 4, 3, 2, 3, 2, 3, 2, 3, 2, 3, 2, 1, 0, 1, 2, 1, 3, 4, 5, 6, 4, 5, 8, 11, 13, 15, 16, 17, 20, 23, 24, 25, 33, 40, 41, 43, 44, 45, 44, 43, 42, 41, 42, 43, 45, 49, 51, 52, 55, 57, 60, 64, 67, 69, 72, 73, 72, 71, 70, 69, 68, 69, 70, 71, 74, 78, 79, 81, 85, 89, 92, 93, 94, 96, 98, 100, 103, 114, 127, 131, 134, 138, 142, 153, 168, 174, 176, 180, 183, 191, 199, 203, 207, 210, 213, 214, 219, 226, 229, 230, 231};
    //private boolean isDetecting;

    @Override
    protected ActDetect1Binding getBinding() {
        return ActDetect1Binding.inflate(getLayoutInflater());
    }

    @Override
    protected void initLabel() {
        zoneBean = (ZoneBean) getIntent().getSerializableExtra("ZoneBean");
        if (zoneBean == null) {
            return;
        }
        initBatteryReceiver(binding.navigationBar);
        initDeviceStatusExecutor(binding.navigationBar);
        binding.navigationBar.setTitleText("锈蚀检测").setProjectName(zoneBean.projectName).setZoneName(zoneBean.number);
        //
        binding.layoutBase.setVisibility(View.VISIBLE);
        binding.btnDetect.setVisibility(View.VISIBLE);
        binding.layoutStart.setVisibility(View.GONE);
    }

    @Override
    protected void initView() {
        minMaxBeanDao = GreenDaoHelper.getDaoSession(this).getRebarMinMaxBeanDao();
        mediaPlayer = CustomMediaPlayer.getMediaPlayer(null);
        lineChartManager = new LineChartManager(this, binding.lineChart);// 为什么携带参数binding.lineChart？
        meanUtils = new Optimized10ElementTrimmedMeanUtils();
        calibrationSignalValue();
        //
        if (zoneBean.corrosionZoneBean == null) {
            //开始检测，获取上次存储的配置，检测参数可修改
            String strZoneBean = SPUtils.getInstance().getString(StaticConstant.SP_CORROSION_LAST_CONFIG);
            if (!TextUtils.isEmpty(strZoneBean)) {
                zoneBean.corrosionZoneBean = new Gson().fromJson(strZoneBean, new TypeToken<CorrosionZoneBean>() {
                }.getType());
            } else {
                zoneBean.corrosionZoneBean = new CorrosionZoneBean();
            }
            initParams();
        } else {
            //续测，检测参数不可修改
            initParams();
            setParamNotChange();
            if (zoneBean.corrosionZoneBean.zoneCount > 0) {
                corrosionBeanList.addAll(new Gson().fromJson(zoneBean.corrosionZoneBean.strZoneList, new TypeToken<List<CorrosionBean>>() {
                }.getType()));
            }
        }
        binding.ivHor.setOnClickListener(this::onViewClicked);
        binding.ivVer.setOnClickListener(this::onViewClicked);
        binding.tvHor.setOnClickListener(this::onViewClicked);
        binding.tvVer.setOnClickListener(this::onViewClicked);
        binding.btnDetect.setOnClickListener(this::onViewClicked);
        binding.btnFinish.setOnClickListener(this::onViewClicked);
        binding.tvSteelDiameter.setOnClickListener(this::onViewClicked);
        binding.btnTempLading.setOnClickListener(this::onViewClicked);
        binding.btnSelectFile.setOnClickListener(this::onViewClicked);
        signalArrayUtilsHor = new SignalArrayBufferUtils();
        signalArrayUtilsHor.setSize(64);
        bufferUtils = new DoubleRingBufferUtils(1024 * 8);
        spMac = SPUtils.getInstance().getString(StaticConstant.SP_CORROSION_BLE_MAC);
        binding.layoutGridTop.tvGrid2.setVisibility(View.GONE);
        binding.layoutGridTop.tvGrid3.setVisibility(View.GONE);
        initExecutor();// 创建线程池
        initBluetooth();
        initOptimizedDataProcessor(); // 创建数据处理工具
        refreshGrid();// 刷新网格

        // 初始化数据显示曲线，自定义8000 的一个0的数据，用于初始化曲线显示
        double[] data = new double[8000];
        showCurveLine(data);
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

        /**
         * 初始化定时线程池，周期性驱动横向与纵向检测计时。
         * <p>
         * 创建单线程的定时线程池，以固定延迟方式每秒执行一次任务：
         * 通过主线程 Handler 切回主线程，累加横向、纵向计时计数器，
         * 当纵向计时累计达到 2 秒及以上时，触发纵向壁厚数据处理。
         * <p>
         * 本方法无参数、无返回值。
         */
        private void initExecutor() {
            Handler handlerPost = new Handler();
            executorService = Executors.newScheduledThreadPool(1);
            // 固定延迟周期任务：初始延迟 0 秒，此后每隔 1 秒执行一次，通过 Handler 投递到主线程执行
            executorService.scheduleWithFixedDelay(() -> handlerPost.post(() -> {
//                // 每秒累加横向、纵向检测计时
//                timerTimeHor++; // 横向计时累加
//                timerTimeVer++; // 纵向计时累加
//                // 纵向计时达到 2 秒后，执行纵向壁厚数据处理
//                if (timerTimeVer >= 2) {
//                    treatVerThickness();
//                }

                // 无数据计时累加
                timerTimeVer++;
                // 2 秒内没有接收到数据，结束缓存并执行结算
                if (timerTimeVer >= 2) {
                    if (!bufferUtils.isEmpty()) {
                        tempLoading();
                    }
                }
            }), 0, 1, TimeUnit.SECONDS);
        }

    private void initParams() {
        setHideKeyboard();
        SpinnerUtils.attachDataSource(binding.spPoleType, StaticConstant.poleTypeList, (spinner, text, position) -> setPoleTopDiameter(position));  //电杆类型
        SpinnerUtils.attachDataSource(binding.spPoleStandard, StaticConstant.poleStandardList, null);  //规范
        binding.etPoleHeight.addTextChangedListener(textWatcher);
        binding.etPoleBottomDiameter.addTextChangedListener(textWatcher);
        binding.etPoleTopDiameter.addTextChangedListener(textWatcher);
        binding.etDistance.addTextChangedListener(textWatcher);
        binding.etPoleBottomDiameter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String strPoleType = binding.spPoleType.getSelectedItem() + "";
                if (TextUtils.equals(strPoleType, "等径杆")) {
                    binding.etPoleTopDiameter.setText(s.toString());
                }
            }
        });
        binding.spPoleType.setSelection(zoneBean.corrosionZoneBean.poleType);  //电杆类型
        binding.spPoleStandard.setSelection(zoneBean.corrosionZoneBean.poleStandard);  //规范
        //试验参数
        binding.etPoleHeight.setText(zoneBean.corrosionZoneBean.poleHeight);  //电杆长度
        binding.etPoleBottomDiameter.setText(zoneBean.corrosionZoneBean.poleBottomDiameter);  //电杆根径
        binding.etPoleTopDiameter.setText(zoneBean.corrosionZoneBean.poleTopDiameter);  //电杆梢径
        binding.tvSteelDiameter.setText(zoneBean.corrosionZoneBean.rebarDiameter);  //设计纵筋直径
        binding.etDistance.setText(zoneBean.corrosionZoneBean.Distance);
        setPoleTopDiameter(binding.spPoleType.getSelectedItemPosition());
    }

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            configCalibration();
        }
    };

    private void setPoleTopDiameter(int poleTypeIndex) {
        if (poleTypeIndex == 0 || poleTypeIndex == 1) {
            binding.etPoleTopDiameter.setBackgroundResource(R.drawable.bg_edittext);
            binding.etPoleTopDiameter.setEnabled(true);
        } else if (poleTypeIndex == 2) {
            binding.etPoleTopDiameter.setBackgroundResource(R.drawable.bg_edittext_gray);
            binding.etPoleTopDiameter.setEnabled(false);
            binding.etPoleTopDiameter.setText(binding.etPoleBottomDiameter.getText().toString().trim());
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setParamNotChange() {
        binding.spPoleType.setOnTouchListener((v, event) -> true);
        binding.spPoleType.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_edittext_gray));
        binding.ivPoleType.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_pull_down_gray_right));
        binding.spPoleStandard.setOnTouchListener((v, event) -> true);
        binding.spPoleStandard.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_edittext_gray));
        binding.ivPoleStandard.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_pull_down_gray_right));
        binding.etPoleHeight.setFocusable(false);
        binding.etPoleHeight.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_edittext_gray));
        binding.etPoleBottomDiameter.setFocusable(false);
        binding.etPoleBottomDiameter.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_edittext_gray));
        binding.etPoleTopDiameter.setFocusable(false);
        binding.etPoleTopDiameter.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_edittext_gray));
        binding.btnDetect.setEnabled(false);
        binding.btnDetect.setBackgroundResource(R.drawable.bg_button_gray);
        binding.tvSteelDiameter.setEnabled(false);
        binding.tvSteelDiameter.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_edittext_gray));
        binding.etDistance.setEnabled(false);
        binding.etDistance.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_edittext_gray));
        configCalibration();
    }

    private double poleRadius;  //电杆半径
    private double poleCompensation;  //电杆直径补偿

    private void configCalibration() {
        String strPoleHeight = binding.etPoleHeight.getText().toString().trim();  //长度
        String strPoleBottomDiameter = binding.etPoleBottomDiameter.getText().toString().trim();  //根径
        String strPoleTopDiameter = binding.etPoleTopDiameter.getText().toString().trim();  //稍径
        String strRebarDiameter = binding.tvSteelDiameter.getText().toString().trim();  //设计纵筋直径
        String strDistance = binding.etDistance.getText().toString().trim();  //距离根部
        if (TextUtils.isEmpty(strPoleHeight) || TextUtils.isEmpty(strPoleBottomDiameter) || TextUtils.isEmpty(strPoleTopDiameter) || TextUtils.isEmpty(strRebarDiameter) || TextUtils.isEmpty(strDistance)) {
            return;
        }
        double x2, poleBottomDiameter, poleTopDiameter, distance;
        try {
            x2 = Double.parseDouble(strPoleHeight);
            poleBottomDiameter = Double.parseDouble(strPoleBottomDiameter);
            poleTopDiameter = Double.parseDouble(strPoleTopDiameter);
            distance = Double.parseDouble(strDistance);
        } catch (NumberFormatException e) {
            return;
        }
        poleRadius = StringUtils.getInterpolationValue(0.001, poleBottomDiameter, x2, poleTopDiameter, distance) / 2;
        double ac = poleRadius + 22;  //22：钢筋仪轱辘半径22mm
        double cd = 52 / 2.0;
        double ad = Math.sqrt(ac * ac - cd * cd);
        //电杆直径补偿
        poleCompensation = StringUtils.getRounding(ac - ad, 2);
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

    /**
     * 订阅蓝牙设备的 Notify 数据推送。
     * <p>
     * 向蓝牙管理器注册数据通知回调：设备每推送一帧原始数据，
     * 在页面可见且已进入检测方向（横向/竖向）的前提下，
     * 将该帧投递给数据处理器进行拼接、解帧与后续信号处理。
     * <p>
     * 本方法无参数、无返回值。
     */
    private void notifyBluetoothDevice() {
        BluetoothManager.notifyBluetoothDevice(spMac, new BleNotifyResponse() {
            @Override
            public void onNotify(UUID service, UUID character, byte[] value) {
                // 页面不可见或尚未选择检测方向时，丢弃该帧数据
                if (!isShow /*|| horVer == 0*/) {
                    return;
                }
                // 将原始帧交给数据处理器，异步解帧后回调处理
                dataProcessor.receiveDataOptimized(value);
            }

            @Override
            public void onResponse(int code) {
            }
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

    private int cur_displacement;

        /**
         * 初始化优化数据处理器。
         * <p>
         * 创建主线程 Handler 并构建 {@link OptimizedDataProcessor}，在其据回调中数完成：
         * 信号校准、滑动平均计算、实时信号界面刷新，并按当前检测方向（横向/竖向）
         * 分发处理——横向实时记录测点数据；竖向仅在横向完成 3 次测量后采集有效信号。
         * <p>
         * 本方法无参数、无返回值。
         */
        private void initOptimizedDataProcessor() {
            dataProcessorHandler = new Handler(Looper.getMainLooper());
            // 数据回调：displacement 位移量、signal 原始信号值、frameData 帧数据
            dataProcessor = new OptimizedDataProcessor((displacement, signal, frameData) -> {
                //校准信号
                int signalValue = getSignalValue(signal);
                //
                //平均信号
                int tempSignal = (int) meanUtils.addValue(signalValue);
                // 切回主线程刷新实时信号显示
                dataProcessorHandler.post(() -> binding.tvSignal.setText( displacement +" , " + tempSignal));
                //
//                // 按检测方向分发数据处理
//                if (horVer == 1) {  //横向
//                    treatHor(displacement, tempSignal);
//                } else if (horVer == 2) {  //竖向
//                    // 信号发生变化时才处理，避免重复采集
//                    if (lastSignal != tempSignal) {
//                        lastSignal = tempSignal;
//                        //横向3次才可以竖向测试
//                        if (checkDataBeanListHor.size() == 3) {
//                            treatVer(tempSignal);
//                        }
//                    }
//                }
                treatSignal(tempSignal);
            });
        }

    private long signalTimeVer;
    /**
     * 缓存蓝牙信号。
     * <p>
     * 收到数据即重置 2 秒无数据计时，并按 300ms 采样间隔写入环形缓冲区，
     * 避免高频推送写入过多重复数据。
     * <p>
     * @param signal 校准并滑动平均后的信号值
     */
    private void treatSignal(int signal) {
        // 收到数据，重置无数据计时
        timerTimeVer = 0;
        long tempTime = System.currentTimeMillis();
        if (tempTime - signalTimeVer <= 10) {
            return;
        }
        signalTimeVer = tempTime;
        bufferUtils.add(signal);
    }
    /**
     * 竖向
     */
    private void treatVer(int signal) {
        //超过2秒清空数据
        {
            if (timerTimeVer >= 2) {
                bufferUtils.clear();
                signalTimeVer = System.currentTimeMillis();
            }
            timerTimeVer = 0;
        }
        long tempTime = System.currentTimeMillis();
        if (tempTime - signalTimeVer <= 300) {
            return;
        }
        bufferUtils.add(signal);
    }

    private void showCurveLine(double[] signals) {
        double max = StringUtils.getMax(signals);
        //显示曲线
        List<LineChartBean> chartBeanList = new ArrayList<>();
        chartBeanList.add(new LineChartBean("", 0, 1 + ". ", max, signals));
        lineChartManager.showLineChart2(chartBeanList);
        //同步刷新波峰波谷颜色显示条
        showColorStrip(signals);
    }

    /**
     * 在曲线图上方颜色条上渲染信号分布彩图。
     * <p>
     * 将信号数组归一化到 [0,1]，波峰（最大值）映射为红色，
     * 波谷（最小值）映射为蓝色，中间值按比例在红→蓝色相带上线性插值，
     * 生成逐像素着色的位图并以拉伸方式铺满颜色条，
     * 使颜色条与下方曲线的波峰波谷位置一一对应。
     *
     * @param data：竖向扫描采集的信号数组
     */
    private void showColorStrip(double[] data) {
        if (data == null || data.length == 0) {
            binding.ivColorStrip.setImageBitmap(null);
            return;
        }
        //高斯低通滤波去除噪点，平滑后的数据仍保留波峰波谷趋势
        double[] filtered = GaussianFilter.gaussianFilter(data, 2, 9);

        //统计信号的最大值与最小值，用于归一化
        double min = data[0];
        double max = data[0];
        for (double value : data) {
            if (value < min) {
                min = value;
            }
            if (value > max) {
                max = value;
            }
        }
        //逐像素计算颜色：ratio=1 为波峰(红)，ratio=0 为波谷(蓝)
        double range = max - min;
        int width = data.length;
        int[] pixels = new int[width];
        for (int i = 0; i < width; i++) {
            float ratio = range <= 0 ? 1f : (float) ((data[i] - min) / range);
            //色相从红(0°)到蓝(240°)，中间态按彩虹色带过渡
//            if(ratio <= 0.2f){
//            //
//            float hue = ratio * 240f;
//            pixels[i] = Color.HSVToColor(new float[]{hue, 1f, 1f});}
//            else{// 显示成灰色
//                pixels[i] = Color.GRAY;
//            }
            float hue = ratio * 240f;
            pixels[i] = Color.HSVToColor(new float[]{hue, 1f, 1f});
        }
        //生成 1 像素高的位图，由 ImageView 的 fitXY 拉伸成长条彩图
        Bitmap bitmap = Bitmap.createBitmap(pixels, width, 1, Bitmap.Config.ARGB_8888);
        binding.ivColorStrip.setImageBitmap(bitmap);
    }

    private void treatHor(int displacement, int signal) {
        //
        if (cur_displacement == displacement) {
            return;
        }
        cur_displacement = displacement;
        //超过2秒清空数据
        {
            if (timerTimeHor >= 2) {
                signalArrayUtilsHor.clear();
                checkDataBeanListHor.clear();
                dataProcessorHandler.post(() -> refreshCheckDataHor(false));
            }
            timerTimeHor = 0;
        }
        //
        //记录数据|获取最大值
        CheckDataBean maxBean = signalArrayUtilsHor.putAndCheck(displacement, signal);
        //
        if (maxBean == null) {
            return;
        }
        CustomMediaPlayer.startSpeaking(this, mediaPlayer, "prompt");
        if (checkDataBeanListHor.size() >= 3) {
            return;
        }
        checkDataBeanListHor.add(maxBean);
        dataProcessorHandler.post(() -> refreshCheckDataHor(true));
        if (checkDataBeanListHor.size() == 3) {
            configCalibration();
        }
    }

    private final StringBuilder stringBuilder = new StringBuilder();//

    private void treatVerThickness() {
        if (checkDataBeanListHor.size() < 3) {
            Logger.e("checkDataBeanListHor<3");
            return;
        }
        if (bufferUtils.isEmpty()) {
            Logger.e("bufferUtils isEmpty");
            return;
        }
        stringBuilder.delete(0, stringBuilder.length());
        //清空数据
        double[] data = bufferUtils.getValues();
        bufferUtils.clear();
        showCurveLine(data);
        //查找4个明显波峰和对应的3个波谷
        Map<String, List<Integer>> result = ValleyDetectionUtils.findPeaksAndValleys(data);
        if (result.isEmpty()) {
            Logger.e("PeaksAndValley isEmpty1");
            return;
        }
        //波峰
        List<Integer> peaks = result.get("peaks");
        //波谷
        List<Integer> valleys = result.get("valleys");
        if (valleys == null || valleys.size() < 3) {
            Logger.e("PeaksAndValley isEmpty2");
            return;
        }
        //
        StringBuilder sbd = new StringBuilder();
        for (int i = 0; i < (peaks != null ? peaks.size() : 0); i++) {
            int idx = peaks.get(i);
            sbd.append("波峰 ").append(i + 1).append(": 索引=").append(idx).append(", 值=").append(data[idx]).append("\n");
        }
        //
        for (int i = 0; i < valleys.size(); i++) {
            int idx = valleys.get(i);
            sbd.append("波谷 ").append(i + 1).append(": 索引=").append(idx).append(", 值=").append(data[idx]).append("\n");
        }
        stringBuilder.append(sbd);
        //
        computeRebarSpacing();
        double a = computeResult((int) data[valleys.get(0)]);
        double b = computeResult((int) data[valleys.get(1)]);
        double c = computeResult((int) data[valleys.get(2)]);
        stringBuilder.append("测厚：").append(a).append("　，　").append(b).append("　，　").append(c);
        //
        stringBuilder.append("\n锈蚀度：");
        double rebarDiameter = Double.parseDouble(binding.tvSteelDiameter.getText().toString().trim());
        double corrodedRebarDiameter;
        boolean flag = false;
        if ((b >= a && b <= c) || (b >= c && b <= a)) {  //
            //无锈蚀
            corrodedRebarDiameter = rebarDiameter;
            stringBuilder.append("\n无锈蚀 ，").append(corrodedRebarDiameter);
        } else {
            //有锈蚀
            double standardThickness = StringUtils.getRounding((a + c) / 2, 2);
            double[] signals = computeCorrodedSignal(standardThickness);
            stringBuilder.append("\n有锈蚀 ，").append("标准厚度：").append(standardThickness).append(",  标准信号：").append(Arrays.toString(signals)).append("\n");
            StringBuilder sss1 = new StringBuilder();
            double midd = (int) data[valleys.get(1)];
            int index = 0;
            for (int i = 1; i < signals.length; i++) {
                if (midd < signals[i]) {
                    index = i;
                    break;
                }
            }
            //
            if (index > 0) {
                corrodedRebarDiameter = StringUtils.getInterpolationValue(signals[index - 1], CorrosionCurve.poleNormList1.get(index - 1), signals[index], CorrosionCurve.poleNormList1.get(index), midd);
                if (!Double.isFinite(corrodedRebarDiameter)) {
                    corrodedRebarDiameter = StringUtils.getRounding((CorrosionCurve.poleNormList1.get(index - 1) + CorrosionCurve.poleNormList1.get(index)) / 2d, 2);
                }
                sss1.append(signals[index - 1]).append(" , ").append(CorrosionCurve.poleNormList1.get(index - 1)).append(" , ").append(signals[index]).append(" , ").append(CorrosionCurve.poleNormList1.get(index)).append(" , ").append(midd).append(" = ").append(corrodedRebarDiameter);
            } else {
                corrodedRebarDiameter = CorrosionCurve.poleNormList1.get(index);
                sss1.append(corrodedRebarDiameter);
            }
            stringBuilder.append(sss1);
            if (!foundTable) {
                stringBuilder.append("无");
                corrodedRebarDiameter = rebarDiameter;
            }
        }
        corrodedRebarDiameter = Math.min(corrodedRebarDiameter, rebarDiameter);
        stringBuilder.append("\n锈蚀直径：").append(corrodedRebarDiameter).append(" , ").append(foundTable);

        Logger.e("锈蚀逻辑==== " + stringBuilder);
        CorrosionBean corrosionBean = new CorrosionBean(binding.etDistance.getText().toString().trim(), zoneBean.corrosionZoneBean.rebarDiameter, StringUtils.getRoundingString(corrodedRebarDiameter, 2));
        corrosionBeanList.add(corrosionBean);
        //
        dataProcessorHandler.post(() -> {
            refreshGrid();
            binding.tvResult.setText(sbd);
            if (!StaticConstant.isRelease) {
                binding.tvResult.setText(corrosionBean.toString());
                StyleAlertDialog2 styleAlertDialog = new StyleAlertDialog2(this);
                styleAlertDialog.setContent(stringBuilder).setOKButton("确定", v -> styleAlertDialog.dismiss()).show();
            }
        });
    }

    private double[] computeCorrodedSignal(double standardThickness) {
        //信号值
        double[] signals = new double[CorrosionCurve.poleNormList.size()];
        for (int i = 0; i < CorrosionCurve.poleNormList.size(); i++) {
            signals[i] = computeSignal(CorrosionCurve.poleNormList.get(i), rebarSpacingMin, rebarSpacingMax, standardThickness);
        }
        return signals;
    }



    // 分段阈值常量（mV）(重大算法)
    private static final double TH1_15_18 = 3530.0;
    private static final double TH2_18_20 = 2515.0;
    private static final double TH3_20_22 = 1730.0;
    private static final double TH4_22_25 = 1310.0;

    /**
     * 主计算入口
     * @param v1 测点1电压 mV
     * @param v2 测点2电压 mV
     * @param v4 测点4电压 mV
     * @param v6 测点6电压 mV
     * @param v7 测点7电压 mV
     * @return 完整计算结果对象
     * @throws ArithmeticException V1+V7为0除零异常
     */
    public static SteelCorrResult calculate(double v1, double v2, double v4, double v6, double v7) {
        // 1. 计算基础电压特征
        double v17 = (v1 + v7) / 2.0;
        double v26 = (v2 + v6) / 2.0;

        // 2. 计算商值x，防除零
        if (Math.abs(v17) < 1e-9) {
            throw new ArithmeticException("V1+V7均值为0，无法计算腐蚀商值x");
        }
        double x = v4 / v17;

        // 3. 根据V26判定保护层厚度
        int cover = judgeCoverThickness(v26);

        // 4. 根据保护层匹配二次方程计算腐蚀度y、获取模型准确率
        double[] fitParam = getFitParam(cover);
        double a = fitParam[0];
        double b = fitParam[1];
        double c = fitParam[2];
        double accuracy = fitParam[3];
        double y = a * x * x + b * x + c;

        // 5. 封装返回结果
        return new SteelCorrResult(v17, v26, x, cover, y, accuracy,x<=0.95);
    }

    /**
     * 步骤3：V26分段判定保护层厚度
     * @param v26 内侧两点均值
     * @return 保护层厚度 15/18/20/22/25 mm
     */
    private static int judgeCoverThickness(double v26) {
        if (v26 >= TH1_15_18) {
            return 15;
        } else if (v26 >= TH2_18_20) {
            return 18;
        } else if (v26 >= TH3_20_22) {
            return 20;
        } else if (v26 >= TH4_22_25) {
            return 22;
        } else {
            return 25;
        }
    }

    /**
     * 步骤4：根据保护层获取二次拟合参数[a,b,c,准确率]
     * y = a*x² + b*x + c
     * @param cover 保护层厚度mm
     * @return double[0]=a, [1]=b, [2]=c, [3]=模型准确率
     */
    private static double[] getFitParam(int cover) {
        return switch (cover) {
            case 15 -> new double[]{0.4901, -1.0150, 1.0044, 76.7};
            case 18 -> new double[]{0.6480, -1.0870, 1.0913, 69.0};
            case 20 -> new double[]{1.1267, -1.2739, 0.9823, 80.0};
            case 22 -> new double[]{0.3963, -0.8219, 1.0372, 73.3};
            case 25 -> new double[]{0.9619, -1.2793, 1.0963, 69.0};
            default -> throw new IllegalArgumentException("无效保护层厚度:" + cover);
        };
    }

    private double rebarSpacingMin, rebarSpacingMax;

    private void computeRebarSpacing() {
        //1. 主筋直径
        String strRebarDiameter = binding.tvSteelDiameter.getText().toString().trim();  //设计纵筋直径
        double rebarDiameter = Double.parseDouble(strRebarDiameter);
        //2. 主筋间距
        double left =0;
        double right = 0;
        if(checkDataBeanListHor.size() < 3){
            right = 25.0 ;
            left = 12.0 ;
        }else {
            left= (checkDataBeanListHor.get(1).x - checkDataBeanListHor.get(0).x) * 0.54;
            right= (checkDataBeanListHor.get(2).x - checkDataBeanListHor.get(1).x) * 0.54;
        }
        double min = Math.min(left, right);
        double max = Math.max(left, right);
        //
        double angleMin = min / (2 * Math.PI * poleRadius) * 360;
        double angleMax = max / (2 * Math.PI * poleRadius) * 360;
        double a = poleRadius - 17 - rebarDiameter / 2;
        rebarSpacingMin = ComputeUtils.calculateThirdSide(a, a, angleMin);
        rebarSpacingMax = ComputeUtils.calculateThirdSide(a, a, angleMax);
        stringBuilder.append("\n钢筋直径：").append(rebarDiameter);
        stringBuilder.append("，　电杆直径：").append(StringUtils.getRounding(poleRadius * 2, 0));
        stringBuilder.append("\n间距Min：").append(rebarSpacingMin).append("　，　Max：").append(rebarSpacingMax);
    }

    private double computeResult(int signal) {
        String strRebarDiameter = binding.tvSteelDiameter.getText().toString().trim();  //设计纵筋直径
        return computeThickness(strRebarDiameter, rebarSpacingMin, rebarSpacingMax, signal);
    }

    private boolean foundTable;

    private double computeSignal(String rebarDiameter, double spacingMin, double spacingMax, double thickness) {
        List<int[]> spaces = new ArrayList<>();
        spaces.add(new int[]{35, 35});
        spaces.add(new int[]{40, 40});
        spaces.add(new int[]{45, 45});
        spaces.add(new int[]{50, 50});
        spaces.add(new int[]{55, 55});
        spaces.add(new int[]{60, 60});
        spaces.add(new int[]{65, 65});
        spaces.add(new int[]{70, 70});
        spaces.add(new int[]{75, 75});
        spaces.add(new int[]{35, 65});
        spaces.add(new int[]{40, 80});
        spaces.add(new int[]{50, 100});
        //
        //统计主筋间距
        List<int[]> rebarSpacings = ComputeUtils.findMatchingSpaces(spaces, new double[]{spacingMin, spacingMax});
        //箍筋间距
        String strDistance = binding.etDistance.getText().toString().trim();  //距离根部
        double distance = Double.parseDouble(strDistance);
        String strSpiralSpacing;
        if (distance >= 2 && distance <= Double.parseDouble(zoneBean.corrosionZoneBean.poleHeight) - 2) {
            strSpiralSpacing = "120";
        } else {
            strSpiralSpacing = "70";
        }
        StringBuilder sss = new StringBuilder();
        for (int[] ints : rebarSpacings) {
            sss.append("\n").append(ints[0]).append(" , ").append(ints[1]);
        }
        stringBuilder.append("\n计算信号查表：").append(sss).append(" , 间距：").append(strSpiralSpacing).append("\n");
        //
        //数据库查询条数
        List<RebarMinMaxBean> rebarMinMaxBeanList = new ArrayList<>();
        for (int[] arr : rebarSpacings) {
            rebarMinMaxBeanList.add(new RebarMinMaxBean(rebarDiameter, "0", arr[0] + "", arr[1] + "", strSpiralSpacing));
        }
        //查数据库
        for (RebarMinMaxBean bean : rebarMinMaxBeanList) {
            RebarMinMaxBean temp = minMaxBeanDao.queryBuilder().where(RebarMinMaxBeanDao.Properties.UniqueKey.eq(bean.uniqueKey)).unique();
            if (temp != null) {
                bean.signalMin = temp.signalMin;
                bean.signalMax = temp.signalMax;
            }
        }
        double temThickness = thickness - poleCompensation - 1;
        temThickness = Math.max(temThickness, 5);
        //计算信号值
        StringBuilder ss1 = new StringBuilder();
        ss1.append("\n厚度：").append(thickness + " - " + poleCompensation + " - " + 1 + " = " + temThickness);
        for (RebarMinMaxBean bean : rebarMinMaxBeanList) {
            if (bean.signalMin != null) {
                int tempThickness = (int) temThickness;
                if (tempThickness == temThickness) {
                    bean.signal = bean.signalMin[tempThickness];
                    ss1.append("\n信号结果：").append(bean.signal);
                } else {
                    // 获取较小的整数（向下取整）
                    int indexMin = (int) Math.floor(temThickness);
                    // 获取较大的整数（向上取整）
                    int indexMax = (int) Math.ceil(temThickness);
                    ss1.append("\n信号：").append(indexMin).append(" , ").append(bean.signalMin[indexMin]).append(" , ").append(indexMax).append(" , ").append(bean.signalMin[indexMax]).append(" , ").append(temThickness);
                    bean.signal = (int) StringUtils.getInterpolationValue(indexMin, bean.signalMin[indexMin], indexMax, bean.signalMin[indexMax], temThickness);
                    ss1.append("\n信号结果：").append(bean.signal).append("\n");
                }
            }
        }
        stringBuilder.append(ss1);
        //整合计算厚度
        double signalResult;
        StringBuilder sbdMessage = new StringBuilder();
        if (rebarMinMaxBeanList.size() >= 2) {  //2
            //主筋间距内插
            signalResult = StringUtils.getRounding((rebarMinMaxBeanList.get(0).signal + rebarMinMaxBeanList.get(1).signal) / 2d, 2);
            sbdMessage.append("\n主筋间距内插：\n").append(rebarMinMaxBeanList.get(0).uniqueKey).append("　～　").append(rebarMinMaxBeanList.get(1).uniqueKey).append("　信号：").append(signalResult);
        } else if (rebarMinMaxBeanList.size() == 1) {  //1
            signalResult = rebarMinMaxBeanList.get(0).signal;
            sbdMessage.append(rebarMinMaxBeanList.get(0).uniqueKey).append(": ").append(signalResult);
        } else {
            signalResult = 2200;
            sbdMessage.append("无：").append(signalResult);
        }
        foundTable = !rebarMinMaxBeanList.isEmpty();
        stringBuilder.append(sbdMessage);
        return signalResult;
    }

    private double computeThickness(String rebarDiameter, double spacingMin, double spacingMax, double signal) {
        List<int[]> spaces = new ArrayList<>();
        spaces.add(new int[]{35, 35});
        spaces.add(new int[]{40, 40});
        spaces.add(new int[]{45, 45});
        spaces.add(new int[]{50, 50});
        spaces.add(new int[]{55, 55});
        spaces.add(new int[]{60, 60});
        spaces.add(new int[]{65, 65});
        spaces.add(new int[]{70, 70});
        spaces.add(new int[]{75, 75});
        spaces.add(new int[]{35, 65});
        spaces.add(new int[]{40, 80});
        spaces.add(new int[]{50, 100});
        //
        //统计主筋间距
        List<int[]> rebarSpacings = ComputeUtils.findMatchingSpaces(spaces, new double[]{spacingMin, spacingMax});
        //箍筋间距
        String strDistance = binding.etDistance.getText().toString().trim();  //距离根部
        double distance = Double.parseDouble(strDistance);
        String strSpiralSpacing;
        if (distance >= 2 && distance <= Double.parseDouble(zoneBean.corrosionZoneBean.poleHeight) - 2) {
            strSpiralSpacing = "120";
        } else {
            strSpiralSpacing = "70";
        }
        //
        //数据库查询条数
        List<RebarMinMaxBean> rebarMinMaxBeanList = new ArrayList<>();
        for (int[] arr : rebarSpacings) {
            rebarMinMaxBeanList.add(new RebarMinMaxBean(rebarDiameter, "0", arr[0] + "", arr[1] + "", strSpiralSpacing));
        }
        //查数据库
        for (RebarMinMaxBean bean : rebarMinMaxBeanList) {
            RebarMinMaxBean temp = minMaxBeanDao.queryBuilder().where(RebarMinMaxBeanDao.Properties.UniqueKey.eq(bean.uniqueKey)).unique();
            if (temp != null) {
                bean.signalMin = temp.signalMin;
                bean.signalMax = temp.signalMax;
            }
        }
        //计算厚度
        for (RebarMinMaxBean bean : rebarMinMaxBeanList) {
            int index = 0;
            if (bean.signalMin != null) {
                for (int i = 0; i < bean.signalMin.length; i++) {
                    if (signal >= bean.signalMin[i]) {
                        index = i;
                        break;
                    }
                }
            }
            if (index == 0) {
                bean.thickness = 1;
            } else {
                bean.thickness = StringUtils.getRounding(StringUtils.getInterpolationValue(bean.signalMin[index - 1], index, bean.signalMin[index], index + 1, signal), 2);
            }
        }
        //整合计算厚度
        double ths;
        StringBuilder sbdMessage = new StringBuilder();
        sbdMessage.append("\n信号值：").append(signal);
        sbdMessage.append("\n匹配结果：");
        if (rebarMinMaxBeanList.isEmpty()) {
            sbdMessage.append("\n无");
        }
        for (RebarMinMaxBean bean : rebarMinMaxBeanList) {
            sbdMessage.append("\n厚度: ").append(bean.thickness).append(",　查表: ").append(bean.uniqueKey);
        }
        if (rebarMinMaxBeanList.size() >= 2) {  //2
            //主筋间距内插
            ths = StringUtils.getRounding((rebarMinMaxBeanList.get(0).thickness + rebarMinMaxBeanList.get(1).thickness) / 2d, 2);
            sbdMessage.append("\n主筋间距内插：").append(rebarMinMaxBeanList.get(0).uniqueKey).append("　～　").append(rebarMinMaxBeanList.get(1).uniqueKey).append("　厚度：").append(ths);
        } else if (rebarMinMaxBeanList.size() == 1) {  //1
            ths = rebarMinMaxBeanList.get(0).thickness;
            sbdMessage.append(rebarMinMaxBeanList.get(0).uniqueKey).append(": ").append(ths);
        } else {
            ths = 15;
            sbdMessage.append("无: ").append(ths);
        }
        double thickness = StringUtils.getRounding(ths + poleCompensation, 2);
        stringBuilder.append("\n厚度：").append(ths).append(" + ").append(poleCompensation).append(" = ").append(thickness);
        sbdMessage.append("\n").append(ths).append(" + ").append(poleCompensation).append(" = ").append(thickness);
        return thickness;
    }

    //横向
    private final List<CheckDataBean> checkDataBeanListHor = new ArrayList<>();

    private void refreshCheckDataHor(boolean isShow) {
        if (isShow) {
            binding.tvCheck11.setVisibility(!checkDataBeanListHor.isEmpty() ? View.VISIBLE : View.INVISIBLE);
            binding.tvCheck12.setVisibility(checkDataBeanListHor.size() >= 2 ? View.VISIBLE : View.INVISIBLE);
            binding.tvCheck13.setVisibility(checkDataBeanListHor.size() >= 3 ? View.VISIBLE : View.INVISIBLE);
        } else {
            binding.tvCheck11.setVisibility(View.INVISIBLE);
            binding.tvCheck12.setVisibility(View.INVISIBLE);
            binding.tvCheck13.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 刷新钢筋列表
     */
    private void refreshGrid() {
        binding.layoutGridContent.removeAllViews();
        if (corrosionBeanList != null) {
            for (int i = 0; i < corrosionBeanList.size(); i++) {
                ViewGridContentBinding gridContentBinding = ViewGridContentBinding.inflate(getLayoutInflater());
                View itemView = gridContentBinding.getRoot();
                configGrid(gridContentBinding, i);
                itemView.setId(i);
                itemView.setOnLongClickListener(gridItemView);
                binding.layoutGridContent.addView(gridContentBinding.getRoot());
            }
        }
    }

    /**
     * 删除
     */
    private final View.OnLongClickListener gridItemView = v -> {
        int idIndex = v.getId();
        int id = idIndex + 1;
        StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this);
        styleAlertDialog.setContent("确定删除“测点" + id + "”的数据？").setLeftButton("取消", v1 -> styleAlertDialog.dismiss()).setRightButton("确定", v1 -> {
            styleAlertDialog.dismiss();
            corrosionBeanList.remove(v.getId());
            //刷新钢筋列表
            refreshGrid();
        }).show();
        return false;
    };

    /**
     * 读取指定目录下的 txt 文件，将文件内以英文逗号分割的有效数据解析为 double 数组。
     * <p>
     * 文件内容允许跨行，解析前会去除换行与空白，空串及非法数字自动跳过。
     * <p>
     * @param dirPath  目录路径，如 Environment.getExternalStorageDirectory() + "/GTJ"
     * @param fileName 文件名，如 "data.txt"
     * @return 解析后的数据数组；文件不存在或读取失败时返回空数组
     */
    private double[] readDataFromTxt(String dirPath, String fileName) {
        File file = new File(dirPath, fileName);
        if (!file.exists()) {
            ToastUtils.showShort("文件不存在：" + file.getAbsolutePath());
            return new double[0];
        }
        //一次性读取文件全部内容
        String content = FileIOUtils.readFile2String(file, "UTF-8");
        if (TextUtils.isEmpty(content)) {
            return new double[0];
        }
        //去除换行符，统一按英文逗号分割
        content = content.replace("\r", ",").replace("\n", ",").replace("，", ",");
        String[] items = content.split(",");
        List<Double> values = new ArrayList<>();
        for (String item : items) {
            String str = item.trim();
            if (TextUtils.isEmpty(str)) {
                continue;
            }
            try {
                values.add(Double.parseDouble(str));
            } catch (NumberFormatException e) {
                //跳过非法数据
                Logger.e("非法数据：" + str);
            }
        }
        //写入 double[] 数组
        double[] data = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            data[i] = values.get(i);
        }
        Logger.e("读取txt数据共 " + data.length + " 个");
        return data;
    }

    /**
     * 将 double 数组按读取格式写入指定目录下的 txt 文件。
     * <p>
     * 数据以英文逗号分隔，与 readDataFromTxt 的解析格式保持一致，
     * 写入前自动创建不存在的目录，覆盖写入原文件内容。
     * <p>
     * @param dirPath  目录路径，如 Environment.getExternalStorageDirectory() + "/GTJ"
     * @param fileName 文件名，如 "data.txt"
     * @param data     待写入的数据数组
     * @return 写入成功返回 true，否则返回 false
     */
    private boolean writeDataToTxt(String dirPath, String fileName, double[] data) {
        if (data == null) {
            ToastUtils.showShort("写入数据为空");
            return false;
        }
        File dir = new File(dirPath);
        //目录不存在时先创建目录
        if (!dir.exists() && !dir.mkdirs()) {
            ToastUtils.showShort("目录创建失败：" + dir.getAbsolutePath());
            return false;
        }
        //按读取格式拼接：英文逗号分隔各数据项
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            //整数数值去掉小数位，保持与原始信号文件格式一致
            if (data[i] == Math.floor(data[i]) && !Double.isInfinite(data[i])) {
                sb.append((long) data[i]);
            } else {
                sb.append(data[i]);
            }
        }
        //覆盖写入文件
        File file = new File(dir, fileName);
        boolean success = FileIOUtils.writeFileFromString(file, sb.toString(), false);
        if (success) {
            Logger.e("写入txt数据共 " + data.length + " 个：" + file.getAbsolutePath());
        } else {
            ToastUtils.showShort("写入txt失败：" + file.getAbsolutePath());
        }
        return success;
    }

    private void tempGetData()
    {
        // 造一组测试数据，满足波峰和波谷的趋势，给定源数据点，线性填充成8000组数据
//        double[] baseData = {1456, 963, 1269, 643, 1396, 927, 1073, 930, 1049,721,1218};
//        // 在每个源数据点两侧生成缓变过渡点，保证波峰波谷不落在数组端点且两侧有明显的升降区间
//        List<Double> sourceList = new ArrayList<>();
//        for (int i = 0; i < baseData.length; i++)
//        {
//            double curr = baseData[i];
//            // 首尾端点用当前值的0.6倍作为虚拟相邻点，保证端点处也呈升降过渡趋势
//            double prev = i > 0 ? baseData[i - 1] : curr * 0.6;
//            double next = i < baseData.length - 1 ? baseData[i + 1] : curr * 0.6;
//            // 左侧过渡点：前一个点到当前点的75%位置
//            sourceList.add(prev + (curr - prev) * 0.75);
//            // 源数据点本身
//            sourceList.add(curr);
//            // 右侧过渡点：当前点到下一个点的25%位置
//            sourceList.add(curr + (next - curr) * 0.25);
//        }
//        double[] sourceData = new double[sourceList.size()];
//        for (int i = 0; i < sourceData.length; i++)
//        {
//            sourceData[i] = sourceList.get(i);
//        }
//        double[] data = new double[2000];
//        // 将源数据点相邻区间线性插值，填充到8000组数据
//        int sourceLen = sourceData.length;
//        for (int i = 0; i < data.length; i++)
//        {
//            // 当前索引在源数据中的映射位置
//            double pos = (double) i / (data.length - 1) * (sourceLen - 1);
//            int index = (int) pos;
//            if (index >= sourceLen - 1)
//            {
//                data[i] = sourceData[sourceLen - 1];
//            }
//            else
//            {
//                // 相邻两点线性插值，取整保持数值为整数
//                double ratio = pos - index;
//                data[i] = sourceData[index] + (sourceData[index + 1] - sourceData[index]) * ratio;
//            }
//        }
        // 初始化数据显示曲线，自定义8000 的一个0的数据，用于初始化曲线显示
        double[] data = readDataFromTxt(android.os.Environment.getExternalStorageDirectory() + "/GTJ", "Log_2026-08-12_15_35_18_signal.txt");
        if (data.length == 0) {
            data = new double[8000];
        }
        // 将data 赋值给 bufferUtils
        bufferUtils.addAll(data, 0, data.length);
    }
    /**
     * 弹出文件选择框，列出 GTJ 目录下的全部 txt 数据文件。
     * <p>
     * 按文件修改时间从新到旧排序，选中确定后调用 loadFileData
     * 读取该文件数据并刷新曲线显示。
     * <p>
     * 本方法无参数、无返回值。
     */
    private void showFileSelectDialog() {
        String dirPath = android.os.Environment.getExternalStorageDirectory() + "/GTJ";
        File dir = new File(dirPath);
        //目录不存在时提示
        if (!dir.exists() || !dir.isDirectory()) {
            ToastUtils.showShort("目录不存在：" + dirPath);
            return;
        }
        //仅列出 txt 文件
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".txt"));
        if (files == null || files.length == 0) {
            ToastUtils.showShort("目录下没有txt文件：" + dirPath);
            return;
        }
        //按修改时间从新到旧排序，最新采集的文件排在最前
        List<File> fileList = new ArrayList<>(Arrays.asList(files));
        Collections.sort(fileList, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        List<String> fileNameList = new ArrayList<>();
        for (File file : fileList) {
            fileNameList.add(file.getName());
        }
        //弹出单列选择框，选中后加载文件数据
        OptionsPickerViewUtils.showSingleColumnPickerView(this, "请选择数据文件", "", fileNameList, "", position -> loadFileData(fileNameList.get(position)));
    }

    /**
     * 读取选中的 txt 文件数据并显示到曲线。
     * <p>
     * 读取成功后清空原蓝牙缓存，将文件数据写入 bufferUtils，
     * 供后续结算使用，同时刷新曲线显示；读取失败仅提示。
     * <p>
     * @param fileName GTJ 目录下的文件名
     */
    private void loadFileData(String fileName) {
        String dirPath = android.os.Environment.getExternalStorageDirectory() + "/GTJ";
        double[] data = readDataFromTxt(dirPath, fileName);
        if (data.length == 0) {
            ToastUtils.showShort("文件无有效数据：" + fileName);
            return;
        }
        //文件数据写入缓存，供后续结算使用
        bufferUtils.clear();
        bufferUtils.addAll(data, 0, data.length);
        //刷新曲线显示
        showCurveLine(data);
        isSaveData = false;
        ToastUtils.showShort("已加载 " + fileName + "，共 " + data.length + " 个数据");
    }


    private void tempLoading()
    {
        if (bufferUtils.isEmpty()) {
            ToastUtils.showShort("蓝牙数据缓存为空，请采集后再次按下");
            return;
        }
        // 将蓝牙数据缓存赋值给 data 作为结算数据源
        double[] data = bufferUtils.getValues();
        bufferUtils.clear();
        stringBuilder.delete(0, stringBuilder.length());
        if(isSaveData) {
            // 生成日期字符串
            String strData = StringUtils.getStringTime(System.currentTimeMillis(), "yyyy-MM-dd_HHmmss");
            writeDataToTxt(android.os.Environment.getExternalStorageDirectory() + "/GTJ",  "data_"+strData+".txt", data);
        }
        isSaveData = true;

        //查找4个明显波峰和对应的3个波谷
        Map<String, List<Integer>> result = ValleyDetectionUtils.findPeaksAndValleys(data);
        if (result.isEmpty()) {
            showCurveLine(data);
            ToastUtils.showLong("没有找到两个明显的波峰");
            return;
        }
        //波峰
        List<Integer> peaks = result.get("peaks");
        if (peaks == null || peaks.size() < 4) {
            Logger.e("PeaksAndValley isEmpty3");
            return;
        }
        //波谷
        List<Integer> valleys = result.get("valleys");
        if (valleys == null || valleys.size() < 3) {
            Logger.e("PeaksAndValley isEmpty2");
            return;
        }
        //
        StringBuilder sbd = new StringBuilder();
       // sbd.append("点1").append(": 索引=").append(peaks.get(0)).append(", 值=").append(data[peaks.get(0)]).append("\n");
        sbd.append("点1").append(": 索引=").append(valleys.get(0)).append(", 值=").append(data[valleys.get(0)]).append("\n");
        sbd.append("点2").append(": 索引=").append(peaks.get(1)).append(", 值=").append(data[peaks.get(1)]).append("\n");
        //点三是点2和点4的中点
        int point3Index = (peaks.get(1) + valleys.get(1)) / 2;
        sbd.append("点3").append(": 索引=").append(point3Index).append(", 值=").append(data[point3Index]).append("\n");
        sbd.append("点4").append(": 索引=").append(valleys.get(1)).append(", 值=").append(data[valleys.get(1)]).append("\n");
        // 点5是点4和点6的中点
        int point5Index = (peaks.get(2) + valleys.get(1)) / 2;
        sbd.append("点5").append(": 索引=").append(point5Index).append(", 值=").append(data[point5Index]).append("\n");
        sbd.append("点6").append(": 索引=").append(peaks.get(2)).append(", 值=").append(data[peaks.get(2)]).append("\n");
        sbd.append("点7").append(": 索引=").append(valleys.get(2)).append(", 值=").append(data[valleys.get(2)]).append("\n");
       // sbd.append("点7").append(": 索引=").append(peaks.get(3)).append(", 值=").append(data[peaks.get(3)]).append("\n");

//        //
//        for (int i = 0; i < valleys.size(); i++) {
//            int idx = valleys.get(i);
//            sbd.append("波谷 ").append(i + 1).append(": 索引=").append(idx).append(", 值=").append(data[idx]).append("\n");
//        }
        // data 取 peaks.get(0)到 valleys.get(3)之间的数据
        double[] data1 = Arrays.copyOfRange(data, peaks.get(0), peaks.get(3) + 1);
        showCurveLine(data);
        stringBuilder.append(sbd);
        double corrodedRebarDiameter = 123; // 腐蚀度
        double valley1 = data[valleys.get(0)];
        double valley2 = data[peaks.get(1)];
        double valley4 = data[valleys.get(1)];
        double valley6 = data[peaks.get(2)];
        double valley7 = data[valleys.get(2)];
        SteelCorrResult steelCorrResult =  calculate(valley1, valley2, valley4, valley6, valley7);
        CorrosionBean corrosionBean;
        if(!steelCorrResult.isCorroded()){
            stringBuilder.append("\n 无锈蚀");
            corrosionBean = new CorrosionBean(binding.etDistance.getText().toString().trim(),
                    "9",
                    "9",
                    "无锈蚀"
            );
            corrosionBeanList.add(corrosionBean);
        }
        else {
            stringBuilder.append("\n V17 =").append(steelCorrResult.getV17());
            stringBuilder.append("\n V26 =").append(steelCorrResult.getV26());
            stringBuilder.append("\n xRatio =").append(steelCorrResult.getxRatio());
            stringBuilder.append("\n  保护层厚度 =").append(steelCorrResult.getCoverThickness());
            stringBuilder.append("\n  腐蚀度 =").append(steelCorrResult.getCorrosionRate());
            stringBuilder.append("\n  modelAccuracy =").append(steelCorrResult.getModelAccuracy());
            corrodedRebarDiameter = steelCorrResult.getCorrosionRate() * 100;

        //Logger.e("锈蚀逻辑==== " + stringBuilder);
            corrosionBean = new CorrosionBean(binding.etDistance.getText().toString().trim(),
                "9",
                "9",
                StringUtils.getRoundingString(corrodedRebarDiameter, 3)
                );
            corrosionBeanList.add(corrosionBean);
        }
        //
        dataProcessorHandler.post(() -> {
            refreshGrid();// 刷新钢筋列表
            //binding.tvResult.setText(sbd);
            if (!StaticConstant.isRelease) {
                //binding.tvResult.setText(corrosionBean.toString());
                StyleAlertDialog2 styleAlertDialog = new StyleAlertDialog2(this);
                styleAlertDialog.setContent(stringBuilder).setOKButton("确定", v -> styleAlertDialog.dismiss()).show();
            }
        });
    }

    public void onViewClicked(View view) {
        OtherUtils.hideKeyboard(this);
        if (view.getId() == binding.btnDetect.getId())
        {
           // checkInfo();
            // Deleted:checkInfo();
            // 按下开始检测：校验保存参数，蓝牙数据开始缓存到 bufferUtils
            if (!checkInfo()) {
                return;
            }
            bufferUtils.clear();
            timerTimeVer = 0;
        }
        else if(view.getId() == binding.btnTempLading.getId())
        {
            isSaveData = false;
            tempGetData();
            tempLoading();
        }
        else if (view.getId() == binding.btnSelectFile.getId())
        {
            //弹出选择框，选择GTJ目录下的文件并加载显示
            showFileSelectDialog();
        }
        else if (view.getId() == binding.tvSteelDiameter.getId())
        {
            OptionsPickerViewUtils.showSingleColumnPickerView(this, "请选择", "", CorrosionCurve.poleNormList, binding.tvSteelDiameter.getText().toString(), position -> binding.tvSteelDiameter.setText(CorrosionCurve.poleNormList.get(position)));
        }
        else if (view.getId() == binding.btnFinish.getId())
        {
            if (zoneBean == null || corrosionBeanList.isEmpty()) {
                finish();
                return;
            }
            zoneBean.corrosionZoneBean.startTime = StringUtils.getTime();
            zoneBean.corrosionZoneBean.zoneCount = corrosionBeanList.size();
            zoneBean.corrosionZoneBean.strZoneList = new Gson().toJson(corrosionBeanList);
            GreenDaoHelper.getDaoSession(this).getZoneBeanDao().insertOrReplace(zoneBean);
            ToastUtils.showShort("保存成功");
            finish();
        }
        else if (view.getId() == binding.ivHor.getId() || view.getId() == binding.tvHor.getId())
        {
            //清空横向数据
            checkDataBeanListHor.clear();
            refreshCheckDataHor(false);
            setHorVer(1);
        }
        else if (view.getId() == binding.ivVer.getId() || view.getId() == binding.tvVer.getId())
        {
            //清空竖向数据
            //bufferUtils.clear();
            //if (checkDataBeanListHor.size() != 3) {
                //ToastUtils.showShort("请采集横向数据");
                return;
            //}
            //setHorVer(2);
        }
    }

    private void setHorVer(int hover) {
        horVer = hover;
        if (hover == 1) {
            binding.ivHor.setImageResource(R.mipmap.icon_single_selected);
            binding.tvHor.setTextColor(ContextCompat.getColor(this, R.color.color006F5F));
            binding.ivVer.setImageResource(R.mipmap.icon_single_unselected);
            binding.tvVer.setTextColor(ContextCompat.getColor(this, R.color.color999));
        } else if (hover == 2) {
            binding.ivHor.setImageResource(R.mipmap.icon_single_unselected);
            binding.tvHor.setTextColor(ContextCompat.getColor(this, R.color.color999));
            binding.ivVer.setImageResource(R.mipmap.icon_single_selected);
            binding.tvVer.setTextColor(ContextCompat.getColor(this, R.color.color006F5F));
        }
    }

    /**
     * 检查并验证检测所需的基本信息
     * 该方法获取界面输入参数，验证其完整性，保存到本地缓存，并切换界面状态以准备开始检测
     * 包括电杆类型、规范、尺寸参数等信息的获取与验证
     */
    private boolean checkInfo() {
        String strPoleType = binding.spPoleType.getSelectedItem() + "";  //类型
        String strPoleStandard = binding.spPoleStandard.getSelectedItem() + "";  //规范
        String strPoleHeight = binding.etPoleHeight.getText().toString().trim();  //长度
        String strPoleBottomDiameter = binding.etPoleBottomDiameter.getText().toString().trim();  //根径
        String strPoleTopDiameter = binding.etPoleTopDiameter.getText().toString().trim();  //稍径
        String strRebarDiameter = binding.tvSteelDiameter.getText().toString().trim();  //设计纵筋直径
        String strDistance = binding.etDistance.getText().toString().trim();  //距离根部

        // 验证所有必需参数是否已填写
        if (/*TextUtils.isEmpty(strPoleType) || TextUtils.isEmpty(strPoleStandard) ||*/ TextUtils.isEmpty(strPoleHeight) || TextUtils.isEmpty(strPoleBottomDiameter) || TextUtils.isEmpty(strPoleTopDiameter) /*|| TextUtils.isEmpty(strRebarDiameter) */|| TextUtils.isEmpty(strDistance)) {
            ToastUtils.showShort("请填写完整");
           // return;
            return false;
        }

        // 保存参数到zoneBean对象
        zoneBean.corrosionZoneBean.poleType = binding.spPoleType.getSelectedItemPosition();
        zoneBean.corrosionZoneBean.poleStandard = binding.spPoleStandard.getSelectedItemPosition();
        zoneBean.corrosionZoneBean.poleHeight = strPoleHeight;
        zoneBean.corrosionZoneBean.poleBottomDiameter = strPoleBottomDiameter;
        zoneBean.corrosionZoneBean.poleTopDiameter = strPoleTopDiameter;
        zoneBean.corrosionZoneBean.Distance = strDistance;
        zoneBean.corrosionZoneBean.rebarDiameter ="";// strRebarDiameter;

        // 将配置信息保存到本地缓存
        SPUtils.getInstance().put(StaticConstant.SP_CORROSION_LAST_CONFIG, new Gson().toJson(zoneBean.corrosionZoneBean));

        // 设置参数为不可更改状态
        setParamNotChange();

        // 切换界面布局，隐藏基础配置区域，显示开始检测区域
        binding.layoutBase.setVisibility(View.GONE);// 隐藏基础配置区域
        binding.btnDetect.setVisibility(View.GONE); // 隐藏开始检测按钮
        binding.layoutStart.setVisibility(View.INVISIBLE); // 显示开始检测区域
        //
        //setHorVer(1); // 设置初始为横向
        return true;
    }

    @SuppressLint("SetTextI18n")
    private void configGrid(ViewGridContentBinding gridContentBinding, int position) {
        gridContentBinding.tvGridNumber.setText(position + 1 + "");
        gridContentBinding.tvGrid1.setText(corrosionBeanList.get(position).distance);
        gridContentBinding.tvGrid2.setText(corrosionBeanList.get(position).diameter);
        gridContentBinding.tvGrid2.setVisibility(View.GONE);
        gridContentBinding.tvGrid3.setText(corrosionBeanList.get(position).strCheckArea);
        gridContentBinding.tvGrid3.setVisibility(View.GONE);
        gridContentBinding.tvGrid4.setText(corrosionBeanList.get(position).corrosion1);
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
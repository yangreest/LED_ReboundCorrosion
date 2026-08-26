package com.example.gtj_f230_rebound_corrosion.corrosion.activity.testing;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import androidx.core.content.ContextCompat;

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
import com.example.gtj_f230_rebound_corrosion.corrosion.storage.CorrosionCurve;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.CurveComparator;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.OptimizedDataProcessor;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.SignalArrayBufferUtils;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.curve.CurveComparatorNew;
import com.example.gtj_f230_rebound_corrosion.databinding.ActDetectBinding;
import com.example.gtj_f230_rebound_corrosion.databinding.ViewGridContentBinding;
import com.example.gtj_f230_rebound_corrosion.functions.database.greenDao.db.StandardCurveBeanDao;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.inuker.bluetooth.library.BluetoothManager;
import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressLint({"SetTextI18n", "DefaultLocale", "SetTextI18n"})
public class CorrosionDetectActivity extends BaseActivity<ActDetectBinding> {
    private MediaPlayer mediaPlayer;
    private ScheduledExecutorService executorService;
    private long timerTime;
    private String spMac;
    private boolean isCollectStop = true;
    private ZoneBean zoneBean;
    private final List<CorrosionBean> corrosionBeanList = new ArrayList<>();
    private final List<CheckDataBean> checkDataList = new ArrayList<>();
    private StyleAlertDialog2 styleAlertDialog;
    private OptimizedDataProcessor dataProcessor;
    private Handler dataProcessorHandler;
    private SignalArrayBufferUtils signalArrayUtils;

    @Override
    protected ActDetectBinding getBinding() {
        return ActDetectBinding.inflate(getLayoutInflater());
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
        binding.btnCalibration.setVisibility(StaticConstant.isRelease ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void initView() {
        initCorrosionCurve();
        mediaPlayer = CustomMediaPlayer.getMediaPlayer(null);
        styleAlertDialog = new StyleAlertDialog2(this);
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
        binding.btnDetect.setOnClickListener(this::onViewClicked);
        binding.btnReturn.setOnClickListener(this::onViewClicked);
        binding.btnCalibration.setOnClickListener(this::onViewClicked);
        binding.btnLookCurve1.setOnClickListener(this::onViewClicked);
        binding.tvSteelDiameter.setOnClickListener(this::onViewClicked);
        spMac = SPUtils.getInstance().getString(StaticConstant.SP_CORROSION_BLE_MAC);
        signalArrayUtils = new SignalArrayBufferUtils();
        signalArrayUtils.setSize(64);
        initExecutor();
        initBluetooth();
        initOptimizedDataProcessor();
        refreshGrid();
    }

    private void initExecutor() {
        Handler handlerPost = new Handler();
        executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleWithFixedDelay(() -> handlerPost.post(() -> timerTime++), 0, 1, TimeUnit.SECONDS);
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
        configCalibration();
    }

    //    private double calibration;
    private double poleRadius;  //电杆半径
    private double rebarDiameter;  //主筋直径

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
            rebarDiameter = Double.parseDouble(strRebarDiameter);
        } catch (NumberFormatException e) {
            return;
        }
        poleRadius = StringUtils.getInterpolationValue(0.001, poleBottomDiameter, x2, poleTopDiameter, distance) / 2;
        double a1 = poleRadius + 22;  //22：钢筋仪轱辘半径22mm
        double a2 = 52 / 2.0;
        double a3 = Math.sqrt(a1 * a1 - a2 * a2);
        double a4 = a3 - poleRadius;
//        calibration = StringUtils.getRounding(22 - a4, 2);
//        String strMessage = "根径：" + poleBottomDiameter + " , 稍径：" + poleTopDiameter + " , 长度：" + x2 + " , 检测点：" + distance + " , 计算直径：" + StringUtils.getRounding(poleRadius * 2, 1) + " , 补偿：" + calibration;
        //
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
            }

            @Override
            public void onResponse(int code) {
            }
        });
    }

    private void initOptimizedDataProcessor() {
        dataProcessorHandler = new Handler(Looper.getMainLooper());
        dataProcessor = new OptimizedDataProcessor((displacement, signal, frameData) -> {
            //
            if (isCollectStop) {
                return;
            }
            //超过2秒清空数据
            {
                if (timerTime >= 2) {
                    signalArrayUtils.clear();
                }
                timerTime = 0;
            }
            dataProcessorHandler.post(() -> binding.tvSignal.setText(displacement + " , " + signal));
            //
            //记录数据|获取最大值
            CheckDataBean maxBean = signalArrayUtils.putAndCheck(displacement, signal);
            //
            if (maxBean == null) {
                return;
            }
            //停止检测
            isCollectStop = true;
            CustomMediaPlayer.startSpeaking(this, mediaPlayer, "prompt");
            //
            configCalibration();
            computeResult(maxBean);
        });
    }

    /**
     * @param maxBean：钢筋数据
     */
    private boolean average(CheckDataBean maxBean) {
        checkDataList.add(maxBean);
        if (checkDataList.size() == 5) {
            double[] signalArray = new double[61];
            for (int i = 0; i < 61; i++) {
                double[] signals = new double[5];
                signals[0] = checkDataList.get(0).curve_y_original[i];
                signals[1] = checkDataList.get(1).curve_y_original[i];
                signals[2] = checkDataList.get(2).curve_y_original[i];
                signals[3] = checkDataList.get(3).curve_y_original[i];
                signals[4] = checkDataList.get(4).curve_y_original[i];
                //去掉一个最大，去掉一个最小，剩余三个取平均值
                Arrays.sort(signals);
                int sum = 0;
                sum += signals[1];
                sum += signals[2];
                sum += signals[3];
                signalArray[i] = sum / 3;
            }
            maxBean.curve_y_original = signalArray;
            maxBean.y = signalArray[30];
            checkDataList.clear();
            return true;
        } else {
            ToastUtils.showShort("已检测" + checkDataList.size() + "次");
            return false;
        }
    }

//    private void computeCurve(CheckDataBean maxBean) {
//        //计算厚度
//        List<CheckDataBean> standardList = new ArrayList<>();
//        standardList.add(getThickness("7", maxBean));
//        standardList.add(getThickness("9", maxBean));
//        standardList.add(getThickness("10", maxBean));
//        standardList.add(getThickness("11", maxBean));
//        standardList.add(getThickness("12", maxBean));
//        standardList.add(getThickness("14", maxBean));
//        standardList.add(getThickness("16", maxBean));
//        //修正曲线
//        for (CheckDataBean bean : standardList) {
//            correctedCurveData(bean);
//        }
//        StringBuilder sbd0 = new StringBuilder();
//        sbd0.append("检测波峰：").append(maxBean.y).append("电杆直径：").append(StringUtils.getRounding(radius * 2, 2)).append("弧度补偿：").append(calibration);
//        //计算变化率
//        CurveComparator.computeCorrelationAnalysis(standardList);
//        //60min
//        CheckDataBean min60 = Collections.min(standardList, (o1, o2) -> (int) (o1.correlation2[6] * 10000 - o2.correlation2[6] * 10000));
//        double rate60 = getRate(min60.correlation2[6]);
//        for (CheckDataBean bean : standardList) {
//            bean.correlation2[6] = StringUtils.getRounding(bean.correlation2[6] * rate60, 3);
//        }
//        //80min
//        CheckDataBean min80 = Collections.min(standardList, (o1, o2) -> (int) (o1.correlation3[6] * 10000 - o2.correlation3[6] * 10000));
//        double rate80 = getRate(min80.correlation3[6]);
//        for (CheckDataBean bean : standardList) {
//            bean.correlation3[6] = StringUtils.getRounding(bean.correlation3[6] * rate80, 3);
//        }
//        //
//        //排序最小mse即为最接近的直径、厚度
//        standardList.sort(Comparator.comparingDouble(o -> (o.leftMin + o.topMin + o.rightMin)));
//        //
//        StringBuilder sbd1 = new StringBuilder();
//        sbd1.append("\n\n信号：左中右");
//        for (CheckDataBean bean : standardList) {
//            sbd1.append(bean).append("\n");
//        }
//        //排序最小mse即为最接近的直径、厚度
//        standardList.sort(Comparator.comparingDouble(o -> (o.correlation2[6] + o.leftMin + o.topMin + o.rightMin)));
//        //
//        StringBuilder sbd2 = new StringBuilder();
//        sbd2.append("\n\n广角：60");
//        for (CheckDataBean bean : standardList) {
//            sbd2.append(bean).append("\n");
//        }
//        //排序最小mse即为最接近的直径、厚度
//        standardList.sort(Comparator.comparingDouble(o -> (o.correlation3[6] + o.leftMin + o.topMin + o.rightMin)));
//        //
//        StringBuilder sbd3 = new StringBuilder();
//        sbd3.append("\n\n广角：80");
//        for (CheckDataBean bean : standardList) {
//            sbd3.append(bean).append("\n");
//        }
//        CorrosionCurve.curveBeanList.clear();
//        for (int i = 0; i < 4; i++) {
//            CheckDataBean bean = standardList.get(i);
//            CorrosionCurve.curveBeanList.add(new StandardCurveBean(bean.poleNorm + "_std", StringUtils.getRoundingString(radius * 2, 1), bean.thickness, bean.curve_y_standard));
//            CorrosionCurve.curveBeanList.add(new StandardCurveBean(bean.poleNorm + "_new", StringUtils.getRoundingString(radius * 2, 1), bean.thickness, bean.curve_y_processed));
//        }
//        //
//        dataProcessorHandler.post(() -> {
//            //
//            showDialog(sbd0.toString(), sbd1.toString(), sbd2.toString(), sbd3.toString(), "");  //显示dialog
//            //-----------------
//            isCollectStop = false;  //继续检测
//        });
//    }

    private void computeResult(CheckDataBean maxBean) {
        double poleDiameter = poleRadius * 2;
        List<CheckDataBean> standardList = new ArrayList<>();
        //钢筋直径7
        if (rebarDiameter >= 7) {
            if (poleDiameter <= 250) {
                standardList.add(CorrosionCurve.getSuitableIndex("7", maxBean, CorrosionCurve.curve_7_250));
            } else if (poleDiameter <= 310) {
                standardList.add(CorrosionCurve.getSuitableIndex("7", poleDiameter, maxBean, CorrosionCurve.curve_7_250, CorrosionCurve.curve_7_310));
            } else if (poleDiameter <= 350) {
                standardList.add(CorrosionCurve.getSuitableIndex("7", poleDiameter, maxBean, CorrosionCurve.curve_7_310, CorrosionCurve.curve_7_350));
            } else if (poleDiameter <= 400) {
                standardList.add(CorrosionCurve.getSuitableIndex("7", poleDiameter, maxBean, CorrosionCurve.curve_7_350, CorrosionCurve.curve_7_400));
            } else if (poleDiameter <= 450) {
                standardList.add(CorrosionCurve.getSuitableIndex("7", poleDiameter, maxBean, CorrosionCurve.curve_7_400, CorrosionCurve.curve_7_450));
            } else {
                standardList.add(CorrosionCurve.getSuitableIndex("7", maxBean, CorrosionCurve.curve_7_450));
            }
        }
        //钢筋直径9
        if (rebarDiameter >= 9) {
            if (poleDiameter <= 250) {
                standardList.add(CorrosionCurve.getSuitableIndex("9", maxBean, CorrosionCurve.curve_9_250));
            } else if (poleDiameter <= 310) {
                standardList.add(CorrosionCurve.getSuitableIndex("9", poleDiameter, maxBean, CorrosionCurve.curve_9_250, CorrosionCurve.curve_9_310));
            } else if (poleDiameter <= 350) {
                standardList.add(CorrosionCurve.getSuitableIndex("9", poleDiameter, maxBean, CorrosionCurve.curve_9_310, CorrosionCurve.curve_9_350));
            } else if (poleDiameter <= 400) {
                standardList.add(CorrosionCurve.getSuitableIndex("9", poleDiameter, maxBean, CorrosionCurve.curve_9_350, CorrosionCurve.curve_9_400));
            } else if (poleDiameter <= 450) {
                standardList.add(CorrosionCurve.getSuitableIndex("9", poleDiameter, maxBean, CorrosionCurve.curve_9_400, CorrosionCurve.curve_9_450));
            } else {
                standardList.add(CorrosionCurve.getSuitableIndex("9", maxBean, CorrosionCurve.curve_9_450));
            }
        }
        //钢筋直径10
        if (rebarDiameter >= 10) {
            if (poleDiameter <= 250) {
                standardList.add(CorrosionCurve.getSuitableIndex("10", maxBean, CorrosionCurve.curve_10_250));
            } else if (poleDiameter <= 310) {
                standardList.add(CorrosionCurve.getSuitableIndex("10", poleDiameter, maxBean, CorrosionCurve.curve_10_250, CorrosionCurve.curve_10_310));
            } else if (poleDiameter <= 350) {
                standardList.add(CorrosionCurve.getSuitableIndex("10", poleDiameter, maxBean, CorrosionCurve.curve_10_310, CorrosionCurve.curve_10_350));
            } else if (poleDiameter <= 400) {
                standardList.add(CorrosionCurve.getSuitableIndex("10", poleDiameter, maxBean, CorrosionCurve.curve_10_350, CorrosionCurve.curve_10_400));
            } else if (poleDiameter <= 450) {
                standardList.add(CorrosionCurve.getSuitableIndex("10", poleDiameter, maxBean, CorrosionCurve.curve_10_400, CorrosionCurve.curve_10_450));
            } else {
                standardList.add(CorrosionCurve.getSuitableIndex("10", maxBean, CorrosionCurve.curve_10_450));
            }
        }
        //钢筋直径11
        if (rebarDiameter >= 11) {
            if (poleDiameter <= 250) {
                standardList.add(CorrosionCurve.getSuitableIndex("11", maxBean, CorrosionCurve.curve_11_250));
            } else if (poleDiameter <= 310) {
                standardList.add(CorrosionCurve.getSuitableIndex("11", poleDiameter, maxBean, CorrosionCurve.curve_11_250, CorrosionCurve.curve_11_310));
            } else if (poleDiameter <= 350) {
                standardList.add(CorrosionCurve.getSuitableIndex("11", poleDiameter, maxBean, CorrosionCurve.curve_11_310, CorrosionCurve.curve_11_350));
            } else if (poleDiameter <= 400) {
                standardList.add(CorrosionCurve.getSuitableIndex("11", poleDiameter, maxBean, CorrosionCurve.curve_11_350, CorrosionCurve.curve_11_400));
            } else if (poleDiameter <= 450) {
                standardList.add(CorrosionCurve.getSuitableIndex("11", poleDiameter, maxBean, CorrosionCurve.curve_11_400, CorrosionCurve.curve_11_450));
            } else {
                standardList.add(CorrosionCurve.getSuitableIndex("11", maxBean, CorrosionCurve.curve_11_450));
            }
        }
        //钢筋直径14
        if (rebarDiameter >= 14) {
            if (poleDiameter <= 250) {
                standardList.add(CorrosionCurve.getSuitableIndex("14", maxBean, CorrosionCurve.curve_14_250));
            } else if (poleDiameter <= 310) {
                standardList.add(CorrosionCurve.getSuitableIndex("14", poleDiameter, maxBean, CorrosionCurve.curve_14_250, CorrosionCurve.curve_14_310));
            } else if (poleDiameter <= 350) {
                standardList.add(CorrosionCurve.getSuitableIndex("14", poleDiameter, maxBean, CorrosionCurve.curve_14_310, CorrosionCurve.curve_14_350));
            } else if (poleDiameter <= 400) {
                standardList.add(CorrosionCurve.getSuitableIndex("14", poleDiameter, maxBean, CorrosionCurve.curve_14_350, CorrosionCurve.curve_14_400));
            } else if (poleDiameter <= 450) {
                standardList.add(CorrosionCurve.getSuitableIndex("14", poleDiameter, maxBean, CorrosionCurve.curve_14_400, CorrosionCurve.curve_14_450));
            } else {
                standardList.add(CorrosionCurve.getSuitableIndex("14", maxBean, CorrosionCurve.curve_14_450));
            }
        }
//        changeRate(standardList, maxBean);

        //----------------------------------综合比较
        maxBean.curve_y_processed1 = new ArrayList<>();
        for (double v : maxBean.signalMovAve) {
            maxBean.curve_y_processed1.add(v);
        }
        List<List<Double>> curveCollection = new ArrayList<>();
        for (CheckDataBean bean : standardList) {
            List<Double> list = new ArrayList<>();
            for (double v : bean.signalMovAve) {
                list.add(v);
            }
            curveCollection.add(list);
        }
        CurveComparatorNew.DTWConfig dtwConfig = new CurveComparatorNew.DTWConfig();
        CurveComparatorNew.EuclideanConfig euclidConfig = new CurveComparatorNew.EuclideanConfig();
        CurveComparatorNew.CorrelationConfig corrConfig = new CurveComparatorNew.CorrelationConfig();
        StringBuilder sbd1 = new StringBuilder();
        Logger.e("---综合比较结果: start");
        for (List<Double> curve : curveCollection) {
            CurveComparatorNew.ComparisonResult result = CurveComparatorNew.compareCurves(maxBean.curve_y_processed1, curve, dtwConfig, euclidConfig, corrConfig);
            Logger.e("---综合比较结果: " + result);
            sbd1.append("\n综合比较结果: ").append(result);
        }
        // 4. 在集合中查找最相似曲线
        int mostSimilarIndex = CurveComparatorNew.findMostSimilarCurve(maxBean.curve_y_processed1, curveCollection);
        Logger.e("---最相似的曲线索引: " + mostSimilarIndex);
        sbd1.append("\n最相似的曲线索引: ").append(mostSimilarIndex);
        Logger.e("---综合比较结果: end");
        //----------------------------------综合比较
        //
        CheckDataBean checkDataBeanResult = standardList.get(0);
        if (mostSimilarIndex >= 0) {
            checkDataBeanResult = standardList.get(mostSimilarIndex);
        }
        StringBuilder sbdTitle = new StringBuilder();
        sbdTitle.append("检测波峰：").append(maxBean.signalMovAve[30]).append("电杆直径：").append(StringUtils.getRounding(poleRadius * 2, 2));
        //
        sbdTitle.append("\n").append(maxBean.poleNorm).append(", ").append(maxBean.thickness).append(", ").append(maxBean.signalMovAve[30]);
        for (CheckDataBean bean : standardList) {
            sbdTitle.append("\n").append(bean.poleNorm).append(", ").append(bean.thickness).append(", ").append(bean.signalMovAve[30]);
        }
        sbdTitle.append("\n\n检测结果---钢筋直径：").append(checkDataBeanResult.poleNorm);
        sbdTitle.append("\n检测结果---厚　　度：").append(checkDataBeanResult.thickness);
        //
        dataProcessorHandler.post(() -> {
            //
            showDialog(sbdTitle.toString(), sbd1.toString(), "", "", "", "", "");  //显示dialog
            //-----------------
//            String result = "钢筋直径：" + checkDataBean.poleNorm + ", 厚度：" + checkDataBean.thickness;
            //
//            binding.tvResult.setText(result);
            //存储
//            CorrosionBean corrosionBean = new CorrosionBean(binding.etDistance.getText().toString().trim(), zoneBean.corrosionZoneBean.rebarDiameter, checkDataBean.poleNorm);
//            corrosionBeanList.add(corrosionBean);
//            refreshGrid();
            //--------------------
            isCollectStop = false;  //继续检测
        });
    }

    /**
     * 变化率
     */
    private void changeRate(List<CheckDataBean> standardList, CheckDataBean maxBean) {
        //计算变化率
        CurveComparator.computeCorrelationAnalysis(standardList, maxBean);
        //
        {
            //广角 10
            CheckDataBean wideAngle = Collections.min(standardList, (o1, o2) -> (int) (o1.correlation1[6] * 10000 - o2.correlation1[6] * 10000));
            double rate = getRate(wideAngle.correlation1[6]);
            for (CheckDataBean bean : standardList) {
                bean.correlation1[6] = StringUtils.getRounding(bean.correlation1[6] * rate, 3);
            }
        }
        {
            //广角 15
            CheckDataBean wideAngle = Collections.min(standardList, (o1, o2) -> (int) (o1.correlation2[6] * 10000 - o2.correlation2[6] * 10000));
            double rate = getRate(wideAngle.correlation2[6]);
            for (CheckDataBean bean : standardList) {
                bean.correlation2[6] = StringUtils.getRounding(bean.correlation2[6] * rate, 3);
            }
        }
        {
            //广角 20
            CheckDataBean wideAngle = Collections.min(standardList, (o1, o2) -> (int) (o1.correlation3[6] * 10000 - o2.correlation3[6] * 10000));
            double rate = getRate(wideAngle.correlation3[6]);
            for (CheckDataBean bean : standardList) {
                bean.correlation3[6] = StringUtils.getRounding(bean.correlation3[6] * rate, 3);
            }
        }
        Map<String, Integer> sortHashMap = new HashMap<>();
        //
        //排序：左中右
        standardList.sort(Comparator.comparingDouble(o -> (o.leftMin + o.topMin + o.rightMin)));
        CheckDataBean resu = standardList.get(0);
        for (CheckDataBean dataBean : standardList) {
            if (rebarDiameter == Double.parseDouble(dataBean.poleNorm)) {
                resu = dataBean;
                break;
            }
        }
        //
        StringBuilder sbdLTR = new StringBuilder("\n\n");
        sbdLTR.append("\n\n信号：左中右");
        for (int i = 0; i < standardList.size(); i++) {
            sbdLTR.append(standardList.get(i).toString1());
            sortHashMap.put(standardList.get(i).poleNorm, i + 1);
        }
        //
        //排序：广角10
        standardList.sort(Comparator.comparingDouble(o -> (o.correlation1[6])));
        //
        StringBuilder sbd1 = new StringBuilder("\n\n");
        sbd1.append("\n\n广角：10");
        for (int i = 0; i < standardList.size(); i++) {
            sbd1.append(standardList.get(i).toString1());
            Integer number = sortHashMap.get(standardList.get(i).poleNorm);
            sortHashMap.put(standardList.get(i).poleNorm, i + 1 + number);
        }
        //
        //排序：广角15
        standardList.sort(Comparator.comparingDouble(o -> (o.correlation2[6])));
        //
        StringBuilder sbd2 = new StringBuilder("\n\n");
        sbd2.append("\n\n广角：15");
        for (int i = 0; i < standardList.size(); i++) {
            sbd2.append(standardList.get(i).toString1());
            Integer number = sortHashMap.get(standardList.get(i).poleNorm);
            sortHashMap.put(standardList.get(i).poleNorm, i + 1 + number);
        }
        //
        //排序：广角20
        standardList.sort(Comparator.comparingDouble(o -> (o.correlation3[6])));
        //
        StringBuilder sbd3 = new StringBuilder("\n\n");
        sbd3.append("\n\n广角：20");
        for (int i = 0; i < standardList.size(); i++) {
            sbd3.append(standardList.get(i).toString1());
            Integer number = sortHashMap.get(standardList.get(i).poleNorm);
            sortHashMap.put(standardList.get(i).poleNorm, i + 1 + number);
        }
        StringBuilder sbdSort = new StringBuilder("\n\n");
        for (Map.Entry<String, Integer> entry : sortHashMap.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            sbdSort.append("直径 ").append(key).append(": ").append(value).append("\n");
        }
        CheckDataBean checkDataBean = resu;
        //
        StringBuilder sbd4 = new StringBuilder("\n\n");
        sbd4.append("\n钢筋保护层厚度：").append(checkDataBean.thickness);
        dataProcessorHandler.post(() -> {
            showDialog("", sbdLTR.toString(), sbd1.toString(), sbd2.toString(), sbd3.toString(), sbdSort.toString(), sbd4.toString());  //显示dialog
        });
    }

    private void showDialog(String str0, String sbdLTR, String str1, String str2, String str3, String strSort, String str4) {
        if (!StaticConstant.isRelease) {
            styleAlertDialog.setContent(str0 + sbdLTR + str1 + str2 + str3 + strSort + str4).setOKButton("确定", v -> styleAlertDialog.dismiss()).show();
        }
    }

    private double getRate(double min) {
        double temp = min;
        for (int i = 0; i < 10; i++) {
            if (temp < 10) {
                temp *= 10;
            }
        }
        return temp / min;
    }

    /**
     * 刷新钢筋列表
     */
    private void refreshGrid() {
        binding.layoutGridContent.removeAllViews();
        int forNum;
        if (corrosionBeanList == null || corrosionBeanList.isEmpty() || corrosionBeanList.size() < 10) {
            forNum = 10;
        } else {
            forNum = corrosionBeanList.size();
        }
        for (int i = 0; i < forNum; i++) {
            ViewGridContentBinding gridContentBinding = ViewGridContentBinding.inflate(getLayoutInflater());
            View itemView = gridContentBinding.getRoot();
            if (corrosionBeanList != null && corrosionBeanList.size() > i) {
                configGrid(gridContentBinding, i);
                itemView.setId(i);
                itemView.setOnLongClickListener(gridItemView);
            }
            binding.layoutGridContent.addView(gridContentBinding.getRoot());
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

    public void onViewClicked(View view) {
        OtherUtils.hideKeyboard(this);
        if (view.getId() == binding.btnCalibration.getId()) {
            startActivity(new Intent(this, CalibrationCurveListActivity.class));
        } else if (view.getId() == binding.btnLookCurve1.getId()) {
            startActivity(new Intent(this, CalibrationCurveListActivity1.class));
        } else if (view.getId() == binding.btnDetect.getId()) {
            checkInfo();
        } else if (view.getId() == binding.tvSteelDiameter.getId()) {
            OptionsPickerViewUtils.showSingleColumnPickerView(this, "请选择", "", CorrosionCurve.poleNormList, binding.tvSteelDiameter.getText().toString(), position -> binding.tvSteelDiameter.setText(CorrosionCurve.poleNormList.get(position)));
        } else if (view.getId() == R.id.btn_return) {
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
    }

    private void checkInfo() {
        String strPoleType = binding.spPoleType.getSelectedItem() + "";  //类型
        String strPoleStandard = binding.spPoleStandard.getSelectedItem() + "";  //规范
        String strPoleHeight = binding.etPoleHeight.getText().toString().trim();  //长度
        String strPoleBottomDiameter = binding.etPoleBottomDiameter.getText().toString().trim();  //根径
        String strPoleTopDiameter = binding.etPoleTopDiameter.getText().toString().trim();  //稍径
        String strRebarDiameter = binding.tvSteelDiameter.getText().toString().trim();  //设计纵筋直径
        String strDistance = binding.etDistance.getText().toString().trim();  //距离根部
        if (TextUtils.isEmpty(strPoleType) || TextUtils.isEmpty(strPoleStandard) || TextUtils.isEmpty(strPoleHeight) || TextUtils.isEmpty(strPoleBottomDiameter) || TextUtils.isEmpty(strPoleTopDiameter) || TextUtils.isEmpty(strRebarDiameter) || TextUtils.isEmpty(strDistance)) {
            ToastUtils.showShort("请填写完整");
            return;
        }
        isCollectStop = false;
        zoneBean.corrosionZoneBean.poleType = binding.spPoleType.getSelectedItemPosition();
        zoneBean.corrosionZoneBean.poleStandard = binding.spPoleStandard.getSelectedItemPosition();
        zoneBean.corrosionZoneBean.poleHeight = strPoleHeight;
        zoneBean.corrosionZoneBean.poleBottomDiameter = strPoleBottomDiameter;
        zoneBean.corrosionZoneBean.poleTopDiameter = strPoleTopDiameter;
        zoneBean.corrosionZoneBean.rebarDiameter = strRebarDiameter;
        SPUtils.getInstance().put(StaticConstant.SP_CORROSION_LAST_CONFIG, new Gson().toJson(zoneBean.corrosionZoneBean));
        setParamNotChange();
    }

    @SuppressLint("SetTextI18n")
    private void configGrid(ViewGridContentBinding gridContentBinding, int position) {
        gridContentBinding.tvGridNumber.setText(position + 1 + "");
        gridContentBinding.tvGrid1.setText(corrosionBeanList.get(position).distance);
        gridContentBinding.tvGrid2.setText(corrosionBeanList.get(position).diameter);
        gridContentBinding.tvGrid3.setText(corrosionBeanList.get(position).corrosion);
    }

    private void initCorrosionCurve() {
        StandardCurveBeanDao curveDao = GreenDaoHelper.getDaoSession(this).getStandardCurveBeanDao();
        //钢筋直径7
        String rebarDiameter = "7";
        CorrosionCurve.curve_7_200 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("200")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_7_250 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("250")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_7_310 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("310")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_7_350 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("350")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_7_400 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("400")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_7_450 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("450")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_7_500 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("500")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        //钢筋直径9
        rebarDiameter = "9";
        CorrosionCurve.curve_9_200 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("200")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_9_250 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("250")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_9_310 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("310")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_9_350 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("350")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_9_400 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("400")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_9_450 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("450")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_9_500 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("500")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        //钢筋直径10
        rebarDiameter = "10";
        CorrosionCurve.curve_10_200 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("200")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_10_250 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("250")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_10_310 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("310")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_10_350 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("350")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_10_400 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("400")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_10_450 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("450")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_10_500 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("500")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        //钢筋直径11
        rebarDiameter = "11";
        CorrosionCurve.curve_11_200 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("200")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_11_250 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("250")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_11_310 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("310")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_11_350 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("350")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_11_400 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("400")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_11_450 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("450")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_11_500 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("500")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        //钢筋直径12
        rebarDiameter = "12";
        CorrosionCurve.curve_12_200 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("200")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_12_250 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("250")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_12_310 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("310")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_12_350 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("350")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_12_400 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("400")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_12_450 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("450")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_12_500 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("500")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        //钢筋直径14
        rebarDiameter = "14";
        CorrosionCurve.curve_14_200 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("200")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_14_250 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("250")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_14_310 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("310")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_14_350 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("350")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_14_400 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("400")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_14_450 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("450")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_14_500 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("500")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        //钢筋直径16
        rebarDiameter = "16";
        CorrosionCurve.curve_16_200 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("200")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_16_250 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("250")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_16_310 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("310")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_16_350 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("350")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_16_400 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("400")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_16_450 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("450")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        CorrosionCurve.curve_16_500 = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq("500")).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
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
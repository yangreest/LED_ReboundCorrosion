package com.example.gtj_f230_rebound_corrosion.thickness.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.base.activity.BaseActivity;
import com.example.gtj_f230_rebound_corrosion.base.model.ZoneBean;
import com.example.gtj_f230_rebound_corrosion.base.storage.GreenDaoHelper;
import com.example.gtj_f230_rebound_corrosion.base.utils.CustomMediaPlayer;
import com.example.gtj_f230_rebound_corrosion.base.utils.OtherUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.StringUtils;
import com.example.gtj_f230_rebound_corrosion.base.view.StyleAlertDialog;
import com.example.gtj_f230_rebound_corrosion.base.view.StyleAlertDialog2;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.CheckDataBean;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.RebarBean;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.RebarMinMaxBean;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.OptimizedDataProcessor;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.SignalArrayBufferUtils;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.fittingcurve.ComputeUtils;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.lowpassfilter.Optimized10ElementTrimmedMeanUtils;
import com.example.gtj_f230_rebound_corrosion.databinding.ActDetectThicknessBinding;
import com.example.gtj_f230_rebound_corrosion.functions.database.greenDao.db.RebarMinMaxBeanDao;
import com.example.gtj_f230_rebound_corrosion.thickness.model.Rebar240ZoneBean;
import com.example.gtj_f230_rebound_corrosion.thickness.model.RebarPageBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.inuker.bluetooth.library.BluetoothManager;
import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressLint({"InflateParams", "SetTextI18n", "UseCompatLoadingForDrawables"})
public class DetectActivity extends BaseActivity<ActDetectThicknessBinding> {
    private int color006F5F, color8E8E8E, colorFFF, color000, colorFF9600;
    private ZoneBean zoneBean;
    private Rebar240ZoneBean rebarZoneBean, cloneRebarZoneBean;
    private String spMac;
    private MediaPlayer mediaPlayer;
    private List<Integer> calibrationStandardList = new ArrayList<>();
    private List<Integer> calibrationList = new ArrayList<>();
    private ScheduledExecutorService scheduledExecutorService;
    private OptimizedDataProcessor dataProcessor;
    private Handler dataProcessorHandler;
    private Optimized10ElementTrimmedMeanUtils meanUtils;  //平均信号
    private SignalArrayBufferUtils signalArrayUtils;
    private final List<CheckDataBean> checkDataList = new ArrayList<>();
    private double thicknessMin, thicknessMax;
    private RebarMinMaxBeanDao minMaxBeanDao;

    @Override
    protected ActDetectThicknessBinding getBinding() {
        return ActDetectThicknessBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initLabel() {
        binding.navigationBar.setTitleText("单点检测");
        initBatteryReceiver(binding.navigationBar);
        initDeviceStatusExecutor(binding.navigationBar);
        setHideKeyboard();
    }

    @Override
    protected void initView() {
        color006F5F = getColor(R.color.color006F5F);
        color8E8E8E = getColor(R.color.color8E8E8E);
        colorFFF = getColor(R.color.colorFFF);
        color000 = getColor(R.color.color000);
        colorFF9600 = getColor(R.color.colorFF9600);
        binding.ivHor.setOnClickListener(this::onViewClicked);
        binding.ivVer.setOnClickListener(this::onViewClicked);
        binding.tvHor.setOnClickListener(this::onViewClicked);
        binding.tvVer.setOnClickListener(this::onViewClicked);
        binding.btnReset.setOnClickListener(this::onViewClicked);
        binding.btnCheck.setOnClickListener(this::onViewClicked);
        binding.btnSetting.setOnClickListener(this::onViewClicked);
        binding.btnSave.setOnClickListener(this::onViewClicked);
        binding.btnFinish.setOnClickListener(this::onViewClicked);
        //
        binding.tvTabDetection.setOnClickListener(this::onViewClicked);
        binding.tvTabForecast.setOnClickListener(this::onViewClicked);
        binding.btnForecast.setOnClickListener(this::onViewClicked);
        //
        minMaxBeanDao = GreenDaoHelper.getDaoSession(this).getRebarMinMaxBeanDao();
        spMac = SPUtils.getInstance().getString(StaticConstant.SP_CORROSION_BLE_MAC);
        meanUtils = new Optimized10ElementTrimmedMeanUtils();
        signalArrayUtils = new SignalArrayBufferUtils();
        signalArrayUtils.setSize(64);
        mediaPlayer = CustomMediaPlayer.getMediaPlayer(null);
        calibrationSignalValue();
        initBluetooth();
        initExecutor();
        initOptimizedDataProcessor();
        showView(0);
        binding.etDistance.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (binding.etDistance.isFocused()) {
                    binding.etDistance1.setText(s);
                }
            }
        });
        binding.etDistance1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (binding.etDistance1.isFocused()) {
                    binding.etDistance.setText(s);
                }
            }
        });
        setHorVer(1);
        zoneBean = (ZoneBean) getIntent().getSerializableExtra("ZoneBean");
        if (zoneBean == null) {
            return;
        }
        binding.navigationBar.setProjectName(zoneBean.projectName).setZoneName(zoneBean.number);
        binding.navigationBar.setTitleText("");
        //查询数据库或创建
    }

    private void calibrationSignalValue() {
        String strCalibrationStandardList = SPUtils.getInstance().getString("calibration-standard-list");
        calibrationStandardList = new Gson().fromJson(strCalibrationStandardList, new TypeToken<List<Integer>>() {
        }.getType());
        String strCalibrationList = SPUtils.getInstance().getString("calibration-list");
        calibrationList = new Gson().fromJson(strCalibrationList, new TypeToken<List<Integer>>() {
        }.getType());
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
        ToastUtils.showLong("连接成功，请检测");
    }

    private void initExecutor() {
        Handler handlerPost = new Handler();
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleWithFixedDelay(() -> handlerPost.post(() -> {
            if (checkType == 0) {
                timerTimeHor++;
                timerTimeVer++;
                if (timerTimeVer >= 2) {
                    treatVerThickness();
                }
            }
        }), 0, 1, TimeUnit.SECONDS);
    }

    private long timerTimeHor, timerTimeVer;
    private int cur_displacement;

    private void initOptimizedDataProcessor() {
        dataProcessorHandler = new Handler(Looper.getMainLooper());
        dataProcessor = new OptimizedDataProcessor((displacement, signal, frameData) -> {
            //校准信号
            int signalValue = getSignalValue(signal);
            //
            //平均信号
            int tempSignal = (int) meanUtils.addValue(signalValue);
            //
            if (checkType == 0) {
                checkThickness(displacement, tempSignal);
            } else {
                forecastDiameter(tempSignal);
            }
        });
    }

    private void checkThickness(int displacement, int signal) {
        if (horVer == 1) {  //横向
            treatHor(displacement, signal);
        } else {
            treatVer(signal);
        }
    }

    private void treatHor(int displacement, int signal) {
        if (cur_displacement == displacement) {
            return;
        }
        cur_displacement = displacement;
        //显示信号
        dataProcessorHandler.post(() -> binding.tvSignal.setText(cur_displacement + " , " + signal));
        //超过2秒清空数据
        {
            if (timerTimeHor >= 2) {
                signalArrayUtils.clear();
                checkDataList.clear();
                dataProcessorHandler.post(() -> refreshTvCheck(false));
            }
            timerTimeHor = 0;
        }
        //
        //记录数据|获取最大值
        CheckDataBean maxBean = signalArrayUtils.putAndCheck(displacement, signal);
        //
        if (maxBean == null) {
            return;
        }
        CustomMediaPlayer.startSpeaking(this, mediaPlayer, "prompt");
        if (rebarZoneBean == null) {
            ToastUtils.showShort("请进行参数设置");
            return;
        }
        if (rebarZoneBean.rebarPageList.isEmpty()) {
            ToastUtils.showShort("请添加检测部位");
            return;
        }
        RebarPageBean pageBean = rebarZoneBean.rebarPageList.get(rebarZoneBean.rebarPageList.size() - 1);
        if (!isAdd(pageBean)) {
            ToastUtils.showShort("请添加检测部位");
            return;
        }
        if (checkDataList.size() == 3) {
            checkDataList.clear();
        }
        //添加
        checkDataList.add(maxBean);
        dataProcessorHandler.post(() -> refreshTvCheck(true));
    }

    private long signalTime;

    private void treatVer(int signal) {
        //超过2秒清空数据
        {
            if (timerTimeVer >= 2) {
                valueMinT = 10000;
                valueMaxT = 0;
                signalTime = System.currentTimeMillis();
            }
            timerTimeVer = 0;
        }
        long tempTime = System.currentTimeMillis();
        if (tempTime - signalTime > 1000) {
            if (valueMinT > signal) {
                valueMinT = signal;
            }
            if (valueMaxT < signal) {
                valueMaxT = signal;
            }
        }
        //显示信号
        dataProcessorHandler.post(() -> {
            binding.tvSignal.setText(cur_displacement + " , " + signal);
            binding.tvMin.setText(valueMinT + "");
            binding.tvMax.setText(valueMaxT + "");
        });
    }

    private void treatVerThickness() {
        if (valueMinT == 10000) {
            valueMaxT = 0;
            return;
        }
        if (valueMinT == 0 || valueMaxT == 0) {
            ToastUtils.showShort("信号值为0");
            valueMinT = 10000;
            valueMaxT = 0;
            return;
        }
        //
        if (rebarZoneBean == null || rebarZoneBean.rebarPageList == null || rebarZoneBean.rebarPageList.isEmpty()) {
            return;
        }
        RebarPageBean pageBean = rebarZoneBean.rebarPageList.get(rebarZoneBean.rebarPageList.size() - 1);
        if (!isAdd(pageBean)) {
            valueMinT = 10000;
            valueMaxT = 0;
            ToastUtils.showShort("请添加检测部位");
            return;
        }
        if (checkDataList.size() < 3) {
            return;
        }
        double thickness = computeResult(valueMinT, pageBean.distance);
        //重置
        valueMinT = 10000;
        valueMaxT = 0;
        //存储
        RebarBean rebarBean = new RebarBean(pageBean.segment, thickness);
        rebarBean.thickness1 = StringUtils.getRounding(rebarBean.thickness, 0);
        pageBean.beanList.add(rebarBean);
        //求平均
        treatAverage(pageBean);
        //
        dataProcessorHandler.post(() -> {
            CustomMediaPlayer.startSpeaking(this, mediaPlayer, "prompt");
            binding.tvResult.setText("厚度：" + thickness + " mm, " + rebarSpacingMin + ", " + rebarSpacingMax);
            //刷新钢筋列表
            refreshGrid();
            //刷新按钮
            refreshButon(true, pageBean);
            //数据异常、剔凿验证
            if (thickness > thicknessMax || thickness < thicknessMin) {
                showErrorThickness();
            }
        });
    }

    private double rebarSpacingMin, rebarSpacingMax;

    private double computeResult(int signal, double distance) {
        //1. 主筋直径
        String strRebarDiameter = StaticConstant.rebarDiameterList.get(rebarZoneBean.rebarDiameter);
        double rebarDiameter = Double.parseDouble(strRebarDiameter);
        //
        //2. 主筋间距
        double left = (checkDataList.get(1).x - checkDataList.get(0).x) * 0.54;
        double right = (checkDataList.get(2).x - checkDataList.get(1).x) * 0.54;
        double min = Math.min(left, right);
        double max = Math.max(left, right);
        //
        double angleMin = min / (2 * Math.PI * poleRadius) * 360;
        double angleMax = max / (2 * Math.PI * poleRadius) * 360;
        double a = poleRadius - 17 - rebarDiameter / 2;
        rebarSpacingMin = (int) ComputeUtils.calculateThirdSide(a, a, angleMin);
        rebarSpacingMax = (int) ComputeUtils.calculateThirdSide(a, a, angleMax);
        double leftThickness, rightThickness;
        if (left < right) {
//            leftThickness = computeThickness(strRebarDiameter, rebarSpacingMin, rebarSpacingMin, checkDataList.get(0), false);
//            rightThickness = computeThickness(strRebarDiameter, rebarSpacingMax, rebarSpacingMax, checkDataList.get(2), false);
        } else {
//            rightThickness = computeThickness(strRebarDiameter, rebarSpacingMin, rebarSpacingMin, checkDataList.get(0), false);
//            leftThickness = computeThickness(strRebarDiameter, rebarSpacingMax, rebarSpacingMax, checkDataList.get(2), false);
        }
        double middleThickness = computeThickness(strRebarDiameter, distance, rebarSpacingMin, rebarSpacingMax, signal, false);
//        dataProcessorHandler.post(() -> {
//            StringBuilder sbd = new StringBuilder();
//            sbd.append("钢筋直径：").append(rebarDiameter);
//            sbd.append("，　电杆直径：").append(StringUtils.getRounding(poleRadius * 2, 0));
//            sbd.append("，\n主筋间距 Min：").append(StringUtils.getRounding(rebarSpacingMin, 0));
//            sbd.append("，　Max：").append(StringUtils.getRounding(rebarSpacingMax, 0));
//            sbd.append("，　电杆直径补偿：").append(poleCompensation);
//            sbd.append("\nleftThickness: ").append(leftThickness);
//            sbd.append("\nmiddleThickness: ").append(middleThickness);
//            sbd.append("\nrightThickness: ").append(rightThickness);
//            StyleAlertDialog2 styleAlertDialog = new StyleAlertDialog2(this);
//            styleAlertDialog.setContent(sbd).setOKButton("确定", v -> styleAlertDialog.dismiss()).show();
        return middleThickness;
    }

    private double computeThickness(String rebarDiameter, double distance, double spacingMin, double spacingMax, int signal, boolean isShowLog) {
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
        ComputeUtils.findMatchingSpaces(spaces, new double[]{53, 62});
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("=====\n");
        stringBuilder.append(spacingMin).append(" , ").append(spacingMax);
        for (int[] ints : rebarSpacings) {
            stringBuilder.append("\n间距: ").append(Arrays.toString(ints));
        }
        stringBuilder.append("\n=====");
        String strSpiralSpacing;
        if (distance > 2 && distance < Double.parseDouble(rebarZoneBean.poleHeight) - 2) {
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
                stringBuilder.append("\nku: ").append(bean.rebarSpacingMin).append(" , ").append(bean.rebarSpacingMax).append(" ~ ").append(temp.signalMin[0]);
            }
        }
        stringBuilder.append("\n=====");
        //计算厚度
        for (RebarMinMaxBean bean : rebarMinMaxBeanList) {
            int index = 0;
            if (bean.signalMin != null) {
                for (int i = 0; i < bean.signalMin.length; i++) {
                    if (signal >= bean.signalMin[i]) {
                        index = i;
                        bean.signal = bean.signalMin[i];
                        break;
                    }
                }
            }
            if (index == 0) {
                bean.thickness = 1;
            } else {
                bean.thickness = StringUtils.getRounding(StringUtils.getInterpolationValue(bean.signalMin[index - 1], index, bean.signalMin[index], index + 1, signal), 2);
            }
            stringBuilder.append("\nindex: ").append(index).append(", signal: ").append(bean.signal).append(" , ").append(signal).append(", thickness: ").append(bean.thickness);
        }
        //整合计算厚度
        double ths;
        StringBuilder sbdMessage = new StringBuilder();
        if (rebarMinMaxBeanList.size() >= 2) {  //2
            //主筋间距内插
            ths = StringUtils.getRounding((rebarMinMaxBeanList.get(0).thickness + rebarMinMaxBeanList.get(1).thickness) / 2d, 2);
//            double rebarSpacing0 = Double.parseDouble(rebarMinMaxBeanList.get(0).rebarSpacingMin);
//            double rebarSpacing1 = Double.parseDouble(rebarMinMaxBeanList.get(1).rebarSpacingMin);
//            ths = StringUtils.getRounding(StringUtils.getInterpolationValue(rebarSpacing0, , rebarSpacing1, rebarMinMaxBeanList.get(1).thickness, spacingMin), 2);
            sbdMessage.append("主筋间距NX：\n").append(rebarMinMaxBeanList.get(0).uniqueKey).append("　～　").append(rebarMinMaxBeanList.get(1).uniqueKey).append("　厚度：").append(ths);
        } else if (rebarMinMaxBeanList.size() == 1) {  //1
            ths = rebarMinMaxBeanList.get(0).thickness;
        } else {
            ths = 15;
        }
        double thickness = StringUtils.getRounding(ths + poleCompensation, 2);
        if (isShowLog) {
            dataProcessorHandler.post(() -> {
                StringBuilder sbd = new StringBuilder();
                sbd.append("钢筋直径：").append(rebarDiameter);
                sbd.append("，　电杆直径：").append(StringUtils.getRounding(poleRadius * 2, 0));
                sbd.append("，\n主筋间距 Min：").append(StringUtils.getRounding(spacingMin, 0));
                sbd.append("，　Max：").append(StringUtils.getRounding(spacingMax, 0));
                sbd.append("，\n箍筋间距：").append(strSpiralSpacing);
                sbd.append("，\n信号值：").append(signal);
                sbd.append("，\n电杆直径补偿：").append(poleCompensation);
                sbd.append("，\n结果：").append(thickness);
                sbd.append("\n\n\n");
                sbd.append("匹配结果: ");
                for (RebarMinMaxBean bean : rebarMinMaxBeanList) {
                    sbd.append("\n厚度: ").append(bean.thickness).append(",　查表: ").append(bean.uniqueKey);
                }
                if (rebarMinMaxBeanList.isEmpty()) {
                    sbd.append("\n匹配结果：无");
                }
                sbd.append("\n\n\n");
                sbd.append(sbdMessage);
                sbd.append("\n\n====\n").append(stringBuilder);
                StyleAlertDialog2 styleAlertDialog = new StyleAlertDialog2(this);
                styleAlertDialog.setContent(sbd).setOKButton("确定", v -> styleAlertDialog.dismiss()).show();
            });
        }
        return thickness;
    }

    private int valueMinT = 10000, valueMaxT;
    private int valueMinF = 10000, valueMaxF;

    private void forecastDiameter(int signal) {
        if (valueMinF > signal) {
            valueMinF = signal;
        }
        if (valueMaxF < signal) {
            valueMaxF = signal;
        }
        dataProcessorHandler.post(() -> {
            binding.tvForecastValue.setText("信号：" + signal);
            binding.tvForecastMin.setText("Min：" + valueMinF);
            binding.tvForecastMax.setText("Max：" + valueMaxF);
        });
    }


    private void showErrorThickness() {
        CustomMediaPlayer.startSpeaking(this, mediaPlayer, "prompt");
        new Handler().postDelayed(() -> CustomMediaPlayer.startSpeaking(this, mediaPlayer, "prompt"), 1000);
        StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this);
        styleAlertDialog.setContent("数据异常，请重复测量或剔凿验证").setOKButton("确定", v -> styleAlertDialog.dismiss()).show();
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

    private void treatAverage(RebarPageBean pageBean) {
        double total0 = 0, average0, total1 = 0, average1;
        double size = 0;
        if (!pageBean.beanList.isEmpty()) {
            for (RebarBean b : pageBean.beanList) {
                if (b.thickness > 0) {
                    total0 += b.thickness;
                    total1 += b.thickness1;
                    size++;
                }
            }
            average0 = StringUtils.getRounding(total0 / size, 1);
            average1 = StringUtils.getRounding(total1 / size, 1);
        } else {

            average0 = 0;
            average1 = 0;
        }
        pageBean.averageThickness = average0;
        pageBean.averageThickness1 = average1;
    }

    private boolean isAdd(RebarPageBean pageBean) {
        return pageBean.beanList.size() < rebarZoneBean.poleBottomCount;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK == resultCode) {
            assert data != null;
            rebarZoneBean = (Rebar240ZoneBean) data.getSerializableExtra("Rebar240ZoneBean");
            assert rebarZoneBean != null;
            if (rebarZoneBean.id == null) {
                cloneRebarZoneBean = null;
            }
            double designThickness = Double.parseDouble(rebarZoneBean.designThickness);
            //+8 -2 剔凿验证
            thicknessMin = StringUtils.getRounding(designThickness - 2, 1);
            thicknessMax = StringUtils.getRounding(designThickness + 8, 1);
            //+8 -2 剔凿验证
            thicknessMin = -1;
            thicknessMax = 100;
            //
            configGridTop(rebarZoneBean.poleBottomCount);
            refreshGrid();
        }
    }

    private double poleCompensation;  //电杆直径补偿
    private double poleRadius;

    private void configCalibration(String strDistance) {
        if (rebarZoneBean != null) {
            double tempDistance = Double.parseDouble(strDistance);
            poleRadius = StringUtils.getInterpolationValue(0.001, rebarZoneBean.poleBottomDiameter, Double.parseDouble(rebarZoneBean.poleHeight), rebarZoneBean.poleTopDiameter, tempDistance) / 2;
            double ac = poleRadius + 22;  //22：钢筋仪轱辘半径22mm
            double cd = 52 / 2.0;
            double ad = Math.sqrt(ac * ac - cd * cd);
            //电杆直径补偿
            poleCompensation = StringUtils.getRounding(ac - ad, 2);
        }
    }

    /**
     * 刷新钢筋列表
     */
    private void refreshGrid() {
        binding.layoutGridContent.removeAllViews();
        for (int i = 0; i < rebarZoneBean.rebarPageList.size(); i++) {
            View itemView = LayoutInflater.from(this).inflate(R.layout.view_grid_content_thickness, null, false);
            configGrid(itemView, rebarZoneBean.poleBottomCount, rebarZoneBean.rebarPageList.get(i));
            itemView.setId(i);
            itemView.setOnLongClickListener(gridItemView);
            binding.layoutGridContent.addView(itemView);
        }
    }

    /**
     * 删除
     */
    private final View.OnLongClickListener gridItemView = v -> {
        RebarPageBean pageBean = rebarZoneBean.rebarPageList.get(v.getId());
        StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this);
        styleAlertDialog.setContent("确定删除“" + pageBean.segment + "”的数据？").setLeftButton("取消", v1 -> styleAlertDialog.dismiss()).setRightButton("确定", v1 -> {
            styleAlertDialog.dismiss();
            rebarZoneBean.rebarPageList.remove(v.getId());
            //刷新钢筋列表
            refreshGrid();
            //刷新按钮
            if (!rebarZoneBean.rebarPageList.isEmpty()) {
                refreshButon(true, rebarZoneBean.rebarPageList.get(rebarZoneBean.rebarPageList.size() - 1));
            } else {
                refreshButon(false, null);
            }
        }).show();
        return false;
    };

    /**
     * 刷新检测按钮状态
     */
    private void refreshButon(boolean flag, RebarPageBean pageBean) {
        if (flag) {
            flag = pageBean.beanList.isEmpty();
        }
        binding.btnCheck.setEnabled(!flag);
        binding.btnCheck.setBackgroundResource(!flag ? R.drawable.bg_button_18 : R.drawable.bg_button_gray);
        binding.btnSetting.setEnabled(!flag);
        binding.btnSetting.setBackgroundResource(!flag ? R.drawable.bg_button_18 : R.drawable.bg_button_gray);
        binding.btnSave.setEnabled(!flag);
        binding.btnSave.setBackgroundResource(!flag ? R.drawable.bg_button_18 : R.drawable.bg_button_gray);
    }

    public void onViewClicked(View view) {
        OtherUtils.hideKeyboard(this);
        if (view.getId() == binding.btnCheck.getId()) {
            checkThickness();
        } else if (view.getId() == binding.btnSetting.getId()) {
            {
                Intent intent = new Intent(this, DetectConfigActivity.class);
                intent.putExtra("ZoneBean", rebarZoneBean);
                startActivityForResult(intent, 1);
            }
        } else if (view.getId() == binding.btnSave.getId()) {
            if (rebarZoneBean == null) {
                ToastUtils.showShort("请进行参数设置");
                return;
            }
            if (rebarZoneBean.rebarPageList.isEmpty()) {
                ToastUtils.showShort("请先检测数据");
                return;
            }
            rebarZoneBean.startTime = StringUtils.getTime();
            rebarZoneBean.id = GreenDaoHelper.getDaoSession(this).getRebar240ZoneBeanDao().insertOrReplace(rebarZoneBean);
            ToastUtils.showShort("保存成功");
            String strClone = new Gson().toJson(rebarZoneBean);
            cloneRebarZoneBean = new Gson().fromJson(strClone, new TypeToken<Rebar240ZoneBean>() {
            }.getType());
            int lastIndex = cloneRebarZoneBean.rebarPageList.size() - 1;
            int lastBeanListSize = cloneRebarZoneBean.rebarPageList.get(lastIndex).beanList.size();
            if (lastBeanListSize == 0) {
                cloneRebarZoneBean.rebarPageList.remove(lastIndex);
            }
            zoneBean.thicknessId = rebarZoneBean.id;
            zoneBean.thicknessCount = cloneRebarZoneBean.rebarPageList.size();
            GreenDaoHelper.getDaoSession(this).getZoneBeanDao().insertOrReplace(zoneBean);
            binding.btnSave.setEnabled(false);
            binding.btnSave.setBackgroundResource(R.drawable.bg_button_gray);
        } else if (view.getId() == binding.btnFinish.getId()) {
            String showText = getShowText();
            StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this);
            styleAlertDialog.setContent(showText).setLeftButton("取消", v1 -> styleAlertDialog.dismiss()).setRightButton("确定", v1 -> {
                styleAlertDialog.dismiss();
                finish();
            }).show();
        } else if (view.getId() == binding.tvTabDetection.getId()) {
            showView(0);
        } else if (view.getId() == binding.tvTabForecast.getId()) {
            showView(1);
        } else if (view.getId() == binding.btnForecast.getId()) {
            double currentDistance;
            {
                if (rebarZoneBean == null) {
                    ToastUtils.showShort("请进行参数设置");
                    return;
                }
                String strDistance = binding.etDistance1.getText().toString().trim();
                if (TextUtils.isEmpty(strDistance)) {
                    ToastUtils.showShort("请填写完整");
                    return;
                }
                currentDistance = Double.parseDouble(strDistance);
            }
            isForecastStart = !isForecastStart;
            if (isForecastStart) {
                binding.btnForecast.setText("结束");
                valueMinF = 10000;
                valueMaxF = 0;
            } else {
                binding.btnForecast.setText("开始");
                if (valueMinF == 0 || valueMaxF == 0) {
                    ToastUtils.showShort("信号值为0，无法预估");
                    return;
                }
                //预估直径
                double rate = StringUtils.getRounding(100.0 * (valueMaxF - valueMinF) / valueMinF, 2);
                String strForecast;
                boolean flag = currentDistance >= Double.parseDouble(rebarZoneBean.poleHeight) - 3;
                if (flag) {
                    strForecast = rate <= 20 ? "14" : "10";
                } else {
                    strForecast = rate <= 40 ? "14" : "10";
                }
                binding.tvForecast.setText(Html.fromHtml("<font color=#000000>预估直径(mm)：</font><font color=#006F5F>" + strForecast + "</font>"));
            }
        } else if (view.getId() == binding.ivHor.getId() || view.getId() == binding.tvHor.getId()) {
            //清空横向数据
            checkDataList.clear();
            refreshTvCheck(false);
            setHorVer(1);
        } else if (view.getId() == binding.ivVer.getId() || view.getId() == binding.tvVer.getId()) {
            if (checkDataList.size() == 3) {
                setHorVer(2);
            }
        }
    }

    private int horVer;  //1.横向，2.竖向

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

    private boolean isForecastStart;

    private void checkThickness() {
        if (rebarZoneBean == null) {
            ToastUtils.showShort("请进行参数设置");
            return;
        }
        if (rebarZoneBean.rebarPageList.size() >= 10) {
            ToastUtils.showShort("最多检测10个位置");
            return;
        }
        String strDistance = binding.etDistance.getText().toString().trim();
        if (TextUtils.isEmpty(strDistance)) {
            ToastUtils.showShort("请填写完整");
            return;
        }
        try {
            Double.parseDouble(strDistance);
        } catch (NumberFormatException ignored) {
            ToastUtils.showShort("请填写完整");
            return;
        }
        configCalibration(strDistance);
        String segment = "距根部" + strDistance + "m";
        rebarZoneBean.rebarPageList.add(new RebarPageBean(segment, Double.parseDouble(strDistance)));
        refreshGrid();
        binding.scrollView.post(() -> binding.scrollView.fullScroll(View.FOCUS_DOWN));
        //
        binding.btnCheck.setEnabled(false);
        binding.btnCheck.setBackgroundResource(R.drawable.bg_button_gray);
        binding.btnSetting.setEnabled(false);
        binding.btnSetting.setBackgroundResource(R.drawable.bg_button_gray);
        binding.btnSave.setEnabled(false);
        binding.btnSave.setBackgroundResource(R.drawable.bg_button_gray);
    }

    private int checkType;  //0:单点检测，1：预估直径

    private void showView(int position) {
        checkType = position;
        binding.tvTabDetection.setTextColor(color8E8E8E);
        binding.tvTabForecast.setTextColor(color8E8E8E);
        Typeface typefaceNormal = Typeface.create("sans-serif-bold", Typeface.NORMAL);
        binding.tvTabDetection.setTypeface(typefaceNormal);
        binding.tvTabForecast.setTypeface(typefaceNormal);
        binding.viewTabDetection.setBackgroundColor(colorFFF);
        binding.viewTabForecast.setBackgroundColor(colorFFF);
        binding.layoutDetection.setVisibility(View.GONE);
        binding.layoutForecast.setVisibility(View.GONE);
        Typeface typefaceBold = Typeface.create("sans-serif-bold", Typeface.BOLD);
        switch (position) {
            case 0:
                binding.tvTabDetection.setTextColor(color006F5F);
                binding.tvTabDetection.setTypeface(typefaceBold);
                binding.viewTabDetection.setBackgroundColor(color006F5F);
                binding.layoutDetection.setVisibility(View.VISIBLE);
                break;
            case 1:
                binding.tvTabForecast.setTextColor(color006F5F);
                binding.tvTabForecast.setTypeface(typefaceBold);
                binding.viewTabForecast.setBackgroundColor(color006F5F);
                binding.layoutForecast.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void refreshTvCheck(boolean isShow) {
        if (isShow) {
            binding.tvCheck11.setVisibility(!checkDataList.isEmpty() ? View.VISIBLE : View.INVISIBLE);
            binding.tvCheck12.setVisibility(checkDataList.size() >= 2 ? View.VISIBLE : View.INVISIBLE);
            binding.tvCheck13.setVisibility(checkDataList.size() >= 3 ? View.VISIBLE : View.INVISIBLE);
        } else {
            binding.tvCheck11.setVisibility(View.INVISIBLE);
            binding.tvCheck12.setVisibility(View.INVISIBLE);
            binding.tvCheck13.setVisibility(View.INVISIBLE);
        }
    }

    @NonNull
    private String getShowText() {
        boolean isShowSave = false;
        if (rebarZoneBean != null) {
            if (!rebarZoneBean.rebarPageList.isEmpty()) {
                int rebarPageListSize = rebarZoneBean.rebarPageList.size();
                int lastBeanListSize = rebarZoneBean.rebarPageList.get(rebarZoneBean.rebarPageList.size() - 1).beanList.size();
                if (lastBeanListSize == 0) {
                    rebarPageListSize -= 1;
                }
                if (rebarZoneBean.id == null) {
                    isShowSave = rebarPageListSize > 0;
                } else {
                    if (rebarPageListSize != cloneRebarZoneBean.rebarPageList.size()) {
                        isShowSave = true;
                    }
                    if (!isShowSave) {
                        for (int i = 0; i < rebarPageListSize; i++) {
                            if (rebarZoneBean.rebarPageList.get(i).beanList.size() != cloneRebarZoneBean.rebarPageList.get(i).beanList.size()) {
                                isShowSave = true;
                            }
                        }
                    }
                    if (!isShowSave) {
                        boolean flag = false;
                        for (int i = 0; i < rebarPageListSize; i++) {
                            if (flag) {
                                break;
                            }
                            RebarPageBean rPageBean = rebarZoneBean.rebarPageList.get(i);
                            RebarPageBean tPageBean = cloneRebarZoneBean.rebarPageList.get(i);
                            for (int j = 0; j < rPageBean.beanList.size(); j++) {
                                RebarBean rRebarBean = rPageBean.beanList.get(j);
                                RebarBean tRebarBean = tPageBean.beanList.get(j);
                                if (rRebarBean.distance != tRebarBean.distance) {
                                    isShowSave = true;
                                    flag = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return isShowSave ? "数据未存储，确定退出检测？" : "确定退出检测？";
    }

    private void configGrid(View itemView, int rebarCount, RebarPageBean bean) {
        TextView tvNumber = itemView.findViewById(R.id.tv_grid_number);
        TextView tv1 = itemView.findViewById(R.id.tv_grid_1);
        View v1 = itemView.findViewById(R.id.v_grid_1);
        tv1.setVisibility(rebarCount >= 1 ? View.VISIBLE : View.GONE);
        v1.setVisibility(rebarCount >= 1 ? View.VISIBLE : View.GONE);
        TextView tv2 = itemView.findViewById(R.id.tv_grid_2);
        View v2 = itemView.findViewById(R.id.v_grid_2);
        tv2.setVisibility(rebarCount >= 2 ? View.VISIBLE : View.GONE);
        v2.setVisibility(rebarCount >= 2 ? View.VISIBLE : View.GONE);
        TextView tv3 = itemView.findViewById(R.id.tv_grid_3);
        View v3 = itemView.findViewById(R.id.v_grid_3);
        tv3.setVisibility(rebarCount >= 3 ? View.VISIBLE : View.GONE);
        v3.setVisibility(rebarCount >= 3 ? View.VISIBLE : View.GONE);
        TextView tv4 = itemView.findViewById(R.id.tv_grid_4);
        View v4 = itemView.findViewById(R.id.v_grid_4);
        tv4.setVisibility(rebarCount >= 4 ? View.VISIBLE : View.GONE);
        v4.setVisibility(rebarCount >= 4 ? View.VISIBLE : View.GONE);
        TextView tv5 = itemView.findViewById(R.id.tv_grid_5);
        View v5 = itemView.findViewById(R.id.v_grid_5);
        tv5.setVisibility(rebarCount >= 5 ? View.VISIBLE : View.GONE);
        v5.setVisibility(rebarCount >= 5 ? View.VISIBLE : View.GONE);
        TextView tv6 = itemView.findViewById(R.id.tv_grid_6);
        View v6 = itemView.findViewById(R.id.v_grid_6);
        tv6.setVisibility(rebarCount >= 6 ? View.VISIBLE : View.GONE);
        v6.setVisibility(rebarCount >= 6 ? View.VISIBLE : View.GONE);
        TextView tv7 = itemView.findViewById(R.id.tv_grid_7);
        View v7 = itemView.findViewById(R.id.v_grid_7);
        tv7.setVisibility(rebarCount >= 7 ? View.VISIBLE : View.GONE);
        v7.setVisibility(rebarCount >= 7 ? View.VISIBLE : View.GONE);
        TextView tv8 = itemView.findViewById(R.id.tv_grid_8);
        View v8 = itemView.findViewById(R.id.v_grid_8);
        tv8.setVisibility(rebarCount >= 8 ? View.VISIBLE : View.GONE);
        v8.setVisibility(rebarCount >= 8 ? View.VISIBLE : View.GONE);
        TextView tv9 = itemView.findViewById(R.id.tv_grid_9);
        View v9 = itemView.findViewById(R.id.v_grid_9);
        tv9.setVisibility(rebarCount >= 9 ? View.VISIBLE : View.GONE);
        v9.setVisibility(rebarCount >= 9 ? View.VISIBLE : View.GONE);
        TextView tv10 = itemView.findViewById(R.id.tv_grid_10);
        View v10 = itemView.findViewById(R.id.v_grid_10);
        tv10.setVisibility(rebarCount >= 10 ? View.VISIBLE : View.GONE);
        v10.setVisibility(rebarCount >= 10 ? View.VISIBLE : View.GONE);
        TextView tvAver = itemView.findViewById(R.id.tv_grid_average);
        tvAver.setVisibility(rebarCount >= 1 ? View.VISIBLE : View.GONE);
        //
        tvNumber.setText(bean.segment);
        if (bean.beanList == null) {
            return;
        }
        if (!bean.beanList.isEmpty()) {
            setTextView(tv1, bean.beanList.get(0));
        }
        if (bean.beanList.size() > 1) {
            setTextView(tv2, bean.beanList.get(1));
        }
        if (bean.beanList.size() > 2) {
            setTextView(tv3, bean.beanList.get(2));
        }
        if (bean.beanList.size() > 3) {
            setTextView(tv4, bean.beanList.get(3));
        }
        if (bean.beanList.size() > 4) {
            setTextView(tv5, bean.beanList.get(4));
        }
        if (bean.beanList.size() > 5) {
            setTextView(tv6, bean.beanList.get(5));
        }
        if (bean.beanList.size() > 6) {
            setTextView(tv7, bean.beanList.get(6));
        }
        if (bean.beanList.size() > 7) {
            setTextView(tv8, bean.beanList.get(7));
        }
        if (bean.beanList.size() > 8) {
            setTextView(tv9, bean.beanList.get(8));
        }
        if (bean.beanList.size() > 9) {
            setTextView(tv10, bean.beanList.get(9));
        }
        double total0 = 0, average0, total1 = 0, average1;
        double size = 0;
        if (!bean.beanList.isEmpty()) {
            for (RebarBean b : bean.beanList) {
                if (b.thickness > 0) {
                    total0 += b.thickness;
                    total1 += b.thickness1;
                    size++;
                }
            }
            average0 = StringUtils.getRounding(total0 / size, 2);
            average1 = StringUtils.getRounding(total1 / size, 2);
            average0 = StringUtils.getRounding(average0, 1);
            average1 = StringUtils.getRounding(average1, 1);
        } else {
            average0 = 0;
            average1 = 0;
        }
        tvAver.setText(average1 + "\n" + average0);
    }

    private void setTextView(TextView textView, RebarBean bean) {
        textView.setText(bean.thickness1 + "\n" + bean.thickness);
        textView.setTextColor((bean.thickness1 > thicknessMax || bean.thickness1 < thicknessMin) ? colorFF9600 : color000);
    }

    private void configGridTop(int rebarCount) {
        binding.layoutGridTop.tvGrid1.setVisibility(rebarCount >= 1 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.vGrid1.setVisibility(rebarCount >= 1 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.tvGrid2.setVisibility(rebarCount >= 2 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.vGrid2.setVisibility(rebarCount >= 2 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.tvGrid3.setVisibility(rebarCount >= 3 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.vGrid3.setVisibility(rebarCount >= 3 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.tvGrid4.setVisibility(rebarCount >= 4 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.vGrid4.setVisibility(rebarCount >= 4 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.tvGrid5.setVisibility(rebarCount >= 5 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.vGrid5.setVisibility(rebarCount >= 5 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.tvGrid6.setVisibility(rebarCount >= 6 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.vGrid6.setVisibility(rebarCount >= 6 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.tvGrid7.setVisibility(rebarCount >= 7 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.vGrid7.setVisibility(rebarCount >= 7 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.tvGrid8.setVisibility(rebarCount >= 8 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.vGrid8.setVisibility(rebarCount >= 8 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.tvGrid9.setVisibility(rebarCount >= 9 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.vGrid9.setVisibility(rebarCount >= 9 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.tvGrid10.setVisibility(rebarCount >= 10 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.vGrid10.setVisibility(rebarCount >= 10 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.tvGridAverage.setVisibility(rebarCount >= 1 ? View.VISIBLE : View.GONE);
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

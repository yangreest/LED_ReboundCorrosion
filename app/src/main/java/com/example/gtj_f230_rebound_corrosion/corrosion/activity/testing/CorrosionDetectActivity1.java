package com.example.gtj_f230_rebound_corrosion.corrosion.activity.testing;

import android.annotation.SuppressLint;
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
import com.example.gtj_f230_rebound_corrosion.base.utils.HexUtil;
import com.example.gtj_f230_rebound_corrosion.base.utils.OptionsPickerViewUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.OtherUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.SpinnerUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.StringUtils;
import com.example.gtj_f230_rebound_corrosion.base.view.StyleAlertDialog;
import com.example.gtj_f230_rebound_corrosion.base.view.StyleAlertDialog2;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.CheckDataBean;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.CorrosionBean;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.CurveMinMaxBean;
import com.example.gtj_f230_rebound_corrosion.corrosion.storage.CorrosionCurve;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.CurveComparator;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.OptimizedDataProcessor;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.SignalArrayBufferUtils;
import com.example.gtj_f230_rebound_corrosion.databinding.ActDetect1Binding;
import com.example.gtj_f230_rebound_corrosion.databinding.ViewGridContentBinding;
import com.example.gtj_f230_rebound_corrosion.functions.database.greenDao.db.CurveMinMaxBeanDao;
import com.example.gtj_f230_rebound_corrosion.functions.database.greenDao.db.StandardCurveBeanDao;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.inuker.bluetooth.library.BluetoothManager;
import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.exception.OutOfRangeException;

import java.util.ArrayList;
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
public class CorrosionDetectActivity1 extends BaseActivity<ActDetect1Binding> {
    private MediaPlayer mediaPlayer;
    private ScheduledExecutorService executorService;
    private long timerTime;
    private String spMac;
    private boolean isCollectStop = true;
    private ZoneBean zoneBean;
    private final List<CorrosionBean> corrosionBeanList = new ArrayList<>();
    private StyleAlertDialog2 styleAlertDialog;
    private OptimizedDataProcessor dataProcessor;
    private Handler dataProcessorHandler;
    private int horVer;  //横向，竖向
    private int checkType = 3;  //3:单点检测，4：预估直径
    private int valueMin = 10000, valueMax = 0;
    private boolean isInvalid;  //无效时刻
    private SignalArrayBufferUtils signalArrayUtils;

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
        isSwitch = SPUtils.getInstance().getInt("isSwitch-ABC", 2);
        binding.navigationBar.getTitleText().setOnClickListener(v -> {
            count++;
            if (count >= 5) {
                isSwitch++;
                isSwitch = isSwitch % 3;
                SPUtils.getInstance().put("isSwitch-ABC", isSwitch);
                switch (isSwitch) {
                    case 0:
                        ToastUtils.showShort("A 切换成功");
                        break;
                    case 1:
                        ToastUtils.showShort("B 切换成功");
                        break;
                    case 2:
                        ToastUtils.showShort("C 切换成功");
                        break;
                }
            }
        });
    }

    private int count;
    private int isSwitch;

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
        binding.ivHor.setOnClickListener(this::onViewClicked);
        binding.ivVer.setOnClickListener(this::onViewClicked);
        binding.tvHor.setOnClickListener(this::onViewClicked);
        binding.tvVer.setOnClickListener(this::onViewClicked);
        binding.btnDetect.setOnClickListener(this::onViewClicked);
        binding.btnFinish.setOnClickListener(this::onViewClicked);
        binding.tvSteelDiameter.setOnClickListener(this::onViewClicked);
        signalArrayUtils = new SignalArrayBufferUtils();
        signalArrayUtils.setSize(64);
        spMac = SPUtils.getInstance().getString(StaticConstant.SP_CORROSION_BLE_MAC);
        BluetoothManager.writeBluetooth(spMac, HexUtil.stringToBytes("F301000000FF"));  //预估直径（结　　束）--- 小车位置归零
        initExecutor();
        initBluetooth();
        initOptimizedDataProcessor();
        refreshGrid();
        initSplineInterpolator();
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
                if (!isShow || horVer == 0) {
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
            dataProcessorHandler.post(() -> binding.tvSignal.setText(displacement + " , " + signal));
            if (horVer == 1) {
                treatHor(displacement, signal);
            } else if (horVer == 2) {
                //横向3次才可以竖向测试
                if (checkDataBeanList.size() == 3) {
                    if (checkType == 4) {
                        treatVer(signal);
                    }
                }
            }
        });
    }

    private void treatVer(int signal) {
        if (valueMin > signal) {
            valueMin = signal;
        }
        if (valueMax < signal) {
            valueMax = signal;
        }
        dataProcessorHandler.post(() -> {
//            binding.tvMin.setText("Min：" + valueMin);
//            binding.tvMax.setText("Max：" + valueMax);
        });
    }

    private void treatHor(int displacement, int signal) {
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

        //
        //记录数据|获取最大值
        CheckDataBean maxBean = signalArrayUtils.putAndCheck(displacement, signal);
        //
        if (maxBean == null) {
            return;
        }
        CustomMediaPlayer.startSpeaking(this, mediaPlayer, "prompt");
        if (checkDataBeanList.size() == 3) {
            return;
        }
        checkDataBeanList.add(maxBean);
        dataProcessorHandler.post(() -> refreshCheckData(true));
        if (checkDataBeanList.size() == 3) {
            //停止检测
            isCollectStop = true;
            configCalibration();
            //
            CheckDataBean bean = Collections.min(checkDataBeanList, (o1, o2) -> (int) (o1.y * 1000 - o2.y * 1000));
            computeResult(bean);
            //停止检测
            isCollectStop = false;
        }
    }

    private final List<CheckDataBean> checkDataBeanList = new ArrayList<>();

    private void refreshCheckData(boolean isShow) {
        if (isShow) {
            if (!checkDataBeanList.isEmpty()) binding.tvCheck11.setVisibility(View.VISIBLE);
            if (checkDataBeanList.size() >= 2) binding.tvCheck12.setVisibility(View.VISIBLE);
            if (checkDataBeanList.size() >= 3) binding.tvCheck13.setVisibility(View.VISIBLE);
        } else {
            binding.tvCheck11.setVisibility(View.INVISIBLE);
            binding.tvCheck12.setVisibility(View.INVISIBLE);
            binding.tvCheck13.setVisibility(View.INVISIBLE);
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

    /**
     * 修正后的曲线数据
     */
//    private void correctedCurveData(CheckDataBean bean) {
//        //直线行走距离数据（修正为直线行走距离）
//        double[] xs = ComputeCurveUtils.getDistance(bean.poleNorm, radius, Integer.parseInt(bean.poleNorm), bean.thickness, 121);
//        double[] ys = new double[bean.curve_y_original.length];
//        for (int i = 0; i < bean.curve_y_original.length; i++) {
//            ys[i] = bean.curve_y_original[i];
//        }
//        //修正后的曲线数据（修正为直线的曲线数据）
//        bean.curve_y_processed = ComputeCurveUtils.getCurveY1(xs, ys, 121);
//        Logger.e("---xs-- 钢筋直径：" + bean.poleNorm + " , maxStd：" + bean.curve_y_standard[60] + " , maxNew：" + bean.curve_y_processed[60] + " , xs:" + Arrays.toString(xs) + " , ys:" + Arrays.toString(bean.curve_y_processed));
//    }
    private void computeResult(CheckDataBean maxBean) {
        double poleDiameter = poleRadius * 2;
        List<CheckDataBean> standardList = new ArrayList<>();
        //钢筋直径6
//        if (rebarDiameter >= 6) {
//            if (poleDiameter <= 250) {
//                standardList.add(CorrosionCurve.getSuitableIndex1("6", maxBean, CorrosionCurve.curve_6_250));
//            } else if (poleDiameter <= 310) {
//                standardList.add(CorrosionCurve.getSuitableIndex1("6", maxBean, CorrosionCurve.curve_6_250, CorrosionCurve.curve_6_310));
//            } else if (poleDiameter <= 350) {
//                standardList.add(CorrosionCurve.getSuitableIndex1("6", maxBean, CorrosionCurve.curve_6_310, CorrosionCurve.curve_6_350));
//            } else if (poleDiameter <= 400) {
//                standardList.add(CorrosionCurve.getSuitableIndex1("6", maxBean, CorrosionCurve.curve_6_350, CorrosionCurve.curve_6_400));
//            } else if (poleDiameter <= 450) {
//                standardList.add(CorrosionCurve.getSuitableIndex1("6", maxBean, CorrosionCurve.curve_6_400, CorrosionCurve.curve_6_450));
//            } else {
//                standardList.add(CorrosionCurve.getSuitableIndex1("6", maxBean, CorrosionCurve.curve_6_450));
//            }
//        }
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
        maxBean.poleNorm = "检测";
        CorrosionCurve.curveBeanList.clear();
        CorrosionCurve.curveBeanList.add(maxBean);
        CorrosionCurve.curveBeanList.addAll(standardList);
        //
        changeRate(standardList, maxBean);

        //----------------------------------综合比较
//        for (CheckDataBean bean : standardList) {
//            bean.curve_y_processed1 = new ArrayList<>();
//            for (double v : bean.signalMovAve) {
//                bean.curve_y_processed1.add(v);
//            }
//        }
//        maxBean.curve_y_processed1 = new ArrayList<>();
//        for (double v : maxBean.signalMovAve) {
//            maxBean.curve_y_processed1.add(v);
//        }
//        //--------------------------
//        List<List<Double>> curveCollection = new ArrayList<>();
//        for (CheckDataBean bean : standardList) {
//            curveCollection.add(bean.curve_y_processed1);
//        }
//        CurveComparatorNew.DTWConfig dtwConfig = new CurveComparatorNew.DTWConfig();
//        CurveComparatorNew.EuclideanConfig euclidConfig = new CurveComparatorNew.EuclideanConfig();
//        CurveComparatorNew.CorrelationConfig corrConfig = new CurveComparatorNew.CorrelationConfig();
//        Logger.e("---综合比较结果: start");
//        for (List<Double> curve : curveCollection) {
//            CurveComparatorNew.ComparisonResult result = CurveComparatorNew.compareCurves(maxBean.curve_y_processed1, curve, dtwConfig, euclidConfig, corrConfig);
//            Logger.e("---综合比较结果: " + result);
//        }
//        // 4. 在集合中查找最相似曲线
//        int mostSimilarIndex = CurveComparatorNew.findMostSimilarCurve(maxBean.curve_y_processed1, curveCollection);
//        Logger.e("---最相似的曲线索引: " + mostSimilarIndex);
//        Logger.e("---综合比较结果: end");
//        //----------------------------------综合比较
//        //
//        CheckDataBean checkDataBeanResult = standardList.get(0);
//        if (mostSimilarIndex >= 0) {
//            checkDataBeanResult = standardList.get(mostSimilarIndex);
//        }
//        StringBuilder sbdTitle = new StringBuilder();
//        sbdTitle.append("检测波峰：").append(maxBean.signalMovAve[30]).append(" , 电杆直径：").append(StringUtils.getRounding(poleRadius * 2, 2));
//        //
//        sbdTitle.append("\n").append(maxBean.poleNorm).append(" , ").append(maxBean.thickness).append(" , ").append(maxBean.signalMovAve[30]);
//        for (CheckDataBean bean : standardList) {
//            sbdTitle.append("\n").append(bean.poleNorm).append(" , ").append(bean.thickness).append(" , ").append(bean.signalMovAve[30]);
//        }
//        sbdTitle.append("\n\n检测结果---钢筋直径：").append(checkDataBeanResult.poleNorm);
//        sbdTitle.append("\n检测结果---厚　　度：").append(checkDataBeanResult.thickness);
        //
//        dataProcessorHandler.post(() -> {
//            //
//            showDialog(sbdTitle.toString(), "", "", "", "", "", "");  //显示dialog
//            //-----------------
//            String result = "钢筋直径：" + checkDataBean.poleNorm + ", 厚度：" + checkDataBean.thickness;
//            //
//            binding.tvResult.setText(result);
//            //存储
//            CorrosionBean corrosionBean = new CorrosionBean(binding.etDistance.getText().toString().trim(), zoneBean.corrosionZoneBean.rebarDiameter, checkDataBean.poleNorm);
//            corrosionBeanList.add(corrosionBean);
//            refreshGrid();
//            //--------------------
//            isCollectStop = false;  //继续检测
//        });
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

//    private void treatCurve18(CheckDataBean bean) {
//        //实际距离
//        double[] distances = new double[bean.curve_y_original.length];
//        for (int i = 0; i < distances.length; i++) {
//            distances[i] = ComputeCurveUtils.calculateDistanceC(bean.poleNorm, poleRadius, rebarDiameter, bean.thickness, i - bean.curve_y_original.length / 2);
//        }
//        Logger.d("aa" + distances);
//        //取一半x值和一半信号值
//        double[] xs = new double[distances.length / 2 + 1];
//        for (int i = 0; i < xs.length; i++) {
//            xs[i] = distances[i];
//        }
//        double[] ys = new double[xs.length];
//        for (int i = 0; i < ys.length; i++) {
//            ys[i] = bean.curve_y_original[i];
//        }
//        //重新计算实际距离曲线
//        bean.signalMovAve = ComputeCurveUtils.getCurveY1(xs, ys, xs.length);
//        bean.curve_y_processed1 = ComputeCurveUtils.standardizeCurve(IntArrayUtils.subarray(bean.curve_y_original, 0, 30));
//        int max = bean.curve_y_processed[0];
//        for (int num : bean.curve_y_processed) {
//            if (num > max) {
//                max = num;
//            }
//        }
//        bean.y = max;
//    }

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
        if (view.getId() == binding.btnDetect.getId()) {
            checkInfo();
        } else if (view.getId() == binding.tvSteelDiameter.getId()) {
            OptionsPickerViewUtils.showSingleColumnPickerView(this, "请选择", "", CorrosionCurve.poleNormList, binding.tvSteelDiameter.getText().toString(), position -> binding.tvSteelDiameter.setText(CorrosionCurve.poleNormList.get(position)));
        } else if (view.getId() == binding.btnFinish.getId()) {
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
        } else if (view.getId() == binding.ivHor.getId() || view.getId() == binding.tvHor.getId()) {
            //清空横向数据
            checkDataBeanList.clear();
            refreshCheckData(false);
            //清空竖向数据
            valueMin = 10000;
            valueMax = 0;
            //修改设备采集模式
            BluetoothManager.writeBluetooth(spMac, HexUtil.stringToBytes("F301000000FF"));  //预估直径（结　　束）--- 小车位置归零
            //
            setHorVer(1);
        } else if (view.getId() == binding.ivVer.getId() || view.getId() == binding.tvVer.getId()) {
            if (checkDataBeanList.size() == 3) {
                setHorVer(2);
            }
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

    private void treatData() {
        if (checkType == 3) {
            checkType = 4;
            BluetoothManager.writeBluetooth(spMac, HexUtil.stringToBytes("F401000000FF"));  //预估直径（开　　始）--- 一直发送信号值
            //
            valueMin = 10000;
            valueMax = 0;
            isInvalid = true;
            new Handler().postDelayed(() -> isInvalid = false, 1000);
            //
            binding.tvSignal.setText("位移|信号：" + "-- , --");
//            binding.tvMin.setText("Min: " + valueMin);
//            binding.tvMax.setText("Max: " + valueMax);
        } else {
            checkType = 3;
            BluetoothManager.writeBluetooth(spMac, HexUtil.stringToBytes("F301000000FF"));  //预估直径（结　　束）--- 小车位置归零
            //计算
//            computeRebarDiameter();
            //测试
//            switch (isSwitch) {
//                case 0:
//                    aaa();
//                    break;
//                case 1:
//                    bbb();
//                    break;
//                case 2:
//                    ccc();
//                    break;
//            }
        }
    }

//    private void aaa() {
//        double rebar = 0;
//        if (valueMin >= 200) {
//            double[] re = getRate(valueMin, valueMax);
//            if (valueMax >= 2000) {
//                //电杆
//                rebar = StringUtils.getInterpolationValue(0.001d, 9d, 1d, 10d, re[1] / 0.28);
//            } else if (valueMin >= 1500) {
//                //模型9
//                if (valueMax >= 1720) {
//                    rebar = StringUtils.getInterpolationValue(0.001d, 8d, 1d, 9d, re[1] / 0.06);
//                } else {
//                    rebar = StringUtils.getInterpolationValue(0.001d, 8d, 1d, 9d, re[1] / 0.05);
//                }
//
//            } else {
//                //模型7
//                if (valueMax >= 1000) {
//                    rebar = StringUtils.getInterpolationValue(0.001d, 6d, 1d, 7d, re[1] / 0.09);
//                } else {
//                    rebar = StringUtils.getInterpolationValue(0.001d, 6d, 1d, 7d, re[1] / 0.1);
//                }
//            }
//            rebar = StringUtils.getRounding(rebar, 1);
//        }
//        String result = "结果---直径：" + rebar;
//        if (rebar <= 0) {
//            return;
//        }
//        binding.tvResult.setText(result);
//        //存储
//        CorrosionBean corrosionBean = new CorrosionBean(binding.etDistance.getText().toString().trim(), zoneBean.corrosionZoneBean.rebarDiameter, rebar + "");
//        corrosionBeanList.add(corrosionBean);
//        refreshGrid();
//    }

//    private void bbb() {
//        double rebar = 0;
//        if (valueMin >= 200) {
//            double[] re = getRate(valueMin, valueMax);
//            if (valueMax >= 900) {
//                //电杆
//                rebar = StringUtils.getInterpolationValueBBB(0.001d, 9d, 1d, 10d, re[1] / 0.35);
//            } else if (valueMax >= 500) {
//                //模型9
//                rebar = StringUtils.getInterpolationValueBBB(0.001d, 8d, 1d, 9d, re[1] / 0.03);
//            } else {
//                //模型7
//                rebar = StringUtils.getInterpolationValueBBB(0.001d, 6d, 1d, 7d, re[1] / 0.03);
//            }
//            rebar = StringUtils.getRounding(rebar, 1);
//        }
//        String result = "结果---直径：" + rebar;
//        if (rebar <= 0) {
//            return;
//        }
//        binding.tvResult.setText(result);
//        //存储
//        CorrosionBean corrosionBean = new CorrosionBean(binding.etDistance.getText().toString().trim(), zoneBean.corrosionZoneBean.rebarDiameter, rebar + "");
//        corrosionBeanList.add(corrosionBean);
//        refreshGrid();
//    }

//    private void ccc() {
//        double rebar = 0;
//        if (valueMin >= 200) {
//            double[] re = getRate(valueMin, valueMax);
//            if (valueMax >= 1800) {
//                //电杆14
//                rebar = StringUtils.getInterpolationValueBBB(0.001d, 13d, 1d, 14d, re[1] / 0.35);
//            } else if (valueMax >= 500) {
//                //电杆10
//                rebar = StringUtils.getInterpolationValueBBB(0.001d, 9d, 1d, 10d, re[1] / 0.3);
//            } else {
//                //模型7
//                rebar = StringUtils.getInterpolationValueBBB(0.001d, 6d, 1d, 7d, re[1] / 0.03);
//            }
//            rebar = StringUtils.getRounding(rebar, 1);
//        }
//        String result = "结果---直径：" + rebar;
//        if (rebar <= 0) {
//            return;
//        }
//        binding.tvResult.setText(result);
//        //存储
//        CorrosionBean corrosionBean = new CorrosionBean(binding.etDistance.getText().toString().trim(), zoneBean.corrosionZoneBean.rebarDiameter, rebar + "");
//        corrosionBeanList.add(corrosionBean);
//        refreshGrid();
//    }

    public static double[] getRate(int min, double max) {
        double[] re = new double[2];
        re[0] = (max - min) / min;
        re[1] = (max - min) / max;
        return re;
    }

    private void computeRebarDiameter() {
        List<double[]> maxYList = new ArrayList<>();
        if (rebarDiameter >= 7) {
            maxYList.add(getSignalMax(7, interpolators_7));
        }
        if (rebarDiameter >= 9) {
            maxYList.add(getSignalMax(9, interpolators_9));
        }
        if (rebarDiameter >= 10) {
            maxYList.add(getSignalMax(10, interpolators_10));
        }
        if (rebarDiameter >= 11) {
            maxYList.add(getSignalMax(11, interpolators_11));
        }
        if (rebarDiameter >= 14) {
            maxYList.add(getSignalMax(14, interpolators_14));
        }
        //1.判断钢筋直径
        double min = 10000;
        int index = 0;
        for (int i = 0; i < maxYList.size(); i++) {
            double temp = Math.abs(maxYList.get(i)[1] - valueMax);
            if (temp < min) {
                min = temp;
                index = i;
            }
        }
        //2.判断锈蚀程度


        StringBuilder sbd = new StringBuilder();
        sbd.append("检测---Min：").append(valueMin).append(" , Max：").append(valueMax);
        for (double[] doubles : maxYList) {
            sbd.append("\n钢筋直径：" + doubles[0] + " , 厚度：" + doubles[1] + " , 最小：" + valueMin + " , 最大：" + doubles[2]);
        }
//        String result = "\n结果---直径：" + maxYList.get(index)[0] + " , 厚度：" + maxYList.get(index)[1];
        String result = "结果---直径：" + maxYList.get(index)[0];
        sbd.append(result);
        //
        ToastUtils.showShort(result);
        if (maxYList.get(index)[0] == 0) {
            return;
        }
        binding.tvResult.setText(result);
//        styleAlertDialog.setContent(sbd).setOKButton("确定", v -> styleAlertDialog.dismiss()).show();
        //存储
        CorrosionBean corrosionBean = new CorrosionBean(binding.etDistance.getText().toString().trim(), zoneBean.corrosionZoneBean.rebarDiameter, maxYList.get(index)[0] + "");
        corrosionBeanList.add(corrosionBean);
        refreshGrid();
    }

    private double[] getSignalMax(int rebar, PolynomialSplineFunction[] interpolators) {
        //        valueMin,valueMax
        double[] result = new double[3];
        try {
            double tempX = interpolators[0].value(valueMin);
            double tempY = interpolators[1].value(tempX);
            //
            result[0] = rebar;
            result[1] = StringUtils.getRounding(tempX, 3);
            result[2] = StringUtils.getRounding(tempY, 3);

        } catch (OutOfRangeException e) {

        }
        return result;
    }

    private PolynomialSplineFunction[] interpolators_7;
    private PolynomialSplineFunction[] interpolators_9;
    private PolynomialSplineFunction[] interpolators_10;
    private PolynomialSplineFunction[] interpolators_11;
    private PolynomialSplineFunction[] interpolators_14;

    private void initSplineInterpolator() {
        CurveMinMaxBeanDao minMaxBeanDao = GreenDaoHelper.getDaoSession(this).getCurveMinMaxBeanDao();
        List<CurveMinMaxBean> list_7 = minMaxBeanDao.queryBuilder().where(CurveMinMaxBeanDao.Properties.RebarDiameter.eq("7")).orderAsc(CurveMinMaxBeanDao.Properties.Thickness).list();
        List<CurveMinMaxBean> list_9 = minMaxBeanDao.queryBuilder().where(CurveMinMaxBeanDao.Properties.RebarDiameter.eq("9")).orderAsc(CurveMinMaxBeanDao.Properties.Thickness).list();
        List<CurveMinMaxBean> list_11 = minMaxBeanDao.queryBuilder().where(CurveMinMaxBeanDao.Properties.RebarDiameter.eq("11")).orderAsc(CurveMinMaxBeanDao.Properties.Thickness).list();
        List<CurveMinMaxBean> list_10 = minMaxBeanDao.queryBuilder().where(CurveMinMaxBeanDao.Properties.RebarDiameter.eq("10")).orderAsc(CurveMinMaxBeanDao.Properties.Thickness).list();
        List<CurveMinMaxBean> list_14 = minMaxBeanDao.queryBuilder().where(CurveMinMaxBeanDao.Properties.RebarDiameter.eq("14")).orderAsc(CurveMinMaxBeanDao.Properties.Thickness).list();
        //
        interpolators_7 = getSplineInterpolator(list_7);
        interpolators_9 = getSplineInterpolator(list_9);
        interpolators_11 = getSplineInterpolator(list_11);
        interpolators_10 = getSplineInterpolator(list_10);
        interpolators_14 = getSplineInterpolator(list_14);
    }

    private PolynomialSplineFunction[] getSplineInterpolator(List<CurveMinMaxBean> list) {
        double[] xsASC = new double[list.size()];
        double[] xsDESC = new double[list.size()];
        double[] ys_min = new double[list.size()];
        double[] ys_max = new double[list.size()];
        //正序
        for (int i = 0; i < list.size(); i++) {
            xsASC[i] = list.get(i).thickness;
            ys_max[i] = list.get(i).signalMax;
        }
        //倒序
        int count = 0;
        for (int i = list.size() - 1; i >= 0; i--) {
            xsDESC[count] = list.get(i).thickness;
            ys_min[count] = list.get(i).signalMin;
            count++;
        }
        //
        PolynomialSplineFunction[] functions = new PolynomialSplineFunction[2];
        SplineInterpolator spline0 = new SplineInterpolator();
        functions[0] = spline0.interpolate(ys_min, xsDESC);
        SplineInterpolator spline1 = new SplineInterpolator();
        functions[1] = spline1.interpolate(xsASC, ys_max);
        return functions;
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
        binding.layoutBase.setVisibility(View.GONE);
        binding.btnDetect.setVisibility(View.GONE);
        binding.layoutStart.setVisibility(View.VISIBLE);
        //
        setHorVer(1);
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
        String rebarDiameter;
        //钢筋直径7
        rebarDiameter = "7";
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
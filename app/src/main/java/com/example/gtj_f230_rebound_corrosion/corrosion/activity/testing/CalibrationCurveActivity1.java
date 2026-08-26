package com.example.gtj_f230_rebound_corrosion.corrosion.activity.testing;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.base.activity.BaseActivity;
import com.example.gtj_f230_rebound_corrosion.base.storage.GreenDaoHelper;
import com.example.gtj_f230_rebound_corrosion.base.utils.CustomMediaPlayer;
import com.example.gtj_f230_rebound_corrosion.base.utils.StringUtils;
import com.example.gtj_f230_rebound_corrosion.base.view.StyleAlertDialog;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.CheckDataBean;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.LineChartBean;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.StandardCurveBean;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.LineChartManager;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.OptimizedDataProcessor;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.SignalArrayBufferUtils;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.lowpassfilter.LowPassFilter;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.lowpassfilter.Optimized10ElementTrimmedMeanUtils;
import com.example.gtj_f230_rebound_corrosion.databinding.ActCalibrationCurveBinding;
import com.example.gtj_f230_rebound_corrosion.functions.database.greenDao.db.StandardCurveBeanDao;
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

/**
 * 标定存储曲线（小车一直发送位移、信号数据）
 */
@SuppressLint("SetTextI18n")
public class CalibrationCurveActivity1 extends BaseActivity<ActCalibrationCurveBinding> {
    private MediaPlayer mediaPlayer;
    private LineChartManager lineChartManager;
    private ScheduledExecutorService executorService;
    private final List<CheckDataBean> checkDataList = new ArrayList<>();
    private StandardCurveBeanDao curveDao;
    private long timerTime;
    private String spMac;
    private boolean isCollectStop;
    private OptimizedDataProcessor dataProcessor;
    private Handler dataProcessorHandler;
    private Optimized10ElementTrimmedMeanUtils meanUtils;  //平均信号
    private SignalArrayBufferUtils signalArrayUtils;  //存储曲线

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
        lineChartManager = new LineChartManager(this, binding.lineChart);
        binding.btnFinish.setOnClickListener(view -> finish());
        mediaPlayer = CustomMediaPlayer.getMediaPlayer(null);
        curveDao = GreenDaoHelper.getDaoSession(this).getStandardCurveBeanDao();
        signalArrayUtils = new SignalArrayBufferUtils();
        signalArrayUtils.setSize(400);
        meanUtils = new Optimized10ElementTrimmedMeanUtils();
        initExecutor();
        initBluetooth();
        initOptimizedDataProcessor();
        binding.navigationBar.getTitleText().setOnClickListener(v -> {
            if (binding.scrollView.getVisibility() == View.VISIBLE) {
                binding.scrollView.setVisibility(View.GONE);
            } else {

                binding.scrollView.setVisibility(View.VISIBLE);
            }
        });
//        binding.etRebarSpacing.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                int size;
//                if (s.length() == 0) {
//                    size = 0;
//                } else {
//                    size = Integer.parseInt(s.toString());
//                }
//                if (size >= 40) {
//                    signalArrayUtils.setSize(64);
//                } else {
//                    signalArrayUtils.setSize(50);
//                }
//            }
//        });
        binding.etRebarDiameter.setText(SPUtils.getInstance().getString("strCal-RebarDiameter"));
        binding.etPoleDiameter.setText(SPUtils.getInstance().getString("strCal-PoleDiameter"));
        binding.etRebarSpacing.setText(SPUtils.getInstance().getString("strCal-RebarSpacing"));
        binding.etSpiralSpacing.setText(SPUtils.getInstance().getString("strCal-SpiralSpacing"));
        binding.etThickness.setText(SPUtils.getInstance().getString("strCal-Thickness"));
        binding.btnClear.setOnClickListener(v -> {
            signalMin = 10000;
            signalMax = 0;
            binding.tvMessage1.setText("位移|信号：-- , --\nMin: " + signalMin + "　　　Max: " + signalMax);
        });
    }

    private void initExecutor() {
        Handler handlerPost = new Handler();
        executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleWithFixedDelay(() -> handlerPost.post(() -> timerTime++), 0, 1, TimeUnit.SECONDS);
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

//    public void filterErrors(double[] signalArray) {
//        //
//        //前6个
//        double[] xs = new double[10];
//        double[] ys = new double[10];
//        int index = 0;
//        for (int i = 0; i < signalArray.length; i++) {
//            if (i >= 6) {
//                xs[index] = i + 1;
//                ys[index] = signalArray[i];
//                index++;
//            }
//            if (i >= 9 + 6) {
//                break;
//            }
//        }
//        PolynomialFunction polyFunc = new PolynomialFunction(ComputeCurveUtils.getCoefficient(xs, ys));
//        for (int i = 0; i < 6; i++) {
//            signalArray[i] = StringUtils.getRounding(polyFunc.value(i + 1), 0);
//        }
//        //后5个
//        index = 0;
//        for (int i = 0; i < signalArray.length; i++) {
//            if (i >= 49 && i < 59) {
//                xs[index] = i;
//                ys[index] = signalArray[i];
//                index++;
//            }
//        }
//        polyFunc = new PolynomialFunction(ComputeCurveUtils.getCoefficient(xs, ys));
//        for (int i = 59; i < 64; i++) {
//            signalArray[i] = StringUtils.getRounding(polyFunc.value(i), 0);
//        }
//    }

    private int signalMin = 10000, signalMax;

    private int cur_displacement;

    private void initOptimizedDataProcessor() {
        dataProcessorHandler = new Handler(Looper.getMainLooper());
        dataProcessor = new OptimizedDataProcessor((displacement, signal, frameData) -> {
            Log.d("====", "====onNotify2: " + displacement + " , " + signal + " , " + frameData);
            //平均信号
            double tempSignal = meanUtils.addValue(signal);
            //
            signalMin = (int) Math.min(tempSignal, signalMin);
            signalMax = (int) Math.max(tempSignal, signalMax);
            //
            dataProcessorHandler.post(() -> binding.tvMessage1.setText("位移|信号：" + displacement + " , " + signal + "\nMin: " + signalMin + "　　　Max: " + signalMax));
            //
            if (isCollectStop) {
                return;
            }
            //
            if (cur_displacement == displacement) {
                return;
            }
            cur_displacement = displacement;
            //超过2秒清空数据
            {
                if (timerTime >= 2) {
                    signalArrayUtils.clear();
                }
                timerTime = 0;
            }
            //
            //记录数据|获取最大值
            CheckDataBean maxBean = signalArrayUtils.putAndCheck(displacement, (int) tempSignal);
            if (maxBean == null) {
                return;
            }
            //停止检测
            isCollectStop = true;
            CustomMediaPlayer.startSpeaking(this, mediaPlayer, "prompt");
            //移动平均滤波
            maxBean.signalMovAve = LowPassFilter.movingAverageFilter(maxBean.curve_y_original, 3);
            //
            //计算5条曲线
            boolean checkSize5 = false;
//            String strRebarSpacing = binding.etRebarSpacing.getText().toString().trim();
//            if (!TextUtils.isEmpty(strRebarSpacing)) {
//                int rebarSpacing = Integer.parseInt(strRebarSpacing);
//                if (rebarSpacing < 40) {
//                    checkSize = 1;
//                }
//            }
            if (checkSize5) {
                check_5(maxBean);
            } else {
                check_1(maxBean);
            }
            //
            dataProcessorHandler.post(() -> {
                isCollectStop = false;  //继续检测
            });
        });
    }

    private void check_1(CheckDataBean maxBean) {
        //存储
        String strRebarDiameter = binding.etRebarDiameter.getText().toString().trim();
        String strPoleDiameter = binding.etPoleDiameter.getText().toString().trim();
        String strRebarSpacing = binding.etRebarSpacing.getText().toString().trim();
        String strSpiralSpacing = binding.etSpiralSpacing.getText().toString().trim();
        String strThickness = binding.etThickness.getText().toString().trim();
        if (TextUtils.isEmpty(strRebarDiameter) || TextUtils.isEmpty(strThickness)) {
            ToastUtils.showShort("不能为空");
            isCollectStop = false;
            return;
        }
        //
        SPUtils.getInstance().put("strCal-RebarDiameter", strRebarDiameter);
        SPUtils.getInstance().put("strCal-PoleDiameter", strPoleDiameter);
        SPUtils.getInstance().put("strCal-RebarSpacing", strRebarSpacing);
        SPUtils.getInstance().put("strCal-SpiralSpacing", strSpiralSpacing);
        SPUtils.getInstance().put("strCal-Thickness", strThickness);
        //
        StandardCurveBean curveBean = new StandardCurveBean(strRebarDiameter, strPoleDiameter, strRebarSpacing, strSpiralSpacing, Double.parseDouble(strThickness), maxBean.signalMovAve);
        StandardCurveBean databaseBean = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.UniqueKey.eq(curveBean.uniqueKey)).unique();
        //
        if (databaseBean != null) {
            dataProcessorHandler.post(() -> {
                StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this);
                styleAlertDialog.setContent("此数据已存在，是否覆盖？").setLeftButton("取消", v -> styleAlertDialog.dismiss()).setRightButton("确定", v -> {
                    styleAlertDialog.dismiss();
                    curveBean.id = databaseBean.id;
                    long id = curveDao.insertOrReplace(curveBean);
                    showDialogg("已检测5次 标定成功\nid：" + id + "　钢筋直径：" + strRebarDiameter + "，电杆直径：" + strPoleDiameter + "，保护层厚度：" + strThickness + "，最大信号：" + maxBean.signalMovAve[maxBean.signalMovAve.length / 2]);
                }).show();
            });
        } else {
            long id = curveDao.insertOrReplace(curveBean);
            showDialogg("已检测5次 标定成功\nid：" + id + "　钢筋直径：" + strRebarDiameter + "，电杆直径：" + strPoleDiameter + "，主筋间距：" + strRebarSpacing + "，箍筋间距：" + strSpiralSpacing + "，保护层厚度：" + strThickness + "，最大信号：" + maxBean.signalMovAve[maxBean.signalMovAve.length / 2]);
        }
        dataProcessorHandler.post(() -> {
            //显示曲线
            List<LineChartBean> chartBeanList = new ArrayList<>();
            chartBeanList.add(new LineChartBean(maxBean.poleNorm,0, 1 + ". ", getMax(maxBean.signalMovAve), maxBean.signalMovAve));
            lineChartManager.showLineChart2(chartBeanList);
            showTableCurve_1(maxBean);  //信号曲线数据值
        });
    }

    private static final int CheckSize = 3;

    private void check_5(CheckDataBean maxBean) {
        //清空数据
        if (checkDataList.size() == CheckSize) {
            checkDataList.clear();
        }
        //添加新数据
        checkDataList.add(maxBean);
        if (checkDataList.size() == CheckSize) {
            //查找左右信号值只差最小的曲线为参考信号
            int standardIndex = 0;
            double signalMin = 10000;
            for (int i = 0; i < checkDataList.size(); i++) {
                CheckDataBean bean = checkDataList.get(i);
                double temp = Math.abs(bean.signalMovAve[0] - bean.signalMovAve[bean.signalMovAve.length - 1]);
                if (signalMin > temp) {
                    signalMin = temp;
                    standardIndex = i;
                }
            }
            //参考信号
            CheckDataBean standardBean = checkDataList.get(standardIndex);
            //存储
            String strRebarDiameter = binding.etRebarDiameter.getText().toString().trim();
            String strPoleDiameter = binding.etPoleDiameter.getText().toString().trim();
            String strRebarSpacing = binding.etRebarSpacing.getText().toString().trim();
            String strSpiralSpacing = binding.etSpiralSpacing.getText().toString().trim();
            String strThickness = binding.etThickness.getText().toString().trim();
            if (TextUtils.isEmpty(strRebarDiameter) || TextUtils.isEmpty(strThickness)) {
                ToastUtils.showShort("不能为空");
                isCollectStop = false;
                return;
            }
            //
            SPUtils.getInstance().put("strCal-RebarDiameter", strRebarDiameter);
            SPUtils.getInstance().put("strCal-PoleDiameter", strPoleDiameter);
            SPUtils.getInstance().put("strCal-RebarSpacing", strRebarSpacing);
            SPUtils.getInstance().put("strCal-SpiralSpacing", strSpiralSpacing);
            SPUtils.getInstance().put("strCal-Thickness", strThickness);
            //
            StandardCurveBean curveBean = new StandardCurveBean(strRebarDiameter, strPoleDiameter, strRebarSpacing, strSpiralSpacing, Double.parseDouble(strThickness), standardBean.signalMovAve);
            StandardCurveBean databaseBean = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.UniqueKey.eq(curveBean.uniqueKey)).unique();
            //
            if (databaseBean != null) {
                dataProcessorHandler.post(() -> {
                    StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this);
                    styleAlertDialog.setContent("此数据已存在，是否覆盖？").setLeftButton("取消", v -> styleAlertDialog.dismiss()).setRightButton("确定", v -> {
                        styleAlertDialog.dismiss();
                        curveBean.id = databaseBean.id;
                        long id = curveDao.insertOrReplace(curveBean);
                        showDialogg("已检测5次 标定成功\nid：" + id + "　钢筋直径：" + strRebarDiameter + "，电杆直径：" + strPoleDiameter + "，保护层厚度：" + strThickness + "，最大信号：" + standardBean.signalMovAve[standardBean.signalMovAve.length / 2]);
                    }).show();
                });
            } else {
                long id = curveDao.insertOrReplace(curveBean);
                showDialogg("已检测5次 标定成功\nid：" + id + "　钢筋直径：" + strRebarDiameter + "，电杆直径：" + strPoleDiameter + "，主筋间距：" + strRebarSpacing + "，箍筋间距：" + strSpiralSpacing + "，保护层厚度：" + strThickness + "，最大信号：" + standardBean.signalMovAve[standardBean.signalMovAve.length / 2]);
            }
        } else {
            showDialogg("已检测" + checkDataList.size() + "次");
        }
        dataProcessorHandler.post(() -> {
            showCurveLine();  //显示曲线
            showTableCurve_5();  //信号曲线数据值
        });
    }

    private void showDialogg(String string) {
        dataProcessorHandler.post(() -> binding.tvMessage2.setText(string));
        ToastUtils.showShort(string);
    }

    private void showCurveLine() {
        //显示曲线
        List<LineChartBean> chartBeanList = new ArrayList<>();
        for (int i = 0; i < checkDataList.size(); i++) {
            chartBeanList.add(new LineChartBean("",0, i + 1 + ". ", getMax(checkDataList.get(i).signalMovAve), checkDataList.get(i).signalMovAve));
        }
        lineChartManager.showLineChart2(chartBeanList);
    }

    public static double getMax(double[] array) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("数组不能为空或长度为0");
        }

        double max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    private void showTableCurve_1(CheckDataBean maxBean) {
        int[] truncated = new int[maxBean.signalMovAve.length];
        for (int i = 0; i < maxBean.signalMovAve.length; i++) {
            truncated[i] = (int) maxBean.signalMovAve[i];
        }
        //
        StringBuilder sbd = new StringBuilder();
        //
        sbd.append(Arrays.toString(Arrays.copyOfRange(truncated, 0, 20))).append("\n");
        sbd.append("\n\n");
        sbd.append(Arrays.toString(Arrays.copyOfRange(truncated, 180, 200))).append("\n");

        sbd.append("\n\n");
        sbd.append(Arrays.toString(Arrays.copyOfRange(truncated, 200, 220))).append("\n");

        sbd.append("\n\n");
        sbd.append(Arrays.toString(Arrays.copyOfRange(truncated, 380, 400))).append("\n");

        binding.tvTable.setText(sbd);
    }

    private void showTableCurve_5() {
        //
        StringBuilder sbd = new StringBuilder();
        //
        for (CheckDataBean bean : checkDataList) {
            int[] truncated = new int[bean.signalMovAve.length];
            for (int i = 0; i < bean.signalMovAve.length; i++) {
                truncated[i] = (int) bean.signalMovAve[i];
            }
            sbd.append(Arrays.toString(Arrays.copyOfRange(truncated, 0, 20))).append("\n");
        }
        sbd.append("\n\n");
        for (CheckDataBean bean : checkDataList) {
            int[] truncated = new int[bean.signalMovAve.length];
            for (int i = 0; i < bean.signalMovAve.length; i++) {
                truncated[i] = (int) bean.signalMovAve[i];
            }
            sbd.append(Arrays.toString(Arrays.copyOfRange(truncated, 180, 200))).append("\n");
        }
        sbd.append("\n\n");
        for (CheckDataBean bean : checkDataList) {
            int[] truncated = new int[bean.signalMovAve.length];
            for (int i = 0; i < bean.signalMovAve.length; i++) {
                truncated[i] = (int) bean.signalMovAve[i];
            }
            sbd.append(Arrays.toString(Arrays.copyOfRange(truncated, 200, 220))).append("\n");
        }
        sbd.append("\n\n");
        for (CheckDataBean bean : checkDataList) {
            int[] truncated = new int[bean.signalMovAve.length];
            for (int i = 0; i < bean.signalMovAve.length; i++) {
                truncated[i] = (int) bean.signalMovAve[i];
            }
            sbd.append(Arrays.toString(Arrays.copyOfRange(truncated, 380, 400))).append("\n");
        }
        binding.tvTable.setText(sbd);
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

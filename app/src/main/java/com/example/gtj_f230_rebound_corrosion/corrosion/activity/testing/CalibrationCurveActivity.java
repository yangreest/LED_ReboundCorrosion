package com.example.gtj_f230_rebound_corrosion.corrosion.activity.testing;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.base.activity.BaseActivity;
import com.example.gtj_f230_rebound_corrosion.base.storage.GreenDaoHelper;
import com.example.gtj_f230_rebound_corrosion.base.utils.CustomMediaPlayer;
import com.example.gtj_f230_rebound_corrosion.base.view.StyleAlertDialog;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.CheckDataBean;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.LineChartBean;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.StandardCurveBean;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.LineChartManager;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.OptimizedDataProcessor;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.SignalArrayBufferUtils;
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
 * 标定存储曲线（小车位移发生变化发送数据）
 */
@SuppressLint("SetTextI18n")
public class CalibrationCurveActivity extends BaseActivity<ActCalibrationCurveBinding> {
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
    private SignalArrayBufferUtils signalArrayUtils;

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
        signalArrayUtils.setSize(64);
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
        binding.etRebarDiameter.setText(SPUtils.getInstance().getString("strCal-RebarDiameter"));
        binding.etPoleDiameter.setText(SPUtils.getInstance().getString("strCal-PoleDiameter"));
        binding.etThickness.setText(SPUtils.getInstance().getString("strCal-Thickness"));
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
            dataProcessorHandler.post(() -> binding.tvMessage1.setText("位移|信号：" + displacement + " , " + signal));
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
            if (maxBean == null) {
                return;
            }
            //停止检测
            isCollectStop = true;
            CustomMediaPlayer.startSpeaking(this, mediaPlayer, "prompt");
            //清空数据
            if (checkDataList.size() == 5) {
                checkDataList.clear();
            }
            //添加新数据
            checkDataList.add(maxBean);
            //计算5条曲线
            if (checkDataList.size() == 5) {
                CheckDataBean curveBean1 = checkDataList.get(0);
                CheckDataBean curveBean2 = checkDataList.get(1);
                CheckDataBean curveBean3 = checkDataList.get(2);
                CheckDataBean curveBean4 = checkDataList.get(3);
                CheckDataBean curveBean5 = checkDataList.get(4);
                int size = 61;
                double[] signalArray = new double[size];
                for (int i = 0; i < size; i++) {
                    double[] signals = new double[5];
                    signals[0] = curveBean1.curve_y_original[i];
                    signals[1] = curveBean2.curve_y_original[i];
                    signals[2] = curveBean3.curve_y_original[i];
                    signals[3] = curveBean4.curve_y_original[i];
                    signals[4] = curveBean5.curve_y_original[i];
                    //去掉一个最大，去掉一个最小，剩余三个取平均值
                    Arrays.sort(signals);
                    int sum = 0;
                    sum += signals[1];
                    sum += signals[2];
                    sum += signals[3];
                    signalArray[i] = sum / 3;
                }
                //存储
                String strRebarDiameter = binding.etRebarDiameter.getText().toString().trim();
                String strPoleDiameter = binding.etPoleDiameter.getText().toString().trim();
                String strThickness = binding.etThickness.getText().toString().trim();
                if (TextUtils.isEmpty(strRebarDiameter) || TextUtils.isEmpty(strPoleDiameter) || TextUtils.isEmpty(strThickness)) {
                    ToastUtils.showShort("直径、保护层厚度不能为空");
                    isCollectStop = false;
                    return;
                }
                //
                SPUtils.getInstance().put("strCal-RebarDiameter", strRebarDiameter);
                SPUtils.getInstance().put("strCal-PoleDiameter", strPoleDiameter);
                SPUtils.getInstance().put("strCal-Thickness", strThickness);
                //
                StandardCurveBean curveBean = new StandardCurveBean(strRebarDiameter, strPoleDiameter, "", "", Double.parseDouble(strThickness), signalArray);
                StandardCurveBean databaseBean = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.UniqueKey.eq(curveBean.uniqueKey)).unique();
                //
                if (databaseBean != null) {
                    dataProcessorHandler.post(() -> {
                        StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this);
                        styleAlertDialog.setContent("此数据已存在，是否覆盖？").setLeftButton("取消", v -> styleAlertDialog.dismiss()).setRightButton("确定", v -> {
                            styleAlertDialog.dismiss();
                            curveBean.id = databaseBean.id;
                            long id = curveDao.insertOrReplace(curveBean);
                            showDialogg("已检测5次 标定成功\nid：" + id + "钢筋直径：" + strRebarDiameter + "，电杆直径：" + strPoleDiameter + "，保护层厚度：" + strThickness + "，最大信号：" + signalArray[30]);
                        }).show();
                    });
                } else {
                    long id = curveDao.insertOrReplace(curveBean);
                    showDialogg("已检测5次 标定成功\nid：" + id + "钢筋直径：" + strRebarDiameter + "，电杆直径：" + strPoleDiameter + "，保护层厚度：" + strThickness + "，最大信号：" + signalArray[30]);
                }
            } else {
                showDialogg("已检测" + checkDataList.size() + "次，信号：" + maxBean.curve_y_original[30]);
            }
            //----------------------------------------
//            //清空数据
//            if (checkDataList.size() == 1) {
//                checkDataList.clear();
//            }
//            //添加新数据
//            checkDataList.add(maxBean);
//            //存储
//            String strRebarDiameter = binding.etRebarDiameter.getText().toString().trim();
//            String strPoleDiameter = binding.etPoleDiameter.getText().toString().trim();
//            String strThickness = binding.etThickness.getText().toString().trim();
//            if (TextUtils.isEmpty(strRebarDiameter) || TextUtils.isEmpty(strPoleDiameter) || TextUtils.isEmpty(strThickness)) {
//                ToastUtils.showShort("直径、保护层厚度不能为空");
//                isCollectStop = false;
//                return;
//            }
//            //
//            SPUtils.getInstance().put("strCal-RebarDiameter", strRebarDiameter);
//            SPUtils.getInstance().put("strCal-PoleDiameter", strPoleDiameter);
//            SPUtils.getInstance().put("strCal-Thickness", strThickness);
//            //
//            StandardCurveBean curveBean = new StandardCurveBean(strRebarDiameter, strPoleDiameter, Double.parseDouble(strThickness), maxBean.curve_y_original);
//            StandardCurveBean databaseBean = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.UniqueKey.eq(curveBean.uniqueKey)).unique();
//            if (databaseBean != null) {
//                dataProcessorHandler.post(() -> {
//                    StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this);
//                    styleAlertDialog.setContent("此数据已存在，是否覆盖？").setLeftButton("取消", v -> styleAlertDialog.dismiss()).setRightButton("确定", v -> {
//                        styleAlertDialog.dismiss();
//                        curveBean.id = databaseBean.id;
//                        long id = curveDao.insertOrReplace(curveBean);
//                        showDialogg("标定成功\nid：" + id + "钢筋直径：" + strRebarDiameter + "，电杆直径：" + strPoleDiameter + "，保护层厚度：" + strThickness + "，最大信号：" + maxBean.curve_y_original[30]);
//                    }).show();
//                });
//            } else {
//                long id = curveDao.insertOrReplace(curveBean);
//                showDialogg("标定成功\nid：" + id + "钢筋直径：" + strRebarDiameter + "，电杆直径：" + strPoleDiameter + "，保护层厚度：" + strThickness + "，最大信号：" + maxBean.curve_y_original[30]);
//            }
            //
            //----------------------------------------
            //
            dataProcessorHandler.post(() -> {
                //
                showCurveLine();  //显示曲线
                //
                showTableCurve();  //信号曲线数据值
                //
                isCollectStop = false;  //继续检测
            });
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
            chartBeanList.add(new LineChartBean("", 0, i + 1 + "", checkDataList.get(i).y, checkDataList.get(i).curve_y_original));
        }
        if (!chartBeanList.isEmpty()) {
            lineChartManager.showLineChart2(chartBeanList);
        }
    }

    private void showTableCurve() {
        //
        StringBuilder sbd = new StringBuilder();
        //
        for (CheckDataBean bean : checkDataList) {
            sbd.append(Arrays.toString(Arrays.copyOfRange(bean.curve_y_original, 0, 20))).append("\n");
        }
        sbd.append("\n\n");
        for (CheckDataBean bean : checkDataList) {
            sbd.append(Arrays.toString(Arrays.copyOfRange(bean.curve_y_original, 20, 30))).append("\n");
        }
        sbd.append("\n\n");
        for (CheckDataBean bean : checkDataList) {
            sbd.append(Arrays.toString(Arrays.copyOfRange(bean.curve_y_original, 30, 40))).append("\n");
        }
        sbd.append("\n\n");
        for (CheckDataBean bean : checkDataList) {
            sbd.append(Arrays.toString(Arrays.copyOfRange(bean.curve_y_original, 40, 61))).append("\n");
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

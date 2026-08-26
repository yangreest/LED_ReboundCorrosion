package com.example.gtj_f230_rebound_corrosion.f230.activity.setting;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Surface;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_f230_rebound_corrosion.GTJApplication;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.base.activity.BaseActivity;
import com.example.gtj_f230_rebound_corrosion.base.utils.BitmapMatrixUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.CurveFittingUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.StringUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.ViewUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.WifiUtils;
import com.example.gtj_f230_rebound_corrosion.databinding.ActCalibrationWidthBinding;
import com.example.gtj_f230_rebound_corrosion.f230.model.PointBean;
import com.example.gtj_f230_rebound_corrosion.f230.model.SlitWidthBean;
import com.example.gtj_f230_rebound_corrosion.f230.model.WidthCalibrationBean;
import com.example.gtj_f230_rebound_corrosion.f230.view.ModifyCalibrationWidthDialog;
import com.example.gtj_f230_rebound_corrosion.f230.view.TriangleLeftView;
import com.example.gtj_f230_rebound_corrosion.f230.view.TriangleRightView;
import com.orhanobut.logger.Logger;

import org.opencv.ImageProcessUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ChirdSdk.Apis.st_DateInfo;
import ChirdSdk.CHD_Client;
import ChirdSdk.ClientCallBack;
import ChirdSdk.StreamView;

@SuppressLint({"SetTextI18n", "NonConstantResourceId"})
public class CalibrationWidthActivity extends BaseActivity<ActCalibrationWidthBinding> {

    private CHD_Client mClient; /* 客户端类 */
    private StreamView mStreamView; /* 视频刷新控件 */
    private Handler postHandler;
    private float viewRatio;
    private TriangleLeftView leftView;
    private TriangleRightView rightView;
    private String connectWifiSsid;
    private float calWidth1, calWidth2, calWidth3, calWidth4;
    private double[] coefficient;

    @Override
    protected ActCalibrationWidthBinding getBinding() {
        return ActCalibrationWidthBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initLabel() {
        initBatteryReceiver(binding.navigationBar);
        initDeviceStatusExecutor(binding.navigationBar);

        binding.navigationBar.setTitleText("缝宽检测标定");
    }

    @Override
    protected void initView() {


        setFocusable();
        setTextData();
        postHandler = new Handler();
        initStreamView();
        clientCallBackListener();

        binding.etNumber1.setOnClickListener(this::onViewClicked);
        binding.etNumber2.setOnClickListener(this::onViewClicked);
        binding.etNumber3.setOnClickListener(this::onViewClicked);
        binding.etNumber4.setOnClickListener(this::onViewClicked);
        binding.btnReturn.setOnClickListener(this::onViewClicked);

        initData();
    }

    private void setFocusable() {
        binding.etNumber1.setFocusable(false);
        binding.etNumber2.setFocusable(false);
        binding.etNumber3.setFocusable(false);
        binding.etNumber4.setFocusable(false);
    }

    private void setTextData() {
        calWidth1 = SPUtils.getInstance().getFloat("cal-width-1");
        calWidth2 = SPUtils.getInstance().getFloat("cal-width-2");
        calWidth3 = SPUtils.getInstance().getFloat("cal-width-3");
        calWidth4 = SPUtils.getInstance().getFloat("cal-width-4");
        binding.etNumber1.setText(calWidth1 + "");
        binding.etNumber2.setText(calWidth2 + "");
        binding.etNumber3.setText(calWidth3 + "");
        binding.etNumber4.setText(calWidth4 + "");
        calibrationWidth();
    }

    public void calibrationWidth() {
        List<WidthCalibrationBean> calibrationBeanList = new ArrayList<>();
        calibrationBeanList.add(new WidthCalibrationBean(calWidth1, calWidth4 / 10));
        calibrationBeanList.add(new WidthCalibrationBean(calWidth2, calWidth4 / 5));
        calibrationBeanList.add(new WidthCalibrationBean(calWidth3, calWidth4 / 2.5f));
        calibrationBeanList.add(new WidthCalibrationBean(calWidth4, calWidth4));
        coefficient = CurveFittingUtils.getCoefficient(calibrationBeanList);
    }

    public double getWidth(double number) {
        return StringUtils.getRounding(number / (calWidth4 / 5), 2);
    }

    private void initData() {
        getConnectWifi();
        checkWifi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //连接设备并播放
        playVideo();
    }

    @Override
    protected void onPause() {
        super.onPause();
        /* 暂停时关闭视频流并断开连接 */
        if (mClient != null) {
            if (mClient.isOpenVideoStream()) {
                mClient.closeVideoStream();
            }
            if (mClient.isConnect()) {
                mClient.disconnectDevice();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        /* 确保视频流已关闭 */
        if (mClient != null && mClient.isOpenVideoStream()) {
            mClient.closeVideoStream();
        }
    }

    private void playVideo() {
        if (mClient == null) {
            return;
        }
        // 如果已经连接且视频流已打开，不需要重新连接
        if (mClient.isConnect() && mClient.isOpenVideoStream()) {
            return;
        }
        // 先断开旧连接，防止多线程问题
        if (mClient.isConnect()) {
            mClient.closeVideoStream();
            mClient.disconnectDevice();
        }
        // 重新连接
        int ret = mClient.connectDevice("192.168.100.254", "chird");
        android.util.Log.i("WidthDetect", "connectDevice ret=" + ret);
        if (ret == 0 && !mClient.isOpenVideoStream()) {
            mClient.openVideoStream();
        }
    }

    private void initStreamView() {
        //设置VideoLayout宽高
        int fullWidth = SizeUtils.dp2px(656);
        int fullHeight = (int) (fullWidth * StaticConstant.matrixRatio);
        viewRatio = fullWidth / 640.0f;
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) binding.layoutVideo.getLayoutParams();
        layoutParams.width = fullWidth;
        layoutParams.height = fullHeight;
        binding.layoutVideo.setLayoutParams(layoutParams);

        //初始化StreamView
        mStreamView = new StreamView(this, new StreamView.CallBack() {
            @Override
            public void callbackSurface(Surface surface) {

            }

            @Override
            public void callbackDestroy() {

            }

            @Override
            public void onClick() {

            }
        });
        mStreamView.setShowMode(StreamView.SHOW_MODE_BEST_FIT);/* 等比例放大适应屏幕显示 */
        binding.VideoLayout.addView(mStreamView);
        //
        leftView = new TriangleLeftView(this, false, fullHeight >> 1);
        rightView = new TriangleRightView(this, false, fullHeight >> 1);
        binding.VideoLayout.addView(leftView);
        binding.VideoLayout.addView(rightView);
        leftView.setOutsidePoint(fullWidth, fullHeight >> 1);
        rightView.setOutsidePoint(1, fullHeight >> 1);
    }

    private long currentTime;

    private void clientCallBackListener() {
        mClient = new CHD_Client();
        mClient.setClientCallBack(new ClientCallBack() {
            //设备状态改变回调函数
            public void paramChangeCallBack(int changeType) {
                Logger.d("changeType=" + changeType);
            }

            //设备断开连接回调函数
            public void disConnectCallBack() {
            }

            //拍照完成回调函数
            @Override
            public void snapBitmapCallBack(String url, Bitmap bitmap, int width, int height) {

            }

            //录像时间回调函数
            public void recordTimeCountCallBack(String times) {
            }

            //录像结束返回结束时的微缩图回调函数
            @Override
            public void recordStopBitmapCallBack(String url, Bitmap bitmap) {
            }

            boolean isReady = true;

            //当前视频流回调函数
            public void videoStreamBitmapCallBack(Bitmap bitmap) {
                // 防护：bitmap 为空或 Activity 已销毁时直接返回
                if (bitmap == null || isFinishing() || isDestroyed()) {
                    return;
                }
                mStreamView.showBitmap(bitmap);
                if (!isReady) {
                    return;
                }
                if (System.currentTimeMillis() - currentTime < 1000) {
                    return;
                }
                isReady = false;
                currentTime = System.currentTimeMillis();
                // 复合处理：高斯模糊 -> 二值化 -> 高斯模糊，避免多次 bitmap <-> Mat 转换
                ImageProcessUtils.gaussianBlurThresholdBlur(bitmap);
                int[][] matrixData = BitmapMatrixUtils.convertToBMW(bitmap);
                postHandler.post(() -> treat(matrixData));
            }

            @SuppressLint("SetTextI18n")
            private void treat(int[][] matrixData) {
                if (BitmapMatrixUtils.getBitmapBlack(matrixData)) {
                    //防护帽没有摘下
                    binding.tvWidth.setText("请取下镜头保护罩");
                } else {
                    SlitWidthBean slitWidthBean = BitmapMatrixUtils.getBitmapWidth(matrixData);
                    List<PointBean> pointBeanList = ViewUtils.getVerticalOffset(slitWidthBean, viewRatio);
                    leftView.currentX = (float) pointBeanList.get(0).x;
                    rightView.currentX = (float) pointBeanList.get(1).x;
                    leftView.setOutsidePoint(rightView.currentX, rightView.currentY);
                    rightView.setOutsidePoint(leftView.currentX, leftView.currentY);
                    //
                    int fx = Math.abs((int) (rightView.currentX / viewRatio) - (int) (leftView.currentX / viewRatio));
                    int fy = Math.abs((int) (rightView.currentY / viewRatio) - (int) (leftView.currentY / viewRatio));
                    float fz = (float) Math.sqrt(fx * fx + fy * fy);
                    double widthCount = StringUtils.getRounding(CurveFittingUtils.getY(coefficient[0], coefficient[1], coefficient[2], fz), 1);
                    double width = getWidth(widthCount);
                    binding.tvWidth.setText(slitWidthBean.width + " : " + fz + " : " + widthCount + "\n" + width + "mm");
                }
                isReady = true;
            }

            //解码成 RGB565的视频数据，默认不回调，需要回调请调用 setCallbackVideoRGB565Data(true)函数开启
            public void videoStreamDataCallBack(int format, int width, int height, int datalen, byte[] data) {
            }

            //串口接收回调函数
            public void serialDataCallBack(int datalen, byte[] data) {
            }

            //音频接收回调函数(音频采集播放已内部做掉，默认不回调)
            public void audioDataCallBack(int datalen, byte[] data) {
            }

            //SD卡录像回调函数
            @Override
            public void playbackStreamBitmapCallBack(st_DateInfo date, Bitmap bitmap) {

            }
        });
    }

    private void getConnectWifi() {
        connectWifiSsid = SPUtils.getInstance().getString(StaticConstant.SP_F230_WIFI_NAME);
        String wifiSsid = WifiUtils.getInstance(this).getConnectWifiSsid();
        setConnectWifiHint(TextUtils.equals(connectWifiSsid, wifiSsid));
    }

    private void setConnectWifiHint(boolean flag) {
        if (flag) {
            binding.tvHint.setText("缝宽探头已经连接");
            binding.tvHint.setTextColor(ContextCompat.getColor(this, R.color.color63C39B));
            // 只有在未连接或视频流未打开时才尝试连接，避免频繁触发重连
            if (mClient == null || !mClient.isConnect() || !mClient.isOpenVideoStream()) {
                playVideo();
            }
        } else {
            binding.tvHint.setText("正在连接缝宽探头，请确保探头开机启动。");
            binding.tvHint.setTextColor(ContextCompat.getColor(this, R.color.colorE66163));
        }
    }

    private void checkWifi() {
        boolean flag = WifiUtils.getInstance(this).isWifiEnable();
        if (!flag) {
            WifiUtils.getInstance(this).openWifi();
            initWifiTimer(3000);
        } else {
            initWifiTimer(0);
        }
    }

    private Timer wifiTimer;
    private TimerTask wifiTimerTask;
    private Handler wifiHandler;

    private void initWifiTimer(int delayTime) {
        wifiHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (isShow) {
                    String wifiSsid = WifiUtils.getInstance(CalibrationWidthActivity.this).getConnectWifiSsid();
                    boolean flag = TextUtils.equals(connectWifiSsid, wifiSsid);
                    setConnectWifiHint(flag);
                    if (!flag) {
                        WifiUtils.getInstance(CalibrationWidthActivity.this).changeToWifi(connectWifiSsid, "12345678");
                    }
                }
            }
        };
        wifiTimer = new Timer();
        wifiTimerTask = new TimerTask() {
            @Override
            public void run() {
                wifiHandler.sendEmptyMessage(1);
            }
        };
        wifiTimer.schedule(wifiTimerTask, delayTime, 1000 * 3);
    }

    private void clearWifiTimer() {
        if (wifiTimerTask != null) {
            wifiTimerTask.cancel();
            wifiTimerTask = null;
        }
        if (wifiTimer != null) {
            wifiTimer.cancel();
            wifiTimer = null;
        }
        wifiHandler = null;
    }

    @Override
    protected void onDestroy() {
        clearWifiTimer();
        // 清理Client 关闭视频流并断开设备连接
        if (mClient != null) {
            if (mClient.isOpenVideoStream()) {
                mClient.closeVideoStream();
            }
            if (mClient.isConnect()) {
                mClient.disconnectDevice();
            }
            mClient = null;
        }
        // 清理 Handler 的所有回调，防止内存泄漏
        if (postHandler != null) {
            postHandler.removeCallbacksAndMessages(null);
            postHandler = null;
        }
        // 取消所有 Toast，防止 WindowLeaked
        ToastUtils.cancel();
        // 清理视图
        if (binding != null) {
            binding.VideoLayout.removeAllViews();
        }
        super.onDestroy();
    }

    public void onViewClicked(View view) {
        if (view.getId() == R.id.et_number1) {
            final ModifyCalibrationWidthDialog modifyDialog = new ModifyCalibrationWidthDialog(this);
            modifyDialog.setCancelable(false);
            modifyDialog.calibrationWidth = binding.etNumber1.getText().toString();
            modifyDialog.setContent("请输入像素值").setLeftButton("取消", v -> modifyDialog.dismiss()).setRightButton("确定", v -> {
                modifyDialog.dismiss();
                String s = modifyDialog.edittext.getText().toString().trim();
                if (s.length() > 0) {
                    binding.etNumber1.setText(s);
                    calWidth1 = Float.parseFloat(s.trim());
                    SPUtils.getInstance().put("cal-width-1", calWidth1);
                    calibrationWidth();
                }
            }).show();
        } else if (view.getId() == R.id.et_number2) {
            final ModifyCalibrationWidthDialog modifyDialog = new ModifyCalibrationWidthDialog(this);
            modifyDialog.setCancelable(false);
            modifyDialog.calibrationWidth = binding.etNumber2.getText().toString();
            modifyDialog.setContent("请输入像素值").setLeftButton("取消", v -> modifyDialog.dismiss()).setRightButton("确定", v -> {
                modifyDialog.dismiss();
                String s = modifyDialog.edittext.getText().toString().trim();
                if (s.length() > 0) {
                    binding.etNumber2.setText(s);
                    calWidth2 = Float.parseFloat(s.trim());
                    SPUtils.getInstance().put("cal-width-2", calWidth2);
                    calibrationWidth();
                }
            }).show();
        } else if (view.getId() == R.id.et_number3) {
            final ModifyCalibrationWidthDialog modifyDialog = new ModifyCalibrationWidthDialog(this);
            modifyDialog.setCancelable(false);
            modifyDialog.calibrationWidth = binding.etNumber3.getText().toString();
            modifyDialog.setContent("请输入像素值").setLeftButton("取消", v -> modifyDialog.dismiss()).setRightButton("确定", v -> {
                modifyDialog.dismiss();
                String s = modifyDialog.edittext.getText().toString().trim();
                if (!s.isEmpty()) {
                    binding.etNumber3.setText(s);
                    calWidth3 = Float.parseFloat(s.trim());
                    SPUtils.getInstance().put("cal-width-3", calWidth3);
                    calibrationWidth();
                }
            }).show();
        } else if (view.getId() == R.id.et_number4) {
            final ModifyCalibrationWidthDialog modifyDialog = new ModifyCalibrationWidthDialog(this);
            modifyDialog.setCancelable(false);
            modifyDialog.calibrationWidth = binding.etNumber4.getText().toString();
            modifyDialog.setContent("请输入像素值").setLeftButton("取消", v -> modifyDialog.dismiss()).setRightButton("确定", v -> {
                modifyDialog.dismiss();
                String s = modifyDialog.edittext.getText().toString().trim();
                if (!s.isEmpty()) {
                    binding.etNumber4.setText(s);
                    calWidth4 = Float.parseFloat(s.trim());
                    SPUtils.getInstance().put("cal-width-4", calWidth4);
                    calibrationWidth();
                }
            }).show();
        } else if (view.getId() == R.id.btn_return) {
            finish();
        }
    }
}

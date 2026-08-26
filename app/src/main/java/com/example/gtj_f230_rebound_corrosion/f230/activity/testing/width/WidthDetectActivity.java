package com.example.gtj_f230_rebound_corrosion.f230.activity.testing.width;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.base.activity.BaseActivity;
import com.example.gtj_f230_rebound_corrosion.base.model.F230ZoneBean;
import com.example.gtj_f230_rebound_corrosion.base.model.ZoneBean;
import com.example.gtj_f230_rebound_corrosion.base.storage.GreenDaoHelper;
import com.example.gtj_f230_rebound_corrosion.base.utils.BitmapMatrixUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.BitmapUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.CurveFittingUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.ShutUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.StringUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.ViewUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.WifiUtils;
import com.example.gtj_f230_rebound_corrosion.base.view.StyleAlertDialog;
import com.example.gtj_f230_rebound_corrosion.databinding.ActWidthDetectBinding;
import com.example.gtj_f230_rebound_corrosion.f230.adapter.F230SerialNumAdapter;
import com.example.gtj_f230_rebound_corrosion.f230.model.PointBean;
import com.example.gtj_f230_rebound_corrosion.f230.model.SlitWidthBean;
import com.example.gtj_f230_rebound_corrosion.f230.model.WidthBean;
import com.example.gtj_f230_rebound_corrosion.f230.model.WidthCalibrationBean;
import com.example.gtj_f230_rebound_corrosion.f230.view.TriangleLeftView;
import com.example.gtj_f230_rebound_corrosion.f230.view.TriangleRightView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.blankj.utilcode.util.SizeUtils;
import com.orhanobut.logger.Logger;

import org.opencv.ImageProcessUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ChirdSdk.Apis.st_DateInfo;
import ChirdSdk.CHD_Client;
import ChirdSdk.ClientCallBack;
import ChirdSdk.StreamView;

@SuppressLint("SetTextI18n")
public class WidthDetectActivity extends BaseActivity<ActWidthDetectBinding> {
    private BroadcastReceiver shutReceiver;
    private CHD_Client mClient; /* 客户端类 */
    private StreamView mStreamView; /* 视频刷新控件 */
    private Handler postHandler;
    private float viewRatio;
    private TriangleLeftView leftView;
    private TriangleRightView rightView;
    private ZoneBean zoneBean;
    private List<WidthBean> widthBeanList = new ArrayList<>();
    private int showPosition;
    private int fullHeight;
    private int currentId;
    protected ScheduledExecutorService executorService;
    private String connectWifiSsid;
    private float calWidth4;
    private double[] coefficient;
    private F230SerialNumAdapter serialNumAdapter;
    private TriangleLeftView dataLeftView;
    private TriangleRightView dataRightView;

    @Override
    protected ActWidthDetectBinding getBinding() {
        return ActWidthDetectBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initLabel() {
        zoneBean = (ZoneBean) getIntent().getSerializableExtra("ZoneBean");
        if (zoneBean != null && zoneBean.f230ZoneBean == null) {
            zoneBean.f230ZoneBean = new F230ZoneBean();
        }
        binding.navigationBar.setTitleText("缝宽检测").setProjectName(zoneBean.projectName).setZoneName(zoneBean.number);
    }

    @Override
    protected void initView() {
        executorService = Executors.newScheduledThreadPool(1);
        shutReceiver = ShutUtils.registShutActivity(this, StaticConstant.ACTION_TESTING);
        postHandler = new Handler();
        calibrationWidth();
        initStreamView();
        clientCallBackListener();
        initExecutorService();

        binding.btnConfirm.setOnClickListener(this::onViewClicked);
        binding.btnData.setOnClickListener(this::onViewClicked);
        binding.btnReturn.setOnClickListener(this::onViewClicked);
        binding.ivDelete.setOnClickListener(this::onViewClicked);

        initData();
    }

    private void initData() {
        setTextViewData();
        refreshCDId();
    }

    public void calibrationWidth() {
        float calWidth1 = SPUtils.getInstance().getFloat("cal-width-1");
        float calWidth2 = SPUtils.getInstance().getFloat("cal-width-2");
        float calWidth3 = SPUtils.getInstance().getFloat("cal-width-3");
        calWidth4 = SPUtils.getInstance().getFloat("cal-width-4");
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showBackHint();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setTextViewData() {
        binding.tvNumber.setText(zoneBean.number);
        binding.tvRemark.setText(zoneBean.remark);
        if (TextUtils.isEmpty(zoneBean.f230ZoneBean.strZoneList)) {
            return;
        }
        widthBeanList = new Gson().fromJson(zoneBean.f230ZoneBean.strZoneList, new TypeToken<List<WidthBean>>() {
        }.getType());
        serialNumAdapter.submitList(widthBeanList);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void onViewClicked(View view) {
        if (view.getId() == R.id.btn_data) {
            if (widthBeanList.isEmpty()) {
                ToastUtils.showShort("暂无测点数据");
                return;
            }
            widthBeanList.get(0).isSelected = true;
            serialNumAdapter.notifyDataSetChanged();
            binding.llData.setVisibility(VISIBLE);
            binding.llDetect.setVisibility(GONE);
            binding.btnData.setVisibility(GONE);
            binding.btnConfirm.setVisibility(GONE);
            setDataInfo(0);
        } else if (view.getId() == R.id.btn_confirm) {
            if (mClient != null) {
                mClient.snapShot(null);
            }
        } else if (view.getId() == R.id.btn_return) {
            if (binding.btnData.getVisibility() == VISIBLE) {
                showBackHint();
            } else {
                binding.llData.setVisibility(GONE);
                binding.llDetect.setVisibility(VISIBLE);
                binding.btnConfirm.setVisibility(VISIBLE);
                binding.btnData.setVisibility(VISIBLE);
                refreshCDId();
            }
        } else if (view.getId() == R.id.iv_delete) {
            StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this);
            styleAlertDialog.setContent("确定删除“测点" + (showPosition + 1) + "”的数据？").setLeftButton("取消", v1 -> styleAlertDialog.dismiss()).setRightButton("确定", v1 -> {
                styleAlertDialog.dismiss();
                widthBeanList.remove(showPosition);
                if (widthBeanList.isEmpty()) {
                    binding.llData.setVisibility(GONE);
                    binding.llDetect.setVisibility(VISIBLE);
                    binding.btnConfirm.setVisibility(VISIBLE);
                    binding.btnData.setVisibility(VISIBLE);
                    refreshCDId();
                } else {
                    serialNumAdapter.notifyDataSetChanged();
                    setDataInfo(0);
                }
            }).show();
        }
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

    private void initExecutorService() {
        connectWifiSsid = SPUtils.getInstance().getString(StaticConstant.SP_F230_WIFI_NAME);
        executorService.scheduleWithFixedDelay(new TimerTask() {
            @Override
            public void run() {
                if (isShow) {
                    String wifiSsid = WifiUtils.getInstance(WidthDetectActivity.this).getConnectWifiSsid();
                    boolean flag = TextUtils.equals(connectWifiSsid, wifiSsid);
                    if (!flag) {
                        WifiUtils.getInstance(WidthDetectActivity.this).changeToWifi(connectWifiSsid, "12345678");
                    }
                    // 只有在未连接或视频流未打开时才尝试连接，避免频繁触发重连
                    if (mClient == null || !mClient.isConnect() || !mClient.isOpenVideoStream()) {
                        new Handler(Looper.getMainLooper()).post(() -> playVideo());
                    }
                }
            }
        }, 0, 6, TimeUnit.SECONDS);
    }

    private void initStreamView() {
        //设置VideoLayout宽高
        int fullWidth = SizeUtils.dp2px(656);
        fullHeight = (int) (fullWidth * StaticConstant.matrixRatio);
        viewRatio = fullWidth / 640.0f;
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) binding.layoutVideo.getLayoutParams();
        layoutParams.width = fullWidth;
        layoutParams.height = fullHeight;
        binding.layoutVideo.setLayoutParams(layoutParams);
        initDataLayout(fullWidth);

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
    }

    private void initDataLayout(int fullWidth) {
        LinearLayout.LayoutParams dataLayoutParams = (LinearLayout.LayoutParams) binding.layoutDataVideo.getLayoutParams();
        dataLayoutParams.width = fullWidth;
        dataLayoutParams.height = fullHeight;
        binding.layoutDataVideo.setLayoutParams(dataLayoutParams);

        binding.rvSerialNum.setLayoutManager(new LinearLayoutManager(this));
        serialNumAdapter = new F230SerialNumAdapter(this);
        binding.rvSerialNum.setAdapter(serialNumAdapter);
        serialNumAdapter.setOnItemClickListener((adapter, view, position) -> setDataInfo(position));
        serialNumAdapter.submitList(widthBeanList);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setDataInfo(int position) {
        for (int i = 0; i < widthBeanList.size(); i++) {
            widthBeanList.get(i).isSelected = false;
        }
        widthBeanList.get(position).isSelected = true;
        showPosition = position;
        serialNumAdapter.notifyDataSetChanged();

        if (binding.VideoLayoutData.getChildCount() == 0) {
            ImageView imageView = new ImageView(this);
            binding.VideoLayoutData.addView(imageView);
            dataLeftView = new TriangleLeftView(this, false, widthBeanList.get(showPosition).widthLeftX, widthBeanList.get(showPosition).widthLeftY);
            dataRightView = new TriangleRightView(this, false, widthBeanList.get(showPosition).widthRightX, widthBeanList.get(showPosition).widthRightY);
            binding.VideoLayoutData.addView(dataLeftView);
            binding.VideoLayoutData.addView(dataRightView);
        }
        WidthBean showWidthBean = widthBeanList.get(showPosition);
        Glide.with(this).load(showWidthBean.imagePath).into((ImageView) binding.VideoLayoutData.getChildAt(0));
        dataLeftView.currentX = showWidthBean.widthLeftX;
        dataLeftView.currentY = showWidthBean.widthLeftY;
        dataLeftView.setOutsidePoint(showWidthBean.widthRightX, showWidthBean.widthRightY);
        dataRightView.currentX = showWidthBean.widthRightX;
        dataRightView.currentY = showWidthBean.widthRightY;
        dataRightView.setOutsidePoint(showWidthBean.widthLeftX, showWidthBean.widthLeftY);

        binding.tvTimeData.setText(showWidthBean.time);
        binding.tvNumData.setText("测点：" + showWidthBean.id);
        binding.tvWidthData.setText(showWidthBean.width + "mm");
        binding.tvJcd.setText(showWidthBean.id + "");
    }

    private double measureWidth;

    @SuppressLint("NotifyDataSetChanged")
    private void storageData(String url) {
        // 检查 Activity 是否已销毁
        if (isFinishing() || isDestroyed()) {
            return;
        }
        //存储
        WidthBean widthBean = new WidthBean();
        widthBean.id = currentId;
        widthBean.time = StringUtils.getTime();
        widthBean.widthLeftX = leftView.currentX;
        widthBean.widthRightX = rightView.currentX;
        widthBean.widthLeftY = leftView.currentY;
        widthBean.widthRightY = rightView.currentY;
        widthBean.width = measureWidth;
        //
        widthBean.imagePath = url;
        //
        widthBean.widthLeftXRaw = widthBean.widthLeftX;
        widthBean.widthRightXRaw = widthBean.widthRightX;
        widthBean.widthLeftYRaw = widthBean.widthLeftY;
        widthBean.widthRightYRaw = widthBean.widthRightY;
        widthBean.widthRaw = widthBean.width;
        widthBeanList.add(widthBean);
        if (binding.btnData.getVisibility() == GONE) {
            serialNumAdapter.notifyDataSetChanged();
        }
        widthBeanList.sort(Comparator.comparingInt(o -> o.id));
        ToastUtils.showShort("拍照成功");
        //
        refreshCDId();
    }

    private long currentTime;

    private void clientCallBackListener() {
        mClient = new CHD_Client();
        String storagePath = StaticConstant.dataFilePathCache + "image_native/" + zoneBean.number + "/";
        BitmapUtils.checkExists(storagePath);
        mClient.setStoragePath(storagePath);
        mClient.setClientCallBack(new ClientCallBack() {
            //设备状态改变回调函数
            public void paramChangeCallBack(int changeType) {
                if (changeType == 32) {  //设备按拍照按钮触发
                    mClient.snapShot(null);
                }
            }

            //设备断开连接回调函数
            public void disConnectCallBack() {
            }

            //拍照完成回调函数
            @Override
            public void snapBitmapCallBack(String url, Bitmap bitmap, int width, int height) {
                storageData(url);
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
                try {
                    // 复合处理：高斯模糊 -> 二值化 -> 高斯模糊，避免多次 bitmap <-> Mat 转换
                    ImageProcessUtils.gaussianBlurThresholdBlur(bitmap);
                    int[][] matrixData = BitmapMatrixUtils.convertToBMW(bitmap);
                    if (postHandler != null) {
                        postHandler.post(() -> treat(matrixData));
                    }
                } catch (Exception e) {
                    Logger.e(e, "ImageProcessUtils_gaussianBlurThresholdBlur");
                } finally {
                    isReady = true;
                }
            }

            private void treat(int[][] matrixData) {
                if (BitmapMatrixUtils.getBitmapBlack(matrixData)) {
                    //防护帽没有摘下
                    binding.tvWidth.setText("0mm");
                    binding.tvHint.setText("请取下镜头保护罩");
                } else {
                    binding.tvHint.setText("");
                    SlitWidthBean slitWidthBean = BitmapMatrixUtils.getBitmapWidth(matrixData);
                    List<PointBean> pointBeanList = ViewUtils.getVerticalOffset(slitWidthBean, viewRatio, fullHeight >> 1);
                    leftView.currentX = (float) pointBeanList.get(0).x;
                    leftView.currentY = (float) pointBeanList.get(0).y;
                    rightView.currentX = (float) pointBeanList.get(1).x;
                    rightView.currentY = (float) pointBeanList.get(1).y;
                    //
                    leftView.setOutsidePoint(rightView.currentX, rightView.currentY);
                    rightView.setOutsidePoint(leftView.currentX, leftView.currentY);
                    //
                    int fx = Math.abs((int) (rightView.currentX / viewRatio) - (int) (leftView.currentX / viewRatio));
                    int fy = Math.abs((int) (rightView.currentY / viewRatio) - (int) (leftView.currentY / viewRatio));
                    float fz = (float) Math.sqrt(fx * fx + fy * fy);
                    double widthCount = StringUtils.getRounding(CurveFittingUtils.getY(coefficient[0], coefficient[1], coefficient[2], fz), 1);
                    measureWidth = getWidth(widthCount);
                    binding.tvWidth.setText(measureWidth + "mm");
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

    private void showBackHint() {
        final StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this);
        styleAlertDialog.setContent("是否退出检测？").setLeftButton("取消", v -> styleAlertDialog.dismiss()).setRightButton("确定", v -> {
            styleAlertDialog.dismiss();
            storageDatabase();
            Intent intent = new Intent();
            intent.putExtra("ZoneBean", zoneBean);
            setResult(RESULT_OK, intent);
            finish();
        }).show();
    }

    private void storageDatabase() {
        if (zoneBean == null) {
            return;
        }
        if (zoneBean.f230ZoneBean == null) {
            zoneBean.f230ZoneBean = new F230ZoneBean();
        }
        if (!widthBeanList.isEmpty()) {
            zoneBean.f230ZoneBean.strZoneList = new Gson().toJson(widthBeanList);
        } else {
            zoneBean.f230ZoneBean.strZoneList = "";
        }
        zoneBean.f230ZoneBean.zoneCount = widthBeanList.size();
        zoneBean.f230ZoneBean.startTime = StringUtils.getTime();
        GreenDaoHelper.getDaoSession(WidthDetectActivity.this).getZoneBeanDao().insertOrReplace(zoneBean);
    }

    private void refreshCDId() {
        for (int i = 0; i < widthBeanList.size(); i++) {
            widthBeanList.get(i).id = i + 1;
        }
        currentId = widthBeanList.isEmpty() ? 1 : widthBeanList.get(widthBeanList.size() - 1).id + 1;
        binding.tvNum.setText("测点： " + currentId);
        if (binding.btnData.getVisibility() == VISIBLE) {
            binding.tvJcd.setText(currentId + "/" + currentId);
        }
    }

    @Override
    protected void onDestroy() {
        // 1. 先停止定时任务
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
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
        unregisterReceiver(shutReceiver);
    }
}

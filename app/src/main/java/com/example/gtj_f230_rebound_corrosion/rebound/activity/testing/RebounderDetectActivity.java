package com.example.gtj_f230_rebound_corrosion.rebound.activity.testing;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.base.activity.BaseActivity;
import com.example.gtj_f230_rebound_corrosion.base.adapter.DetectSerialNumAdapter;
import com.example.gtj_f230_rebound_corrosion.base.model.DetectSerialNumBean;
import com.example.gtj_f230_rebound_corrosion.base.model.RebounderZoneBean;
import com.example.gtj_f230_rebound_corrosion.base.model.ZoneBean;
import com.example.gtj_f230_rebound_corrosion.base.storage.GreenDaoHelper;
import com.example.gtj_f230_rebound_corrosion.base.utils.BeepSoundVibrateUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.BytesConversion;
import com.example.gtj_f230_rebound_corrosion.base.utils.OptionsPickerViewUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.OtherUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.StringUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.rectify.RebounderUtils;
import com.example.gtj_f230_rebound_corrosion.base.view.StyleAlertDialog;
import com.example.gtj_f230_rebound_corrosion.databinding.ActRebounderDetectBinding;
import com.example.gtj_f230_rebound_corrosion.rebound.adapter.TestingListAdapter1;
import com.example.gtj_f230_rebound_corrosion.rebound.model.RebounderBean;
import com.example.gtj_f230_rebound_corrosion.rebound.model.RebounderConfigBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.inuker.bluetooth.library.BluetoothManager;
import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressLint({"SetTextI18n", "DefaultLocale"})
public class RebounderDetectActivity extends BaseActivity<ActRebounderDetectBinding> {
    private int pouringSurfaceId, detectionSurfaceId, detectionAngleId, detectionPumpingId = -1, detectionStandardId;
    private RebounderConfigBean rebounderConfigBean;
    private RebounderConfigBean.RConfigBean rConfigBean;
    private int color999, color333;

    private final List<RebounderBean> testingList = new ArrayList<>();
    private DetectSerialNumAdapter serialNumAdapter;
    private final List<DetectSerialNumBean> serialNumList = new ArrayList<>();
    private TestingListAdapter1 testingListAdapter1;
    private BeepSoundVibrateUtils beepSoundVibrateUtils;
    private String spMac;
    private int testCountForArea;  //每个测区测点数
    private ZoneBean zoneBean;

    @Override
    protected ActRebounderDetectBinding getBinding() {
        return ActRebounderDetectBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initLabel() {
        zoneBean = (ZoneBean) getIntent().getSerializableExtra("ZoneBean");
        initBatteryReceiver(binding.navigationBar);
        initDeviceStatusExecutor(binding.navigationBar);

        binding.navigationBar.setTitleText("回弹检测").setProjectName(zoneBean.projectName).setZoneName(zoneBean.number);
    }

    @Override
    protected void initView() {
        spMac = SPUtils.getInstance().getString(StaticConstant.SP_REBOUND_BLE_MAC);
        beepSoundVibrateUtils = new BeepSoundVibrateUtils(this);

        binding.etBzth.setOnClickListener(this::onViewClicked);
        binding.etJjm.setOnClickListener(this::onViewClicked);
        binding.etJcm.setOnClickListener(this::onViewClicked);
        binding.etJd.setOnClickListener(this::onViewClicked);
        binding.etSfbs.setOnClickListener(this::onViewClicked);
        binding.etJcyj.setOnClickListener(this::onViewClicked);
        binding.btnReturn.setOnClickListener(this::onViewClicked);

        color333 = ContextCompat.getColor(this, R.color.color333);
        color999 = ContextCompat.getColor(this, R.color.color999);

        initData();
    }

    private void initData() {
        //开始检测，获取上次存储的配置，检测参数可修改
        String strZoneBean = SPUtils.getInstance().getString(StaticConstant.SP_REBOUND_LAST_CONFIG);
        if (zoneBean.rebounderZoneBean == null) {
            if (!TextUtils.isEmpty(strZoneBean)) {
                zoneBean.rebounderZoneBean = new Gson().fromJson(strZoneBean, new TypeToken<RebounderZoneBean>() {
                }.getType());
                zoneBean.rebounderZoneBean.zoneCount = 0;
                zoneBean.rebounderZoneBean.strZoneList = "";

                binding.etBzth.setText(zoneBean.rebounderZoneBean.detectionStandardCarbonization + "mm");
                binding.etBzth.setTag(zoneBean.rebounderZoneBean.detectionStandardCarbonization);
                binding.etJcm.setText(StaticConstant.detectionSurface.get(zoneBean.rebounderZoneBean.detectionSurface - 1));
                detectionSurfaceId = zoneBean.rebounderZoneBean.detectionSurface;
                binding.etJd.setText(StaticConstant.getDetectionAngle(zoneBean.rebounderZoneBean.detectionStandard).get(zoneBean.rebounderZoneBean.detectionAngle - 1) + "°");
                detectionAngleId = zoneBean.rebounderZoneBean.detectionAngle;
                binding.etSfbs.setText(StaticConstant.detectionPumping.get(zoneBean.rebounderZoneBean.isPumping));
                detectionPumpingId = zoneBean.rebounderZoneBean.isPumping;
                binding.etJcyj.setText(StaticConstant.detectionStandard.get(zoneBean.rebounderZoneBean.detectionStandard - 1));
                detectionStandardId = zoneBean.rebounderZoneBean.detectionStandard;

                setTestingConfig();
            } else {
                zoneBean.rebounderZoneBean = new RebounderZoneBean();
                binding.etBzth.setText("");
                binding.etBzth.setTag(null);
                binding.etJcm.setText("");
                detectionSurfaceId = 0;
                binding.etJd.setText("");
                detectionAngleId = 0;
                binding.etSfbs.setText("");
                detectionPumpingId = -1;
                binding.etJcyj.setText(StaticConstant.detectionStandard.get(0));
                detectionStandardId = 1;
                setTestingConfig();
            }
        }
        initRecyclerView();
//        if (!TextUtils.isEmpty(rebounderZoneBean.strZoneList)) {
//            testingList = new Gson().fromJson(rebounderZoneBean.strZoneList, new TypeToken<List<RebounderBean>>() {
//            }.getType());
//        }
        refreshTestingList();
        connectDevice();
        testCountForArea = zoneBean.rebounderZoneBean.detectionStandard == 28 ? 10 : 16;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initRecyclerView() {
        binding.rvSerialNum.setLayoutManager(new LinearLayoutManager(this));
        serialNumAdapter = new DetectSerialNumAdapter(this);
        binding.rvSerialNum.setAdapter(serialNumAdapter);
        serialNumAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (testingList.size() >= zoneBean.rebounderZoneBean.zoneCount && testingList.size() == serialNumList.size()) {
                for (int i = 0; i < serialNumList.size(); i++) {
                    serialNumList.get(i).isSelected = false;
                }
                serialNumList.get(position).isSelected = true;
                testingListAdapter1.submitList(testingList.get(position).testNumber);
                serialNumAdapter.notifyDataSetChanged();
            }
        });
        serialNumAdapter.submitList(serialNumList);

        binding.rvData.setLayoutManager(new GridLayoutManager(this, 4));
        testingListAdapter1 = new TestingListAdapter1();
        binding.rvData.setAdapter(testingListAdapter1);
    }

    public void onViewClicked(View view) {
        String currentContent;
        if (view.getId() == R.id.et_bzth) {
            if (detectionStandardId == 0) {
                ToastUtils.showShort("请先选择检测依据");
                return;
            }
            if (!rConfigBean.isCar) {
                return;
            }
            currentContent = binding.etBzth.getText().toString();
            currentContent = TextUtils.isEmpty(currentContent) ? "" : currentContent.substring(0, currentContent.length() - 2);
            OptionsPickerViewUtils.showSingleColumnPickerView(this, "请选择", "mm", StaticConstant.getDetectionStandardCarbonization(detectionStandardId), currentContent, position -> {
                binding.etBzth.setText(StaticConstant.getDetectionStandardCarbonization(detectionStandardId).get(position) + "mm");
                binding.etBzth.setTag(Double.parseDouble(StaticConstant.getDetectionStandardCarbonization(detectionStandardId).get(position)));
            });
        } else if (view.getId() == R.id.et_jjm) {
            if (detectionStandardId == 0) {
                ToastUtils.showShort("请先选择检测依据");
                return;
            }
            currentContent = binding.etJjm.getText().toString();
            OptionsPickerViewUtils.showSingleColumnPickerView(this, "请选择", "", StaticConstant.pouringSurface, currentContent, position -> {
                binding.etJjm.setText(StaticConstant.pouringSurface.get(position));
                pouringSurfaceId = position;
                setJJM();
            });
        } else if (view.getId() == R.id.et_jcm) {
            if (detectionStandardId == 0) {
                ToastUtils.showShort("请先选择检测依据");
                return;
            }
            if (!rConfigBean.isSurface) {
                return;
            }
            if (detectionStandardId == 5 && pouringSurfaceId == 1) {
                return;
            }
            currentContent = binding.etJcm.getText().toString();
            OptionsPickerViewUtils.showSingleColumnPickerView(this, "请选择", "", StaticConstant.detectionSurface, currentContent, position -> {
                binding.etJcm.setText(StaticConstant.detectionSurface.get(position));
                detectionSurfaceId = position + 1;
            });
        } else if (view.getId() == R.id.et_jd) {
            if (detectionStandardId == 0) {
                ToastUtils.showShort("请先选择检测依据");
                return;
            }
            if (!rConfigBean.isAngle) {
                return;
            }
            if (detectionStandardId == 5 && pouringSurfaceId == 1) {
                return;
            }
            currentContent = binding.etJd.getText().toString();
            currentContent = TextUtils.isEmpty(currentContent) ? "" : currentContent.substring(0, currentContent.length() - 1);
            OptionsPickerViewUtils.showSingleColumnPickerView(this, "请选择", "°", StaticConstant.getDetectionAngle(detectionStandardId), currentContent, position -> {
                binding.etJd.setText(StaticConstant.getDetectionAngle(detectionStandardId).get(position) + "°");
                detectionAngleId = position + 1;
            });
        } else if (view.getId() == R.id.et_sfbs) {
            if (detectionStandardId == 0) {
                ToastUtils.showShort("请先选择检测依据");
                return;
            }
            if (!rebounderConfigBean.isPumping) {
                return;
            }
            currentContent = binding.etSfbs.getText().toString();
            OptionsPickerViewUtils.showSingleColumnPickerView(this, "请选择", "", StaticConstant.detectionPumping, currentContent, position -> {
                binding.etSfbs.setText(StaticConstant.detectionPumping.get(position));
                detectionPumpingId = position;
                rConfigBean = rebounderConfigBean.rConfigBeanList.get(position);
                setTestingConfig1();
            });
        } else if (view.getId() == R.id.et_jcyj) {
            currentContent = binding.etJcyj.getText().toString();
            OptionsPickerViewUtils.showSingleColumnPickerView(this, "请选择", "", StaticConstant.detectionStandard, currentContent, position -> {
                binding.etJcyj.setText(StaticConstant.detectionStandard.get(position));
                detectionStandardId = position + 1;
                setTestingConfig();
            });
        } else if (view.getId() == R.id.btn_return) {
            showBackHint(200);
        }
    }

    private void setTestingConfig() {
        String strName = StaticConstant.detectionStandard.get(detectionStandardId - 1);
        rebounderConfigBean = new RebounderConfigBean();
        for (int i = 0; i < StaticConstant.getRebounderConfigBeanList().size(); i++) {
            if (TextUtils.equals(StaticConstant.getRebounderConfigBeanList().get(i).curve, strName)) {
                rebounderConfigBean = StaticConstant.getRebounderConfigBeanList().get(i);
                break;
            }
        }
        rConfigBean = rebounderConfigBean.rConfigBeanList.get(0);
        setTestingConfig1();
    }

    private void setTestingConfig1() {
        detectionPumpingId = rConfigBean.pumping;
        detectionSurfaceId = rConfigBean.surface;
        detectionAngleId = rConfigBean.angle;
        binding.etSfbs.setText(StaticConstant.detectionPumping.get(detectionPumpingId));
        binding.etJcm.setText(StaticConstant.detectionSurface.get(detectionSurfaceId - 1));
        binding.etJd.setText(StaticConstant.getDetectionAngle(detectionStandardId).get(detectionAngleId - 1) + "°");
        binding.etBzth.setText("0mm");
        binding.etBzth.setTag(0.0);
        binding.etJcm.setTextColor(rConfigBean.isSurface ? color333 : color999);
        binding.etJd.setTextColor(rConfigBean.isAngle ? color333 : color999);
        binding.etBzth.setTextColor(rConfigBean.isCar ? color333 : color999);
        binding.etSfbs.setTextColor(rebounderConfigBean.isPumping ? color333 : color999);
        if (detectionStandardId == 5 && detectionPumpingId == 1) {
            binding.layoutJjm.setVisibility(View.VISIBLE);
        } else {
            binding.layoutJjm.setVisibility(View.GONE);
            pouringSurfaceId = 0;
        }
        binding.etJjm.setText(StaticConstant.pouringSurface.get(pouringSurfaceId));
    }

    private void setJJM() {
        if (pouringSurfaceId == 1) {
            detectionSurfaceId = 3;
            detectionAngleId = 1;
            binding.etJcm.setText(StaticConstant.detectionSurface.get(detectionSurfaceId - 1));
            binding.etJd.setText(StaticConstant.getDetectionAngle(detectionStandardId).get(detectionAngleId - 1) + "°");
            binding.etJcm.setTextColor(color999);
            binding.etJd.setTextColor(color999);
        } else {
            detectionSurfaceId = rConfigBean.surface;
            detectionAngleId = rConfigBean.angle;
            binding.etJcm.setText(StaticConstant.detectionSurface.get(detectionSurfaceId - 1));
            binding.etJd.setText(StaticConstant.getDetectionAngle(detectionStandardId).get(detectionAngleId - 1) + "°");
            binding.etJcm.setTextColor(rConfigBean.isSurface ? color333 : color999);
            binding.etJd.setTextColor(rConfigBean.isAngle ? color333 : color999);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showBackHint(200);
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showBackHint(long time) {
        final StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this);
        if (zoneBean.rebounderZoneBean.zoneCount > 0 && testingList.size() >= zoneBean.rebounderZoneBean.zoneCount) {
            if (detectionSurfaceId == 0 || binding.etBzth.getTag() == null || detectionAngleId == 0 || detectionPumpingId == -1 || detectionStandardId == 0) {
                testingListAdapter1.submitList(testingList.get(testingList.size() - 1).testNumber);
                styleAlertDialog.setContent("检测完成，请填写完整参数信息").setOKButton("去填写", v -> styleAlertDialog.dismiss()).show();
            } else {
                zoneBean.rebounderZoneBean.detectionStandardCarbonization = (double) binding.etBzth.getTag();
                zoneBean.rebounderZoneBean.pouringSurface = pouringSurfaceId;
                zoneBean.rebounderZoneBean.detectionSurface = detectionSurfaceId;
                zoneBean.rebounderZoneBean.detectionAngle = detectionAngleId;
                zoneBean.rebounderZoneBean.isPumping = detectionPumpingId;
                zoneBean.rebounderZoneBean.detectionStandard = detectionStandardId;
                zoneBean.rebounderZoneBean.startTime = StringUtils.getTime();
                //
                zoneBean.rebounderZoneBean.strZoneList = new Gson().toJson(testingList);
                //计算
                RebounderUtils.calculate(testingList, zoneBean.rebounderZoneBean);
                //
                GreenDaoHelper.getDaoSession(this).getZoneBeanDao().insertOrReplace(zoneBean);
                //
                SPUtils.getInstance().put(StaticConstant.SP_REBOUND_LAST_CONFIG, new Gson().toJson(zoneBean.rebounderZoneBean));
                new Handler(Looper.getMainLooper()).postDelayed(this::finish, time);
            }
        } else {
            styleAlertDialog.setContent("检测未完成，是否退出检测？").setLeftButton("取消", v -> styleAlertDialog.dismiss()).setRightButton("确定", v -> {
                styleAlertDialog.dismiss();
                finish();
            }).show();
        }
    }

    private void connectDevice() {
        if (isExist) {
            BluetoothManager.getClient().registerConnectStatusListener(spMac, mBleConnectStatusListener);
            if (BluetoothManager.getConnectStatus(spMac)) {
                notifyBluetoothDevice();
            } else {
                progressiveDialog.show();
                BluetoothManager.connectBluetoothDevice(spMac, flag -> {
                });
            }
        }
    }

    private final BleConnectStatusListener mBleConnectStatusListener = new BleConnectStatusListener() {
        @Override
        public void onConnectStatusChanged(String mac, int status) {
            progressiveDialog.dismiss();
            if (status == Constants.STATUS_CONNECTED) {
                Logger.d(mac + " =STATUS_CONNECTED");
                notifyBluetoothDevice();
            } else if (status == Constants.STATUS_DISCONNECTED) {
                Logger.d(mac + "=STATUS_DISCONNECTED");
                binding.navigationBar.postDelayed(() -> connectDevice(), 1000);
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
                String string = StringUtils.byteArraytoHex(value);
                notifyRefreshData(string);
            }

            @Override
            public void onResponse(int code) {
            }
        });
    }

    private final List<Integer> tempList = new ArrayList<>();
    private final List<Integer> outliersList = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    private void notifyRefreshData(String string) {
        //上传协议：ff X1 X2 X3 fe (X1：测区数量、X2：回弹值、X3=X1+X2)
        //重测协议：fd X1 X2 X3 fc (X1：测点位置、X2：回弹值、X3=X1+X2)
        //X1：重测测区号（0-f代表1-16测区）*16 + 重测测点位置（0-f代表1-16测点）
        String[] array = string.split(" ");
        if (array.length != 5) {
            return;
        }
        beepSoundVibrateUtils.playBeepSoundAndVibrate();
        if (TextUtils.equals("FF", array[0])) {  //上传
            int number1 = BytesConversion.hex2decimal(array[1]);  //测区数量
            int number2 = BytesConversion.hex2decimal(array[2]);  //回弹值
//            int number3 = BytesConversion.hex2decimal(array[3]);  //校验和X3=X1+X2
            zoneBean.rebounderZoneBean.zoneCount = number1;
            tempList.add(number2);
            if (serialNumList.size() != number1) {
                serialNumList.clear();
                for (int i = 0; i < number1; i++) {
                    serialNumList.add(new DetectSerialNumBean(i + 1, i == 0));
                }
                serialNumAdapter.notifyDataSetChanged();
            }
        } else if (TextUtils.equals("FD", array[0])) {  //重测
            //FD 11 1B 2C FC
            //FD 01 1E 1F FC
            //FD 00 32 32 FC
            //11：第二个测区第二个测点
            int zoneIndex = BytesConversion.hex2decimal(array[1].charAt(0) + "");  //测区index
            int testIndex = BytesConversion.hex2decimal(array[1].charAt(1) + "");  //测点index
            int number2 = BytesConversion.hex2decimal(array[2]);  //回弹值
            if (zoneIndex < testingList.size()) {
                testingList.get(zoneIndex).outliersNumber.add(testingList.get(zoneIndex).testNumber.get(testIndex));
                testingList.get(zoneIndex).testNumber.set(testIndex, number2);
                //存储
                RebounderUtils.calculate(testingList, zoneBean.rebounderZoneBean);
            } else {
                //当前测区
                outliersList.add(tempList.get(testIndex));
                tempList.set(testIndex, number2);
            }
        }
        //
        storageDatabase();
        refreshTestingList();
        complete();
    }

    private void complete() {
        if (testingList.size() >= zoneBean.rebounderZoneBean.zoneCount) {
            //测试完成 跳转到结果页
            Logger.d("测试完成");
            showBackHint(1000);
        }
    }

    private void refreshTestingList() {
//        int current = testingList.size() + 1;
//        current = Math.min(current, rebounderZoneBean.zoneCount);
//        binding.tvAreaNumber.setText("当前测区：" + current + "/" + rebounderZoneBean.zoneCount);
        testingListAdapter1.submitList(tempList);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void storageDatabase() {
        //采集数据 16个为一个测区
        if (tempList.size() == testCountForArea) {
            RebounderBean rebounderBean = new RebounderBean(zoneBean.rebounderZoneBean.pouringSurface, zoneBean.rebounderZoneBean.detectionSurface, zoneBean.rebounderZoneBean.detectionAngle, zoneBean.rebounderZoneBean.isPumping, zoneBean.rebounderZoneBean.detectionStandardCarbonization, zoneBean.rebounderZoneBean.b, zoneBean.rebounderZoneBean.detectionStandard, OtherUtils.deepCopy(tempList), OtherUtils.deepCopy(outliersList));
            testingList.add(rebounderBean);
            tempList.clear();
            outliersList.clear();
            if (!serialNumList.isEmpty()) {
                for (int i = 0; i < serialNumList.size(); i++) {
                    if (serialNumList.get(i).isSelected && i + 1 < serialNumList.size()) {
                        serialNumList.get(i).isSelected = false;
                        serialNumList.get(i + 1).isSelected = true;
                        serialNumAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beepSoundVibrateUtils.clear();
        BluetoothManager.unNotifyBluetoothDevice(spMac);
        BluetoothManager.getClient().unregisterConnectStatusListener(spMac, null);
    }
}

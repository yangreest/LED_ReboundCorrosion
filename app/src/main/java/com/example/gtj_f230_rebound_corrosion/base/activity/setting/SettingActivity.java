package com.example.gtj_f230_rebound_corrosion.base.activity.setting;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.base.activity.BaseActivity;
import com.example.gtj_f230_rebound_corrosion.base.view.DialogLogin;
import com.example.gtj_f230_rebound_corrosion.base.view.StyleAlertDialog;
import com.example.gtj_f230_rebound_corrosion.corrosion.activity.setting.CalibrationDeviceActivity;
import com.example.gtj_f230_rebound_corrosion.corrosion.activity.setting.MeteringDeviceActivity;
import com.example.gtj_f230_rebound_corrosion.databinding.ActSettingBinding;
import com.example.gtj_f230_rebound_corrosion.f230.activity.setting.CalibrationWidthActivity;
import com.example.gtj_f230_rebound_corrosion.f230.activity.setting.WifiListActivity;
import com.example.gtj_f230_rebound_corrosion.rebound.activity.setting.BluetoothPrinterActivity;
import com.example.gtj_f230_rebound_corrosion.rebound.activity.setting.InstrumentCalibrationActivity;

@SuppressLint({"InflateParams", "SetTextI18n"})
public class SettingActivity extends BaseActivity<ActSettingBinding> {
    private static final String strVersion = "Ver:1.0-202604151401";

    @Override
    protected ActSettingBinding getBinding() {
        return ActSettingBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initLabel() {
        initBatteryReceiver(binding.navigationBar);
        initDeviceStatusExecutor(binding.navigationBar);
        binding.navigationBar.setTitleText("系统设置");
        binding.layoutAboutUs.setVisibility(StaticConstant.isNeutral ? View.GONE : View.VISIBLE);
        binding.tvVersion1.setText(strVersion);
        binding.tvVersion2.setText(strVersion);
        boolean isCorrosion = TextUtils.equals(StaticConstant.APP_TYPE_DEFAULT, StaticConstant.APP_CORROSION);
        binding.tv01.setText(isCorrosion ? "混凝土电杆钢筋锈蚀检测仪" : "混凝土电杆钢筋保护层检测仪");
    }

    @Override
    protected void initView() {
        binding.btnF230Connect.setOnClickListener(this::onViewClicked);
        binding.btnF230Calibration.setOnClickListener(this::onViewClicked);
        binding.btnReboundConnect.setOnClickListener(this::onViewClicked);
        binding.btnReboundPrinter.setOnClickListener(this::onViewClicked);
        binding.btnReboundCalibration.setOnClickListener(this::onViewClicked);
        binding.btnCorrosionConnect.setOnClickListener(this::onViewClicked);
        binding.btnCorrosionCalibration.setOnClickListener(this::onViewClicked);
        binding.btnReturn.setOnClickListener(this::onViewClicked);
        binding.btnUpload.setOnClickListener(this::onViewClicked);
        binding.btnJiliang.setOnClickListener(this::onViewClicked);
        config();
    }

    private void config() {
        float calWidth1 = SPUtils.getInstance().getFloat("cal-width-1");
        float calWidth2 = SPUtils.getInstance().getFloat("cal-width-2");
        float calWidth3 = SPUtils.getInstance().getFloat("cal-width-3");
        float calWidth4 = SPUtils.getInstance().getFloat("cal-width-4");
        if (calWidth1 == -1) {
            SPUtils.getInstance().put("cal-width-1", 34.0f);
        }
        if (calWidth2 == -1) {
            SPUtils.getInstance().put("cal-width-2", 59.0f);
        }
        if (calWidth3 == -1) {
            SPUtils.getInstance().put("cal-width-3", 114.0f);
        }
        if (calWidth4 == -1) {
            SPUtils.getInstance().put("cal-width-4", 277.0f);
        }
    }

    public void onViewClicked(View view) {
        Intent intent;
        if (view.getId() == R.id.btn_f230_connect) {
            intent = new Intent(this, WifiListActivity.class);
            startActivity(intent);
        } else if (view.getId() == R.id.btn_f230_calibration) {
            if (TextUtils.isEmpty(SPUtils.getInstance().getString(StaticConstant.SP_F230_WIFI_NAME))) {
                ToastUtils.showLong("请先连接缝宽探头");
                return;
            }
            DialogLogin dialogLogin = new DialogLogin(this, 1);
            dialogLogin.setDialogListener((flag, type) -> {
                if (flag) {
                    Intent it = new Intent(this, CalibrationWidthActivity.class);
                    startActivity(it);
                }
            }).show();
        } else if (view.getId() == R.id.btn_rebound_connect) {
            intent = new Intent(this, ScanningBluetoothActivity.class);
            intent.putExtra(ScanningBluetoothActivity.INTENT_TYPE, ScanningBluetoothActivity.SCAN_REBOUND);
            startActivity(intent);
        } else if (view.getId() == R.id.btn_rebound_printer) {
            intent = new Intent(this, BluetoothPrinterActivity.class);
            startActivity(intent);
        } else if (view.getId() == R.id.btn_rebound_calibration) {
            if (TextUtils.isEmpty(SPUtils.getInstance().getString(StaticConstant.SP_REBOUND_BLE_MAC))) {
                ToastUtils.showShort("请先进行蓝牙设备连接");
                return;
            }
            DialogLogin dialogLogin = new DialogLogin(this, 1);
            dialogLogin.setDialogListener((flag, type) -> {
                if (flag) {
                    startActivity(new Intent(this, InstrumentCalibrationActivity.class));
                }
            }).show();
        } else if (view.getId() == R.id.btn_corrosion_connect) {
            intent = new Intent(this, ScanningBluetoothActivity.class);
            intent.putExtra(ScanningBluetoothActivity.INTENT_TYPE, ScanningBluetoothActivity.SCAN_CORROSION);
            startActivity(intent);
        } else if (view.getId() == R.id.btn_corrosion_calibration) {
            DialogLogin dialogLogin = new DialogLogin(this, 2);
            dialogLogin.setDialogListener((flag, type) -> {
                if (flag) {
                    Intent it = new Intent(this, CalibrationDeviceActivity.class);
                    startActivity(it);
                }
            }).show();
        } else if (view.getId() == binding.btnJiliang.getId()) {
            startActivity(new Intent(this, MeteringDeviceActivity.class));
        } else if (view.getId() == R.id.btn_return) {
            finish();
        } else if (view.getId() == binding.btnUpload.getId()) {
            StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this);
            styleAlertDialog.setContent("当前版本：" + strVersion).setOKButton("确定", v -> styleAlertDialog.dismiss()).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.tvF230Device.setText(SPUtils.getInstance().getString(StaticConstant.SP_F230_WIFI_NAME));
        binding.tvReboundDevice.setText(SPUtils.getInstance().getString(StaticConstant.SP_REBOUND_BLE_NAME));
        binding.tvCorrosionDevice.setText(SPUtils.getInstance().getString(StaticConstant.SP_CORROSION_BLE_NAME));
    }
}

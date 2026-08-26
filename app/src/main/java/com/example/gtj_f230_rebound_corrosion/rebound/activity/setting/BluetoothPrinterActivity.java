package com.example.gtj_f230_rebound_corrosion.rebound.activity.setting;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.base.activity.BaseActivity;
import com.example.gtj_f230_rebound_corrosion.base.utils.StringUtils;
import com.example.gtj_f230_rebound_corrosion.databinding.ActBluetoothPrinterBinding;

@SuppressLint("SetTextI18n")
public class BluetoothPrinterActivity extends BaseActivity<ActBluetoothPrinterBinding> {

    @Override
    protected ActBluetoothPrinterBinding getBinding() {
        return ActBluetoothPrinterBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initLabel() {
        initBatteryReceiver(binding.navigationBar);
        initDeviceStatusExecutor(binding.navigationBar);
        
        binding.navigationBar.setTitleText("蓝牙打印机连接");
    }

    @Override
    protected void initView() {
        binding.btnSave.setOnClickListener(this::onViewClicked);
        binding.btnReturn.setOnClickListener(this::onViewClicked);
        binding.ivDelete.setOnClickListener(this::onViewClicked);
        initData();
    }

    private void initData() {
        //00:1B:35:11:5D:31 黑
        //00:1B:35:06:BD:60 白
        String strMac = SPUtils.getInstance().getString("BluetoothPrinterMac");
        if (!TextUtils.isEmpty(strMac)) {
            binding.tvMac.setVisibility(View.VISIBLE);
            binding.ivDelete.setVisibility(View.VISIBLE);
        } else {
            binding.tvMac.setVisibility(View.GONE);
            binding.ivDelete.setVisibility(View.GONE);
        }
        binding.tvMac.setText(strMac);
        binding.etMac.setText(strMac.replaceAll(":", ""));
        binding.etMac.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                StringUtils.stringFormatFilter(s, binding.tvMac, binding.ivDelete);
            }
        });
    }

    public void onViewClicked(View view) {
        if (view.getId() == R.id.iv_delete) {
            binding.etMac.setText("");
        } else if (view.getId() == R.id.btn_save) {
            String string = binding.etMac.getText().toString().trim();
            if (string.length() != 12) {
                ToastUtils.showShort("请填写完整MAC地址");
                return;
            }
            SPUtils.getInstance().put("BluetoothPrinterMac", binding.tvMac.getText().toString());
            ToastUtils.showShort("打印机设置成功");
            finish();
        } else if (view.getId()==R.id.btn_return) {
            finish();
        }
    }
}

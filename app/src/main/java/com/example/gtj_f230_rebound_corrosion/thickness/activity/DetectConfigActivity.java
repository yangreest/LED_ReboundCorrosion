package com.example.gtj_f230_rebound_corrosion.thickness.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.base.activity.BaseActivity;
import com.example.gtj_f230_rebound_corrosion.base.utils.OtherUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.SpinnerUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.StringUtils;
import com.example.gtj_f230_rebound_corrosion.databinding.ActDetectConfigBinding;
import com.example.gtj_f230_rebound_corrosion.thickness.model.ConfigBean;
import com.example.gtj_f230_rebound_corrosion.thickness.model.Rebar240ZoneBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

@SuppressLint("SetTextI18n")
public class DetectConfigActivity extends BaseActivity<ActDetectConfigBinding> {
    private Rebar240ZoneBean zoneBean;
    private final int REQUEST_CODE_CONFIG = 3;
    private ConfigBean configBean;

    @Override
    protected ActDetectConfigBinding getBinding() {
        return ActDetectConfigBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initLabel() {
        binding.navigationBar.setTitleText("参数设置");
        initBatteryReceiver(binding.navigationBar);
        initDeviceStatusExecutor(binding.navigationBar);
    }

    @Override
    protected void initView() {
        configBean = (ConfigBean) getIntent().getSerializableExtra("ConfigBean");
        setHideKeyboard();
        binding.tvConfig.setOnClickListener(this::onViewClicked);
        binding.btnCancel.setOnClickListener(this::onViewClicked);
        binding.btnOk.setOnClickListener(this::onViewClicked);
        binding.tvManager.setOnClickListener(this::onViewClicked);
        attachDataSource();
        addTextChangedListener();
        initData();
    }

    private void initData() {
        zoneBean = (Rebar240ZoneBean) getIntent().getSerializableExtra("Rebar240ZoneBean");
        if (zoneBean == null) {
            String strZoneBean = SPUtils.getInstance().getString("last-config-240ZoneBean");
            if (!TextUtils.isEmpty(strZoneBean)) {
                zoneBean = new Gson().fromJson(strZoneBean, new TypeToken<Rebar240ZoneBean>() {
                }.getType());
                zoneBean.id = null;
                zoneBean.rebarPageList = new ArrayList<>();
            }
        }
        setZoneBeanView();
    }

    private void setZoneBeanView() {
        if (zoneBean != null) {
            //工程信息
            binding.tvPoleType.setText(StaticConstant.configPoleTypeList.get(zoneBean.poleType));  //电杆类型
            binding.spPoleStructure.setSelection(zoneBean.poleStructure);  //电杆结构
            binding.spPoleStandard.setSelection(zoneBean.poleStandard);  //规范
            //试验参数
            binding.etPoleHeight.setText(zoneBean.poleHeight);  //电杆长度
            binding.etPoleBottomDiameter.setText(zoneBean.poleBottomDiameter + "");  //电杆根径
            binding.etPoleTopDiameter.setText(zoneBean.poleTopDiameter + "");  //电杆梢径
            binding.spRebarDiameter.setSelection(zoneBean.rebarDiameter);  //纵筋直径
            binding.spHoopingDiameter.setSelection(getDiameterIndex(zoneBean.hoopingDiameter));  //螺旋筋直径
            binding.tvDesignThickness.setText(zoneBean.designThickness);
        } else {
            zoneBean = new Rebar240ZoneBean();
            zoneBean.rebarPageList = new ArrayList<>();
        }
        setImageView(binding.spPoleStructure.getSelectedItemPosition());
    }

    private int getDiameterIndex(int diameter) {
        for (int i = 0; i < StaticConstant.hoopingDiameterList.size(); i++) {
            if (diameter == Integer.parseInt(StaticConstant.hoopingDiameterList.get(i))) {
                return i;
            }
        }
        return 0;
    }

    private void attachDataSource() {
        SpinnerUtils.attachDataSource(binding.spPoleStructure, StaticConstant.configpoleStructureList, (spinner, text, position) -> setImageView(position));  //电杆结构
        SpinnerUtils.attachDataSource(binding.spPoleStandard, StaticConstant.poleStandardList, null);  //规范
        SpinnerUtils.attachDataSource(binding.spRebarDiameter, StaticConstant.rebarDiameterList, null);  //钢筋直径
        SpinnerUtils.attachDataSource(binding.spHoopingDiameter, StaticConstant.hoopingDiameterList, null);  //螺旋筋直径
    }

    private void addTextChangedListener() {
        binding.etPoleBottomDiameter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String strPoleType = binding.spPoleStructure.getSelectedItem() + "";
                if (TextUtils.equals(strPoleType, "等径杆")) {
                    binding.etPoleTopDiameter.setText(s.toString());
                }
            }
        });
    }

    private void setImageView(int index) {
        switch (index) {
            case 0:
                binding.imageView.setImageResource(R.mipmap.icon_pole_zx);
                binding.etPoleTopDiameter.setBackgroundResource(R.drawable.bg_edittext);
                binding.etPoleTopDiameter.setEnabled(true);
                break;
            case 1:
                binding.imageView.setImageResource(R.mipmap.icon_pole_flzx);
                binding.etPoleTopDiameter.setBackgroundResource(R.drawable.bg_edittext);
                binding.etPoleTopDiameter.setEnabled(true);
                break;
            case 2:
                binding.imageView.setImageResource(R.mipmap.icon_pole_dj);
                binding.etPoleTopDiameter.setBackgroundResource(R.drawable.bg_edittext_gray);
                binding.etPoleTopDiameter.setEnabled(false);
                binding.etPoleTopDiameter.setText(binding.etPoleBottomDiameter.getText().toString().trim());
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_CONFIG && data != null) {
            configBean = (ConfigBean) data.getSerializableExtra("configBean");
            if (configBean == null) {
                return;
            }
            binding.tvConfig.setText(configBean.poleName);
            binding.tvPoleType.setText(configBean.poleType);
            binding.spPoleStructure.setSelection(configBean.poleStructure);  //电杆结构
            binding.etPoleHeight.setText(configBean.poleHeight);
            binding.tvDesignThickness.setText(configBean.designThickness);
            binding.etPoleBottomDiameter.setText(configBean.poleBottomDiameter + "");
            binding.etPoleTopDiameter.setText(configBean.poleTopDiameter + "");
            binding.spRebarDiameter.setSelection(StaticConstant.rebarDiameterList.indexOf(configBean.rebarDiameter + ""));
            binding.spHoopingDiameter.setSelection(StaticConstant.hoopingDiameterList.indexOf(configBean.hoopingDiameter + ""));
            setImageView(binding.spPoleStructure.getSelectedItemPosition());
        }
    }

    public void onViewClicked(View view) {
        OtherUtils.hideKeyboard(this);
        if (view.getId() == R.id.tv_config) {
            Intent intent = new Intent(this, ConfigListActivity.class);
            intent.putExtra("configBean", configBean);
            startActivityForResult(intent, REQUEST_CODE_CONFIG);
        } else if (view.getId() == R.id.btn_cancel) {
            finish();
        } else if (view.getId() == R.id.btn_ok) {
            if (saveData()) {
                progressiveDialog.dismiss();
                setResult(RESULT_OK, new Intent().putExtra("Rebar240ZoneBean", zoneBean));
                finish();
            }
        } else if (view.getId() == R.id.tv_manager) {
            Intent intent = new Intent(this, ConfigManagerActivity.class);
            startActivityForResult(intent, 3);
        }
    }

    private boolean saveData() {
        String strPoleType = binding.tvPoleType.getText().toString();
        String strPoleStructure = binding.spPoleStructure.getSelectedItem() + "";
        String strPoleStandard = binding.spPoleStandard.getSelectedItem() + "";
        //
        String strPoleHeight = binding.etPoleHeight.getText().toString().trim();
        String strDesignThickness = binding.tvDesignThickness.getText().toString().trim();
        String strPoleBottomDiameter = binding.etPoleBottomDiameter.getText().toString().trim();
        String strPoleTopDiameter = binding.etPoleTopDiameter.getText().toString().trim();
        String strRebarDiameter = binding.spRebarDiameter.getSelectedItem() + "";
        String strHoopingDiameter = binding.spHoopingDiameter.getSelectedItem() + "";
        if (TextUtils.isEmpty(strPoleType) || TextUtils.isEmpty(strPoleStructure) || TextUtils.isEmpty(strPoleStandard) || TextUtils.isEmpty(strPoleHeight) || TextUtils.isEmpty(strDesignThickness) || TextUtils.isEmpty(strPoleBottomDiameter) || TextUtils.isEmpty(strPoleTopDiameter) || TextUtils.isEmpty(strRebarDiameter) || TextUtils.isEmpty(strHoopingDiameter)) {
            ToastUtils.showShort("请填写完整");
            return false;
        }
        progressiveDialog.show();
        zoneBean.poleType = StringUtils.getPoleTypeIndex(strPoleType);
        zoneBean.poleStructure = binding.spPoleStructure.getSelectedItemPosition();
        zoneBean.poleStandard = binding.spPoleStandard.getSelectedItemPosition();
        //
        zoneBean.poleHeight = strPoleHeight;
        zoneBean.designThickness = strDesignThickness;
        zoneBean.poleHeightReinforce = Double.parseDouble(strPoleHeight) - 1.5 + "";
        zoneBean.poleBottomDiameter = Integer.parseInt(strPoleBottomDiameter);
        zoneBean.poleTopDiameter = Integer.parseInt(strPoleTopDiameter);
        zoneBean.rebarDiameter = binding.spRebarDiameter.getSelectedItemPosition();
        zoneBean.hoopingDiameter = Integer.parseInt(strHoopingDiameter);
        zoneBean.poleBottomCount = 10;
        zoneBean.poleTopCount = 10;
        if (configBean != null) {
            zoneBean.designThickness = configBean.designThickness;
        }
        //
        SPUtils.getInstance().put("last-config-240ZoneBean", new Gson().toJson(zoneBean));
        return true;
    }
}

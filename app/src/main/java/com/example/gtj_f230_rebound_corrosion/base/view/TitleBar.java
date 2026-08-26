package com.example.gtj_f230_rebound_corrosion.base.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.SizeUtils;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.databinding.ViewTitleBarBinding;

public class TitleBar extends RelativeLayout {
    ViewTitleBarBinding binding;

    public TitleBar(Context context) {
        super(context);
        initView(context);
    }

    public TitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public TitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        binding = ViewTitleBarBinding.inflate(LayoutInflater.from(context), this,    // 将当前 View 作为父容器
                true     // 自动附加到父容器
        );
        binding.layoutLogo.setVisibility(StaticConstant.isNeutral ? GONE : VISIBLE);
        switch (StaticConstant.APP_LOGO_DEFAULT) {
            case StaticConstant.APP_LIERDA:
                binding.ivLogoLrd.setVisibility(VISIBLE);
                binding.ivLogoZhi.setVisibility(GONE);
                binding.vCenter.setVisibility(GONE);
                binding.ivLogoGjdw.setVisibility(GONE);
                binding.ivLogoMshz.setVisibility(GONE);
                binding.ivLogoGtj.setVisibility(GONE);
                break;
            case StaticConstant.APP_DIANKEYUAN:
                binding.ivLogoLrd.setVisibility(GONE);
                binding.ivLogoZhi.setVisibility(VISIBLE);
                binding.vCenter.setVisibility(VISIBLE);
                binding.ivLogoGjdw.setVisibility(VISIBLE);
                binding.ivLogoMshz.setVisibility(GONE);
                binding.ivLogoGtj.setVisibility(GONE);
                break;
            case StaticConstant.APP_MINGSHENGHENGZHUO:
                binding.ivLogoLrd.setVisibility(VISIBLE);
                binding.ivLogoZhi.setVisibility(GONE);
                binding.vCenter.setVisibility(VISIBLE);
                binding.ivLogoGjdw.setVisibility(GONE);
                binding.ivLogoMshz.setVisibility(VISIBLE);
                binding.ivLogoGtj.setVisibility(GONE);
                break;
            case StaticConstant.APP_GTJ:
                binding.ivLogoLrd.setVisibility(GONE);
                binding.ivLogoZhi.setVisibility(GONE);
                binding.vCenter.setVisibility(GONE);
                binding.ivLogoGjdw.setVisibility(GONE);
                binding.ivLogoMshz.setVisibility(GONE);
                binding.ivLogoGtj.setVisibility(VISIBLE);
                break;

        }
    }

    public TitleBar setTitleText(String string) {
        binding.tvNavTitle.setText(string);
        return this;
    }

    public TextView getTitleText() {
        return binding.tvNavTitle;
    }

    public TitleBar setProjectName(String string) {
        binding.tvNProject.setText(string);
        binding.layoutNProject.setVisibility(VISIBLE);
        return this;
    }

    public TitleBar setZoneName(String string) {
        binding.tvNZone.setText(string);
        binding.layoutNZone.setVisibility(VISIBLE);
        return this;
    }

    public TitleBar setIvCorBluetooth(boolean isShow) {
        binding.ivCorBluetooth.setVisibility(isShow ? VISIBLE : GONE);
        binding.tvCorBle.setVisibility(isShow ? VISIBLE : GONE);
        return this;
    }

    public TitleBar setIvReboundBluetooth(boolean isShow) {
        binding.ivReboundBluetooth.setVisibility(isShow ? VISIBLE : GONE);
        binding.tvReboundBle.setVisibility(isShow ? VISIBLE : GONE);
        return this;
    }

    public TitleBar setIvF230WIFI(boolean isShow) {
        binding.ivF230.setVisibility(isShow ? VISIBLE : GONE);
        return this;
    }

    public void setivBattery(boolean flag) {
        binding.ivBattery.setVisibility(VISIBLE);
        binding.tvBattery.setVisibility(VISIBLE);
        binding.ivBattery.setImageResource(flag ? R.mipmap.icon_battery1 : R.mipmap.icon_battery);
        binding.tvBattery.setVisibility(flag ? GONE : VISIBLE);
    }

    @SuppressLint("SetTextI18n")
    public void setBatteryLevel(int rawlevel, int scale) {
        binding.ivBatteryLevel.setVisibility(VISIBLE);
        int labelLevel = 1;
        float level = 0.1f;
        if (rawlevel >= 0 && scale > 0) {
            labelLevel = (rawlevel * 100) / scale;
            level = rawlevel * 1.0f / scale;
        }
        binding.tvBattery.setText(labelLevel + "");
        //48dp
        if (level >= 0.95) {
            binding.ivBatteryLevel.getLayoutParams().width = (int) (SizeUtils.dp2px(50) * level);
        } else {
            binding.ivBatteryLevel.getLayoutParams().width = (int) (SizeUtils.dp2px(48) * level);
        }

        binding.ivBatteryLevel.setLayoutParams(binding.ivBatteryLevel.getLayoutParams());
    }
}


package com.example.gtj_f230_rebound_corrosion.base.activity;

import com.example.gtj_f230_rebound_corrosion.databinding.ActCrashMessageBinding;

public class CrashMessageActivity extends BaseActivity<ActCrashMessageBinding> {

    @Override
    protected ActCrashMessageBinding getBinding() {
        return ActCrashMessageBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initLabel() {

    }

    @Override
    protected void initView() {
        String string = getIntent().getStringExtra("msg");
        binding.tvMsg.setText(string);
    }
}

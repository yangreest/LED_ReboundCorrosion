package com.example.gtj_f230_rebound_corrosion.base.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;

import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.base.utils.PermissionUtils;

public class StartActivity extends ComponentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_start);
    }

    @Override
    protected void onStart() {
        super.onStart();
        PermissionUtils.initPermission(this, this::initData);
    }

    private void initData() {
        new Handler(getMainLooper()).postDelayed(this::goMain, 1000);
    }

    private void goMain() {
        Intent it = new Intent(this, MainActivity.class);
        startActivity(it);
        finish();
    }
}

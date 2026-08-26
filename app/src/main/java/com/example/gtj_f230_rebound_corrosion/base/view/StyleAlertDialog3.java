package com.example.gtj_f230_rebound_corrosion.base.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.gtj_f230_rebound_corrosion.R;

public class StyleAlertDialog3 extends Dialog {
    private CharSequence leftText = "";
    private CharSequence rightText = "";
    private CharSequence okText = "";
    private String str0 = "", str1 = "", str2 = "", str3 = "", str4 = "";
    private View.OnClickListener leftListener;
    private View.OnClickListener rightListener;
    private View.OnClickListener okListener;
    private TextView tvD0, tvD1, tvD2, tvD3, tvD4;

    public StyleAlertDialog3(Context context) {
        super(context, R.style.StyleAlertDialog);
        setCancelable(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_dialog3);
        initView();
    }

    private void initView() {
        tvD0 = findViewById(R.id.tv_d0);
        tvD1 = findViewById(R.id.tv_d1);
        tvD2 = findViewById(R.id.tv_d2);
        tvD3 = findViewById(R.id.tv_d3);
        tvD4 = findViewById(R.id.tv_d4);
        Button left = findViewById(R.id.btn_dialog_left);
        Button right = findViewById(R.id.btn_dialog_right);
        Button ok = findViewById(R.id.btn_dialog_ok);
        tvD0.setText(str0);
        tvD1.setText(str1);
        tvD2.setText(str2);
        tvD3.setText(str3);
        tvD4.setText(str4);
        if (!TextUtils.isEmpty(okText)) {
            ok.setVisibility(View.VISIBLE);
            ok.setText(okText);
            ok.setOnClickListener(okListener);
            return;
        }
        if (!TextUtils.isEmpty(leftText)) {
            left.setVisibility(View.VISIBLE);
            left.setText(leftText);
            left.setOnClickListener(leftListener);
        }
        if (!TextUtils.isEmpty(rightText)) {
            right.setVisibility(View.VISIBLE);
            right.setText(rightText);
            right.setOnClickListener(rightListener);
        }
    }

    /**
     * 设置显示内容
     */
    public StyleAlertDialog3 setContent(String str0, String str1, String str2, String str3, String str4) {
        if (tvD1 != null) {
            tvD0.setText(str0);
            tvD1.setText(str1);
            tvD2.setText(str2);
            tvD3.setText(str3);
            tvD4.setText(str4);
        } else {
            this.str0 = str0;
            this.str1 = str1;
            this.str2 = str2;
            this.str3 = str3;
            this.str4 = str4;
        }
        return this;
    }

    /**
     * 设置按钮
     */
    public StyleAlertDialog3 setLeftButton(CharSequence text, View.OnClickListener listener) {
        leftText = text;
        leftListener = listener;
        return this;
    }

    /**
     * 设置按钮
     */
    public StyleAlertDialog3 setRightButton(CharSequence text, View.OnClickListener listener) {
        rightText = text;
        rightListener = listener;
        return this;
    }

    /**
     * 设置按钮
     */
    public StyleAlertDialog3 setOKButton(CharSequence text, View.OnClickListener listener) {
        okText = text;
        okListener = listener;
        return this;
    }

}

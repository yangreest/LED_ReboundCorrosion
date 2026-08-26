package com.example.gtj_f230_rebound_corrosion.base.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.gtj_f230_rebound_corrosion.R;

public class StyleAlertDialog2 extends Dialog {
    private CharSequence leftText = "";
    private CharSequence rightText = "";
    private CharSequence okText = "";
    private CharSequence contentText;
    private View.OnClickListener leftListener;
    private View.OnClickListener rightListener;
    private View.OnClickListener okListener;
    private TextView tvContent;

    public StyleAlertDialog2(Context context) {
        super(context, R.style.StyleAlertDialog);
        setCancelable(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_dialog2);
        initView();
    }

    private void initView() {
        tvContent = findViewById(R.id.dialog_content);
        Button left = findViewById(R.id.btn_dialog_left);
        Button right = findViewById(R.id.btn_dialog_right);
        Button ok = findViewById(R.id.btn_dialog_ok);
//        content.setGravity(Gravity.CENTER);
        if (!TextUtils.isEmpty(contentText)) {
            tvContent.setText(contentText);
        }
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
    public StyleAlertDialog2 setContent(CharSequence content) {
        if (tvContent != null) {
            tvContent.setText(content);
        } else {
            contentText = content;
        }
        return this;
    }

    /**
     * 设置按钮
     */
    public StyleAlertDialog2 setLeftButton(CharSequence text, View.OnClickListener listener) {
        leftText = text;
        leftListener = listener;
        return this;
    }

    /**
     * 设置按钮
     */
    public StyleAlertDialog2 setRightButton(CharSequence text, View.OnClickListener listener) {
        rightText = text;
        rightListener = listener;
        return this;
    }

    /**
     * 设置按钮
     */
    public StyleAlertDialog2 setOKButton(CharSequence text, View.OnClickListener listener) {
        okText = text;
        okListener = listener;
        return this;
    }

}

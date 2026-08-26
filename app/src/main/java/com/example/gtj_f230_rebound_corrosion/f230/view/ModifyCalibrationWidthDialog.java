package com.example.gtj_f230_rebound_corrosion.f230.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;

public class ModifyCalibrationWidthDialog extends Dialog {
    private CharSequence leftText = "";
    private CharSequence rightText = "";
    private CharSequence contentText;
    private View.OnClickListener leftListener;
    private View.OnClickListener rightListener;
    private TextView tvContent;
    public EditText edittext;
    public String calibrationWidth = "";

    public ModifyCalibrationWidthDialog(Context context) {
        super(context, R.style.StyleAlertDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_modify_calibration_width);
        initView();
    }

    private void initView() {
        tvContent = findViewById(R.id.dialog_title);
        Button left = findViewById(R.id.btn_dialog_left);
        Button right = findViewById(R.id.btn_dialog_right);
        edittext = findViewById(R.id.edittext);
        if (!TextUtils.isEmpty(contentText)) {
            tvContent.setText(contentText);
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
        edittext.setText(calibrationWidth);
        edittext.setHint("请填写像素值");
    }

    /**
     * 设置显示内容
     */
    public ModifyCalibrationWidthDialog setContent(CharSequence content) {
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
    public ModifyCalibrationWidthDialog setLeftButton(CharSequence text, View.OnClickListener listener) {
        leftText = text;
        leftListener = listener;
        return this;
    }

    /**
     * 设置按钮
     */
    public ModifyCalibrationWidthDialog setRightButton(CharSequence text, View.OnClickListener listener) {
        rightText = text;
        rightListener = listener;
        return this;
    }
}

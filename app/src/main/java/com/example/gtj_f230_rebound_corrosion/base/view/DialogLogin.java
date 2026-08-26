package com.example.gtj_f230_rebound_corrosion.base.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.base.utils.OtherUtils;

/**
 * 登录弹出框
 */
@SuppressLint("SetTextI18n")
public class DialogLogin extends Dialog {
    private AppCompatActivity context;
    private int pswNumber;
    private EditText et_password;
    private onDialogListener onDialogListener;

    public interface onDialogListener {
        void onClick(boolean flag, int type);
    }

    public DialogLogin(AppCompatActivity context, int pswNumber) {
        super(context, R.style.MyDialog);
        this.context = context;
        this.pswNumber = pswNumber;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_login);
        initView();
    }

    private void initView() {
        et_password = findViewById(R.id.et_password);
        et_password.requestFocus();
        Button btnLeft = findViewById(R.id.btn_dialog_left);
        btnLeft.setOnClickListener(v -> dismiss());
        Button btnRight = findViewById(R.id.btn_dialog_right);
        btnRight.setOnClickListener(view -> {
            String strPassword = et_password.getText().toString().trim();
            et_password.setText("");
            if (pswNumber == 1 && TextUtils.equals(StaticConstant.pwd, strPassword)) {
                OtherUtils.hideKeyboard(context);
                dismiss();
                onDialogListener.onClick(true, 1);
            } else if (pswNumber == 2) {
                if (TextUtils.equals(StaticConstant.pwd, strPassword)) {
                    OtherUtils.hideKeyboard(context);
                    dismiss();
                    onDialogListener.onClick(true, 1);
                } else if (TextUtils.equals(StaticConstant.pwd1, strPassword)) {
                    OtherUtils.hideKeyboard(context);
                    dismiss();
                    onDialogListener.onClick(true, 2);
                }
            } else {
                ToastUtils.showShort("密码错误");
            }
        });
    }

    public DialogLogin setDialogListener(onDialogListener onDialogListener) {
        this.onDialogListener = onDialogListener;
        return this;
    }
}

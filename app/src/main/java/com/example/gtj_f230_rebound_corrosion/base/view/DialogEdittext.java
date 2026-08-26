package com.example.gtj_f230_rebound_corrosion.base.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.base.utils.OtherUtils;

/**
 * 登录弹出框
 */
@SuppressLint("SetTextI18n")
public class DialogEdittext extends Dialog {
    private AppCompatActivity context;
    private EditText edittext;
    private onDialogListener onDialogListener;

    public interface onDialogListener {
        void onClick(String data);
    }

    public DialogEdittext(AppCompatActivity context) {
        super(context, R.style.MyDialog);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_edittext);
        initView();
    }

    private void initView() {
        edittext = findViewById(R.id.edittext);
        edittext.requestFocus();
        Button btnLeft = findViewById(R.id.btn_dialog_left);
        btnLeft.setOnClickListener(v -> dismiss());
        Button btnRight = findViewById(R.id.btn_dialog_right);
        btnRight.setOnClickListener(view -> {
            String string = edittext.getText().toString().trim();
            if (TextUtils.isEmpty(string)) {
                return;
            }
            edittext.setText("");
            OtherUtils.hideKeyboard(context);
            dismiss();
            onDialogListener.onClick(string);
        });
    }

    public DialogEdittext setDialogListener(onDialogListener onDialogListener) {
        this.onDialogListener = onDialogListener;
        return this;
    }
}

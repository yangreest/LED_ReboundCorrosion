package com.example.gtj_f230_rebound_corrosion.base.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.databinding.ProgressViewBinding;

/**
 * 加载框
 */
public class ProgressiveDialog extends Dialog {

    private final ProgressViewBinding binding;
    private String contentString;

    @SuppressLint("InflateParams")
    public ProgressiveDialog(Context context) {
        super(context, R.style.ProgressiveDialog);
        setCanceledOnTouchOutside(false);
        this.setCancelable(true);
        binding = ProgressViewBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
    }

    @Override
    public void show() {
        super.show();
        if (!TextUtils.isEmpty(contentString)) {
            binding.tvContent.setText(contentString);
        }
    }

    public ProgressiveDialog setTvContent(String string) {
        contentString = string;
        return this;
    }
}

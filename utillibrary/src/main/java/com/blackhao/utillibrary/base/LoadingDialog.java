package com.blackhao.utillibrary.base;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.TextView;


import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;

import com.blackhao.utillibrary.library.R;

/**
 * Author ： BlackHao
 * Time : 2018/12/4 16:30
 * Description : 加载框
 */
public class LoadingDialog extends DialogFragment {

    private String loadingTxt = "";
    private TextView tvMsg;
    private View loadingImg;
    private Animation rotateAnimation;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View view = inflater.inflate(R.layout.fragment_dialog_loading, container, false);
        tvMsg = (TextView) view.findViewById(R.id.login_status_message);
        if (!loadingTxt.equals("")) {
            tvMsg.setText(loadingTxt);
        }
        //设置动画
        loadingImg = view.findViewById(R.id.loading_img);
        rotateAnimation = new RotateAnimation(0, 360f, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(1000);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setRepeatMode(Animation.RESTART);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        loadingImg.startAnimation(rotateAnimation);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setCancelable(false);
    }

    @Override
    public void onDestroyView() {
        // 清除动画，避免内存泄漏
        if (loadingImg != null) {
            loadingImg.clearAnimation();
            loadingImg = null;
        }
        if (rotateAnimation != null) {
            rotateAnimation.cancel();
            rotateAnimation = null;
        }
        super.onDestroyView();
    }

    public void setLoadingTxt(String loadingTxt) {
        this.loadingTxt = loadingTxt;
        if (this.tvMsg != null) {
            this.tvMsg.setText(loadingTxt);
        }
    }

    public void setLoadingTxt(@StringRes int strId) {
        setLoadingTxt(getString(strId));
    }
}

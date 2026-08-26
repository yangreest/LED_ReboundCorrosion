package com.example.gtj_f230_rebound_corrosion.base.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;

public class EmptyDataViewUtils {
    public static View getEmptyView(Activity context, boolean isCenter) {
        @SuppressLint("InflateParams") View view = context.getLayoutInflater().inflate(R.layout.view_data_empty, null);
        TextView textView = view.findViewById(R.id.textView);
        textView.setText(StaticConstant.getLanguage() ? "暂无内容" : "No content");
        View vCenter = view.findViewById(R.id.v_center);
        vCenter.setVisibility(isCenter ? View.GONE : View.VISIBLE);
        //关键：必须手动设置 LayoutParams，否则可能宽高为0不显示
        view.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        return view;
    }

    public static View getEmptyRefreshView(Activity context, boolean isCenter, String string1, String string2, View.OnClickListener listener) {
        @SuppressLint("InflateParams") View view = context.getLayoutInflater().inflate(R.layout.view_data_refresh, null);
        View vCenter = view.findViewById(R.id.v_center);
        vCenter.setVisibility(isCenter ? View.GONE : View.VISIBLE);
        TextView textView1 = view.findViewById(R.id.textView1);
        TextView textView2 = view.findViewById(R.id.textView2);
        textView1.setText(string1);
        textView2.setText(string2);
        textView2.setOnClickListener(listener);
        //关键：必须手动设置 LayoutParams，否则可能宽高为0不显示
        view.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        return view;
    }
}
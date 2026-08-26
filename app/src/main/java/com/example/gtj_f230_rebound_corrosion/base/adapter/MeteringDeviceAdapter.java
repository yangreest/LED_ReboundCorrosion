package com.example.gtj_f230_rebound_corrosion.base.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.base.model.MeteringBean;

import java.util.ArrayList;

public class MeteringDeviceAdapter extends BaseQuickAdapter<MeteringBean, QuickViewHolder> {

    public MeteringDeviceAdapter() {
        super(new ArrayList<>());
    }

    @Override
    protected QuickViewHolder onCreateViewHolder(Context context, ViewGroup parent, int viewType) {
        return new QuickViewHolder(LayoutInflater.from(context).inflate(R.layout.item_metering_device, parent, false));
    }

    @Override
    protected void onBindViewHolder(QuickViewHolder holder, int position, MeteringBean item) {
        holder.setText(R.id.textView, "直　径：" + item.diameter + "\n保护层：" + item.thickness);
    }
}
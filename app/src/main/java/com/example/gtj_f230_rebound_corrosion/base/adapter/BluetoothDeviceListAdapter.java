package com.example.gtj_f230_rebound_corrosion.base.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.base.utils.EmptyDataViewUtils;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.BluetoothDeviceBean;

public class BluetoothDeviceListAdapter extends BaseQuickAdapter<BluetoothDeviceBean, QuickViewHolder> {

    public BluetoothDeviceListAdapter() {
        super();
    }

    @Override
    protected QuickViewHolder onCreateViewHolder(Context context, ViewGroup parent, int viewType) {
        return new QuickViewHolder(LayoutInflater.from(context).inflate(R.layout.item_bluetooth_device_list, parent, false));
    }

    public void setEmptyView(Activity activity) {
        this.setStateView(EmptyDataViewUtils.getEmptyView(activity, false));
    }

    @Override
    protected void onBindViewHolder(QuickViewHolder holder, int position, BluetoothDeviceBean item) {
        holder.setText(R.id.tv_title, item.name);
        holder.setText(R.id.tv_content0, item.mac);
        if (item.isSelected) {
            holder.setBackgroundResource(R.id.imageView, R.mipmap.icon_multiple_selected);
        } else {
            holder.setBackgroundResource(R.id.imageView, R.mipmap.icon_multiple_unselected);
        }
    }
}
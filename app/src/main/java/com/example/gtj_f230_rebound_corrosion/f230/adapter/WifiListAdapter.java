package com.example.gtj_f230_rebound_corrosion.f230.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.base.utils.EmptyDataViewUtils;
import com.example.gtj_f230_rebound_corrosion.f230.model.WifiBean;

import org.jetbrains.annotations.NotNull;

public class WifiListAdapter extends BaseQuickAdapter<WifiBean, QuickViewHolder> {

    private final String strConnect1, strConnect2;

    public WifiListAdapter() {
        this.setStateViewEnable(true);
        if (StaticConstant.getLanguage()) {
            strConnect1 = "找不到设备WIFI，查看WIFI列表，";
            strConnect2 = "请点击";
        } else {
            strConnect1 = "Can not find the device WIFI, to view the WIFI list, ";
            strConnect2 = "please click";
        }
    }

    @NotNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NotNull Context context, @NotNull ViewGroup parent, int viewType) {
        return new QuickViewHolder(LayoutInflater.from(context).inflate(R.layout.item_wifi_list, parent, false));
    }

    public void setEmptyView(Activity activity) {
        this.setStateView(EmptyDataViewUtils.getEmptyView(activity, false));
    }

    public void setEmptyRefreshView(Activity activity) {
        View notDataView = EmptyDataViewUtils.getEmptyRefreshView(activity, false, strConnect1, strConnect2, v -> {
            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            activity.startActivity(intent);
        });
        this.setStateView(notDataView);
    }

    @Override
    protected void onBindViewHolder(@NotNull QuickViewHolder holder, int position, WifiBean item) {
        if (item != null) {
            holder.setText(R.id.tv_title, item.name);
            if (item.isSelected) {
                holder.setBackgroundResource(R.id.imageView, R.mipmap.icon_multiple_selected);
            } else {
                holder.setBackgroundResource(R.id.imageView, R.mipmap.icon_multiple_unselected);
            }
        }
    }
}
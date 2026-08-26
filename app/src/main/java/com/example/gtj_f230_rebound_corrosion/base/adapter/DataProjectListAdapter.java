package com.example.gtj_f230_rebound_corrosion.base.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.base.model.ProjectBean;
import com.example.gtj_f230_rebound_corrosion.base.utils.EmptyDataViewUtils;

public class DataProjectListAdapter extends BaseQuickAdapter<ProjectBean, QuickViewHolder> {
    private int colorFFF, color65B4AA, color333;

    public DataProjectListAdapter(Context context) {
        super();
        colorFFF = ContextCompat.getColor(context, R.color.colorFFF);
        color65B4AA = ContextCompat.getColor(context, R.color.color65B4AA);
        color333 = ContextCompat.getColor(context, R.color.color333);
    }

    @Override
    protected QuickViewHolder onCreateViewHolder(Context context, ViewGroup parent, int viewType) {
        return new QuickViewHolder(LayoutInflater.from(context).inflate(R.layout.item_data, parent, false));
    }

    public void setEmptyView(Activity activity) {
        this.setStateView(EmptyDataViewUtils.getEmptyView(activity, false));
    }

    @Override
    protected void onBindViewHolder(QuickViewHolder holder, int position, ProjectBean item) {
        holder.setText(R.id.textView, item.getName());
        holder.setTextColor(R.id.textView, item.isSelected_open ? colorFFF : color333);
        holder.setBackgroundColor(R.id.textView, item.isSelected_open ? color65B4AA : colorFFF);
        holder.setImageResource(R.id.imageView, item.isSelected_treat ? R.mipmap.icon_multiple1 : R.mipmap.icon_multiple0);
    }
}
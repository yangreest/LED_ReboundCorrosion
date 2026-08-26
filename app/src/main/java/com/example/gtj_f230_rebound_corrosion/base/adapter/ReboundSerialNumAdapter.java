package com.example.gtj_f230_rebound_corrosion.base.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.base.utils.EmptyDataViewUtils;
import com.example.gtj_f230_rebound_corrosion.rebound.model.RebounderBean;

public class ReboundSerialNumAdapter extends BaseQuickAdapter<RebounderBean, QuickViewHolder> {
    private Context context;
    private int color333, color65B4AA;

    public ReboundSerialNumAdapter(Context context) {
        super();
        this.context = context;
        color333 = ContextCompat.getColor(context, R.color.color333);
        color65B4AA = ContextCompat.getColor(context, R.color.color65B4AA);
    }

    @Override
    protected QuickViewHolder onCreateViewHolder(Context context, ViewGroup parent, int viewType) {
        return new QuickViewHolder(LayoutInflater.from(context).inflate(R.layout.item_serial_num, parent, false));
    }

    public void setEmptyView(Activity activity) {
        this.setStateView(EmptyDataViewUtils.getEmptyView(activity, false));
    }

    @Override
    protected void onBindViewHolder(QuickViewHolder holder, int position, RebounderBean item) {
        int serialNum = position + 1;
        holder.setText(R.id.textView, serialNum + ".测区" + (serialNum < 10 ? "0" + serialNum : serialNum));
        holder.setTextColor(R.id.textView, item.isSelected ? color65B4AA : color333);
        if (item.isSelected) {
            holder.setBackgroundColor(R.id.textView, ContextCompat.getColor(context, R.color.colorFFF));
        } else {
            holder.setBackgroundResource(R.id.textView, R.drawable.bg_stroke_gray);
        }
    }
}
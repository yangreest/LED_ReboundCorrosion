package com.example.gtj_f230_rebound_corrosion.base.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.base.model.DetectSerialNumBean;
import com.example.gtj_f230_rebound_corrosion.base.utils.EmptyDataViewUtils;

import org.jetbrains.annotations.NotNull;

public class DetectSerialNumAdapter extends BaseQuickAdapter<DetectSerialNumBean, QuickViewHolder> {
    private final Context context;
    private final int color333, color65B4AA;

    public DetectSerialNumAdapter(Context context) {
        super();
        this.context = context;
        color333 = ContextCompat.getColor(context, R.color.color333);
        color65B4AA = ContextCompat.getColor(context, R.color.color65B4AA);
    }

    @Override
    protected QuickViewHolder onCreateViewHolder(@NotNull Context context, @NotNull ViewGroup parent, int viewType) {
        return new QuickViewHolder(LayoutInflater.from(context).inflate(R.layout.item_serial_num, parent, false));
    }

    public void setEmptyView(Activity activity) {
        this.setStateView(EmptyDataViewUtils.getEmptyView(activity, false));
    }

    @Override
    protected void onBindViewHolder(QuickViewHolder holder, int position, DetectSerialNumBean item) {
        holder.setText(R.id.textView, item.serialNum + ".测区" + (item.serialNum < 10 ? "0" + item.serialNum : item.serialNum));
        holder.setTextColor(R.id.textView, item.isSelected ? color65B4AA : color333);
        if (item.isSelected) {
            holder.setBackgroundColor(R.id.textView, ContextCompat.getColor(context, R.color.colorFFF));
        } else {
            holder.setBackgroundResource(R.id.textView, R.drawable.bg_stroke_gray);
        }
    }
}
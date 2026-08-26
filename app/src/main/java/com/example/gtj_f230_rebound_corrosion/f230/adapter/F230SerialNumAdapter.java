package com.example.gtj_f230_rebound_corrosion.f230.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.f230.model.WidthBean;

import org.jetbrains.annotations.NotNull;

public class F230SerialNumAdapter extends BaseQuickAdapter<WidthBean, QuickViewHolder> {
    private final Context context;
    private final int color333, color65B4AA;

    public F230SerialNumAdapter(Context context) {
        super();
        this.context = context;
        color333 = ContextCompat.getColor(context, R.color.color333);
        color65B4AA = ContextCompat.getColor(context, R.color.color65B4AA);
    }

    @NotNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NotNull Context context, @NotNull ViewGroup parent, int viewType) {
        return new QuickViewHolder(LayoutInflater.from(context).inflate(R.layout.item_serial_num, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NotNull QuickViewHolder holder, int position, WidthBean item) {
        if (item == null) {
            return;
        }
        int serialNum = position + 1;
        holder.setText(R.id.textView, serialNum + ".测点" + (item.id < 10 ? "0" + item.id : item.id));
        holder.setTextColor(R.id.textView, item.isSelected ? color65B4AA : color333);
        if (item.isSelected) {
            holder.setBackgroundColor(R.id.textView, ContextCompat.getColor(context, R.color.colorFFF));
        } else {
            holder.setBackgroundResource(R.id.textView, R.drawable.bg_stroke_gray);
        }
    }
}
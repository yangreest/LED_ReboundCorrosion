package com.example.gtj_f230_rebound_corrosion.thickness.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.thickness.model.ConfigBean;

import org.jetbrains.annotations.NotNull;

public class ConfigTypeListAdapter extends BaseQuickAdapter<ConfigBean, QuickViewHolder> {
    private final int colorFFF, color4CB4B9, color333;
    private final boolean isCreatePage;

    public ConfigTypeListAdapter(Context context, boolean isCreatePage) {
        super();
        colorFFF = ContextCompat.getColor(context, R.color.colorFFF);
        color4CB4B9 = ContextCompat.getColor(context, R.color.color006F5F);
        color333 = ContextCompat.getColor(context, R.color.color333);
        this.isCreatePage = isCreatePage;
    }

    @NotNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NotNull Context context, @NotNull ViewGroup parent, int viewType) {
        return new QuickViewHolder(LayoutInflater.from(context).inflate(R.layout.item_project_list1, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NotNull QuickViewHolder holder, int position, ConfigBean item) {
        if (item == null) {
            return;
        }
        holder.setText(R.id.textView, item.poleType);
        holder.setTextColor(R.id.textView, item.isSelected ? colorFFF : color333);
        holder.setBackgroundColor(R.id.textView, item.isSelected ? color4CB4B9 : colorFFF);
        if (isCreatePage) {
            holder.setImageResource(R.id.imageView, item.isDeleteSelected ? R.mipmap.icon_multiple1 : R.mipmap.icon_multiple0);
        } else {
            holder.setImageResource(R.id.imageView, item.isSelected ? R.mipmap.icon_multiple1 : R.mipmap.icon_multiple0);
        }
    }
}

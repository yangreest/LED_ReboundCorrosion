package com.example.gtj_f230_rebound_corrosion.corrosion.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.base.utils.StringUtils;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.MinMaxBean;

public class CalibrationMinMaxListAdapter extends BaseQuickAdapter<MinMaxBean, QuickViewHolder> {

    public CalibrationMinMaxListAdapter() {
        super();
    }

    @Override
    protected QuickViewHolder onCreateViewHolder(Context context, ViewGroup parent, int viewType) {
        return new QuickViewHolder(LayoutInflater.from(context).inflate(R.layout.item_calibration_min_max_list, parent, false));
    }

    @Override
    protected void onBindViewHolder(QuickViewHolder holder, int position, MinMaxBean item) {
        holder.setText(R.id.tv_thickness, item.thickness + ".　");
        holder.setText(R.id.tvMin, ((int) item.min70) + "\n" + ((int) item.max70) + "\n" + StringUtils.getRoundingString((item.max70 - item.min70) / item.max70, 2));
        holder.setText(R.id.tvMax, ((int) item.min120) + "\n" + ((int) item.max120) + "\n" + StringUtils.getRoundingString((item.max120 - item.min120) / item.max120, 2));
    }
}
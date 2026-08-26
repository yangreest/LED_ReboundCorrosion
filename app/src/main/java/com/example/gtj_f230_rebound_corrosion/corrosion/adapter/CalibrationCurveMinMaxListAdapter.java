package com.example.gtj_f230_rebound_corrosion.corrosion.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.CurveMinMaxBean;

public class CalibrationCurveMinMaxListAdapter extends BaseQuickAdapter<CurveMinMaxBean, QuickViewHolder> {

    public CalibrationCurveMinMaxListAdapter(Context context) {
        super();
    }

    @Override
    protected QuickViewHolder onCreateViewHolder(Context context, ViewGroup parent, int viewType) {
        return new QuickViewHolder(LayoutInflater.from(context).inflate(R.layout.item_calibration_curve_list, parent, false));
    }

    @Override
    protected void onBindViewHolder(QuickViewHolder holder, int position, CurveMinMaxBean item) {
        holder.setText(R.id.textView, position + 1 + ".钢筋：" + item.rebarDiameter + "，厚度：" + item.thickness + "，Min：" + item.signalMin + "，Max：" + item.signalMax);
    }
}
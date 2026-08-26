package com.example.gtj_f230_rebound_corrosion.corrosion.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.StandardCurveBean;

public class CalibrationCurveListAdapter extends BaseQuickAdapter<StandardCurveBean, QuickViewHolder> {
    private final int colorFFF, color65B4AA, color333;

    public CalibrationCurveListAdapter(Context context) {
        super();
        colorFFF = ContextCompat.getColor(context, R.color.colorFFF);
        color65B4AA = ContextCompat.getColor(context, R.color.color65B4AA);
        color333 = ContextCompat.getColor(context, R.color.color333);
    }

    @Override
    protected QuickViewHolder onCreateViewHolder(Context context, ViewGroup parent, int viewType) {
        return new QuickViewHolder(LayoutInflater.from(context).inflate(R.layout.item_calibration_curve_list, parent, false));
    }

    @Override
    protected void onBindViewHolder(QuickViewHolder holder, int position, StandardCurveBean item) {
        double min = Math.min(item.signalArray[0], item.signalArray[item.signalArray.length - 1]);
        holder.setText(R.id.textView, position + 1 + ".钢筋：" + item.rebarDiameter + "，电杆：" + item.poleDiameter + "，厚度：" + item.thickness + "\n主筋间距：" + item.rebarSpacing + "，箍筋间距：" + item.spiralSpacing + "，信号：" + ((int) min) + "　" + ((int) item.signalArray[item.signalArray.length / 2]));
        holder.setTextColor(R.id.textView, item.isSelected ? colorFFF : color333);
        holder.setBackgroundColor(R.id.textView, item.isSelected ? color65B4AA : colorFFF);
        holder.setImageResource(R.id.imageView, item.isSelected ? R.mipmap.icon_multiple1 : R.mipmap.icon_multiple0);
    }
}
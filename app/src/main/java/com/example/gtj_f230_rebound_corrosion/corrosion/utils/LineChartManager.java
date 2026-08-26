package com.example.gtj_f230_rebound_corrosion.corrosion.utils;

import android.content.Context;
import android.graphics.Color;

import androidx.core.content.ContextCompat;

import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.LineChartBean;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

public class LineChartManager {
    private final LineChart lineChart;
    private final int color000000;
    private final int color666;
    private final static int[] measureLineArray = {10, 50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 1500, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000, 10000, 11000, 12000};
    private final List<Integer> colorList = new ArrayList<>();
    private final float textSize = 16f, lineSize = 1f;


    public LineChartManager(Context context, LineChart lineChart) {
        color000000 = ContextCompat.getColor(context, R.color.color000000);
        color666 = ContextCompat.getColor(context, R.color.color666);
        //
        colorList.add(ContextCompat.getColor(context, R.color.colorFF0000));
        colorList.add(ContextCompat.getColor(context, R.color.color00FF00));
        colorList.add(ContextCompat.getColor(context, R.color.color0000FF));
        colorList.add(ContextCompat.getColor(context, R.color.colorFFFF00));
        colorList.add(ContextCompat.getColor(context, R.color.color00FFFF));
        colorList.add(ContextCompat.getColor(context, R.color.colorFF00FF));
        colorList.add(ContextCompat.getColor(context, R.color.colorFF9900));
        colorList.add(ContextCompat.getColor(context, R.color.colorA52A2A));
        colorList.add(ContextCompat.getColor(context, R.color.color8A2BE2));
        colorList.add(ContextCompat.getColor(context, R.color.color006F5F));
        colorList.add(color000000);
        this.lineChart = lineChart;
        initChart(lineChart);
    }

    /**
     * 初始化图表
     */
    private void initChart(LineChart lineChart) {
        lineChart.setNoDataText("");
        lineChart.setNoDataTextColor(Color.parseColor("#333333"));
        lineChart.setHardwareAccelerationEnabled(true);//开启硬件加速
        //设置chart是否可以触摸
        lineChart.setTouchEnabled(true);
        //设置是否可以拖拽
        lineChart.setDragEnabled(true);
        //设置是否可以缩放 x和y，默认true
        lineChart.setScaleEnabled(true);
        //是否缩放X轴
        lineChart.setScaleXEnabled(true);
        //设置是否可以通过双击屏幕放大图表。默认是true
        lineChart.setDoubleTapToZoomEnabled(false);

        //是否展示网格线
        lineChart.setDrawGridBackground(false);
        lineChart.setBackgroundColor(Color.WHITE);
        //是否显示边界
        lineChart.setDrawBorders(true);

        //说明
        Description description = new Description();
        description.setText("");
        lineChart.setDescription(description);
        lineChart.setBorderColor(color000000);

        //XY轴的设置
        XAxis xAxis = lineChart.getXAxis();
        //X轴设置显示位置在底部
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(1024 * 8);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(13);
        xAxis.enableGridDashedLine(10f, 10f, 0f);

        YAxis leftYAxis = lineChart.getAxisLeft();
        leftYAxis.setDrawGridLines(true);
        leftYAxis.setEnabled(true);//是否绘制左侧Y轴
        //设置Y轴网格线为虚线
        leftYAxis.enableGridDashedLine(10f, 10f, 0f);
        //保证Y轴从0开始，不然会上移一点
        leftYAxis.setAxisMaximum(1200);
        leftYAxis.setAxisMinimum(0);
        leftYAxis.setLabelCount(10, false);
        leftYAxis.setGridColor(color666);

        lineChart.getAxisRight().setEnabled(false);

        lineChart.invalidate();

        //折线图例 标签 设置
        Legend legend = lineChart.getLegend();
        //设置显示类型，LINE CIRCLE SQUARE EMPTY 等等 多种方式，查看LegendForm 即可
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextSize(textSize);
        //显示位置 左下方
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        //是否绘制在图表里面
        legend.setDrawInside(false);
        //是否显示
        legend.setEnabled(true);
    }

    public void showLineChart2(List<LineChartBean> chartBeanList) {
        List<ILineDataSet> lineDataSets = new ArrayList<>();
        float valueMax = 0;
        float xMax = 0;
        for (int i = 0; i < chartBeanList.size(); i++) {
            //曲线
            LineChartBean bean = chartBeanList.get(i);
            LineDataSet dataSet = new LineDataSet(chartBeanList.get(i).entryList, chartBeanList.get(i).name);
            dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            dataSet.setColor(colorList.get(i % 11));
            dataSet.setLineWidth(lineSize);
            dataSet.setDrawCircles(false);
            dataSet.setDrawValues(false);
            dataSet.setMode(LineDataSet.Mode.LINEAR);
            dataSet.setValueTextSize(textSize);
            lineDataSets.add(dataSet);
            if (valueMax < chartBeanList.get(i).maxSignal) {
                valueMax = (float) chartBeanList.get(i).maxSignal;
            }
            //记录当前曲线最大的横坐标，用于动态确定X轴范围
            if (!bean.entryList.isEmpty()) {
                xMax = Math.max(xMax, bean.entryList.get(bean.entryList.size() - 1).getX());
            }
        }
        YAxis axisLeft = lineChart.getAxisLeft();
        axisLeft.setAxisMaximum(valueMax + 100);
        axisLeft.setAxisMinimum(0);
        axisLeft.setLabelCount(10, false);
        //X轴范围随数据长度动态调整，保证横坐标与实际数据一一对应
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(Math.max(xMax, 1f)+100);

        LineData lineData = new LineData(lineDataSets);
        lineChart.setData(lineData);

        lineChart.getData().notifyDataChanged();
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    public void showLineChart3(List<List<LineChartBean>> chartBeanList1) {
        List<ILineDataSet> lineDataSets = new ArrayList<>();
        float valueMax = 0;
        for (int i = 0; i < chartBeanList1.size(); i++) {
            List<LineChartBean> lineChartBeanList = chartBeanList1.get(i);
            int color = colorList.get(i % 11);
            for (int j = 0; j < lineChartBeanList.size(); j++) {
                //曲线
                LineDataSet dataSet = new LineDataSet(lineChartBeanList.get(j).entryList, lineChartBeanList.get(j).name);
                dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                dataSet.setColor(color);
                dataSet.setLineWidth(lineSize);
                dataSet.setDrawCircles(false);
                dataSet.setDrawValues(false);
                dataSet.setMode(LineDataSet.Mode.LINEAR);
                dataSet.setValueTextSize(textSize);
                lineDataSets.add(dataSet);
                if (valueMax < lineChartBeanList.get(j).maxSignal) {
                    valueMax = (float) lineChartBeanList.get(j).maxSignal;
                }
            }
        }
        //
        YAxis axisLeft = lineChart.getAxisLeft();
        axisLeft.setAxisMaximum(valueMax + 100);
        axisLeft.setAxisMinimum(0);
        axisLeft.setLabelCount(10, false);
        LineData lineData = new LineData(lineDataSets);
        lineChart.setData(lineData);

        lineChart.getData().notifyDataChanged();
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    public void showDetectChart(List<Entry> list, int max) {
        if (list.get(list.size() - 1).getX() > 10) {
            int maxMeasureLine = getMaxValue(list.get(list.size() - 1).getX());

            XAxis xAxis = lineChart.getXAxis();
            //保证Y轴从0开始，不然会上移一点
            xAxis.setAxisMaximum(maxMeasureLine);
            xAxis.setAxisMinimum(0);
            xAxis.setLabelCount(10, false);
        }

        if (max > 10) {
            int maxLeftY = getMaxValue(max);

            YAxis axisLeft = lineChart.getAxisLeft();
            //保证Y轴从0开始，不然会上移一点
            axisLeft.setAxisMaximum(maxLeftY);
            axisLeft.setAxisMinimum(0);
            axisLeft.setLabelCount(10, false);
        }
        // 每一个LineDataSet代表一条线
        LineDataSet dataSet = new LineDataSet(list, "");
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSet.setColor(color000000);
        dataSet.setLineWidth(lineSize);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.LINEAR);
        dataSet.setValueTextSize(textSize);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        lineChart.getData().notifyDataChanged();
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    private static int getMaxValue(double maxPressure) {
        int tempIndex = -1;
        double tempSettlement = Math.abs(maxPressure);
        for (int i = 0; i < measureLineArray.length; i++) {
            if (tempSettlement <= measureLineArray[i]) {
                tempIndex = i;
                break;
            }
        }
        return measureLineArray[tempIndex == -1 ? measureLineArray.length - 1 : tempIndex];
    }

}

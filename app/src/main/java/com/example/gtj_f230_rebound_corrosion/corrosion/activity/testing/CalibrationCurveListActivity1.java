package com.example.gtj_f230_rebound_corrosion.corrosion.activity.testing;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_f230_rebound_corrosion.base.activity.BaseActivity;
import com.example.gtj_f230_rebound_corrosion.base.storage.GreenDaoHelper;
import com.example.gtj_f230_rebound_corrosion.base.view.StyleAlertDialog;
import com.example.gtj_f230_rebound_corrosion.corrosion.adapter.CalibrationCurveListAdapter;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.CheckDataBean;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.LineChartBean;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.StandardCurveBean;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.ComputeCurveUtils;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.IntArrayUtils;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.LineChartManager;
import com.example.gtj_f230_rebound_corrosion.databinding.ActCalibrationCurveList1Binding;
import com.example.gtj_f230_rebound_corrosion.functions.database.greenDao.db.StandardCurveBeanDao;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 标准曲线列表查看
 */
@SuppressLint("SdCardPath")
public class CalibrationCurveListActivity1 extends BaseActivity<ActCalibrationCurveList1Binding> {
    private CalibrationCurveListAdapter listAdapter;
    private LineChartManager lineChartManager;
    private StandardCurveBeanDao curveDao;

    @Override
    protected ActCalibrationCurveList1Binding getBinding() {
        return ActCalibrationCurveList1Binding.inflate(getLayoutInflater());
    }

    @Override
    protected void initLabel() {
        binding.navigationBar.setTitleText("标准曲线列表");
        initBatteryReceiver(binding.navigationBar);
        initDeviceStatusExecutor(binding.navigationBar);
    }

    @Override
    protected void initView() {
        lineChartManager = new LineChartManager(this, binding.lineChart);
        curveDao = GreenDaoHelper.getDaoSession(this).getStandardCurveBeanDao();
        setListener();
        setRecyclerView();
    }

    private void setListener() {
        binding.btnAll.setOnClickListener(onClickListener);
        binding.btn200.setOnClickListener(onClickListener);
        binding.btn250.setOnClickListener(onClickListener);
        binding.btn310.setOnClickListener(onClickListener);
        binding.btn350.setOnClickListener(onClickListener);
        binding.btn400.setOnClickListener(onClickListener);
        binding.btn450.setOnClickListener(onClickListener);
        binding.btn500.setOnClickListener(onClickListener);
        //
        binding.btn7.setOnClickListener(onClickListener);
        binding.btn9.setOnClickListener(onClickListener);
        binding.btn10.setOnClickListener(onClickListener);
        binding.btn11.setOnClickListener(onClickListener);
        binding.btn12.setOnClickListener(onClickListener);
        binding.btn14.setOnClickListener(onClickListener);
        binding.btn16.setOnClickListener(onClickListener);
        binding.btnFinish.setOnClickListener(v -> finish());
    }

    private void setRecyclerView() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        listAdapter = new CalibrationCurveListAdapter(this);
        binding.recyclerView.setAdapter(listAdapter);

        listAdapter.setOnItemClickListener((adapter, view, position) -> {
            StandardCurveBean bean = listAdapter.getItems().get(position);
            bean.isSelected = !bean.isSelected;
            listAdapter.notifyItemChanged(position);
            setLineChartData(bean);
        });
        listAdapter.setOnItemLongClickListener((adapter, view, position) -> {
            StandardCurveBean curveBean = listAdapter.getItems().get(position);
            StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this);
            styleAlertDialog.setContent("确定删除 直径：" + curveBean.rebarDiameter + "，厚度：" + curveBean.thickness + " ？").setLeftButton("取消", v -> styleAlertDialog.dismiss()).setRightButton("确定", v -> {
                styleAlertDialog.dismiss();
                curveDao.deleteByKey(curveBean.id);
                listAdapter.getItems().remove(position);
                listAdapter.notifyItemRemoved(position);
            }).show();
            return false;
        });
    }

    private void setLineChartData(StandardCurveBean bean) {
        List<StandardCurveBean> beanList = new ArrayList<>();
        beanList.add(getStandardCurveBean(bean, "200"));
        beanList.add(getStandardCurveBean(bean, "250"));
        beanList.add(getStandardCurveBean(bean, "310"));
        beanList.add(getStandardCurveBean(bean, "350"));
        beanList.add(getStandardCurveBean(bean, "400"));
        beanList.add(getStandardCurveBean(bean, "450"));
        beanList.add(getStandardCurveBean(bean, "500"));
        //
        List<LineChartBean> chartBeanList = new ArrayList<>();
        for (StandardCurveBean curveBean : beanList) {
            if (curveBean != null) {
                chartBeanList.add(new LineChartBean(curveBean.rebarDiameter,0, curveBean.uniqueKey, curveBean.signalArray[curveBean.signalArray.length / 2], curveBean.signalArray));
            }
        }
        lineChartManager.showLineChart2(chartBeanList);
    }

    private StandardCurveBean getStandardCurveBean(StandardCurveBean bean, String poleDiameter) {
        return curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(bean.rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq(poleDiameter), StandardCurveBeanDao.Properties.Thickness.eq(bean.thickness)).unique();
    }

    /**
     * 处理曲线数据（曲面）
     */
    private CheckDataBean treatCurve18(StandardCurveBean bean) {
        //实际距离
        double[] distances = new double[bean.signalArray.length];
        for (int i = 0; i < distances.length; i++) {
            distances[i] = ComputeCurveUtils.calculateDistanceC(bean.rebarDiameter, Double.parseDouble(bean.poleDiameter) / 2, Double.parseDouble(bean.rebarDiameter), bean.thickness, i - bean.signalArray.length / 2);
        }
        Logger.d("aa" + distances);
        //取一半x值和一半信号值
        double[] xs = new double[distances.length / 2 + 1];
        for (int i = 0; i < xs.length; i++) {
            xs[i] = distances[i];
        }
        double[] ys = new double[xs.length];
        for (int i = 0; i < ys.length; i++) {
            ys[i] = bean.signalArray[i];
        }
        CheckDataBean checkDataBean = new CheckDataBean(bean.rebarDiameter + "_" + bean.poleDiameter + "_" + bean.thickness);
        checkDataBean.thickness = bean.thickness;
        checkDataBean.curve_y_original = IntArrayUtils.subarrayDouble(bean.signalArray, 0, bean.signalArray.length);
        checkDataBean.y = checkDataBean.curve_y_original[checkDataBean.curve_y_original.length / 2];
        //重新计算实际距离曲线
        checkDataBean.signalMovAve = ComputeCurveUtils.getCurveY1(xs, ys, xs.length);
//        checkDataBean.curve_y_processed1 = ComputeCurveUtils.standardizeCurve(checkDataBean.curve_y_original);
        return checkDataBean;
    }

    private void showAllLineChart() {
        List<LineChartBean> chartBeanList = new ArrayList<>();
        for (int i = 0; i < listAdapter.getItems().size(); i++) {
            StandardCurveBean curveBean = listAdapter.getItems().get(i);
            chartBeanList.add(new LineChartBean(curveBean.rebarDiameter,0, curveBean.uniqueKey, curveBean.signalArray[curveBean.signalArray.length / 2], curveBean.signalArray));
        }
        if (!chartBeanList.isEmpty()) {
            lineChartManager.showLineChart2(chartBeanList);
        }
    }

    private String currentRebarDiameter;
    private final View.OnClickListener onClickListener = view -> {
        if (binding.btnAll.getId() == view.getId()) {
            queryCurveList(currentRebarDiameter, "all");
        } else if (binding.btn200.getId() == view.getId()) {
            queryCurveList(currentRebarDiameter, "200");
        } else if (binding.btn250.getId() == view.getId()) {
            queryCurveList(currentRebarDiameter, "250");
        } else if (binding.btn310.getId() == view.getId()) {
            queryCurveList(currentRebarDiameter, "310");
        } else if (binding.btn350.getId() == view.getId()) {
            queryCurveList(currentRebarDiameter, "350");
        } else if (binding.btn400.getId() == view.getId()) {
            queryCurveList(currentRebarDiameter, "400");
        } else if (binding.btn450.getId() == view.getId()) {
            queryCurveList(currentRebarDiameter, "450");
        } else if (binding.btn500.getId() == view.getId()) {
            queryCurveList(currentRebarDiameter, "500");
        } else if (binding.btn7.getId() == view.getId()) {
            currentRebarDiameter = "7";
            queryCurveList(currentRebarDiameter, "al");
        } else if (binding.btn9.getId() == view.getId()) {
            currentRebarDiameter = "9";
            queryCurveList(currentRebarDiameter, "al");
        } else if (binding.btn10.getId() == view.getId()) {
            currentRebarDiameter = "10";
            queryCurveList(currentRebarDiameter, "al");
        } else if (binding.btn11.getId() == view.getId()) {
            currentRebarDiameter = "11";
            queryCurveList(currentRebarDiameter, "al");
        } else if (binding.btn12.getId() == view.getId()) {
            currentRebarDiameter = "12";
            queryCurveList(currentRebarDiameter, "al");
        } else if (binding.btn14.getId() == view.getId()) {
            currentRebarDiameter = "14";
            queryCurveList(currentRebarDiameter, "al");
        } else if (binding.btn16.getId() == view.getId()) {
            currentRebarDiameter = "16";
            queryCurveList(currentRebarDiameter, "al");
        }
    };


    /**
     * 数据库查询 指定钢筋直径的所有数据
     */
    @SuppressLint("SetTextI18n")
    public void queryCurveList(String rebarDiameter, String poleDiameter) {
        if (TextUtils.equals("all", poleDiameter)) {
            List<StandardCurveBean> curveBeanList = curveDao.queryBuilder().orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
            showNewData(curveBeanList);
        } else if (TextUtils.equals("al", poleDiameter)) {
            List<StandardCurveBean> curveBeanList = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter)).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
            showNewData(curveBeanList);
        } else {
            if (TextUtils.isEmpty(rebarDiameter)) {
                ToastUtils.showShort("请选择一种钢筋直径");
                return;
            }
            List<StandardCurveBean> curveBeanList = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq(poleDiameter)).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
            showNewData(curveBeanList);
        }
    }

    private void showNewData(List<StandardCurveBean> curveBeanList) {
        listAdapter.submitList(curveBeanList);
        showAllLineChart();

    }
}

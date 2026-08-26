package com.example.gtj_f230_rebound_corrosion.corrosion.activity.testing;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.blackhao.utillibrary.usbHelper.USBBroadCastReceiver;
import com.blackhao.utillibrary.usbHelper.UsbHelper;
import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.base.activity.BaseActivity;
import com.example.gtj_f230_rebound_corrosion.base.storage.GreenDaoHelper;
import com.example.gtj_f230_rebound_corrosion.base.utils.ExportUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.StringUtils;
import com.example.gtj_f230_rebound_corrosion.base.view.StyleAlertDialog;
import com.example.gtj_f230_rebound_corrosion.base.view.StyleAlertDialog1;
import com.example.gtj_f230_rebound_corrosion.base.view.StyleAlertDialog2;
import com.example.gtj_f230_rebound_corrosion.corrosion.adapter.CalibrationCurveListAdapter;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.CheckDataBean;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.LineChartBean;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.StandardCurveBean;
import com.example.gtj_f230_rebound_corrosion.corrosion.storage.CorrosionCurve;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.CubicSplineInterpolationUtils;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.CurveDataUtils;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.IntArrayUtils;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.LineChartManager;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.MovingAverage;
import com.example.gtj_f230_rebound_corrosion.databinding.ActCalibrationCurveListBinding;
import com.example.gtj_f230_rebound_corrosion.functions.database.greenDao.db.StandardCurveBeanDao;
import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.orhanobut.logger.Logger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 标准曲线列表查看（曲面）
 */
@SuppressLint("SdCardPath")
public class CalibrationCurveListActivity extends BaseActivity<ActCalibrationCurveListBinding> {
    private CalibrationCurveListAdapter listAdapter;
    private LineChartManager lineChartManager;
    private StandardCurveBeanDao curveDao;
    //    private final LinkedHashMap<String, StandardCurveBean> hashStandardMap = new LinkedHashMap<>();
    private final LinkedHashMap<String, CheckDataBean> hashCheckDataMap = new LinkedHashMap<>();
    private final List<String> exportPathList = new ArrayList<>();  //导出地址
    private String dataFilePathExport, dataFilePathExportUSb;
    private int exportCount;  // "7", "9", "10", "11", "12", "14", "16"
    private final List<String> poleNormList = new ArrayList<>();
    private String currentRebarDiameter, currentPoleDiameter, currentRebarSpacing;
    private List<Double> standardThicknessList;

    @Override
    protected ActCalibrationCurveListBinding getBinding() {
        return ActCalibrationCurveListBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initLabel() {
        binding.navigationBar.setTitleText("单根标准曲线列表");
        initBatteryReceiver(binding.navigationBar);
        initDeviceStatusExecutor(binding.navigationBar);
    }

    @Override
    protected void initView() {
        lineChartManager = new LineChartManager(this, binding.lineChart);
        curveDao = GreenDaoHelper.getDaoSession(this).getStandardCurveBeanDao();
        dataFilePathExport = "/sdcard/" + getString(R.string.app_name) + "/";
        dataFilePathExportUSb = getString(R.string.app_name) + "/";
        setListener();
        setRecyclerView();
        initUsbHelper();
        initStandardThicknessList();
    }

    private void initStandardThicknessList() {
        standardThicknessList = new ArrayList<>();
        for (double i = 6; i <= 30; i++) {
            standardThicknessList.add(i);
        }
    }

    private void setListener() {
        //钢筋直径
        binding.btn7.setOnClickListener(onClickListener);
        binding.btn8.setOnClickListener(onClickListener);
        binding.btn9.setOnClickListener(onClickListener);
        binding.btn10.setOnClickListener(onClickListener);
        binding.btn11.setOnClickListener(onClickListener);
        binding.btn12.setOnClickListener(onClickListener);
        binding.btn14.setOnClickListener(onClickListener);
        binding.btn16.setOnClickListener(onClickListener);
        //电杆直径
        binding.btn200.setOnClickListener(onClickListener);
        binding.btn250.setOnClickListener(onClickListener);
        binding.btn310.setOnClickListener(onClickListener);
        binding.btn350.setOnClickListener(onClickListener);
        binding.btn400.setOnClickListener(onClickListener);
        binding.btn450.setOnClickListener(onClickListener);
        binding.btn500.setOnClickListener(onClickListener);
        //主筋间距
        binding.btn35.setOnClickListener(onClickListener);
        binding.btn45.setOnClickListener(onClickListener);
        binding.btn55.setOnClickListener(onClickListener);
        //箍筋间距
        binding.btn70.setOnClickListener(onClickListener);
        binding.btn120.setOnClickListener(onClickListener);
        binding.btnAll.setOnClickListener(onClickListener);
        //导入导出
        binding.btnInput.setOnClickListener(onClickListener);
        binding.btnExport.setOnClickListener(onClickListener);
        //标定曲线
        binding.btnCalibration.setOnClickListener(view -> startActivity(new Intent(this, CalibrationCurveActivity1.class)));
        binding.btnFinish.setOnClickListener(view -> finish());
        //长按根据钢筋直径搜索
        binding.btn7.setOnLongClickListener(onLongClickListener);
        binding.btn8.setOnLongClickListener(onLongClickListener);
        binding.btn9.setOnLongClickListener(onLongClickListener);
        binding.btn10.setOnLongClickListener(onLongClickListener);
        binding.btn11.setOnLongClickListener(onLongClickListener);
        binding.btn12.setOnLongClickListener(onLongClickListener);
        binding.btn14.setOnLongClickListener(onLongClickListener);
        binding.btn16.setOnLongClickListener(onLongClickListener);
    }

    private void setRecyclerView() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        listAdapter = new CalibrationCurveListAdapter(this);
        binding.recyclerView.setAdapter(listAdapter);
        listAdapter.setOnItemClickListener((adapter, view, position) -> {
            StandardCurveBean bean = listAdapter.getItems().get(position);
            //
            bean.isSelected = !bean.isSelected;
            listAdapter.notifyItemChanged(position);
            //
            showCurveLines(bean);
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

    private void showCurveLines(StandardCurveBean bean) {
        CheckDataBean checkDataBean = treatCurve(bean);
        //
        if (bean.isSelected) {
            hashCheckDataMap.put(checkDataBean.poleNorm, checkDataBean);
        } else {
            hashCheckDataMap.remove(checkDataBean.poleNorm);
        }
        setLineChartData();
    }

    private void setLineChartData() {
        List<LineChartBean> chartBeanList = new ArrayList<>();
//        if (CorrosionCurve.checkData != null && CorrosionCurve.checkData.length > 0) {
//            chartBeanList.add(new LineChartBean("current", CorrosionCurve.checkData[30], CorrosionCurve.checkData));
//        }
        for (CheckDataBean bean : hashCheckDataMap.values()) {
            chartBeanList.add(new LineChartBean(bean.poleNorm, 0, bean.poleNorm + "标", bean.y, bean.curve_y_original));
        }
        lineChartManager.showLineChart2(chartBeanList);
    }

    private CheckDataBean treatCurve(StandardCurveBean bean) {
        CheckDataBean checkDataBean = new CheckDataBean(bean.uniqueKey);
        checkDataBean.thickness = bean.thickness;
        checkDataBean.curve_y_original = IntArrayUtils.subarrayDouble(bean.signalArray, 0, bean.signalArray.length);
        checkDataBean.y = checkDataBean.curve_y_original[checkDataBean.curve_y_original.length / 2];
        return checkDataBean;
    }

    private void showAllLineChart() {
        List<LineChartBean> chartBeanList = new ArrayList<>();
        for (int i = 0; i < listAdapter.getItems().size(); i++) {
            StandardCurveBean bean = listAdapter.getItems().get(i);
            chartBeanList.add(new LineChartBean(bean.rebarDiameter, 0, bean.uniqueKey, bean.signalArray[bean.signalArray.length / 2], bean.signalArray));
        }
        lineChartManager.showLineChart2(chartBeanList);
    }

    private void showAllLineChart3() {
        List<List<LineChartBean>> chartBeanListAll = new ArrayList<>();
        List<LineChartBean> chartBeanList7 = new ArrayList<>();
        List<LineChartBean> chartBeanList8 = new ArrayList<>();
        List<LineChartBean> chartBeanList9 = new ArrayList<>();
        List<LineChartBean> chartBeanList10 = new ArrayList<>();
        List<LineChartBean> chartBeanList11 = new ArrayList<>();
        List<LineChartBean> chartBeanList12 = new ArrayList<>();
        List<LineChartBean> chartBeanList14 = new ArrayList<>();
        List<LineChartBean> chartBeanList16 = new ArrayList<>();
        for (int i = 0; i < listAdapter.getItems().size(); i++) {
            StandardCurveBean bean = listAdapter.getItems().get(i);
            switch (bean.rebarDiameter) {
                case "7":
                    chartBeanList7.add(new LineChartBean(bean.rebarDiameter, ((int) bean.thickness), bean.uniqueKey, bean.signalArray[bean.signalArray.length / 2], bean.signalArray));
                    break;
                case "8":
                    chartBeanList8.add(new LineChartBean(bean.rebarDiameter, ((int) bean.thickness), bean.uniqueKey, bean.signalArray[bean.signalArray.length / 2], bean.signalArray));
                    break;
                case "9":
                    chartBeanList9.add(new LineChartBean(bean.rebarDiameter, ((int) bean.thickness), bean.uniqueKey, bean.signalArray[bean.signalArray.length / 2], bean.signalArray));
                    break;
                case "10":
                    chartBeanList10.add(new LineChartBean(bean.rebarDiameter, ((int) bean.thickness), bean.uniqueKey, bean.signalArray[bean.signalArray.length / 2], bean.signalArray));
                    break;
                case "11":
                    chartBeanList11.add(new LineChartBean(bean.rebarDiameter, ((int) bean.thickness), bean.uniqueKey, bean.signalArray[bean.signalArray.length / 2], bean.signalArray));
                    break;
                case "12":
                    chartBeanList12.add(new LineChartBean(bean.rebarDiameter, ((int) bean.thickness), bean.uniqueKey, bean.signalArray[bean.signalArray.length / 2], bean.signalArray));
                    break;
                case "14":
                    chartBeanList14.add(new LineChartBean(bean.rebarDiameter, ((int) bean.thickness), bean.uniqueKey, bean.signalArray[bean.signalArray.length / 2], bean.signalArray));
                    break;
                case "16":
                    chartBeanList16.add(new LineChartBean(bean.rebarDiameter, ((int) bean.thickness), bean.uniqueKey, bean.signalArray[bean.signalArray.length / 2], bean.signalArray));
                    break;
            }
        }
        chartBeanListAll.add(chartBeanList7);
        chartBeanListAll.add(chartBeanList8);
        chartBeanListAll.add(chartBeanList9);
        chartBeanListAll.add(chartBeanList10);
        chartBeanListAll.add(chartBeanList11);
        chartBeanListAll.add(chartBeanList12);
        chartBeanListAll.add(chartBeanList14);
        chartBeanListAll.add(chartBeanList16);
        lineChartManager.showLineChart3(chartBeanListAll);
        StringBuilder sbd = new StringBuilder();
        sbd.append("7.  ").append(getSignal(chartBeanList7)).append("\n");
        sbd.append("8.  ").append(getSignal(chartBeanList8)).append("\n");
        sbd.append("9.  ").append(getSignal(chartBeanList9)).append("\n");
        sbd.append("10.  ").append(getSignal(chartBeanList10)).append("\n");
        sbd.append("11.  ").append(getSignal(chartBeanList11)).append("\n");
        sbd.append("12.  ").append(getSignal(chartBeanList12)).append("\n");
        sbd.append("14.  ").append(getSignal(chartBeanList14)).append("\n");
        sbd.append("16.  ").append(getSignal(chartBeanList16)).append("\n");
        //
        StyleAlertDialog1 styleAlertDialog = new StyleAlertDialog1(this);
        styleAlertDialog.setContent(sbd.toString()).setOKButton("确定", v -> styleAlertDialog.dismiss()).show();
    }

    private String getSignal(List<LineChartBean> chartBeanList) {
        StringBuilder sbd = new StringBuilder();
        for (LineChartBean bean : chartBeanList) {
            if (bean.thickness == 15) {
                sbd.append("　　15:  ").append((int) bean.maxSignal);
            }
        }
        for (LineChartBean bean : chartBeanList) {
            if (bean.thickness == 20) {
                sbd.append("　　20:  ").append((int) bean.maxSignal);
            }
        }
        for (LineChartBean bean : chartBeanList) {
            if (bean.thickness == 25) {
                sbd.append("　　25:  ").append((int) bean.maxSignal);
            }
        }
        for (LineChartBean bean : chartBeanList) {
            if (bean.thickness == 30) {
                sbd.append("　　30:  ").append((int) bean.maxSignal);
            }
        }
        for (LineChartBean bean : chartBeanList) {
            if (bean.thickness == 35) {
                sbd.append("　　35:  ").append((int) bean.maxSignal);
            }
        }
        for (LineChartBean bean : chartBeanList) {
            if (bean.thickness == 40) {
                sbd.append("　　40:  ").append((int) bean.maxSignal);
            }
        }
        return sbd.toString();
    }

    private final View.OnLongClickListener onLongClickListener = view -> {
        //钢筋直径
        if (binding.btn7.getId() == view.getId()) {
            queryLongCurveList("7");
        } else if (binding.btn8.getId() == view.getId()) {
            queryLongCurveList("8");
        } else if (binding.btn9.getId() == view.getId()) {
            queryLongCurveList("9");
        } else if (binding.btn10.getId() == view.getId()) {
            queryLongCurveList("10");
        } else if (binding.btn11.getId() == view.getId()) {
            queryLongCurveList("11");
        } else if (binding.btn12.getId() == view.getId()) {
            queryLongCurveList("12");
        } else if (binding.btn14.getId() == view.getId()) {
            queryLongCurveList("14");
        } else if (binding.btn16.getId() == view.getId()) {
            queryLongCurveList("16");
        }
        return false;
    };
    private final View.OnClickListener onClickListener = view -> {
        //钢筋直径
        if (binding.btn7.getId() == view.getId()) {
            currentRebarDiameter = "7";
        } else if (binding.btn8.getId() == view.getId()) {
            currentRebarDiameter = "8";
        } else if (binding.btn9.getId() == view.getId()) {
            currentRebarDiameter = "9";
        } else if (binding.btn10.getId() == view.getId()) {
            currentRebarDiameter = "10";
        } else if (binding.btn11.getId() == view.getId()) {
            currentRebarDiameter = "11";
        } else if (binding.btn12.getId() == view.getId()) {
            currentRebarDiameter = "12";
        } else if (binding.btn14.getId() == view.getId()) {
            currentRebarDiameter = "14";
        } else if (binding.btn16.getId() == view.getId()) {
            currentRebarDiameter = "16";
        }
        //电杆直径
        if (binding.btn200.getId() == view.getId()) {
            currentPoleDiameter = "200";
        } else if (binding.btn250.getId() == view.getId()) {
            currentPoleDiameter = "250";
        } else if (binding.btn310.getId() == view.getId()) {
            currentPoleDiameter = "310";
        } else if (binding.btn350.getId() == view.getId()) {
            currentPoleDiameter = "350";
        } else if (binding.btn400.getId() == view.getId()) {
            currentPoleDiameter = "400";
        } else if (binding.btn450.getId() == view.getId()) {
            currentPoleDiameter = "450";
        } else if (binding.btn500.getId() == view.getId()) {
            currentPoleDiameter = "500";
        }
        //主筋间距
        if (binding.btn35.getId() == view.getId()) {
            currentRebarSpacing = "30";  //TODO  30，65
        } else if (binding.btn45.getId() == view.getId()) {
            currentRebarSpacing = "45";
        } else if (binding.btn55.getId() == view.getId()) {
            currentRebarSpacing = "55";
        }
        //箍筋间距
        if (binding.btn70.getId() == view.getId()) {
            queryCurveList("70");
        } else if (binding.btn120.getId() == view.getId()) {
            queryCurveList("120");
        } else if (binding.btnAll.getId() == view.getId()) {
            queryCurveList("all");
        }
        //导入导出
        if (binding.btnExport.getId() == view.getId()) {
            exportAllData();
        } else if (binding.btnInput.getId() == view.getId()) {
            if (StaticConstant.isLock) {
                ToastUtils.showShort("数据库已锁");
                return;
            }
            inputAllData();
        }
    };


    /**
     * 数据库查询 指定钢筋直径的所有数据
     */
    @SuppressLint("SetTextI18n")
    public void queryCurveList(String spiralSpacing) {
        List<StandardCurveBean> curveBeanList;
        if (TextUtils.equals("all", spiralSpacing)) {
            curveBeanList = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.Thickness.between(25, 35)).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
            refreshNewData(spiralSpacing, curveBeanList, false);
        } else {
            if (TextUtils.isEmpty(currentRebarDiameter)) {
                ToastUtils.showShort("请选择一种钢筋直径");
                return;
            }
            if (TextUtils.isEmpty(currentPoleDiameter)) {
                ToastUtils.showShort("请选择一种电杆直径");
                return;
            }
            curveBeanList = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(currentRebarDiameter), StandardCurveBeanDao.Properties.PoleDiameter.eq(currentPoleDiameter), StandardCurveBeanDao.Properties.SpiralSpacing.eq(spiralSpacing), StandardCurveBeanDao.Properties.RebarSpacing.eq(currentRebarSpacing)).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
            refreshNewData(spiralSpacing, curveBeanList, false);
        }
    }

    private void queryLongCurveList(String rebarDiameter) {
        List<StandardCurveBean> curveBeanList = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(rebarDiameter)).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        refreshNewData("", curveBeanList, false);
    }

    private void refreshNewData(String spiralSpacing, List<StandardCurveBean> curveBeanList, boolean isInsert) {
        List<StandardCurveBean> list;
        if (isInsert) {
            list = insertData(curveBeanList);
        } else {
            list = curveBeanList;
        }
        listAdapter.submitList(list);
        if (TextUtils.equals("all", spiralSpacing)) {
            showAllLineChart3();
        } else {
            showAllLineChart();
        }
    }

//    private void showNewData(List<StandardCurveBean> curveBeanList, boolean isInsert) {
//        List<StandardCurveBean> list;
//        if (curveBeanList.size() < 25 && isInsert) {
//            list = insertData(curveBeanList);
//            //存储
//            curveDao.insertOrReplaceInTx(list);
//        } else {
//            list = curveBeanList;
//        }
//        //过滤

    /// /        filterDataList(curveBeanList);
//        listAdapter.setNewData(list);
//        showAllLineChart();
//    }
    private List<StandardCurveBean> insertData(List<StandardCurveBean> curveBeanList) {
        if (curveBeanList.isEmpty()) {
            return new ArrayList<>();
        }
        // 已知数据点
        StandardCurveBean sCBean = curveBeanList.get(0);
        double[] knownX = new double[curveBeanList.size()];
        double[][] knownY = new double[curveBeanList.size()][sCBean.signalArray.length];
        for (int i = 0; i < curveBeanList.size(); i++) {
            StandardCurveBean curveBean = curveBeanList.get(i);
            //
            knownX[i] = curveBean.thickness;
            //
            System.arraycopy(curveBean.signalArray, 0, knownY[i], 0, curveBean.signalArray.length);
        }
        // 目标x值
        double[] targetX = {10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30};
        //
        // 使用Apache Commons Math进行三次样条插值
        double[][] result = CubicSplineInterpolationUtils.interpolateWithApacheMath(knownX, knownY, targetX, false);
        //
        List<StandardCurveBean> curveList = new ArrayList<>();
        if (result != null) {
            for (int i = 0; i < result.length; i++) {
                double[] signalArray = new double[sCBean.signalArray.length];
                for (int i1 = 0; i1 < signalArray.length; i1++) {
                    signalArray[i1] = result[i][i1];
                }
                curveList.add(new StandardCurveBean(sCBean.rebarDiameter, sCBean.poleDiameter, sCBean.rebarSpacing, sCBean.spiralSpacing, targetX[i], signalArray));
            }
        }
        return curveList;
    }

    private void exportAllData() {
        poleNormList.clear();
        exportPathList.clear();
        progressiveDialog.setTvContent("处理中...").show();
        //
        for (String string : CorrosionCurve.poleNormList) {
            poleNormList.add(string + "-200");
            poleNormList.add(string + "-250");
            poleNormList.add(string + "-310");
            poleNormList.add(string + "-350");
            poleNormList.add(string + "-400");
            poleNormList.add(string + "-450");
            poleNormList.add(string + "-500");
        }
        exportCount = poleNormList.size();
        //
        treatExportData();
    }

    private void treatExportData() {
        int count = poleNormList.size();
        if (count > 0) {
            int rate = (int) StringUtils.getRounding((exportCount - count) * 100.0 / exportCount, 0);
            progressiveDialog.setTvContent("处理中..." + rate + "%").show();
            exportData(poleNormList.get(poleNormList.size() - 1));
        } else {
            progressiveDialog.setTvContent("处理中...100%").show();
            new Handler().postDelayed(() -> progressiveDialog.dismiss(), 1000);
            StringBuilder sbd = new StringBuilder("导出结果：\n");
            for (String s : exportPathList) {
                sbd.append(s).append("\n");
            }
            StyleAlertDialog1 styleAlertDialog = new StyleAlertDialog1(this);
            styleAlertDialog.setContent(sbd.toString()).setOKButton("确定", v -> styleAlertDialog.dismiss()).show();
        }
    }

    private void exportData(String diameter) {
        uiHandler.postDelayed(() -> doInBackgroundExport(diameter), 100);
        uiHandler.postDelayed(this::onPostExecuteExport, 2000);
    }


    private void doInBackgroundExport(String diameter) {
        String strExportString = CurveDataUtils.queryCurveListString(curveDao, diameter);
        if (TextUtils.isEmpty(strExportString)) {
            return;
        }
        //生成数据byte
        byte[] textData = strExportString.getBytes(StandardCharsets.UTF_8);
        if (textData != null) {
            //数据保存到平板
            String dataFileName = dataFilePathExport + diameter + ".csv";
            String strFile = ExportUtils.saveDataFile(dataFileName, textData);
            if (!TextUtils.isEmpty(strFile)) {
                exportPathList.add(diameter + ": 导出---成功");
            } else {
                exportPathList.add(diameter + ": 内存卡读写异常，无法导出");
            }
            //数据保存到USB
            String dataName = dataFilePathExportUSb + diameter + ".csv";
            saveDataToUsb(dataName, textData);
        }
    }

    private void onPostExecuteExport() {
        poleNormList.remove(poleNormList.size() - 1);
        treatExportData();
    }

    //---------------------USB--------------------------
    private UsbHelper usbHelper;

    private void initUsbHelper() {
        //U盘只能识别Pad格式化的U盘或格式化选FAT32的文件系统
        usbHelper = new UsbHelper(this, new USBBroadCastReceiver.UsbListener() {
            @Override
            public void insertUsb(UsbDevice device_add) {
                ToastUtils.showShort("U盘插入");
                initCurrentFolder();
            }

            @Override
            public void removeUsb(UsbDevice device_remove) {
                ToastUtils.showShort("U盘被拔出");
                if (usbHelper != null) {
                    usbHelper.rootFolder = null;
                }
            }

            @Override
            public void getReadUsbPermission(UsbDevice usbDevice) {
                initCurrentFolder();
            }

            @Override
            public void failedReadUsb(UsbDevice usbDevice) {
            }
        });
        initCurrentFolder();
    }

    private final CompositeDisposable mRxEvent = new CompositeDisposable();

    private void initCurrentFolder() {
        Observable<Integer> observableIdentificationUsb = Observable.create(e -> {
            if (usbHelper != null) {
                UsbMassStorageDevice[] usbMassStorageDevices = usbHelper.getDeviceList();
                if (usbMassStorageDevices.length > 0) {
                    if (usbHelper.rootFolder == null) {
                        usbHelper.readDevice(usbMassStorageDevices[0]);
                    }
                    if (usbHelper.rootFolder != null) {
                        ToastUtils.showShort("U盘识别成功");
                    }
                }
            }
        });
        Disposable disposable = observableIdentificationUsb.subscribeOn(Schedulers.newThread()).subscribe();
        mRxEvent.add(disposable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRxEvent.clear();
        if (usbHelper != null) {
            usbHelper.finishUsbHelper();
        }
    }

    private void saveDataToUsb(String fileName, byte[] data) {
        if (usbHelper == null || usbHelper.rootFolder == null) {
            return;
        }
        String strFilePath = usbHelper.saveDataToUsb(fileName, data);
        Logger.d(strFilePath);
    }


    private void inputAllData() {
        StringBuilder sbd = new StringBuilder();
        sbd.append("导入数据");
        String rebarDiameter;
        //钢筋直径：7
        rebarDiameter = "7";
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-200：").append(save(rebarDiameter, "200", rebarDiameter + "-200.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-250：").append(save(rebarDiameter, "250", rebarDiameter + "-250.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-310：").append(save(rebarDiameter, "310", rebarDiameter + "-310.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-350：").append(save(rebarDiameter, "350", rebarDiameter + "-350.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-400：").append(save(rebarDiameter, "400", rebarDiameter + "-400.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-450：").append(save(rebarDiameter, "450", rebarDiameter + "-450.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-500：").append(save(rebarDiameter, "500", rebarDiameter + "-500.csv") ? "导入---成功" : "失败");
        //钢筋直径：9
        rebarDiameter = "9";
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-200：").append(save(rebarDiameter, "200", rebarDiameter + "-200.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-250：").append(save(rebarDiameter, "250", rebarDiameter + "-250.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-310：").append(save(rebarDiameter, "310", rebarDiameter + "-310.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-350：").append(save(rebarDiameter, "350", rebarDiameter + "-350.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-400：").append(save(rebarDiameter, "400", rebarDiameter + "-400.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-450：").append(save(rebarDiameter, "450", rebarDiameter + "-450.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-500：").append(save(rebarDiameter, "500", rebarDiameter + "-500.csv") ? "导入---成功" : "失败");
        //钢筋直径：10
        rebarDiameter = "10";
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-200：").append(save(rebarDiameter, "200", rebarDiameter + "-200.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-250：").append(save(rebarDiameter, "250", rebarDiameter + "-250.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-310：").append(save(rebarDiameter, "310", rebarDiameter + "-310.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-350：").append(save(rebarDiameter, "350", rebarDiameter + "-350.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-400：").append(save(rebarDiameter, "400", rebarDiameter + "-400.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-450：").append(save(rebarDiameter, "450", rebarDiameter + "-450.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-500：").append(save(rebarDiameter, "500", rebarDiameter + "-500.csv") ? "导入---成功" : "失败");
        //钢筋直径：11
        rebarDiameter = "11";
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-200：").append(save(rebarDiameter, "200", rebarDiameter + "-200.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-250：").append(save(rebarDiameter, "250", rebarDiameter + "-250.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-310：").append(save(rebarDiameter, "310", rebarDiameter + "-310.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-350：").append(save(rebarDiameter, "350", rebarDiameter + "-350.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-400：").append(save(rebarDiameter, "400", rebarDiameter + "-400.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-450：").append(save(rebarDiameter, "450", rebarDiameter + "-450.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-500：").append(save(rebarDiameter, "500", rebarDiameter + "-500.csv") ? "导入---成功" : "失败");
        //钢筋直径：12
        rebarDiameter = "12";
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-200：").append(save(rebarDiameter, "200", rebarDiameter + "-200.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-250：").append(save(rebarDiameter, "250", rebarDiameter + "-250.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-310：").append(save(rebarDiameter, "310", rebarDiameter + "-310.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-350：").append(save(rebarDiameter, "350", rebarDiameter + "-350.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-400：").append(save(rebarDiameter, "400", rebarDiameter + "-400.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-450：").append(save(rebarDiameter, "450", rebarDiameter + "-450.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-500：").append(save(rebarDiameter, "500", rebarDiameter + "-500.csv") ? "导入---成功" : "失败");
        //钢筋直径：14
        rebarDiameter = "14";
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-200：").append(save(rebarDiameter, "200", rebarDiameter + "-200.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-250：").append(save(rebarDiameter, "250", rebarDiameter + "-250.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-310：").append(save(rebarDiameter, "310", rebarDiameter + "-310.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-350：").append(save(rebarDiameter, "350", rebarDiameter + "-350.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-400：").append(save(rebarDiameter, "400", rebarDiameter + "-400.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-450：").append(save(rebarDiameter, "450", rebarDiameter + "-450.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-500：").append(save(rebarDiameter, "500", rebarDiameter + "-500.csv") ? "导入---成功" : "失败");
        //钢筋直径：16
        rebarDiameter = "16";
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-200：").append(save(rebarDiameter, "200", rebarDiameter + "-200.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-250：").append(save(rebarDiameter, "250", rebarDiameter + "-250.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-310：").append(save(rebarDiameter, "310", rebarDiameter + "-310.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-350：").append(save(rebarDiameter, "350", rebarDiameter + "-350.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-400：").append(save(rebarDiameter, "400", rebarDiameter + "-400.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-450：").append(save(rebarDiameter, "450", rebarDiameter + "-450.csv") ? "导入---成功" : "失败");
        sbd.append("\n钢筋直径").append(rebarDiameter).append("-500：").append(save(rebarDiameter, "500", rebarDiameter + "-500.csv") ? "导入---成功" : "失败");
        //过滤

        //
        StyleAlertDialog2 styleAlertDialog = new StyleAlertDialog2(this);
        styleAlertDialog.setContent(sbd).setOKButton("确定", v -> styleAlertDialog.dismiss()).show();
    }

    private boolean save(String rebarDiameter, String poleDiameter, String fileName) {
        List<StandardCurveBean> standardCurveBeanList = CurveDataUtils.getCurveList(this, rebarDiameter, poleDiameter, fileName);
        if (standardCurveBeanList == null || standardCurveBeanList.isEmpty()) {
            return false;
        }
        for (StandardCurveBean curveBean : standardCurveBeanList) {
            //平滑曲线
            curveBean.signalMovAve = MovingAverage.movingAverage(curveBean.signalArray, 5);
            //覆盖id
            StandardCurveBean databaseBean = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.UniqueKey.eq(curveBean.uniqueKey)).unique();
            if (databaseBean != null) {
                curveBean.id = databaseBean.id;
            }
            //更新
            curveDao.insertOrReplace(curveBean);
        }
        //过滤数据
        filterDataList(standardCurveBeanList);
        return true;
    }

    private void filterDataList(List<StandardCurveBean> list) {
        List<Long> IdList = new ArrayList<>();
        for (int i = list.size() - 1; i >= 0; i--) {
            if (!standardThicknessList.contains(list.get(i).thickness)) {
                IdList.add(list.get(i).id);
                list.remove(i);
            }
        }
        curveDao.deleteByKeyInTx(IdList);
    }

    private double getMaxValue(double[] data) {
        double max = Double.MIN_VALUE;
        for (double value : data) {
            if (value > max) max = value;  // 找出最大值
        }
        return max;
    }
}

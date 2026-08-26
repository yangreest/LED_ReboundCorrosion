package com.example.gtj_f230_rebound_corrosion.corrosion.activity.testing;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.base.activity.BaseActivity;
import com.example.gtj_f230_rebound_corrosion.base.storage.GreenDaoHelper;
import com.example.gtj_f230_rebound_corrosion.base.utils.ExportUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.StringUtils;
import com.example.gtj_f230_rebound_corrosion.base.view.StyleAlertDialog;
import com.example.gtj_f230_rebound_corrosion.corrosion.adapter.CalibrationMinMaxListAdapter;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.MinMaxBean;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.RebarMinMaxBean;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.ExportMinMaxBeanUtils;
import com.example.gtj_f230_rebound_corrosion.databinding.ActCalibrationMinMaxListBinding;
import com.example.gtj_f230_rebound_corrosion.functions.database.greenDao.db.RebarMinMaxBeanDao;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@SuppressLint({"SetTextI18n", "SdCardPath"})
public class CalibrationMinMaxListActivity extends BaseActivity<ActCalibrationMinMaxListBinding> {
    private String dataFilePathExport;
    private RebarMinMaxBeanDao minMaxBeanDao;
    private List<CalibrationMinMaxListAdapter> adapterList;

    @Override
    protected ActCalibrationMinMaxListBinding getBinding() {
        return ActCalibrationMinMaxListBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initLabel() {
        binding.navigationBar.setTitleText("标准数据列表");
        initBatteryReceiver(binding.navigationBar);
        initDeviceStatusExecutor(binding.navigationBar);
        setRecyclerView();
    }

    @Override
    protected void initView() {
        dataFilePathExport = "/sdcard/" + getString(R.string.app_name) + "/";
        minMaxBeanDao = GreenDaoHelper.getDaoSession(this).getRebarMinMaxBeanDao();
        //钢筋直径
        binding.btn7.setOnClickListener(onClickListener);
        binding.btn8.setOnClickListener(onClickListener);
        binding.btn9.setOnClickListener(onClickListener);
        binding.btn10.setOnClickListener(onClickListener);
        binding.btn11.setOnClickListener(onClickListener);
        binding.btn12.setOnClickListener(onClickListener);
        binding.btn14.setOnClickListener(onClickListener);
        binding.btn16.setOnClickListener(onClickListener);
        //导入导出
        binding.btnExport.setOnClickListener(onClickListener);
        binding.btnInput.setOnClickListener(onClickListener);
        binding.btnCalibration.setOnClickListener(onClickListener);
        //
        binding.btnFinish.setOnClickListener(v -> finish());
//        update();
//        queryInsertList();
    }

    private void setRecyclerView() {
        //35
        binding.recyclerView35.setLayoutManager(new LinearLayoutManager(this));
        CalibrationMinMaxListAdapter listAdapter_35 = new CalibrationMinMaxListAdapter();
        binding.recyclerView35.setAdapter(listAdapter_35);
        //40
        binding.recyclerView40.setLayoutManager(new LinearLayoutManager(this));
        CalibrationMinMaxListAdapter listAdapter_40 = new CalibrationMinMaxListAdapter();
        binding.recyclerView40.setAdapter(listAdapter_40);
        //45
        binding.recyclerView45.setLayoutManager(new LinearLayoutManager(this));
        CalibrationMinMaxListAdapter listAdapter_45 = new CalibrationMinMaxListAdapter();
        binding.recyclerView45.setAdapter(listAdapter_45);
        //50
        binding.recyclerView50.setLayoutManager(new LinearLayoutManager(this));
        CalibrationMinMaxListAdapter listAdapter_50 = new CalibrationMinMaxListAdapter();
        binding.recyclerView50.setAdapter(listAdapter_50);
        //55
        binding.recyclerView55.setLayoutManager(new LinearLayoutManager(this));
        CalibrationMinMaxListAdapter listAdapter_55 = new CalibrationMinMaxListAdapter();
        binding.recyclerView55.setAdapter(listAdapter_55);
        //60
        binding.recyclerView60.setLayoutManager(new LinearLayoutManager(this));
        CalibrationMinMaxListAdapter listAdapter_60 = new CalibrationMinMaxListAdapter();
        binding.recyclerView60.setAdapter(listAdapter_60);
        //65
        binding.recyclerView65.setLayoutManager(new LinearLayoutManager(this));
        CalibrationMinMaxListAdapter listAdapter_65 = new CalibrationMinMaxListAdapter();
        binding.recyclerView65.setAdapter(listAdapter_65);
        //70
        binding.recyclerView70.setLayoutManager(new LinearLayoutManager(this));
        CalibrationMinMaxListAdapter listAdapter_70 = new CalibrationMinMaxListAdapter();
        binding.recyclerView70.setAdapter(listAdapter_70);
        //75
        binding.recyclerView75.setLayoutManager(new LinearLayoutManager(this));
        CalibrationMinMaxListAdapter listAdapter_75 = new CalibrationMinMaxListAdapter();
        binding.recyclerView75.setAdapter(listAdapter_75);
        //3565
        binding.recyclerView3565.setLayoutManager(new LinearLayoutManager(this));
        CalibrationMinMaxListAdapter listAdapter_3565 = new CalibrationMinMaxListAdapter();
        binding.recyclerView3565.setAdapter(listAdapter_3565);
        //4080
        binding.recyclerView4080.setLayoutManager(new LinearLayoutManager(this));
        CalibrationMinMaxListAdapter listAdapter_4080 = new CalibrationMinMaxListAdapter();
        binding.recyclerView4080.setAdapter(listAdapter_4080);
        //50100
        binding.recyclerView50100.setLayoutManager(new LinearLayoutManager(this));
        CalibrationMinMaxListAdapter listAdapter_50100 = new CalibrationMinMaxListAdapter();
        binding.recyclerView50100.setAdapter(listAdapter_50100);
        //
        adapterList = new ArrayList<>();
        adapterList.add(listAdapter_35);
        adapterList.add(listAdapter_40);
        adapterList.add(listAdapter_45);
        adapterList.add(listAdapter_50);
        adapterList.add(listAdapter_55);
        adapterList.add(listAdapter_60);
        adapterList.add(listAdapter_65);
        adapterList.add(listAdapter_70);
        adapterList.add(listAdapter_75);
        adapterList.add(listAdapter_3565);
        adapterList.add(listAdapter_4080);
        adapterList.add(listAdapter_50100);
        setupSyncScroll();
    }

    private boolean isSyncing = false; // 防止循环调用

    private void setupSyncScroll() {
        RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (isSyncing) return; // 如果当前是代码触发的滚动，忽略

                isSyncing = true;
                // 将当前的滚动增量应用到其他RecyclerView
                scrollOtherRecyclerViews(recyclerView, dx, dy);
                isSyncing = false;
            }
        };
        binding.recyclerView35.addOnScrollListener(scrollListener);
        binding.recyclerView40.addOnScrollListener(scrollListener);
        binding.recyclerView45.addOnScrollListener(scrollListener);
        binding.recyclerView50.addOnScrollListener(scrollListener);
        binding.recyclerView55.addOnScrollListener(scrollListener);
        binding.recyclerView60.addOnScrollListener(scrollListener);
        binding.recyclerView65.addOnScrollListener(scrollListener);
        binding.recyclerView70.addOnScrollListener(scrollListener);
        binding.recyclerView75.addOnScrollListener(scrollListener);
        binding.recyclerView3565.addOnScrollListener(scrollListener);
        binding.recyclerView4080.addOnScrollListener(scrollListener);
        binding.recyclerView50100.addOnScrollListener(scrollListener);
    }

    private void scrollOtherRecyclerViews(RecyclerView source, int dx, int dy) {
        // 除了触发源，其他的都滚动
        if (binding.recyclerView35 != source) binding.recyclerView35.scrollBy(dx, dy);
        if (binding.recyclerView40 != source) binding.recyclerView40.scrollBy(dx, dy);
        if (binding.recyclerView45 != source) binding.recyclerView45.scrollBy(dx, dy);
        if (binding.recyclerView50 != source) binding.recyclerView50.scrollBy(dx, dy);
        if (binding.recyclerView55 != source) binding.recyclerView55.scrollBy(dx, dy);
        if (binding.recyclerView60 != source) binding.recyclerView60.scrollBy(dx, dy);
        if (binding.recyclerView65 != source) binding.recyclerView65.scrollBy(dx, dy);
        if (binding.recyclerView70 != source) binding.recyclerView70.scrollBy(dx, dy);
        if (binding.recyclerView75 != source) binding.recyclerView75.scrollBy(dx, dy);
        if (binding.recyclerView3565 != source) binding.recyclerView3565.scrollBy(dx, dy);
        if (binding.recyclerView4080 != source) binding.recyclerView4080.scrollBy(dx, dy);
        if (binding.recyclerView50100 != source) binding.recyclerView50100.scrollBy(dx, dy);
    }

    private final View.OnClickListener onClickListener = view -> {
        //钢筋直径
        if (binding.btn7.getId() == view.getId()) {
            queryCurveList("7");
        } else if (binding.btn8.getId() == view.getId()) {
            queryCurveList("8");
        } else if (binding.btn9.getId() == view.getId()) {
            queryCurveList("9");
        } else if (binding.btn10.getId() == view.getId()) {
            queryCurveList("10");
        } else if (binding.btn11.getId() == view.getId()) {
            queryCurveList("11");
        } else if (binding.btn12.getId() == view.getId()) {
            queryCurveList("12");
        } else if (binding.btn14.getId() == view.getId()) {
            queryCurveList("14");
        } else if (binding.btn16.getId() == view.getId()) {
            queryCurveList("16");
        }
        //导入导出
        if (binding.btnExport.getId() == view.getId()) {
            StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this);
            styleAlertDialog.setContent("确定导出数据？").setLeftButton("取消", v -> styleAlertDialog.dismiss()).setRightButton("确定", v -> {
                styleAlertDialog.dismiss();
                exportAllData();
            }).show();
        } else if (binding.btnInput.getId() == view.getId()) {
            if (StaticConstant.isLock) {
                ToastUtils.showShort("数据库已锁");
                return;
            }
            StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this);
            styleAlertDialog.setContent("确定导入数据？").setLeftButton("取消", v -> styleAlertDialog.dismiss()).setRightButton("确定", v -> {
                styleAlertDialog.dismiss();
                inputAllData();
            }).show();
        } else if (binding.btnCalibration.getId() == view.getId()) {
            startActivity(new Intent(this, CalibrationCurveActivity2.class));
        }
    };

    /**
     * 查询
     */
    private void queryCurveList(String rebarDiameter) {
        List<RebarMinMaxBean[]> minMaxBeans = new ArrayList<>();
        String[] spaces = setTextViewTitle(rebarDiameter);
        for (String s : spaces) {
            minMaxBeans.add(getRebarMinMaxBeans(rebarDiameter, s));
        }
        //
        for (int i = 0; i < minMaxBeans.size(); i++) {
            adapterList.get(i).submitList(getSignalMinMax(minMaxBeans.get(i)));
        }
    }

    /**
     * 更新
     */
    private void update() {
        RebarMinMaxBean[] beans = getRebarMinMaxBeans("14", "50_50");
        if (beans[0] != null) {
            double rate = 1.00 + 0.03;
            for (int i = 0; i < beans[0].signalMin.length; i++) {
//                beans[0].signalMin[i] = StringUtils.getRounding(beans[0].signalMin[i] * rate, 1);
//                beans[0].signalMax[i] = StringUtils.getRounding(beans[0].signalMax[i] * rate, 1);
                //
                beans[1].signalMin[i] = StringUtils.getRounding(beans[1].signalMin[i] * rate, 1);
                beans[1].signalMax[i] = StringUtils.getRounding(beans[1].signalMax[i] * rate, 1);
            }
            RebarMinMaxBeanDao minMaxBeanDao = GreenDaoHelper.getDaoSession(this).getRebarMinMaxBeanDao();
            long id0 = 0, id1 = 0;
//            id0 = minMaxBeanDao.insertOrReplace(beans[0]);
            id1 = minMaxBeanDao.insertOrReplace(beans[1]);
            ToastUtils.showShort(id0 + ", " + id1 + ", ");
        }
    }

    /**
     * 内插 插值
     */
    private void queryInsertList() {
        String rebarDiameter = "16";
        List<RebarMinMaxBean[]> minMaxBeans = new ArrayList<>();
        //35, 40, 45, 50, 55, 60, 65, 70, 75
        // 0   1   2  3   4   5   6   7   8
        String[] spaces = setTextViewTitle(rebarDiameter);
        for (String s : spaces) {
            minMaxBeans.add(getRebarMinMaxBeans(rebarDiameter, s));
        }
        //插入
        List<RebarMinMaxBean> list = new ArrayList<>();
        list.addAll(insertMinMaxBean(minMaxBeans.get(1), minMaxBeans.get(3), 45));  //40, 50
        list.addAll(insertMinMaxBean(minMaxBeans.get(3), minMaxBeans.get(5), 55));  //50, 60
//        list.addAll(insertMinMaxBean(minMaxBeans.get(4), minMaxBeans.get(5), 65));  //55, 60
//        list.addAll(insertMinMaxBean(minMaxBeans.get(3), minMaxBeans.get(4), 65));
//        list.addAll(insertMinMaxBean(minMaxBeans.get(3), minMaxBeans.get(4), 70));
        //
        //存储01
        StringBuilder strMessage = new StringBuilder();
        RebarMinMaxBeanDao minMaxBeanDao = GreenDaoHelper.getDaoSession(this).getRebarMinMaxBeanDao();
        for (RebarMinMaxBean bean : list) {
            RebarMinMaxBean temp = minMaxBeanDao.queryBuilder().where(RebarMinMaxBeanDao.Properties.UniqueKey.eq(bean.uniqueKey)).unique();
            if (temp != null) {
                bean.id = temp.id;
            }
            long id = minMaxBeanDao.insertOrReplace(bean);
            strMessage.append(id).append(", ");
        }
        ToastUtils.showShort(strMessage.toString());
    }

    /**
     * 规格统计
     */
    private String[] setTextViewTitle(String rebarDiameter) {
        String[] spaces = new String[12];
        spaces[0] = "35_35";
        spaces[1] = "40_40";
        spaces[2] = "45_45";
        spaces[3] = "50_50";
        spaces[4] = "55_55";
        spaces[5] = "60_60";
        spaces[6] = "65_65";
        spaces[7] = "70_70";
        spaces[8] = "75_75";
        spaces[9] = "35_65";
        spaces[10] = "40_80";
        spaces[11] = "50_100";
        String strEnd = "一70_120";
        binding.tv35.setText(rebarDiameter + "一" + spaces[0] + strEnd);
        binding.tv40.setText(rebarDiameter + "一" + spaces[1] + strEnd);
        binding.tv45.setText(rebarDiameter + "一" + spaces[2] + strEnd);
        binding.tv50.setText(rebarDiameter + "一" + spaces[3] + strEnd);
        binding.tv55.setText(rebarDiameter + "一" + spaces[4] + strEnd);
        binding.tv60.setText(rebarDiameter + "一" + spaces[5] + strEnd);
        binding.tv65.setText(rebarDiameter + "一" + spaces[6] + strEnd);
        binding.tv70.setText(rebarDiameter + "一" + spaces[7] + strEnd);
        binding.tv75.setText(rebarDiameter + "一" + spaces[8] + strEnd);
        binding.tv3565.setText(rebarDiameter + "一" + spaces[9] + strEnd);
        binding.tv4080.setText(rebarDiameter + "一" + spaces[10] + strEnd);
        binding.tv50100.setText(rebarDiameter + "一" + spaces[11] + strEnd);
        return spaces;
    }

    /**
     * 内插法
     */
    private List<RebarMinMaxBean> insertMinMaxBean(RebarMinMaxBean[] bean_0, RebarMinMaxBean[] bean_1, int mm) {
        //内插数据
        List<RebarMinMaxBean> list = new ArrayList<>();
        //70-min-max
        list.add(aaa(Integer.parseInt(bean_0[0].rebarSpacingMin), bean_0[0], Integer.parseInt(bean_1[0].rebarSpacingMin), bean_1[0], mm));
        //120-min-max
        list.add(aaa(Integer.parseInt(bean_0[1].rebarSpacingMin), bean_0[1], Integer.parseInt(bean_1[1].rebarSpacingMin), bean_1[1], mm));
        return list;
    }

    /**
     * 查询（根据钢筋直径、间距）
     */
    private RebarMinMaxBean[] getRebarMinMaxBeans(String rebarDiameter, String s) {
        RebarMinMaxBean[] beans = new RebarMinMaxBean[2];
        //35_70
        RebarMinMaxBean bean_35_70 = new RebarMinMaxBean(rebarDiameter, "0", s.split("_")[0], s.split("_")[1], "70");
        beans[0] = minMaxBeanDao.queryBuilder().where(RebarMinMaxBeanDao.Properties.UniqueKey.eq(bean_35_70.uniqueKey)).unique();
        //35_120
        RebarMinMaxBean bean_35_120 = new RebarMinMaxBean(rebarDiameter, "0", s.split("_")[0], s.split("_")[1], "120");
        beans[1] = minMaxBeanDao.queryBuilder().where(RebarMinMaxBeanDao.Properties.UniqueKey.eq(bean_35_120.uniqueKey)).unique();
        return beans;
    }

    /**
     * 组装厚度list
     */
    private List<MinMaxBean> getSignalMinMax(RebarMinMaxBean[] bean) {
        List<MinMaxBean> list = new ArrayList<>();
        if (bean[0] != null && bean[0].signalMin != null && bean[0].signalMin.length > 0) {
            int size = bean[0].signalMin.length;
            for (int i = 0; i < size; i++) {
                list.add(new MinMaxBean(i + 1, bean[0].signalMin[i], bean[0].signalMax[i], bean[1].signalMin[i], bean[1].signalMax[i]));
            }
        }
        return list;
    }

    /**
     * 导出
     */
    private void exportAllData() {
        String strExportString = ExportMinMaxBeanUtils.toString(minMaxBeanDao.loadAll());
        if (TextUtils.isEmpty(strExportString)) {
            return;
        }
        //生成数据byte
        byte[] textData = strExportString.getBytes(StandardCharsets.UTF_8);
        if (textData != null) {
            //数据保存到平板
            String dataFileName = dataFilePathExport + "RebarMinMax.csv";
            String strFile = ExportUtils.saveDataFile(dataFileName, textData);
            if (!TextUtils.isEmpty(strFile)) {
                ToastUtils.showShort("导出成功");
            } else {
                ToastUtils.showShort("内存卡读写异常，无法导出");
            }
        }
    }

    /**
     * 导入
     */
    private void inputAllData() {
        //导入数据
        List<RebarMinMaxBean> minMaxBeanList = ExportMinMaxBeanUtils.getRebarMinMaxBeanList(this, "RebarMinMax.csv");
        if (minMaxBeanList == null || minMaxBeanList.isEmpty()) {
            ToastUtils.showShort("导入失败");
            return;
        }
        //查找已存在id
        for (RebarMinMaxBean bean : minMaxBeanList) {
            RebarMinMaxBean temp = minMaxBeanDao.queryBuilder().where(RebarMinMaxBeanDao.Properties.UniqueKey.eq(bean.uniqueKey)).unique();
            if (temp != null) {
                bean.id = temp.id;
            }
        }
        //存储
        minMaxBeanDao.insertOrReplaceInTx(minMaxBeanList);
        //查询
        int size = minMaxBeanDao.loadAll().size();
        ToastUtils.showShort("导入成功，" + size);
    }

    private RebarMinMaxBean aaa(int min, RebarMinMaxBean bean_min, int max, RebarMinMaxBean bean_max, int mm) {
        RebarMinMaxBean midd = new RebarMinMaxBean(bean_min.rebarDiameter, bean_min.poleDiameter, mm + "", mm + "", bean_min.spiralSpacing);
        midd.signalMin = new double[bean_min.signalMin.length];
        midd.signalMax = new double[bean_min.signalMax.length];
        for (int i = 0; i < midd.signalMin.length; i++) {
            midd.signalMin[i] = StringUtils.getInterpolationValue(min, bean_min.signalMin[i], max, bean_max.signalMin[i], mm);
            midd.signalMax[i] = StringUtils.getInterpolationValue(min, bean_min.signalMax[i], max, bean_max.signalMax[i], mm);
        }
        return midd;
    }
}

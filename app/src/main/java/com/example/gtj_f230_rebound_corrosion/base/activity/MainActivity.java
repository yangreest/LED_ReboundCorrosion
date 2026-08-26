package com.example.gtj_f230_rebound_corrosion.base.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.base.activity.data.DataManagerActivity;
import com.example.gtj_f230_rebound_corrosion.base.activity.detect.DetectionActivity;
import com.example.gtj_f230_rebound_corrosion.base.activity.setting.SettingActivity;
import com.example.gtj_f230_rebound_corrosion.base.storage.GreenDaoHelper;
import com.example.gtj_f230_rebound_corrosion.base.utils.PermissionUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.rectify.RebounderRectifyUtils;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.RebarMinMaxBean;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.ExportMinMaxBeanUtils;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.fittingcurve.ComputeUtils;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.symmetry.LinearInterpolationExtrapolation;
import com.example.gtj_f230_rebound_corrosion.databinding.ActivityMainBinding;
import com.example.gtj_f230_rebound_corrosion.functions.database.greenDao.db.ConfigBeanDao;
import com.example.gtj_f230_rebound_corrosion.functions.database.greenDao.db.RebarMinMaxBeanDao;
import com.example.gtj_f230_rebound_corrosion.thickness.model.ConfigBean;
import com.inuker.bluetooth.library.BluetoothManager;

import org.opencv.android.OpenCVLoader;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("SetTextI18n")
public class MainActivity extends BaseMainActivity<ActivityMainBinding> {

    @Override
    protected ActivityMainBinding getBinding() {
        return ActivityMainBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initLabel() {
        initBatteryReceiver(binding.navigationBar);
        initDeviceStatusExecutor(binding.navigationBar);
    }

    @Override
    protected void initView() {
        binding.ivTesting.setOnClickListener(this::onViewClicked);
        binding.tvTesting.setOnClickListener(this::onViewClicked);
        binding.ivData.setOnClickListener(this::onViewClicked);
        binding.tvData.setOnClickListener(this::onViewClicked);
        binding.ivSetting.setOnClickListener(this::onViewClicked);
        binding.tvSetting.setOnClickListener(this::onViewClicked);
        binding.tvName.setText(getString(R.string.app_model) + " " + getString(R.string.app_name));
        if (!OpenCVLoader.initDebug()) {
            Log.e("MainActivity", "OpenCV 初始化失败");
        }
        RebounderRectifyUtils.initRectifyUtils(this);
        inputAllData();
        insertConfig();
//        CommonsMathPowerFit.main(null);
//        aaa();
    }

    @Override
    protected void onStart() {
        super.onStart();
        PermissionUtils.initPermission(this, () -> {
        });
    }

    public void onViewClicked(View view) {
        if (view.getId() == R.id.iv_testing || view.getId() == R.id.tv_testing) {
            startActivity(new Intent(this, DetectionActivity.class));  //DetectionActivity | CalibrationCurveListActivity
        } else if (view.getId() == R.id.iv_data || view.getId() == R.id.tv_data) {
            startActivity(new Intent(this, DataManagerActivity.class));
//            selectSize();
        } else if (view.getId() == R.id.iv_setting || view.getId() == R.id.tv_setting) {
            startActivity(new Intent(this, SettingActivity.class));
//            createMinMax();
        }
    }

    private long mExitTime;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                ToastUtils.showShort("再按一次退出程序");
                mExitTime = System.currentTimeMillis();
                return false;
            } else {
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void insertConfig() {
        //普通混凝土电杆
        addConfig(StaticConstant.poleType_PT, 0, 17, "190mm，10米杆，I级", "10", 190, 323, 10, 4);
        addConfig(StaticConstant.poleType_PT, 0, 17, "190mm，12米杆，M级", "12", 190, 350, 14, 4);
        addConfig(StaticConstant.poleType_PT, 0, 17, "190mm，15米杆，M级", "15", 190, 390, 14, 4);
        //预应力混凝土电杆Y
        addConfig(StaticConstant.poleType_Y, 0, 17, "Z Ø150×8×G×Y", "8", 150, 257, 7, 3);
        addConfig(StaticConstant.poleType_Y, 0, 17, "Z Ø190×10×K×Y", "10", 190, 323, 7, 3);
        addConfig(StaticConstant.poleType_Y, 0, 17, "Z Ø190×12×K×Y", "12", 190, 350, 7, 3);
        addConfig(StaticConstant.poleType_Y, 0, 17, "Z Ø190×15×K×Y", "15", 190, 390, 7, 3);
        addConfig(StaticConstant.poleType_Y, 1, 17, "Z Ø190×12(6+6)×K×Y", "12", 190, 390, 7, 3);
        addConfig(StaticConstant.poleType_Y, 1, 17, "Z Ø190×15(9+6)×K×Y", "15", 190, 390, 7, 3);
        //部分预应力混凝土电杆BY
        addConfig(StaticConstant.poleType_BY, 0, 17, "Z Ø190×12×100×BY", "12", 190, 350, 11, 3);
        addConfig(StaticConstant.poleType_BY, 0, 17, "Z Ø190×12×M×BY", "12", 190, 350, 9, 3);
        addConfig(StaticConstant.poleType_BY, 0, 17, "Z Ø190×15×M×BY", "15", 190, 390, 9, 4);
        addConfig(StaticConstant.poleType_BY, 0, 17, "Z Ø230×10×O×BY", "10", 230, 323, 9, 4);
        addConfig(StaticConstant.poleType_BY, 0, 17, "Z Ø230×12×125×BY", "12", 230, 390, 11, 4);
        addConfig(StaticConstant.poleType_BY, 0, 17, "Z Ø230×15×150×BY", "15", 230, 430, 9, 4);
        addConfig(StaticConstant.poleType_BY, 0, 17, "Z Ø350×15×U×BY", "15", 350, 550, 11, 4);
        addConfig(StaticConstant.poleType_BY, 1, 17, "Z Ø190×15(9+6)×100×BY", "15", 190, 390, 9, 3);
        addConfig(StaticConstant.poleType_BY, 1, 17, "Z Ø190×18(9+9)×M×BY", "18", 190, 430, 9, 4);
        addConfig(StaticConstant.poleType_BY, 1, 17, "Z Ø310×12(6+6)×O×BY", "12", 310, 470, 9, 4);
        addConfig(StaticConstant.poleType_BY, 1, 17, "Z Ø310×12(6+6)×U×BY", "12", 310, 470, 9, 4);
        addConfig(StaticConstant.poleType_BY, 1, 17, "Z Ø230×12(6+6)×O×BY", "12", 230, 390, 9, 4);
    }

    private void addConfig(String poleType, int poleStructure, int designThickness, String name, String height, int topDiameter, int bottomDiameter, int rebarDiameter, int hoopingDiameter) {
        List<ConfigBean> configBeanList = GreenDaoHelper.getDaoSession(this).getConfigBeanDao().queryBuilder().where(ConfigBeanDao.Properties.PoleName.eq(name)).list();
        long id = -1;
        if (!configBeanList.isEmpty()) {
            id = configBeanList.get(0).id;
        }
        ConfigBean configBean = new ConfigBean();
        if (id > 0) {
            configBean.id = id;
        }
        configBean.poleType = poleType;
        configBean.poleStructure = poleStructure;
        configBean.poleName = name;
        configBean.poleTopDiameter = topDiameter;
        configBean.poleBottomDiameter = bottomDiameter;
        configBean.rebarDiameter = rebarDiameter;
        configBean.hoopingDiameter = hoopingDiameter;
        configBean.poleHeight = height;
        configBean.designThickness = designThickness + "";
        //
        GreenDaoHelper.getDaoSession(this).getConfigBeanDao().insertOrReplace(configBean);
    }

    /**
     * 导入
     */
    private void inputAllData() {
        if (StaticConstant.isLock) {
            return;
        }
        //导入数据
        List<RebarMinMaxBean> minMaxBeanList = ExportMinMaxBeanUtils.getRebarMinMaxBeanList(this, "RebarMinMax.csv");
        if (minMaxBeanList == null || minMaxBeanList.isEmpty()) {
            return;
        }
        RebarMinMaxBeanDao minMaxBeanDao = GreenDaoHelper.getDaoSession(this).getRebarMinMaxBeanDao();
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
//        int size = minMaxBeanDao.loadAll().size();
//        ToastUtils.showShort("导入成功，" + size);
    }

    private final double[] targetXs = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40};

    private void createMinMax() {
        String rebarDiameter = "14";  //主筋直径  7，8，9，10，11，12，14，16
        String poleDiameter = "0";  //电杆直径  244，300，350，400，450
        //主筋间距：30
        //箍筋间距：70
        double[] xs_30_70 = {10, 15, 20, 25, 30, 35, 40};
        double[] ys_30_70_min = {4280, 2357, 1460, 892, 616, 357, 291};
        double[] ys_30_70_max = {5164, 2645, 1532, 941, 653, 449, 333};
        double[] s_30_70_min = LinearInterpolationExtrapolation.interpolateBatch(xs_30_70, ys_30_70_min, targetXs);
        double[] s_30_70_max = LinearInterpolationExtrapolation.interpolateBatch(xs_30_70, ys_30_70_max, targetXs);
        RebarMinMaxBean bean_30_70 = new RebarMinMaxBean(rebarDiameter, poleDiameter, "40", "80", "70", s_30_70_min, s_30_70_max);
        //箍筋间距：120
        double[] xs_30_120 = {10, 15, 20, 25, 30, 35, 40};
        double[] ys_30_120_min = {3776, 1950, 1137, 666, 455, 303, 210};
        double[] ys_30_120_max = {5158, 2519, 1411, 818, 538, 364, 264};
        double[] s_30_120_min = LinearInterpolationExtrapolation.interpolateBatch(xs_30_120, ys_30_120_min, targetXs);
        double[] s_30_120_max = LinearInterpolationExtrapolation.interpolateBatch(xs_30_120, ys_30_120_max, targetXs);
        RebarMinMaxBean bean_30_120 = new RebarMinMaxBean(rebarDiameter, poleDiameter, "40", "80", "120", s_30_120_min, s_30_120_max);
        //
        //主筋间距：45
        //箍筋间距：70
        double[] xs_45_70 = {10, 15, 20, 25, 30, 35, 40};
        double[] ys_45_70_min = {4280, 2357, 1460, 892, 616, 357, 291};
        double[] ys_45_70_max = {5164, 2645, 1532, 941, 653, 449, 333};
        double[] s_45_70_min = LinearInterpolationExtrapolation.interpolateBatch(xs_45_70, ys_45_70_min, targetXs);
        double[] s_45_70_max = LinearInterpolationExtrapolation.interpolateBatch(xs_45_70, ys_45_70_max, targetXs);
        RebarMinMaxBean bean_45_70 = new RebarMinMaxBean(rebarDiameter, poleDiameter, "50", "100", "70", s_45_70_min, s_45_70_max);
        //箍筋间距：120
        double[] xs_45_120 = {10, 15, 20, 25, 30, 35, 40};
        double[] ys_45_120_min = {3776, 1950, 1137, 666, 455, 303, 210};
        double[] ys_45_120_max = {5158, 2519, 1411, 818, 538, 364, 264};
        double[] s_45_120_min = LinearInterpolationExtrapolation.interpolateBatch(xs_45_120, ys_45_120_min, targetXs);
        double[] s_45_120_max = LinearInterpolationExtrapolation.interpolateBatch(xs_45_120, ys_45_120_max, targetXs);
        RebarMinMaxBean bean_45_120 = new RebarMinMaxBean(rebarDiameter, poleDiameter, "50", "100", "120", s_45_120_min, s_45_120_max);

        //主筋间距：55
        //箍筋间距：70
        double[] xs_55_70 = {10, 15, 20, 25, 30, 35, 40};
        double[] ys_55_70_min = {4280, 2357, 1460, 892, 616, 357, 291};
        double[] ys_55_70_max = {5164, 2645, 1532, 941, 653, 449, 333};
        double[] s_55_70_min = LinearInterpolationExtrapolation.interpolateBatch(xs_55_70, ys_55_70_min, targetXs);
        double[] s_55_70_max = LinearInterpolationExtrapolation.interpolateBatch(xs_55_70, ys_55_70_max, targetXs);
//        RebarMinMaxBean bean_55_70 = new RebarMinMaxBean(rebarDiameter, poleDiameter, "35", "65", "70", s_55_70_min, s_55_70_max);
        //箍筋间距：120
        double[] xs_55_120 = {10, 15, 20, 25, 30, 35, 40};
        double[] ys_55_120_min = {3776, 1950, 1137, 666, 455, 303, 210};
        double[] ys_55_120_max = {5158, 2519, 1411, 818, 538, 364, 264};
        double[] s_55_120_min = LinearInterpolationExtrapolation.interpolateBatch(xs_55_120, ys_55_120_min, targetXs);
        double[] s_55_120_max = LinearInterpolationExtrapolation.interpolateBatch(xs_55_120, ys_55_120_max, targetXs);
//        RebarMinMaxBean bean_55_120 = new RebarMinMaxBean(rebarDiameter, poleDiameter, "35", "65", "120", s_55_120_min, s_55_120_max);
        //
        //主筋间距：65
        //箍筋间距：70
        double[] xs_65_70 = {10, 15, 20, 25, 30, 35, 40};
        double[] ys_65_70_min = {3721, 2081, 1304, 828, 566, 395, 292};
        double[] ys_65_70_max = {5069, 2492, 1438, 863, 591, 416, 316};
        double[] s_65_70_min = LinearInterpolationExtrapolation.interpolateBatch(xs_65_70, ys_65_70_min, targetXs);
        double[] s_65_70_max = LinearInterpolationExtrapolation.interpolateBatch(xs_65_70, ys_65_70_max, targetXs);
//        RebarMinMaxBean bean_65_70 = new RebarMinMaxBean(rebarDiameter, poleDiameter, "60", "60", "70", s_65_70_min, s_65_70_max);
        //箍筋间距：120
        double[] xs_65_120 = {10, 15, 20, 25, 30, 35, 40};
        double[] ys_65_120_min = {3322, 1762, 1075, 685, 475, 317, 230};
        double[] ys_65_120_max = {4688, 2284, 1279, 760, 519, 373, 277};
        double[] s_65_120_min = LinearInterpolationExtrapolation.interpolateBatch(xs_65_120, ys_65_120_min, targetXs);
        double[] s_65_120_max = LinearInterpolationExtrapolation.interpolateBatch(xs_65_120, ys_65_120_max, targetXs);
//        RebarMinMaxBean bean_65_120 = new RebarMinMaxBean(rebarDiameter, poleDiameter, "60", "60", "120", s_65_120_min, s_65_120_max);
//        //
//        //主筋间距：30-60
//        //箍筋间距：70
//        double[] xs_3060_70 = {10, 15, 20, 25, 30, 35, 40};
//        double[] ys_3060_70_min = {2293, 1485, 932, 575, 408, 283, 206};
//        double[] ys_3060_70_max = {3238, 1604, 965, 621, 446, 317, 232};
//        double[] s_3060_70_min = LinearInterpolationExtrapolation.interpolateBatch(xs_3060_70, ys_3060_70_min, targetXs);
//        double[] s_3060_70_max = LinearInterpolationExtrapolation.interpolateBatch(xs_3060_70, ys_3060_70_max, targetXs);
//        RebarMinMaxBean bean_3060_70 = new RebarMinMaxBean(rebarDiameter, poleDiameter, "35", "65", "70", s_3060_70_min, s_3060_70_max);
//        //箍筋间距：120
//        double[] xs_3060_120 = {10, 15, 20, 25, 30, 35, 40};
//        double[] ys_3060_120_min = {2260, 1200, 746, 356, 208, 120, 86};
//        double[] ys_3060_120_max = {3124, 1526, 872, 510, 361, 254, 190};
//        double[] s_3060_120_min = LinearInterpolationExtrapolation.interpolateBatch(xs_3060_120, ys_3060_120_min, targetXs);
//        double[] s_3060_120_max = LinearInterpolationExtrapolation.interpolateBatch(xs_3060_120, ys_3060_120_max, targetXs);
//        RebarMinMaxBean bean_3060_120 = new RebarMinMaxBean(rebarDiameter, poleDiameter, "35", "65", "120", s_3060_120_min, s_3060_120_max);
        //
        List<RebarMinMaxBean> list = new ArrayList<>();
        list.add(bean_30_70);
        list.add(bean_30_120);
        list.add(bean_45_70);
        list.add(bean_45_120);
//        list.add(bean_55_70);
//        list.add(bean_55_120);
//        list.add(bean_65_70);
//        list.add(bean_65_120);
//        list.add(bean_3060_70);
//        list.add(bean_3060_120);
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

    private void selectSize() {
        RebarMinMaxBeanDao minMaxBeanDao = GreenDaoHelper.getDaoSession(this).getRebarMinMaxBeanDao();
        List<RebarMinMaxBean> list = minMaxBeanDao.loadAll();
        ToastUtils.showShort(list.size() + "");
    }

    private void aaa() {
        List<int[]> spaces = new ArrayList<>();
        spaces.add(new int[]{35, 35});
        spaces.add(new int[]{40, 40});
        spaces.add(new int[]{45, 45});
        spaces.add(new int[]{50, 50});
        spaces.add(new int[]{55, 55});
        spaces.add(new int[]{60, 60});
        spaces.add(new int[]{65, 65});
        spaces.add(new int[]{70, 70});
        spaces.add(new int[]{75, 75});
        spaces.add(new int[]{35, 65});
        spaces.add(new int[]{40, 80});
        spaces.add(new int[]{50, 100});
        //
        //10
        Log.e("====", "================================= 10");
        ComputeUtils.findMatchingSpaces(spaces, new double[]{52, 58});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{57, 61});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{60, 107});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{0, 0});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{57, 106});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{57, 109});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{57, 70});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{45, 57});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{43, 53});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{53, 57});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{48, 57});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{54, 58});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{53, 54});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{32, 92});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{41, 76});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{38, 84});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{55, 61});
        //
        //14
        Log.e("====", "================================= 14");
        ComputeUtils.findMatchingSpaces(spaces, new double[]{71, 73});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{74, 92});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{30, 68});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{0, 0});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{29, 69});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{66, 68});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{65, 68});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{65, 94});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{91, 95});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{48, 93});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{48, 49});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{49, 50});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{49, 89});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{40, 98});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{40, 50});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{51, 54});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{47, 96});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{60, 80});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{67, 73});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{39, 50});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{40, 49});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{41, 52});
        ComputeUtils.findMatchingSpaces(spaces, new double[]{27.71, 68.93});
        Log.e("========", "========================START===");
        for (int i = 20; i < 80; i++) {
            int ss = 34;
            int min = Math.min(i, ss);
            int max = Math.max(i, ss);
            ComputeUtils.findMatchingSpaces(spaces, new double[]{min, max});
        }
        Log.e("========", "========================END===");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BluetoothManager.unConnect(SPUtils.getInstance().getString(StaticConstant.SP_CORROSION_BLE_MAC));
        BluetoothManager.unConnect(SPUtils.getInstance().getString(StaticConstant.SP_REBOUND_BLE_MAC));
    }
}

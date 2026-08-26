package com.example.gtj_f230_rebound_corrosion.base.activity.detect;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.base.activity.BaseActivity;
import com.example.gtj_f230_rebound_corrosion.base.activity.data.DataDetailsActivity;
import com.example.gtj_f230_rebound_corrosion.base.model.ProjectBean;
import com.example.gtj_f230_rebound_corrosion.base.model.ZoneBean;
import com.example.gtj_f230_rebound_corrosion.base.storage.GreenDaoHelper;
import com.example.gtj_f230_rebound_corrosion.base.utils.SpinnerUtils;
import com.example.gtj_f230_rebound_corrosion.base.view.StyleAlertDialog;
import com.example.gtj_f230_rebound_corrosion.corrosion.activity.testing.CorrosionDetectActivity2;
import com.example.gtj_f230_rebound_corrosion.databinding.ActDetectionBinding;
import com.example.gtj_f230_rebound_corrosion.f230.activity.testing.width.WidthDetectActivity;
import com.example.gtj_f230_rebound_corrosion.functions.database.greenDao.db.ZoneBeanDao;
import com.example.gtj_f230_rebound_corrosion.rebound.activity.testing.RebounderDetectActivity;
import com.example.gtj_f230_rebound_corrosion.thickness.activity.DetectActivity;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("SetTextI18n")
public class DetectionActivity extends BaseActivity<ActDetectionBinding> {

    private ProjectBean projectBean;
    private ZoneBean zoneBean;

    @Override
    protected ActDetectionBinding getBinding() {
        return ActDetectionBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initLabel() {
        initBatteryReceiver(binding.navigationBar);
        initDeviceStatusExecutor(binding.navigationBar);
        binding.navigationBar.setTitleText(getString(R.string.app_name));
    }

    @Override
    protected void initView() {
        binding.tvNew.setOnClickListener(this::onViewClicked);
        binding.tvJia.setOnClickListener(this::onViewClicked);
        binding.tvJian.setOnClickListener(this::onViewClicked);
        binding.btnF230.setOnClickListener(this::onViewClicked);
        binding.tvF230Save.setOnClickListener(this::onViewClicked);
        binding.btnRebound.setOnClickListener(this::onViewClicked);
        binding.tvReboundSave.setOnClickListener(this::onViewClicked);
        binding.btnCorrosion.setOnClickListener(this::onViewClicked);
        binding.tvCorrosionSave.setOnClickListener(this::onViewClicked);
        binding.btnReturn.setOnClickListener(this::onViewClicked);
        setProjectList(false);
        boolean isCorrosion = TextUtils.equals(StaticConstant.APP_TYPE_DEFAULT, StaticConstant.APP_CORROSION);
        binding.tv01.setText(isCorrosion ? "锈蚀检测" : "保护层厚度检测");
    }

    public void onViewClicked(View view) {
        Intent intent;
        if (view.getId() == binding.tvNew.getId()) {
            intent = new Intent(this, NewProjectActivity.class);
            startActivityForResult(intent, 1);
        } else if (view.getId() == binding.tvJia.getId()) {
            String num = binding.etNumber.getText().toString().trim();
            if (!TextUtils.isEmpty(num)) {
                binding.etNumber.setText(Integer.parseInt(num) + 1 + "");
            }
        } else if (view.getId() == binding.tvJian.getId()) {
            String num = binding.etNumber.getText().toString().trim();
            if (!TextUtils.isEmpty(num)) {
                binding.etNumber.setText(Integer.parseInt(num) == 0 ? "0" : Integer.parseInt(num) - 1 + "");
            }
        } else if (view.getId() == binding.btnF230.getId()) {
            if (!checkInfo()) {
                return;
            }
            if (TextUtils.isEmpty(SPUtils.getInstance().getString(StaticConstant.SP_F230_WIFI_NAME))) {
                ToastUtils.showShort("请先连接缝宽探头");
                return;
            }
            dotClick();
            intent = new Intent(this, WidthDetectActivity.class);
            intent.putExtra("ZoneBean", zoneBean);
            startActivity(intent);
        } else if (view.getId() == binding.tvF230Save.getId()) {
            if (zoneBean == null || zoneBean.f230ZoneBean == null || zoneBean.f230ZoneBean.zoneCount == 0 || TextUtils.isEmpty(zoneBean.f230ZoneBean.strZoneList)) {
                ToastUtils.showShort("暂无图像");
                return;
            }
            intent = new Intent(this, DataDetailsActivity.class);
            intent.putExtra("ProjectBean", projectBean);
            intent.putExtra("ZoneBean", zoneBean);
            intent.putExtra("index", 0);
            startActivity(intent);
        } else if (view.getId() == binding.btnRebound.getId()) {
            if (!checkInfo()) {
                return;
            }
            if (TextUtils.isEmpty(SPUtils.getInstance().getString(StaticConstant.SP_REBOUND_BLE_NAME))) {
                ToastUtils.showShort("请先进行蓝牙设备连接");
                return;
            }
            dotClick();
            if (zoneBean.rebounderZoneBean != null && !TextUtils.isEmpty(zoneBean.rebounderZoneBean.strZoneList)) {
                StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this);
                styleAlertDialog.setContent("确定删除已存测点数据，重新检测？").setLeftButton("取消", v1 -> styleAlertDialog.dismiss()).setRightButton("确定", v1 -> {
                    styleAlertDialog.dismiss();
                    zoneBean.rebounderZoneBean = null;
                    GreenDaoHelper.getDaoSession(this).getZoneBeanDao().insertOrReplace(zoneBean);
                    Intent intent1 = new Intent(this, RebounderDetectActivity.class);
                    intent1.putExtra("ZoneBean", zoneBean);
                    startActivity(intent1);
                }).show();
            } else {
                intent = new Intent(this, RebounderDetectActivity.class);
                intent.putExtra("ZoneBean", zoneBean);
                startActivity(intent);
            }
        } else if (view.getId() == binding.tvReboundSave.getId()) {
            if (zoneBean == null || zoneBean.rebounderZoneBean == null || TextUtils.isEmpty(zoneBean.rebounderZoneBean.strZoneList)) {
                ToastUtils.showShort("暂无数据");
                return;
            }
            intent = new Intent(this, DataDetailsActivity.class);
            intent.putExtra("ProjectBean", projectBean);
            intent.putExtra("ZoneBean", zoneBean);
            intent.putExtra("index", 1);
            startActivity(intent);
        } else if (view.getId() == binding.btnCorrosion.getId()) {
            if (!checkInfo()) {
                return;
            }
            if (TextUtils.isEmpty(SPUtils.getInstance().getString(StaticConstant.SP_CORROSION_BLE_NAME))) {
                ToastUtils.showShort("请先进行蓝牙设备连接");
               return;
            }
            String strCalibrationStandardList = SPUtils.getInstance().getString("calibration-standard-list");
            String strCalibrationList = SPUtils.getInstance().getString("calibration-list");
            if (TextUtils.isEmpty(strCalibrationStandardList) || TextUtils.isEmpty(strCalibrationList)) {
                ToastUtils.showShort("请先在系统设置里校准设备主机");
               return;
            }
            dotClick();
            if (TextUtils.equals(StaticConstant.APP_TYPE_DEFAULT, StaticConstant.APP_CORROSION)) {
                intent = new Intent(this, CorrosionDetectActivity2.class);
                intent.putExtra("ZoneBean", zoneBean); // putExtra 是干嘛用的
                startActivity(intent);
            } else {
                intent = new Intent(this, DetectActivity.class);
                intent.putExtra("ZoneBean", zoneBean);
                startActivity(intent);
            }
        } else if (view.getId() == binding.tvCorrosionSave.getId()) {
            if (TextUtils.equals(StaticConstant.APP_TYPE_DEFAULT, StaticConstant.APP_CORROSION)) {
                if (zoneBean == null || zoneBean.corrosionZoneBean == null || TextUtils.isEmpty(zoneBean.corrosionZoneBean.strZoneList)) {
                    ToastUtils.showShort("暂无数据");
                    return;
                }
                intent = new Intent(this, DataDetailsActivity.class);
                intent.putExtra("ProjectBean", projectBean);
                intent.putExtra("ZoneBean", zoneBean);
                intent.putExtra("index", 2);
                startActivity(intent);
            } else {
                if (zoneBean.thicknessCount == 0) {
                    ToastUtils.showShort("暂无数据");
                    return;
                }
                intent = new Intent(this, DataDetailsActivity.class);
                intent.putExtra("ProjectBean", projectBean);
                intent.putExtra("ZoneBean", zoneBean);
                intent.putExtra("index", 2);
                startActivity(intent);
            }
        } else if (view.getId() == binding.btnReturn.getId()) {
            if (zoneBean == null || (zoneBean.f230ZoneBean == null && zoneBean.rebounderZoneBean == null && zoneBean.corrosionZoneBean == null)) {
                finish();
            } else {
                if ((zoneBean.f230ZoneBean == null || zoneBean.f230ZoneBean.zoneCount == 0) && (zoneBean.rebounderZoneBean == null || zoneBean.rebounderZoneBean.zoneCount == 0) && (zoneBean.corrosionZoneBean == null || zoneBean.corrosionZoneBean.zoneCount == 0)) {
                    GreenDaoHelper.getDaoSession(this).getZoneBeanDao().delete(zoneBean);
                }
                finish();
            }
        }
    }

    private boolean checkInfo() {
        int projectPosition = binding.spProject.getSelectedItemPosition();
        String num = binding.etNumber.getText().toString().trim();
        String remark = binding.etRemark.getText().toString().trim();
        if (projectBean == null || projectPosition < 0 || TextUtils.isEmpty(num) || TextUtils.isEmpty(remark)) {
            ToastUtils.showShort("请填写完整");
            return false;
        }
        if (zoneBean == null) {List<ZoneBean> zoneBeanList = GreenDaoHelper.getDaoSession(this).getZoneBeanDao().queryBuilder().where(ZoneBeanDao.Properties.ProjectName.eq(projectBean.name), ZoneBeanDao.Properties.Number.eq(num)).list();


            if (!zoneBeanList.isEmpty()) {
                ToastUtils.showShort("构件编号不可重复");
                return false;
            }
            zoneBean = new ZoneBean();
            zoneBean.projectId = projectBean.id;
            zoneBean.projectName = projectBean.name;
            zoneBean.number = num;
            zoneBean.remark = remark;
        }
        return true;
    }

    /**
 * 禁用界面交互元素
 * 该方法用于在特定操作期间禁用界面上的可交互组件，防止用户进行重复操作
 * 包括禁用下拉框触摸、移除按钮点击监听器、将按钮置为灰色禁用状态、以及禁用输入框焦点
 */
private void dotClick() {
        binding.spProject.setOnTouchListener((v, event) -> true);
        binding.tvNew.setOnClickListener(null);
        binding.ivProject.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_pull_down_gray_right));
        binding.tvNew.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button_gray));
        binding.tvJia.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button_gray));
        binding.tvJian.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button_gray));
        binding.tvJia.setOnClickListener(null);
        binding.tvJian.setOnClickListener(null);
        binding.etNumber.setFocusable(false);
        binding.etRemark.setFocusable(false);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (zoneBean == null) {
            return;
        }
        List<ZoneBean> zoneBeanList = GreenDaoHelper.getDaoSession(this).getZoneBeanDao().queryBuilder().where(ZoneBeanDao.Properties.ProjectId.eq(zoneBean.projectId), ZoneBeanDao.Properties.Number.eq(zoneBean.number)).list();
        if (zoneBeanList.isEmpty()) {
            return;
        }
        zoneBean = zoneBeanList.get(0);

        binding.tvF230Num.setText("测点数：" + (zoneBean.f230ZoneBean == null ? 0 : zoneBean.f230ZoneBean.zoneCount));
        binding.tvReboundNum.setText("测点数：" + (zoneBean.rebounderZoneBean == null ? 0 : zoneBean.rebounderZoneBean.zoneCount));
        boolean isCorrosion = TextUtils.equals(StaticConstant.APP_TYPE_DEFAULT, StaticConstant.APP_CORROSION);
        if (isCorrosion) {
            binding.tvCorrosionNum.setText("测点数：" + (zoneBean.corrosionZoneBean == null ? 0 : zoneBean.corrosionZoneBean.zoneCount));
        } else {
            binding.tvCorrosionNum.setText("测点数：" + zoneBean.thicknessCount);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK == resultCode) {
            setProjectList(true);
        }
    }

    private void setProjectList(boolean isLast) {
        List<ProjectBean> projectBeanList = GreenDaoHelper.getDaoSession(this).getProjectBeanDao().loadAll();
        List<String> projectList = new ArrayList<>();
        for (ProjectBean bean : projectBeanList) {
            projectList.add(bean.name);
        }
        SpinnerUtils.attachDataSource(binding.spProject, projectList, (spinner, text, position) -> {
            projectBean = projectBeanList.get(position);
            binding.etAddress.setText(projectBeanList.get(position).getAddress());
        });
        if (isLast) {
            binding.spProject.setDefaultContent(projectList.get(projectList.size() - 1));  //工程名称
        } else {
            ZoneBean lastZoneBean = GreenDaoHelper.getDaoSession(this).getZoneBeanDao().queryBuilder().orderDesc(ZoneBeanDao.Properties.Id).limit(1).unique();
            if (lastZoneBean != null) {
                if (!projectList.isEmpty() && !TextUtils.isEmpty(lastZoneBean.projectName) && projectList.contains(lastZoneBean.projectName)) {
                    binding.spProject.setDefaultContent(lastZoneBean.projectName);
                }
                binding.etNumber.setText((Integer.parseInt(lastZoneBean.number) + 1) + "");
                binding.etRemark.setText(lastZoneBean.remark);
            }
        }
    }
}

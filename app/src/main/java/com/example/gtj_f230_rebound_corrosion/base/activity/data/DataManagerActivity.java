package com.example.gtj_f230_rebound_corrosion.base.activity.data;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blackhao.utillibrary.usbHelper.USBBroadCastReceiver;
import com.blackhao.utillibrary.usbHelper.UsbHelper;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.base.activity.BaseActivity;
import com.example.gtj_f230_rebound_corrosion.base.adapter.DataProjectListAdapter;
import com.example.gtj_f230_rebound_corrosion.base.adapter.ZoneListAdapter;
import com.example.gtj_f230_rebound_corrosion.base.model.ProjectBean;
import com.example.gtj_f230_rebound_corrosion.base.model.ZoneBean;
import com.example.gtj_f230_rebound_corrosion.base.storage.GreenDaoHelper;
import com.example.gtj_f230_rebound_corrosion.base.utils.DeleteFileUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.OtherUtils;
import com.example.gtj_f230_rebound_corrosion.base.view.StyleAlertDialog;
import com.example.gtj_f230_rebound_corrosion.databinding.ActDataManagerBinding;
import com.example.gtj_f230_rebound_corrosion.functions.database.greenDao.db.ZoneBeanDao;
import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.google.gson.Gson;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 数据管理
 */
@SuppressLint("SetTextI18n")
public class DataManagerActivity extends BaseActivity<ActDataManagerBinding> {
    private DataProjectListAdapter projectListAdapter;
    private ProjectBean projectBean;
    private ZoneListAdapter zoneListAdapter;
    private ZoneBean zoneBean;
    private final Set<Long> treatZoneIdSet = new HashSet<>();

    @Override
    protected ActDataManagerBinding getBinding() {
        return ActDataManagerBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initLabel() {
        initBatteryReceiver(binding.navigationBar);
        initDeviceStatusExecutor(binding.navigationBar);

        binding.navigationBar.setTitleText("数据管理");
    }

    @Override
    protected void initView() {
        initUsbHelper();
        setProjectRecyclerView();
        setZoneRecyclerView();
        queryPageListProject();

        binding.btnOpen.setOnClickListener(this::onViewClicked);
        binding.btnUpload.setOnClickListener(this::onViewClicked);
        binding.btnExport.setOnClickListener(this::onViewClicked);
        binding.btnDelete.setOnClickListener(this::onViewClicked);
        binding.btnReturn.setOnClickListener(this::onViewClicked);
    }

    private int projectShowIndex;

    @SuppressLint("NotifyDataSetChanged")
    private void setProjectRecyclerView() {
        binding.recyclerViewProject.setLayoutManager(new LinearLayoutManager(this));
        projectListAdapter = new DataProjectListAdapter(this);
        binding.recyclerViewProject.setAdapter(projectListAdapter);
        projectListAdapter.addOnItemChildClickListener(R.id.textView, (adapter, view, position) -> {
            projectBean = projectListAdapter.getItem(position);
            projectShowIndex = position;
            for (int i = 0; i < projectListAdapter.getItems().size(); i++) {
                projectListAdapter.getItems().get(i).isSelected_open = false;
            }
            projectListAdapter.getItems().get(position).isSelected_open = true;
            projectListAdapter.notifyDataSetChanged();
            //搜索zoneList
            queryShowZoneList();
            //清空detail
            zoneBean = null;
            refreshTextView(false);
        });
        projectListAdapter.addOnItemChildClickListener(R.id.imageView, (adapter, view, position) -> {
            boolean flag = !projectListAdapter.getItems().get(position).isSelected_treat;
            projectListAdapter.getItems().get(position).isSelected_treat = flag;
            projectListAdapter.notifyDataSetChanged();
            queryTreatZoneList(projectListAdapter.getItems().get(position));
            if (!zoneListAdapter.getItems().isEmpty()) {
                if (zoneListAdapter.getItems().get(0).projectId == projectListAdapter.getItems().get(position).id) {
                    for (ZoneBean bean : zoneListAdapter.getItems()) {
                        bean.isSelected_treat = flag;
                    }
                }
                zoneListAdapter.notifyDataSetChanged();
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setZoneRecyclerView() {
        binding.recyclerViewZone.setLayoutManager(new LinearLayoutManager(this));
        zoneListAdapter = new ZoneListAdapter(this);
        binding.recyclerViewZone.setAdapter(zoneListAdapter);
        zoneListAdapter.addOnItemChildClickListener(R.id.textView, (adapter, view, position) -> {
            for (int i = 0; i < zoneListAdapter.getItems().size(); i++) {
                zoneListAdapter.getItems().get(i).isSelected_open = false;
            }
            zoneListAdapter.getItems().get(position).isSelected_open = true;
            zoneListAdapter.notifyDataSetChanged();
            zoneBean = zoneListAdapter.getItem(position);
            //显示detail
            refreshTextView(true);
        });
        zoneListAdapter.addOnItemChildClickListener(R.id.imageView, (adapter, view, position) -> {
            boolean flag = !zoneListAdapter.getItems().get(position).isSelected_treat;
            zoneListAdapter.getItems().get(position).isSelected_treat = flag;
            zoneListAdapter.notifyDataSetChanged();
            if (flag) {
                treatZoneIdSet.add(zoneListAdapter.getItems().get(position).id);
            } else {
                treatZoneIdSet.remove(zoneListAdapter.getItems().get(position).id);
            }
            boolean isUnSelected = false;
            for (ZoneBean bean : zoneListAdapter.getItems()) {
                if (!bean.isSelected_treat) {
                    isUnSelected = true;
                    break;
                }
            }
            projectListAdapter.getItems().get(projectShowIndex).isSelected_treat = !isUnSelected;
            projectListAdapter.notifyDataSetChanged();
        });
    }

    public void queryPageListProject() {
        List<ProjectBean> projectBeanList = GreenDaoHelper.getDaoSession(this).getProjectBeanDao().loadAll();
        for (ProjectBean bean : projectBeanList) {
            bean.isSelected_open = false;
            bean.isSelected_treat = false;
        }
        projectListAdapter.submitList(projectBeanList);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void queryShowZoneList() {
        if (projectBean == null) {
            zoneListAdapter.getItems().clear();
            zoneListAdapter.notifyDataSetChanged();
            return;
        }
        List<ZoneBean> zoneBeanList = GreenDaoHelper.getDaoSession(this).getZoneBeanDao().queryBuilder().where(ZoneBeanDao.Properties.ProjectId.eq(projectBean.id)).list();
        for (ZoneBean bean : zoneBeanList) {
            bean.isSelected_open = false;
            bean.isSelected_treat = false;
        }
        for (ZoneBean bean : zoneBeanList) {
            for (Long aLong : treatZoneIdSet) {
                if (bean.id.equals(aLong)) {
                    bean.isSelected_treat = true;
                    break;
                }
            }
        }
        zoneListAdapter.submitList(zoneBeanList);
    }

    private void refreshTextView(boolean flagShow) {
        if (flagShow) {
            StringBuilder sbd = getStringBuilder();
            binding.tvDetail.setText(sbd);
        } else {
            binding.tvDetail.setText("");
        }
    }

    @NonNull
    private StringBuilder getStringBuilder() {
        StringBuilder sbd = new StringBuilder();
        sbd.append("工程名称：").append(projectBean.name).append("\n").append("工程地址：").append(projectBean.address).append("\n构件编号：").append(zoneBean.number).append("\n构件备注：").append(zoneBean.remark).append("\n缝宽测点：").append(zoneBean.f230ZoneBean == null ? 0 : zoneBean.f230ZoneBean.zoneCount).append("\n回弹测点：").append(zoneBean.rebounderZoneBean == null ? 0 : zoneBean.rebounderZoneBean.zoneCount);
        if (TextUtils.equals(StaticConstant.APP_TYPE_DEFAULT, StaticConstant.APP_CORROSION)) {
            sbd.append("\n锈蚀测点：").append(zoneBean.corrosionZoneBean == null ? 0 : zoneBean.corrosionZoneBean.zoneCount);
        } else {
            sbd.append("\n保护层厚度测点：").append(zoneBean.thicknessCount);
        }
        return sbd;
    }

    public void queryTreatZoneList(ProjectBean projectBean) {
        List<ZoneBean> zoneBeanList = GreenDaoHelper.getDaoSession(this).getZoneBeanDao().queryBuilder().where(ZoneBeanDao.Properties.ProjectId.eq(projectBean.id + "")).list();
        boolean flag = projectBean.isSelected_treat;
        for (ZoneBean bean : zoneBeanList) {
            if (flag) {
                treatZoneIdSet.add(bean.id);
            } else {
                treatZoneIdSet.remove(bean.id);
            }
        }
    }

    public void onViewClicked(View view) {
        OtherUtils.hideKeyboard(this);
        if (view.getId() == R.id.btn_open) {
            if (zoneBean == null) {
                ToastUtils.showShort("请选择一个项目+");
                return;
            }
            Intent intent = new Intent(this, DataDetailsActivity.class);
            intent.putExtra("ProjectBean", projectBean);
            intent.putExtra("ZoneBean", zoneBean);
            startActivity(intent);
        } else if (view.getId() == R.id.btn_upload) {
            ToastUtils.showShort("暂无上传功能");
        } else if (view.getId() == R.id.btn_export) {
            if (treatZoneIdSet.isEmpty()) {
                ToastUtils.showShort("暂无可导出的数据");
                return;
            }
            if (StaticConstant.usbHelper.rootFolder == null) {
                ToastUtils.showShort("未识别到U盘");
            }
            showExportDialog();
        } else if (view.getId() == R.id.btn_delete) {
            int projectCount = 0;
            for (int i = projectListAdapter.getItems().size() - 1; i >= 0; i--) {
                if (projectListAdapter.getItems().get(i).isSelected_treat) {
                    projectCount++;
                }
            }
            if (projectCount > 0 || !treatZoneIdSet.isEmpty()) {
                StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this);
                styleAlertDialog.setContent("确定删除勾选项？").setLeftButton("取消", v -> styleAlertDialog.dismiss()).setRightButton("确定", v -> {
                    styleAlertDialog.dismiss();
                    removeTreatZoneList();
                }).show();
            } else {
                ToastUtils.showShort("请勾选删除项");
            }
        } else if (view.getId() == R.id.btn_return) {
            finish();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void removeTreatZoneList() {
        //删除
        for (Long aLong : treatZoneIdSet) {
            List<ZoneBean> list = GreenDaoHelper.getDaoSession(this).getZoneBeanDao().queryBuilder().where(ZoneBeanDao.Properties.Id.eq(aLong)).list();
            if (!list.isEmpty() && list.get(0).f230ZoneBean != null && list.get(0).f230ZoneBean.zoneCount > 0) {
                //删除图片缓存
                String strDir = StaticConstant.dataFilePathCache + "image_native/" + list.get(0).number + "/";
                File fileDir = new File(strDir);
                if (fileDir.exists()) {
                    DeleteFileUtils.deleteDirectory(strDir);
                }
            }
            if (zoneBean != null && aLong.equals(zoneBean.id)) {
                zoneBean = null;
                refreshTextView(false);
            }
        }
        //ZoneBean数据库删除
        GreenDaoHelper.getDaoSession(this).getZoneBeanDao().deleteByKeyInTx(treatZoneIdSet);
        //zoneListAdapter删除
        for (int i = zoneListAdapter.getItems().size() - 1; i >= 0; i--) {
            for (Long aLong : treatZoneIdSet) {
                if (aLong.equals(zoneListAdapter.getItems().get(i).id)) {
                    zoneListAdapter.getItems().remove(i);
                    break;
                }
            }
        }
        zoneListAdapter.notifyDataSetChanged();
        treatZoneIdSet.clear();
        //projectListAdapter删除
        for (int i = projectListAdapter.getItems().size() - 1; i >= 0; i--) {
            if (projectListAdapter.getItems().get(i).isSelected_treat) {
                GreenDaoHelper.getDaoSession(this).getProjectBeanDao().deleteByKey(projectListAdapter.getItems().get(i).id);
                projectListAdapter.getItems().remove(i);
            }
        }
        projectListAdapter.notifyDataSetChanged();
        if (projectListAdapter.getItems().isEmpty()) {
            SPUtils.getInstance().put("last-config-ZoneBean", "");
        }
    }

    private void showExportDialog() {
        StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this);
        styleAlertDialog.setContent("确定导出数据？").setLeftButton("取消", v -> styleAlertDialog.dismiss()).setRightButton("确定", v -> {
            styleAlertDialog.dismiss();
            Intent intent;
            if (TextUtils.equals(StaticConstant.APP_TYPE_DEFAULT, StaticConstant.APP_CORROSION)) {
                intent = new Intent(this, DataExportDataActivity.class);
            } else {
                intent = new Intent(this, DataExportThicknessActivity.class);
            }
            intent.putExtra("treatZoneIdSet", new Gson().toJson(treatZoneIdSet));
            startActivity(intent);
        }).show();
    }

    //---------------------USB--------------------------
    private void initUsbHelper() {
        //U盘只能识别Pad格式化的U盘或格式化选FAT32的文件系统
        StaticConstant.usbHelper = new UsbHelper(this, new USBBroadCastReceiver.UsbListener() {
            @Override
            public void insertUsb(UsbDevice device_add) {
                ToastUtils.showShort("U盘插入");
                initCurrentFolder();
            }

            @Override
            public void removeUsb(UsbDevice device_remove) {
                ToastUtils.showShort("U盘被拔出");
                StaticConstant.usbHelper.rootFolder = null;
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
            UsbMassStorageDevice[] usbMassStorageDevices = StaticConstant.usbHelper.getDeviceList();
            if (usbMassStorageDevices.length > 0) {
                if (StaticConstant.usbHelper.rootFolder == null) {
                    StaticConstant.usbHelper.readDevice(usbMassStorageDevices[0]);
                }
                if (StaticConstant.usbHelper.rootFolder != null) {
                    ToastUtils.showShort("U盘识别成功");
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
        StaticConstant.usbHelper.finishUsbHelper();
    }
}

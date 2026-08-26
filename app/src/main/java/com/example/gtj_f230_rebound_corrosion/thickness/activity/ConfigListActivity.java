package com.example.gtj_f230_rebound_corrosion.thickness.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.base.activity.BaseActivity;
import com.example.gtj_f230_rebound_corrosion.base.storage.GreenDaoHelper;
import com.example.gtj_f230_rebound_corrosion.databinding.ActConfigListBinding;
import com.example.gtj_f230_rebound_corrosion.functions.database.greenDao.db.ConfigBeanDao;
import com.example.gtj_f230_rebound_corrosion.thickness.adapter.ConfigListAdapter;
import com.example.gtj_f230_rebound_corrosion.thickness.adapter.ConfigTypeListAdapter;
import com.example.gtj_f230_rebound_corrosion.thickness.model.ConfigBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置列表
 */
@SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
public class ConfigListActivity extends BaseActivity<ActConfigListBinding> {
    private ConfigTypeListAdapter typeAdapter;
    private ConfigListAdapter configAdapter;
    private ConfigBean configBean;

    @Override
    protected ActConfigListBinding getBinding() {
        return ActConfigListBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initLabel() {
        binding.navigationBar.setTitleText("参数配置列表");
        initBatteryReceiver(binding.navigationBar);
        initDeviceStatusExecutor(binding.navigationBar);
    }

    @Override
    protected void initView() {
        configBean = (ConfigBean) getIntent().getSerializableExtra("configBean");

        binding.btnOk.setOnClickListener(this::onViewClicked);
        binding.btnReturn.setOnClickListener(this::onViewClicked);

        initType();
        initConfigView();
    }

    private void onViewClicked(View view) {
        if (view.getId() == R.id.btn_ok) {
            if (configBean == null) {
                ToastUtils.showShort("请选择电杆配置");
                return;
            }
            setResult(RESULT_OK, new Intent().putExtra("configBean", configBean));
            finish();
        } else {
            finish();
        }
    }

    private void initType() {
        ArrayList<ConfigBean> typeList = new ArrayList<>();
        typeList.add(new ConfigBean(StaticConstant.poleType_PT, configBean != null && configBean.poleType.equals(StaticConstant.poleType_PT), false));
        typeList.add(new ConfigBean(StaticConstant.poleType_Y, configBean != null && configBean.poleType.equals(StaticConstant.poleType_Y), false));
        typeList.add(new ConfigBean(StaticConstant.poleType_BY, configBean != null && configBean.poleType.equals(StaticConstant.poleType_BY), false));
        binding.rvType.setLayoutManager(new LinearLayoutManager(this));
        typeAdapter = new ConfigTypeListAdapter(this, false);
        binding.rvType.setAdapter(typeAdapter);
        typeAdapter.addOnItemChildClickListener(R.id.textView, (adapter, view, position) -> {
            for (int i = 0; i < adapter.getItems().size(); i++) {
                typeAdapter.getItems().get(i).isSelected = false;
            }
            ConfigBean bean = typeAdapter.getItems().get(position);
            bean.isSelected = true;
            typeAdapter.notifyDataSetChanged();

            List<ConfigBean> configBeans = GreenDaoHelper.getDaoSession(this).getConfigBeanDao().queryBuilder().where(ConfigBeanDao.Properties.PoleType.eq(bean.poleType)).list();
            if (!configBeans.isEmpty()) {
                for (int i = 0; i < configBeans.size(); i++) {
                    if (configBean != null && configBean.poleName.equals(configBeans.get(i).poleName)) {
                        configBeans.get(i).isSelected = true;
                        refreshTextView();
                    } else {
                        configBeans.get(i).isSelected = false;
                    }
                }
            }
            configAdapter.submitList(configBeans);
        });
        typeAdapter.submitList(typeList);
    }

    private void initConfigView() {
        binding.rvConfig.setLayoutManager(new LinearLayoutManager(this));
        configAdapter = new ConfigListAdapter(this, false);
        binding.rvConfig.setAdapter(configAdapter);
        configAdapter.addOnItemChildClickListener(R.id.textView, (adapter, view, position) -> {
            for (int i = 0; i < configAdapter.getItems().size(); i++) {
                configAdapter.getItems().get(i).isSelected = false;
            }
            configBean = configAdapter.getItems().get(position);
            configAdapter.getItems().get(position).isSelected = true;
            adapter.notifyDataSetChanged();

            refreshTextView();
        });
        if (configBean == null) {
            return;
        }
        List<ConfigBean> configBeanList = GreenDaoHelper.getDaoSession(this).getConfigBeanDao().queryBuilder().where(ConfigBeanDao.Properties.PoleType.eq(configBean.poleType)).list();
        if (!configBeanList.isEmpty()) {
            for (int i = 0; i < configBeanList.size(); i++) {
                if (configBean != null && configBean.poleName.equals(configBeanList.get(i).poleName)) {
                    configBeanList.get(i).isSelected = true;
                    refreshTextView();
                } else {
                    configBeanList.get(i).isSelected = false;
                }
            }
        }
        configAdapter.submitList(configBeanList);
    }

    private void refreshTextView() {
        if (configBean == null) {
            binding.tvDetail.setText("");
            return;
        }
        StringBuilder sbd = new StringBuilder();
        sbd.append("配 置 名 称 ：").append(configBean.poleName)
                .append("\n电 杆 类 型 ：").append(configBean.poleType)
                .append("\n电 杆 结 构 ：").append(StaticConstant.configpoleStructureList.get(configBean.poleStructure))
                .append("\n电 杆 长 度 ：").append(configBean.poleHeight).append("m")
                .append("\n电 杆 根 径 ：").append(configBean.poleBottomDiameter).append("mm")
                .append("\n电 杆 梢 径 ：").append(configBean.poleTopDiameter).append("mm")
                .append("\n纵 筋 直 径 ：").append(configBean.rebarDiameter).append("mm")
                .append("\n螺旋筋直径：").append(configBean.hoopingDiameter).append("mm")
                .append("\n设计保护层厚度：").append(configBean.designThickness).append("（+8 -2）mm");
        binding.tvDetail.setText(sbd);
    }
}

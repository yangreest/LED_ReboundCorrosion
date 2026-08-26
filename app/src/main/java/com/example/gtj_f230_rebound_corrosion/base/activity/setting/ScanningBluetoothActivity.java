package com.example.gtj_f230_rebound_corrosion.base.activity.setting;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.base.activity.BaseActivity;
import com.example.gtj_f230_rebound_corrosion.base.adapter.BluetoothDeviceListAdapter;
import com.example.gtj_f230_rebound_corrosion.base.utils.PermissionUtils;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.BluetoothDeviceBean;
import com.example.gtj_f230_rebound_corrosion.databinding.ActScanningBluetoothBinding;
import com.inuker.bluetooth.library.BluetoothManager;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("SetTextI18n")
public class ScanningBluetoothActivity extends BaseActivity<ActScanningBluetoothBinding> {
    private final List<SearchResult> mDevices = new ArrayList<>();
    private BluetoothDeviceListAdapter deviceListAdapter;
    public static String INTENT_TYPE = "type";
    public static String SCAN_REBOUND = "scanRebound";
    public static String SCAN_CORROSION = "scanCorrosion";

    private String type = "";
    private String bleName = "";
    private String bleMac = "";

    @Override
    protected ActScanningBluetoothBinding getBinding() {
        return ActScanningBluetoothBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initLabel() {
        initBatteryReceiver(binding.navigationBar);
        initDeviceStatusExecutor(binding.navigationBar);

        binding.navigationBar.setTitleText("连接设备主机");
    }

    @Override
    protected void initView() {
        initData();
    }

    protected void initData() {
        type = getIntent().getStringExtra(INTENT_TYPE);
        binding.tvContent.setText(getDeviceName());
        binding.btnScanning.setOnClickListener(this::onViewClicked);
        binding.btnReturn.setOnClickListener(this::onViewClicked);
        initRecyclerView();
        if (type.equals(SCAN_CORROSION)) {
            BluetoothManager.unConnect(SPUtils.getInstance().getString(StaticConstant.SP_CORROSION_BLE_MAC));
        } else {
            BluetoothManager.unConnect(SPUtils.getInstance().getString(StaticConstant.SP_REBOUND_BLE_MAC));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!deviceListAdapter.getItems().isEmpty()) {
            return;
        }
        PermissionUtils.initPermission(this, this::scanningBlueboothDevice);
    }

    public void onViewClicked(View view) {
        if (view.getId() == R.id.btn_scanning) {
            PermissionUtils.initPermission(this, this::scanningBlueboothDevice);
        } else if (view.getId() == R.id.btn_return) {
            finish();
        }
    }

    private void scanningBlueboothDevice() {
        BluetoothManager.searchDeviceDevice(new SearchResponse() {
            @Override
            public void onSearchStarted() {
                progressiveDialog.setTvContent("加载中...").show();
                //清空列表
                mDevices.clear();
            }

            @Override
            public void onDeviceFounded(SearchResult device) {
                String deviceName = device.getName().toUpperCase();
                Logger.d("onDeviceFounded: " + deviceName + " ~ " + device.getAddress());
                if (TextUtils.equals("NULL", deviceName)) {
                    return;
                }
                if (type.equals(SCAN_CORROSION)) {
                    if (!mDevices.contains(device)) {
                        mDevices.add(device);
                    }
                } else {
                    if (!mDevices.contains(device)) {
                        mDevices.add(device);
                    }
                }
            }

            @Override
            public void onSearchStopped() {
                List<BluetoothDeviceBean> tempList = new ArrayList<>();
                for (int i = 0; i < mDevices.size(); i++) {
                    tempList.add(new BluetoothDeviceBean(mDevices.get(i).getAddress(), mDevices.get(i).getName()));
                }
                deviceListAdapter.submitList(tempList);
                progressiveDialog.dismiss();
                for (int i = 0; i < tempList.size(); i++) {
                    if (TextUtils.equals(tempList.get(i).mac, bleMac)) {
                        connectDevice(i, tempList.get(i));
                        break;
                    }
                }
            }

            @Override
            public void onSearchCanceled() {
                progressiveDialog.dismiss();
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void connectDevice(final int position, final BluetoothDeviceBean deviceBean) {
        progressiveDialog.setTvContent("连接中...").show();
        BluetoothManager.connectBluetoothDevice(deviceBean.mac, flag -> {
            progressiveDialog.dismiss();
            if (flag) {
                for (int i = 0; i < deviceListAdapter.getItems().size(); i++) {
                    deviceListAdapter.getItems().get(i).isSelected = false;
                }
                deviceListAdapter.getItems().get(position).isSelected = true;
                deviceListAdapter.notifyDataSetChanged();
                if (type.equals(SCAN_CORROSION)) {
                    SPUtils.getInstance().put(StaticConstant.SP_CORROSION_BLE_MAC, deviceBean.mac);
                    SPUtils.getInstance().put(StaticConstant.SP_CORROSION_BLE_NAME, deviceBean.name);
                } else {
                    SPUtils.getInstance().put(StaticConstant.SP_REBOUND_BLE_MAC, deviceBean.mac);
                    SPUtils.getInstance().put(StaticConstant.SP_REBOUND_BLE_NAME, deviceBean.name);
                }
                getDeviceName();
                ToastUtils.showShort("连接成功");
            }
        });
    }

    private void initRecyclerView() {
        binding.mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        deviceListAdapter = new BluetoothDeviceListAdapter();
        binding.mRecyclerView.setAdapter(deviceListAdapter);
        deviceListAdapter.setOnItemClickListener((adapter, view, position) -> connectDevice(position, ((BluetoothDeviceListAdapter) adapter).getItems().get(position)));
    }

    private String getDeviceName() {
        StringBuilder sbf = new StringBuilder();
        bleName = type.equals(SCAN_CORROSION) ? SPUtils.getInstance().getString(StaticConstant.SP_CORROSION_BLE_NAME) : SPUtils.getInstance().getString(StaticConstant.SP_REBOUND_BLE_NAME);
        bleMac = type.equals(SCAN_CORROSION) ? SPUtils.getInstance().getString(StaticConstant.SP_CORROSION_BLE_MAC) : SPUtils.getInstance().getString(StaticConstant.SP_REBOUND_BLE_MAC);
        if (!TextUtils.isEmpty(bleMac)) {
            sbf.append(bleName).append("（").append(bleMac).append("）");
        }
        return sbf.toString();
    }
}

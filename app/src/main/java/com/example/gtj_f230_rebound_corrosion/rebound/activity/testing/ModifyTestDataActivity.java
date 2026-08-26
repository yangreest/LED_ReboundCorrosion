package com.example.gtj_f230_rebound_corrosion.rebound.activity.testing;

import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_f230_rebound_corrosion.base.activity.BaseActivity;
import com.example.gtj_f230_rebound_corrosion.base.utils.StringUtils;
import com.example.gtj_f230_rebound_corrosion.databinding.ActModifyTestDataBinding;
import com.example.gtj_f230_rebound_corrosion.rebound.model.RebounderBean;
import com.example.gtj_f230_rebound_corrosion.base.model.RebounderZoneBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class ModifyTestDataActivity extends BaseActivity<ActModifyTestDataBinding> {

    private RebounderZoneBean rebounderZoneBean;
    private int index;
    private List<RebounderBean> testingList;

    @Override
    protected ActModifyTestDataBinding getBinding() {
        return ActModifyTestDataBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initLabel() {
        initBatteryReceiver(binding.navigationBar);
        initDeviceStatusExecutor(binding.navigationBar);
        
        binding.navigationBar.setTitleText("编辑数据");
    }

    @Override
    protected void initView() {
        binding.editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                StringUtils.stringFormatFilter(editable, binding.textView);
            }
        });

        initData();
        binding.btnConfirm.setOnClickListener(v -> {
            String[] array = binding.textView.getText().toString().split("　");
            if (array.length < 16) {
                ToastUtils.showShort("请输入完整16个数据");
                return;
            }
            testingList.get(index).testNumber.clear();
            for (String s : array) {
                testingList.get(index).testNumber.add(Integer.parseInt(s.trim()));
            }
            testingList.get(index).calculate();
            rebounderZoneBean.strZoneList = new Gson().toJson(testingList);
            Intent intent = new Intent();
            intent.putExtra("ProjectItemPointBean", rebounderZoneBean);
            setResult(RESULT_OK, intent);
            finish();
        });
        binding.btnReturn.setOnClickListener(v -> finish());
    }

    private void initData() {
        rebounderZoneBean = (RebounderZoneBean) getIntent().getSerializableExtra("ProjectItemPointBean");
        index = getIntent().getIntExtra("index", 0);
        if (!TextUtils.isEmpty(rebounderZoneBean.strZoneList)) {
            testingList = new Gson().fromJson(rebounderZoneBean.strZoneList, new TypeToken<List<RebounderBean>>() {
            }.getType());
        }
    }
}

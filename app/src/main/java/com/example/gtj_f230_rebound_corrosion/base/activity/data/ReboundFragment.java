package com.example.gtj_f230_rebound_corrosion.base.activity.data;

import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.base.adapter.ReboundSerialNumAdapter;
import com.example.gtj_f230_rebound_corrosion.base.model.RebounderZoneBean;
import com.example.gtj_f230_rebound_corrosion.base.model.ZoneBean;
import com.example.gtj_f230_rebound_corrosion.base.storage.GreenDaoHelper;
import com.example.gtj_f230_rebound_corrosion.base.utils.OptionsPickerViewUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.StringUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.ViewUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.rectify.RebounderUtils;
import com.example.gtj_f230_rebound_corrosion.databinding.FragmentRebounderResultBinding;
import com.example.gtj_f230_rebound_corrosion.rebound.activity.testing.ModifyTestDataActivity;
import com.example.gtj_f230_rebound_corrosion.rebound.adapter.TestingListAdapter2;
import com.example.gtj_f230_rebound_corrosion.rebound.model.RebounderBean;
import com.example.gtj_f230_rebound_corrosion.rebound.model.RebounderConfigBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReboundFragment extends Fragment {

    private FragmentRebounderResultBinding binding;
    private RebounderZoneBean rebounderZoneBean;
    private List<RebounderBean> testingList = new ArrayList<>();
    private TestingListAdapter2 testingListAdapter2;
    private boolean isRefresh;
    private int currentColor;
    private int selectedItem = 0;
    private RebounderConfigBean rebounderConfigBean;
    private RebounderConfigBean.RConfigBean rConfigBean;
    private ZoneBean zoneBean;
    private ReboundSerialNumAdapter serialNumAdapter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        zoneBean = ((DataDetailsActivity) context).getZoneBean();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRebounderResultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (zoneBean == null || zoneBean.rebounderZoneBean == null) {
            binding.tvNull.setVisibility(VISIBLE);
            return;
        }

        currentColor = ContextCompat.getColor(getContext(), R.color.color4B92F8);

        binding.tvNull.setVisibility(GONE);

        initData();

        binding.tvPouringSurface.setOnClickListener(this::onViewClicked);
        binding.tvSurface.setOnClickListener(this::onViewClicked);
        binding.tvAngle.setOnClickListener(this::onViewClicked);
        binding.tvIsPumping.setOnClickListener(this::onViewClicked);
        binding.tvStandard.setOnClickListener(this::onViewClicked);
        binding.tvCarbonization.setOnClickListener(this::onViewClicked);
    }

    private void initData() {
        rebounderZoneBean = zoneBean.rebounderZoneBean;
        setRecyclerView();
        setTestingConfig();
        setTextViewData();
    }

    private void setRecyclerView() {
        binding.rvSerialNum.setLayoutManager(new LinearLayoutManager(getContext()));
        serialNumAdapter = new ReboundSerialNumAdapter(getContext());
        binding.rvSerialNum.setAdapter(serialNumAdapter);
        serialNumAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (selectedItem == position) {
                return;
            }
            testingList.get(selectedItem).isSelected = false;
            testingList.get(position).isSelected = true;
            selectedItem = position;
            serialNumAdapter.notifyDataSetChanged();
            updateRvData();
        });

        binding.mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        testingListAdapter2 = new TestingListAdapter2();
        binding.mRecyclerView.setAdapter(testingListAdapter2);
        testingListAdapter2.setOnItemLongClickListener((adapter, view, position) -> {
            Intent intent = new Intent(getContext(), ModifyTestDataActivity.class);
            intent.putExtra("index", selectedItem);
            intent.putExtra("ProjectItemPointBean", rebounderZoneBean);
            startActivityForResult(intent, 1);
            return true;
        });
    }

    private void updateRvData() {
        binding.tvAreaNumber.setText("当前测区：" + (selectedItem + 1) + "/" + testingList.size());
        RebounderBean rebounderBean = testingList.get(selectedItem);
        binding.tvTitle1.setText("平均值: " + rebounderBean.average);
        binding.tvTitle2.setText("角度修正: " + rebounderBean.calAngle);
        binding.tvTitle3.setText("面修正: " + rebounderBean.calSurface);
        binding.tvTitle4.setText("b: " + rebounderBean.b);
        binding.tvTitle5.setText("强度值: " + rebounderBean.strCar + "MPa");
        testingListAdapter2.submitList(rebounderBean.testNumber);
    }

    private void setTextViewData() {
        binding.tvCarbonization.setText(ViewUtils.setTextColor("碳  化  值：", rebounderZoneBean.detectionStandardCarbonization + "mm", currentColor));
        binding.tvCarbonization.setTag(rebounderZoneBean.detectionStandardCarbonization);
        binding.tvSurface.setText(ViewUtils.setTextColor("检  测  面：", StaticConstant.detectionSurface.get(rebounderZoneBean.detectionSurface - 1), currentColor));
        binding.tvAngle.setText(ViewUtils.setTextColor("角        度：", StaticConstant.getDetectionAngle(rebounderZoneBean.detectionStandard).get(rebounderZoneBean.detectionAngle - 1) + "°", currentColor));
        binding.tvIsPumping.setText(ViewUtils.setTextColor("是否泵送：", StaticConstant.detectionPumping.get(rebounderZoneBean.isPumping), currentColor));
        binding.tvStandard.setText(ViewUtils.setTextColor("检测依据：", StaticConstant.detectionStandard.get(rebounderZoneBean.detectionStandard - 1), currentColor));
        binding.tvTime.setText("检测时间：" + rebounderZoneBean.startTime);
        //
        binding.tvMin.setText("强度最小值：" + rebounderZoneBean.strMin + "MPa");
        binding.tvAverage.setText("强度平均值：" + rebounderZoneBean.strAverage + "MPa");
        binding.tvStandardDeviation.setText("标    准    差：" + rebounderZoneBean.strStdDev + "MPa");
        binding.tvEstimated.setText("强度推定值：" + rebounderZoneBean.strPresumption + "MPa");
        //山东变异系数
        switch (rebounderZoneBean.detectionStandard) {
            case 7:  //山东曲线
            case 8:  //山东高强度曲线
            case 9:  //青岛曲线
            case 10:  //青岛高强度曲线
            case 28:  //广西泵送2023（泵送）
                binding.tvVariation.setVisibility(View.VISIBLE);
                if (TextUtils.equals("--", rebounderZoneBean.strStdDev) || TextUtils.equals("--", rebounderZoneBean.strAverage)) {
                    binding.tvVariation.setText("变异系数：--");
                } else {
                    double variation = StringUtils.getRounding(rebounderZoneBean.stdDev / rebounderZoneBean.average, 2);
                    binding.tvVariation.setText("变异系数：" + variation);
                    if (StringUtils.getVariation(rebounderZoneBean.average, variation)) {
                        ToastUtils.showLong("构件变异系数超出上限");
                    }
                }
                break;
            default:
                binding.tvVariation.setVisibility(View.GONE);
                break;
        }
        //河北浇筑面
        if (rebounderZoneBean.detectionStandard == 5 && rebounderZoneBean.isPumping == 1) {
            binding.tvPouringSurface.setVisibility(View.VISIBLE);
            binding.tvPouringSurface.setText(ViewUtils.setTextColor("浇  筑  面：", StaticConstant.pouringSurface.get(rebounderZoneBean.pouringSurface), currentColor));
        } else {
            binding.tvPouringSurface.setVisibility(View.GONE);
        }
        //
        if (testingList.isEmpty()) {
            if (!TextUtils.isEmpty(rebounderZoneBean.strZoneList)) {
                testingList = new Gson().fromJson(rebounderZoneBean.strZoneList, new TypeToken<List<RebounderBean>>() {
                }.getType());
                for (int i = 0; i < testingList.size(); i++) {
                    testingList.get(i).isSelected = i == 0;
                }
                serialNumAdapter.submitList(testingList);
            }
        }
        for (int i = 0; i < testingList.size(); i++) {
            testingList.get(i).flagCarbonization = !rConfigBean.isCar;
        }
        updateRvData();
    }

    private void setJJM() {
        if (rebounderZoneBean.pouringSurface == 1) {
            rebounderZoneBean.detectionSurface = 3;
            rebounderZoneBean.detectionAngle = 1;
        } else {
            rebounderZoneBean.detectionSurface = rConfigBean.surface;
            rebounderZoneBean.detectionAngle = rConfigBean.angle;
        }
    }

    private void storageDatabase(boolean isCalculate) {
        if (isCalculate) {
            for (int i = 0; i < testingList.size(); i++) {
                testingList.get(i).surface = rebounderZoneBean.detectionSurface;
                testingList.get(i).angle = rebounderZoneBean.detectionAngle;
                testingList.get(i).isPumping = rebounderZoneBean.isPumping;
                testingList.get(i).standardId = rebounderZoneBean.detectionStandard;
                testingList.get(i).carbonization = rebounderZoneBean.detectionStandardCarbonization;
                testingList.get(i).pouringSurface = rebounderZoneBean.pouringSurface;
                testingList.get(i).b = rebounderZoneBean.b;
                testingList.get(i).calculate();
            }
            rebounderZoneBean.strZoneList = new Gson().toJson(testingList);
            //碳化值更新后重新计算并存储
            RebounderUtils.calculate(testingList, rebounderZoneBean);
            setTextViewData();
        }
        isRefresh = true;
        zoneBean.rebounderZoneBean = rebounderZoneBean;
        GreenDaoHelper.getDaoSession(getContext()).getZoneBeanDao().insertOrReplace(zoneBean);
    }

    public void onViewClicked(View view) {
         if (view.getId() == R.id.tv_pouringSurface) {
            OptionsPickerViewUtils.showSingleColumnPickerView(getContext(), "请选择", "",
                    StaticConstant.pouringSurface, binding.tvPouringSurface.getText().toString(), position -> {
                        binding.tvPouringSurface.setText(StaticConstant.pouringSurface.get(position));
                        rebounderZoneBean.pouringSurface = position;
                        setJJM();
                        storageDatabase(true);
                    });
        } else if (view.getId() == R.id.tv_surface) {
            if (!rConfigBean.isSurface) {
                ToastUtils.showShort("无法选择");
                return;
            }
            if (rebounderZoneBean.detectionStandard == 5 && rebounderZoneBean.isPumping == 1 && rebounderZoneBean.pouringSurface == 1) {
                ToastUtils.showShort("无法选择");
                return;
            }
            OptionsPickerViewUtils.showSingleColumnPickerView(getContext(), "请选择", "",
                    StaticConstant.detectionSurface, StaticConstant.detectionSurface.get(rebounderZoneBean.detectionSurface - 1), position -> {
                        binding.tvSurface.setText(StaticConstant.detectionSurface.get(position));
                        rebounderZoneBean.detectionSurface = position + 1;
                        storageDatabase(true);
                    });
        } else if (view.getId() == R.id.tv_angle) {
            if (!rConfigBean.isAngle) {
                ToastUtils.showShort("无法选择");
                return;
            }
            if (rebounderZoneBean.detectionStandard == 5 && rebounderZoneBean.isPumping == 1 && rebounderZoneBean.pouringSurface == 1) {
                ToastUtils.showShort("无法选择");
                return;
            }
            OptionsPickerViewUtils.showSingleColumnPickerView(getContext(), "请选择", "°",
                    StaticConstant.getDetectionAngle(rebounderZoneBean.detectionStandard), StaticConstant.getDetectionAngle(rebounderZoneBean.detectionStandard).get(rebounderZoneBean.detectionAngle - 1), position -> {
                        binding.tvAngle.setText(StaticConstant.getDetectionAngle(rebounderZoneBean.detectionStandard).get(position) + "°");
                        rebounderZoneBean.detectionAngle = position + 1;
                        storageDatabase(true);
                    });
        } else if (view.getId() == R.id.tv_isPumping) {
            if (!rebounderConfigBean.isPumping) {
                ToastUtils.showShort("无法选择");
                return;
            }
            OptionsPickerViewUtils.showSingleColumnPickerView(getContext(), "请选择", "",
                    StaticConstant.detectionPumping, StaticConstant.detectionPumping.get(rebounderZoneBean.isPumping), position -> {
                        binding.tvIsPumping.setText(StaticConstant.detectionPumping.get(position));
                        rConfigBean = rebounderConfigBean.rConfigBeanList.get(position);
                        rebounderZoneBean.isPumping = rConfigBean.pumping;
                        storageDatabase(true);
                    });
        } else if (view.getId() == R.id.tv_standard) {
            OptionsPickerViewUtils.showSingleColumnPickerView(getContext(), "请选择", "",
                    StaticConstant.detectionStandard, StaticConstant.detectionStandard.get(rebounderZoneBean.detectionStandard - 1), position -> {
                        binding.tvStandard.setText(StaticConstant.detectionStandard.get(position));
                        rebounderZoneBean.detectionStandard = position + 1;
                        setTestingConfig();
                        setTestingConfig1();
                        storageDatabase(true);
                    });
        } else if (view.getId() == R.id.tv_carbonization) {
            if (!rConfigBean.isCar) {
                ToastUtils.showShort("无法选择");
                return;
            }
            double carbonizationTag = (double) binding.tvCarbonization.getTag();
            OptionsPickerViewUtils.showSingleColumnPickerView(getContext(), "请选择", "mm",
                    StaticConstant.getDetectionStandardCarbonization(rebounderZoneBean.detectionStandard), carbonizationTag + "", position -> {
                        binding.tvCarbonization.setText("碳  化  值：" + StaticConstant.getDetectionStandardCarbonization(rebounderZoneBean.detectionStandard).get(position) + "mm");
                        binding.tvCarbonization.setTag(StaticConstant.getDetectionStandardCarbonization(rebounderZoneBean.detectionStandard).get(position));
                        rebounderZoneBean.detectionStandardCarbonization = Double.parseDouble(StaticConstant.getDetectionStandardCarbonization(rebounderZoneBean.detectionStandard).get(position));
                        storageDatabase(true);
                    });
        }
    }

    private void setTestingConfig() {
        if (rebounderZoneBean.detectionStandard == 0) {
            return;
        }
        String strName = StaticConstant.detectionStandard.get(rebounderZoneBean.detectionStandard - 1);
        rebounderConfigBean = new RebounderConfigBean();
        for (int i = 0; i < StaticConstant.getRebounderConfigBeanList().size(); i++) {
            if (TextUtils.equals(StaticConstant.getRebounderConfigBeanList().get(i).curve, strName)) {
                rebounderConfigBean = StaticConstant.getRebounderConfigBeanList().get(i);
                break;
            }
        }
        rConfigBean = rebounderConfigBean.rConfigBeanList.get(0);
    }

    private void setTestingConfig1() {
        rebounderZoneBean.isPumping = rConfigBean.pumping;
        rebounderZoneBean.detectionSurface = rConfigBean.surface;
        rebounderZoneBean.detectionAngle = rConfigBean.angle;
        binding.tvIsPumping.setText(ViewUtils.setTextColor("是否泵送：", StaticConstant.detectionPumping.get(rebounderZoneBean.isPumping), currentColor));
        binding.tvSurface.setText(ViewUtils.setTextColor("检  测  面：", StaticConstant.detectionSurface.get(rebounderZoneBean.detectionSurface - 1), currentColor));
        binding.tvAngle.setText(ViewUtils.setTextColor("角        度：", StaticConstant.getDetectionAngle(rebounderZoneBean.detectionStandard).get(rebounderZoneBean.detectionAngle - 1) + "°", currentColor));
        boolean isExist = false;
        for (int i = 0; i < StaticConstant.getDSCarbonization(rebounderZoneBean.detectionStandard).length; i++) {
            if (rebounderZoneBean.detectionStandardCarbonization == StaticConstant.getDSCarbonization(rebounderZoneBean.detectionStandard)[i]) {
                isExist = true;
                break;
            }
        }
        if (!isExist || !rConfigBean.isCar) {
            rebounderZoneBean.detectionStandardCarbonization = StaticConstant.getDSCarbonization(rebounderZoneBean.detectionStandard)[0];
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            RebounderZoneBean rebounderZoneBean = (RebounderZoneBean) Objects.requireNonNull(data).getSerializableExtra("ProjectItemPointBean");
            //碳化值更新后重新计算并存储
            if (!TextUtils.isEmpty(Objects.requireNonNull(rebounderZoneBean).strZoneList)) {
                testingList.clear();
                testingList.addAll(new Gson().fromJson(rebounderZoneBean.strZoneList, new TypeToken<List<RebounderBean>>() {
                }.getType()));
                if (testingList.size() > selectedItem) {
                    testingList.get(selectedItem).isSelected = true;
                }
            }
            zoneBean.rebounderZoneBean.strZoneList = rebounderZoneBean.strZoneList;
            RebounderUtils.calculate(testingList, rebounderZoneBean);
            setTextViewData();
            storageDatabase(false);
        }
    }
}

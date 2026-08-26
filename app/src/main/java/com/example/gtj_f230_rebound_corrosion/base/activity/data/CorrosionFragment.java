package com.example.gtj_f230_rebound_corrosion.base.activity.data;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.base.model.ZoneBean;
import com.example.gtj_f230_rebound_corrosion.base.utils.StringUtils;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.CorrosionBean;
import com.example.gtj_f230_rebound_corrosion.databinding.FragmentCorrosionResultBinding;
import com.example.gtj_f230_rebound_corrosion.databinding.ViewGridContentBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

@SuppressLint("SetTextI18n")
public class CorrosionFragment extends Fragment {

    private FragmentCorrosionResultBinding binding;

    private ZoneBean zoneBean;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        zoneBean = ((DataDetailsActivity) context).getZoneBean();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCorrosionResultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (zoneBean == null || zoneBean.corrosionZoneBean == null) {
            binding.llData.setVisibility(GONE);
            binding.tvNull.setVisibility(VISIBLE);
            return;
        }
        binding.llData.setVisibility(VISIBLE);
        binding.tvNull.setVisibility(GONE);

        binding.tvDiameterTop.setText("d: " + zoneBean.corrosionZoneBean.poleTopDiameter + "mm");
        binding.tvDiameterBottom.setText("D: " + zoneBean.corrosionZoneBean.poleBottomDiameter + "mm");
        binding.tvPoleHeight.setText("L: " + zoneBean.corrosionZoneBean.poleHeight + "m");

        //截面积
        double designR = Double.parseDouble(zoneBean.corrosionZoneBean.rebarDiameter) / 2;
        double designArea = StringUtils.getRounding(Math.PI * designR * designR, 2);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("电杆类型：").append(StaticConstant.poleTypeList.get(zoneBean.corrosionZoneBean.poleType)).append("\n适用范围：").append(StaticConstant.poleStandardList.get(zoneBean.corrosionZoneBean.poleStandard)).append("\n测点数量：").append(zoneBean.corrosionZoneBean.zoneCount).append("\n主筋直径：").append(zoneBean.corrosionZoneBean.rebarDiameter).append("mm");

        binding.tvInfo1.setText(stringBuilder);
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder.append("电杆长度：").append(zoneBean.corrosionZoneBean.poleHeight).append("m").append("\n电杆根径：").append(zoneBean.corrosionZoneBean.poleBottomDiameter).append("mm").append("\n电杆稍径：").append(zoneBean.corrosionZoneBean.poleTopDiameter).append("mm").append("\n主筋截面积：").append(designArea).append("mm²");
        binding.tvInfo2.setText(stringBuilder);
        refreshGrid();
    }

    @Override
    public void onResume() {
        super.onResume();
        showPoleLocation();
    }

    private void showPoleLocation() {
        binding.vMark.post(() -> {
            //x
            int[] location = new int[2];
            binding.vMark.getLocationInWindow(location);
            int[] parentLocation = new int[2];
            binding.rlMark.getLocationInWindow(parentLocation);
            location[0] = location[0] - parentLocation[0];
            int width = binding.vMark.getWidth();
            int originalPoint = location[0];
            //y
            int heightTop = binding.vTop.getHeight();
            int heightBottom1 = binding.vBottom1.getHeight();
            //
            int heightTop1 = binding.vTop1.getHeight();
            int heightBottom = binding.vBottom.getHeight();
            if (zoneBean.corrosionZoneBean != null && !TextUtils.isEmpty(zoneBean.corrosionZoneBean.strZoneList)) {
                List<CorrosionBean> corrosionBeanList = new Gson().fromJson(zoneBean.corrosionZoneBean.strZoneList, new TypeToken<List<CorrosionBean>>() {
                }.getType());
                if (!corrosionBeanList.isEmpty() && checkLength(corrosionBeanList.get(0).distance)) {
                    treatLocation(binding.vMiddle1, binding.tvMiddle1, corrosionBeanList.get(0), originalPoint, width, heightTop, heightBottom1);
                }
                if (corrosionBeanList.size() > 1 && checkLength(corrosionBeanList.get(1).distance)) {
                    treatLocation(binding.vMiddle2, binding.tvMiddle2, corrosionBeanList.get(1), originalPoint, width, heightTop1, heightBottom);
                }
                if (corrosionBeanList.size() > 2 && checkLength(corrosionBeanList.get(2).distance)) {
                    treatLocation(binding.vMiddle3, binding.tvMiddle3, corrosionBeanList.get(2), originalPoint, width, heightTop, heightBottom1);
                }
                if (corrosionBeanList.size() > 3 && checkLength(corrosionBeanList.get(3).distance)) {
                    treatLocation(binding.vMiddle4, binding.tvMiddle4, corrosionBeanList.get(3), originalPoint, width, heightTop1, heightBottom);
                }
                if (corrosionBeanList.size() > 4 && checkLength(corrosionBeanList.get(4).distance)) {
                    treatLocation(binding.vMiddle5, binding.tvMiddle5, corrosionBeanList.get(4), originalPoint, width, heightTop, heightBottom1);
                }
                if (corrosionBeanList.size() > 5 && checkLength(corrosionBeanList.get(5).distance)) {
                    treatLocation(binding.vMiddle6, binding.tvMiddle6, corrosionBeanList.get(5), originalPoint, width, heightTop1, heightBottom);
                }
                if (corrosionBeanList.size() > 6 && checkLength(corrosionBeanList.get(6).distance)) {
                    treatLocation(binding.vMiddle7, binding.tvMiddle7, corrosionBeanList.get(6), originalPoint, width, heightTop, heightBottom1);
                }
                if (corrosionBeanList.size() > 7 && checkLength(corrosionBeanList.get(7).distance)) {
                    treatLocation(binding.vMiddle8, binding.tvMiddle8, corrosionBeanList.get(7), originalPoint, width, heightTop1, heightBottom);
                }
                if (corrosionBeanList.size() > 8 && checkLength(corrosionBeanList.get(8).distance)) {
                    treatLocation(binding.vMiddle9, binding.tvMiddle9, corrosionBeanList.get(8), originalPoint, width, heightTop, heightBottom1);
                }
                if (corrosionBeanList.size() > 9 && checkLength(corrosionBeanList.get(9).distance)) {
                    treatLocation(binding.vMiddle10, binding.tvMiddle10, corrosionBeanList.get(9), originalPoint, width, heightTop1, heightBottom);
                }
            }
        });
    }

    private void treatLocation(View view, TextView textView, CorrosionBean pageBean, int originalPoint, int width, int heightTop, int heightBottom) {
        int location_x = (int) StringUtils.getInterpolationValue(0.0001, originalPoint, Double.parseDouble(zoneBean.corrosionZoneBean.poleHeight), originalPoint + width, Double.parseDouble(pageBean.distance));
        int height = (int) StringUtils.getInterpolationValue(0.0001, heightBottom, Double.parseDouble(zoneBean.corrosionZoneBean.poleHeight), heightTop, Double.parseDouble(pageBean.distance));
        RelativeLayout.LayoutParams middleLayoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        middleLayoutParams.leftMargin = location_x;
        middleLayoutParams.height = height;
        view.setLayoutParams(middleLayoutParams);
        textView.setText("锈蚀度: " + pageBean.corrosion + "%");
        view.setVisibility(VISIBLE);
        textView.setVisibility(VISIBLE);
    }

    /**
     * 刷新钢筋列表
     */
    private void refreshGrid() {
        binding.layoutGridContent.removeAllViews();

        if (!TextUtils.isEmpty(zoneBean.corrosionZoneBean.strZoneList)) {
            List<CorrosionBean> corrosionBeanList = new Gson().fromJson(zoneBean.corrosionZoneBean.strZoneList, new TypeToken<List<CorrosionBean>>() {
            }.getType());
            for (int i = 0; i < corrosionBeanList.size(); i++) {
                ViewGridContentBinding gridContentBinding = ViewGridContentBinding.inflate(getLayoutInflater());
                View itemView = gridContentBinding.getRoot();
                configGrid(gridContentBinding, i, corrosionBeanList.get(i));
                itemView.setId(i);
                binding.layoutGridContent.addView(gridContentBinding.getRoot());
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void configGrid(ViewGridContentBinding gridContentBinding, int position, CorrosionBean corrosion) {
        gridContentBinding.tvGridNumber.setText(position + 1 + "");
        gridContentBinding.tvGrid1.setText(corrosion.distance);
        gridContentBinding.tvGrid2.setText(corrosion.diameter);
        gridContentBinding.tvGrid3.setText(corrosion.strCheckArea);
        gridContentBinding.tvGrid4.setText(corrosion.corrosion1);
    }

    private boolean checkLength(String distance) {
        if (TextUtils.isEmpty(distance) || TextUtils.isEmpty(zoneBean.corrosionZoneBean.poleHeight)) {
            return false;
        }
        int distanceInt = (int) Double.parseDouble(distance);
        int poleHeight = (int) Double.parseDouble(zoneBean.corrosionZoneBean.poleHeight);
        return distanceInt <= poleHeight;
    }
}

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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.blankj.utilcode.util.SizeUtils;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.base.model.ZoneBean;
import com.example.gtj_f230_rebound_corrosion.base.storage.GreenDaoHelper;
import com.example.gtj_f230_rebound_corrosion.base.utils.StringUtils;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.RebarBean;
import com.example.gtj_f230_rebound_corrosion.databinding.FragmentThicknessResultBinding;
import com.example.gtj_f230_rebound_corrosion.thickness.model.Rebar240ZoneBean;
import com.example.gtj_f230_rebound_corrosion.thickness.model.RebarPageBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("SetTextI18n")
public class ThicknessFragment extends Fragment {
    private FragmentThicknessResultBinding binding;
    private int color000, colorFF9600;
    private ZoneBean zoneBean;
    private Rebar240ZoneBean rebarZoneBean;
    private final Map<String, RebarPageBean> averageMap = new HashMap<>();
    private double thicknessMin, thicknessMax;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        zoneBean = ((DataDetailsActivity) context).getZoneBean();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentThicknessResultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        color000 = ContextCompat.getColor(requireActivity(), R.color.color000);
        colorFF9600 = ContextCompat.getColor(requireActivity(), R.color.colorFF9600);
        rebarZoneBean = GreenDaoHelper.getDaoSession(requireActivity()).getRebar240ZoneBeanDao().load(zoneBean.thicknessId);
        if (zoneBean == null || rebarZoneBean == null) {
            binding.llData.setVisibility(GONE);
            binding.tvNull.setVisibility(VISIBLE);
            return;
        }
        binding.llData.setVisibility(VISIBLE);
        binding.tvNull.setVisibility(GONE);
        binding.tvDiameterTop.setText("d: " + rebarZoneBean.poleTopDiameter + "mm");
        binding.tvDiameterBottom.setText("D: " + rebarZoneBean.poleBottomDiameter + "mm");
        binding.tvPoleHeight.setText("L: " + rebarZoneBean.poleHeight + "m");
        String strRebarDiameter = StaticConstant.rebarDiameterList.get(rebarZoneBean.rebarDiameter);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("电杆类型：").append(StaticConstant.poleTypeList.get(rebarZoneBean.poleType)).append("\n适用范围：").append(StaticConstant.poleStandardList.get(rebarZoneBean.poleStandard)).append("\n主筋直径：").append(strRebarDiameter).append("mm");

        binding.tvInfo1.setText(stringBuilder);
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder.append("电杆长度：").append(rebarZoneBean.poleHeight).append("m\n电杆根径：").append(rebarZoneBean.poleBottomDiameter).append("mm\n电杆稍径：").append(rebarZoneBean.poleTopDiameter).append("mm");
        binding.tvInfo2.setText(stringBuilder);
        //------------------------------------

        double designThickness = Double.parseDouble(rebarZoneBean.designThickness);
        //+8 -2 剔凿验证
        thicknessMin = StringUtils.getRounding(designThickness - 2, 1);
        thicknessMax = StringUtils.getRounding(designThickness + 8, 1);
        //+8 -2 剔凿验证
        thicknessMin = -1;
        thicknessMax = 100;
        //
        configGridTop(rebarZoneBean.poleBottomCount);
        refreshGrid();
        showPoleLocation();
    }

    private void configGridTop(int rebarCount) {
        binding.layoutGridTop.tvGrid1.setVisibility(rebarCount >= 1 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.vGrid1.setVisibility(rebarCount >= 1 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.tvGrid2.setVisibility(rebarCount >= 2 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.vGrid2.setVisibility(rebarCount >= 2 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.tvGrid3.setVisibility(rebarCount >= 3 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.vGrid3.setVisibility(rebarCount >= 3 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.tvGrid4.setVisibility(rebarCount >= 4 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.vGrid4.setVisibility(rebarCount >= 4 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.tvGrid5.setVisibility(rebarCount >= 5 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.vGrid5.setVisibility(rebarCount >= 5 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.tvGrid6.setVisibility(rebarCount >= 6 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.vGrid6.setVisibility(rebarCount >= 6 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.tvGrid7.setVisibility(rebarCount >= 7 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.vGrid7.setVisibility(rebarCount >= 7 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.tvGrid8.setVisibility(rebarCount >= 8 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.vGrid8.setVisibility(rebarCount >= 8 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.tvGrid9.setVisibility(rebarCount >= 9 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.vGrid9.setVisibility(rebarCount >= 9 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.tvGrid10.setVisibility(rebarCount >= 10 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.vGrid10.setVisibility(rebarCount >= 10 ? View.VISIBLE : View.GONE);
        binding.layoutGridTop.tvGridAverage.setVisibility(rebarCount >= 1 ? View.VISIBLE : View.GONE);
    }

    /**
     * 刷新钢筋列表
     */
    private void refreshGrid() {
        binding.layoutGridContent.removeAllViews();
        int rebarPageListSize = rebarZoneBean.rebarPageList.size();
        if (rebarPageListSize > 0) {
            int lastBeanListSize = rebarZoneBean.rebarPageList.get(rebarZoneBean.rebarPageList.size() - 1).beanList.size();
            if (lastBeanListSize == 0) {
                rebarPageListSize -= 1;
            }
            for (int i = 0; i < rebarPageListSize; i++) {
                @SuppressLint("InflateParams") View itemView = LayoutInflater.from(requireActivity()).inflate(R.layout.view_grid_result_content, null, false);
                configGrid(itemView, rebarZoneBean.poleBottomCount, rebarZoneBean.rebarPageList.get(i));
                itemView.setId(i);
//              itemView.setOnLongClickListener(gridItemView);
                binding.layoutGridContent.addView(itemView);
            }
        }
    }

    private void showPoleLocation() {
        binding.vMark.post(() -> {
            //x
            int[] location = new int[2];
            binding.vMark.getLocationInWindow(location);
            int width = binding.vMark.getWidth();
            int originalPoint = location[0] - SizeUtils.dp2px(14);
            //y
            int heightTop = binding.vTop.getHeight();
            int heightBottom1 = binding.vBottom1.getHeight();
            //
            int heightTop1 = binding.vTop1.getHeight();
            int heightBottom = binding.vBottom.getHeight();
            if (!averageMap.isEmpty()) {
                List<RebarPageBean> pageBeanList = new ArrayList<>();
                for (String key : averageMap.keySet()) {
                    pageBeanList.add(averageMap.get(key));
                }
                Collections.sort(pageBeanList);
                if (!pageBeanList.isEmpty()) {
                    treatLocation(binding.vMiddle1, binding.tvMiddle1, pageBeanList.get(0), originalPoint, width, heightTop, heightBottom1);
                }
                if (pageBeanList.size() > 1) {
                    treatLocation(binding.vMiddle2, binding.tvMiddle2, pageBeanList.get(1), originalPoint, width, heightTop1, heightBottom);
                }
                if (pageBeanList.size() > 2) {
                    treatLocation(binding.vMiddle3, binding.tvMiddle3, pageBeanList.get(2), originalPoint, width, heightTop, heightBottom1);
                }
                if (pageBeanList.size() > 3) {
                    treatLocation(binding.vMiddle4, binding.tvMiddle4, pageBeanList.get(3), originalPoint, width, heightTop1, heightBottom);
                }
                if (pageBeanList.size() > 4) {
                    treatLocation(binding.vMiddle5, binding.tvMiddle5, pageBeanList.get(4), originalPoint, width, heightTop, heightBottom1);
                }
                if (pageBeanList.size() > 5) {
                    treatLocation(binding.vMiddle6, binding.tvMiddle6, pageBeanList.get(5), originalPoint, width, heightTop1, heightBottom);
                }
                if (pageBeanList.size() > 6) {
                    treatLocation(binding.vMiddle7, binding.tvMiddle7, pageBeanList.get(6), originalPoint, width, heightTop, heightBottom1);
                }
                if (pageBeanList.size() > 7) {
                    treatLocation(binding.vMiddle8, binding.tvMiddle8, pageBeanList.get(7), originalPoint, width, heightTop1, heightBottom);
                }
                if (pageBeanList.size() > 8) {
                    treatLocation(binding.vMiddle9, binding.tvMiddle9, pageBeanList.get(8), originalPoint, width, heightTop, heightBottom1);
                }
                if (pageBeanList.size() > 9) {
                    treatLocation(binding.vMiddle10, binding.tvMiddle10, pageBeanList.get(9), originalPoint, width, heightTop1, heightBottom);
                }
            }
        });
    }

    private void treatLocation(View view, TextView textView, RebarPageBean pageBean, int originalPoint, int width, int heightTop, int heightBottom) {
        int location_x = (int) StringUtils.getInterpolationValue(0, originalPoint, Double.parseDouble(rebarZoneBean.poleHeight), originalPoint + width, pageBean.distance);
        int height = (int) StringUtils.getInterpolationValue(0, heightBottom, Double.parseDouble(rebarZoneBean.poleHeight), heightTop, pageBean.distance);
        RelativeLayout.LayoutParams middleLayoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        middleLayoutParams.leftMargin = location_x;
        middleLayoutParams.height = height;
        view.setLayoutParams(middleLayoutParams);
        textView.setText("H: " + pageBean.averageThickness);
        view.setVisibility(View.VISIBLE);
        textView.setVisibility(View.VISIBLE);
    }

    private void configGrid(View itemView, int rebarCount, RebarPageBean bean) {
        TextView tvNumber = itemView.findViewById(R.id.tv_grid_number);
        TextView tv1 = itemView.findViewById(R.id.tv_grid_1);
        View v1 = itemView.findViewById(R.id.v_grid_1);
        tv1.setVisibility(rebarCount >= 1 ? View.VISIBLE : View.GONE);
        v1.setVisibility(rebarCount >= 1 ? View.VISIBLE : View.GONE);
        TextView tv2 = itemView.findViewById(R.id.tv_grid_2);
        View v2 = itemView.findViewById(R.id.v_grid_2);
        tv2.setVisibility(rebarCount >= 2 ? View.VISIBLE : View.GONE);
        v2.setVisibility(rebarCount >= 2 ? View.VISIBLE : View.GONE);
        TextView tv3 = itemView.findViewById(R.id.tv_grid_3);
        View v3 = itemView.findViewById(R.id.v_grid_3);
        tv3.setVisibility(rebarCount >= 3 ? View.VISIBLE : View.GONE);
        v3.setVisibility(rebarCount >= 3 ? View.VISIBLE : View.GONE);
        TextView tv4 = itemView.findViewById(R.id.tv_grid_4);
        View v4 = itemView.findViewById(R.id.v_grid_4);
        tv4.setVisibility(rebarCount >= 4 ? View.VISIBLE : View.GONE);
        v4.setVisibility(rebarCount >= 4 ? View.VISIBLE : View.GONE);
        TextView tv5 = itemView.findViewById(R.id.tv_grid_5);
        View v5 = itemView.findViewById(R.id.v_grid_5);
        tv5.setVisibility(rebarCount >= 5 ? View.VISIBLE : View.GONE);
        v5.setVisibility(rebarCount >= 5 ? View.VISIBLE : View.GONE);
        TextView tv6 = itemView.findViewById(R.id.tv_grid_6);
        View v6 = itemView.findViewById(R.id.v_grid_6);
        tv6.setVisibility(rebarCount >= 6 ? View.VISIBLE : View.GONE);
        v6.setVisibility(rebarCount >= 6 ? View.VISIBLE : View.GONE);
        TextView tv7 = itemView.findViewById(R.id.tv_grid_7);
        View v7 = itemView.findViewById(R.id.v_grid_7);
        tv7.setVisibility(rebarCount >= 7 ? View.VISIBLE : View.GONE);
        v7.setVisibility(rebarCount >= 7 ? View.VISIBLE : View.GONE);
        TextView tv8 = itemView.findViewById(R.id.tv_grid_8);
        View v8 = itemView.findViewById(R.id.v_grid_8);
        tv8.setVisibility(rebarCount >= 8 ? View.VISIBLE : View.GONE);
        v8.setVisibility(rebarCount >= 8 ? View.VISIBLE : View.GONE);
        TextView tv9 = itemView.findViewById(R.id.tv_grid_9);
        View v9 = itemView.findViewById(R.id.v_grid_9);
        tv9.setVisibility(rebarCount >= 9 ? View.VISIBLE : View.GONE);
        v9.setVisibility(rebarCount >= 9 ? View.VISIBLE : View.GONE);
        TextView tv10 = itemView.findViewById(R.id.tv_grid_10);
        View v10 = itemView.findViewById(R.id.v_grid_10);
        tv10.setVisibility(rebarCount >= 10 ? View.VISIBLE : View.GONE);
        v10.setVisibility(rebarCount >= 10 ? View.VISIBLE : View.GONE);
        TextView tvAver = itemView.findViewById(R.id.tv_grid_average);
        tvAver.setVisibility(rebarCount >= 1 ? View.VISIBLE : View.GONE);
        //
        tvNumber.setText(bean.segment);
        if (bean.beanList == null) {
            return;
        }
        if (!bean.beanList.isEmpty()) {
            setTextView(tv1, bean.beanList.get(0));
        }
        if (bean.beanList.size() > 1) {
            setTextView(tv2, bean.beanList.get(1));
        }
        if (bean.beanList.size() > 2) {
            setTextView(tv3, bean.beanList.get(2));
        }
        if (bean.beanList.size() > 3) {
            setTextView(tv4, bean.beanList.get(3));
        }
        if (bean.beanList.size() > 4) {
            setTextView(tv5, bean.beanList.get(4));
        }
        if (bean.beanList.size() > 5) {
            setTextView(tv6, bean.beanList.get(5));
        }
        if (bean.beanList.size() > 6) {
            setTextView(tv7, bean.beanList.get(6));
        }
        if (bean.beanList.size() > 7) {
            setTextView(tv8, bean.beanList.get(7));
        }
        if (bean.beanList.size() > 8) {
            setTextView(tv9, bean.beanList.get(8));
        }
        if (bean.beanList.size() > 9) {
            setTextView(tv10, bean.beanList.get(9));
        }
        if (!bean.beanList.isEmpty()) {
            tvAver.setText(bean.averageThickness1 + "\n" + bean.averageThickness);
        }
        if (TextUtils.equals("梢部", bean.segment)) {
            binding.tvTop.setText("H: " + bean.averageThickness);
            binding.tvTop.setVisibility(View.VISIBLE);
            binding.vTop.setVisibility(View.VISIBLE);
        } else if (TextUtils.equals("根部", bean.segment)) {
            binding.tvBottom.setText("H: " + bean.averageThickness);
            binding.tvBottom.setVisibility(View.VISIBLE);
            binding.vBottom.setVisibility(View.VISIBLE);
        } else {
            averageMap.put(bean.segment, bean);
        }
    }

    private void setTextView(TextView textView, RebarBean bean) {
        textView.setText(bean.thickness1 + "\n" + bean.thickness);
        textView.setTextColor((bean.thickness1 > thicknessMax || bean.thickness1 < thicknessMin) ? colorFF9600 : color000);
    }
}

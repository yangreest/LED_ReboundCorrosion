package com.example.gtj_f230_rebound_corrosion.base.activity.data;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.base.model.ZoneBean;
import com.example.gtj_f230_rebound_corrosion.base.storage.GreenDaoHelper;
import com.example.gtj_f230_rebound_corrosion.base.utils.ShutUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.StringUtils;
import com.example.gtj_f230_rebound_corrosion.databinding.FragmentDataF230WidthBinding;
import com.example.gtj_f230_rebound_corrosion.f230.adapter.F230SerialNumAdapter;
import com.example.gtj_f230_rebound_corrosion.f230.model.WidthBean;
import com.example.gtj_f230_rebound_corrosion.f230.view.TriangleLeftView;
import com.example.gtj_f230_rebound_corrosion.f230.view.TriangleRightView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Collections;
import java.util.List;

public class F230WidthFragment extends Fragment {

    private FragmentDataF230WidthBinding binding;
    private ImageView imageView;
    private TriangleLeftView leftView;
    private TriangleRightView rightView;
    private ZoneBean zoneBean;
    private List<WidthBean> widthBeanList;
    private F230SerialNumAdapter serialNumAdapter;
    private float viewRatio;
    private double modifyWidth;
    private double calWidth4;
    //正在显示的数据
    private int showIndex = 0;
    private WidthBean widthBean = null;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        zoneBean = ((DataDetailsActivity) context).getZoneBean();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDataF230WidthBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        zoneBean = GreenDaoHelper.getDaoSession(getActivity()).getZoneBeanDao().load(zoneBean.id);

        if (zoneBean == null || zoneBean.f230ZoneBean == null || TextUtils.isEmpty(zoneBean.f230ZoneBean.strZoneList)) {
            binding.tvNull.setVisibility(VISIBLE);
            return;
        }
        binding.tvNull.setVisibility(GONE);
        widthBeanList = new Gson().fromJson(zoneBean.f230ZoneBean.strZoneList, new TypeToken<List<WidthBean>>() {
        }.getType());
        for (int i = 0; i < widthBeanList.size(); i++) {
            widthBeanList.get(i).isSelected = i == 0;
        }
        calWidth4 = SPUtils.getInstance().getFloat("cal-width-4", 279);

        initView();
        initData(0);
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        int fullWidth = SizeUtils.dp2px(656);
        int fullHeight = (int) (fullWidth * StaticConstant.matrixRatio);
        viewRatio = fullWidth / 640.0f;
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) binding.layoutVideo.getLayoutParams();
        layoutParams.width = fullWidth;
        layoutParams.height = fullHeight;
        binding.layoutVideo.setLayoutParams(layoutParams);

        imageView = new ImageView(getContext());
        binding.videoLayout.addView(imageView);
        leftView = new TriangleLeftView(getContext(), false, fullHeight / 2);
        rightView = new TriangleRightView(getContext(), false, fullHeight / 2);
        binding.videoLayout.addView(leftView);
        binding.videoLayout.addView(rightView);

        binding.rvSerialNum.setLayoutManager(new LinearLayoutManager(getContext()));
        serialNumAdapter = new F230SerialNumAdapter(getContext());
        binding.rvSerialNum.setAdapter(serialNumAdapter);
        serialNumAdapter.setOnItemClickListener((adapter, view, position) -> {
            initData(position);
        });
        serialNumAdapter.submitList(widthBeanList);

        binding.btnChange.setOnClickListener(this::onViewClicked);
        binding.btnDefault.setOnClickListener(this::onViewClicked);
        binding.btnConfirm.setOnClickListener(this::onViewClicked);
        binding.btnLTop.setOnClickListener(this::onViewClicked);
        binding.btnLBottom.setOnClickListener(this::onViewClicked);
        binding.btnLLeft.setOnClickListener(this::onViewClicked);
        binding.btnLRight.setOnClickListener(this::onViewClicked);
        binding.btnRTop.setOnClickListener(this::onViewClicked);
        binding.btnRBottom.setOnClickListener(this::onViewClicked);
        binding.btnRLeft.setOnClickListener(this::onViewClicked);
        binding.btnRRight.setOnClickListener(this::onViewClicked);
        leftView.setPointTouchListener(this::treatWidth);
        rightView.setPointTouchListener(this::treatWidth);
    }

    private void initData(int position) {
        widthBeanList.get(showIndex).isSelected = false;
        widthBeanList.get(position).isSelected = true;
        showIndex = position;
        widthBean = widthBeanList.get(showIndex);
        serialNumAdapter.notifyDataSetChanged();

        Glide.with(this).load(widthBean.imagePath).into(imageView);
        leftView.currentX = widthBean.widthLeftX;
        leftView.currentY = widthBean.widthLeftY;
        leftView.setOutsidePoint(widthBean.widthRightX, widthBean.widthRightY);
        rightView.currentX = widthBean.widthRightX;
        rightView.currentY = widthBean.widthRightY;
        rightView.setOutsidePoint(widthBean.widthLeftX, widthBean.widthLeftY);
        binding.tvWidth.setText(widthBean.width + "mm");

        binding.tvTime.setText(widthBean.time);
        binding.tvNum.setText("测点: " + widthBean.id + "/" + widthBeanList.size());
        binding.tvTime.setText(widthBeanList.get(position).time);
        binding.tvWidth.setText(widthBeanList.get(position).width + "mm");
    }

    public void onViewClicked(View view) {
        if (view.getId() == binding.btnChange.getId()) {
            binding.rlLeft.setVisibility(VISIBLE);
            binding.rlRight.setVisibility(VISIBLE);
            binding.btnDefault.setVisibility(VISIBLE);
            binding.btnConfirm.setVisibility(VISIBLE);
            binding.btnChange.setVisibility(GONE);
            leftView.setFocus(true);
            rightView.setFocus(true);
        } else if (view.getId() == binding.btnDefault.getId()) {
            setTriangleViewRaw();
        } else if (view.getId() == binding.btnConfirm.getId()) {
            binding.rlLeft.setVisibility(GONE);
            binding.rlRight.setVisibility(GONE);
            binding.btnDefault.setVisibility(INVISIBLE);
            binding.btnConfirm.setVisibility(GONE);
            binding.btnChange.setVisibility(VISIBLE);
            leftView.setFocus(false);
            rightView.setFocus(false);
            widthBean.widthLeftX = leftView.currentX;
            widthBean.widthRightX = rightView.currentX;
            widthBean.widthLeftY = leftView.currentY;
            widthBean.widthRightY = rightView.currentY;
            widthBean.width = modifyWidth;
            //
            for (int i = 0; i < widthBeanList.size(); i++) {
                if (widthBean.id == widthBeanList.get(i).id) {
                    widthBeanList.remove(i);
                    break;
                }
            }
            widthBeanList.add(widthBean);
            Collections.sort(widthBeanList, (o1, o2) -> o1.id - o2.id);
            //
            zoneBean.f230ZoneBean.strZoneList = new Gson().toJson(widthBeanList);
            zoneBean.f230ZoneBean.zoneCount = widthBeanList.size();
            GreenDaoHelper.getDaoSession(getContext()).getZoneBeanDao().update(zoneBean);
            ToastUtils.showShort("保存成功");
            ShutUtils.shutDataChangeActivity(getContext(), 1);
        } else if (view.getId() == binding.btnLTop.getId()) {
            leftView.currentY--;
            treatWidth();
        } else if (view.getId() == binding.btnLBottom.getId()) {
            leftView.currentY++;
            treatWidth();
        } else if (view.getId() == binding.btnLLeft.getId()) {
            leftView.currentX--;
            treatWidth();
        } else if (view.getId() == binding.btnLRight.getId()) {
            leftView.currentX++;
            treatWidth();
        } else if (view.getId() == binding.btnRTop.getId()) {
            rightView.currentY--;
            treatWidth();
        } else if (view.getId() == binding.btnRBottom.getId()) {
            rightView.currentY++;
            treatWidth();
        } else if (view.getId() == binding.btnRLeft.getId()) {
            rightView.currentX--;
            treatWidth();
        } else if (view.getId() == binding.btnRRight.getId()) {
            rightView.currentX++;
            treatWidth();
        }
    }

    private void setTriangleViewRaw() {
        leftView.currentX = widthBean.widthLeftXRaw;
        leftView.currentY = widthBean.widthLeftYRaw;
        leftView.setOutsidePoint(widthBean.widthRightXRaw, widthBean.widthRightYRaw);
        rightView.currentX = widthBean.widthRightXRaw;
        rightView.currentY = widthBean.widthRightYRaw;
        rightView.setOutsidePoint(widthBean.widthLeftXRaw, widthBean.widthLeftYRaw);
        binding.tvWidth.setText(widthBean.width + "mm");
    }

    private void treatWidth() {
        leftView.setOutsidePoint(rightView.currentX, rightView.currentY);
        rightView.setOutsidePoint(leftView.currentX, leftView.currentY);

        int fx = Math.abs((int) (rightView.currentX / viewRatio) - (int) (leftView.currentX / viewRatio));
        int fy = Math.abs((int) (rightView.currentY / viewRatio) - (int) (leftView.currentY / viewRatio));
        float fz = (float) Math.sqrt(fx * fx + fy * fy);
        modifyWidth = getWidth(fz);
        binding.tvWidth.setText(modifyWidth + "mm");
    }

    public double getWidth(double number) {
        return StringUtils.getRounding1(number / (calWidth4 / 5), 2);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.videoLayout.removeAllViews();
    }
}

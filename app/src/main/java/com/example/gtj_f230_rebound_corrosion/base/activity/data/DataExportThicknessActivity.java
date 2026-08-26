package com.example.gtj_f230_rebound_corrosion.base.activity.data;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.SizeUtils;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.base.activity.BaseActivity;
import com.example.gtj_f230_rebound_corrosion.base.model.ZoneBean;
import com.example.gtj_f230_rebound_corrosion.base.storage.GreenDaoHelper;
import com.example.gtj_f230_rebound_corrosion.base.utils.BitmapUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.DataOutputUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.ExportUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.FileUtils;
import com.example.gtj_f230_rebound_corrosion.base.utils.StringUtils;
import com.example.gtj_f230_rebound_corrosion.base.view.StyleAlertDialog;
import com.example.gtj_f230_rebound_corrosion.base.view.StyleAlertDialog1;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.RebarBean;
import com.example.gtj_f230_rebound_corrosion.databinding.ActDataExportThicknessBinding;
import com.example.gtj_f230_rebound_corrosion.functions.database.greenDao.db.ZoneBeanDao;
import com.example.gtj_f230_rebound_corrosion.thickness.model.Rebar240ZoneBean;
import com.example.gtj_f230_rebound_corrosion.thickness.model.RebarPageBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.logger.Logger;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressLint({"SetTextI18n", "InflateParams", "SdCardPath"})
public class DataExportThicknessActivity extends BaseActivity<ActDataExportThicknessBinding> {
    private final Map<String, RebarPageBean> averageMap = new HashMap<>();
    private Set<Long> treatZoneIdSet = new HashSet<>();
    private final List<String> exportPathList = new ArrayList<>();
    private final List<ZoneBean> exportZoneList = new ArrayList<>();
    private int exportCount;
    private float viewRatio;

    @Override
    protected ActDataExportThicknessBinding getBinding() {
        return ActDataExportThicknessBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initLabel() {
        binding.navigationBar.setTitleText("数据导出");
        initBatteryReceiver(binding.navigationBar);
        initDeviceStatusExecutor(binding.navigationBar);
        int fullWidth = SizeUtils.dp2px(656);
        viewRatio = fullWidth / 640.0f;
    }

    @Override
    protected void initView() {
        String strZoneIdSet = getIntent().getStringExtra("treatZoneIdSet");
        treatZoneIdSet = new Gson().fromJson(strZoneIdSet, new TypeToken<Set<Long>>() {
        }.getType());
        queryExportData();
        //
        binding.layoutLogo.setVisibility(StaticConstant.isNeutral ? View.GONE : View.VISIBLE);
        switch (StaticConstant.APP_LOGO_DEFAULT) {
            case StaticConstant.APP_LIERDA:
                binding.ivLogoLrd.setVisibility(View.VISIBLE);
                binding.ivLogoZhi.setVisibility(View.GONE);
                binding.vCenter.setVisibility(View.GONE);
                binding.ivLogoGjdw.setVisibility(View.GONE);
                binding.ivLogoMshz.setVisibility(View.GONE);
                binding.ivLogoGtj.setVisibility(View.GONE);
                break;
            case StaticConstant.APP_DIANKEYUAN:
                binding.ivLogoLrd.setVisibility(View.GONE);
                binding.ivLogoZhi.setVisibility(View.VISIBLE);
                binding.vCenter.setVisibility(View.VISIBLE);
                binding.ivLogoGjdw.setVisibility(View.VISIBLE);
                binding.ivLogoMshz.setVisibility(View.GONE);
                binding.ivLogoGtj.setVisibility(View.GONE);
                break;
            case StaticConstant.APP_MINGSHENGHENGZHUO:
                binding.ivLogoLrd.setVisibility(View.VISIBLE);
                binding.ivLogoZhi.setVisibility(View.GONE);
                binding.vCenter.setVisibility(View.VISIBLE);
                binding.ivLogoGjdw.setVisibility(View.GONE);
                binding.ivLogoMshz.setVisibility(View.VISIBLE);
                binding.ivLogoGtj.setVisibility(View.GONE);
                break;
            case StaticConstant.APP_GTJ:
                binding.ivLogoLrd.setVisibility(View.GONE);
                binding.ivLogoZhi.setVisibility(View.GONE);
                binding.vCenter.setVisibility(View.GONE);
                binding.ivLogoGjdw.setVisibility(View.GONE);
                binding.ivLogoMshz.setVisibility(View.GONE);
                binding.ivLogoGtj.setVisibility(View.VISIBLE);
                break;

        }
    }

    private void refreshDataView(ZoneBean zoneBean) {
        binding.tvProject.setText(zoneBean.projectName);
        binding.tvZone.setText(zoneBean.number);
        Rebar240ZoneBean rebar240ZoneBean = GreenDaoHelper.getDaoSession(this).getRebar240ZoneBeanDao().load(zoneBean.thicknessId);
        if (rebar240ZoneBean == null) {
            return;
        }
        binding.tvDiameterTop.setText("d: " + rebar240ZoneBean.poleTopDiameter + "mm");
        binding.tvDiameterBottom.setText("D: " + rebar240ZoneBean.poleBottomDiameter + "mm");
        binding.tvPoleHeight.setText("L: " + rebar240ZoneBean.poleHeight + "m");
        configGridTop(rebar240ZoneBean.poleBottomCount);
        refreshGrid(rebar240ZoneBean);
        showPoleLocation(rebar240ZoneBean);
    }

    private void showPoleLocation(Rebar240ZoneBean rebar240ZoneBean) {
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
                    treatLocation(binding.vMiddle1, binding.tvMiddle1, pageBeanList.get(0), rebar240ZoneBean, originalPoint, width, heightTop, heightBottom1);
                }
                if (pageBeanList.size() > 1) {
                    treatLocation(binding.vMiddle2, binding.tvMiddle2, pageBeanList.get(1), rebar240ZoneBean, originalPoint, width, heightTop1, heightBottom);
                }
                if (pageBeanList.size() > 2) {
                    treatLocation(binding.vMiddle3, binding.tvMiddle3, pageBeanList.get(2), rebar240ZoneBean, originalPoint, width, heightTop, heightBottom1);
                }
                if (pageBeanList.size() > 3) {
                    treatLocation(binding.vMiddle4, binding.tvMiddle4, pageBeanList.get(3), rebar240ZoneBean, originalPoint, width, heightTop1, heightBottom);
                }
                if (pageBeanList.size() > 4) {
                    treatLocation(binding.vMiddle5, binding.tvMiddle5, pageBeanList.get(4), rebar240ZoneBean, originalPoint, width, heightTop, heightBottom1);
                }
                if (pageBeanList.size() > 5) {
                    treatLocation(binding.vMiddle6, binding.tvMiddle6, pageBeanList.get(5), rebar240ZoneBean, originalPoint, width, heightTop1, heightBottom);
                }
                if (pageBeanList.size() > 6) {
                    treatLocation(binding.vMiddle7, binding.tvMiddle7, pageBeanList.get(6), rebar240ZoneBean, originalPoint, width, heightTop, heightBottom1);
                }
                if (pageBeanList.size() > 7) {
                    treatLocation(binding.vMiddle8, binding.tvMiddle8, pageBeanList.get(7), rebar240ZoneBean, originalPoint, width, heightTop1, heightBottom);
                }
                if (pageBeanList.size() > 8) {
                    treatLocation(binding.vMiddle9, binding.tvMiddle9, pageBeanList.get(8), rebar240ZoneBean, originalPoint, width, heightTop, heightBottom1);
                }
                if (pageBeanList.size() > 9) {
                    treatLocation(binding.vMiddle10, binding.tvMiddle10, pageBeanList.get(9), rebar240ZoneBean, originalPoint, width, heightTop1, heightBottom);
                }
            }
        });
    }

    private void treatLocation(View view, TextView textView, RebarPageBean pageBean, Rebar240ZoneBean rebar240ZoneBean, int originalPoint, int width, int heightTop, int heightBottom) {
        int location_x = (int) StringUtils.getInterpolationValue(0, originalPoint, Double.parseDouble(rebar240ZoneBean.poleHeight), originalPoint + width, pageBean.distance);
        int height = (int) StringUtils.getInterpolationValue(0, heightBottom, Double.parseDouble(rebar240ZoneBean.poleHeight), heightTop, pageBean.distance);
        RelativeLayout.LayoutParams middleLayoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        middleLayoutParams.leftMargin = location_x;
        middleLayoutParams.height = height;
        view.setLayoutParams(middleLayoutParams);
        textView.setText("H: " + pageBean.averageThickness);
        view.setVisibility(View.VISIBLE);
        textView.setVisibility(View.VISIBLE);
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
    private void refreshGrid(Rebar240ZoneBean rebar240ZoneBean) {
        averageMap.clear();
        binding.layoutGridContent.removeAllViews();
        int rebarPageListSize = rebar240ZoneBean.rebarPageList.size();
        if (rebarPageListSize > 0) {
            int lastBeanListSize = rebar240ZoneBean.rebarPageList.get(rebar240ZoneBean.rebarPageList.size() - 1).beanList.size();
            if (lastBeanListSize == 0) {
                rebarPageListSize -= 1;
            }
            for (int i = 0; i < rebarPageListSize; i++) {
                View itemView = LayoutInflater.from(this).inflate(R.layout.view_grid_result_content, null, false);
                configGrid(itemView, rebar240ZoneBean.poleBottomCount, rebar240ZoneBean.rebarPageList.get(i));
                itemView.setId(i);
                binding.layoutGridContent.addView(itemView);
            }
        }
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
        double total0 = 0, total1 = 0, average0, average1;
        double size = 0;
        if (!bean.beanList.isEmpty()) {
            for (RebarBean b : bean.beanList) {
                if (b.thickness > 0) {
                    total0 += b.thickness;
                    total1 += b.thickness1;
                    size++;
                }
            }
            average0 = StringUtils.getRounding(total0 / size, 2);
            average1 = StringUtils.getRounding(total1 / size, 2);
            average0 = StringUtils.getRounding(average0, 1);
            average1 = StringUtils.getRounding(average1, 1);
        } else {
            average0 = 0;
            average1 = 0;
        }
        tvAver.setText(average1 + "\n" + average0);
        if (TextUtils.equals("梢部", bean.segment)) {
            binding.tvTop.setText("H: " + average0);
            binding.tvTop.setVisibility(View.VISIBLE);
            binding.vTop.setVisibility(View.VISIBLE);
        } else if (TextUtils.equals("根部", bean.segment)) {
            binding.tvBottom.setText("H: " + average0);
            binding.tvBottom.setVisibility(View.VISIBLE);
            binding.vBottom.setVisibility(View.VISIBLE);
        } else {
            averageMap.put(bean.segment, bean);
        }
    }

    private void setTextView(TextView textView, RebarBean bean) {
        textView.setText(bean.thickness1 + "\n" + bean.thickness);
    }

    //---------------------导出--------------------------
    private void queryExportData() {
        progressiveDialog.setTvContent("处理中...").show();
        executorService.submit(() -> {
            //执行耗时操作
            doInBackgroundQueryExportData();
            //更新UI
            uiHandler.postDelayed(this::onPostExecuteQueryExportData, 500);
        });
    }

    private void doInBackgroundQueryExportData() {
        exportZoneList.clear();
        exportPathList.clear();
        //查询数据库ZoneBean数据
        for (Long aLong : treatZoneIdSet) {
            exportZoneList.add(GreenDaoHelper.getDaoSession(this).getZoneBeanDao().queryBuilder().where(ZoneBeanDao.Properties.Id.eq(aLong)).unique());
        }
        //统计项目名称
        Set<String> projectNameList = new HashSet<>();
        for (int i = 0; i < exportZoneList.size(); i++) {
            projectNameList.add(exportZoneList.get(i).projectName);
        }
        //删除项目文件夹
        for (String projectName : projectNameList) {
            BitmapUtils.clearFiles(StaticConstant.dataFilePathExport + projectName);
        }
        //导出数据
        for (ZoneBean zoneBean : exportZoneList) {
            if (zoneBean.f230ZoneBean == null && zoneBean.rebounderZoneBean == null && zoneBean.corrosionZoneBean == null && zoneBean.thicknessCount == 0) {
                exportPathList.add(zoneBean.number + ": 暂无可导出数据");
                continue;
            }
            //缝宽数据 导出
            if (zoneBean.f230ZoneBean != null && zoneBean.f230ZoneBean.zoneCount > 0) {
                String string = FileUtils.saveDataFile(zoneBean.projectName + "/" + zoneBean.number + "/", "缝宽检测数据.dat", DataOutputUtils.dataOutputToBytes_f230(zoneBean, viewRatio));
                zoneBean.f230Export = !TextUtils.isEmpty(string);
            } else {
                zoneBean.f230Export = true;
            }
            //回弹数据 导出
            if (zoneBean.rebounderZoneBean != null && zoneBean.rebounderZoneBean.zoneCount > 0) {
                try {
                    String string = FileUtils.saveDataFile(zoneBean.projectName + "/" + zoneBean.number + "/", "回弹检测数据.REC", DataOutputUtils.dataOutputToString(this, zoneBean).getBytes("GB2312"));
                    zoneBean.reboundExport = !TextUtils.isEmpty(string);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            } else {
                zoneBean.reboundExport = true;
            }
        }
    }

    private void onPostExecuteQueryExportData() {
        if (exportZoneList.isEmpty()) {
            progressiveDialog.dismiss();
            StyleAlertDialog styleAlertDialog = new StyleAlertDialog(this);
            styleAlertDialog.setContent("暂无可导出数据").setOKButton("确定", v -> {
                styleAlertDialog.dismiss();
                setResult(RESULT_OK);
                finish();
            }).show();
            return;
        }
        exportCount = exportZoneList.size();
        //锈蚀数据 导出
        treatExportCorrosionData();
    }

    private void treatExportCorrosionData() {
        int count = exportZoneList.size();
        if (count > 0) {
            int rate = (int) StringUtils.getRounding((exportCount - count) * 100.0 / exportCount, 0);
            progressiveDialog.setTvContent("处理中..." + rate + "%").show();
            exportCorrosionData(exportZoneList.get(exportZoneList.size() - 1));
        } else {
            progressiveDialog.setTvContent("处理中...100%").show();
            new Handler().postDelayed(() -> progressiveDialog.dismiss(), 1000);
            StringBuilder sbd = new StringBuilder("导出结果：\n");
            for (String s : exportPathList) {
                sbd.append(s).append("\n");
            }
            StyleAlertDialog1 styleAlertDialog = new StyleAlertDialog1(this);
            styleAlertDialog.setContent(sbd.toString()).setOKButton("确定", v -> {
                styleAlertDialog.dismiss();
                setResult(RESULT_OK);
                finish();
            }).show();
        }
    }

    private void exportCorrosionData(ZoneBean zoneBean) {
        Rebar240ZoneBean rebar240ZoneBean = GreenDaoHelper.getDaoSession(this).getRebar240ZoneBeanDao().load(zoneBean.thicknessId);
        String strExportString = null;
        if (rebar240ZoneBean != null) {
            strExportString = ExportUtils.getExportString(getString(R.string.app_name), zoneBean.projectName, rebar240ZoneBean);
        }
        if (!TextUtils.isEmpty(strExportString)) {
            refreshDataView(zoneBean);
            takePictureService(zoneBean, strExportString);
        } else {
            if (zoneBean.f230Export && zoneBean.reboundExport) {
                exportPathList.add(zoneBean.number + ": 导出成功");
            } else {
                exportPathList.add(zoneBean.number + ((!zoneBean.f230Export && !zoneBean.reboundExport) ? "：内存卡读写异常，无法导出" : !zoneBean.f230Export ? "缝宽数据导出失败" : "回弹数据导出失败"));
            }
            uiHandler.postDelayed(this::onPostExecuteTakePicture, 1500);
        }
    }

    private void takePictureService(ZoneBean zoneBean, String strExportString) {
        uiHandler.postDelayed(() -> doInBackgroundTakePicture(zoneBean, strExportString), 500);
        uiHandler.postDelayed(this::onPostExecuteTakePicture, 1500);
    }

    private void doInBackgroundTakePicture(ZoneBean zoneBean, String strExportString) {
        //生成快照
        Bitmap bitmapScrollView = BitmapUtils.scrollViewScreenShot(binding.scrollView);
        byte[] imageData = BitmapUtils.compressBitmap(bitmapScrollView);
        //快照保存到平板
        String imageFileName = StaticConstant.dataFilePathExport + zoneBean.projectName + "/" + zoneBean.number + "/钢筋保护层检测数据.jpg";
        ExportUtils.saveBitmapFile(this, imageFileName, imageData);
        //快照保存到USB
        String imageName = StaticConstant.dataFilePathExportUSb + zoneBean.projectName + "/" + zoneBean.number + "/钢筋保护层检测数据.jpg";
        saveDataToUsb(imageName, imageData);
        //
        //生成数据byte
        byte[] textData = null;
        try {
            textData = strExportString.getBytes("GBK");
        } catch (Exception ignored) {
        }
        if (textData != null) {
            //数据保存到平板
            String dataFileName = StaticConstant.dataFilePathExport + zoneBean.projectName + "/" + zoneBean.number + "/钢筋保护层检测数据.csv";
            String strFile = ExportUtils.saveDataFile(dataFileName, textData);
            if (!TextUtils.isEmpty(strFile)) {
                exportPathList.add(zoneBean.number + ": 导出成功");
            } else {
                exportPathList.add(zoneBean.number + ": 内存卡读写异常，无法导出");
            }
            //数据保存到USB
            String dataName = StaticConstant.dataFilePathExportUSb + zoneBean.projectName + "/" + zoneBean.number + "/钢筋保护层检测数据.csv";
            saveDataToUsb(dataName, textData);
        }
    }

    private void onPostExecuteTakePicture() {
        exportZoneList.remove(exportZoneList.size() - 1);
        treatExportCorrosionData();
    }

    //---------------------USB导出--------------------------
    private void saveDataToUsb(String fileName, byte[] data) {
        if (StaticConstant.usbHelper == null || StaticConstant.usbHelper.rootFolder == null) {
            return;
        }
        String strFilePath = StaticConstant.usbHelper.saveDataToUsb(fileName, data);
        Logger.d(strFilePath);
    }
}

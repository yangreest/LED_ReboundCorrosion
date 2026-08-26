package com.example.gtj_f230_rebound_corrosion.base.activity.data;

import static android.view.View.INVISIBLE;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.SizeUtils;
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
import com.example.gtj_f230_rebound_corrosion.corrosion.model.CorrosionBean;
import com.example.gtj_f230_rebound_corrosion.databinding.ActPrintExportDataBinding;
import com.example.gtj_f230_rebound_corrosion.databinding.ViewGridContentBinding;
import com.example.gtj_f230_rebound_corrosion.functions.database.greenDao.db.ZoneBeanDao;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.logger.Logger;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressLint({"SetTextI18n", "InflateParams"})
public class DataExportDataActivity extends BaseActivity<ActPrintExportDataBinding> {
    private Set<Long> treatZoneIdSet = new HashSet<>();
    private final List<String> exportPathList = new ArrayList<>();
    private final List<ZoneBean> exportZoneList = new ArrayList<>();
    private int exportCount;
    private float viewRatio;

    @Override
    protected ActPrintExportDataBinding getBinding() {
        return ActPrintExportDataBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initLabel() {

    }

    @Override
    protected void initView() {
        binding.navigationBar.setTitleText("数据导出");
        int fullWidth = SizeUtils.dp2px(656);
        viewRatio = fullWidth / 640.0f;

        String strZoneIdSet = getIntent().getStringExtra("treatZoneIdSet");
        treatZoneIdSet = new Gson().fromJson(strZoneIdSet, new TypeToken<Set<Long>>() {
        }.getType());
        queryExportData();
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
        //删除项目文件夹
        for (int i = 0; i < exportZoneList.size(); i++) {
            BitmapUtils.clearFiles(StaticConstant.dataFilePathExport + exportZoneList.get(i).projectName);
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
            int rate = (int) StringUtils.getRounding((double) ((exportCount - count) * 100) / exportCount, 0);
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
                finish();
            }).show();
        }
    }

    private void exportCorrosionData(ZoneBean zoneBean) {
        String strExportString = ExportUtils.getExportString(zoneBean);
        if (!TextUtils.isEmpty(strExportString)) {
            refreshDataView(zoneBean);
            takePictureService(zoneBean, strExportString);
        } else {
            if (zoneBean.f230Export && zoneBean.reboundExport) {
                exportPathList.add(zoneBean.number + ": 导出成功");
            } else {
                exportPathList.add(zoneBean.number + ((!zoneBean.f230Export && !zoneBean.reboundExport) ? "：内存卡读写异常，无法导出" : !zoneBean.f230Export ? "缝宽数据导出失败" : "回弹数据导出失败"));
            }
            uiHandler.postDelayed(this::onPostExecuteTakePicture, 1000);
        }
    }

    private void takePictureService(ZoneBean zoneBean, String strExportString) {
        uiHandler.postDelayed(() -> doInBackgroundTakePicture(zoneBean, strExportString), 500);
        uiHandler.postDelayed(this::onPostExecuteTakePicture, 1500);
    }

    private void doInBackgroundTakePicture(ZoneBean zoneBean, String strExportString) {
        //生成快照
        Bitmap bitmapScrollView = BitmapUtils.scrollViewScreenShot(binding.scrollViewCorrosion);
        byte[] imageData = BitmapUtils.compressBitmap(bitmapScrollView);
        //快照保存到平板
        String imageFileName = StaticConstant.dataFilePathExport + zoneBean.projectName + "/" + zoneBean.number + "/锈蚀检测数据.jpg";
        ExportUtils.saveBitmapFile(this, imageFileName, imageData);
        //快照保存到USB
        String imageName = StaticConstant.dataFilePathExportUSb + zoneBean.projectName + "/" + zoneBean.number + "/锈蚀检测数据.jpg";
        saveDataToUsb(imageName, imageData);
        //
        //生成数据byte
        byte[] textData = null;
        try {
            textData = strExportString.getBytes("GBK");
        } catch (UnsupportedEncodingException e) {
            Logger.e("Exception", e);
        }
        if (textData != null) {
            //数据保存到平板
            String dataFileName = StaticConstant.dataFilePathExport + zoneBean.projectName + "/" + zoneBean.number + "/锈蚀检测数据.csv";
            String strFile = ExportUtils.saveDataFile(dataFileName, textData);
            if (!TextUtils.isEmpty(strFile) && zoneBean.f230Export && zoneBean.reboundExport) {
                exportPathList.add(zoneBean.number + ": 导出成功");
            } else {
                exportPathList.add(zoneBean.number + (!zoneBean.f230Export && !zoneBean.reboundExport && !TextUtils.isEmpty(strFile) ? ": 内存卡读写异常，无法导出" : !zoneBean.f230Export ? "缝宽数据导出失败" : !zoneBean.reboundExport ? "回弹数据导出失败" : "锈蚀数据导出失败"));
            }
            //数据保存到USB
            String dataName = StaticConstant.dataFilePathExportUSb + zoneBean.projectName + "/" + zoneBean.number + "/锈蚀检测数据.csv";
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

    //--------------------锈蚀数据---------------------------
    private void refreshDataView(ZoneBean zoneBean) {
        binding.tvProject.setText(zoneBean.projectName);
        binding.tvZone.setText(zoneBean.number);
        binding.tvDiameterTop.setText("d: " + zoneBean.corrosionZoneBean.poleTopDiameter + "mm");
        binding.tvDiameterBottom.setText("D: " + zoneBean.corrosionZoneBean.poleBottomDiameter + "mm");
        binding.tvPoleHeight.setText("L: " + zoneBean.corrosionZoneBean.poleHeight + "m");
        BigDecimal average = BigDecimal.ZERO;
        if (!TextUtils.isEmpty(zoneBean.corrosionZoneBean.strZoneList)) {
            BigDecimal corrosion = BigDecimal.ZERO;
            List<CorrosionBean> corrosionBeanList = new Gson().fromJson(zoneBean.corrosionZoneBean.strZoneList, new TypeToken<List<CorrosionBean>>() {
            }.getType());
            for (CorrosionBean bean : corrosionBeanList) {
                corrosion = corrosion.add(new BigDecimal(bean.corrosion));
            }
            average = corrosion.divide(new BigDecimal(corrosionBeanList.size()), 2, RoundingMode.HALF_UP);
        }
        binding.tvBottom.setText("锈蚀度: " + average + "%");
        refreshGrid(zoneBean);
        showPoleLocation(zoneBean);
    }

    private void showPoleLocation(ZoneBean zoneBean) {
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
            if (!TextUtils.isEmpty(zoneBean.corrosionZoneBean.strZoneList)) {
                List<CorrosionBean> corrosionBeanList = new Gson().fromJson(zoneBean.corrosionZoneBean.strZoneList, new TypeToken<List<CorrosionBean>>() {
                }.getType());
                if (!corrosionBeanList.isEmpty() && checkLength(corrosionBeanList.get(0).distance, zoneBean)) {
                    treatLocation(zoneBean, binding.vMiddle1, binding.tvMiddle1, corrosionBeanList.get(0), originalPoint, width, heightTop, heightBottom1);
                } else {
                    binding.vMiddle1.setVisibility(INVISIBLE);
                    binding.tvMiddle1.setVisibility(INVISIBLE);
                }
                if (corrosionBeanList.size() > 1 && checkLength(corrosionBeanList.get(1).distance, zoneBean)) {
                    treatLocation(zoneBean, binding.vMiddle2, binding.tvMiddle2, corrosionBeanList.get(1), originalPoint, width, heightTop1, heightBottom);
                } else {
                    binding.vMiddle2.setVisibility(INVISIBLE);
                    binding.tvMiddle2.setVisibility(INVISIBLE);
                }
                if (corrosionBeanList.size() > 2 && checkLength(corrosionBeanList.get(2).distance, zoneBean)) {
                    treatLocation(zoneBean, binding.vMiddle3, binding.tvMiddle3, corrosionBeanList.get(2), originalPoint, width, heightTop, heightBottom1);
                } else {
                    binding.vMiddle3.setVisibility(INVISIBLE);
                    binding.tvMiddle3.setVisibility(INVISIBLE);
                }
                if (corrosionBeanList.size() > 3 && checkLength(corrosionBeanList.get(3).distance, zoneBean)) {
                    treatLocation(zoneBean, binding.vMiddle4, binding.tvMiddle4, corrosionBeanList.get(3), originalPoint, width, heightTop1, heightBottom);
                } else {
                    binding.vMiddle4.setVisibility(INVISIBLE);
                    binding.tvMiddle4.setVisibility(INVISIBLE);
                }
                if (corrosionBeanList.size() > 4 && checkLength(corrosionBeanList.get(4).distance, zoneBean)) {
                    treatLocation(zoneBean, binding.vMiddle5, binding.tvMiddle5, corrosionBeanList.get(4), originalPoint, width, heightTop, heightBottom1);
                } else {
                    binding.vMiddle5.setVisibility(INVISIBLE);
                    binding.tvMiddle5.setVisibility(INVISIBLE);
                }
                if (corrosionBeanList.size() > 5 && checkLength(corrosionBeanList.get(5).distance, zoneBean)) {
                    treatLocation(zoneBean, binding.vMiddle6, binding.tvMiddle6, corrosionBeanList.get(5), originalPoint, width, heightTop1, heightBottom);
                } else {
                    binding.vMiddle6.setVisibility(INVISIBLE);
                    binding.tvMiddle6.setVisibility(INVISIBLE);
                }
                if (corrosionBeanList.size() > 6 && checkLength(corrosionBeanList.get(6).distance, zoneBean)) {
                    treatLocation(zoneBean, binding.vMiddle7, binding.tvMiddle7, corrosionBeanList.get(6), originalPoint, width, heightTop, heightBottom1);
                } else {
                    binding.vMiddle7.setVisibility(INVISIBLE);
                    binding.tvMiddle7.setVisibility(INVISIBLE);
                }
                if (corrosionBeanList.size() > 7 && checkLength(corrosionBeanList.get(7).distance, zoneBean)) {
                    treatLocation(zoneBean, binding.vMiddle8, binding.tvMiddle8, corrosionBeanList.get(7), originalPoint, width, heightTop1, heightBottom);
                } else {
                    binding.vMiddle8.setVisibility(INVISIBLE);
                    binding.tvMiddle8.setVisibility(INVISIBLE);
                }
                if (corrosionBeanList.size() > 8 && checkLength(corrosionBeanList.get(8).distance, zoneBean)) {
                    treatLocation(zoneBean, binding.vMiddle9, binding.tvMiddle9, corrosionBeanList.get(8), originalPoint, width, heightTop, heightBottom1);
                } else {
                    binding.vMiddle9.setVisibility(INVISIBLE);
                    binding.tvMiddle9.setVisibility(INVISIBLE);
                }
                if (corrosionBeanList.size() > 9 && checkLength(corrosionBeanList.get(9).distance, zoneBean)) {
                    treatLocation(zoneBean, binding.vMiddle10, binding.tvMiddle10, corrosionBeanList.get(9), originalPoint, width, heightTop1, heightBottom);
                } else {
                    binding.vMiddle10.setVisibility(INVISIBLE);
                    binding.tvMiddle10.setVisibility(INVISIBLE);
                }
            }
        });
    }

    private boolean checkLength(String distance, ZoneBean zoneBean) {
        if (TextUtils.isEmpty(distance) || zoneBean == null || TextUtils.isEmpty(zoneBean.corrosionZoneBean.poleHeight)) {
            return false;
        }
        int distanceInt = (int) Double.parseDouble(distance);
        int poleHeight = (int) Double.parseDouble(zoneBean.corrosionZoneBean.poleHeight);
        return distanceInt <= poleHeight;
    }

    private void treatLocation(ZoneBean zoneBean, View view, TextView textView, CorrosionBean pageBean, int originalPoint, int width, int heightTop, int heightBottom) {
        int location_x = (int) StringUtils.getInterpolationValue(0.0001, originalPoint, Double.parseDouble(zoneBean.corrosionZoneBean.poleHeight), originalPoint + width, Double.parseDouble(pageBean.distance));
        int height = (int) StringUtils.getInterpolationValue(0.0001, heightBottom, Double.parseDouble(zoneBean.corrosionZoneBean.poleHeight), heightTop, Double.parseDouble(pageBean.distance));
        RelativeLayout.LayoutParams middleLayoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        middleLayoutParams.leftMargin = location_x;
        middleLayoutParams.height = height;
        view.setLayoutParams(middleLayoutParams);
        textView.setText("锈蚀度: " + pageBean.corrosion + "%");
        view.setVisibility(View.VISIBLE);
        textView.setVisibility(View.VISIBLE);
    }

    /**
     * 刷新钢筋列表
     */
    private void refreshGrid(ZoneBean zoneBean) {
        binding.layoutGridContent.removeAllViews();
        if (!TextUtils.isEmpty(zoneBean.corrosionZoneBean.strZoneList)) {
            List<CorrosionBean> corrosionBeanList = new Gson().fromJson(zoneBean.corrosionZoneBean.strZoneList, new TypeToken<List<CorrosionBean>>() {
            }.getType());
            for (int i = 0; i < corrosionBeanList.size(); i++) {
                ViewGridContentBinding gridContentBinding = ViewGridContentBinding.inflate(getLayoutInflater());
                View itemView = gridContentBinding.getRoot();
                if (corrosionBeanList.size() > i) {
                    configGrid(gridContentBinding, i, corrosionBeanList.get(i));
                    itemView.setId(i);
                }
                binding.layoutGridContent.addView(gridContentBinding.getRoot());
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void configGrid(ViewGridContentBinding gridContentBinding, int position, CorrosionBean corrosionBean) {
        gridContentBinding.tvGridNumber.setText(position + 1 + "");
        gridContentBinding.tvGrid1.setText(corrosionBean.distance);
        gridContentBinding.tvGrid2.setText(corrosionBean.diameter);
        gridContentBinding.tvGrid3.setText(corrosionBean.corrosion);
    }

}

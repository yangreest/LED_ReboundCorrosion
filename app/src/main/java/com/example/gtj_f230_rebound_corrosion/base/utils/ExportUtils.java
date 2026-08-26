package com.example.gtj_f230_rebound_corrosion.base.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import androidx.core.content.FileProvider;

import com.blankj.utilcode.util.AppUtils;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.base.model.ZoneBean;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.CorrosionBean;
import com.example.gtj_f230_rebound_corrosion.thickness.model.Rebar240ZoneBean;
import com.example.gtj_f230_rebound_corrosion.thickness.model.RebarPageBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ExportUtils {

    /**
     * 生成导出数据
     */
    public static String getExportString(ZoneBean zoneBean) {
        if (zoneBean == null || zoneBean.corrosionZoneBean == null || TextUtils.isEmpty(zoneBean.corrosionZoneBean.strZoneList)) {
            return null;
        }
        StringBuilder sbd = new StringBuilder();
        sbd.append(StaticConstant.appName).append(" 检测记录");
        sbd.append("\n工程信息").append("\n工程名称：,").append(zoneBean.projectName).append("\n电杆编号：,").append(zoneBean.number).append("\n电杆类型：,").append(StaticConstant.poleTypeList.get(zoneBean.corrosionZoneBean.poleType)).append("\n适用规范：,").append(StaticConstant.poleStandardList.get(zoneBean.corrosionZoneBean.poleStandard));
        sbd.append("\n试验参数").append("\n电杆长度：,").append(zoneBean.corrosionZoneBean.poleHeight).append("m").append("\n电杆根径：,").append(zoneBean.corrosionZoneBean.poleBottomDiameter).append("mm").append(",电杆梢径：,").append(zoneBean.corrosionZoneBean.poleTopDiameter).append("mm");
        sbd.append("\n主筋锈蚀度检测\n");
        sbd.append("测点数量,距离根部(m),实测直径(mm),纵筋锈蚀度(%)\n");
        List<CorrosionBean> corrosionBeanList = new Gson().fromJson(zoneBean.corrosionZoneBean.strZoneList, new TypeToken<List<CorrosionBean>>() {
        }.getType());
        for (int i = 0; i < corrosionBeanList.size(); i++) {
            sbd.append(i + 1).append(",").append(corrosionBeanList.get(i).distance).append(",").append(corrosionBeanList.get(i).diameter).append(",").append(corrosionBeanList.get(i).corrosion).append("\n");
        }
        return sbd.toString();
    }

    /**
     * 保存图片到本地
     *
     * @param context：context
     * @param data：流
     * @return 本地图片地址
     */
    public static String saveBitmapFile(Context context, String fileName, byte[] data) {
        String path = createFilePath(fileName);
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        Uri imageFileUri = FileProvider.getUriForFile(context, AppUtils.getAppPackageName() + ".fileProvider", new File(path));
        String filePath = null;
        try {
            OutputStream imageFileOS = context.getContentResolver().openOutputStream(imageFileUri);
            if (imageFileOS != null) {
                imageFileOS.write(data);
                imageFileOS.flush();
                imageFileOS.close();
                filePath = path;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filePath;
    }

    /**
     * 保存文件到本地
     */
    public static String saveDataFile(String fileName, byte[] data) {
        String path = createFilePath(fileName);
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        String filePath = null;
        try {
            OutputStream fos = new FileOutputStream(fileName);
            fos.write(data);
            fos.flush();
            fos.close();
            filePath = new File(fileName).getPath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filePath;
    }

    private static String createFilePath(String fileName) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dir = new File(fileName.substring(0, fileName.lastIndexOf("/")));
            if (!dir.exists()) {
                Logger.d(dir.mkdirs());
            }
            File file = new File(fileName);
            if (file.exists()) {
                Logger.d(file.delete());
            }
            return fileName;
        }
        return null;
    }

    /**
     * 生成导出数据
     */
    public static String getExportString(String appName, String projectName, Rebar240ZoneBean zoneBean) {
        if (zoneBean.rebarPageList.size() == 0) {
            return null;
        }
        StringBuilder sbd = new StringBuilder();
        sbd.append(appName).append("检测记录");
        sbd.append("\n工程信息").append("\n工程名称：,").append(projectName).append("\n电杆编号：,").append(zoneBean.poleNo).append("\n适用规范：,").append(StaticConstant.poleStandardList.get(zoneBean.poleStandard));
        sbd.append("\n试验参数").append("\n电杆类型：,").append(StaticConstant.configPoleTypeList.get(zoneBean.poleType)).append("\n电杆结构：,").append(StaticConstant.configpoleStructureList.get(zoneBean.poleStructure)).append("\n电杆长度：,").append(zoneBean.poleHeight).append("m").append("\n电杆根径：,").append(zoneBean.poleBottomDiameter).append("mm").append(",电杆梢径：,").append(zoneBean.poleTopDiameter).append("mm").append("\n纵筋直径：,").append(StaticConstant.rebarDiameterList.get(zoneBean.rebarDiameter)).append("mm").append(",螺旋筋直径：,").append(zoneBean.hoopingDiameter).append("mm").append("\n设计保护层厚度：,").append(zoneBean.designThickness).append("（+8 -2）mm");
        sbd.append("\n保护层厚度（单位：mm）");
        StringBuilder strCount = new StringBuilder();
        for (int i = 1; i <= zoneBean.poleBottomCount; i++) {
            strCount.append(i).append(",");
        }
        strCount.append("平均厚度\n");
        sbd.append("\n位置|数量,").append(strCount);
        for (RebarPageBean pageBean : zoneBean.rebarPageList) {
            StringBuilder string = new StringBuilder(pageBean.segment + ",");
            StringBuilder string1 = new StringBuilder(pageBean.segment + ",");
            for (int i = 0; i < zoneBean.poleBottomCount; i++) {
                if (i < pageBean.beanList.size()) {
                    string.append(pageBean.beanList.get(i).thickness);
                    string1.append(pageBean.beanList.get(i).thickness1);
                }
                string.append(",");
                string1.append(",");
            }
            string.append(pageBean.averageThickness).append("\n");
            string1.append(pageBean.averageThickness1).append("\n");
            sbd.append(string1);
            sbd.append(string);
        }
        return sbd.toString();
    }
}

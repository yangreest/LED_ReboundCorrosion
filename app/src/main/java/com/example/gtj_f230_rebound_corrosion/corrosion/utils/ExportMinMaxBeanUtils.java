package com.example.gtj_f230_rebound_corrosion.corrosion.utils;

import android.content.Context;

import com.example.gtj_f230_rebound_corrosion.base.utils.StringUtils;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.RebarMinMaxBean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ExportMinMaxBeanUtils {
    public static String toString(List<RebarMinMaxBean> list) {
        StringBuilder sbd = new StringBuilder();
        if (!list.isEmpty()) {
            for (RebarMinMaxBean bean : list) {
                if (bean.signalMin != null && bean.signalMin.length == 40) {
                    StringBuilder signalMin = new StringBuilder();
                    StringBuilder signalMax = new StringBuilder();
                    for (int i = 0; i < bean.signalMin.length; i++) {
                        signalMin.append(StringUtils.getRoundingString(bean.signalMin[i], 1)).append(",");
                        signalMax.append(StringUtils.getRoundingString(bean.signalMax[i], 1)).append(",");
                    }
                    sbd.append(bean.rebarDiameter).append(",").append(bean.poleDiameter).append(",").append(bean.rebarSpacingMin).append(",").append(bean.rebarSpacingMax).append(",").append(bean.spiralSpacing).append(",");
                    sbd.append(signalMin.substring(0, signalMin.length() - 1)).append(",");
                    sbd.append(signalMax.substring(0, signalMax.length() - 1)).append("\n");
                }
            }
        }
        return sbd.toString();
    }

    public static List<RebarMinMaxBean> getRebarMinMaxBeanList(Context context, String fileName) {
        InputStreamReader is = null;
        try {
            is = new InputStreamReader(context.getAssets().open(fileName));
        } catch (IOException ignored) {
        }
        if (is != null) {
            return getCarbonizeArray(is);
        }
        return null;
    }

    private static List<RebarMinMaxBean> getCarbonizeArray(InputStreamReader is) {
        List<RebarMinMaxBean> beanList = new ArrayList<>();
        BufferedReader br = new BufferedReader(is);// 读取文件
        try {
            String line;
            String[] sp;
            while ((line = br.readLine()) != null) {
                sp = line.split(",");
                if (sp.length >= 85) {
                    double[] signalMin = new double[40];
                    double[] signalMax = new double[40];
                    for (int i = 0; i < signalMin.length; i++) {
                        signalMin[i] = Double.parseDouble(sp[i + 5].trim());
                    }
                    for (int i = 0; i < signalMax.length; i++) {
                        signalMax[i] = Double.parseDouble(sp[i + 45].trim());
                    }
                    beanList.add(new RebarMinMaxBean(sp[0], sp[1], sp[2], sp[3], sp[4], signalMin, signalMax));
                }
            }
        } catch (Exception ignored) {
        }
        return beanList;
    }
}

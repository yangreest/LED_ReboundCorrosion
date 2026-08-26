package com.example.gtj_f230_rebound_corrosion.corrosion.utils;

import android.content.Context;

import com.example.gtj_f230_rebound_corrosion.corrosion.model.StandardCurveBean;
import com.example.gtj_f230_rebound_corrosion.functions.database.greenDao.db.StandardCurveBeanDao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CurveDataUtils {

    /**
     * 生成导出数据
     *
     * @param diameter ：直径
     */
    public static String queryCurveListString(StandardCurveBeanDao curveDao, String diameter) {
        String[] array = diameter.split("-");
        List<StandardCurveBean> curveBeanList = curveDao.queryBuilder().where(StandardCurveBeanDao.Properties.RebarDiameter.eq(array[0]), StandardCurveBeanDao.Properties.PoleDiameter.eq(array[1])).orderAsc(StandardCurveBeanDao.Properties.Thickness).list();
        StringBuilder sbd = new StringBuilder();
        if (!curveBeanList.isEmpty()) {
            sbd.append("钢筋直径：").append(diameter).append(" 曲线表\n");
            for (StandardCurveBean curveBean : curveBeanList) {
                if (curveBean.signalArray != null && curveBean.signalArray.length >= 61) {
                    StringBuilder sbdSignal = new StringBuilder();
                    for (double signal : curveBean.signalArray) {
                        sbdSignal.append(signal).append(",");
                    }
                    sbd.append(curveBean.thickness).append(",").append(sbdSignal.substring(0, sbdSignal.length() - 1)).append("\n");
                }
            }
        }
        return sbd.toString();
    }

    public static List<StandardCurveBean> getCurveList(Context context, String rebarDiameter, String poleDiameter, String fileName) {
        InputStreamReader is = null;
        try {
            is = new InputStreamReader(context.getAssets().open(fileName));
        } catch (IOException ignored) {
        }
        if (is != null) {
            return getCarbonizeArray(is, rebarDiameter, poleDiameter, 61);
        }
        return null;
    }

    private static List<StandardCurveBean> getCarbonizeArray(InputStreamReader is, String rebarDiameter, String poleDiameter, int signalLength) {
        List<StandardCurveBean> standardCurveBeanList = new ArrayList<>();
        BufferedReader br = new BufferedReader(is);// 读取文件
        try {
            String line;
            String[] sp;
            while ((line = br.readLine()) != null) {
                sp = line.split(",");
                if (sp.length >= signalLength) {
                    double[] signalArray = new double[signalLength];
                    for (int i = 0; i < signalLength; i++) {
                        signalArray[i] = Integer.parseInt(sp[i + 1].trim());
                    }
                    standardCurveBeanList.add(new StandardCurveBean(rebarDiameter, poleDiameter, "", "", Double.parseDouble(sp[0].trim()), signalArray));
                }
            }
            //空值为错误格式数据，返回null
            if (standardCurveBeanList.isEmpty()) {
                return null;
            }
        } catch (Exception ignored) {
        }
        return standardCurveBeanList;
    }
}

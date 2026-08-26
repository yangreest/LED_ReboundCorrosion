package com.example.gtj_f230_rebound_corrosion.corrosion.utils;

import android.content.Context;

import com.example.gtj_f230_rebound_corrosion.corrosion.model.CurveMinMaxBean;
import com.example.gtj_f230_rebound_corrosion.functions.database.greenDao.db.CurveMinMaxBeanDao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CurveDataMinMaxUtils {

    /**
     * 生成导出数据
     */
    public static String queryCurveListString(CurveMinMaxBeanDao curveDao) {
        List<CurveMinMaxBean> curveBeanList = new ArrayList<>();
        List<CurveMinMaxBean> curveList7 = curveDao.queryBuilder().where(CurveMinMaxBeanDao.Properties.RebarDiameter.eq("7")).orderAsc(CurveMinMaxBeanDao.Properties.Thickness).list();
        List<CurveMinMaxBean> curveList9 = curveDao.queryBuilder().where(CurveMinMaxBeanDao.Properties.RebarDiameter.eq("9")).orderAsc(CurveMinMaxBeanDao.Properties.Thickness).list();
        List<CurveMinMaxBean> curveList10 = curveDao.queryBuilder().where(CurveMinMaxBeanDao.Properties.RebarDiameter.eq("10")).orderAsc(CurveMinMaxBeanDao.Properties.Thickness).list();
        List<CurveMinMaxBean> curveList11 = curveDao.queryBuilder().where(CurveMinMaxBeanDao.Properties.RebarDiameter.eq("11")).orderAsc(CurveMinMaxBeanDao.Properties.Thickness).list();
        List<CurveMinMaxBean> curveList14 = curveDao.queryBuilder().where(CurveMinMaxBeanDao.Properties.RebarDiameter.eq("14")).orderAsc(CurveMinMaxBeanDao.Properties.Thickness).list();
        //adapter
        curveBeanList.addAll(curveList7);
        curveBeanList.addAll(curveList9);
        curveBeanList.addAll(curveList10);
        curveBeanList.addAll(curveList11);
        curveBeanList.addAll(curveList14);
        StringBuilder sbd = new StringBuilder();
        if (!curveBeanList.isEmpty()) {
            sbd.append("钢筋直径最小值最大值").append(" 曲线表\n");
            for (CurveMinMaxBean curveBean : curveBeanList) {
                sbd.append(curveBean.rebarDiameter).append(",").append(curveBean.thickness).append(",").append(curveBean.signalMin).append(",").append(curveBean.signalMax).append("\n");
            }
        }
        return sbd.toString();
    }

    public static List<CurveMinMaxBean> getCurveMinMaxList(Context context, String fileName) {
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

    private static List<CurveMinMaxBean> getCarbonizeArray(InputStreamReader is) {
        List<CurveMinMaxBean> curveMinMaxList = new ArrayList<>();
        BufferedReader br = new BufferedReader(is);// 读取文件
        try {
            String line;
            String[] sp;
            while ((line = br.readLine()) != null) {
                sp = line.split(",");
                if (sp.length >= 4) {
                    String[] array = new String[4];
                    for (int i = 0; i < 4; i++) {
                        array[i] = sp[i].trim();
                    }
                    curveMinMaxList.add(new CurveMinMaxBean(array[0], array[1], array[2], array[3]));
                }
            }
            //空值为错误格式数据，返回null
            if (curveMinMaxList.isEmpty()) {
                return null;
            }
        } catch (Exception ignored) {
        }
        return curveMinMaxList;
    }
}

package com.example.gtj_f230_rebound_corrosion.corrosion.storage;

import com.example.gtj_f230_rebound_corrosion.base.utils.StringUtils;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.CheckDataBean;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.StandardCurveBean;
import com.example.gtj_f230_rebound_corrosion.corrosion.utils.CubicSplineInterpolationUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CorrosionCurve {
    public static List<String> poleNormList = Arrays.asList("7", "8", "9", "10", "11", "14", "16");  //12
    public static List<Integer> poleNormList1 = Arrays.asList(7, 8, 9, 10, 11, 14, 16);  //12
    //钢筋直径7
    public static List<StandardCurveBean> curve_7_200 = new ArrayList<>();
    public static List<StandardCurveBean> curve_7_250 = new ArrayList<>();
    public static List<StandardCurveBean> curve_7_310 = new ArrayList<>();
    public static List<StandardCurveBean> curve_7_350 = new ArrayList<>();
    public static List<StandardCurveBean> curve_7_400 = new ArrayList<>();
    public static List<StandardCurveBean> curve_7_450 = new ArrayList<>();
    public static List<StandardCurveBean> curve_7_500 = new ArrayList<>();
    //钢筋直径9
    public static List<StandardCurveBean> curve_9_200 = new ArrayList<>();
    public static List<StandardCurveBean> curve_9_250 = new ArrayList<>();
    public static List<StandardCurveBean> curve_9_310 = new ArrayList<>();
    public static List<StandardCurveBean> curve_9_350 = new ArrayList<>();
    public static List<StandardCurveBean> curve_9_400 = new ArrayList<>();
    public static List<StandardCurveBean> curve_9_450 = new ArrayList<>();
    public static List<StandardCurveBean> curve_9_500 = new ArrayList<>();
    //钢筋直径10
    public static List<StandardCurveBean> curve_10_200 = new ArrayList<>();
    public static List<StandardCurveBean> curve_10_250 = new ArrayList<>();
    public static List<StandardCurveBean> curve_10_310 = new ArrayList<>();
    public static List<StandardCurveBean> curve_10_350 = new ArrayList<>();
    public static List<StandardCurveBean> curve_10_400 = new ArrayList<>();
    public static List<StandardCurveBean> curve_10_450 = new ArrayList<>();
    public static List<StandardCurveBean> curve_10_500 = new ArrayList<>();
    //钢筋直径11
    public static List<StandardCurveBean> curve_11_200 = new ArrayList<>();
    public static List<StandardCurveBean> curve_11_250 = new ArrayList<>();
    public static List<StandardCurveBean> curve_11_310 = new ArrayList<>();
    public static List<StandardCurveBean> curve_11_350 = new ArrayList<>();
    public static List<StandardCurveBean> curve_11_400 = new ArrayList<>();
    public static List<StandardCurveBean> curve_11_450 = new ArrayList<>();
    public static List<StandardCurveBean> curve_11_500 = new ArrayList<>();
    //钢筋直径12
    public static List<StandardCurveBean> curve_12_200 = new ArrayList<>();
    public static List<StandardCurveBean> curve_12_250 = new ArrayList<>();
    public static List<StandardCurveBean> curve_12_310 = new ArrayList<>();
    public static List<StandardCurveBean> curve_12_350 = new ArrayList<>();
    public static List<StandardCurveBean> curve_12_400 = new ArrayList<>();
    public static List<StandardCurveBean> curve_12_450 = new ArrayList<>();
    public static List<StandardCurveBean> curve_12_500 = new ArrayList<>();
    //钢筋直径14
    public static List<StandardCurveBean> curve_14_200 = new ArrayList<>();
    public static List<StandardCurveBean> curve_14_250 = new ArrayList<>();
    public static List<StandardCurveBean> curve_14_310 = new ArrayList<>();
    public static List<StandardCurveBean> curve_14_350 = new ArrayList<>();
    public static List<StandardCurveBean> curve_14_400 = new ArrayList<>();
    public static List<StandardCurveBean> curve_14_450 = new ArrayList<>();
    public static List<StandardCurveBean> curve_14_500 = new ArrayList<>();
    //钢筋直径16
    public static List<StandardCurveBean> curve_16_200 = new ArrayList<>();
    public static List<StandardCurveBean> curve_16_250 = new ArrayList<>();
    public static List<StandardCurveBean> curve_16_310 = new ArrayList<>();
    public static List<StandardCurveBean> curve_16_350 = new ArrayList<>();
    public static List<StandardCurveBean> curve_16_400 = new ArrayList<>();
    public static List<StandardCurveBean> curve_16_450 = new ArrayList<>();
    public static List<StandardCurveBean> curve_16_500 = new ArrayList<>();
    //
    public static List<CheckDataBean> curveBeanList = new ArrayList<>();
    public static int[] checkData;

    public static CheckDataBean getSuitableIndex(String poleNorm, CheckDataBean maxBean, List<StandardCurveBean> dataList) {
        int index = 0;
        double y = 0;
        double minValue = 100000d;
        double leftMin = 100000d, rightMin = 100000d;
        int size = maxBean.signalMovAve.length;
        for (int i = 0; i < dataList.size(); i++) {
            double max = dataList.get(i).signalMovAve[size / 2];
            double min = Math.abs(maxBean.y - max);
            if (minValue > min) {
                minValue = min;
                index = i;
                y = max;
                leftMin = Math.abs(maxBean.signalMovAve[0] - dataList.get(i).signalMovAve[0]);
                rightMin = Math.abs(maxBean.signalMovAve[size - 1] - dataList.get(i).signalMovAve[size - 1]);

            }
        }
        CheckDataBean dataBean = new CheckDataBean(poleNorm, index, size / 2, StringUtils.getRounding(y, 3));
        dataBean.signalMovAve = dataList.get(index).signalMovAve;
        dataBean.thickness = dataList.get(index).thickness;
        dataBean.topMin = StringUtils.getRounding(minValue, 3);
        dataBean.leftMin = StringUtils.getRounding(leftMin, 3);
        dataBean.rightMin = StringUtils.getRounding(rightMin, 3);
        return dataBean;
    }

    public static CheckDataBean getSuitableIndex(String poleNorm, double poleDiameter, CheckDataBean maxBean, List<StandardCurveBean> dataList0, List<StandardCurveBean> dataList1) {
        List<StandardCurveBean> newList = getNewCurve(dataList0, dataList1, poleDiameter);
        return getSuitableIndex(poleNorm, maxBean, newList);
    }

    private static List<StandardCurveBean> getNewCurve(List<StandardCurveBean> dataList0, List<StandardCurveBean> dataList1, double poleDiameter) {
        List<StandardCurveBean> list = new ArrayList<>();
        for (int i = 0; i < dataList1.size(); i++) {
            StandardCurveBean bean = insertData(dataList0.get(i), dataList1.get(i), poleDiameter);
            if (bean != null) {
                list.add(bean);
            }
        }
        return list;
    }

    private static StandardCurveBean insertData(StandardCurveBean curveBean0, StandardCurveBean curveBean1, double poleDiameter) {
        // 已知数据点
        double[] knownX = new double[2];
        double[][] knownY = new double[2][curveBean1.signalArray.length];
        //x
        knownX[0] = Double.parseDouble(curveBean0.poleDiameter);
        knownX[1] = Double.parseDouble(curveBean1.poleDiameter);
        //y
        for (int i = 0; i < curveBean0.signalMovAve.length; i++) {
            knownY[0][i] = curveBean0.signalMovAve[i];
        }
        for (int i = 0; i < curveBean1.signalMovAve.length; i++) {
            knownY[1][i] = curveBean1.signalMovAve[i];
        }
        // 目标x值
        double[] targetX = {poleDiameter};
        //
        // 使用Apache Commons Math进行三次样条插值
        double[][] result = CubicSplineInterpolationUtils.interpolateWithApacheMath(knownX, knownY, targetX, true);
        //
        if (result != null) {
            double[] signalMovAve = new double[curveBean0.signalMovAve.length];
            for (int i = 0; i < signalMovAve.length; i++) {
                signalMovAve[i] = (int) result[0][i];
            }
            StandardCurveBean curveBean = new StandardCurveBean(curveBean0.rebarDiameter, StringUtils.getRoundingString(poleDiameter, 1), "", "", curveBean0.thickness, null);  //TODO
            curveBean.signalMovAve = signalMovAve;
            return curveBean;
        }
        return null;
    }
}

package com.example.gtj_f230_rebound_corrosion.base.utils;


import com.example.gtj_f230_rebound_corrosion.f230.model.WidthCalibrationBean;

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import java.util.List;

public class CurveFittingUtils {
    /**
     * 最小二乘法
     */
    public static double[] getCoefficient(List<WidthCalibrationBean> arrayList) {
        WeightedObservedPoints obs = new WeightedObservedPoints();
        for (int i = 0; i < arrayList.size(); i++) {
            obs.add(arrayList.get(i).x, arrayList.get(i).y);
        }
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(2);
        return fitter.fit(obs.toList());
    }

    public static float getY(double a0, double a1, double a2, double x) {
        return (float) (a0 + a1 * x + a2 * x * x);
    }
}

package com.example.gtj_f230_rebound_corrosion.corrosion.utils;

import com.example.gtj_f230_rebound_corrosion.base.utils.StringUtils;
import com.orhanobut.logger.Logger;

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import java.util.Arrays;

public class CurveFittingUtils {

    public static double[] getCoefficient(int[] x, int[] y) {
        WeightedObservedPoints obs = new WeightedObservedPoints();
        for (int i = 0; i < x.length; i++) {
            obs.add(x[i], y[i]);
        }
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(2);
        double[] result = fitter.fit(obs.toList());
        Logger.d("曲线系数：" + Arrays.toString(result));
        return result;
    }

    public static int getY(double a0, double a1, double a2, double x) {
        return (int) StringUtils.getRounding(a0 + a1 * x + a2 * x * x, 0);
    }

}

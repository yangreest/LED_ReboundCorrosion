package com.example.gtj_f230_rebound_corrosion.corrosion.utils.fittingcurve;

import com.orhanobut.logger.Logger;

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.fitting.CurveFitter;
import org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizer;

public class CommonsMathPowerFit {

    // 定义幂函数模型
    private static class PowerModel implements ParametricUnivariateFunction {
        @Override
        public double value(double x, double... parameters) {
            double a = parameters[0];
            double b = parameters[1];
            return a * Math.pow(x, b);
        }

        @Override
        public double[] gradient(double x, double... parameters) {
            double a = parameters[0];
            double b = parameters[1];
            return new double[]{Math.pow(x, b),                    // ∂f/∂a
                    a * Math.pow(x, b) * Math.log(x)  // ∂f/∂b
            };
        }
    }

    public static double[] fitWithCommonsMath(double[] x, double[] y, double[] initialGuess) {
        // 创建曲线拟合器
        CurveFitter<PowerModel> fitter = new CurveFitter<>(new LevenbergMarquardtOptimizer());

        // 添加观测数据
        for (int i = 0; i < x.length; i++) {
            fitter.addObservedPoint(1.0, x[i], y[i]); // 权重为1
        }

        // 执行拟合
        return fitter.fit(new PowerModel(), initialGuess);
    }

    public static void main(String[] args) {
        double[] x = {771.5, 1540.8, 2310.5, 3080.2, 3849.5, 4619.3};
        double[] y = {10, 20, 30, 40, 50, 60};

        // 初始猜测值
        double[] initialGuess = {2.0, 1.5}; // [a, b]

        double[] parameters = fitWithCommonsMath(x, y, initialGuess);

        System.out.println("===== Apache Commons Math 拟合结果:");
        System.out.printf("===== y = %.4f * x^%.4f\n", parameters[0], parameters[1]);
        Logger.d("===== 771.5: " + parameters[0] * Math.pow(771.5, parameters[1]));
        Logger.d("===== 1540.8: " + parameters[0] * Math.pow(1540.8, parameters[1]));
        Logger.d("===== 2310.5: " + parameters[0] * Math.pow(2310.5, parameters[1]));
        Logger.d("===== 3080.2: " + parameters[0] * Math.pow(3080.2, parameters[1]));
        Logger.d("===== 3849.5: " + parameters[0] * Math.pow(3849.5, parameters[1]));
        Logger.d("===== 4619.3: " + parameters[0] * Math.pow(4619.3, parameters[1]));
    }
}
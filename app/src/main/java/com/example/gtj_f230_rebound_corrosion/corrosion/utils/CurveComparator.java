package com.example.gtj_f230_rebound_corrosion.corrosion.utils;

import com.example.gtj_f230_rebound_corrosion.corrosion.model.CheckDataBean;

import java.util.List;

public class CurveComparator {
    /**
     * 计算变化率
     */
    public static void computeCorrelationAnalysis(List<CheckDataBean> standardList, CheckDataBean bean2) {
        for (CheckDataBean bean : standardList) {
            //广角 10
            bean.correlation1 = CurveComparator1.computeCorrelationAnalysis1(bean.signalMovAve, bean2.signalMovAve, 10);
            //广角 15
            bean.correlation2 = CurveComparator1.computeCorrelationAnalysis1(bean.signalMovAve, bean2.signalMovAve, 15);
            //广角 20
            bean.correlation3 = CurveComparator1.computeCorrelationAnalysis1(bean.signalMovAve, bean2.signalMovAve, 20);
        }
    }

    /**
     * 计算变化率
     */
//    public static void computeCorrelationAnalysis(List<CheckDataBean> standardList) {
//        for (CheckDataBean bean : standardList) {
//            //广角 60
//            bean.correlation2 = CurveComparator1.computeCorrelationAnalysis1(bean.curve_y_original, bean.curve_y_processed, 20);
//            //广角 80
//            bean.correlation3 = CurveComparator1.computeCorrelationAnalysis1(bean.curve_y_original, bean.curve_y_processed, 30);
//        }
//    }
}

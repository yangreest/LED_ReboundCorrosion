package com.example.gtj_f230_rebound_corrosion.corrosion.model;

import androidx.annotation.NonNull;

import com.example.gtj_f230_rebound_corrosion.base.utils.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CheckDataBean {
    public String poleNorm;  //电杆规格，主筋直径
    public double thickness;  //保护层厚度
    public int index;  //取采集数据最大信号bean时，代表下标index | 取标准表数据里的厚度曲线index即厚度index
    public int x;  //位移
    public double y;  //信号值
    public double[] curve_y_original;  //采集的原始信号值
    public double[] curve_y_processed;  //原始修正后信号值
    public List<Double> curve_y_processed1;  //原始修正后信号值
    public double[] signalMovAve;  //平滑信号值（根据电杆直径计算）
    public double[] signalFrequency;  //频域数据
    public int[] curve_x;  //信号值
    public double RMSE;  //均方根误差
    public double[] correlation1;  //变化率
    public double[] correlation2;  //广角30
    public double[] correlation3;  //广角40
    public double topMin;  //波峰误差值
    public double leftMin;  //原始数据差值
    public double rightMin;  //原始数据差值

    //
    public CheckDataBean(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public CheckDataBean(String poleNorm) {
        this.poleNorm = poleNorm;
    }

    public CheckDataBean(String poleNorm, int index, int x, double y) {
        this.poleNorm = poleNorm;
        this.index = index;
        this.x = x;
        this.y = y;
    }

    public String toString1() {
        String string = "\n{";
        string += "直：" + String.format(Locale.CHINA, "%02d", Integer.parseInt(poleNorm)) + ",";
        string += "厚：" + String.format(Locale.CHINA, "%02d", ((int) StringUtils.getRounding(thickness, 0))) + ",";
        //广角10
        if (correlation1 != null) {
            StringBuilder s = new StringBuilder("[");
            for (double v : correlation1) {
                s.append(v).append(",　");
            }
            s.append("]");
            string += "\n广角10：" + s + ",";
        }
        //广角15
        if (correlation2 != null) {
            StringBuilder s = new StringBuilder("[");
            for (double v : correlation2) {
                s.append(v).append(",　");
            }
            s.append("]");
            string += "\n广角15：" + s + ",";
        }
        //广角20
        if (correlation3 != null) {
            StringBuilder s = new StringBuilder("[");
            for (double v : correlation3) {
                s.append(v).append(",　");
            }
            s.append("]");
            string += "\n广角20：" + s + ",";
        }
        string += "\n左中右：" + (int) leftMin + "," + (int) topMin + "," + (int) rightMin;
        return string;
    }

    @NonNull
    @Override
    public String toString() {
        return "{" + index + ", " + curve_y_original.length + ",  " + Arrays.toString(curve_y_original) + "}";
    }
}

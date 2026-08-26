package com.example.gtj_f230_rebound_corrosion.corrosion.activity.testing;

/**
 * 钢筋保护层+腐蚀度算法输出结果封装
 */
public class SteelCorrResult {
    // 中间特征量
    private double v17;
    private double v26;
    private double xRatio;
    // 判定结果
    private int coverThickness; // 保护层厚度 15/18/20/22/25 mm

    private boolean isCorroded;// 是否腐蚀
    private double corrosionRate; // 腐蚀度y，百分比
    // 拟合准确率
    private double modelAccuracy;

    // 构造、get/set省略，自行补充
    public SteelCorrResult(double v17, double v26, double xRatio, int coverThickness, double corrosionRate, double modelAccuracy, boolean isCorroded) {
        this.v17 = v17;
        this.v26 = v26;
        this.xRatio = xRatio;
        this.coverThickness = coverThickness;
        this.corrosionRate = corrosionRate;
        this.modelAccuracy = modelAccuracy;
        this.isCorroded = isCorroded;
    }

    // getter
    public double getV17() { return v17; }
    public double getV26() { return v26; }
    public double getxRatio() { return xRatio; }
    public int getCoverThickness() { return coverThickness; }
    public double getCorrosionRate() { return corrosionRate; }
    public double getModelAccuracy() { return modelAccuracy; }
    public boolean isCorroded() { return isCorroded; }
}

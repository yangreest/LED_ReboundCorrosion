package com.example.gtj_f230_rebound_corrosion.base.utils;

import android.graphics.Bitmap;

import com.example.gtj_f230_rebound_corrosion.f230.model.ComputeWidthBean;
import com.example.gtj_f230_rebound_corrosion.f230.model.SlitWidthBean;
import com.example.gtj_f230_rebound_corrosion.f230.model.PointBean;

import java.util.ArrayList;
import java.util.List;

public class BitmapMatrixUtils {

    /**
     * 转为二值图像
     */
    public static int[][] convertToBMW(Bitmap bmp) {
        float tmp = 95f;  //二值化参考值（中间比较值）二值化的域值
        int width = bmp.getWidth(); // 获取位图的宽
        int height = bmp.getHeight(); // 获取位图的高
        int[] pixels = new int[width * height]; // 通过位图的大小创建像素点数组
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha;
        int Colors;
        int[][] matrixData = new int[480][640];
        for (int i = 0; i < height; i++) {
            int[] matrix = new int[640];
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];
                // 分离三原色
                alpha = ((grey & 0xFF000000) >> 24);
                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);
                if (red > tmp) {
                    red = 255;
                } else {
                    red = 0;
                }
                if (blue > tmp) {
                    blue = 255;
                } else {
                    blue = 0;
                }
                if (green > tmp) {
                    green = 255;
                } else {
                    green = 0;
                }
                pixels[width * i + j] = alpha << 24 | red << 16 | green << 8 | blue;
                if (pixels[width * i + j] == -1) {
                    pixels[width * i + j] = -1;
                    Colors = 0;
                } else {
                    pixels[width * i + j] = -16777216;
                    Colors = 1;
                }
                matrix[j] = Colors;
            }
            matrixData[i] = matrix;
        }
        return matrixData;
    }

    public static boolean getBitmapBlack(int[][] matrixData) {
        int count = 0;
        for (int i = 0; i < 480; i++) {
            for (int j = 0; j < 640; j++) {
                if (matrixData[i][j] == 1) {
                    count++;
                }
            }
        }
        return count == 480 * 640;
    }

    public static SlitWidthBean getBitmapWidth(int[][] matrixData) {
        //求缝宽
        SlitWidthBean slitWidthBean = new SlitWidthBean();
        //
        int[] widthCount = new int[640];
        for (int i = 0; i < 640; i++) {
            int heightCount = 0;
            if (matrixData[236][i] == 1) {
                heightCount++;
            }
            if (matrixData[237][i] == 1) {
                heightCount++;
            }
            if (matrixData[238][i] == 1) {
                heightCount++;
            }
            if (matrixData[239][i] == 1) {
                heightCount++;
            }
            if (matrixData[240][i] == 1) {
                heightCount++;
            }
            if (matrixData[241][i] == 1) {
                heightCount++;
            }
            if (matrixData[242][i] == 1) {
                heightCount++;
            }
            if (matrixData[243][i] == 1) {
                heightCount++;
            }
            if (matrixData[244][i] == 1) {
                heightCount++;
            }
            if (heightCount >= 4) {  //高度超过4个，宽度标记1
                widthCount[i] = 1;
            }
        }
        int maxCount = 0, count = 0;
        for (int i = 0; i < widthCount.length; i++) {
            if (widthCount[i] == 1) {
                count++;
            } else {
                if (maxCount < count) {  //找出最大连续宽度就是此bitmap的宽度
                    maxCount = count;
                    slitWidthBean.start = i - 1 - count;
                    slitWidthBean.end = i - 1;
                }
                count = 0;
            }
            if (count == widthCount.length) {
                maxCount = count;
                slitWidthBean.start = 0;
                slitWidthBean.end = count - 1;
            }
        }
        slitWidthBean.width = maxCount;
        int point = (slitWidthBean.end - slitWidthBean.start) / 2 * slitWidthBean.start;
        if (point >= 240 && point <= 400) {
            slitWidthBean.start -= 2;
            slitWidthBean.end -= 2;
            slitWidthBean.width = slitWidthBean.end - slitWidthBean.start;
        } else if ((point >= 80 && point <= 240) || (point >= 400 && point < 560)) {
            slitWidthBean.start -= 1;
            slitWidthBean.end -= 1;
            slitWidthBean.width = slitWidthBean.end - slitWidthBean.start;
        }
        //上10个点
        List<ComputeWidthBean> topList = new ArrayList<>();
        for (int i = 230; i < 240; i++) {
            int maxYCount = 0, yCount = 0, x = 0, passCount = 0;
            for (int j = 0; j < 640; j++) {
                if (matrixData[i][j] == 1) {
                    yCount++;
                    //pass
                    yCount += passCount;
                    passCount = 0;
                } else {
                    passCount++;
                    if (passCount == 4) {
                        passCount = 0;
                        if (maxYCount < yCount) {  //找出最大连续宽度就是此bitmap的宽度
                            maxYCount = yCount;
                            x = j - 1 - yCount / 2;
                        }
                        yCount = 0;
                    }
                }
            }
            topList.add(new ComputeWidthBean(maxYCount, new PointBean(x, i)));
        }
        //下10个点
        List<ComputeWidthBean> bottomList = new ArrayList<>();
        for (int i = 240; i < 250; i++) {
            int maxYCount = 0, yCount = 0, x = 0, passCount = 0;
            for (int j = 0; j < 640; j++) {
                if (matrixData[i][j] == 1) {
                    yCount++;
                    //pass
                    yCount += passCount;
                    passCount = 0;
                } else {
                    passCount++;
                    if (passCount == 4) {
                        passCount = 0;
                        if (maxYCount < yCount) {  //找出最大连续宽度就是此bitmap的宽度
                            maxYCount = yCount;
                            x = j - 1 - yCount / 2;
                        }
                        yCount = 0;
                    }
                }
            }
            bottomList.add(new ComputeWidthBean(maxYCount, new PointBean(x, i)));
        }
        //根据上下10个点确定斜率2点
        for (int size = topList.size() - 1; size >= 0; size--) {
            if (Math.abs(topList.get(size).width - slitWidthBean.width) >= 5) {
                topList.remove(size);
            }
        }
        for (int size = bottomList.size() - 1; size >= 0; size--) {
            if (Math.abs(bottomList.get(size).width - slitWidthBean.width) >= 5) {
                bottomList.remove(size);
            }
        }
        PointBean topBean = new PointBean(slitWidthBean.end - slitWidthBean.width, 239);
        PointBean bottomBean = new PointBean(slitWidthBean.end - slitWidthBean.width, 239);
        if (topList.size() > 0) {
            double x = 0, y = 0;
            for (ComputeWidthBean bean : topList) {
                x += bean.pointBean.x;
                y += bean.pointBean.y;
            }
            x = StringUtils.getRounding1(x / topList.size(), 0);
            y = StringUtils.getRounding1(y / topList.size(), 0);
//            x = topList.get(0).pointBean.x;
//            y = topList.get(0).pointBean.y;
            topBean = new PointBean(x, y);
        }
        if (bottomList.size() > 0) {
            double x = 0, y = 0;
            for (ComputeWidthBean bean : bottomList) {
                x += bean.pointBean.x;
                y += bean.pointBean.y;
            }
            x = StringUtils.getRounding1(x / bottomList.size(), 0);
            y = StringUtils.getRounding1(y / bottomList.size(), 0);
//            x = bottomList.get(0).pointBean.x;
//            y = bottomList.get(0).pointBean.y;
            bottomBean = new PointBean(x, y);
        }
        slitWidthBean.p1 = topBean;
        slitWidthBean.p2 = bottomBean;
        return slitWidthBean;
    }
}

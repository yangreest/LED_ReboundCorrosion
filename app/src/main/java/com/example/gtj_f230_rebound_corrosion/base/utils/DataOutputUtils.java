package com.example.gtj_f230_rebound_corrosion.base.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.SPUtils;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.base.model.ProjectBean;
import com.example.gtj_f230_rebound_corrosion.base.model.RebounderZoneBean;
import com.example.gtj_f230_rebound_corrosion.base.model.ZoneBean;
import com.example.gtj_f230_rebound_corrosion.base.storage.GreenDaoHelper;
import com.example.gtj_f230_rebound_corrosion.f230.model.WidthBean;
import com.example.gtj_f230_rebound_corrosion.functions.database.greenDao.db.ProjectBeanDao;
import com.example.gtj_f230_rebound_corrosion.rebound.model.RebounderBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.logger.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class DataOutputUtils {
    /**
     * 回弹仪
     */
    public static String dataOutputToString(Context context, ZoneBean zoneBean) {
        List<ProjectBean> list = GreenDaoHelper.getDaoSession(context).getProjectBeanDao().queryBuilder().where(ProjectBeanDao.Properties.Id.eq(zoneBean.projectId)).list();
        ProjectBean projectBean = null;
        if (!list.isEmpty()) {
            projectBean = list.get(0);
        }

        StringBuilder sbd = new StringBuilder("FFFFFFFFFFFFFFFF").append("\n\n");
        //工程名称，工程地址
        sbd.append(projectBean == null ? "" : projectBean.name).append(",").append(projectBean == null ? "" : projectBean.address).append(",").append("\n\n");

        RebounderZoneBean itemPointBean = zoneBean.rebounderZoneBean;
        String detectionStandard = StaticConstant.detectionStandard.get(itemPointBean.detectionStandard - 1);
        String detectionSurface = "0" + (itemPointBean.detectionSurface == 3 ? 0 : itemPointBean.detectionSurface);
        String angle1 = itemPointBean.detectionAngle <= 5 ? "00" : "01";
        int angle2 = Math.abs(Integer.parseInt(StaticConstant.getDetectionAngleCompute(itemPointBean.detectionStandard).get(itemPointBean.detectionAngle - 1)));
        String pumping = "0" + itemPointBean.isPumping;
        //构件名称，测区数，规程曲线，测试面（00侧面 01表面 02底面），测试角度（向上00或向下01），角度值（30.60.90），是否泵送（00非，01是），构件部位，检测人, 检测日期，楼层（位置），位置坐标，设计等级，浇筑日期，
        sbd.append("F").append(zoneBean.number).append(",").append(itemPointBean.zoneCount).append(",").append(detectionStandard).append(",").append(detectionSurface).append(",").append(angle1).append(",").append(HexUtil.intToHex(angle2)).append(",").append(pumping).append(",").append(itemPointBean.startTime).append(",").append("\n\n");
        //
        if (!TextUtils.isEmpty(itemPointBean.strZoneList)) {
            List<RebounderBean> testingList = new Gson().fromJson(itemPointBean.strZoneList, new TypeToken<List<RebounderBean>>() {
            }.getType());
            //循环添加回弹值，每一行做一个测区，每个测区16个回弹值
            for (RebounderBean rebounderBean : testingList) {
                for (Integer integer : rebounderBean.testNumber) {
                    sbd.append(integer).append(",");
                }
                sbd.append("\n\n");
            }
            //循环添加未修正的平均回弹值
            for (RebounderBean rebounderBean : testingList) {
                sbd.append(rebounderBean.average).append(",");
            }
            sbd.append("\n\n");
            //循环添加碳化值
            double total = 0;
            for (RebounderBean rebounderBean : testingList) {
                total += rebounderBean.carbonization;
                sbd.append(rebounderBean.carbonization).append(",");
            }
            sbd.append("\n\n");
            //添加平均碳化值，就一个
            sbd.append(StringUtils.getRounding(total / testingList.size(), 1)).append(",").append("\n\n");
            //循环添加角度修正值
            for (int i = 0; i < testingList.size(); i++) {
                sbd.append("0.0,");
            }
            sbd.append("\n\n");
            //循环添加测试面修正值
            for (int i = 0; i < testingList.size(); i++) {
                sbd.append("0.0,");
            }
            sbd.append("\n\n");
            //循环添加修正后平均回弹值
            for (RebounderBean rebounderBean : testingList) {
                sbd.append(rebounderBean.average).append(",");
            }
            sbd.append("\n\n");
            //循环添加混凝土强度换算值
            for (RebounderBean rebounderBean : testingList) {
                sbd.append(rebounderBean.carbonize).append(",");
            }
            sbd.append("\n\n");
        }
        //强度换算值的最大值，强度换算值的最小值，强度平均值，强度标准差，构件推定强度
        sbd.append("0.0,").append(itemPointBean.min).append(",").append(itemPointBean.average).append(",").append(itemPointBean.stdDev).append(",").append(itemPointBean.presumption).append(",").append("\n\n");

        sbd.append("DDDDDD");
        return sbd.toString();
    }

    /**
     * 导出byte
     *
     * @param zoneBean  strZoneList不能为空
     * @param viewRatio 校准到640*480 上的点坐标
     * @return byte
     */
    public static byte[] dataOutputToBytes_f230(ZoneBean zoneBean, float viewRatio) {
        byte[] bytes = new byte[224 + zoneBean.f230ZoneBean.zoneCount * 164616];
        //1.文件头定义
        byte[] num = strToBytes1(zoneBean.number);
        byte[] remark = strTobytes(zoneBean.remark, 100);
        float calWidth = SPUtils.getInstance().getFloat("cal-width-4", 279) / 5;
        byte[] pixNumPerMm = DoubleByteConverter.toBigEndianBytes(calWidth);
        byte[] isMeasure = integerToBytes(1);
        List<WidthBean> widthBeanList = new Gson().fromJson(zoneBean.f230ZoneBean.strZoneList, new TypeToken<List<WidthBean>>() {
        }.getType());
        byte[] pointCnt = integerToBytes(widthBeanList.size());
        byte[] maxSeq = integerToBytes(widthBeanList.get(widthBeanList.size() - 1).id);
        //
        int begin = 0;
        BytesConversion.insert(bytes, num, begin);
        begin += 100;
        BytesConversion.insert(bytes, remark, begin);
        begin += 100;
        BytesConversion.insert(bytes, pixNumPerMm, begin);
        begin += 8;
        BytesConversion.insert(bytes, isMeasure, begin);
        begin += 4;
        BytesConversion.insert(bytes, pointCnt, begin);
        begin += 4;
        BytesConversion.insert(bytes, maxSeq, begin);
        begin += 4;
        begin += 4;
        //2.文件体定义
        for (WidthBean widthBean : widthBeanList) {
            byte[] pointSeq = integerToBytes(widthBean.id);
            byte[] pointName = strToBytes1(widthBean.id + "");
            byte[] pointRemark = strToBytes1("");
            //缝宽信息
            byte[] num_of_crack = integerToBytes(1);
            int isx = (int) (widthBean.widthLeftX / viewRatio);
            int isy = (int) (widthBean.widthLeftY / viewRatio);
            byte[] start_x = integerToBytes(isx);
            byte[] start_y = integerToBytes(isy);
            String str_sx = StringUtils.ByteArraytoHex(start_x);
            String str_sy = StringUtils.ByteArraytoHex(start_y);
            int iex = (int) (widthBean.widthRightX / viewRatio);
            int iey = (int) (widthBean.widthRightY / viewRatio);
            byte[] end_x = integerToBytes(iex);
            byte[] end_y = integerToBytes(iey);
            String str_ex = StringUtils.ByteArraytoHex(end_x);
            String str_ey = StringUtils.ByteArraytoHex(end_y);
            byte[] width_crack = DoubleByteConverter.toBigEndianBytes(widthBean.width);
            byte[] angle_crack = integerToBytes((int) getRotate(widthBean.widthLeftX, widthBean.widthLeftY, widthBean.widthRightX, widthBean.widthRightY) * 1000);
            //
            byte[] width = DoubleByteConverter.toBigEndianBytes(widthBean.width);
            //缝深信息
            byte[] HCheckType = integerToBytes(1);
            byte[] Length_VV = integerToBytes(150);
            byte[] Time_VV = integerToBytes(3);
            byte[] Length_H = integerToBytes(150);
            byte[] Time_H = integerToBytes(4);
            byte[] height = DoubleByteConverter.toBigEndianBytes(0);
            byte[] heightEdit = DoubleByteConverter.toBigEndianBytes(0);
            byte[] VV = DoubleByteConverter.toBigEndianBytes(0);
            byte[] L0 = DoubleByteConverter.toBigEndianBytes(0);
            byte[] DateTimeH = strToBytes1(widthBean.time);
            //
            byte[] DateTime = strToBytes1(widthBean.time);
            //图片
            byte[] Jpgdata = readStream(widthBean.imagePath);
            byte[] jpgsize = integerToBytes(Jpgdata.length);
            //
            BytesConversion.insert(bytes, pointSeq, begin);
            begin += 4;
            BytesConversion.insert(bytes, pointName, begin);
            begin += 100;
            BytesConversion.insert(bytes, pointRemark, begin);
            begin += 100;
            begin += 4;
            //
            BytesConversion.insert(bytes, num_of_crack, begin);
            begin += 4;
            BytesConversion.insert(bytes, start_x, begin);
            begin += 4 * 5;
            BytesConversion.insert(bytes, start_y, begin);
            begin += 4 * 5;
            BytesConversion.insert(bytes, end_x, begin);
            begin += 4 * 5;
            BytesConversion.insert(bytes, end_y, begin);
            begin += 4 * 5;
            BytesConversion.insert(bytes, width_crack, begin);
            begin += 8 * 5;
            BytesConversion.insert(bytes, angle_crack, begin);
            begin += 4 * 5;
            //
            BytesConversion.insert(bytes, width, begin);
            begin += 8;
            //
            BytesConversion.insert(bytes, HCheckType, begin);
            begin += 4;
            BytesConversion.insert(bytes, Length_VV, begin);
            begin += 4 * 10;
            BytesConversion.insert(bytes, Time_VV, begin);
            begin += 4 * 10;
            BytesConversion.insert(bytes, Length_H, begin);
            begin += 4 * 10;
            BytesConversion.insert(bytes, Time_H, begin);
            begin += 4 * 10;
            BytesConversion.insert(bytes, height, begin);
            begin += 8;
            BytesConversion.insert(bytes, heightEdit, begin);
            begin += 8;
            BytesConversion.insert(bytes, VV, begin);
            begin += 8;
            BytesConversion.insert(bytes, L0, begin);
            begin += 8;
            BytesConversion.insert(bytes, DateTimeH, begin);
            begin += 100 + 16;
            //
            BytesConversion.insert(bytes, DateTime, begin);
            begin += 100;
            BytesConversion.insert(bytes, jpgsize, begin);
            begin += 4;
            BytesConversion.insert(bytes, Jpgdata, begin);
            begin += 8 * 20 * 1024;
        }
        Logger.d("begin=" + begin);
        return bytes;
    }

    /**
     * 字符串（字符类型）转byte
     */
    private static byte[] strToBytes1(String str) {
        return str.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 字符串（16进制类型）转byte
     */
    private static byte[] strToBytes2(String str) {
        int size = str.length();
        String[] array = new String[size];
        for (int i = 0; i < size; i++) {
            array[i] = str.charAt(i) + "";
        }
        //对接协议：低位在前，高位在后（根据对接协议对字符串进行修改）
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < array.length / 2; i++) {
            string.insert(0, array[i * 2] + array[i * 2 + 1]);
        }
        return decodeHex(string.toString());
    }


    private static byte[] decodeHex(String str) {
        if (!str.isEmpty()) {
            byte[] temp = new byte[str.length() / 2];
            for (int i = 0; i < str.length() / 2; i++) {
                temp[i] = (byte) Integer.parseInt(str.charAt(i * 2) + "" + str.charAt(i * 2 + 1), 16);
            }
            return temp;
        }
        return new byte[8];
    }

    /**
     * 16进制字符串转double
     */
    public static double getHex2Double(String string) {
        return Double.longBitsToDouble(Long.parseLong(string, 16));
    }

    /**
     * 按unioncode编码转换成字节流
     */
    private static byte[] strTobytes(String content, int len) {
        byte[] bytes = new byte[len];
        for (int i = 0; i < content.length() && i < len / 2; i++) {
            char c = content.charAt(i);
            BytesConversion.insert(bytes, BytesConversion.charToByte1(c), i * 2);
        }
        return bytes;
    }

    /**
     * 数字转byte
     */
    private static byte[] integerToBytes(int num) {
        byte[] result = new byte[4];

        for (int i = 0; i < 4; i++) {
            int n = i * 8;
            if (n == 0) {
                result[i] = (byte) (num & 0xff);
            } else {
                result[i] = (byte) (num >> n & 0xff);
            }
        }
        return result;
    }

    /**
     * 时间转换byte数组
     */
    private static byte[] timeTobytes(String time) {
        byte[] bytes = new byte[8];
        String[] timearr = time.split(",");
        byte[] bytePressure = BytesConversion.readByte(Integer.parseInt(timearr[0]), 2);
        BytesConversion.insert(bytes, bytePressure, 0);
        bytes[2] = (byte) Integer.parseInt(timearr[1]);
        bytes[3] = (byte) Integer.parseInt(timearr[2]);
        bytes[4] = (byte) Integer.parseInt(timearr[3]);
        bytes[5] = (byte) Integer.parseInt(timearr[4]);
        if (timearr.length > 5) {
            bytes[6] = (byte) Integer.parseInt(timearr[5]);
        }
        return bytes;
    }

    /**
     * 坐标信息转换byte数组
     */
    private static byte[] gpsTobytes(String gpsstr, String hb) {
        byte[] bytes = new byte[8];
        String[] gps = gpsstr.split(",");
        //===============================经度=====================================
        BigDecimal jd = new BigDecimal(gps[0].trim());
        //度数
        BytesConversion.insert(bytes, BytesConversion.readByte(jd.intValue(), 2), 0);
        jd = jd.subtract(new BigDecimal(String.valueOf(jd.intValue())));
        //获取分
        jd = jd.multiply(new BigDecimal("60"));
        int jdf = jd.intValue();
        bytes[2] = (byte) jdf;
        jd = jd.subtract(new BigDecimal(String.valueOf(jdf)));
        //获取秒
        jd = jd.multiply(new BigDecimal("60"));
        bytes[3] = (byte) jd.intValue();
        //===============================纬度=====================================
        BigDecimal wd = new BigDecimal(gps[1].trim());
        //度数
        bytes[4] = (byte) wd.intValue();
        wd = wd.subtract(new BigDecimal(String.valueOf(wd.intValue())));
        //获取分
        wd = wd.multiply(new BigDecimal("60"));
        bytes[5] = (byte) wd.intValue();
        wd = wd.subtract(new BigDecimal(String.valueOf(wd.intValue())));
        //获取秒
        wd = wd.multiply(new BigDecimal("60"));
        bytes[6] = (byte) wd.intValue();
        //===============================海拔=====================================
        bytes[7] = (byte) new BigDecimal(hb.isEmpty() ? "0" : hb).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP).intValue();
        return bytes;
    }

    /**
     * 将图片解析成字节数组
     */
    public static byte[] readStream(String imagePath) {
        File file = new File(imagePath);
        double fileSize = file.length();
        byte[] data;
        if (fileSize <= 163840) {
            try {
                InputStream inStream = new FileInputStream(imagePath);
                byte[] buffer = new byte[1024];
                int len;
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                while ((len = inStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, len);
                }
                data = outStream.toByteArray();
                outStream.close();
                inStream.close();
            } catch (IOException e) {
                data = new byte[1];
            }
        } else {
            double rate = fileSize / 163840 + 0.1;
            Bitmap bitmap = convertFileToBitmap(imagePath);
            assert bitmap != null;
            data = compressBitmapToByte(bitmap, (int) (100 / rate));
        }
        return data;
    }

    private static byte[] compressBitmapToByte(Bitmap bitmap, int quality) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private static Bitmap convertFileToBitmap(String filePath) {
        try {
            FileInputStream fis = new FileInputStream(filePath);
            return BitmapFactory.decodeStream(fis);
        } catch (Exception e) {
            return null;
        }
    }

    public static double getRotate(float x1, float y1, float x2, float y2) {
        double abx = 240 - x1;
        double aby = 320 - y1;
        double acx = 240 - x2;
        double acy = 320 - y2;
        double bcx = x2 - x1;
        double bcy = y2 - y1;
        double c = Math.hypot(abx, aby);
        Log.e("TAG", "c == " + c);
        double b = Math.hypot(acx, acy);
        Log.e("TAG", "b == " + b);
        double a = Math.hypot(bcx, bcy);
        Log.e("TAG", "a == " + a);
        double cos1 = (c * c + b * b - a * a) / (2 * b * c);
        Log.e("TAG", "cos == " + cos1);
        if (cos1 >= 1) {
            cos1 = 1f;
        }
        double radian = Math.acos(cos1);
//        return Math.toDegrees(radian);
        return 0;
    }
}

package com.example.gtj_f230_rebound_corrosion.base.utils;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gtj_f230_rebound_corrosion.StaticConstant;
import com.example.gtj_f230_rebound_corrosion.rebound.model.RebounderBean;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.regex.Pattern;

@SuppressLint("SimpleDateFormat")
public class StringUtils {
    private static final DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String getTime() {
        return format.format(new Date());
    }

    public static String getTime(long time, String format) {
        DateFormat ft = new SimpleDateFormat(format);
        return ft.format(new Date(time));
    }

    public static int formatInteger(String string) {
        return Integer.valueOf(string, 16);
    }

    public static String formatDouble(String string, float divisor, String format) {
        float d = Integer.valueOf(string, 16) / divisor;
        return String.format(format, d);
    }

    public static short parseHex4(String num) {
        if (num.length() != 4) {
            throw new NumberFormatException("Wrong length: " + num.length() + ", must be 4.");
        }
        int ret = Integer.parseInt(num, 16);
        ret = ((ret & 0x8000) > 0) ? (ret - 0x10000) : (ret);
        return (short) ret;
    }

    public static String getFormatNumber(String num, int length) {
        int size = length - num.length();
        if (size <= 0) {
            return num;
        }
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < size; i++) {
            temp.append("0");
        }
        return temp + num;
    }

    public static double getFormatNumber(double d) {
        return (double) Math.round(d * 1000) / 1000;
    }

    public static double getDivide(double d1, double d2) {
        BigDecimal bdl_temp = new BigDecimal(d1).divide(new BigDecimal(d2), 3, BigDecimal.ROUND_DOWN);
        return bdl_temp.doubleValue();
    }

    public static double getSettlement(double a0, double a1, double a2, double pressure) {
        BigDecimal bdlp = new BigDecimal(pressure);
        BigDecimal a1p = new BigDecimal(a1).multiply(bdlp);
        BigDecimal a2p = new BigDecimal(a2).multiply(bdlp).multiply(bdlp);
        BigDecimal bdl_result = new BigDecimal(a0).add(a1p).add(a2p);
        return bdl_result.doubleValue();
    }

    public static void stringFormatFilter(CharSequence s, TextView textView, ImageView imageView) {
        StringBuilder sbstr = new StringBuilder(s.toString());
        if (imageView != null) {
            if (sbstr.length() > 0) {
                imageView.setVisibility(View.VISIBLE);
            } else {
                imageView.setVisibility(View.GONE);
            }
        }
        if (sbstr.length() > 0) {
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
        //D8:B0:4C:CC:74:AF
        if (sbstr.length() > 2) {
            sbstr.insert(2, ":");
        }
        if (sbstr.length() > 5) {
            sbstr.insert(5, ":");
        }
        if (sbstr.length() > 8) {
            sbstr.insert(8, ":");
        }
        if (sbstr.length() > 11) {
            sbstr.insert(11, ":");
        }
        if (sbstr.length() > 14) {
            sbstr.insert(14, ":");
        }
        textView.setText(sbstr.toString().toUpperCase());
    }

    public static void stringFormatFilter(CharSequence s, TextView textView) {
        StringBuilder sbstr = new StringBuilder(s.toString());
        if (sbstr.length() > 0) {
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
        //D8:B0:4C:CC:74:AF
        if (sbstr.length() > 2) {
            sbstr.insert(2, "　");
        }
        if (sbstr.length() > 5) {
            sbstr.insert(5, "　");
        }
        if (sbstr.length() > 8) {
            sbstr.insert(8, "　");
        }
        if (sbstr.length() > 11) {
            sbstr.insert(11, "　\n");
        }
        if (sbstr.length() > 15) {
            sbstr.insert(15, "　");
        }
        if (sbstr.length() > 18) {
            sbstr.insert(18, "　");
        }
        if (sbstr.length() > 21) {
            sbstr.insert(21, "　");
        }
        if (sbstr.length() > 24) {
            sbstr.insert(24, "　\n");
        }
        if (sbstr.length() > 28) {
            sbstr.insert(28, "　");
        }
        if (sbstr.length() > 31) {
            sbstr.insert(31, "　");
        }
        if (sbstr.length() > 34) {
            sbstr.insert(34, "　");
        }
        if (sbstr.length() > 37) {
            sbstr.insert(37, "　\n");
        }
        if (sbstr.length() > 41) {
            sbstr.insert(41, "　");
        }
        if (sbstr.length() > 44) {
            sbstr.insert(44, "　");
        }
        if (sbstr.length() > 47) {
            sbstr.insert(47, "　");
        }
        textView.setText(sbstr.toString().toUpperCase());
    }

    @SuppressLint("DefaultLocale")
    public static String getRandom() {
        Random random = new Random();
        int ends = random.nextInt(99);
        return String.format("%02d", ends);
    }

    public static String D2Dms(double d_data) {
        int d = (int) d_data;
        int m = (int) ((d_data - d) * 60);
        int s = (int) (((d_data - d) * 60 - m) * 60);
        return d + "°" + m + "′" + s + "″";
    }

    public static String getStringTime(long time, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(new Date(time));
    }

    public static String byteArraytoHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String bs = String.format("%02X ", b);
            sb.append(bs);
        }
        return sb.toString();
    }

    /**
     * byte[]转换为16进制字符串
     */
    public static String[] bytesToHexStrArray(byte[] bArray) {
        String[] strArray = new String[bArray.length];
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2) {
                sTemp = "0" + sTemp;
            }
            strArray[i] = sTemp.toUpperCase();
        }
        return strArray;
    }

    public static String getStringNumber(int number) {
        if (number < 10) {
            return "0" + number;
        }
        return "" + number;
    }

    /**
     * 四舍五入
     * 当数足够小的时候，double默认显示小数点后3位，如0.001，4位需要BigDecimal转String显示（0.0001等于1.0E-4）
     */
    public static double getRounding1(double d, int rate) {
        if (!Double.isFinite(d)) {
            d = 0;
        }
        BigDecimal big = new BigDecimal(d + "");
        return big.setScale(rate, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 保留小数（四舍五入）
     */
    public static double getRounding(double d, int rate) {
        if (!Double.isFinite(d)) {
            d = 0;
        }
        BigDecimal big = new BigDecimal(d + "");
        return big.setScale(rate, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static String getRoundingString(double d, int rate) {
        if (Double.isNaN(d)) {
            d = 0;
        }
        BigDecimal big = new BigDecimal(d + "");
        return big.setScale(rate, BigDecimal.ROUND_HALF_UP).toPlainString();
    }

    /**
     * 保留小数（四舍五入）
     */
    public static double getRoundingMode(double d, int roundingMode, int rate) {
        if (!Double.isFinite(d)) {
            d = 0;
        }
        BigDecimal big = new BigDecimal(d + "");
        return big.setScale(rate, roundingMode).doubleValue();
    }

    /**
     * 获取字符串里最后数字index
     */
    public static int getNumberIndex(String str) {
        int index = -1;
        for (int i = str.length(); --i >= 0; ) {
            if (!Character.isDigit(str.charAt(i))) {
                index = i + 1;
                break;
            }
        }
        if (index == -1) {
            if (Pattern.compile("^[-\\+]?[\\d]*$").matcher(str).matches()) {
                return 0;
            }
        }
        if (index >= str.length()) {
            index = -1;
        }
        return index;
    }

    /**
     * 字符串转换成为16进制(无需Unicode编码)
     */
    public static String str2HexStr(String str) {
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder(""); //String的getBytes()方法是得到一个操作系统默认的编码格式的字节数组
        byte[] bs = str.getBytes();

        int bit;
        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4; // 高4位, 与操作 1111 0000
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f; // 低四位, 与操作 0000 1111
            sb.append(chars[bit]);
        }
        return sb.toString().trim();
    }

    /**
     * 16进制直接转换成为字符串(无需Unicode解码)
     */
    public static String hexStr2Str(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2]; //1个byte数值 -> 两个16进制字符
        int n;
        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            // 保持二进制补码的一致性 因为byte类型字符是8bit的  而int为32bit 会自动补齐高位1  所以与上0xFF之后可以保持高位一致性
            //当byte要转化为int的时候，高的24位必然会补1，这样，其二进制补码其实已经不一致了，&0xff可以将高的24位置为0，低8位保持原样，这样做的目的就是为了保证二进制数据的一致性。
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
        // return new String(bytes,"gb2312"); 转换时指定,则解析时指定
    }

    public static String getStringCar(RebounderBean bean) {
        String strCar = bean.carbonize + "";
        switch (bean.standardId) {
            case 1://国家统一新版（泵送）10MPa～60 MPa
            case 5://河北曲线（泵送）10MPa～60 MPa
            case 7://山东曲线（泵送）10MPa～60 MPa M225型修正
            case 9://青岛曲线（泵送）10MPa～60 MPa M225型修正
            case 11://辽宁曲线（泵送）10MPa～60 MPa
            case 12://安徽低强度（泵送）10MPa～60 MPa
            case 14://江苏泵送（泵送）10MPa～60 MPa
            case 15://浙江卵石（泵送）10MPa～60 MPa
            case 18://福建卵石（泵送）10MPa～60 MPa
            case 19://福建碎石（泵送）10MPa～60 MPa
            case 20://福建泵送（泵送）10MPa～60 MPa
            case 21://陕西泵送曲线（泵送）10MPa～60 MPa
            case 22://宁夏泵送（泵送）//TODO 暂无 现使用国家标准
            case 23://甘肃庆阳预拌混凝土曲线（泵送）
            case 26://水运工程2.207J（泵送）10MPa～60 MPa
            case 27://铁路工程曲线（泵送）10MPa～60 MPa
            case 28://广西泵送2023（泵送）10MPa～60 MPa
                if (bean.carbonize < 10) {
                    strCar = "<10";
                } else if (bean.carbonize > 60) {
                    strCar = ">60";
                }
                break;
            case 2:////国家高强度4.5J（泵送）20MPa～110 MPa
                if (bean.carbonize < 20) {
                    strCar = "<20";
                } else if (bean.carbonize > 110) {
                    strCar = ">110";
                }
                break;
            case 3://国家高强度5.5J（泵送）60.2MPa～79.9 MPa
                if (bean.carbonize < 60.2) {
                    strCar = "<60.2";
                } else if (bean.carbonize > 79.9) {
                    strCar = ">79.9";
                }
                break;
            case 4://北京曲线（泵送）15MPa～60 MPa
                if (bean.carbonize < 15) {
                    strCar = "<15";
                } else if (bean.carbonize > 60) {
                    strCar = ">60";
                }
                break;
            case 6://河南商品混凝土曲线（泵送）15MPa～80 MPa
            case 16://浙江碎石（泵送）15MPa～80 MPa
                if (bean.carbonize < 15) {
                    strCar = "<15";
                } else if (bean.carbonize > 80) {
                    strCar = ">80";
                }
                break;
            case 8://山东高强度曲线（泵送）60MPa～80 MPa
            case 10://青岛高强度曲线（泵送）60MPa～80 MPa
                if (bean.carbonize < 60) {
                    strCar = "<60";
                } else if (bean.carbonize > 80) {
                    strCar = ">80";
                }
                break;
            case 13://安徽高强度（泵送）60MPa～90 MPa
                if (bean.carbonize < 60) {
                    strCar = "<60";
                } else if (bean.carbonize > 90) {
                    strCar = ">90";
                }
                break;
            case 17://上海结构混凝土曲线（泵送）10MPa～70 MPa
                if (bean.carbonize < 10) {
                    strCar = "<10";
                } else if (bean.carbonize > 70) {
                    strCar = ">70";
                }
                break;
            case 24://四川高强度 5.5J（泵送）50MPa～100 MPa （构件中 出现测区强度无法查出(如 c cu f ＜50.0MPa 或 c cu f ＞100.0MPa)时，因无法计算平均值及标准差，也只能 24 以最小值作为该强度推定值）
                if (bean.carbonize < 50) {
                    strCar = "<50";
                } else if (bean.carbonize > 100) {
                    strCar = ">100";
                }
                break;
            case 25://贵州曲线
                if (bean.isPumping == 0) {  //非泵送 10MPa～50 MPa
                    if (bean.carbonize < 10) {
                        strCar = "<10";
                    } else if (bean.carbonize > 50) {
                        strCar = ">50";
                    }
                } else {  //泵送 10MPa～50 MPa
                    if (bean.carbonize < 10) {
                        strCar = "<10";
                    } else if (bean.carbonize > 60) {
                        strCar = ">60";
                    }
                }
                break;
        }
        return strCar;
    }

    /**
     * 变异系数
     */
    public static boolean getVariation(double average, double variation) {
        boolean flag = false;
        if (average <= 25) {
            if (variation > 0.2) {
                flag = true;
            }
        } else if (average <= 45) {
            if (variation > 0.15) {
                flag = true;
            }
        } else if (average <= 60) {
            if (variation > 0.12) {
                flag = true;
            }
        } else if (average <= 80) {
            if (variation > 0.1) {
                flag = true;
            }
        }
        return flag;
    }

    public static String getDesignStrength(String level) {
        //"", "C20", "C25", "C30", "C35", "C40", "C45", "C50", "C55", "C60", "C65", "C70",
        // "C75", "C80", "C90", "C100", "C——"
        switch (level) {
            case "C15":
                return "15≤fcu，k<20";
            case "C20":
                return "20≤fcu，k<25";
            case "C25":
                return "25≤fcu，k<30";
            case "C30":
                return "30≤fcu，k<35";
            case "C35":
                return "35≤fcu，k<40";
            case "C40":
                return "40≤fcu，k<45";
            case "C45":
                return "45≤fcu，k<50";
            case "C50":
                return "50≤fcu，k<55";
            case "C55":
                return "55≤fcu，k<60";
            case "C60":
                return "60≤fcu，k<65";
            case "C65":
                return "65≤fcu，k<70";
            case "C70":
                return "70≤fcu，k<75";
            case "C75":
                return "75≤fcu，k<80";
            case "C80":
                return "80≤fcu，k<90";
            case "C90":
                return "90≤fcu，k<100";
            case "C100":
                return "100≤fcu，k<100+";
            default:
                return "--≤fcu，k<--";
        }
    }

    /**
     * 求内插数
     */
    public static int getInterpolationValueInt(double x1, double y1, double x2, double y2, double x) {
        // K＝(y2-y1)/(x2-x1)
        double K = (y2 - y1) / (x2 - x1);
        // y-y0=k(x-x0)
        return (int) (K * (x - x1) + y1);
    }

    /**
     * 求内插数
     */
    public static double getInterpolationValue(double x1, double y1, double x2, double y2, double x) {
        // K＝(y2-y1)/(x2-x1)
        double K = (y2 - y1) / (x2 - x1);
        // y-y0=k(x-x0)
        return K * (x - x1) + y1;
    }

    /**
     * 求内插数
     */
    public static double getInterpolationValueBBB(double x1, double y1, double x2, double y2, double x) {
        // K＝(y2-y1)/(x2-x1)
        double K = (y2 - y1) / (x2 - x1);
        // y-y0=k(x-x0)
        double temp = K * (x - x1) + y1;
        return Math.min(temp, y2);
    }

    public static double getMax(double[] array) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("数组不能为空或长度为0");
        }

        double max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    public static double getMax(int[] array) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("数组不能为空或长度为0");
        }

        double max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    public static String ByteArraytoHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String bs = String.format("%02X ", b);
            sb.append(bs);
        }
        return sb.toString();
    }

    public static int getPoleTypeIndex(String string) {
        for (int i = 0; i < StaticConstant.configPoleTypeList.size(); i++) {
            if (TextUtils.equals(string, StaticConstant.configPoleTypeList.get(i))) {
                return i;
            }
        }
        return 0;
    }
}

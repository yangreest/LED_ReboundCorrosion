package com.example.gtj_f230_rebound_corrosion.base.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.text.Html;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.corrosion.model.CPointBean;
import com.example.gtj_f230_rebound_corrosion.f230.model.PointBean;
import com.example.gtj_f230_rebound_corrosion.f230.model.SlitWidthBean;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViewUtils {

    public static void hide_keyboard_from(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 格式化数字
     */
    public static void numberValueFilter(CharSequence s, int maxLength, int decimalLength, EditText editText) {
        //删除“.”后面超过2位后的数据
        if (s.toString().contains(".")) {
            if (s.length() - 1 - s.toString().indexOf(".") > decimalLength) {
                s = s.toString().subSequence(0,
                        s.toString().indexOf(".") + decimalLength + 1);
                editText.setText(s);
                editText.setSelection(s.length()); //光标移到最后
            }
        }
        //输入最大位数(不包含正负符号和小数点)
        int size = getTextLength(s.toString(), new String[]{".", "-"});
        CharSequence s1 = s.toString().subSequence(0,
                s.toString().length() - 1);
        if (size == maxLength) {
            if (s.toString().endsWith(".")) {
                s = s1;
                editText.setText(s);
                editText.setSelection(s.length()); //光标移到最后
            }
        } else if (size > maxLength) {
            s = s1;
            editText.setText(s);
            editText.setSelection(s.length()); //光标移到最后
        }
        //如果"."在起始位置,则起始位置自动补0
        if (s.toString().startsWith("+")) {
            s = "";
            editText.setText(s);
        } else
            //如果"."在起始位置,则起始位置自动补0
            if (s.toString().trim().equals(".")) {
                s = "0" + s;
                editText.setText(s);
                editText.setSelection(2);
            } else if (s.toString().trim().equals("-.")) {
                s = "-0.";
                editText.setText(s);
                editText.setSelection(3);
            } else
                //如果起始位置为0,且第二位跟的不是".",则无法后续输入
                if (s.toString().startsWith("0")
                        && s.toString().trim().length() > 1) {
                    if (!s.toString().substring(1, 2).equals(".")) {
                        editText.setText(s.subSequence(0, 1));
                        editText.setSelection(1);
                    }
                }
    }

    private static int getTextLength(String string, String[] ignores) {
        int size = 0;
        if (string.length() > 0) {
            int ignoreSize = 0;
            if (ignores != null) {
                for (String ignore : ignores) {
                    if (string.contains(ignore)) {
                        ignoreSize++;
                    }
                }
            }
            size = string.length() - ignoreSize;
        }
        return size;
    }

    /**
     * 禁止EditText输入特殊字符
     */
    public static void setEditTextInhibitInputSpeChat(EditText editText) {
        InputFilter filter = (source, start, end, dest, dstart, dend) -> {
            String speChat = "[:*?\"<>|/\\\\]";
            Pattern pattern = Pattern.compile(speChat);
            Matcher matcher = pattern.matcher(source.toString());
            if (matcher.find()) return "";
            else return null;
        };
        editText.setFilters(new InputFilter[]{filter});
    }

    public static Spanned getTextColor(String str1, String str2, String color) {
        return Html.fromHtml("<font color=#000000>" + str1 + "</font><font color=" + color + ">" + str2 + "</font>");
    }

    public static SpannableString setTextColor(String str1, String str2, int color) {
        SpannableString spannableString = new SpannableString(str1 + str2);
        spannableString.setSpan(new ForegroundColorSpan(color), TextUtils.isEmpty(str1) ? 0 : str1.length(), str1.length()+str2.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    public static void setTextViewStyles(TextView... params) {
        for (int i = 0; i < params.length; i++) {
            LinearGradient mLinearGradient = new LinearGradient(0, 0, 0, params[i].getPaint().getTextSize(),
                    Color.parseColor("#6a11cb"), Color.parseColor("#2575fc"), Shader.TileMode.CLAMP);
            params[i].getPaint().setShader(mLinearGradient);
            params[i].invalidate();
        }
    }

    /**
     * @param tempPaint:圆点
     * @param radius：半径
     * @param tempAngle：角度
     * @return 角度点
     */
    @NonNull
    public static PointBean getCircleArcPoint(PointBean tempPaint, float radius, float tempAngle) {
        if (tempAngle > 360) {
            tempAngle = tempAngle % 360;
        }
        if (tempAngle >= 270 && tempAngle < 360) {
            float arcWidth = (float) (radius * Math.sin((tempAngle % 90) * Math.PI / 180));
            float arcHeight = (float) (radius * Math.sin((90 - tempAngle % 90) * Math.PI / 180));
            return new PointBean(tempPaint.x + arcWidth, tempPaint.y - arcHeight);
        } else if (tempAngle >= 180 && tempAngle < 270) {// 240
            float arcWidth = (float) (radius * Math.sin((90 - tempAngle % 90) * Math.PI / 180));
            float arcHeight = (float) (radius * Math.sin((tempAngle % 90) * Math.PI / 180));
            return new PointBean(tempPaint.x - arcWidth, tempPaint.y - arcHeight);
        } else if (tempAngle >= 90 && tempAngle < 180) {
            float arcWidth = (float) (radius * Math.sin((tempAngle % 90) * Math.PI / 180));
            float arcHeight = (float) (radius * Math.sin((90 - tempAngle % 90) * Math.PI / 180));
            return new PointBean(tempPaint.x - arcWidth, tempPaint.y + arcHeight);
        } else {
            float arcWidth = (float) (radius * Math.sin((90 - tempAngle % 90) * Math.PI / 180));
            float arcHeight = (float) (radius * Math.sin((tempAngle % 90) * Math.PI / 180));
            return new PointBean(tempPaint.x + arcWidth, tempPaint.y + arcHeight);
        }
    }

    /**
     * 获取角度
     *
     * @param pointFixed 固定点
     * @param pointSelf  三角点
     * @return 三角点相对于固定点的角度
     */
    public static double getAngle(PointBean pointFixed, PointBean pointSelf) {
        double angle;
        if (pointSelf.x == pointFixed.x && pointSelf.y < pointFixed.y) {
            angle = 90;
        } else if (pointSelf.x == pointFixed.x && pointSelf.y > pointFixed.y) {
            angle = 270;
        } else if (pointSelf.x > pointFixed.x && pointSelf.y == pointFixed.y) {
            angle = 180;
        } else if (pointSelf.x < pointFixed.x && pointSelf.y == pointFixed.y) {
            angle = 0;
        } else {
            double atan = Math.atan2(Math.abs(pointSelf.y - pointFixed.y), Math.abs(pointSelf.x - pointFixed.x));
            angle = 180 / Math.PI * atan + 180;
            if (pointSelf.x > pointFixed.x && pointSelf.y < pointFixed.y) {
                //第一象限
                angle = 360 - angle;
            } else if (pointSelf.x > pointFixed.x && pointSelf.y > pointFixed.y) {
                //第二象限
                angle = 0 + angle;
            } else if (pointSelf.x < pointFixed.x && pointSelf.y > pointFixed.y) {
                //第三象限
                angle = 180 - angle;
            } else if (pointSelf.x < pointFixed.x && pointSelf.y < pointFixed.y) {
                //第四象限
                angle = 180 + angle;
            }
        }
        return angle;
    }

    /**
     * 获取三角点位置补偿（x、y）
     *
     * @param pointFixed 固定点
     * @param pointSelf  三角点
     * @return 三角点相对于固定点的角度
     */
    public static PointBean getOffset(PointBean pointFixed, PointBean pointSelf) {
        double x = 0, y = 0;
        if (pointSelf.x == pointFixed.x && pointSelf.y < pointFixed.y) {
            //angle = 90
            x = 0;
            y = 28;
        } else if (pointSelf.x == pointFixed.x && pointSelf.y > pointFixed.y) {
//            angle = 270;
            x = 0;
            y = -28;
        } else if (pointSelf.x > pointFixed.x && pointSelf.y == pointFixed.y) {
//            angle = 180;
            x = 28;
            y = 0;

        } else if (pointSelf.x < pointFixed.x && pointSelf.y == pointFixed.y) {
//            angle = 0;
            x = -28;
            y = 0;
        } else {
            double atan = Math.atan2(Math.abs(pointSelf.y - pointFixed.y), Math.abs(pointSelf.x - pointFixed.x));
            if (pointSelf.x > pointFixed.x && pointSelf.y < pointFixed.y) {
                //第一象限
                x = Math.cos(atan) * 28;
                y = -Math.sin(atan) * 28;
            } else if (pointSelf.x > pointFixed.x && pointSelf.y > pointFixed.y) {
                //第二象限
                x = Math.cos(atan) * 28;
                y = Math.sin(atan) * 28;
            } else if (pointSelf.x < pointFixed.x && pointSelf.y > pointFixed.y) {
                //第三象限
                x = -Math.cos(atan) * 28;
                y = Math.sin(atan) * 28;
            } else if (pointSelf.x < pointFixed.x && pointSelf.y < pointFixed.y) {
                //第四象限
                x = -Math.cos(atan) * 28;
                y = -Math.sin(atan) * 28;
            }
        }
        return new PointBean(x, y);
    }

    /**
     * 获取垂直裂缝的2个箭头点
     */
    public static List<PointBean> getVerticalOffset(SlitWidthBean slitWidthBean, float viewRatio, float Y) {
        PointBean pointSelf = slitWidthBean.p1;
        PointBean pointFixed = slitWidthBean.p2;
        List<PointBean> beanList = new ArrayList<>();
        if (pointSelf.x == pointFixed.x) {
            //angle = 90
            beanList.add(new PointBean(viewRatio * slitWidthBean.start, Y));
            beanList.add(new PointBean(viewRatio * slitWidthBean.end, Y));
        } else {
            double atan = Math.atan2(Math.abs(pointSelf.y - pointFixed.y), Math.abs(pointSelf.x - pointFixed.x));
            if (pointSelf.x > pointFixed.x) {
                //第一象限
                float b = viewRatio * (slitWidthBean.width >> 1);
                PointBean pointB = new PointBean(viewRatio * (slitWidthBean.end - (slitWidthBean.width >> 1)), Y);
                float a = (float) (b * Math.tan(atan));
                PointBean pointA = new PointBean(pointB.x, Y + a);
                float d = (float) (a * Math.sin(atan));
                float e = (float) (d * Math.sin(atan));
                float f = (float) (d * Math.cos(atan));
                PointBean pointD = new PointBean(pointA.x + f, pointA.y - e);
                PointBean pointTop = new PointBean(pointB.x - (pointD.x - pointB.x), pointB.y - (pointD.y - pointB.y));
                beanList.add(pointTop);
                beanList.add(pointD);
            } else if (pointSelf.x < pointFixed.x) {
                //第四象限
                float b = viewRatio * (slitWidthBean.width >> 1);
                PointBean pointB = new PointBean(viewRatio * (slitWidthBean.end - (slitWidthBean.width >> 1)), Y);
                float a = (float) (b * Math.tan(atan));
                PointBean pointA = new PointBean(pointB.x, Y + a);
                float d = (float) (a * Math.sin(atan));
                float e = (float) (d * Math.sin(atan));
                float f = (float) (d * Math.cos(atan));
                PointBean pointD = new PointBean(pointA.x - f, pointA.y - e);
                PointBean pointTop = new PointBean(pointB.x + Math.abs(pointD.x - pointB.x), pointB.y - Math.abs(pointD.y - pointB.y));
                beanList.add(pointTop);
                beanList.add(pointD);
            }
        }
        return beanList;
    }

    public static List<PointBean> getVerticalOffset(SlitWidthBean slitWidthBean, float viewRatio) {
        List<PointBean> beanList = new ArrayList<>();
        beanList.add(new PointBean(viewRatio * slitWidthBean.start, 0));
        beanList.add(new PointBean(viewRatio * slitWidthBean.end, 0));
        return beanList;
    }

    /**
     * @param : tempPaint:圆点
     * @param : radius：半径
     * @param : tempAngle：角度
     * @return : 计算当前角度点位置
     */
    @NonNull
    public static CPointBean getCircleArcPoint(CPointBean tempPaint, float radius, float tempAngle) {
        if (tempAngle > 360) {
            tempAngle = tempAngle % 360;
        }
        if (tempAngle >= 270 && tempAngle < 360) {
            float arcWidth = (float) (radius * Math.sin((tempAngle % 90) * Math.PI / 180));
            float arcHeight = (float) (radius * Math.sin((90 - tempAngle % 90) * Math.PI / 180));
            return new CPointBean(tempPaint.x + arcWidth, tempPaint.y - arcHeight);
        } else if (tempAngle >= 180 && tempAngle < 270) {// 240
            float arcWidth = (float) (radius * Math.sin((90 - tempAngle % 90) * Math.PI / 180));
            float arcHeight = (float) (radius * Math.sin((tempAngle % 90) * Math.PI / 180));
            return new CPointBean(tempPaint.x - arcWidth, tempPaint.y - arcHeight);
        } else if (tempAngle >= 90 && tempAngle < 180) {
            float arcWidth = (float) (radius * Math.sin((tempAngle % 90) * Math.PI / 180));
            float arcHeight = (float) (radius * Math.sin((90 - tempAngle % 90) * Math.PI / 180));
            return new CPointBean(tempPaint.x - arcWidth, tempPaint.y + arcHeight);
        } else {
            float arcWidth = (float) (radius * Math.sin((90 - tempAngle % 90) * Math.PI / 180));
            float arcHeight = (float) (radius * Math.sin((tempAngle % 90) * Math.PI / 180));
            return new CPointBean(tempPaint.x + arcWidth, tempPaint.y + arcHeight);
        }
    }
}

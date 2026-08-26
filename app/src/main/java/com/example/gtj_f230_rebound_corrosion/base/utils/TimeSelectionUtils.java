package com.example.gtj_f230_rebound_corrosion.base.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.view.TimePickerView;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

@SuppressLint("StaticFieldLeak")
public class TimeSelectionUtils {
    public static final String DATE = "date";
    public static final String TIME = "time";
    public static final String DATETIME = "datetime";
    private static TimePickerView timePickerView;

    public interface OnSelectListener1 {
        void onSelect(String string);
    }

    /**
     * 注意事项：
     * 1.自定义布局中，id为 optionspicker 或者 timepicker 的布局以及其子控件必须要有，否则会报空指针.
     * 具体可参考demo 里面的两个自定义layout布局。
     * 2.因为系统Calendar的月份是从0-11的,所以如果是调用Calendar的set方法来设置时间,月份的范围也要是从0-11
     * setRangDate方法控制起始终止时间(如果不设置范围，则使用默认时间1900-2100年，此段代码可注释)
     */
    @SuppressLint("SimpleDateFormat")
    public static void showTimePicker(Context context, String dataType, OnSelectListener1 onSelectListener1) {
        Calendar selectedDate = Calendar.getInstance();//系统当前时间
        selectedDate.add(Calendar.MINUTE, 5);
        Date tempDate = selectedDate.getTime();
        //
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        startDate.set(2000, 0, 1);
        endDate.set(2030, 11, 31);
        boolean[] showItem;
        switch (dataType) {
            case DATE:
                showItem = new boolean[]{true, true, true, false, false, false};
                break;
            case TIME:
                showItem = new boolean[]{false, false, false, true, true, false};
                break;
            default:
                showItem = new boolean[]{true, true, true, true, true, false};
                break;
        }
        timePickerView = new TimePickerBuilder(context, (date, v) -> {
            String string = "";
            switch (dataType) {
                case DATE:
                    string = StringUtils.getStringTime(date.getTime(), "yyyy-MM-dd");
                    break;
                case TIME:
                    string = StringUtils.getStringTime(date.getTime(), "HH:mm");
                    break;
                case DATETIME:
                    string = StringUtils.getStringTime(date.getTime(), "yyyy-MM-dd HH:mm:SS");
                    break;
            }
            onSelectListener1.onSelect(string);
        })
                .setDate(selectedDate)
                .setRangDate(startDate, endDate)
                .setTitleColor(context.getResources().getColor(R.color.color999))
                .setTitleBgColor(context.getResources().getColor(R.color.colorFFF))
                .setSubmitText("确定")
                .setCancelText("取消")
                .setSubCalSize(8)
                .setSubmitColor(context.getResources().getColor(R.color.color333))
                .setCancelColor(context.getResources().getColor(R.color.color999))
                .setTextColorCenter(context.getResources().getColor(R.color.color333)) //设置选中项文字颜色
                .setType(showItem)
                .setGravity(Gravity.BOTTOM)
                .setLabel("年", "月", "日", "时", "分", "秒")
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .setContentTextSize(8)
                .setDividerColor(context.getResources().getColor(R.color.color_0C8B8A))
                .build();
        RelativeLayout topBar = (RelativeLayout) timePickerView.findViewById(com.bigkoo.pickerview.R.id.rv_topbar);
        ViewGroup.LayoutParams layoutParams = topBar.getLayoutParams();
        layoutParams.height = 80;
        topBar.setLayoutParams(layoutParams);
        timePickerView.findViewById(com.bigkoo.pickerview.R.id.btnCancel).setOnClickListener(v -> {
            timePickerView.dismiss();
        });
        timePickerView.show();
    }

    public interface SelectionTime {
        void selectTime(String string);
    }

    @SuppressLint("SimpleDateFormat")
    public static void showTimePicker(Context context, String dataType, String time, SelectionTime selectionTime) {
        Calendar selectedDate = Calendar.getInstance();//系统当前时间
        if (!TextUtils.isEmpty(time)) {
            try {
                selectedDate.setTime(Objects.requireNonNull(new SimpleDateFormat("yyyy-mm-dd").parse(time)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        startDate.set(2000, 11, 31);
        endDate.set(2030, 11, 31);
        boolean[] showItem;
        switch (dataType) {
            case DATE:
                showItem = new boolean[]{true, true, true, false, false, false};
                break;
            case TIME:
                showItem = new boolean[]{false, false, false, true, true, false};
                break;
            default:
                showItem = new boolean[]{true, true, true, true, true, false};
                break;
        }
        timePickerView = new TimePickerBuilder(context, (date, v) -> {
            String string = "";
            switch (dataType) {
                case DATE:
                    string = StringUtils.getStringTime(date.getTime(), "yyyy-MM-dd");
                    break;
                case TIME:
                    string = StringUtils.getStringTime(date.getTime(), "HH:mm");
                    break;
                case DATETIME:
                    string = StringUtils.getStringTime(date.getTime(), "yyyy-MM-dd HH:mm:SS");
                    break;
            }
            selectionTime.selectTime(string);
        })
                .setDate(selectedDate)
                .setRangDate(startDate, endDate)
                .setTitleColor(context.getResources().getColor(R.color.color999))
                .setTitleBgColor(context.getResources().getColor(R.color.colorFFF))
                .setSubmitText("确定")
                .setCancelText("取消")
                .setSubmitColor(context.getResources().getColor(R.color.color333))
                .setCancelColor(context.getResources().getColor(R.color.color999))
                .setTextColorCenter(context.getResources().getColor(R.color.color333)) //设置选中项文字颜色
                .setType(showItem)
                .setGravity(Gravity.BOTTOM)
                .setLabel("年", "月", "日", "时", "分", "秒")
                .isCenterLabel(true) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .setDividerColor(context.getResources().getColor(R.color.color_0C8B8A))
                .build();
        timePickerView.findViewById(com.bigkoo.pickerview.R.id.btnCancel).setOnClickListener(v -> timePickerView.dismiss());
        timePickerView.show();
    }
}

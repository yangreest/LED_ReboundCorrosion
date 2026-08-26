package com.example.gtj_f230_rebound_corrosion.base.utils;

import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.example.gtj_f230_rebound_corrosion.R;

import java.util.List;

public class OptionsPickerViewUtils {
    public interface OnSelectListener1 {
        void onSelect(int position);
    }

    public interface OnSelectListener2 {
        void onSelect(int position);

        void onEmpty();
    }

    public static void showSingleColumnPickerView(Context context, String title, String label, final List<String> stringList,
                                                  String currentString, final OnSelectListener1 onSelectListener) {
        int index = optionsItem1Index(currentString, stringList);
        final OptionsPickerView optionsPickerView = new OptionsPickerBuilder(context, (options1, options2, options3, v) -> onSelectListener.onSelect(options1)).setTitleText(title)
                .setTitleColor(context.getResources().getColor(R.color.color999))
                .setTitleSize(22)
                .setTitleBgColor(context.getResources().getColor(R.color.colorWhite))
                .setSubmitText("确定")
                .setCancelText("取消")
                .setSubCalSize(22)
                .setSubmitColor(context.getResources().getColor(R.color.color333))
                .setCancelColor(context.getResources().getColor(R.color.color999))
                .setDividerColor(context.getResources().getColor(R.color.color333))
                .setLineSpacingMultiplier(2)
                .setTextColorCenter(context.getResources().getColor(R.color.color000)) //设置选中项文字颜色
                .setContentTextSize(22)
                .setLabels(label, null, null)
                .build();
        RelativeLayout topBar = (RelativeLayout) optionsPickerView.findViewById(com.bigkoo.pickerview.R.id.rv_topbar);
        ViewGroup.LayoutParams layoutParams = topBar.getLayoutParams();
        layoutParams.height = 80;
        topBar.setLayoutParams(layoutParams);
        optionsPickerView.setPicker(stringList);
        optionsPickerView.setSelectOptions(index);
        optionsPickerView.findViewById(com.bigkoo.pickerview.R.id.btnCancel).setOnClickListener(v -> {
            optionsPickerView.dismiss();
            //可以处理清空操作
        });
        optionsPickerView.show();
    }

    public static void showSingleColumnPickerView(Context context, String title, String label, final List<String> stringList,
                                                  String currentString, final OnSelectListener2 onSelectListener) {
        int index = optionsItem1Index(currentString, stringList);
        final OptionsPickerView optionsPickerView = new OptionsPickerBuilder(context, (options1, options2, options3, v) -> onSelectListener.onSelect(options1)).setTitleText(title)
                .setTitleColor(context.getResources().getColor(R.color.color999))
                .setTitleSize(22)
                .setTitleBgColor(context.getResources().getColor(R.color.colorWhite))
                .setSubmitText("确定")
                .setCancelText("清空")
                .setSubCalSize(22)
                .setSubmitColor(context.getResources().getColor(R.color.color333))
                .setCancelColor(context.getResources().getColor(R.color.color999))
                .setDividerColor(context.getResources().getColor(R.color.color333))
                .setLineSpacingMultiplier(2)
                .setTextColorCenter(context.getResources().getColor(R.color.color000)) //设置选中项文字颜色
                .setContentTextSize(22)
                .setLabels(label, null, null)
                .build();
        optionsPickerView.setPicker(stringList);
        optionsPickerView.setSelectOptions(index);
        optionsPickerView.findViewById(com.bigkoo.pickerview.R.id.btnCancel).setOnClickListener(v -> {
            optionsPickerView.dismiss();
            //可以处理清空操作
            onSelectListener.onEmpty();
        });
        optionsPickerView.show();
    }

    private static int optionsItem1Index(String item, List<String> stringList) {
        int index = 0;
        if (!TextUtils.isEmpty(item)) {
            for (int i = 0; i < stringList.size(); i++) {
                if (TextUtils.equals(item, stringList.get(i))) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }
}

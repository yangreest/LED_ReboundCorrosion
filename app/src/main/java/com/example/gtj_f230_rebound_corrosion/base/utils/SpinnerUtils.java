package com.example.gtj_f230_rebound_corrosion.base.utils;


import com.example.gtj_f230_rebound_corrosion.corrosion.view.SpinnerStaticView;

import java.util.List;

public class SpinnerUtils {

    public interface StaticSelectedListener {
        void onItemSelected(SpinnerStaticView spinner, String text, int position);
    }

    public static void attachDataSource(SpinnerStaticView spinner, List<String> list, StaticSelectedListener listener) {
        spinner.setContent(list);
        spinner.setOnItemSelectedListener((adapterView, view, position, id, content) -> {
            if (listener != null) {
                listener.onItemSelected(spinner, list.get(position), position);
            }
        });
    }
}

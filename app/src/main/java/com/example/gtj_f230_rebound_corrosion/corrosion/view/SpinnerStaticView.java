package com.example.gtj_f230_rebound_corrosion.corrosion.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.example.gtj_f230_rebound_corrosion.R;

import java.util.ArrayList;
import java.util.List;

public class SpinnerStaticView extends androidx.appcompat.widget.AppCompatSpinner {
    private ArrayAdapter<String> adapter;
    private ArrayList<String> contents = new ArrayList<>();
    public String selectedContent = "";

    public SpinnerStaticView(Context context) {
        super(context);
    }

    public SpinnerStaticView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SpinnerStaticView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setContent(List<String> contents) {
        this.contents.clear();
        this.contents.addAll(contents);
        if (adapter != null) {
            adapter = null;
        }
        adapter = new ArrayAdapter<>(getContext(), R.layout.sp_item_select, contents.toArray(new String[]{}));
        adapter.setDropDownViewResource(R.layout.sp_item_dropdown);
        this.setAdapter(adapter);
    }

    public void setDefaultContent(String defaultContent) {
        setSelectedContent(defaultContent);
        for (int i = 0; i < contents.size(); i++) {
            if (TextUtils.equals(defaultContent, contents.get(i))) {
                this.setSelection(i);
                break;
            }
        }
    }

    public void setSelectedContent(String selectedContent) {
        this.selectedContent = selectedContent;
    }

    public String getSelectedContent() {
        return this.selectedContent;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onItemSelectedListener.onItemSelected(parent, view, position, id, contents.get(position));
                setSelectedContent(contents.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public interface OnItemSelectedListener {
        void onItemSelected(AdapterView<?> adapterView, View view, int position, long id, String content);
    }
}

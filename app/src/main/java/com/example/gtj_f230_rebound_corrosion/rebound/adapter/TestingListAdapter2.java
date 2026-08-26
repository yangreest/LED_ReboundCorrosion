package com.example.gtj_f230_rebound_corrosion.rebound.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.example.gtj_f230_rebound_corrosion.R;

public class TestingListAdapter2 extends BaseQuickAdapter<Integer, QuickViewHolder> {
    public TestingListAdapter2() {
        super();
    }

    @Override
    protected QuickViewHolder onCreateViewHolder(Context context, ViewGroup parent, int viewType) {
        return new QuickViewHolder(LayoutInflater.from(context).inflate(R.layout.item_testing_list2, parent, false));
    }

    @Override
    protected void onBindViewHolder(QuickViewHolder holder, int position, Integer item) {
        holder.setText(R.id.text, item == 0 ? "--" : item + "");
    }
}
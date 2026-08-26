package com.example.gtj_f230_rebound_corrosion.base.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.example.gtj_f230_rebound_corrosion.R;
import com.example.gtj_f230_rebound_corrosion.base.model.ProjectBean;
import com.example.gtj_f230_rebound_corrosion.base.utils.EmptyDataViewUtils;

public class ProjectListAdapter extends BaseQuickAdapter<ProjectBean, QuickViewHolder> {

    public ProjectListAdapter(Context context) {
        super();
    }

    @Override
    protected QuickViewHolder onCreateViewHolder(Context context, ViewGroup parent, int viewType) {
        return new QuickViewHolder(LayoutInflater.from(context).inflate(R.layout.item_project_list, parent, false));
    }

    public void setEmptyView(Activity activity) {
        this.setStateView(EmptyDataViewUtils.getEmptyView(activity, false));
    }

    @Override
    protected void onBindViewHolder(QuickViewHolder holder, int position, ProjectBean item) {
        holder.setText(R.id.tv_title, item.name);
        holder.setText(R.id.tv_content, "工程地址：" + item.address);
    }
}
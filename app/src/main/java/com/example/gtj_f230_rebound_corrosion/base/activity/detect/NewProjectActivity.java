package com.example.gtj_f230_rebound_corrosion.base.activity.detect;

import android.annotation.SuppressLint;
import android.view.Window;

import com.blankj.utilcode.util.ToastUtils;
import com.example.gtj_f230_rebound_corrosion.base.activity.BaseActivity;
import com.example.gtj_f230_rebound_corrosion.base.model.ProjectBean;
import com.example.gtj_f230_rebound_corrosion.base.storage.GreenDaoHelper;
import com.example.gtj_f230_rebound_corrosion.base.utils.OtherUtils;
import com.example.gtj_f230_rebound_corrosion.databinding.ActNewProjectBinding;
import com.example.gtj_f230_rebound_corrosion.functions.database.greenDao.db.ProjectBeanDao;

import java.util.List;

@SuppressLint("SetTextI18n")
public class NewProjectActivity extends BaseActivity<ActNewProjectBinding> {

    @Override
    protected ActNewProjectBinding getBinding() {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        return ActNewProjectBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initLabel() {
        binding.etName.post(() -> OtherUtils.showSoftKeyboard(binding.etName));
    }

    @Override
    protected void initView() {
        binding.button.setOnClickListener(v -> createProject());
    }

    private void createProject() {
        String strProjectName = binding.etName.getText().toString().trim();
        if (strProjectName.isEmpty()) {
            ToastUtils.showShort("请输入工程名称");
            return;
        }
        String strProjectAddress = binding.etAddress.getText().toString().trim();
        if (strProjectAddress.isEmpty()) {
            ToastUtils.showShort("请输入工程地址");
            return;
        }
        List<ProjectBean> projectBeanList = GreenDaoHelper.getDaoSession(this).getProjectBeanDao().queryBuilder().where(ProjectBeanDao.Properties.Name.eq(strProjectName)).list();

        if (!projectBeanList.isEmpty()) {
            ToastUtils.showShort("已存在相同的工程名称，请修改");
            return;
        }
        GreenDaoHelper.getDaoSession(this).getProjectBeanDao().insert(new ProjectBean(strProjectName, strProjectAddress));
        ToastUtils.showShort("创建成功");
        setResult(RESULT_OK);
        finish();
    }
}

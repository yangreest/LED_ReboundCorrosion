package com.blackhao.utillibrary.mvp;

import android.view.View;

/**
 * Author ： BlackHao
 * Time : 2019/4/8 09:11
 * Description : 空白的 Fragment，主要用于不使用 MVP 模式，继承此类即可
 */
public abstract class BaseFragment extends BaseMvpFragment<BlankContract.PresenterImpl>
        implements BlankContract.ViewImpl, View.OnClickListener {

    @Override
    protected BlankContract.PresenterImpl initPresenter() {
        // 返回一个空的 Presenter 实现，避免空指针异常
        return new BlankContract.PresenterImpl(getActivity(), this);
    }
}
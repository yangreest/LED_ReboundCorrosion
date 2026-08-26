package com.blackhao.utillibrary.mvp;

import android.content.Context;

import java.lang.ref.WeakReference;

/**
 * Author ： BlackHao
 * Time : 2018/12/20 09:05
 * Description : Presenter 抽象实现类
 */
public abstract class BaseMvpPresenterImpl<T extends BaseMvpViewImpl> implements BaseMvpPresenter {

    /**
     * 弱引用持有,避免造成内存泄漏
     */
    private WeakReference<T> mView;
    private WeakReference<Context> mContext;

    public BaseMvpPresenterImpl(Context context, T mView) {
        this.mView = new WeakReference<>(mView);
        this.mContext = new WeakReference<>(context);
    }


    public T getView() {
        return mView.get();
    }

    public Context getContext() {
        return mContext.get();
    }

    @Override
    public void onViewCreate() {

    }

    @Override
    public void onViewCreated() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestroy() {

    }

}
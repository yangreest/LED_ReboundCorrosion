package com.blackhao.utillibrary.mvp;


import android.content.Context;

/**
 * Author ： BlackHao
 * Time : 2018/12/20 10:16
 * Description : 空白 ViewImpl,PresenterImpl。
 */
public class BlankContract {

    public interface ViewImpl extends BaseMvpViewImpl {

    }

    /**
     * 空白 Presenter 实现类，用于不需要 Presenter 的 Fragment
     */
    public static class PresenterImpl extends BaseMvpPresenterImpl<ViewImpl> {

        public PresenterImpl(Context context, ViewImpl view) {
            super(context, view);
        }
    }
}

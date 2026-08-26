package com.blackhao.utillibrary.imgProxy;

import android.widget.ImageView;

import com.blackhao.utillibrary.log.LogHelper;

import java.lang.ref.WeakReference;

/**
 * Author ： BlackHao
 * Time : 2019/5/8 13:49
 * Description : 图片显示代理
 */
public class ImgDisplayUtil implements ImgProxyImpl {

    private WeakReference<ImgProxyImpl> implRef;
    //单例模式
    private static volatile ImgDisplayUtil util;
    //Log 打印
    private LogHelper log = LogHelper.getInstance();

    public static ImgDisplayUtil getInstance() {
        if (util == null) {
            synchronized (ImgDisplayUtil.class) {
                if (util == null) {
                    util = new ImgDisplayUtil();
                }
            }
        }
        return util;
    }

    private ImgDisplayUtil() {
    }

    /**
     * 初始化
     */
    public void init(ImgProxyImpl impl) {
        this.implRef = new WeakReference<>(impl);
    }

    /**
     * 清理引用
     */
    public void clear() {
        if (implRef != null) {
            implRef.clear();
            implRef = null;
        }
    }

    /**
     * 获取实现对象
     */
    private ImgProxyImpl getImpl() {
        return implRef != null ? implRef.get() : null;
    }

    /**
     * 检查实现是否可用
     */
    private boolean isImplAvailable() {
        if (getImpl() == null) {
            log.e("ImgDisplayUtil Not Init");
            return false;
        }
        return true;
    }

    @Override
    public void displayAvatar(ImageView iv, String url) {
        if (!isImplAvailable()) return;
        getImpl().displayAvatar(iv, url);
    }

    @Override
    public void displayAvatar(ImageView iv, String url, int loadErrorResId) {
        if (!isImplAvailable()) return;
        getImpl().displayAvatar(iv, url, loadErrorResId);
    }

    @Override
    public void displayAvatar(ImageView iv, int resId) {
        if (!isImplAvailable()) return;
        getImpl().displayAvatar(iv, resId);
    }

    @Override
    public void displayImg(ImageView iv, String url) {
        if (!isImplAvailable()) return;
        getImpl().displayImg(iv, url);
    }

    @Override
    public void displayImg(ImageView iv, int resId) {
        if (!isImplAvailable()) return;
        getImpl().displayImg(iv, resId);
    }

    @Override
    public void displayImg(ImageView iv, String url, int loadErrorResId) {
        if (!isImplAvailable()) return;
        getImpl().displayImg(iv, url, loadErrorResId);
    }
}

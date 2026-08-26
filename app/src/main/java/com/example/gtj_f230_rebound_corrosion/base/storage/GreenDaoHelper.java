package com.example.gtj_f230_rebound_corrosion.base.storage;

import android.content.Context;

import com.example.gtj_f230_rebound_corrosion.functions.database.greenDao.db.DaoMaster;
import com.example.gtj_f230_rebound_corrosion.functions.database.greenDao.db.DaoSession;
import com.example.gtj_f230_rebound_corrosion.StaticConstant;

public class GreenDaoHelper {
    private static DaoMaster daoMaster;
    private static DaoSession daoSession;

    /**
     * 获取DaoMaster
     */
    public static DaoMaster getDaoMaster(Context context) {
        if (daoMaster == null) {
            try {
                DaoOpenHelper helper = new DaoOpenHelper(context, StaticConstant.DB_NAME, null);
                daoMaster = new DaoMaster(helper.getWritableDatabase());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return daoMaster;
    }

    /**
     * 获取DaoSession对象
     */
    public static DaoSession getDaoSession(Context context) {

        if (daoSession == null) {
            if (daoMaster == null) {
                getDaoMaster(context);
            }
            daoSession = daoMaster.newSession();
        }
        return daoSession;
    }
}

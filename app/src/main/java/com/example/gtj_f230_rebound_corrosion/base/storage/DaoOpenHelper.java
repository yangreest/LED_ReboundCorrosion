package com.example.gtj_f230_rebound_corrosion.base.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.gtj_f230_rebound_corrosion.functions.database.greenDao.db.ConfigBeanDao;
import com.example.gtj_f230_rebound_corrosion.functions.database.greenDao.db.CurveMinMaxBeanDao;
import com.example.gtj_f230_rebound_corrosion.functions.database.greenDao.db.DaoMaster;
import com.example.gtj_f230_rebound_corrosion.functions.database.greenDao.db.MinMaxBean1Dao;
import com.example.gtj_f230_rebound_corrosion.functions.database.greenDao.db.ProjectBeanDao;
import com.example.gtj_f230_rebound_corrosion.functions.database.greenDao.db.Rebar240ZoneBeanDao;
import com.example.gtj_f230_rebound_corrosion.functions.database.greenDao.db.RebarMinMaxBeanDao;
import com.example.gtj_f230_rebound_corrosion.functions.database.greenDao.db.StandardCurveBeanDao;
import com.example.gtj_f230_rebound_corrosion.functions.database.greenDao.db.WidthZoneBeanDao;
import com.example.gtj_f230_rebound_corrosion.functions.database.greenDao.db.ZoneBeanDao;
import com.github.yuweiguocn.library.greendao.MigrationHelper;

import org.greenrobot.greendao.database.Database;

public class DaoOpenHelper extends DaoMaster.OpenHelper {
    DaoOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        MigrationHelper.migrate(db, new MigrationHelper.ReCreateAllTableListener() {
                    @Override
                    public void onCreateAllTables(Database db, boolean ifNotExists) {
                        DaoMaster.createAllTables(db, ifNotExists);
                    }

                    @Override
                    public void onDropAllTables(Database db, boolean ifExists) {
                        DaoMaster.dropAllTables(db, ifExists);
                    }
                }, WidthZoneBeanDao.class, ProjectBeanDao.class, ZoneBeanDao.class, StandardCurveBeanDao.class, CurveMinMaxBeanDao.class, RebarMinMaxBeanDao.class,
                ConfigBeanDao.class, Rebar240ZoneBeanDao.class, MinMaxBean1Dao.class);
    }
}
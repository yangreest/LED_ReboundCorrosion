package com.example.gtj_f230_rebound_corrosion.base.storage;

import com.example.gtj_f230_rebound_corrosion.base.model.RebounderZoneBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.greendao.converter.PropertyConverter;

public class ReboundZoneBeanConverter implements PropertyConverter<RebounderZoneBean,String> {
    private final Gson mGson;
    @Override
    public RebounderZoneBean convertToEntityProperty(String databaseValue) {
        return mGson.fromJson(databaseValue,new TypeToken<RebounderZoneBean>(){}.getType());
    }

    @Override
    public String convertToDatabaseValue(RebounderZoneBean entityProperty) {
        return mGson.toJson(entityProperty);
    }

    public ReboundZoneBeanConverter() {
        mGson = new Gson();
    }
}

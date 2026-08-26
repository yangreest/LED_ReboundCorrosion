package com.example.gtj_f230_rebound_corrosion.base.storage;

import com.example.gtj_f230_rebound_corrosion.base.model.F230ZoneBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.greendao.converter.PropertyConverter;

public class F230ZoneBeanConverter implements PropertyConverter<F230ZoneBean ,String> {
    private final Gson mGson;
    @Override
    public F230ZoneBean convertToEntityProperty(String databaseValue) {
        return mGson.fromJson(databaseValue,new TypeToken<F230ZoneBean>(){}.getType());
    }

    @Override
    public String convertToDatabaseValue(F230ZoneBean entityProperty) {
        return mGson.toJson(entityProperty);
    }

    public F230ZoneBeanConverter() {
        mGson = new Gson();
    }
}

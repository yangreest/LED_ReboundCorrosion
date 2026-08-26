package com.example.gtj_f230_rebound_corrosion.base.storage;

import com.example.gtj_f230_rebound_corrosion.base.model.CorrosionZoneBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.greendao.converter.PropertyConverter;

public class CorrosionZoneBeanConverter implements PropertyConverter<CorrosionZoneBean,String> {
    private final Gson mGson;
    @Override
    public CorrosionZoneBean convertToEntityProperty(String databaseValue) {
        return mGson.fromJson(databaseValue,new TypeToken<CorrosionZoneBean>(){}.getType());
    }

    @Override
    public String convertToDatabaseValue(CorrosionZoneBean entityProperty) {
        return mGson.toJson(entityProperty);
    }

    public CorrosionZoneBeanConverter() {
        mGson = new Gson();
    }
}

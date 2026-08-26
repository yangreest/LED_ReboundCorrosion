package com.example.gtj_f230_rebound_corrosion.base.storage;

import com.example.gtj_f230_rebound_corrosion.thickness.model.RebarPageBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.greendao.converter.PropertyConverter;

import java.util.List;

public class RebarBeanConverter implements PropertyConverter<List<RebarPageBean>, String> {
    private final Gson mGson;

    public RebarBeanConverter() {
        mGson = new Gson();
    }

    @Override
    public List<RebarPageBean> convertToEntityProperty(String databaseValue) {
        return mGson.fromJson(databaseValue, new TypeToken<List<RebarPageBean>>() {
        }.getType());
    }

    @Override
    public String convertToDatabaseValue(List<RebarPageBean> entityProperty) {
        return mGson.toJson(entityProperty);
    }
}

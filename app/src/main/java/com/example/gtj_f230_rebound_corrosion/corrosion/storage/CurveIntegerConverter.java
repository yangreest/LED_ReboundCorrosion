package com.example.gtj_f230_rebound_corrosion.corrosion.storage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.greendao.converter.PropertyConverter;


public class CurveIntegerConverter implements PropertyConverter<int[], String> {
    @Override
    public int[] convertToEntityProperty(String value) {
        return mGson.fromJson(value, new TypeToken<int[]>() {
        }.getType());
    }

    @Override
    public String convertToDatabaseValue(int[] entityProperty) {
        return mGson.toJson(entityProperty);
    }

    private final Gson mGson;

    public CurveIntegerConverter() {
        mGson = new Gson();
    }
}

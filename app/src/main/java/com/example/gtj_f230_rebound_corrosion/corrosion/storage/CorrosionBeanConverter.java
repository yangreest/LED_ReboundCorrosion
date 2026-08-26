package com.example.gtj_f230_rebound_corrosion.corrosion.storage;

import com.example.gtj_f230_rebound_corrosion.corrosion.model.CorrosionBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.greendao.converter.PropertyConverter;

import java.util.List;


public class CorrosionBeanConverter implements PropertyConverter<List<CorrosionBean>, String> {
    @Override
    public List<CorrosionBean> convertToEntityProperty(String databaseValue) {
        return mGson.fromJson(databaseValue,new TypeToken<List<CorrosionBean>>(){}.getType());
    }

    @Override
    public String convertToDatabaseValue(List<CorrosionBean> entityProperty) {
        return mGson.toJson(entityProperty);
    }
    private final Gson mGson;

    public CorrosionBeanConverter() {
        mGson = new Gson();
    }
}

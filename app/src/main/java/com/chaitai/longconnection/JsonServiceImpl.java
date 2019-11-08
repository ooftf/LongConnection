package com.chaitai.longconnection;

import android.content.Context;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.facade.service.SerializationService;
import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * @author ooftf
 * @email 994749769@qq.com
 * @date 2019/5/22 0022
 */
@Route(path = "/service/service/json")
public class JsonServiceImpl implements SerializationService {
    @Override
    public <T> T json2Object(String input, Class<T> clazz) {
        return new Gson().fromJson(input,clazz);
    }

    @Override
    public String object2Json(Object instance) {
        return new Gson().toJson(instance);
    }

    @Override
    public <T> T parseObject(String input, Type clazz) {
        return new Gson().fromJson(input,clazz);
    }

    @Override
    public void init(Context context) {

    }
}

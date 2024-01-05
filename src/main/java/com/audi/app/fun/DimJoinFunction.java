package com.audi.app.fun;

/**
 * User : LinShiYue
 * Date : 2024/1/5
 * Time : 11:21
 * Description :
 */
import com.alibaba.fastjson.JSONObject;

public interface DimJoinFunction<T> {

    String getKey(T input);

    void join(T input, JSONObject dimInfo);
}


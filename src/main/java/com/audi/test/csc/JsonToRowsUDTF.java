package com.audi.test.csc;

/**
 * User : LinShiYue
 * Date : 2023-12-14 11:16:21
 * Description :
 */

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.annotation.FunctionHint;
import org.apache.flink.table.functions.TableFunction;
import org.apache.flink.types.Row;

@FunctionHint(output = @DataTypeHint("ROW<contentTagName STRING, contentTagId STRING>"))
public class JsonToRowsUDTF extends TableFunction<Row> {
    public void eval(String str) {
        JSONArray objects = JSON.parseArray(str);
        for(int i = 0; i < objects.size(); i++){
            String string = objects.getString(i);
            JSONObject jsonObject1 = JSON.parseObject(string);
            String type = jsonObject1.getString("contentTagName");
            String value = jsonObject1.getString("contentTagId");
            collect(Row.of(type,value));
        }
    }
}


package com.audi.app.fun;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONObject;
import com.audi.util.DruidDSUtil;
import com.audi.util.PhoenixUtil;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

/**
 * User : LinShiYue
 * Date : 2023-12-07 15:49:43
 * Description :
 */
public class DimSinkFunction extends RichSinkFunction<JSONObject> {

    private DruidDataSource druidDataSource = null;

    @Override
    public void open(Configuration parameters) throws Exception {
        druidDataSource = DruidDSUtil.createDataSource();

    }

    @Override
    public void invoke(JSONObject value, Context context) throws Exception {
        //获取连接
        DruidPooledConnection connection = druidDataSource.getConnection();

        //写出数据
//        {"sinkTable":"dim_user_info","database":"gmall2021","xid":24815,"data":{"birthday":"1987-12-04 00:00:00","login_name":"qzpcueo","gender":"F","create_time":"2020-12-04 23:15:59","name":"戴育滢","user_level":"1","id":3593,"operate_time":"2023-12-07 16:16:34"},"old":{"birthday":"1987-12-04","nick_name":"娟英","operate_time":"2020-12-04 23:25:46"},"xoffset":74,"type":"update","table":"user_info","ts":1701936995}

        String sinkTable = value.getString("sinkTable");
        JSONObject data = value.getJSONObject("data");
        PhoenixUtil.upsertValues(connection, sinkTable, data);


        //归还连接
        connection.close();
    }
}

package com.audi.util;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONObject;
import com.audi.common.GmallConfig;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
/**
 * User : LinShiYue
 * Date : 2023-12-07 15:47:27
 * Description :
 */
public class PhoenixUtil {


    /**
     * 将维度数据写到Phoenix
     * @param connection
     * @param sinkTable
     * @param data
     */
    public static void upsertValues(DruidPooledConnection connection, String sinkTable, JSONObject data) throws SQLException {
        //1.拼接sql语句 upsert into db.tn(id,name,size) values ('1001','zhangsan','12')
        Set<String> strings = data.keySet();
        Collection<Object> values = data.values();
        String sql  = "upsert into " +GmallConfig.HBASE_SCHEMA + "." +sinkTable + "(" +
                StringUtils.join(strings,",") + ") values ( '" +
                StringUtils.join(values,"','") + "')";

//        System.out.println("插入phoenix数据的sql=> "+sql);

        //2.预编译SQL
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        //3.执行
        preparedStatement.execute();
        connection.commit();

        //4.释放资源
        preparedStatement.close();

    }
}

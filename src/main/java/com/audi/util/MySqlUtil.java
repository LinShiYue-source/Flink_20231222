package com.audi.util;

/**
 * User : LinShiYue
 * Date : 2023-12-17 21:08:45
 * Description :
 */
public class MySqlUtil {
    public static String getBaseDicLookUpDDL() {

        return "create table `base_dic`( " +
                "`dic_code` string, " +
                "`dic_name` string, " +
                "`parent_code` string, " +
                "`create_time` timestamp, " +
                "`operate_time` timestamp, " +
                "primary key(`dic_code`) not enforced " +
                ")" + MySqlUtil.mysqlLookUpTableDDL("base_dic");
    }

    public static String mysqlLookUpTableDDL(String tableName) {

        String ddl = "WITH ( " +
                "'connector' = 'jdbc', " +
                "'url' = 'jdbc:mysql://hadoop102:3306/gmall2021', " +
                "'table-name' = '" + tableName + "', " +
                "'lookup.cache.max-rows' = '10', " +
                "'lookup.cache.ttl' = '1 hour', " +
                "'username' = 'root', " +
                "'password' = '123456', " +
                "'driver' = 'com.mysql.cj.jdbc.Driver' " +
                ")";
        return ddl;
    }
}

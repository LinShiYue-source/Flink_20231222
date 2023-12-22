package com.audi.common;

/**
 * User : LinShiYue
 * Date : 2023-11-30 10:34:04
 * Description :
 */
public class GmallConfig {
    // Phoenix库名
    public static final String HBASE_SCHEMA = "Gmall231206_REALTIME";

    // Phoenix驱动
    public static final String PHOENIX_DRIVER = "org.apache.phoenix.jdbc.PhoenixDriver";

    // Phoenix连接参数
    public static final String PHOENIX_SERVER = "jdbc:phoenix:hadoop102:2181";

}

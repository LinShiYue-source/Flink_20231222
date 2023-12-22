package com.audi.test.csc;

import com.audi.test.myself.CustomUDTF;
import com.audi.util.KafkaUtil;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

/**
 * User : LinShiYue
 * Date : 2023-12-14 10:25:54
 * Description :
 */
public class FlinkSqlExample {
    public static void main(String[] args) throws Exception {
        //todo 1创建表执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        //todo 2.创建临时表 并 向表中插入数据
        tableEnv.executeSql("create table `udtftest`( " +
                " materialId string ," +
                "contentTags string" +
                ")" + KafkaUtil.getKafkaDDL("udtftest", "udtftest"));

        //todo 3.注册自定义 UDTF
        tableEnv.createTemporarySystemFunction("customUDTF", JsonToRowsUDTF.class);


        //todo 4.执行 SQL 查询
        tableEnv.sqlQuery(
                "SELECT materialId," +
                        "contentTagName," +
                        "contentTagId " +
                   "FROM udtftest " +
                   "LEFT JOIN LATERAL TABLE(customUDTF(contentTags)) ON TRUE")
                .execute()
                .print();



        env.execute("Flink SQL Example");

    }

}



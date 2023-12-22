package com.audi.test.myself;

import com.audi.util.KafkaUtil;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import java.time.Duration;

/**
 * User : LinShiYue
 * Date : 2023-12-16 16:38:48
 * Description :
 */
public class FlinkSqlJoinTest {
    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        System.out.println(tableEnv.getConfig().getIdleStateRetention());
        tableEnv.getConfig().setIdleStateRetention(Duration.ofSeconds(10));

        //1001,23.6,1324
        SingleOutputStreamOperator<WaterSensor> waterSensorDS1 = env.socketTextStream("hadoop102", 8888)
                .map(line -> {
                    String[] split = line.split(",");
                    return new WaterSensor(split[0],
                            Double.parseDouble(split[1]),
                            Long.parseLong(split[2]));
                });
        SingleOutputStreamOperator<WaterSensor2> waterSensorDS2 = env.socketTextStream("hadoop102", 9999)
                .map(line -> {
                    String[] split = line.split(",");
                    return new WaterSensor2(split[0],
                            split[1],
                            Long.parseLong(split[2]));
                });

        //将流转换为动态表
        tableEnv.createTemporaryView("t1", waterSensorDS1);
        tableEnv.createTemporaryView("t2", waterSensorDS2);


//        tableEnv.sqlQuery("select id,vc,ts from t1")
//                .execute()
//                .print();
//
//        tableEnv.sqlQuery("select id,name,ts from t2")
//                .execute()
//                .print();


//        inner join  左表:OnCreateAndWrite   右表:OnCreateAndWrite   固定有效期为10秒
//        tableEnv.sqlQuery("select t1.id,t1.vc,t2.id,t2.name from t1 join t2 on t1.id=t2.id")
//                .execute()
//                .print();
        //状态有效期内，左表来了1001的数据，右边来了1001能关联上，右边再来一条1001，还能再关联一次


        //left join   左表:OnReadAndWrite     右表:OnCreateAndWrite
        Table table = tableEnv.sqlQuery("select t1.id,t1.vc,t2.id,t2.name from t1 left join t2 on t1.id=t2.id");

        tableEnv.createTemporaryView("resultTable", table);

        tableEnv.executeSql("" +
                "create table resulttable(" +
                "id string," +
                "vc double," +
                "id0 string," +
                "name string," +
                "primary key(id) not enforced" +
                ")" + KafkaUtil.getUpsertKafkaDDL("first"));

        tableEnv.executeSql("insert into resulttable select * from resultTable");
//控制台输出的结果
//| op |                             id |                             vc |                            id0 |                           name |
//+----+--------------------------------+--------------------------------+--------------------------------+--------------------------------+
//| +I |                           1001 |                           20.1 |                         (NULL) |                         (NULL) |
//| -D |                           1001 |                           20.1 |                         (NULL) |                         (NULL) |
//| +I |                           1001 |                           20.1 |                           1001 |                          sen_1 |
//| +I |                           1001 |                           20.1 |                           1001 |                          sen_2 |
//回撤流数据写到kafka里边会是null
        //写到kafka的结果
//        {"id":"1001","vc":20.1,"id0":null,"name":null}
//        null
//        {"id":"1001","vc":20.1,"id0":"1001","name":"sen_1"}
//        {"id":"1001","vc":20.1,"id0":"1001","name":"sen_2"}


    }
}

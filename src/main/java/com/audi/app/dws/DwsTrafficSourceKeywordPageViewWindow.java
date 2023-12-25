package com.audi.app.dws;

import com.audi.app.fun.SplitFunction;
import com.audi.bean.KeywordBean;
import com.audi.util.KafkaUtil;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

/**
 * User : LinShiYue
 * Date : 2023-12-22 18:17:58
 * Description :
 */
public class DwsTrafficSourceKeywordPageViewWindow {
    public static void main(String[] args) throws Exception {
        //todo 1.获取执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        //todo 2.使用DDL方式读取kafka page_log 主题的数据 创建表 并生产watermark
        String topic = "dwd_traffic_page_log";
        String groupId = "dws_traffic_source_keyword_page_view_window";
        tableEnv.executeSql("" +
                "create table page_log(" +
                " `page` map<String,String>," +
                " `ts` bigint," +
                " `rt` as TO_TIMESTAMP(FROM_UNIXTIME(ts/1000))," +
                " WATERMARK FOR rt as rt -interval '2' second " +
                ")" +KafkaUtil.getKafkaDDL(topic,groupId));

        //todo 3.过滤出搜索数据
        Table filterTable = tableEnv.sqlQuery("" +
                " select " +
                "  page['item'] item, " +
                "  rt " +
                " from page_log " +
                " where page['last_page_id'] = 'search' " +
                " and page['item_type'] = 'keyword' " +
                " and page['item'] is not null ");
        tableEnv.createTemporaryView("filter_table",filterTable);

        //todo 4.注册UDTF 切词
        tableEnv.createTemporarySystemFunction("SplitFunction", SplitFunction.class);
        Table splitTable = tableEnv.sqlQuery("" +
                "select " +
                "  word, " +
                "  rt " +
                "from filter_table ," +
                "lateral table(SplitFunction(item)) ");
        tableEnv.createTemporaryView("split_table",splitTable);
//        tableEnv.toAppendStream(splitTable, Row.class).print("split >>>");

        //todo 5.分组开窗聚合
        Table resultTable = tableEnv.sqlQuery("" +
                "SELECT " +
                "  'search'AS source, " +
                "  DATE_FORMAT(TUMBLE_START(rt, INTERVAL '10' SECOND),'yyyy-MM-dd HH:mm:ss') AS stt , " +
                "  DATE_FORMAT(TUMBLE_END(rt, INTERVAL '10' SECOND),'yyyy-MM-dd HH:mm:ss') AS  edt , " +
                "  word AS keyword, " +
                "  count(1) AS keyword_count, " +
                "  UNIX_TIMESTAMP() * 1000 AS ts  " +
                "FROM split_table " +
                "GROUP BY " +
                "  TUMBLE(rt, INTERVAL '10' SECOND), " +
                "  word");

        //todo 6.将动态表转换为流
        DataStream<KeywordBean> resultDS = tableEnv.toAppendStream(resultTable, KeywordBean.class);
        resultDS.print("resultDS");

        //todo 7.将结果写到clickhouse

        //todo 8.启动任务
        env.execute();
    }
}

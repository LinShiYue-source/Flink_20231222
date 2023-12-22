package com.audi.app.dwd;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.audi.util.DateFormatUtil;
import com.audi.util.KafkaUtil;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * User : LinShiYue
 * Date : 2023-12-12 14:14:29
 * Description :
 */
public class DwdTrafficUniqueVisitorDetail {
    public static void main(String[] args) throws Exception {
        //todo 1.获取执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        //todo 2.读取kafka的数据  页面主题 创建流
        String topic = "dwd_traffic_page_log";
        String groupId = "uv_detail";
        DataStreamSource<String> kafkaSourceDS = env.addSource(KafkaUtil.getFlinkKafkaConsumer(topic, groupId));

        //todo 3.过滤掉上一跳不为null的数据，并把每行数据转换为json
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaSourceDS.flatMap(new FlatMapFunction<String, JSONObject>() {
            @Override
            public void flatMap(String s, Collector<JSONObject> collector) throws Exception {
                try {
                    JSONObject jsonObject = JSON.parseObject(s);
                    String lastPageId = jsonObject.getJSONObject("page").getString("last_page_id");
                    if (lastPageId == null) {
                        collector.collect(jsonObject);
                    }
                } catch (Exception e) {
                    System.out.println(s);
                    e.printStackTrace();
                }
            }
        });

        //todo 4.按照mid分组
        KeyedStream<JSONObject, String> keyedDS = jsonObjDS.keyBy(json -> json.getJSONObject("common").getString("mid"));

        //todo 5.使用状态编程实现按照mid的去重功能
        SingleOutputStreamOperator<JSONObject> filterDS = keyedDS.filter(new RichFilterFunction<JSONObject>() {
            private ValueState<String> lastVisitDate;

            @Override
            public void open(Configuration parameters) throws Exception {
                ValueStateDescriptor<String> last_visit = new ValueStateDescriptor<>("last_visit", String.class);
                StateTtlConfig ttlConfig = new StateTtlConfig.Builder(Time.days(1))
                        .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
                        .build();
                last_visit.enableTimeToLive(ttlConfig);
                lastVisitDate = getRuntimeContext().getState(last_visit);
            }

            @Override
            public boolean filter(JSONObject jsonObject) throws Exception {
                String stateDate = lastVisitDate.value();
                Long ts = jsonObject.getLong("ts");
                String currDate = DateFormatUtil.toDate(ts);
                if (stateDate == null || !stateDate.equals(currDate)) {
                    lastVisitDate.update(currDate);
                    return true;
                } else {
                    return false;
                }
            }
        });

        //todo 6.将数据写到kafka
        String targetTopic = "dwd_traffic_unique_visitor_detail";
        filterDS.print("filterDS >>>");
        filterDS.map(json -> json.toJSONString())
                .addSink(KafkaUtil.getFlinkKafkaProducer(targetTopic));

        //todo 7.启动程序
        env.execute();

    }

}

package com.audi.app.dwd;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.audi.util.DateFormatUtil;
import com.audi.util.KafkaUtil;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

/**
 * User : LinShiYue
 * Date : 2023-12-08 17:21:06
 * Description :
 */
public class BaseLogApp {
    public static void main(String[] args) throws Exception {
        //TODO 1.获取执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        //todo 2.读取kafka topic_log 的数据
        String topic = "topic_log";
        String groupId = "BaseLogApp";
        DataStreamSource<String> kafkaDS = env.addSource(KafkaUtil.getFlinkKafkaConsumer(topic, groupId));


        //todo 3.过滤掉非json格式的数据&将数据转换成jsonObject
        //定义测输出流，保存脏数据
        OutputTag<String> dirtyTag = new OutputTag<String>("dirty") {
        };
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaDS.process(new ProcessFunction<String, JSONObject>() {
            @Override
            public void processElement(String value, Context ctx, Collector<JSONObject> out) throws Exception {
                try {
                    JSONObject jsonObject = JSON.parseObject(value);
                    out.collect(jsonObject);
                } catch (Exception e) {
                    ctx.output(dirtyTag, value);
                }
            }
        });

        DataStream<String> dirtyDS = jsonObjDS.getSideOutput(dirtyTag);
        dirtyDS.print("脏数据 =》");
//        jsonObjDS.print("主流数据 =》");

        //todo 4.根据mid分组
        KeyedStream<JSONObject, String> keyedDS = jsonObjDS.keyBy(jsonobj -> jsonobj.getJSONObject("common").getString("mid"));
//        keyedDS.print("keyedDS >>> ");

        //todo 5.使用状态编程进行新老访客校验
        //{"common":{"ar":"370000","uid":"132","os":"Android 11.0","ch":"xiaomi","is_new":"1","md":"Xiaomi 10 Pro ","mid":"mid_758664","vc":"v2.1.132","ba":"Xiaomi"},"page":{"page_id":"good_detail","item":"23","during_time":14254,"item_type":"sku_id","last_page_id":"register","source_type":"query"},"displays":[{"display_type":"query","item":"12","item_type":"sku_id","pos_id":1,"order":1},{"display_type":"promotion","item":"12","item_type":"sku_id","pos_id":4,"order":2},{"display_type":"query","item":"20","item_type":"sku_id","pos_id":1,"order":3},{"display_type":"recommend","item":"13","item_type":"sku_id","pos_id":2,"order":4},{"display_type":"promotion","item":"1","item_type":"sku_id","pos_id":3,"order":5}],"actions":[{"item":"23","action_id":"favor_add","item_type":"sku_id","ts":1702028706127}],"ts":1702028699000}
        SingleOutputStreamOperator<JSONObject> mapDS = keyedDS.map(new RichMapFunction<JSONObject, JSONObject>() {
            private ValueState<String> lastVisitState;

            @Override
            public void open(Configuration parameters) throws Exception {
                ValueStateDescriptor<String> lastVistDes = new ValueStateDescriptor<>("last-visit", String.class);
                lastVisitState = getRuntimeContext().getState(lastVistDes);
            }

            @Override
            public JSONObject map(JSONObject jsonObject) throws Exception {
                String is_new = jsonObject.getJSONObject("common").getString("is_new");
                Long ts = jsonObject.getLong("ts");
                String curDate = DateFormatUtil.toDate(ts);

                String stateValue = lastVisitState.value();

                if ("1".equals(is_new)) {
                    if (stateValue == null) {
                        lastVisitState.update(curDate);
                    } else if (!stateValue.equals(curDate)) {
                        jsonObject.getJSONObject("common").put("is__new", 0);
                    }
                } else if (stateValue == null) {
                    lastVisitState.update(DateFormatUtil.toDate(ts - 24 * 60 * 60 * 1000L));
                }
                return jsonObject;
            }
        });

        //todo 6.使用侧输出流进行分流处理
        OutputTag<String> startTag = new OutputTag<String>("start") {
        };
        OutputTag<String> displayTag = new OutputTag<String>("display") {
        };
        OutputTag<String> actionTag = new OutputTag<String>("action") {
        };
        OutputTag<String> errorTag = new OutputTag<String>("error") {
        };
        SingleOutputStreamOperator<String> pageDS = mapDS.process(new ProcessFunction<JSONObject, String>() {
            @Override
            public void processElement(JSONObject value, Context ctx, Collector<String> out) throws Exception {

                //尝试获取错误信息
                String err = value.getString("err");
                if (err != null) {
                    //将数据写到error侧输出流
                    ctx.output(errorTag, value.toJSONString());
                }

                //移除错误信息
                value.remove("err");

                //尝试获取启动信息
                String start = value.getString("start");
                if (start != null) {
                    //将数据写到start侧输出流
                    ctx.output(startTag, value.toJSONString());
                } else {

                    //获取公共信息&页面id&时间戳
                    String common = value.getString("common");
                    String pageId = value.getJSONObject("page").getString("page_id");
                    Long ts = value.getLong("ts");

                    //尝试获取曝光数据
                    JSONArray displays = value.getJSONArray("displays");
                    if (displays != null && displays.size() > 0) {
                        //遍历曝光数据&写到display侧输出流
                        for (int i = 0; i < displays.size(); i++) {
                            JSONObject display = displays.getJSONObject(i);
                            display.put("common", common);
                            display.put("page_id", pageId);
                            display.put("ts", ts);
                            ctx.output(displayTag, display.toJSONString());
                        }
                    }

                    //尝试获取动作数据
                    JSONArray actions = value.getJSONArray("actions");
                    if (actions != null && actions.size() > 0) {
                        //遍历曝光数据&写到display侧输出流
                        for (int i = 0; i < actions.size(); i++) {
                            JSONObject action = actions.getJSONObject(i);
                            action.put("common", common);
                            action.put("page_id", pageId);
                            ctx.output(actionTag, action.toJSONString());
                        }
                    }

                    //移除曝光和动作数据&写到页面日志主流
                    value.remove("displays");
                    value.remove("actions");
                    out.collect(value.toJSONString());
                }
            }
        });

        //todo 7.提取各个测输出流
        DataStream<String> startDS = pageDS.getSideOutput(startTag);
        DataStream<String> displayDS = pageDS.getSideOutput(displayTag);
        DataStream<String> actionDS = pageDS.getSideOutput(actionTag);
        DataStream<String> errorDS = pageDS.getSideOutput(errorTag);

        pageDS.print("Page>>>>>>>>>>");
        startDS.print("Start>>>>>>>>");
        displayDS.print("Display>>>>");
        actionDS.print("Action>>>>>>");
        errorDS.print("Error>>>>>>>>");

        String page_topic = "dwd_traffic_page_log";
        String start_topic = "dwd_traffic_start_log";
        String display_topic = "dwd_traffic_display_log";
        String action_topic = "dwd_traffic_action_log";
        String error_topic = "dwd_traffic_error_log";

        //todo 8.将侧输出流写到对应的kafka的主题中
        pageDS.addSink(KafkaUtil.getFlinkKafkaProducer(page_topic));
        startDS.addSink(KafkaUtil.getFlinkKafkaProducer(start_topic));
        displayDS.addSink(KafkaUtil.getFlinkKafkaProducer(display_topic));
        actionDS.addSink(KafkaUtil.getFlinkKafkaProducer(action_topic));
        errorDS.addSink(KafkaUtil.getFlinkKafkaProducer(error_topic));


        //todo 9.启动任务
        env.execute();

    }

}

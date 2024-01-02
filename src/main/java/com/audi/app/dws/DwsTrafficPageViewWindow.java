package com.audi.app.dws;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.audi.bean.TrafficHomeDetailPageViewBean;
import com.audi.util.DateFormatUtil;
import com.audi.util.KafkaUtil;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.time.Duration;

/**
 * User : LinShiYue
 * Date : 2023-12-29 09:49:49
 * Description :
 */
public class DwsTrafficPageViewWindow {
    public static void main(String[] args) throws Exception {
        //todo 1.获取执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        //todo 2.从dwd的页面主题读取数据并封装成流
        String topic = "dwd_traffic_page_log";
        String groupId = "dws_traffic_page_view_window_211126";
        DataStreamSource<String> kafkaDS = env.addSource(KafkaUtil.getFlinkKafkaConsumer(topic, groupId));

        //todo 3.转换流的结构为json格式 过滤首页和商品详情页的数据
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaDS.flatMap(new FlatMapFunction<String, JSONObject>() {
            @Override
            public void flatMap(String value, Collector<JSONObject> out) throws Exception {
                try {
                    JSONObject jsonObject = JSON.parseObject(value);
                    //获取当前页面id
                    String pageId = jsonObject.getJSONObject("page").getString("page_id");
                    if ("home".equals(pageId) || "good_detail".equals(pageId)) {
                        out.collect(jsonObject);
                    }
                } catch (Exception e) {
                    System.out.println("脏数据 " + value);
                }
            }
        });

        //todo 4.提取事件时间生成watermark
        SingleOutputStreamOperator<JSONObject> jsonObjWithWmDS = jsonObjDS.assignTimestampsAndWatermarks(
                WatermarkStrategy.<JSONObject>forBoundedOutOfOrderness(Duration.ofSeconds(2))
                        .withTimestampAssigner(new SerializableTimestampAssigner<JSONObject>() {
                            @Override
                            public long extractTimestamp(JSONObject element, long recordTimestamp) {
                                return element.getLong("ts");
                            }
                        }));

        //todo 5.按照mid分组
        KeyedStream<JSONObject, String> keybyMidDS = jsonObjWithWmDS.keyBy(json -> json.getJSONObject("common").getString("mid"));

        //todo 6.使用状态编程过滤出 首页和商品详情页的独立访客
        SingleOutputStreamOperator<TrafficHomeDetailPageViewBean> trafficHomeDetailDS = keybyMidDS.flatMap(new RichFlatMapFunction<JSONObject, TrafficHomeDetailPageViewBean>() {
            private ValueState<String> homeLastState;
            private ValueState<String> detailLastState;

            @Override
            public void open(Configuration parameters) throws Exception {
                super.open(parameters);

                StateTtlConfig stateTtlConfig = new StateTtlConfig.Builder(Time.days(1))
                        .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
                        .build();

                ValueStateDescriptor<String> homeLastStateDes = new ValueStateDescriptor<>("homeLastState", String.class);
                ValueStateDescriptor<String> detailLastStateDes = new ValueStateDescriptor<>("detailLastState", String.class);

                //设置ttl
                homeLastStateDes.enableTimeToLive(stateTtlConfig);
                detailLastStateDes.enableTimeToLive(stateTtlConfig);

                homeLastState = getRuntimeContext().getState(homeLastStateDes);
                detailLastState = getRuntimeContext().getState(detailLastStateDes);
            }

            @Override
            public void flatMap(JSONObject value, Collector<TrafficHomeDetailPageViewBean> out) throws Exception {
                //获取当前状态中的时间和数据中的时间
                Long ts = value.getLong("ts");
                String valueDate = DateFormatUtil.toDate(ts);

                String homeDt = homeLastState.value();
                String detailDt = detailLastState.value();

                //定义初始首页和详情页的访客数
                Long homeCou = 0L;
                Long detailCou = 0L;

                if ("home".equals(value.getJSONObject("page").getString("page_id"))) {
                    //如果状态为空或者状态时间与当前时间不同,则为需要的数据
                    if (homeDt == null || !homeDt.equals(valueDate)) {
                        homeCou = 1L;
                        homeLastState.update(valueDate);
                    }
                } else {
                    if (detailDt == null || !detailDt.equals(valueDate)) {
                        detailCou = 1L;
                        detailLastState.update(valueDate);
                    }
                }
                //满足任何一个值不等于0就可以输出
                if (homeCou != 0 || detailCou != 0) {
                    out.collect(new TrafficHomeDetailPageViewBean("",
                            "",
                            homeCou,
                            detailCou,
                            ts));
                }

            }
        });

        //todo 7.开窗 聚合
        SingleOutputStreamOperator<TrafficHomeDetailPageViewBean> resultDS = trafficHomeDetailDS.windowAll(TumblingEventTimeWindows.of(org.apache.flink.streaming.api.windowing.time.Time.seconds(10)))
                .reduce(new ReduceFunction<TrafficHomeDetailPageViewBean>() {
                    @Override
                    public TrafficHomeDetailPageViewBean reduce(TrafficHomeDetailPageViewBean value1, TrafficHomeDetailPageViewBean value2) throws Exception {
                        value1.setHomeUvCt(value1.getHomeUvCt() + value2.getHomeUvCt());
                        value1.setGoodDetailUvCt(value1.getGoodDetailUvCt() + value2.getGoodDetailUvCt());
                        return value1;
                    }
                }, new AllWindowFunction<TrafficHomeDetailPageViewBean, TrafficHomeDetailPageViewBean, TimeWindow>() {
                    @Override
                    public void apply(TimeWindow window, Iterable<TrafficHomeDetailPageViewBean> values, Collector<TrafficHomeDetailPageViewBean> out) throws Exception {
                        TrafficHomeDetailPageViewBean result = values.iterator().next();

                        result.setTs(System.currentTimeMillis());
                        result.setSdt(DateFormatUtil.toYmdHms(window.getEnd()));
                        result.setStt(DateFormatUtil.toYmdHms(window.getStart()));
                        out.collect(result);
                    }
                });


        //todo 8.将数据写到clickhouse
        resultDS.print();
        //todo 9.启动任务
        env.execute();
    }

}

package com.audi.app.dws;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.audi.bean.TrafficPageViewBean;
import com.audi.util.DateFormatUtil;
import com.audi.util.KafkaUtil;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.time.Duration;

/**
 * User : LinShiYue
 * Date : 2023-12-28 10:03:15
 * Description :
 */
public class DwsTrafficVcChArIsNewPageViewWindow {
    public static void main(String[] args) throws Exception {
        //todo 1.获取执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        //todo 2.读取DWD页面主题数据并转换结构
        String topic = "dwd_traffic_page_log";
        String groupId = "vccharisnew_pageview_window_1126";
        DataStreamSource<String> pageDS = env.addSource(KafkaUtil.getFlinkKafkaConsumer(topic, groupId));
        SingleOutputStreamOperator<TrafficPageViewBean> trafficPageViewWithPageDS = pageDS.map(line -> {
            JSONObject jsonObject = JSON.parseObject(line);
            JSONObject common = jsonObject.getJSONObject("common");

            JSONObject page = jsonObject.getJSONObject("page");
            String lastPageId = page.getString("last_page_id");
            long sv = 0L;
            if (lastPageId == null) {
                sv = 1L;
            }

            return new TrafficPageViewBean("", "",
                    common.getString("vc"),
                    common.getString("ch"),
                    common.getString("ar"),
                    common.getString("is_new"),
                    0L, sv, 1L, page.getLong("during_time"), 0L,
                    jsonObject.getLong("ts"));
        });

        //todo 3.读取DWD用户跳出数据并转换结构
        String ujdTopic = "dwd_traffic_user_jump_detail";
        DataStreamSource<String> ujdDS = env.addSource(KafkaUtil.getFlinkKafkaConsumer(ujdTopic, groupId));
        SingleOutputStreamOperator<TrafficPageViewBean> trafficPageViewWithUjDS = ujdDS.map(line -> {
            JSONObject jsonObject = JSON.parseObject(line);
            JSONObject common = jsonObject.getJSONObject("common");

            return new TrafficPageViewBean("", "",
                    common.getString("vc"),
                    common.getString("ch"),
                    common.getString("ar"),
                    common.getString("is_new"),
                    0L, 0L, 0L, 0L, 1L,
                    jsonObject.getLong("ts"));
        });

        //todo 4.读取DWD独立访客数据并转换结构
        String uvTopic = "dwd_traffic_unique_visitor_detail";
        DataStreamSource<String> uvDS = env.addSource(KafkaUtil.getFlinkKafkaConsumer(uvTopic, groupId));
        SingleOutputStreamOperator<TrafficPageViewBean> trafficPageViewWithUvDS = uvDS.map(line -> {
            JSONObject jsonObject = JSON.parseObject(line);
            JSONObject common = jsonObject.getJSONObject("common");

            return new TrafficPageViewBean("", "",
                    common.getString("vc"),
                    common.getString("ch"),
                    common.getString("ar"),
                    common.getString("is_new"),
                    1L, 0L, 0L, 0L, 0L,
                    jsonObject.getLong("ts"));
        });
        //todo 5.合并三条流
        DataStream<TrafficPageViewBean> unionDS = trafficPageViewWithPageDS
                .union(trafficPageViewWithUjDS)
                .union(trafficPageViewWithUvDS);


        //todo 6.设置水位线
        SingleOutputStreamOperator<TrafficPageViewBean> trafficPageViewBeanWithTsDS = unionDS.assignTimestampsAndWatermarks(
                WatermarkStrategy
                        .<TrafficPageViewBean>forBoundedOutOfOrderness(Duration.ofSeconds(14))
                        .withTimestampAssigner(new SerializableTimestampAssigner<TrafficPageViewBean>() {
                            @Override
                            public long extractTimestamp(TrafficPageViewBean element, long recordTimestamp) {
                                return element.getTs();
                            }
                        }));

        //todo 7.分组
        KeyedStream<TrafficPageViewBean, Tuple4<String, String, String, String>> keyedDS = trafficPageViewBeanWithTsDS.keyBy(new KeySelector<TrafficPageViewBean, Tuple4<String, String, String, String>>() {
            @Override
            public Tuple4<String, String, String, String> getKey(TrafficPageViewBean value) throws Exception {
                return new Tuple4<>(value.getAr(),
                        value.getCh(),
                        value.getIsNew(),
                        value.getVc());
            }
        });

        //todo 8.开窗
        WindowedStream<TrafficPageViewBean, Tuple4<String, String, String, String>, TimeWindow> windosDS = keyedDS.window(TumblingEventTimeWindows.of(Time.seconds(10)));

        //todo 9.聚合
        SingleOutputStreamOperator<Object> resultDS = windosDS.reduce(new ReduceFunction<TrafficPageViewBean>() {
            @Override
            public TrafficPageViewBean reduce(TrafficPageViewBean value1, TrafficPageViewBean value2) throws Exception {
                value1.setSvCt(value1.getSvCt() + value2.getSvCt());
                value1.setUvCt(value1.getUvCt() + value2.getUvCt());
                value1.setUjCt(value1.getUjCt() + value2.getUjCt());
                value1.setPvCt(value1.getPvCt() + value2.getPvCt());
                value1.setDurSum(value1.getDurSum() + value2.getDurSum());
                return value1;
            }
        }, new WindowFunction<TrafficPageViewBean, Object, Tuple4<String, String, String, String>, TimeWindow>() {
            @Override
            public void apply(Tuple4<String, String, String, String> stringStringStringStringTuple4, TimeWindow window, Iterable<TrafficPageViewBean> input, Collector<Object> out) throws Exception {
                //获取数据
                TrafficPageViewBean next = input.iterator().next();

                //补充信息
                next.setStt(DateFormatUtil.toYmdHms(window.getStart()));
                next.setEdt(DateFormatUtil.toYmdHms(window.getEnd()));

                //修改TS
                next.setTs(System.currentTimeMillis());

                //输出数据
                out.collect(next);
            }
        });
        resultDS.print();

        //todo 10.将数据写到clickhouse

        //todo 11.启动任务
        env.execute();


    }

}

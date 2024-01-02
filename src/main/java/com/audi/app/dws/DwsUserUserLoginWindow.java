package com.audi.app.dws;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.audi.bean.UserLoginBean;
import com.audi.util.DateFormatUtil;
import com.audi.util.KafkaUtil;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.time.Duration;

/**
 * User : LinShiYue
 * Date : 2023-12-30 11:10:51
 * Description :
 */
public class DwsUserUserLoginWindow {
    public static void main(String[] args) throws Exception {
        //todo 1.创建执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        //todo 2.从dwd层读取数据封装为流
        String topic = "dwd_traffic_page_log";
        String groupId = "dws_user_login_window_211126";
        DataStreamSource<String> kafkaDS = env.addSource(KafkaUtil.getFlinkKafkaConsumer(topic, groupId));

        //todo 3.转换流的格式 过滤数据
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaDS.flatMap(new FlatMapFunction<String, JSONObject>() {
            @Override
            public void flatMap(String value, Collector<JSONObject> out) throws Exception {
                JSONObject jsonObject = JSON.parseObject(value);
                String uid = jsonObject.getJSONObject("common").getString("uid");
                String lastPageId = jsonObject.getJSONObject("page").getString("last_page_id");
                //当UID不等于空并且上一跳页面为null或者为"login"才是登录数据
                if (uid != null && (lastPageId == null || lastPageId.equals("login"))) {
                    out.collect(jsonObject);
                }
            }
        });

        //todo 4.提取事件时间生成watermark
        SingleOutputStreamOperator<JSONObject> jsonObjWithWmDS = jsonObjDS.assignTimestampsAndWatermarks(WatermarkStrategy.<JSONObject>forBoundedOutOfOrderness(Duration.ofSeconds(2))
                .withTimestampAssigner(new SerializableTimestampAssigner<JSONObject>() {
                    @Override
                    public long extractTimestamp(JSONObject element, long recordTimestamp) {
                        return element.getLong("ts");
                    }
                }));

        //todo 5.根据mid分组
        KeyedStream<JSONObject, String> keyedDS = jsonObjWithWmDS.keyBy(json -> json.getJSONObject("common").getString("id"));

        //todo 6.使用状态编程统计独立用户数和回流用户数
        SingleOutputStreamOperator<UserLoginBean> userLoginDS = keyedDS.flatMap(new RichFlatMapFunction<JSONObject, UserLoginBean>() {
            private ValueState<String> lastLoginState;

            @Override
            public void open(Configuration parameters) throws Exception {
                lastLoginState = getRuntimeContext().getState(new ValueStateDescriptor<String>("lastDt", String.class));
            }

            @Override
            public void flatMap(JSONObject value, Collector<UserLoginBean> out) throws Exception {
                //获取状态的时间 和 数据中的时间
                Long ts = value.getLong("ts");
                String curDt = DateFormatUtil.toDate(ts);
                String lastLoginDt = lastLoginState.value();

                //定义当日独立用户数 和 独立用户数
                Long uv = 0L;
                Long backUv = 0L;

                if (lastLoginDt == null) {
                    uv = 1L;
                    lastLoginState.update(curDt);
                } else if (!lastLoginDt.equals(curDt)) {

                    uv = 1L;
                    lastLoginState.update(curDt);

                    if ((DateFormatUtil.toTs(curDt) - DateFormatUtil.toTs(lastLoginDt)) / (24 * 60 * 60 * 1000L) >= 8) {
                        backUv = 1L;
                    }
                }

                if (uv != 0L) {
                    out.collect(new UserLoginBean("", "",
                            backUv, uv, ts));
                }
            }
        });


        //todo 7.开窗聚合
        SingleOutputStreamOperator<UserLoginBean> resultDS = userLoginDS.windowAll(TumblingEventTimeWindows.of(Time.seconds(10)))
                .reduce(new ReduceFunction<UserLoginBean>() {
                    @Override
                    public UserLoginBean reduce(UserLoginBean value1, UserLoginBean value2) throws Exception {
                        value1.setUuCt(value1.getUuCt() + value2.getUuCt());
                        value1.setBaskCt(value1.getBaskCt() + value2.getBaskCt());
                        return value2;
                    }
                }, new AllWindowFunction<UserLoginBean, UserLoginBean, TimeWindow>() {
                    @Override
                    public void apply(TimeWindow window, Iterable<UserLoginBean> values, Collector<UserLoginBean> out) throws Exception {
                        UserLoginBean result = values.iterator().next();

                        result.setSdt(DateFormatUtil.toYmdHms(window.getEnd()));
                        result.setStt(DateFormatUtil.toYmdHms(window.getStart()));
                        out.collect(result);
                    }
                });

        //todo 8.将结果写到clickhouse

        //todo 9.启动程序
        env.execute();
    }

}

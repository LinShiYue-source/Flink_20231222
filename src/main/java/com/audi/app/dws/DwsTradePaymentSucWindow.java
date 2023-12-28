package com.audi.app.dws;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.audi.bean.TradePaymentWindowBean;
import com.audi.util.DateFormatUtil;
import com.audi.util.KafkaUtil;
import com.audi.util.TimestampLtz3CompareUtil;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
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
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.time.Duration;

/**
 * User : LinShiYue
 * Date : 2023-12-28 14:40:49
 * Description :
 */
public class DwsTradePaymentSucWindow {
    public static void main(String[] args) throws Exception {
        //todo 1.获取执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        //todo 2.读取DWD层成功支付主题的数据创建流
        String topic = "dwd_trade_pay_detail_suc";
        String groupId = "dws_trade_payment_suc_window_211126";
        DataStreamSource<String> kafkaSourceDS = env.addSource(KafkaUtil.getFlinkKafkaConsumer(topic, groupId));

        //todo 3.将数据转换为json对象
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaSourceDS.flatMap(new FlatMapFunction<String, JSONObject>() {
            @Override
            public void flatMap(String value, Collector<JSONObject> out) throws Exception {
                try {
                    JSONObject jsonObject = JSON.parseObject(value);
                    out.collect(jsonObject);
                } catch (Exception e) {
                    System.out.println("脏数据 " + value);
                }
            }
        });

        //todo 4.按照订单明细id分组
        KeyedStream<JSONObject, String> keyByDetailIdDS = jsonObjDS.keyBy(json -> json.getString("order_detail_id"));

        //todo 5.使用状态编程保留最新的数据输出
        SingleOutputStreamOperator<JSONObject> filterDS = keyByDetailIdDS.process(new KeyedProcessFunction<String, JSONObject, JSONObject>() {
            ValueState<JSONObject> valueState;

            @Override
            public void open(Configuration parameters) throws Exception {
                valueState = getRuntimeContext().getState(new ValueStateDescriptor<JSONObject>("value-state", JSONObject.class));
            }

            @Override
            public void processElement(JSONObject value, Context ctx, Collector<JSONObject> out) throws Exception {
                //获取状态中的数据
                JSONObject state = valueState.value();

                if (state == null) {
                    valueState.update(value);
                    ctx.timerService().registerProcessingTimeTimer(ctx.timerService().currentProcessingTime() + 5000L);
                } else {
                    String stateRt = state.getString("row_op_ts");
                    String curRt = value.getString("row_op_ts");

                    int compare = TimestampLtz3CompareUtil.compare(stateRt, curRt);

                    if (compare != 1) {
                        valueState.update(value);
                    }
                }
            }

            @Override
            public void onTimer(long timestamp, OnTimerContext ctx, Collector<JSONObject> out) throws Exception {
                JSONObject value = valueState.value();
                out.collect(value);

                value.clear();
            }
        });

        //todo 6.提取事件时间生成watermark
        SingleOutputStreamOperator<JSONObject> jsonObjWithWmDS = filterDS.assignTimestampsAndWatermarks(WatermarkStrategy.<JSONObject>forBoundedOutOfOrderness(Duration.ofSeconds(2))
                .withTimestampAssigner(new SerializableTimestampAssigner<JSONObject>() {
                    @Override
                    public long extractTimestamp(JSONObject element, long recordTimestamp) {
                        String callbackTime = element.getString("callback_time");
                        return DateFormatUtil.toTs(callbackTime, true);
                    }
                }));

        //todo 7.根据user_id分组
        KeyedStream<JSONObject, String> keyedByUidDS = jsonObjWithWmDS.keyBy(json -> json.getString("user_id"));

        //todo 8.提取独立支付成功用户
        SingleOutputStreamOperator<TradePaymentWindowBean> tradePaymentDS = keyedByUidDS.flatMap(new RichFlatMapFunction<JSONObject, TradePaymentWindowBean>() {
            ValueState<String> lastDtState;

            @Override
            public void open(Configuration parameters) throws Exception {
                lastDtState = getRuntimeContext().getState(new ValueStateDescriptor<String>("last-dt", String.class));
            }

            @Override
            public void flatMap(JSONObject value, Collector<TradePaymentWindowBean> out) throws Exception {
                String stateDt = lastDtState.value();
                String curDt = value.getString("callback_time").split(" ")[0];

                //定义当前支付人数以及新增付费人数
                long paymentSucUniqueUserCount = 0L;
                long paymentSucNewUserCount = 0L;

                //判断状态日期是否为null
                if (stateDt == null) {
                    paymentSucUniqueUserCount = 1L;
                    paymentSucNewUserCount = 1L;
                    lastDtState.update(curDt);
                } else if (!stateDt.equals(curDt)) {
                    paymentSucUniqueUserCount = 1L;
                    lastDtState.update(curDt);
                }

                //返回数据
                if (paymentSucUniqueUserCount == 1L) {
                    out.collect(new TradePaymentWindowBean(
                            "",
                            "",
                            paymentSucUniqueUserCount,
                            paymentSucNewUserCount,
                            null));
                }

            }
        });

        //todo 9.开窗 聚合
        SingleOutputStreamOperator<TradePaymentWindowBean> resultDS = tradePaymentDS.windowAll(TumblingEventTimeWindows.of(Time.seconds(10)))
                .reduce(new ReduceFunction<TradePaymentWindowBean>() {
                            @Override
                            public TradePaymentWindowBean reduce(TradePaymentWindowBean value1, TradePaymentWindowBean value2) throws Exception {
                                value1.setPaymentSucUniqueUserCount(value1.getPaymentSucUniqueUserCount() + value2.getPaymentSucUniqueUserCount());
                                value1.setPaymentSucNewUserCount(value1.getPaymentSucNewUserCount() + value2.getPaymentSucNewUserCount());
                                return value1;
                            }
                        }
                        , new AllWindowFunction<TradePaymentWindowBean, TradePaymentWindowBean, TimeWindow>() {
                            @Override
                            public void apply(TimeWindow window, Iterable<TradePaymentWindowBean> values, Collector<TradePaymentWindowBean> out) throws Exception {
                                TradePaymentWindowBean next = values.iterator().next();

                                next.setTs(System.currentTimeMillis());
                                next.setEdt(DateFormatUtil.toYmdHms(window.getEnd()));
                                next.setStt(DateFormatUtil.toYmdHms(window.getStart()));

                                out.collect(next);
                            }
                        });

        //todo 10.将数据写到clickhouse

        //todo 11.启动任务
        env.execute("DwsTradePaymentSucWindow");
    }

}

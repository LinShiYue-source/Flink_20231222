package com.audi.app.dwd;

import com.audi.util.KafkaUtil;
import com.audi.util.MySqlUtil;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import java.time.Duration;

/**
 * User : LinShiYue
 * Date : 2023-12-20 13:19:30
 * Description :
 */
public class DwdTradePayDetailSuc {
    public static void main(String[] args) {
        //todo 1.准备环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1); //生产环境中设置为Kafka主题的分区数
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
        //1.3 设置状态的TTL  生产环境设置为最大乱序程度
        tableEnv.getConfig().setIdleStateRetention(Duration.ofSeconds(905));

        //todo 2.读取topic_db的数据 并过滤支付成功的数据
        tableEnv.executeSql(KafkaUtil.getTopicDb("pay_detail_suc_211126"))  ;
        Table paymentInfo = tableEnv.sqlQuery("select " +
                "data['user_id'] user_id, " +
                "data['order_id'] order_id, " +
                "data['payment_type'] payment_type, " +
                "data['callback_time'] callback_time, " +
                "`pt` " +        //
                "from topic_db " +
                "where `table` = 'payment_info' " +
                "and `type` = 'update' " +
                "and data['payment_status']='1602'");
        tableEnv.createTemporaryView("payment_info", paymentInfo);

        //todo 3.消费下单主题数据
        tableEnv.executeSql("" +
                "create table dwd_trade_order_detail(" +
                "id string, " +
                "order_id string, " +
                "user_id string, " +
                "sku_id string, " +
                "sku_name string, " +
                "sku_num string, " +              //+++
                "order_price string, " +          //+++
                "province_id string, " +
                "activity_id string, " +
                "activity_rule_id string, " +
                "coupon_id string, " +
                //"date_id string, " +
                "create_time string, " +
                "source_id string, " +
                "source_type_id string, " +     //"source_type_code string, " +
                "source_type_name string, " +
                //"sku_num string, " +
                //"split_original_amount string, " +
                "split_activity_amount string, " +
                "split_coupon_amount string, " +
                "split_total_amount string, " +  //删除","
                //"ts string, " +
                "row_op_ts timestamp_ltz(3) " +
                ")" + KafkaUtil.getKafkaDDL("dwd_trade_order_detail","pay_detail_suc_order_211126"));


        //todo 4.读取mysql base_dic字典表
        tableEnv.executeSql(MySqlUtil.getBaseDicLookUpDDL());

        //todo 5.关联三张表
        Table resultTable = tableEnv.sqlQuery("" +
                "select " +
                "od.id order_detail_id, " +
                "od.order_id, " +
                "od.user_id, " +
                "od.sku_id, " +
                "od.sku_name, " +
                "od.province_id, " +
                "od.activity_id, " +
                "od.activity_rule_id, " +
                "od.coupon_id, " +
                "pi.payment_type payment_type_code, " +
                "dic.dic_name payment_type_name, " +
                "pi.callback_time, " +
                "od.source_id, " +
                "od.source_type_id, " +  //"od.source_type_code, " +
                "od.source_type_name, " +
                "od.sku_num, " +
                "od.order_price, " +    //+++
                //"od.split_original_amount, " +
                "od.split_activity_amount, " +
                "od.split_coupon_amount, " +
                "od.split_total_amount split_payment_amount, " +  //删除","
                //"pi.ts, " +
                "od.row_op_ts row_op_ts " +
                "from payment_info pi " +
                "join dwd_trade_order_detail od " +
                "on pi.order_id = od.order_id " +
                "join `base_dic` for system_time as of pi.pt as dic " +
                "on pi.payment_type = dic.dic_code");
        tableEnv.createTemporaryView("result_table",resultTable);

        //todo 6.创建kafka支付成功表
        tableEnv.executeSql("" +
                "create table dwd_trade_pay_detail_suc( " +
                "order_detail_id string, " +
                "order_id string, " +
                "user_id string, " +
                "sku_id string, " +
                "sku_name string, " +
                "province_id string, " +
                "activity_id string, " +
                "activity_rule_id string, " +
                "coupon_id string, " +
                "payment_type_code string, " +
                "payment_type_name string, " +
                "callback_time string, " +
                "source_id string, " +
                "source_type_id string, " +  //"source_type_code string, " +
                "source_type_name string, " +
                "sku_num string, " +
                "order_price string, " +    //+++
                //"split_original_amount string, " +
                "split_activity_amount string, " +
                "split_coupon_amount string, " +
                "split_payment_amount string, " +
                //"ts string, " +
                "row_op_ts timestamp_ltz(3), " +
                "primary key(order_detail_id) not enforced " +
                ")" + KafkaUtil.getUpsertKafkaDDL("dwd_trade_pay_detail_suc"));

        //todo 7.将数据写出
        tableEnv.executeSql("insert into dwd_trade_pay_detail_suc select * from result_table ");
    }
}

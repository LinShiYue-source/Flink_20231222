package com.audi.app.fun;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.audi.bean.TableProcess;
import com.audi.common.GmallConfig;
import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.util.Collector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * User : LinShiYue
 * Date : 2023-11-30 15:28:06
 * Description :
 */
public class MyBroadcastFunction extends BroadcastProcessFunction<JSONObject,String,JSONObject> {

    Connection connection;
    MapStateDescriptor<String, TableProcess> tableConfigDescriptor = new MapStateDescriptor<String, TableProcess>("table-process-state", String.class, TableProcess.class);


    public MyBroadcastFunction(MapStateDescriptor<String, TableProcess> tableConfigDescriptor) {
        this.tableConfigDescriptor = tableConfigDescriptor;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
       connection = DriverManager.getConnection(GmallConfig.PHOENIX_SERVER);
    }

//    {"before":null,"after":{"source_table":"activity_sku","sink_table":"dim_activity_sku","sink_columns":"id,activity_id,sku_id,create_time","sink_pk":"id","sink_extend":null},"source":{"version":"1.5.4.Final","connector":"mysql","name":"mysql_binlog_source","ts_ms":1701940267089,"snapshot":"false","db":"gmall_config","sequence":null,"table":"table_process","server_id":0,"gtid":null,"file":"","pos":0,"row":0,"thread":null,"query":null},"op":"r","ts_ms":1701940267089,"transaction":null}

    @Override
    public void processBroadcastElement(String value, Context ctx, Collector<JSONObject> out) throws Exception {

        //1.获取并解析数据
        JSONObject jsonObject = JSON.parseObject(value);
        TableProcess tableProcess = jsonObject.getObject("after", TableProcess.class);
//        System.out.println("从配置流中拿到的表名有"+tableProcess.getSourceTable());
//        System.out.println("从配置流中拿到的配置有"+tableProcess);

        //2.校验并创建表
        checkTable(tableProcess.getSinkTable(),
                tableProcess.getSinkColumns(),
                tableProcess.getSinkExtend(),
                tableProcess.getSinkPk());

        //3.写入状态 并广播出去
        BroadcastState<String, TableProcess> broadcastState = ctx.getBroadcastState(tableConfigDescriptor);
        //key -》 activity_info
        //value -》 TableProcess{sourceTable='activity_info', sinkTable='dim_activity_info', sinkColumns='id,activity_name,activity_type,activity_desc,start_time,end_time,create_time', sinkPk='id', sinkExtend='null'}
        broadcastState.put(tableProcess.getSourceTable(),tableProcess);



    }

    /**
     * 校验并创建表
     * @param sinkTable  create table if not exists db.tn (id varchar primary key ,bb varchar ,cc varchar) xxxx
     * @param sinkColumns
     * @param sinkExtend
     * @param sinkPk
     */
    private void checkTable(String sinkTable, String sinkColumns, String sinkExtend, String sinkPk) {
        PreparedStatement preparedStatement = null;
        try {
            //处理特殊字段
            if (sinkPk == null || "".equals(sinkPk)) {
                sinkPk = "id";
            }

            if (sinkExtend == null) {
                sinkExtend = "";
            }
            //拼接sql create table if not exists db.tn (id varchar primary key ,bb varchar ,cc varchar) xxxx
            StringBuilder createTableSql = new StringBuilder("create table if not exists ")
                    .append(GmallConfig.HBASE_SCHEMA)
                    .append(".")
                    .append(sinkTable)
                    .append("(");

            String[] columns = sinkColumns.split(",");
            for (int i = 0; i < columns.length; i++) {
                //取出字段
                String column = columns[i];
                //判断是否为主键
                if (column.equals(sinkPk)) {
                    createTableSql.append(column).append(" varchar primary key");
                }else {
                    createTableSql.append(column).append(" varchar");
                }
                //判断是否为最后一个字段
                if (i < columns.length-1) {
                    createTableSql.append(",");
                }
            }

            createTableSql.append(")").append(sinkExtend);

            //编译执行sql
//            System.out.println("建表语句为" + createTableSql);
            preparedStatement = connection.prepareStatement(createTableSql.toString());

            preparedStatement.execute();

        } catch (SQLException throwables) {
            throw new RuntimeException("建表失败" + sinkTable);
        } finally {
            try {
                preparedStatement.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

    }


    //value:{"database":"gmall-211126-flink","table":"base_trademark","type":"update","ts":1652499176,"xid":188,"commit":true,"data":{"id":13,"tm_name":"atguigu","logo_url":"/bbb/bbb"},"old":{"logo_url":"/aaa/aaa"}}
    //value:{"database":"gmall-211126-flink","table":"order_info","type":"update","ts":1652499176,"xid":188,"commit":true,"data":{"id":13,...},"old":{"xxx":"/aaa/aaa"}}
    @Override
    public void processElement(JSONObject value, ReadOnlyContext ctx, Collector<JSONObject> out) throws Exception {
        //1.获取广播流中的配置
        ReadOnlyBroadcastState<String, TableProcess> broadcastState = ctx.getBroadcastState(tableConfigDescriptor);
        //从主流中拿到表名
        String table = value.getString("table");
//        System.out.println("从主流中拿到的表名为"+table);
        //用主流中的表名去广播状态中找配置
        TableProcess tableProcess = broadcastState.get(table);
//        System.out.println("根据"+table+"从状态中拿到的配置为"+tableProcess);

        //2.如果在广播状态中找到，则说明是维度表 只取维度表 并过滤字段
        if (tableProcess != null) {

            filterColum(value.getJSONObject("data"),tableProcess.getSinkColumns());

            //3.补充sinkTable 并输出到流中
            value.put("sinkTable",tableProcess.getSinkTable()) ;
            out.collect(value);

        }



    }

    /**
     * 过滤数据和字段
     * @param data
     * @param sinkColumns
     */
    private void filterColum(JSONObject data, String sinkColumns) {
        //切分字段
        String[] split = sinkColumns.split(",");
        List<String> columnList = Arrays.asList(split);

        Set<Map.Entry<String, Object>> entries = data.entrySet();
        entries.removeIf(next -> !columnList.contains(next.getKey()));

    }


}

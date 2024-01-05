package com.audi.bean;

import lombok.Builder;

import java.util.Set;

/**
 * User : LinShiYue
 * Date : 2024-01-05 16:00:48
 * Description :
 */
public class TradeProvinceOrderWindow {
    // 窗口起始时间
    String stt;

    // 窗口结束时间
    String edt;

    // 省份 ID
    String provinceId;

    // 省份名称
    @Builder.Default
    String provinceName = "";

    // 累计下单次数
    Long orderCount;

    // 订单 ID 集合，用于统计下单次数
    @TransientSink
    Set<String> orderIdSet;

    // 累计下单金额
    Double orderAmount;

    // 时间戳
    Long ts;

    public String getStt() {
        return stt;
    }

    public void setStt(String stt) {
        this.stt = stt;
    }

    public String getEdt() {
        return edt;
    }

    public void setEdt(String edt) {
        this.edt = edt;
    }

    public String getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(String provinceId) {
        this.provinceId = provinceId;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public Long getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(Long orderCount) {
        this.orderCount = orderCount;
    }

    public Set<String> getOrderIdSet() {
        return orderIdSet;
    }

    public void setOrderIdSet(Set<String> orderIdSet) {
        this.orderIdSet = orderIdSet;
    }

    public Double getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(Double orderAmount) {
        this.orderAmount = orderAmount;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public TradeProvinceOrderWindow(String stt, String edt, String provinceId, String provinceName, Long orderCount, Set<String> orderIdSet, Double orderAmount, Long ts) {
        this.stt = stt;
        this.edt = edt;
        this.provinceId = provinceId;
        this.provinceName = provinceName;
        this.orderCount = orderCount;
        this.orderIdSet = orderIdSet;
        this.orderAmount = orderAmount;
        this.ts = ts;
    }
}

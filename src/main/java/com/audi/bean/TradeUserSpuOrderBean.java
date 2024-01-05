package com.audi.bean;

import lombok.Builder;

import java.util.Set;

/**
 * User : LinShiYue
 * Date : 2024-01-05 13:59:33
 * Description :
 */
@Builder
public class TradeUserSpuOrderBean {
    // 窗口起始时间
    String stt;
    // 窗口结束时间
    String edt;
    // 品牌 ID
    String trademarkId;
    // 品牌名称
    String trademarkName;
    // 一级品类 ID
    String category1Id;
    // 一级品类名称
    String category1Name;
    // 二级品类 ID
    String category2Id;
    // 二级品类名称
    String category2Name;
    // 三级品类 ID
    String category3Id;
    // 三级品类名称
    String category3Name;

    // 订单 ID
    @TransientSink
    Set<String> orderIdSet;

    // sku_id
    @TransientSink
    String skuId;

    // 用户 ID
    String userId;
    // spu_id
    String spuId;
    // spu 名称
    String spuName;
    // 下单次数
    Long orderCount;
    // 下单金额
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

    public String getTrademarkId() {
        return trademarkId;
    }

    public void setTrademarkId(String trademarkId) {
        this.trademarkId = trademarkId;
    }

    public String getTrademarkName() {
        return trademarkName;
    }

    public void setTrademarkName(String trademarkName) {
        this.trademarkName = trademarkName;
    }

    public String getCategory1Id() {
        return category1Id;
    }

    public void setCategory1Id(String category1Id) {
        this.category1Id = category1Id;
    }

    public String getCategory1Name() {
        return category1Name;
    }

    public void setCategory1Name(String category1Name) {
        this.category1Name = category1Name;
    }

    public String getCategory2Id() {
        return category2Id;
    }

    public void setCategory2Id(String category2Id) {
        this.category2Id = category2Id;
    }

    public String getCategory2Name() {
        return category2Name;
    }

    public void setCategory2Name(String category2Name) {
        this.category2Name = category2Name;
    }

    public String getCategory3Id() {
        return category3Id;
    }

    public void setCategory3Id(String category3Id) {
        this.category3Id = category3Id;
    }

    public String getCategory3Name() {
        return category3Name;
    }

    public void setCategory3Name(String category3Name) {
        this.category3Name = category3Name;
    }

    public Set<String> getOrderIdSet() {
        return orderIdSet;
    }

    public void setOrderIdSet(Set<String> orderIdSet) {
        this.orderIdSet = orderIdSet;
    }

    public String getSkuId() {
        return skuId;
    }

    public void setSkuId(String skuId) {
        this.skuId = skuId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSpuId() {
        return spuId;
    }

    public void setSpuId(String spuId) {
        this.spuId = spuId;
    }

    public String getSpuName() {
        return spuName;
    }

    public void setSpuName(String spuName) {
        this.spuName = spuName;
    }

    public Long getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(Long orderCount) {
        this.orderCount = orderCount;
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

}

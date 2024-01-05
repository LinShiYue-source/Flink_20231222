package com.audi.bean;

import lombok.Builder;

import java.util.Set;

/**
 * User : LinShiYue
 * Date : 2024-01-05 16:54:44
 * Description :
 */
@Builder
public class TradeTrademarkCategoryUserRefundBean {
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
    // 退单次数
    Long refundCount;
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

    public Long getRefundCount() {
        return refundCount;
    }

    public void setRefundCount(Long refundCount) {
        this.refundCount = refundCount;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public TradeTrademarkCategoryUserRefundBean(String stt, String edt, String trademarkId, String trademarkName, String category1Id, String category1Name, String category2Id, String category2Name, String category3Id, String category3Name, Set<String> orderIdSet, String skuId, String userId, Long refundCount, Long ts) {
        this.stt = stt;
        this.edt = edt;
        this.trademarkId = trademarkId;
        this.trademarkName = trademarkName;
        this.category1Id = category1Id;
        this.category1Name = category1Name;
        this.category2Id = category2Id;
        this.category2Name = category2Name;
        this.category3Id = category3Id;
        this.category3Name = category3Name;
        this.orderIdSet = orderIdSet;
        this.skuId = skuId;
        this.userId = userId;
        this.refundCount = refundCount;
        this.ts = ts;
    }
}

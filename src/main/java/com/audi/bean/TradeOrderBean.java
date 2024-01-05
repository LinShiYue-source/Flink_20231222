package com.audi.bean;

/**
 * User : LinShiYue
 * Date : 2024-01-04 14:05:21
 * Description :
 */
public class TradeOrderBean {
    // 窗口起始时间
    String stt;

    // 窗口关闭时间
    String edt;

    // 下单独立用户数
    Long orderUniqueUserCount;

    // 下单新用户数
    Long orderNewUserCount;

    // 下单活动减免金额
    Double orderActivityReduceAmount;

    // 下单优惠券减免金额
    Double orderCouponReduceAmount;

    // 下单原始金额
    Double orderOriginalTotalAmount;

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

    public Long getOrderUniqueUserCount() {
        return orderUniqueUserCount;
    }

    public void setOrderUniqueUserCount(Long orderUniqueUserCount) {
        this.orderUniqueUserCount = orderUniqueUserCount;
    }

    public Long getOrderNewUserCount() {
        return orderNewUserCount;
    }

    public void setOrderNewUserCount(Long orderNewUserCount) {
        this.orderNewUserCount = orderNewUserCount;
    }

    public Double getOrderActivityReduceAmount() {
        return orderActivityReduceAmount;
    }

    public void setOrderActivityReduceAmount(Double orderActivityReduceAmount) {
        this.orderActivityReduceAmount = orderActivityReduceAmount;
    }

    public Double getOrderCouponReduceAmount() {
        return orderCouponReduceAmount;
    }

    public void setOrderCouponReduceAmount(Double orderCouponReduceAmount) {
        this.orderCouponReduceAmount = orderCouponReduceAmount;
    }

    public Double getOrderOriginalTotalAmount() {
        return orderOriginalTotalAmount;
    }

    public void setOrderOriginalTotalAmount(Double orderOriginalTotalAmount) {
        this.orderOriginalTotalAmount = orderOriginalTotalAmount;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public TradeOrderBean(String stt, String edt, Long orderUniqueUserCount, Long orderNewUserCount, Double orderActivityReduceAmount, Double orderCouponReduceAmount, Double orderOriginalTotalAmount, Long ts) {
        this.stt = stt;
        this.edt = edt;
        this.orderUniqueUserCount = orderUniqueUserCount;
        this.orderNewUserCount = orderNewUserCount;
        this.orderActivityReduceAmount = orderActivityReduceAmount;
        this.orderCouponReduceAmount = orderCouponReduceAmount;
        this.orderOriginalTotalAmount = orderOriginalTotalAmount;
        this.ts = ts;
    }
}

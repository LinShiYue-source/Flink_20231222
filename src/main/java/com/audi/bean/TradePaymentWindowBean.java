package com.audi.bean;

/**
 * User : LinShiYue
 * Date : 2023-12-28 14:40:09
 * Description :
 */
public class TradePaymentWindowBean {
    // 窗口起始时间
    String stt;

    // 窗口终止时间
    String edt;

    // 支付成功独立用户数
    Long paymentSucUniqueUserCount;

    // 支付成功新用户数
    Long paymentSucNewUserCount;

    // 时间戳
    Long ts;

    public TradePaymentWindowBean(String stt, String edt, Long paymentSucUniqueUserCount, Long paymentSucNewUserCount, Long ts) {
        this.stt = stt;
        this.edt = edt;
        this.paymentSucUniqueUserCount = paymentSucUniqueUserCount;
        this.paymentSucNewUserCount = paymentSucNewUserCount;
        this.ts = ts;
    }

    @Override
    public String toString() {
        return "TradePaymentWindowBean{" +
                "stt='" + stt + '\'' +
                ", edt='" + edt + '\'' +
                ", paymentSucUniqueUserCount=" + paymentSucUniqueUserCount +
                ", paymentSucNewUserCount=" + paymentSucNewUserCount +
                ", ts=" + ts +
                '}';
    }

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

    public Long getPaymentSucUniqueUserCount() {
        return paymentSucUniqueUserCount;
    }

    public void setPaymentSucUniqueUserCount(Long paymentSucUniqueUserCount) {
        this.paymentSucUniqueUserCount = paymentSucUniqueUserCount;
    }

    public Long getPaymentSucNewUserCount() {
        return paymentSucNewUserCount;
    }

    public void setPaymentSucNewUserCount(Long paymentSucNewUserCount) {
        this.paymentSucNewUserCount = paymentSucNewUserCount;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }
}

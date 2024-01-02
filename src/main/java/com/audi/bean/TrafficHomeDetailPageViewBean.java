package com.audi.bean;

/**
 * User : LinShiYue
 * Date : 2023-12-29 09:47:23
 * Description :
 */
public class TrafficHomeDetailPageViewBean {
    //窗口开始时间
    private String stt;

    //窗口结束时间
    private String sdt;

    //首页独立访客数
    private Long homeUvCt;

    //商品详情页独立访客数
    private Long goodDetailUvCt;

    //时间戳
    private Long ts;

    @Override
    public String toString() {
        return "TrafficHomeDetailPageViewBean{" +
                "stt='" + stt + '\'' +
                ", sdt='" + sdt + '\'' +
                ", homeUvCt=" + homeUvCt +
                ", goodDetailUvCt=" + goodDetailUvCt +
                ", ts=" + ts +
                '}';
    }

    public String getStt() {
        return stt;
    }

    public void setStt(String stt) {
        this.stt = stt;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public Long getHomeUvCt() {
        return homeUvCt;
    }

    public void setHomeUvCt(Long homeUvCt) {
        this.homeUvCt = homeUvCt;
    }

    public Long getGoodDetailUvCt() {
        return goodDetailUvCt;
    }

    public void setGoodDetailUvCt(Long goodDetailUvCt) {
        this.goodDetailUvCt = goodDetailUvCt;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public TrafficHomeDetailPageViewBean(String stt, String sdt, Long homeUvCt, Long goodDetailUvCt, Long ts) {
        this.stt = stt;
        this.sdt = sdt;
        this.homeUvCt = homeUvCt;
        this.goodDetailUvCt = goodDetailUvCt;
        this.ts = ts;
    }
}

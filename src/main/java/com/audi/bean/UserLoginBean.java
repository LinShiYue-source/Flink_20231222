package com.audi.bean;

/**
 * User : LinShiYue
 * Date : 2023-12-30 11:08:15
 * Description :
 */
public class UserLoginBean {
    //窗口开始时间
    String stt;

    //窗口结束时间
    String sdt;

    //独立用户数
    Long uuCt;

    //回流用户数
    Long baskCt;

    @Override
    public String toString() {
        return "UserLoginBean{" +
                "stt='" + stt + '\'' +
                ", sdt='" + sdt + '\'' +
                ", uuCt=" + uuCt +
                ", baskCt=" + baskCt +
                ", ts=" + ts +
                '}';
    }

    Long ts;

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

    public Long getUuCt() {
        return uuCt;
    }

    public void setUuCt(Long uuCt) {
        this.uuCt = uuCt;
    }

    public Long getBaskCt() {
        return baskCt;
    }

    public void setBaskCt(Long baskCt) {
        this.baskCt = baskCt;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public UserLoginBean(String stt, String sdt, Long uuCt, Long baskCt, Long ts) {
        this.stt = stt;
        this.sdt = sdt;
        this.uuCt = uuCt;
        this.baskCt = baskCt;
        this.ts = ts;
    }
}

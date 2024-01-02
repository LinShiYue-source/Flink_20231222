package com.audi.bean;

/**
 * User : LinShiYue
 * Date : 2024-01-02 14:55:16
 * Description :
 */
public class UserRegisterBean {
    // 窗口起始时间
    String stt;
    // 窗口终止时间
    String edt;
    // 注册用户数
    Long registerCt;
    // 时间戳
    Long ts;

    @Override
    public String toString() {
        return "UserRegisterBean{" +
                "stt='" + stt + '\'' +
                ", edt='" + edt + '\'' +
                ", registerCt=" + registerCt +
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

    public Long getRegisterCt() {
        return registerCt;
    }

    public void setRegisterCt(Long registerCt) {
        this.registerCt = registerCt;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public UserRegisterBean(String stt, String edt, Long registerCt, Long ts) {
        this.stt = stt;
        this.edt = edt;
        this.registerCt = registerCt;
        this.ts = ts;
    }
}

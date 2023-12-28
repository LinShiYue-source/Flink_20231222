package com.audi.bean;

/**
 * User : LinShiYue
 * Date : 2023-12-27 13:47:22
 * Description :
 */
public class TrafficPageViewBean {
    // 窗口起始时间
    String stt;
    // 窗口结束时间
    String edt;
    // app 版本号
    String vc;
    // 渠道
    String ch;
    // 地区
    String ar;
    // 新老访客状态标记
    String isNew ;
    // 独立访客数
    Long uvCt;
    // 会话数
    Long svCt;
    // 页面浏览数
    Long pvCt;
    // 累计访问时长
    Long durSum;
    // 跳出会话数
    Long ujCt;
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

    public String getVc() {
        return vc;
    }

    public void setVc(String vc) {
        this.vc = vc;
    }

    public String getCh() {
        return ch;
    }

    public void setCh(String ch) {
        this.ch = ch;
    }

    public String getAr() {
        return ar;
    }

    public void setAr(String ar) {
        this.ar = ar;
    }

    public String getIsNew() {
        return isNew;
    }

    public void setIsNew(String isNew) {
        this.isNew = isNew;
    }

    public Long getUvCt() {
        return uvCt;
    }

    public void setUvCt(Long uvCt) {
        this.uvCt = uvCt;
    }

    public Long getSvCt() {
        return svCt;
    }

    public void setSvCt(Long svCt) {
        this.svCt = svCt;
    }

    public Long getPvCt() {
        return pvCt;
    }

    public void setPvCt(Long pvCt) {
        this.pvCt = pvCt;
    }

    public Long getDurSum() {
        return durSum;
    }

    public void setDurSum(Long durSum) {
        this.durSum = durSum;
    }

    public Long getUjCt() {
        return ujCt;
    }

    public void setUjCt(Long ujCt) {
        this.ujCt = ujCt;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public TrafficPageViewBean(String stt, String edt, String vc, String ch, String ar, String isNew, Long uvCt, Long svCt, Long pvCt, Long durSum, Long ujCt, Long ts) {
        this.stt = stt;
        this.edt = edt;
        this.vc = vc;
        this.ch = ch;
        this.ar = ar;
        this.isNew = isNew;
        this.uvCt = uvCt;
        this.svCt = svCt;
        this.pvCt = pvCt;
        this.durSum = durSum;
        this.ujCt = ujCt;
        this.ts = ts;
    }

    @Override
    public String toString() {
        return "TrafficPageViewBean{" +
                "stt='" + stt + '\'' +
                ", edt='" + edt + '\'' +
                ", vc='" + vc + '\'' +
                ", ch='" + ch + '\'' +
                ", ar='" + ar + '\'' +
                ", isNew='" + isNew + '\'' +
                ", uvCt=" + uvCt +
                ", svCt=" + svCt +
                ", pvCt=" + pvCt +
                ", durSum=" + durSum +
                ", ujCt=" + ujCt +
                ", ts=" + ts +
                '}';
    }
}

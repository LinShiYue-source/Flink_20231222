package com.audi.bean;

/**
 * User : LinShiYue
 * Date : 2023-12-25 11:24:59
 * Description :
 */
public class KeywordBean {
    // 窗口起始时间
    private String stt;

    // 窗口闭合时间
    private String edt;

    // 关键词来源
    private String source;

    // 关键词
    private String keyword;

    // 关键词出现频次
    private Long keyword_count;

    // 时间戳
    private Long ts;

    @Override
    public String toString() {
        return "KeywordBean{" +
                "stt='" + stt + '\'' +
                ", edt='" + edt + '\'' +
                ", source='" + source + '\'' +
                ", keyword='" + keyword + '\'' +
                ", keyword_count=" + keyword_count +
                ", ts=" + ts +
                '}';
    }

    public KeywordBean(String stt, String edt, String source, String keyword, Long keyword_count, Long ts) {
        this.stt = stt;
        this.edt = edt;
        this.source = source;
        this.keyword = keyword;
        this.keyword_count = keyword_count;
        this.ts = ts;
    }

    public KeywordBean() {
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Long getKeyword_count() {
        return keyword_count;
    }

    public void setKeyword_count(Long keyword_count) {
        this.keyword_count = keyword_count;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }
}

package com.audi.test.myself;


public class WaterSensor {
    private String id;
    private double vc;
    private long ts;

    public WaterSensor() {
    }

    public WaterSensor(String id, double vc, long ts) {
        this.id = id;
        this.vc = vc;
        this.ts = ts;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getVc() {
        return vc;
    }

    public void setVc(double vc) {
        this.vc = vc;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    @Override
    public String toString() {
        return "WaterSensor{" +
                "id='" + id + '\'' +
                ", vc=" + vc +
                ", ts=" + ts +
                '}';
    }
}

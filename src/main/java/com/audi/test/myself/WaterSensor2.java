package com.audi.test.myself;


public class WaterSensor2 {
    private String id;
    private String name;
    private long ts;

    public WaterSensor2(String id, String name, long ts) {
        this.id = id;
        this.name = name;
        this.ts = ts;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    @Override
    public String toString() {
        return "WaterSensor2{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", ts=" + ts +
                '}';
    }

    public WaterSensor2() {
    }
}

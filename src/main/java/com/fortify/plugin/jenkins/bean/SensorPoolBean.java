package com.fortify.plugin.jenkins.bean;

public class SensorPoolBean implements Comparable {

    private String name;
    private String uuid;

    public SensorPoolBean() {}

    public SensorPoolBean(String name, String uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SensorPoolBean) {
            return ((SensorPoolBean)obj).getUuid().equals(this.getUuid());
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return getUuid().hashCode();
    }

    @Override
    public int compareTo(Object o) {
        SensorPoolBean other = (SensorPoolBean)o;
        return this.getName().compareTo(other.getName());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}

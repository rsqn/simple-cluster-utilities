package tech.rsqn.simpleclusterutilities.masterslave;

import java.io.Serializable;

public class MasterSlaveMember implements Serializable {
    private String id;
    private long startTime;
    private long expires;
    private long ts;

    public MasterSlaveMember() {
        startTime = System.currentTimeMillis();
    }

    public long getExpires() {
        return expires;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MasterSlaveMember)) return false;

        MasterSlaveMember that = (MasterSlaveMember) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "MasterSlaveMember{" +
                "id='" + id + '\'' +
                ", startTime=" + startTime +
                ", expires=" + expires +
                ", ts=" + ts +
                '}';
    }
}

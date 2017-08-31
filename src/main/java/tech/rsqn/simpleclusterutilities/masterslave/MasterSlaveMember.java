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
        if (o == null || getClass() != o.getClass()) return false;

        MasterSlaveMember that = (MasterSlaveMember) o;

        if (startTime != that.startTime) return false;
        if (expires != that.expires) return false;
        if (ts != that.ts) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (int) (startTime ^ (startTime >>> 32));
        result = 31 * result + (int) (expires ^ (expires >>> 32));
        result = 31 * result + (int) (ts ^ (ts >>> 32));
        return result;
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

package tech.rsqn.simpleclusterutilities.masterslave.model;

import java.io.Serializable;
import java.util.Objects;

public class Member implements Serializable, Cloneable {
    private String scope;
    private String id;
    private long startTime;
    private long expires;
    private long ts;
    private long ttl;

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

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public boolean isExpired() {
        return expires < System.currentTimeMillis();
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    public void updateTimestamps() {
        setTs(System.currentTimeMillis());
        setExpires(getTs() + ttl);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof Member)) {
            return false;
        }
        Member that = (Member) o;
        return Objects.equals(scope, that.scope) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scope, id);
    }

    @Override
    public String toString() {
        return scope + "/" + id;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

package tech.rsqn.simpleclusterutilities.topics;

import tech.rsqn.useful.things.identifiers.UIDHelper;

import java.io.Serializable;

public class Message<T extends Serializable> {
    private String id;
    private Subscription source;
    private T payload;

    public Message() {
        id = UIDHelper.generate();
    }

    public Message with(T s) {
        payload = s;
        return this;
    }

    public Message andSource(Subscription s) {
        source = s;
        return this;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Subscription getSource() {
        return source;
    }

    public void setSource(Subscription source) {
        this.source = source;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }
}

package tech.rsqn.simpleclusterutilities.topics;

import tech.rsqn.useful.things.concurrency.Notifiable;
import tech.rsqn.useful.things.identifiers.UIDHelper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// this is not meant to be threadsafe
public class TopicUnitTestSupport implements Topic {

    private Map<Subscription,Notifiable> subscriptionMap;

    public TopicUnitTestSupport() {
        subscriptionMap = new ConcurrentHashMap<>();
    }

    @Override
    public void publish(Message v) {
        for (Notifiable n : subscriptionMap.values()) {
           n.onNotify(v);
        }
    }

    @Override
    public <T> Subscription subscribe(Notifiable<Message> callable) {
        Subscription s = new Subscription();
        s.setId(UIDHelper.generate());

        subscriptionMap.put(s,callable);
        return s;
    }

    @Override
    public void unSubscribe(Subscription subscription) {
        subscriptionMap.remove(subscription);
    }
}

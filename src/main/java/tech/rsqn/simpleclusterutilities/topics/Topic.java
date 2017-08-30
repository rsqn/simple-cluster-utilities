package tech.rsqn.simpleclusterutilities.topics;

import tech.rsqn.useful.things.concurrency.Notifiable;

public interface Topic {
    void publish(Message v);

    <T>  Subscription subscribe(Notifiable<Message> callable);

    void unSubscribe(Subscription subscription);
}

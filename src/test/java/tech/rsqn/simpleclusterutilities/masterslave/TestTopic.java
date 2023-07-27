package tech.rsqn.simpleclusterutilities.masterslave;

import tech.rsqn.simpleclusterutilities.topics.Message;
import tech.rsqn.simpleclusterutilities.topics.Subscription;
import tech.rsqn.simpleclusterutilities.topics.Topic;
import tech.rsqn.useful.things.concurrency.Notifiable;
import tech.rsqn.useful.things.concurrency.Notifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TestTopic implements Topic {

    private List<Notifiable> listeners = new ArrayList<>();

    @Override
    public void publish(Message v) {
        listeners.stream().forEach((l) -> l.onNotify(v));
    }

    @Override
    public <T> Subscription subscribe(Notifiable<Message> callable) {
        listeners.add(callable);
        return null;
    }

    @Override
    public void unSubscribe(Subscription subscription) {

    }
}

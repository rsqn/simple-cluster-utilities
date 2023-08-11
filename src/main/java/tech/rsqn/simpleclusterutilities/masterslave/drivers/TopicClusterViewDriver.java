package tech.rsqn.simpleclusterutilities.masterslave.drivers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.rsqn.simpleclusterutilities.masterslave.model.Member;
import tech.rsqn.simpleclusterutilities.topics.Message;
import tech.rsqn.simpleclusterutilities.topics.Topic;

import java.util.ArrayList;
import java.util.List;

public class TopicClusterViewDriver implements ClusterViewDriver {
    private static Logger logger = LoggerFactory.getLogger(TopicClusterViewDriver.class);
    private Topic topic;
    private List<Member> allKnownMembers;

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public void init() {
        allKnownMembers = new ArrayList<>();

        topic.subscribe((Message m) -> {
            onEvent((Message<Member>) m);
        });
    }

    private void onEvent(Message<Member> msg) {
        Member member = msg.getPayload();

        logger.trace("received member " + member + " on topic " + topic);
        synchronized (allKnownMembers) {
            allKnownMembers.remove(member);
            allKnownMembers.add(member);
        }
    }

    @Override
    public void remove(Member member) {
        synchronized (allKnownMembers) {
            allKnownMembers.remove(member);
        }
    }

    @Override
    public List<Member> fetchMembers() {
        return new ArrayList<>(allKnownMembers);
    }


    @Override
    public void sendHeartBeat(Member member) {
        topic.publish(new Message().with(member));
        synchronized (allKnownMembers) {
            allKnownMembers.remove(member);
            allKnownMembers.add(member);
        }
    }
}

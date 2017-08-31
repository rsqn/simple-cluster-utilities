package tech.rsqn.simpleclusterutilities.masterslave;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.rsqn.simpleclusterutilities.topics.Message;
import tech.rsqn.simpleclusterutilities.topics.Topic;
import tech.rsqn.useful.things.identifiers.UIDHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TopicMasterSlaveSelector implements MasterSlaveSelector {
    private static Logger logger = LoggerFactory.getLogger(TopicMasterSlaveSelector.class);
    private Topic topic;
    private MasterSlaveMember mySelf;
    private List<MasterSlaveMember> members;
    private boolean iAmMaster = false;

    private long ttlMs = 30L * 1000L;
    private long heartbeatMs = 15L * 1000L;

    public void setTtlMs(long ttlMs) {
        this.ttlMs = ttlMs;
    }

    public void setHeartbeatMs(long heartbeatMs) {
        this.heartbeatMs = heartbeatMs;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public void init() {
        mySelf = new MasterSlaveMember();
        mySelf.setId(UIDHelper.generate());
        members = new ArrayList<>();

        topic.subscribe((Message m) -> {
            onEvent((Message<MasterSlaveMember>) m);
        });

        boolean keepRunning = true;

        Thread t = new Thread() {
            @Override
            public void run() {
                while (keepRunning) {
                    try {
                        sendHeartbeat();
                        Thread.sleep(heartbeatMs);
                    } catch (Exception ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                }
            }
        };

        t.setDaemon(true);
        t.start();
    }

    private void onEvent(Message<MasterSlaveMember> msg) {
        MasterSlaveMember member = msg.getPayload();
        enrichMemberTimstamps(member);
        enrichMemberTimstamps(mySelf);

        synchronized (members) {
            members.remove(member);
            members.add(member);
            members.remove(mySelf);
            members.add(mySelf);
            determineMaster();
        }
    }

    private void determineMaster() {
        for (MasterSlaveMember member : members) {
            if (member.getExpires() < System.currentTimeMillis()) {
                members.remove(member);
            }
        }

        Collections.sort(members, new Comparator<MasterSlaveMember>() {
            @Override
            public int compare(MasterSlaveMember o1, MasterSlaveMember o2) {
                // compare by startTIme
                Long l1 = o1.getStartTime();
                Long l2 = o2.getStartTime();

                int ret = l2.compareTo(l1);

                if (ret == 0) {
                    ret = o2.getId().compareTo(o1.getId());
                }
                return ret;
            }
        });

        MasterSlaveMember master = members.get(0);

        boolean iWasMaster = iAmMaster;

        if (master.equals(mySelf)) {
            iAmMaster = true;
        } else {
            iAmMaster = false;
        }

        if ( iWasMaster != iAmMaster) {
            logger.info("Master state change from " + iWasMaster + " to " + iAmMaster);
        }

        logger.info("Members size is " + members.size());

    }

    private void enrichMemberTimstamps(MasterSlaveMember member) {
        member.setTs(System.currentTimeMillis());
        member.setExpires(member.getTs() + ttlMs);
    }

    private void sendHeartbeat() {
        topic.publish(new Message().with(mySelf));
    }

    @Override
    public boolean isMaster() {
        return iAmMaster;
    }

    @Override
    public String toString() {
        return "TopicMasterSlaveSelector{" +
                "mySelf=" + mySelf +
                ", members=" + members +
                ", iAmMaster=" + iAmMaster +
                '}';
    }
}

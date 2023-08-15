package tech.rsqn.simpleclusterutilities.masterslave;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.rsqn.simpleclusterutilities.masterslave.drivers.ClusterViewDriver;
import tech.rsqn.simpleclusterutilities.masterslave.model.Member;
import tech.rsqn.useful.things.identifiers.UIDHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class SimpleMasterSlaveClusterView implements ClusterView, InitializingBean {
    private static Logger LOG = LoggerFactory.getLogger(SimpleMasterSlaveClusterView.class);

    @Autowired
    private ClusterViewDriver driver;
    private Member mySelf;
    private boolean iAmMaster = false;
    private Member master = null;
    private long ttlMs = 30L * 1000L;
    private long heartbeatMs = 15L * 1000L;

    private long stabilisationPeriodMs = heartbeatMs * 2;

    private List<Member> reportedMembers;

    private boolean keepRunning = false;
    private Thread t;

    public SimpleMasterSlaveClusterView() {
        reportedMembers = new ArrayList<>();
    }

    public void setTtlMs(long ttlMs) {
        this.ttlMs = ttlMs;
    }

    public void setHeartbeatMs(long heartbeatMs) {
        this.heartbeatMs = heartbeatMs;
    }

    public void setDriver(ClusterViewDriver driver) {
        this.driver = driver;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    public void init() {
        mySelf = new Member();
        mySelf.setStartTime(System.currentTimeMillis());
        mySelf.setId("member-" + UIDHelper.generate());
        mySelf.setTtl(ttlMs);

        stabilisationPeriodMs = heartbeatMs * 2;

        keepRunning = true;
        t = new Thread() {
            @Override
            public void run() {
                while (keepRunning) {
                    try {
                        mainLoop();
                        Thread.sleep(heartbeatMs);
                    } catch (Exception ex) {
                        LOG.error(ex.getMessage(), ex);
                    }
                }
            }
        };

        t.setDaemon(true);
        t.start();
    }

    @Override
    public boolean clusterContainsMemberId(String memberId) {
        return reportedMembers.stream().anyMatch((m) -> m.getId().equals(memberId));
    }

    public void stop() {
        if (keepRunning) {
            keepRunning = false;
            try {
                t.notify();
            } catch (Exception ignore) {

            }
            try {
                t.join();
            } catch (Exception ignore) {

            }
        }
    }

    private void mainLoop() {
        sendHeartbeat();
        determineMaster();
    }

    private synchronized void determineMaster() {
        List<Member> members = driver.fetchMembers();
        // remove expired
        members.stream().filter((m) -> m.isExpired()).forEach((expired) -> driver.remove(expired));
        members = driver.fetchMembers();

        members.sort(new Comparator<Member>() {
            @Override
            public int compare(Member o1, Member o2) {
                // compare by startTIme
                Long l1 = o1.getStartTime();
                Long l2 = o2.getStartTime();

                int ret = l2.compareTo(l1) * -1;
                // then ID
                if (ret == 0) {
                    ret = o2.getId().compareTo(o1.getId());
                }
                return ret;
            }
        });

        // remove duplicates after ordering (multiple heartbeats in the window are ok)
        members = members.stream().distinct().collect(Collectors.toList());

        synchronized (reportedMembers) {
            reportedMembers.clear();
            reportedMembers.addAll(members);
        }

        if (mySelf.getStartTime() + stabilisationPeriodMs > System.currentTimeMillis()) {
            LOG.info("In stabilisation period " + this.toString());
            return;
        }
        if ( members.size() == 0) {
            LOG.warn("no members? should not get here but this will resolve " + members);
            return;
        }
        Member detectedMaster = members.get(0);
        boolean logView = false;

        if (master == null) {
            LOG.info("First master selection " + detectedMaster);
            logView = true;
        } else {
            if (!detectedMaster.equals(master)) {
                LOG.info("New master selected " + detectedMaster);
                logView = true;
            }
        }
        master = detectedMaster;
        if (master.equals(mySelf)) {
            iAmMaster = true;
        } else {
            iAmMaster = false;
        }
        LOG.trace(this.toString());
        if (logView) {
            LOG.info(this.toString());
        }
    }


    private void sendHeartbeat() {
        mySelf.updateTimestamps();
        driver.sendHeartBeat(mySelf);
    }

    @Override
    public boolean isMaster() {
        return iAmMaster;
    }

    @Override
    public boolean isReady() {
        return master != null;
    }

    public Member getSelf() {
        try {
            return (Member) mySelf.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Member> getMembers() {
        synchronized (reportedMembers) {
            return new ArrayList<>(reportedMembers);
        }
    }

    @Override
    public String toString() {
        return "ClusterView[iAmMaster(" + iAmMaster +
                ") Master(" + master +
                ") Self(" + mySelf +
                ") Members(" + reportedMembers.size() + ":" + reportedMembers + ")";
    }
}

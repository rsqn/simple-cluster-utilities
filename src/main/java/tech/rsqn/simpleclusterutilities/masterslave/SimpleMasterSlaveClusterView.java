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
public class SimpleMasterSlaveClusterView implements ClusterView , InitializingBean {
    private static Logger LOG = LoggerFactory.getLogger(SimpleMasterSlaveClusterView.class);

    @Autowired
    private ClusterViewDriver driver;
    private Member mySelf;
    private String scope;
    private boolean iAmMaster = false;
    private Member master = null;
    private long ttlMs = 30L * 1000L;
    private long heartbeatMs = 15L * 1000L;

    private long stabilisationPeriodMs = heartbeatMs * 2;

    private List<Member> reportedMembers;

    private boolean keepRunning = false;
    private Thread t;

    public SimpleMasterSlaveClusterView() {
        scope = "default";
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

    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    public void init() {
        mySelf = new Member();
        mySelf.setStartTime(System.currentTimeMillis());
        mySelf.setId("mid" + UIDHelper.generate());
        mySelf.setScope(scope);
        mySelf.setTtl(ttlMs);

        stabilisationPeriodMs = heartbeatMs*2;

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
        List<Member> members = driver.fetchMembers(scope);
        // remove expired
        members.stream().filter((m) -> m.isExpired()).forEach((expired) -> driver.remove(expired));
        members = driver.fetchMembers(scope);

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

        Member detectedMaster = members.get(0);
        if ( master == null ) {
            LOG.info("First master selection " + detectedMaster);
        } else {
            if ( !detectedMaster.equals(master)) {
                LOG.info("New master selected " + detectedMaster);
            }
        }
        master = detectedMaster;
        if (master.equals(mySelf)) {
            iAmMaster = true;
        } else {
            iAmMaster = false;
        }
        LOG.trace(this.toString());
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

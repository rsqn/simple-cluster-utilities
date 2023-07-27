package tech.rsqn.simpleclusterutilities.masterslave;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import tech.rsqn.simpleclusterutilities.masterslave.drivers.TopicClusterViewDriver;
import tech.rsqn.simpleclusterutilities.masterslave.model.Member;
import tech.rsqn.simpleclusterutilities.topics.Topic;

import java.util.List;

public class TopicClusterViewTest {
    private TopicClusterViewDriver driver;
    private Topic topic;
    private SimpleMasterSlaveClusterSelector selectorA;
    private SimpleMasterSlaveClusterSelector selectorB;
    private SimpleMasterSlaveClusterSelector selectorC;

    @BeforeMethod
    public void setUp() {
        topic = new TestTopic();
        driver = new TopicClusterViewDriver();
        driver.setTopic(topic);
        driver.init();

        selectorA = new SimpleMasterSlaveClusterSelector();
        selectorA.setScope("test");
        selectorA.setTtlMs(100);
        selectorA.setHeartbeatMs(10);
        selectorA.setDriver(driver);

        selectorB = new SimpleMasterSlaveClusterSelector();
        selectorB.setScope("test");
        selectorB.setTtlMs(100);
        selectorB.setHeartbeatMs(10);
        selectorB.setDriver(driver);

        selectorC = new SimpleMasterSlaveClusterSelector();
        selectorC.setScope("test");
        selectorC.setTtlMs(100);
        selectorC.setHeartbeatMs(10);
        selectorC.setDriver(driver);
    }

    @AfterMethod
    public void tearDown() {
        selectorA.stop();
        selectorB.stop();
        selectorC.stop();
    }

    @Test
    public void shouldSelectEarliestMemberAsMaster() throws Exception {
        selectorA.init();
        selectorA.setTtlMs(25);
        selectorA.setHeartbeatMs(10);
        Thread.sleep(100);

        selectorB.init();
        selectorB.setTtlMs(25);
        selectorB.setHeartbeatMs(10);
        Thread.sleep(100);

        selectorC.init();
        selectorC.setTtlMs(25);
        selectorA.setHeartbeatMs(10);
        Thread.sleep(100);

        Thread.sleep(500);

        System.out.println(selectorA.getMembers());
        Assert.assertFalse(selectorB.isMaster());
        Assert.assertFalse(selectorC.isMaster());
        Assert.assertTrue(selectorA.isMaster());

        Assert.assertEquals(3, selectorA.getMembers().size());
        Assert.assertEquals(3, selectorC.getMembers().size());
    }

    @Test
    public void shouldSelectNextEarliestMemberAsMasterIfMasterFails() throws Exception {
        selectorA.init();
        selectorA.setTtlMs(25);
        selectorA.setHeartbeatMs(10);
        Thread.sleep(100);

        selectorB.init();
        selectorB.setTtlMs(25);
        selectorB.setHeartbeatMs(10);
        Thread.sleep(100);

        selectorC.init();
        selectorC.setTtlMs(25);
        selectorA.setHeartbeatMs(10);
        Thread.sleep(100);

        Thread.sleep(500);

        System.out.println(selectorA.getMembers());
        Assert.assertFalse(selectorB.isMaster());
        Assert.assertFalse(selectorC.isMaster());
        Assert.assertTrue(selectorA.isMaster());

        Assert.assertEquals(3, selectorA.getMembers().size());
        Assert.assertEquals(3, selectorC.getMembers().size());

        selectorA.stop();
        Thread.sleep(1000);

        Assert.assertTrue(selectorB.isMaster());
        Assert.assertEquals(2, selectorC.getMembers().size());

    }

    @Test
    public void shouldCleanupExpiredMembers() throws Exception {
        selectorA.init();
        selectorA.setTtlMs(25);
        selectorA.setHeartbeatMs(10);
        Thread.sleep(100);

        selectorB.init();
        selectorB.setTtlMs(25);
        selectorB.setHeartbeatMs(10);
        Thread.sleep(100);

        selectorC.init();
        selectorC.setTtlMs(25);
        selectorA.setHeartbeatMs(10);
        Thread.sleep(100);

        Thread.sleep(500);

        System.out.println(selectorA.getMembers());
        Assert.assertFalse(selectorB.isMaster());
        Assert.assertFalse(selectorC.isMaster());
        Assert.assertTrue(selectorA.isMaster());

        Assert.assertEquals(3, selectorA.getMembers().size());
        Assert.assertEquals(3, selectorC.getMembers().size());

        selectorA.stop();
        Thread.sleep(1000);

        List<Member> members = driver.fetchMembers(null);

        Assert.assertTrue(members.size() < 50);
    }
}

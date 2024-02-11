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
    private SimpleMasterSlaveClusterView selectorA;
    private SimpleMasterSlaveClusterView selectorB;
    private SimpleMasterSlaveClusterView selectorC;
    private SimpleMasterSlaveClusterView selectorD;

    @BeforeMethod
    public void setUp() {
        topic = new TestTopic();
        driver = new TopicClusterViewDriver();
        driver.setTopic(topic);
        driver.init();

        selectorA = new SimpleMasterSlaveClusterView();
        selectorA.setTtlMs(100);
        selectorA.setHeartbeatMs(10);
        selectorA.setDriver(driver);
        selectorA.setTag("testA");

        selectorB = new SimpleMasterSlaveClusterView();
        selectorB.setTtlMs(100);
        selectorB.setHeartbeatMs(10);
        selectorB.setDriver(driver);
        selectorB.setTag("testA");

        selectorC = new SimpleMasterSlaveClusterView();
        selectorC.setTtlMs(100);
        selectorC.setHeartbeatMs(10);
        selectorC.setDriver(driver);
        selectorC.setTag("testA");

        selectorD = new SimpleMasterSlaveClusterView();
        selectorD.setTtlMs(100);
        selectorD.setHeartbeatMs(10);
        selectorD.setDriver(driver);
        selectorD.setTag("testB");


    }

    @AfterMethod
    public void tearDown() {
        selectorA.stop();
        selectorB.stop();
        selectorC.stop();
        selectorD.stop();

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
        selectorC.setHeartbeatMs(10);
        Thread.sleep(100);

        selectorD.init();
        selectorD.setTtlMs(25);
        selectorD.setHeartbeatMs(10);
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
        selectorC.setHeartbeatMs(10);
        Thread.sleep(100);

        selectorD.init();
        selectorD.setTtlMs(25);
        selectorD.setHeartbeatMs(10);
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
        selectorC.setHeartbeatMs(10);
        Thread.sleep(100);

        selectorD.init();
        selectorD.setTtlMs(25);
        selectorD.setHeartbeatMs(10);
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

        List<Member> members = driver.fetchMembersWithAnyTag();

        Assert.assertTrue(members.size() < 50);
    }

    @Test
    public void shouldNotFindDInMembersAndShoulFindItInOtherMembers() throws Exception {
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
        selectorC.setHeartbeatMs(10);
        Thread.sleep(100);

        selectorD.init();
        selectorD.setTtlMs(25);
        selectorD.setHeartbeatMs(10);
        Thread.sleep(100);

        Thread.sleep(500);

        System.out.println(selectorA.getMembers());
        Assert.assertFalse(selectorB.isMaster());
        Assert.assertFalse(selectorC.isMaster());
        Assert.assertTrue(selectorA.isMaster());

        Assert.assertEquals(3, selectorA.getMembers().size());
        Assert.assertEquals(3, selectorC.getMembers().size());

        for (Member member : selectorA.getMembers()) {
            if ( member.getTag().equals("testB")) {
                Assert.fail();
            }
        }
        Assert.assertEquals(1, selectorA.getNonMembersWithTag("testB").size());
        Assert.assertEquals(3, selectorA.getNonMembersWithTag("testA").size());
        Assert.assertEquals(1, selectorD.getMembers().size());

        selectorA.stop();
        Thread.sleep(1000);

        List<Member> members = driver.fetchMembersWithAnyTag();

        Assert.assertTrue(members.size() < 50);
    }
}

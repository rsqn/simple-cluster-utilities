package tech.rsqn.simpleclusterutilities.masterslave;

import org.testng.Assert;
import org.testng.annotations.Test;
import tech.rsqn.simpleclusterutilities.masterslave.model.Member;

public class MemberTest {


    @Test
    public void shouldEqualIfIdAndScopeAreEqual() {
        Member a = new Member();
        Member b = new Member();

        a.setId("a");
        a.setStartTime(1);
        a.setExpires(2);

        b.setId("a");
        b.setStartTime(2);
        b.setExpires(3);

        Assert.assertEquals(a,b);

    }


    @Test
    public void shouldNotEqualIfIdAndScopeAreNotEqual() {
        Member a = new Member();
        Member b = new Member();

        a.setId("a");
        a.setStartTime(1);
        a.setExpires(2);

        b.setId("b");
        b.setStartTime(2);
        b.setExpires(3);

        Assert.assertNotEquals(a,b);

    }

    @Test
    public void shouldExpireAfterTtl() throws Exception {
        Member a = new Member();

        a.setId("a");
        a.setStartTime(System.currentTimeMillis());
        a.setTs(System.currentTimeMillis());
        a.setTtl(50);
        a.updateTimestamps();

        Assert.assertFalse(a.isExpired());

        Thread.sleep(100);

        Assert.assertTrue(a.isExpired());

    }
}

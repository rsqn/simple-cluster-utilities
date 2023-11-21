package tech.rsqn.simpleclusterutilities.masterslave;

import tech.rsqn.simpleclusterutilities.masterslave.model.Member;
import tech.rsqn.useful.things.concurrency.Callback;

import java.util.List;

public interface ClusterView {

    boolean isMaster();

    boolean isReady();

    Member getSelf();

    boolean clusterContainsMemberId(String memberId);

    void onReady(Callback cb);

    List<Member> getMembers();
}

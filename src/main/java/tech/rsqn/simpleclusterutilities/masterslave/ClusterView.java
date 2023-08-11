package tech.rsqn.simpleclusterutilities.masterslave;

import tech.rsqn.simpleclusterutilities.masterslave.model.Member;

import java.util.List;

public interface ClusterView {

    boolean isMaster();

    boolean isReady();

    Member getSelf();

    boolean clusterContainsMemberId(String memberId);

    List<Member> getMembers();
}

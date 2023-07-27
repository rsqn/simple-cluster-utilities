package tech.rsqn.simpleclusterutilities.masterslave;

import tech.rsqn.simpleclusterutilities.masterslave.model.Member;

import java.util.List;

public interface ClusterView {

    boolean isMaster();

    List<Member> getMembers();
}

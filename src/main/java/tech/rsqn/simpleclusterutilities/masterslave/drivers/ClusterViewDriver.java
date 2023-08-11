package tech.rsqn.simpleclusterutilities.masterslave.drivers;

import tech.rsqn.simpleclusterutilities.masterslave.model.Member;

import java.util.List;

public interface ClusterViewDriver {

    List<Member> fetchMembers();

    void remove(Member member);

    void sendHeartBeat(Member self);

}

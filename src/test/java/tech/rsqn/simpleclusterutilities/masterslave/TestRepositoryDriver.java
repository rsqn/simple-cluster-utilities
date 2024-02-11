package tech.rsqn.simpleclusterutilities.masterslave;

import org.springframework.beans.BeanUtils;
import tech.rsqn.simpleclusterutilities.masterslave.drivers.ClusterViewDriver;
import tech.rsqn.simpleclusterutilities.masterslave.model.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class TestRepositoryDriver implements ClusterViewDriver {
    private List<Member> collection = new CopyOnWriteArrayList<>();


    @Override
    public List<Member> fetchMembersWithTag(String tag) {
        List<Member> ret = collection.stream().filter((m) -> Objects.equals(tag,m.getTag())).collect(Collectors.toList());
        return ret;
    }

    @Override
    public List<Member> fetchMembersWithAnyTag() {
        List<Member> ret = collection.stream().collect(Collectors.toList());
        return ret;
    }

    @Override
    public void remove(Member member) {
        System.out.println("Removing a member collection is " + collection.size());
        collection.remove(member);
        System.out.println("Removing a member collection now " + collection.size());

    }

    @Override
    public void sendHeartBeat(Member self) {
        Member copy = new Member();
        BeanUtils.copyProperties(self,copy);
        collection.add(copy); // otherwise they are all not expired
    }

}

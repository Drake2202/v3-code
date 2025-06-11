/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.world;

import it.gotoandplay.smartfoxserver.data.User;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import net.sf.json.JSONArray;

/**
 *
 * @author Mystical
 */
public class PartyInfo {

    private int id;
    private List<User> members;
    private User owner;

    public PartyInfo(User owner, int id) {
        this.members = new ArrayList<User>();
        this.owner = owner;
        this.id = id;

        owner.properties.put(Users.PARTY_ID, this.id);
    }

    public JSONArray getUsers() {
        JSONArray partyMembers = new JSONArray();

        for (User u : members)
            partyMembers.add(u.properties.get(Users.USERNAME));

        partyMembers.add(this.owner.properties.get(Users.USERNAME));

        return partyMembers;
    }

    public int getMemberCount() {
        return members.size();
    }

    public User getNextOwner() {
        return members.get(0);
    }

    public boolean isMember(User user) {
        return members.contains(user);
    }

    public void addMember(User user) {
        if (!members.contains(user)) {
            members.add(user);
            user.properties.put(Users.PARTY_ID, this.id);
        } else
            throw new UnsupportedOperationException("unable to add member already in the party");
    }

    public void removeMember(User user) {
        if (members.contains(user)) {
            members.remove(user);
            user.properties.put(Users.PARTY_ID, -1);
        } else
            throw new UnsupportedOperationException("unable to remove member not in the party");
    }

    public LinkedList<SocketChannel> getChannelListButOne(User user) {
        LinkedList<SocketChannel> partyMembers = new LinkedList<SocketChannel>();

        for (User u : members)
            if (user != u)
                partyMembers.add(u.getChannel());

        partyMembers.add(this.owner.getChannel());

        return partyMembers;
    }

    public LinkedList<SocketChannel> getChannelList() {
        LinkedList<SocketChannel> partyMembers = new LinkedList<SocketChannel>();

        for (User u : members)
            partyMembers.add(u.getChannel());

        partyMembers.add(this.owner.getChannel());

        return partyMembers;
    }

    public String getOwner() {
        return (String) owner.properties.get(Users.USERNAME);
    }

    public User getOwnerObject() {
        return owner;
    }

    public void setOwner(User user) {
        addMember(this.owner);
        removeMember(user);

        this.owner = user;
        owner.properties.put(Users.PARTY_ID, this.id);
    }
}

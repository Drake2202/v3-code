/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.world;

import it.gotoandplay.smartfoxserver.data.User;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Mystical
 */
public class Parties {

    private AtomicInteger partyId;
    private Map<Integer, PartyInfo> parties;

    public Parties() {
        partyId = new AtomicInteger();
        parties = new HashMap<Integer, PartyInfo>();
    }

    public PartyInfo getPartyInfo(int partyId) {
        return parties.get(partyId);
    }

    public int size() {
        return parties.size();
    }

    public void removeParty(int partyId) {
        parties.remove(Integer.valueOf(partyId));
    }

    public int getPartyId(User owner) {
        int pid = partyId.incrementAndGet();
        parties.put(pid, new PartyInfo(owner, pid));
        return pid;
    }
}

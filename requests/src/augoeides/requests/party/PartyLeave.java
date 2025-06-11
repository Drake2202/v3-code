/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests.party;

import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.PartyInfo;
import augoeides.world.Users;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class PartyLeave implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        int partyId = (Integer) user.properties.get(Users.PARTY_ID);
        if (partyId <= 0)
            throw new RequestException("You are not in a party.");
        PartyInfo pi = world.parties.getPartyInfo(partyId);

        if (pi.getOwner().equals(user.properties.get(Users.USERNAME)))
            pi.setOwner(pi.getNextOwner());

        pi.removeMember(user);

        JSONObject pr = new JSONObject();
        pr.put("cmd", "pr");
        pr.put("owner", pi.getOwner());
        pr.put("typ", "l");
        pr.put("unm", user.properties.get(Users.USERNAME));

        world.send(pr, pi.getChannelListButOne(user));
        world.send(pr, user);

        if (pi.getMemberCount() <= 0) {
            JSONObject pc = new JSONObject();
            pc.put("cmd", "pc");
            world.send(pc, pi.getOwnerObject());
            world.parties.removeParty(partyId);
            pi.getOwnerObject().properties.put(Users.PARTY_ID, -1);
        }
    }

}

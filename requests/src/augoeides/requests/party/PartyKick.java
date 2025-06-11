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
public class PartyKick implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        String username = params[1].toLowerCase();
        User client = world.zone.getUserByName(username);
        if (client == null) throw new RequestException("Player \"" + username + "\" could not be found.");
        
        int partyId = (Integer) user.properties.get(Users.PARTY_ID);
        PartyInfo pi = world.parties.getPartyInfo(partyId);

        if (!pi.isMember(client)) throw new RequestException("That player is not in your party.");

        JSONObject pr = new JSONObject();
        pr.put("cmd", "pr");
        pr.put("owner", pi.getOwner());
        pr.put("typ", "k");
        pr.put("unm", user.properties.get(Users.USERNAME));

        world.send(pr, pi.getChannelList());

        pi.removeMember(client);

        if (pi.getMemberCount() <= 0 && pi.getOwner().equals(user.getName())) {
            JSONObject pc = new JSONObject();
            pc.put("cmd", "pc");
            world.send(pc, pi.getOwnerObject());
            world.parties.removeParty(partyId);
            pi.getOwnerObject().properties.put(Users.PARTY_ID, -1);
        }

    }

}

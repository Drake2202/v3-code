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
public class PartyPromote implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        String username = params[1].toLowerCase();
        User newOwner = world.zone.getUserByName(username);
        if (newOwner == null) throw new RequestException("Player \"" + params[1].toLowerCase() + "\" could not be found.");

        int partyId = (Integer) user.properties.get(Users.PARTY_ID);
        PartyInfo pi = world.parties.getPartyInfo(partyId);

        if (!pi.isMember(newOwner))
            throw new RequestException("That player is not in your party.");

        pi.setOwner(newOwner);

        JSONObject pp = new JSONObject();
        pp.put("cmd", "pp");
        pp.put("owner", newOwner.getName());

        world.send(pp, pi.getChannelList());
    }

}

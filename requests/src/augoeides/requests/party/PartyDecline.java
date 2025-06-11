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
public class PartyDecline implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        int partyId = Integer.parseInt(params[1]);

        PartyInfo pi = world.parties.getPartyInfo(partyId);
        JSONObject pd = new JSONObject();
        pd.put("unm", user.properties.get(Users.USERNAME));
        pd.put("cmd", "pd");

        if (pi.getMemberCount() <= 0) {
            world.parties.removeParty(partyId);
            pi.getOwnerObject().properties.put(Users.PARTY_ID, -1);
        }

        world.send(pd, pi.getOwnerObject());
        world.send(new String[]{"server", "You have declined the invitation."}, user);
    }

}

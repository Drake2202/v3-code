/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests.party;

import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.Users;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class PartySummon implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        User client = world.zone.getUserByName(params[1].toLowerCase());
        if (client == null) throw new RequestException("Player \"" + params[1].toLowerCase() + "\" could not be found.");
        int partyId = (Integer) user.properties.get(Users.PARTY_ID);
        int clientPID = (Integer) client.properties.get(Users.PARTY_ID);

        if (partyId < 0) throw new RequestException("You are not in a party!");

        if ((partyId != clientPID)) throw new RequestException("The user you are trying to summon is not in your party.");
        
        JSONObject pd = new JSONObject();
        pd.put("unm", user.properties.get(Users.USERNAME));
        pd.put("cmd", "ps");

        world.send(pd, client);
        world.send(new String[]{"server", "You attempt to summon " + client.getName() + " to you."}, user);
    }

}

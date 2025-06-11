/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests.party;

import augoeides.aqw.Settings;
import augoeides.avatars.State;
import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.Users;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.Set;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class PartyInvite implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        String username = params[1].toLowerCase();
        User client = world.zone.getUserByName(username);
        if (client == null) throw new RequestException("Player \"" + username + "\" could not be found.");

        int partyId = (Integer) user.properties.get(Users.PARTY_ID);
        int clientPID = (Integer) client.properties.get(Users.PARTY_ID);

        if (!Settings.isAllowed(Settings.PARTY, user, client)) throw new RequestException(client.getName() + " cannot recieve party invitations.");
        if (((State) client.properties.get(Users.USER_STATE)).onCombat()) throw new RequestException(client.getName() + " is currently busy.");
        if (clientPID > 0) throw new RequestException("User is already in a party!");

        if (partyId < 0)
            partyId = world.parties.getPartyId(user);

        Set<Integer> requestedParty = (Set<Integer>) client.properties.get(Users.REQUESTED_PARTY);
        requestedParty.add(partyId);

        JSONObject pi = new JSONObject();
        pi.put("cmd", "pi");
        pi.put("pid", partyId);
        pi.put("owner", user.properties.get(Users.USERNAME));

        world.send(pi, client);
        world.send(new String[]{"server", "You have invited " + client.getName() + " to join your party."}, user);
    }

}

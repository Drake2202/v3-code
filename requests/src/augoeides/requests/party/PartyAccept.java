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
import java.util.Set;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class PartyAccept implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        if ((Integer) user.properties.get(Users.PARTY_ID) > 0)
            throw new RequestException("You are already in a party!");
        int partyId = Integer.parseInt(params[1]);

        Set<Integer> requestedParty = (Set<Integer>) user.properties.get(Users.REQUESTED_PARTY);
        if (requestedParty.contains(partyId)) {
            PartyInfo pi = world.parties.getPartyInfo(partyId);

            pi.addMember(user);

            JSONObject pa = new JSONObject();
            JSONArray ul = new JSONArray();
            ul.add(user.properties.get(Users.USERNAME));
            pa.put("cmd", "pa");
            pa.put("pid", partyId);
            pa.put("ul", ul);
            pa.put("owner", pi.getOwner());

            world.send(pa, pi.getChannelListButOne(user));

            pa.put("ul", pi.getUsers());
            world.send(pa, user);

            requestedParty.remove(Integer.valueOf(partyId));
        } else {
            world.users.kick(user);
            world.users.log(user, "Packet Edit [PartyAccept]", "forcing party accept");
        }
    }

}

/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests.party;

import augoeides.aqw.Settings;
import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.PartyInfo;
import augoeides.world.Users;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;

/**
 *
 * @author Mystical
 */
public class PartyAcceptSummon implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        int partyId = (Integer) user.properties.get(Users.PARTY_ID);
        PartyInfo pi = world.parties.getPartyInfo(partyId);
        User client = pi.getOwnerObject();
        if (!Settings.isAllowed(Settings.GOTO, client, user)) {
            world.send(new String[]{"warning", user.getName() + " failed to be summoned."}, client);
            world.send(new String[]{"warning", "Summon failed. Please do not block goto requests."}, user);
        } else
            world.send(new String[]{"server", user.getName() + " accepted your summon."}, client);
    }

}

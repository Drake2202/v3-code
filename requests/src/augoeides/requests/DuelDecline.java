/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.Users;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.Set;

/**
 *
 * @author Mystical
 */
public class DuelDecline implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        User client = world.zone.getUserByName(params[0].toLowerCase());
        if(client == null) return;
        
        Set<Integer> requestedDuel = (Set<Integer>) user.properties.get(Users.REQUESTED_DUEL);
        requestedDuel.remove(Integer.valueOf(client.getUserId()));
        world.send(new String[]{"server", user.getName() + " declined your duel challenge."}, client);
    }
}

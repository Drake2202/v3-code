/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

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
public class DuelInvite implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        String username = params[0].toLowerCase();
        User client = world.zone.getUserByName(username);

        if (client == null) throw new RequestException("Player \"" + username + "\" could not be found.");
        if (client.equals(user)) throw new RequestException("You cannot challenge yourself to a duel!");
        if (!Settings.isAllowed(Settings.DUEL, user, client)) throw new RequestException("Player \"" + username + "\" is not accepting duel invites.");
        if (((State) client.properties.get(Users.USER_STATE)).onCombat()) throw new RequestException(client.getName() + " is currently busy.");

        Set<Integer> requestedDuel = (Set<Integer>) client.properties.get(Users.REQUESTED_DUEL);
        requestedDuel.add(user.getUserId());

        JSONObject di = new JSONObject();
        di.put("owner", user.getName());
        di.put("cmd", "di");

        world.send(di, client);
        world.send(new String[]{"server", "You have challenged " + username + " to a duel."}, user);
    }

}

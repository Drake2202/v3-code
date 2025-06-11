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
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class RequestFriend implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        String username = params[0].toLowerCase();
        User client = world.zone.getUserByName(username);

        if (client == null) throw new RequestException("Player \"" + username + "\" could not be found.");
        if (!Settings.isAllowed(Settings.FRIEND, user, client)) throw new RequestException(client.getName() + " is not accepting friend requests.");
        if (((State) client.properties.get(Users.USER_STATE)).onCombat()) throw new RequestException(client.getName() + " is currently busy.");

        Set<Integer> requestedFriends = (Set<Integer>) client.properties.get(Users.REQUESTED_FRIEND);
        requestedFriends.add(user.getUserId());

        JSONArray friends = world.users.getFriends(user);

        for (Object o : friends) {
            JSONObject friend = (JSONObject) o;
            if (friend.containsValue(client.properties.get(Users.USERNAME)))
                throw new RequestException(client.getName() + " was already added to your friends list.");
        }

        JSONObject friendRequest = new JSONObject();
        friendRequest.put("cmd", "requestFriend");
        friendRequest.put("unm", user.properties.get(Users.USERNAME));

        world.send(friendRequest, client);
        world.send(new String[]{"server", "You have requested " + client.getName() + " to be friends."}, user);
    }

}

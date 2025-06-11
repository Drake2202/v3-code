/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class GetFriendList implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        JSONObject friends = new JSONObject();
        friends.put("cmd", "friends");
        friends.put("friends", world.users.getFriends(user));
        world.send(friends, user);
    }

}

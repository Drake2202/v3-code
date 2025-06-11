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
public class PvpQueueRequest implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        String warzone = params[0];

        JSONObject PVPQ = new JSONObject();

        PVPQ.put("cmd", "PVPQ");
        PVPQ.put("bitSuccess", 0);

        if (warzone.equals("none")) {
            world.warzoneQueue.removeUserFromQueues(user);
            world.send(PVPQ, user);
            world.send(new String[]{"server", "You have been removed from the Warzone's queue"}, user);
        } else {
            world.warzoneQueue.queueUser(warzone, user);

            PVPQ.put("bitSuccess", 1);
            PVPQ.put("warzone", warzone);
            PVPQ.put("avgWait", -1);
            world.send(PVPQ, user);
            world.send(new String[]{"server", "You joined the Warzone queue for " + warzone + "!"}, user);
        }
    }

}

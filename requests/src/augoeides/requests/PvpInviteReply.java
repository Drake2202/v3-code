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
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class PvpInviteReply implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        JSONObject PVPQ = new JSONObject();

        PVPQ.put("cmd", "PVPQ");
        PVPQ.put("bitSuccess", 0);

        if (params[0].equals("1")) {
            Room warzoneRoom = (Room) user.properties.get(Users.ROOM_QUEUED);
            world.rooms.joinRoom(warzoneRoom, user, "Enter" + user.properties.get(Users.PVP_TEAM), "Spawn");
        }

        world.send(PVPQ, user);
    }

}

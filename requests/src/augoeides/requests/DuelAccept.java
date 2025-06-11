/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.Rooms;
import augoeides.world.Users;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import it.gotoandplay.smartfoxserver.extensions.ExtensionHelper;
import java.util.Set;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class DuelAccept implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        User client = world.zone.getUserByName(params[0].toLowerCase());
        if(client == null) return;
        
        Set<Integer> userRequestedDuel = (Set<Integer>) user.properties.get(Users.REQUESTED_DUEL);
        if (client == null) throw new RequestException("Player \"" + params[0].toLowerCase() + "\" could not be found.");

        if (userRequestedDuel.contains(client.getUserId())) {
            userRequestedDuel.remove(Integer.valueOf(client.getUserId()));

            String roomNumber = "deadlock-" + user.hashCode();
            Room newRoom = world.rooms.createRoom(roomNumber);

            JSONObject b = new JSONObject();
            b.put("id", user.getUserId());
            b.put("sName", user.properties.get(Users.USERNAME));

            newRoom.properties.put(Rooms.BLUE_TEAM_NAME, user.properties.get(Users.USERNAME));

            JSONObject r = new JSONObject();
            r.put("id", client.getUserId());
            r.put("sName", client.properties.get(Users.USERNAME));

            newRoom.properties.put(Rooms.RED_TEAM_NAME, client.properties.get(Users.USERNAME));
            JSONArray PVPFactions = new JSONArray();
            PVPFactions.add(b);
            PVPFactions.add(r);

            newRoom.properties.put(Rooms.PVP_FACTIONS, PVPFactions);

            user.properties.put(Users.PVP_TEAM, 0);
            client.properties.put(Users.PVP_TEAM, 1);

            world.rooms.joinRoom(newRoom, user, "Enter0", "Spawn");
            world.rooms.joinRoom(newRoom, client, "Enter1", "Spawn");

            JSONObject duelEx = new JSONObject();
            duelEx.put("cmd", "DuelEX");
            for (int userId : userRequestedDuel) {
                User challenger = ExtensionHelper.instance().getUserById(userId);
                world.send(duelEx, challenger);
            }
        } else {
            world.users.kick(user);
            world.users.log(user, "Packet Edit [DuelAccept]", "Force duel accept.");
        }
    }
}

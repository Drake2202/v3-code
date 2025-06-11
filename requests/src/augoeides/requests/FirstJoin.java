/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

import augoeides.config.ConfigData;
import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.Users;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class FirstJoin implements IRequest {

    private static final List<String> exceptions = Arrays.asList("house", "deadlock");

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        //world.sendGuildUpdate(world.users.getGuildObject((Integer) user.properties.get(Users.GUILD_ID)));
        sendCoreValues(user, world);

        String lastArea = (String) user.properties.get(Users.LAST_AREA);

        String roomName = "faroff";
        String roomFrame = "Enter";
        String roomPad = "Spawn";

        if (!lastArea.isEmpty()) {
            boolean gotoLastArea = true;

            String[] arrLastArea = lastArea.split("\\|");
            String lastAreaName = arrLastArea[0].split("-")[0];

            if (!world.areas.containsKey(lastAreaName)
                    || (world.areas.get(lastAreaName).isUpgrade() && (Integer) user.properties.get(Users.UPGRADE_DAYS) <= 0)
                    || (world.areas.get(lastAreaName).isPvP())
                    || (world.areas.get(lastAreaName).getReqLevel() > (Integer) user.properties.get(Users.LEVEL))
                    || (world.areas.get(lastAreaName).getFile().equals("Guildhall/guildHallTest.swf"))
                    || exceptions.contains(lastAreaName))
                gotoLastArea = false;

            if (gotoLastArea) {
                roomName = arrLastArea[0].split("-")[0];
                roomFrame = arrLastArea[1];
                roomPad = arrLastArea[2];
            }
        }

        if ((Integer) user.properties.get(Users.ACCESS) <= 0) {
            roomName = "prison";
            roomFrame = "Enter";
            roomPad = "Prison";
        }

        world.rooms.basicRoomJoin(user, roomName, roomFrame, roomPad);

        world.db.jdbc.run("UPDATE servers SET Count = ? WHERE Name = ?", world.zone.getUserCount(), ConfigData.SERVER_NAME);
    }

    private void sendCoreValues(User user, World world) {
        JSONObject cvu = new JSONObject();
        JSONObject o = new JSONObject();

        if (world.coreValues == null)
            throw new RuntimeException("CVU is null!");

        for (Map.Entry<String, Double> e : world.coreValues.entrySet())
            o.put(e.getKey(), e.getValue());

        cvu.put("cmd", "cvu");
        cvu.put("o", o);

        world.send(cvu, user);
    }
}

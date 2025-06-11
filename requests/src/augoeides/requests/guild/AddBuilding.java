/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests.guild;

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
public class AddBuilding implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        String curCell = params[1];
        int slot = Integer.parseInt(params[2]);
        int itemId = Integer.parseInt(params[3]);
        int guildId = (Integer) user.properties.get(Users.GUILD_ID);
        int size = world.items.get(itemId).getStack();

        int hallId = world.db.jdbc.queryForInt("SELECT id FROM guilds_halls WHERE Cell = ? AND GuildID = ?", curCell, guildId);

        world.db.jdbc.run("INSERT INTO guilds_halls_buildings (HallID, ItemID, Slot, Size) VALUES (?, ?, ?)", hallId, itemId, slot, size);

        JSONObject guildhall = new JSONObject();

        guildhall.put("cmd", "guildhall");
        guildhall.put("gCmd", "addbuilding");
        guildhall.put("Buildings", world.users.getBuildingString(hallId));
        guildhall.put("Cell", curCell);

        world.sendToRoom(guildhall, user, room);
    }
}

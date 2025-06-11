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
public class SaveInterior implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        String interior = params[1];
        String cell = params[2];

        int guildId = (Integer) user.properties.get(Users.GUILD_ID);
        int hallId = world.db.jdbc.queryForInt("SELECT id FROM guilds_halls WHERE Cell = ? AND GuildID = ?", cell, guildId);

        world.db.jdbc.run("UPDATE guilds_halls SET Interior = ? WHERE id = ?", interior, hallId);

        JSONObject guildhall = new JSONObject();

        guildhall.put("cmd", "guildhall");
        guildhall.put("gCmd", "updateInterior");
        guildhall.put("interior", interior);

        world.sendToRoom(guildhall, user, room);
    }

}

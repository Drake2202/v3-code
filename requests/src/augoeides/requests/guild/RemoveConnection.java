/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests.guild;

import augoeides.aqw.Pad;
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
public class RemoveConnection implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        String curCell = params[1];
        String toCell = params[2];
        String pad = params[3];

        int guildId = (Integer) user.properties.get(Users.GUILD_ID);
        int hallId = world.db.jdbc.queryForInt("SELECT id FROM guilds_halls WHERE Cell = ? AND GuildID = ?", curCell, guildId);
        int otherHallId = world.db.jdbc.queryForInt("SELECT id FROM guilds_halls WHERE Cell = ? AND GuildID = ?", toCell, guildId);
        world.db.jdbc.run("DELETE FROM guilds_halls_connections WHERE HallID = ? AND Pad = ? AND Cell = ? AND PadPosition = ?", hallId, pad, toCell, Pad.getPad(pad));
        world.db.jdbc.run("DELETE FROM guilds_halls_connections WHERE HallID = ? AND Pad = ? AND Cell = ? AND PadPosition = ?", otherHallId, Pad.getPair(pad), curCell, Pad.getPad(Pad.getPair(pad)));

        JSONObject guildhall = new JSONObject();

        JSONObject cellB = new JSONObject();
        JSONObject cellA = new JSONObject();

        cellB.put("strFrame", toCell);
        cellB.put("strConnections", world.users.getConnectionsString(otherHallId));
        cellA.put("strFrame", curCell);
        cellA.put("strConnections", world.users.getConnectionsString(hallId));

        guildhall.put("cmd", "guildhall");
        guildhall.put("gCmd", "removeconnection");
        guildhall.put("cellA", cellA);
        guildhall.put("cellB", cellB);
        guildhall.put("doDelete", true);

        world.sendToRoom(guildhall, user, room);
    }

}

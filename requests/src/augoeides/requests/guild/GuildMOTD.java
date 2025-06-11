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
public class GuildMOTD implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        if ((Integer) user.properties.get(Users.GUILD_ID) > 0 && (Integer) user.properties.get(Users.GUILD_RANK) >= 2) {
            String message = params[1].trim();
            world.db.jdbc.run("UPDATE guilds SET MessageOfTheDay = ? WHERE id = ?", message, user.properties.get(Users.GUILD_ID));

            JSONObject object = new JSONObject();
            object.put("cmd", "gMOTD");
            object.put("unm", user.properties.get(Users.USERNAME));
            object.put("msg", message);

            JSONObject guildObj = (JSONObject) user.properties.get(Users.GUILD);

            guildObj.put("MOTD", message);

            world.sendToGuild(object, guildObj);
            world.sendToGuild(new String[]{"server", "Guild message has been changed."}, guildObj);
            world.sendGuildUpdate(guildObj);
        }
    }

}

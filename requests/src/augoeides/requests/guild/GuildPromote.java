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
import jdbchelper.QueryResult;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class GuildPromote implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        int guildId = (Integer) user.properties.get(Users.GUILD_ID);
        int userRank = (Integer) user.properties.get(Users.GUILD_RANK);
        String username = params[1].toLowerCase();

        if (guildId <= 0) throw new RequestException("You do not have a guild!");
        if (userRank < 2) throw new RequestException("Invalid /gp request.");

        QueryResult result = world.db.jdbc.query("SELECT users.id, users_guilds.GuildID, users_guilds.Rank FROM users LEFT JOIN users_guilds ON UserID = id WHERE Name = ?", username);
        result.setAutoClose(true);

        if (!result.next()) throw new RequestException("Player \"" + username + "\" could not be found.");
        
        int clientGuildID = result.getInt("GuildID");
        int clientRank = result.getInt("Rank");
        int clientDbId = result.getInt("id");
        
        result.close();

        clientRank++;

        if (clientGuildID <= 0) throw new RequestException(username + " does belong to a guild!");
        if (clientGuildID != guildId) throw new RequestException(username + " is not in your guild!");
        if (clientRank >= userRank) throw new RequestException("Invalid /gp request.");

        world.db.jdbc.run("UPDATE users_guilds SET Rank = ? WHERE UserID = ?", clientRank, clientDbId);
        world.sendGuildUpdate(world.users.getGuildObject((Integer) user.properties.get(Users.GUILD_ID)));
        world.sendToGuild(new String[]{"server", username + "'s rank has been changed to " + world.users.getGuildRank(clientRank)}, (JSONObject) user.properties.get(Users.GUILD));

        User client = world.zone.getUserByName(username);
        if (client != null)
            client.properties.put(Users.GUILD_RANK, clientRank);
    }

}

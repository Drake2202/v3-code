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
public class GuildRemove implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        int guildId = (Integer) user.properties.get(Users.GUILD_ID);
        int userRank = (Integer) user.properties.get(Users.GUILD_RANK);

        QueryResult result = world.db.jdbc.query("SELECT users.id, users.Name, users_guilds.GuildID, users_guilds.Rank FROM users LEFT JOIN users_guilds ON UserID = id WHERE Name = ?", params[1]);
        if (result.next()) {
            String username = result.getString("Name");
            int clientId = result.getInt("id");
            int clientRank = result.getInt("Rank");
            int clientGuildID = result.getInt("GuildID");
            result.close();

            if (clientGuildID <= 0)
                throw new RequestException(username + " does belong to a guild!");
            else if (clientGuildID != guildId)
                throw new RequestException(username + " is not in your guild!");
            else if ((clientRank > userRank || userRank < 2) && !user.getName().equals(username.toLowerCase()))
                throw new RequestException("Invalid /gr request.");
            else {
                int remainingMembers = world.db.jdbc.queryForInt("SELECT COUNT(*) AS rowcount FROM users_guilds WHERE GuildID = ?", guildId);

                if (clientRank == 3 && remainingMembers > 1)
                    throw new RequestException("Invalid /gr request.");
                else {
                    world.db.jdbc.run("DELETE FROM users_guilds WHERE GuildID = ? AND UserID = ?", guildId, clientId);

                    if (clientRank == 3 && remainingMembers == 1)
                        world.db.jdbc.run("DELETE FROM guilds WHERE id = ?", guildId);

                    JSONObject guildObj = world.users.getGuildObject(guildId);
                    world.sendGuildUpdate(guildObj);

                    if (user.getName().equals(username.toLowerCase()))
                        world.sendToGuild(new String[]{"server", username.toLowerCase() + " has left the guild."}, guildObj);
                    else
                        world.sendToGuild(new String[]{"server", username.toLowerCase() + " has been kicked."}, guildObj);

                    User client = world.zone.getUserByName(username.toLowerCase());
                    if (client != null) {
                        JSONObject object = new JSONObject();
                        object.put("cmd", params[0]);
                        object.put("unm", username);
                        object.put("guild", guildObj);

                        client.properties.put(Users.GUILD_ID, 0);
                        client.properties.put(Users.GUILD_RANK, 0);
                        client.properties.put(Users.GUILD, new JSONObject());
                        world.send(object, world.zone.getRoom(client.getRoom()).getChannellList());
                    }
                }
            }

        } else {
            result.close();
            throw new RequestException("Player \"" + params[1].toLowerCase() + "\" could not be found.");
        }
        result.close();
    }

}

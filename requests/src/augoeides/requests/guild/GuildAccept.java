/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests.guild;

import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.Users;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.Set;
import jdbchelper.JdbcException;
import jdbchelper.QueryResult;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class GuildAccept implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        User client = world.zone.getUserByName(params[2].toLowerCase());

        if (client == null) return;

        int guildId = Integer.parseInt(params[1]);
        int clientGuildID = (Integer) client.properties.get(Users.GUILD_ID);

        if (clientGuildID != guildId) {
            world.users.kick(user);
            world.users.log(user, "Packet Edit [GuildAccept]", "Guild id does not match with requesting client");
            return;
        }

        Set<Integer> requestedGuild = (Set<Integer>) user.properties.get(Users.REQUESTED_GUILD);

        if (requestedGuild.contains(clientGuildID)) {
            requestedGuild.remove(Integer.valueOf(clientGuildID));
            world.db.jdbc.beginTransaction();
            try {
                QueryResult result = world.db.jdbc.query("SELECT * FROM users_guilds WHERE UserID = ? FOR UPDATE", user.properties.get(Users.DATABASE_ID));
                if (result.next())
                    world.db.jdbc.run("UPDATE users_guilds SET GuildID = ?, Rank = 0 WHERE UserID = ?", guildId, user.properties.get(Users.DATABASE_ID));
                else
                    world.db.jdbc.run("INSERT INTO users_guilds (`GuildID`, `UserID`, `Rank`) VALUES (?, ?, 0)", guildId, user.properties.get(Users.DATABASE_ID));
                result.close();
            } catch (JdbcException je) {
                if (world.db.jdbc.isInTransaction())
                    world.db.jdbc.rollbackTransaction();
                SmartFoxServer.log.severe("Error in guild accept transaction: " + je.getMessage());
            } finally {
                if (world.db.jdbc.isInTransaction())
                    world.db.jdbc.commitTransaction();
            }

            user.properties.put(Users.GUILD_ID, guildId);
            user.properties.put(Users.GUILD_RANK, 0);
            user.properties.put(Users.GUILD, world.users.getGuildObject(guildId));
            client.properties.put(Users.GUILD, (JSONObject) user.properties.get(Users.GUILD));

            JSONObject object = new JSONObject();
            object.put("cmd", params[0]);
            object.put("unm", user.properties.get(Users.USERNAME));
            object.put("guild", user.properties.get(Users.GUILD));
            world.sendToRoom(object, user, room);
            world.sendGuildUpdate((JSONObject) user.properties.get(Users.GUILD));
        } else {
            world.users.kick(user);
            world.users.log(user, "Packet Edit [GuildAccept]", "Forcing guild accept");
        }
    }

}

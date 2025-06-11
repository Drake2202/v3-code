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
import jdbchelper.JdbcException;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class GuildRename implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        if ((Integer) user.properties.get(Users.GUILD_ID) <= 0)
            throw new RequestException("You do not belong to a guild.", "server");
        else if ((Integer) user.properties.get(Users.GUILD_RANK) < 3)
            throw new RequestException("Invalid /rename request.");

        world.db.jdbc.beginTransaction();
        try {
            String guildName = params[1].trim();

            int Coins = world.db.jdbc.queryForInt("SELECT Coins FROM users WHERE id = ? FOR UPDATE", user.properties.get(Users.DATABASE_ID));
            if (guildName.length() > 25) {
                world.db.jdbc.rollbackTransaction();
                throw new RequestException("Guild names must be 25 characters or less.", "server");
            } else if (guildName.length() <= 0) {
                world.db.jdbc.rollbackTransaction();
                throw new RequestException("Please specify a name for your guild.", "server");
            } else if (Coins <= 0) {
                world.db.jdbc.rollbackTransaction();
                throw new RequestException("You do not have enough ACs.", "server");
            } else {
                world.db.jdbc.run("UPDATE users SET Coins = (Coins - 1000) WHERE id = ?", user.properties.get(Users.DATABASE_ID));

                int rowcount = world.db.jdbc.queryForInt("SELECT COUNT(*) AS rowcount FROM guilds WHERE Name = ?", guildName);
                if (rowcount <= 0) {
                    JSONObject guildData = (JSONObject) user.properties.get(Users.GUILD);
                    guildData.put("Name", params[1].trim());

                    world.db.jdbc.run("UPDATE guilds SET Name = ? WHERE id = ?", params[1].trim(), user.properties.get(Users.GUILD_ID));

                    world.send(new String[]{"gRename"}, user);

                    world.sendGuildUpdate(guildData);
                    world.sendToGuild(new String[]{"server", "Guild has been renamed to " + guildName + "."}, guildData);
                } else {
                    world.db.jdbc.rollbackTransaction();
                    throw new RequestException("Guild name is already in use.");
                }
            }
        } catch (JdbcException je) {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.rollbackTransaction();
            SmartFoxServer.log.severe("Error in rename guild transaction: " + je.getMessage());
        } finally {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.commitTransaction();
        }
    }

}

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
public class GuildCreate implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        world.db.jdbc.beginTransaction();
        try {
            String guildName = params[1].trim();
            int rowcount = world.db.jdbc.queryForInt("SELECT COUNT(*) AS rowcount FROM users_guilds WHERE UserID = ?", user.properties.get(Users.DATABASE_ID));
            if (rowcount <= 0) {
                int Coins = world.db.jdbc.queryForInt("SELECT Coins FROM users WHERE id = ? FOR UPDATE", user.properties.get(Users.DATABASE_ID));
                int UpgradeDays = (Integer) user.properties.get(Users.UPGRADE_DAYS);
                if (UpgradeDays < 0) {
                    world.db.jdbc.rollbackTransaction();
                    throw new RequestException("Only members may create guilds.", "server");
                } else if (guildName.length() > 25) {
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

                    rowcount = world.db.jdbc.queryForInt("SELECT COUNT(*) AS rowcount FROM guilds WHERE Name = ?", guildName);
                    if (rowcount <= 0) {

                        world.db.jdbc.run("INSERT INTO guilds (Name, MessageOfTheDay) VALUES (?, 'Hello World!')", guildName);
                        int guildId = Long.valueOf(world.db.jdbc.getLastInsertId()).intValue();
                        if (guildId > 0) {
                            world.db.jdbc.run("INSERT INTO users_guilds (`GuildID`, `UserID`, `Rank`) VALUES (?, ?, 3)", guildId, user.properties.get(Users.DATABASE_ID));
                            world.db.jdbc.commitTransaction();

                            JSONObject guildData = world.users.getGuildObject(guildId);

                            user.properties.put(Users.GUILD_ID, guildId);
                            user.properties.put(Users.GUILD, guildData);
                            user.properties.put(Users.GUILD_RANK, 3);

                            JSONObject object = new JSONObject();
                            object.put("cmd", params[0]);
                            object.put("uid", user.getUserId());
                            object.put("guild", guildData);
                            world.sendToRoom(object, user, room);

                            world.send(new String[]{"server", "Guild " + guildName + " successfuly created."}, user);
                        } else {
                            world.db.jdbc.rollbackTransaction();
                            throw new RequestException("There was an error during guild creation!");
                        }

                    } else {
                        world.db.jdbc.rollbackTransaction();
                        throw new RequestException("Guild name is already in use.");
                    }
                }
            } else {
                world.db.jdbc.rollbackTransaction();
                throw new RequestException("You already have a guild!");
            }
        } catch (JdbcException je) {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.rollbackTransaction();
            SmartFoxServer.log.severe("Error in create guild transaction: " + je.getMessage());
        } finally {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.commitTransaction();
        }

    }

}

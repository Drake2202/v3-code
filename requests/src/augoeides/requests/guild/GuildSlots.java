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
public class GuildSlots implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        int slotsToPurchase = Integer.parseInt(params[1]);

        if ((Integer) user.properties.get(Users.GUILD_ID) > 0 && (Integer) user.properties.get(Users.GUILD_RANK) == 3) {
            world.db.jdbc.beginTransaction();
            try {
                int guildSlots = world.db.jdbc.queryForInt("SELECT MaxMembers FROM guilds WHERE id = ? FOR UPDATE", user.properties.get(Users.GUILD_ID)) + slotsToPurchase;
                int totalCost = (slotsToPurchase * 200);

                if (guildSlots > 50)
                    throw new RequestException("You have already reached the maximum amount of guild member slots.");
                int coins = world.db.jdbc.queryForInt("SELECT Coins FROM users WHERE id = ?", user.properties.get(Users.DATABASE_ID));
                int coinsLeft = (coins - totalCost);
                if (coinsLeft >= 0) {
                    world.db.jdbc.run("UPDATE users SET Coins = ? WHERE id = ?", coinsLeft, user.properties.get(Users.DATABASE_ID));
                    world.db.jdbc.run("UPDATE guilds SET MaxMembers = ? WHERE id = ?", guildSlots, user.properties.get(Users.GUILD_ID));

                    world.db.jdbc.commitTransaction();

                    user.properties.put(Users.GUILD, world.users.getGuildObject((Integer) user.properties.get(Users.GUILD_ID)));

                    world.sendGuildUpdate((JSONObject) user.properties.get(Users.GUILD));
                    world.send(new String[]{"buyGSlots", params[1]}, user);
                } else
                    throw new RequestException("You don't have enough coins!");

            } catch (JdbcException je) {
                if (world.db.jdbc.isInTransaction())
                    world.db.jdbc.rollbackTransaction();
                SmartFoxServer.log.severe("Error in guild slots transaction: " + je.getMessage());
            } finally {
                if (world.db.jdbc.isInTransaction())
                    world.db.jdbc.commitTransaction();
            }
        }

    }
}

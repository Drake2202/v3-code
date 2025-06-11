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
public class BuyPlot implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        int guildId = (Integer) user.properties.get(Users.GUILD_ID);
        int guildRank = (Integer) user.properties.get(Users.GUILD_RANK);

        if (guildRank >= 2) {
            world.db.jdbc.beginTransaction();
            try {
                int gold = world.db.jdbc.queryForInt("SELECT Gold FROM users WHERE id = ? FOR UPDATE", user.properties.get(Users.DATABASE_ID));
                int hallSize = world.db.jdbc.queryForInt("SELECT HallSize FROM guilds WHERE id = ? FOR UPDATE", guildId);

                if (gold < 1000)
                    return;

                if (hallSize < 16) {
                    int deltaGold = (gold - 1000);
                    world.db.jdbc.run("UPDATE users SET Gold = ? WHERE id = ?", deltaGold, user.properties.get(Users.DATABASE_ID));
                    hallSize++;

                    world.db.jdbc.run("UPDATE guilds SET HallSize = ? WHERE id = ?", hallSize, guildId);

                    JSONObject guildhall = new JSONObject();

                    guildhall.put("cmd", "guildhall");
                    guildhall.put("gCmd", "buyPlot");
                    guildhall.put("bitSuccess", 1);

                    world.send(guildhall, user);
                }
            } catch (JdbcException je) {
                if (world.db.jdbc.isInTransaction())
                    world.db.jdbc.rollbackTransaction();
                SmartFoxServer.log.severe("Error in buy plot transaction: " + je.getMessage());
            } finally {
                if (world.db.jdbc.isInTransaction())
                    world.db.jdbc.commitTransaction();
            }
        }
    }

}

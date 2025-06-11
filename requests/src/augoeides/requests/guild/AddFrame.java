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
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import jdbchelper.JdbcException;
import jdbchelper.QueryResult;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class AddFrame implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        String newCell = params[1];
        String linkage = params[2];
        String curCell = params[3];
        String toPad = params[4];
        int purchase = Integer.parseInt(params[5]);
        int cost = Integer.parseInt(params[6]);
        boolean isCoins = Boolean.parseBoolean(params[7]);
        int guildId = (Integer) user.properties.get(Users.GUILD_ID);
        int hallId = world.db.jdbc.queryForInt("SELECT id FROM guilds_halls WHERE Cell = ? AND GuildID = ?", curCell, guildId);

        world.db.jdbc.beginTransaction();
        try {
            if (purchase > 0) {
                QueryResult userResult = world.db.jdbc.query("SELECT Gold, Coins FROM users WHERE id = ? FOR UPDATE", user.properties.get(Users.DATABASE_ID));
                if (userResult.next()) {
                    int coins = userResult.getInt("Coins");
                    int gold = userResult.getInt("Gold");
                    userResult.close();
                    boolean valid = (isCoins && (cost <= coins)) ? true : (cost <= gold);

                    if (valid) {

                        if (!isCoins) {
                            int goldLeft = (gold - cost);
                            world.db.jdbc.run("UPDATE users SET Gold = ? WHERE id=?", goldLeft, user.properties.get(Users.DATABASE_ID));
                        } else {
                            int coinsLeft = (coins - cost);
                            world.db.jdbc.run("UPDATE users SET Coins = ? WHERE id=?", coinsLeft, user.properties.get(Users.DATABASE_ID));
                        }

                        world.db.jdbc.run("INSERT INTO guilds_halls (GuildID, Linkage, Cell, Interior) VALUES (?, ?, ?, ?)", guildId, linkage, newCell, "|||");
                        int newHallId = Long.valueOf(world.db.jdbc.getLastInsertId()).intValue();
                        world.db.jdbc.run("INSERT INTO guilds_halls_connections (HallID, Pad, Cell, PadPosition) VALUES (? ,? ,? ,?)", hallId, toPad, newCell, Pad.getPad(toPad));
                        world.db.jdbc.run("INSERT INTO guilds_halls_connections (HallID, Pad, Cell, PadPosition) VALUES (? ,? ,? ,?)", newHallId, Pad.getPair(toPad), curCell, Pad.getPad(Pad.getPair(toPad)));
                    }
                }
                userResult.close();
            } else {
                world.db.jdbc.run("INSERT INTO guilds_halls (GuildID, Linkage, Cell) VALUES (?, ?, ?)", guildId, linkage, newCell);
                int newHallId = Long.valueOf(world.db.jdbc.getLastInsertId()).intValue();
                world.db.jdbc.run("INSERT INTO guilds_halls_connections (HallID, Pad, Cell, PadPosition) VALUES (? ,? ,? ,?)", hallId, toPad, newCell, Pad.getPad(toPad));
                world.db.jdbc.run("INSERT INTO guilds_halls_connections (HallID, Pad, Cell, PadPosition) VALUES (? ,? ,? ,?)", newHallId, Pad.getPair(toPad), curCell, Pad.getPad(Pad.getPair(toPad)));
            }
        } catch (JdbcException je) {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.rollbackTransaction();
            SmartFoxServer.log.severe("Error in buy frame transaction: " + je.getMessage());
        } finally {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.commitTransaction();
        }

        JSONObject guildhall = new JSONObject();

        guildhall.put("cmd", "guildhall");
        guildhall.put("gCmd", "addframe");
        guildhall.put("guildHall", world.users.getGuildHallData(guildId));
        guildhall.put("bitSuccess", 1);

        world.sendToRoom(guildhall, user, room);
    }

}

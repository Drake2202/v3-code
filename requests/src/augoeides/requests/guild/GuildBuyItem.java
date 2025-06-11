/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests.guild;

import augoeides.db.objects.Item;
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
public class GuildBuyItem implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        int guildId = (Integer) user.properties.get(Users.GUILD_ID);
        int userId = (Integer) user.properties.get(Users.DATABASE_ID);

        int itemId = Integer.parseInt(params[1]);
        //int shopId = Integer.parseInt(params[2]);
        //int shopItemId = Integer.parseInt(params[3]);

        world.db.jdbc.beginTransaction();
        try {
            //Checks will be added later. Test first lel
            Item item = world.items.get(itemId);
            int cost = item.getCost();

            QueryResult userResult = world.db.jdbc.query("SELECT Gold, Coins FROM users WHERE id = ? FOR UPDATE", user.properties.get(Users.DATABASE_ID));
            userResult.setAutoClose(true);
            if (userResult.next()) {
                int coins = userResult.getInt("Coins");
                int gold = userResult.getInt("Gold");
                userResult.close();
                boolean valid = (item.isCoins() && (cost <= coins)) ? true : (cost <= gold);

                if (valid) {
                    world.db.jdbc.run("INSERT INTO guilds_inventory (GuildID, ItemID, UserID) VALUES (? ,? ,?)", guildId, itemId, userId);
                    //Subtract User Coin/Gold in database
                    if (!item.isCoins()) {
                        int goldLeft = (gold - cost);
                        world.db.jdbc.run("UPDATE users SET Gold = ? WHERE id=?", goldLeft, user.properties.get(Users.DATABASE_ID));
                    } else {
                        int coinsLeft = (coins - cost);
                        world.db.jdbc.run("UPDATE users SET Coins = ? WHERE id=?", coinsLeft, user.properties.get(Users.DATABASE_ID));
                    }

                    JSONObject guildhall = new JSONObject();

                    guildhall.put("cmd", "guildhall");
                    guildhall.put("gCmd", "buyItem");
                    guildhall.put("Item", Item.getItemJSON(item));
                    guildhall.put("bitSuccess", 1);

                    world.send(guildhall, user);
                }
            }
            userResult.close();
        } catch (JdbcException je) {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.rollbackTransaction();
            SmartFoxServer.log.severe("Error in guild item buy transaction: " + je.getMessage());
        } finally {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.commitTransaction();
        }
    }

}

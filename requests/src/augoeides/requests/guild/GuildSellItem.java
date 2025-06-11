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
public class GuildSellItem implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        int itemId = Integer.parseInt(params[1]);
        int shopId = Integer.parseInt(params[2]);
        int guildId = (Integer) user.properties.get(Users.GUILD_ID);
        int userId = (Integer) user.properties.get(Users.DATABASE_ID);

        world.db.jdbc.beginTransaction();
        try {
            int rowcount = world.db.jdbc.queryForInt("SELECT COUNT(*) AS rowcount FROM guilds_inventory WHERE GuildID = ? AND ItemID = ? AND UserID = ?", guildId, itemId, userId);

            if (rowcount > 0) {
                QueryResult userResult = world.db.jdbc.query("SELECT Gold, Coins FROM users WHERE id = ? FOR UPDATE", userId);
                userResult.setAutoClose(true);
                if (userResult.next()) {
                    int coins = userResult.getInt("Coins");
                    int gold = userResult.getInt("Gold");
                    userResult.close();
                    Item item = world.items.get(itemId);

                    JSONObject guildhall = new JSONObject();

                    guildhall.put("cmd", "guildhall");
                    guildhall.put("gCmd", "removeItem");
                    guildhall.put("ItemID", itemId);
                    guildhall.put("bitSuccess", 1);

                    world.send(guildhall, user);

                    if (!item.isCoins()) {
                        int goldPrice = item.getCost() / 5 / 2;
                        int totalGold = (gold + goldPrice);
                        guildhall.put("iCost", goldPrice);
                        world.db.jdbc.run("UPDATE users SET Gold = ? WHERE id = ?", totalGold, userId);
                    } else {
                        int coinPrice = (item.getCost() / 4);
                        int totalCoins = (coins + coinPrice);
                        guildhall.put("iCost", coinPrice);
                        world.db.jdbc.run("UPDATE users SET Coins = ? WHERE id = ?", totalCoins, userId);
                    }

                    world.db.jdbc.run("DELETE FROM guilds_inventory WHERE GuildID = ? AND ItemID = ? AND UserID = ?", guildId, itemId, userId);
                    world.send(guildhall, user);
                }
                userResult.close();
            } else
                world.users.log(user, "Packet Edit [GuildSellItem]", "Attempted to sell an item not in possession");
        } catch (JdbcException je) {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.rollbackTransaction();
            SmartFoxServer.log.severe("Error in guild sell item transaction: " + je.getMessage());
        } finally {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.commitTransaction();
        }
    }

}

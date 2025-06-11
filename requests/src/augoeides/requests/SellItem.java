/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

import augoeides.db.objects.Item;
import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.Users;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.Date;
import jdbchelper.JdbcException;
import jdbchelper.QueryResult;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class SellItem implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        int userId = (Integer) user.properties.get(Users.DATABASE_ID);
        int itemId = Integer.parseInt(params[0]);
        int charItemId = Integer.parseInt(params[2]);

        JSONObject sell = new JSONObject();
        sell.put("cmd", "sellItem");

        world.db.jdbc.beginTransaction();
        try {
            QueryResult itemResult = world.db.jdbc.query("SELECT Quantity, DatePurchased, ItemID, UserID FROM users_items WHERE id = ?", charItemId);
            if (itemResult.next()) {
                int quantity = itemResult.getInt("Quantity");
                int userDbId = itemResult.getInt("UserID");
                int itemDbId = itemResult.getInt("ItemID");
                Date purchase = itemResult.getDate("DatePurchased");
                itemResult.close();
                
                Item item = world.items.get(itemId);

                if (userDbId == userId && itemDbId == itemId) {
                    QueryResult userResult = world.db.jdbc.query("SELECT Gold, Coins FROM users WHERE id = ? FOR UPDATE", userId);

                    if (userResult.next()) {
                        int coins = userResult.getInt("Coins");
                        int gold = userResult.getInt("Gold");
                        userResult.close();
                        Date startDate = purchase;
                        Date endDate = new Date();
                        long diff = endDate.getTime() - startDate.getTime();
                        long diffHours = diff / (60 * 60 * 1000);

                        if (!item.isCoins()) {
                            int goldPrice = item.getCost() / 5 / 2;
                            int totalGold = (gold + goldPrice);
                            sell.put("intAmount", goldPrice);
                            world.db.jdbc.run("UPDATE users SET Gold = ? WHERE id = ?", totalGold, userId);
                        } else {
                            int coinPrice = (diffHours < 24) ? ((item.getCost() / 10) * 9) : (item.getCost() / 4);
                            int totalCoins = (coins + coinPrice);
                            sell.put("intAmount", coinPrice);
                            world.db.jdbc.run("UPDATE users SET Coins = ? WHERE id = ?", totalCoins, userId);
                        }

                        if (item.getStack() > 1) {
                            int quantityLeft = (quantity - 1);
                            if (quantityLeft > 0)
                                world.db.jdbc.run("UPDATE users_items SET Quantity = ? WHERE id = ?", quantityLeft, charItemId);
                            else
                                world.db.jdbc.run("DELETE FROM users_items WHERE id = ?", charItemId);
                        } else
                            world.db.jdbc.run("DELETE FROM users_items WHERE id = ?", charItemId);

                        sell.put("CharItemID", charItemId);
                        sell.put("bCoins", item.isCoins() ? 1 : 0);

                        world.send(sell, user);
                    }
                    userResult.close();
                } else
                    world.users.log(user, "Packet Edit [SellItem]", "Attempted to sell an item not in possession");
            }
            itemResult.close();
        } catch (JdbcException je) {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.rollbackTransaction();
            SmartFoxServer.log.severe("Error in sell item transaction: " + je.getMessage());
        } finally {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.commitTransaction();
        }
    }

}

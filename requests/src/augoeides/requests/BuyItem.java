/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

import augoeides.db.objects.Item;
import augoeides.db.objects.Shop;
import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.Users;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.Iterator;
import java.util.Map;
import jdbchelper.JdbcException;
import jdbchelper.QueryResult;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class BuyItem implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        int itemId = Integer.parseInt(params[0]);
        int shopId = Integer.parseInt(params[1]);

        Item item = world.items.get(itemId);
        Shop shop = world.shops.get(shopId);

        if (!shop.items.containsValue(itemId)) {
            world.users.log(user, "Packet Edit [BuyItem]", "Attempted to purchase an item from wrong shop");
            return;
        }

        if ((shop.isStaff()) && !(user.isAdmin() || user.isModerator())) {
            world.users.log(user, "Packet Edit [BuyItem]", "Attempted to purchase from staff shop");
            return;
        }

        int cost = (item.getCost() * item.getQuantity());
        int userLevel = (Integer) user.properties.get(Users.LEVEL);
        int houseCount = world.db.jdbc.queryForInt("SELECT COUNT(*) AS rowcount FROM users_items LEFT JOIN items ON items.id = users_items.ItemID WHERE Equipment IN ('ho','hi') AND Bank = 0 AND UserID = ?", user.properties.get(Users.DATABASE_ID));
        int inventoryCount = world.db.jdbc.queryForInt("SELECT COUNT(*) AS rowcount FROM users_items LEFT JOIN items ON items.id = users_items.ItemID WHERE Equipment NOT IN ('ho','hi') AND Bank = 0 AND UserID = ?", user.properties.get(Users.DATABASE_ID));
        boolean upgrade = (Integer) user.properties.get(Users.UPGRADE_DAYS) > 0;

        JSONObject buy = new JSONObject();

        buy.put("cmd", "buyItem");
        buy.put("bitSuccess", 0);
        buy.put("CharItemID", -1);

        if (item.isUpgrade() && !upgrade)
            buy.put("strMessage", "This item is member only!");
        else if (item.getLevel() > userLevel)
            buy.put("strMessage", "Level requirement not met!");
        else if (item.isStaff() && !(user.isAdmin() || user.isModerator())) {
            buy.put("strMessage", "Test Item: Cannot be purchased yet!");
            world.users.log(user, "Packet Edit [BuyItem]", "Attempted to purchase a staff only item");
        } else if (!shop.isHouse() && inventoryCount >= (Integer) user.properties.get(Users.SLOTS_BAG))
            buy.put("strMessage", "Inventory Full!");
        else if (shop.isHouse() && houseCount >= (Integer) user.properties.get(Users.SLOTS_HOUSE))
            buy.put("strMessage", "House Inventory Full!");
        else {
            //LQ shop check
            if (shop.isLimited()) {
                int remainingQuantity = world.db.jdbc.queryForInt("SELECT QuantityRemain FROM shops_items WHERE ShopID = ? AND ItemID = ?", shopId, itemId);
                if (remainingQuantity <= 0) {
                    buy.put("strMessage", item.getName());
                    buy.put("bSoldOut", 1);
                    world.send(buy, user);
                    return;
                }
            }

            //Check item requirements
            if (!item.requirements.isEmpty()) {
                for (Map.Entry<Integer, Integer> require : item.requirements.entrySet()) {
                    int reqItemId = require.getKey();
                    Item reqItemObj = world.items.get(reqItemId);
                    int quantityNeeded = require.getValue() >= reqItemObj.getStack() ? reqItemObj.getStack() : require.getValue();

                    QueryResult result = world.db.jdbc.query("SELECT Quantity FROM users_items WHERE ItemID = ? AND UserID = ?", reqItemId, user.properties.get(Users.DATABASE_ID));
                    if (result.next()) {
                        int quantity = result.getInt("Quantity");
                        result.close();
                        if (quantity < quantityNeeded) {
                            buy.put("strMessage", "You do not meet the requirements to buy this item.");
                            world.send(buy, user);
                            return;
                        }
                    } else {
                        result.close();
                        buy.put("strMessage", "You do not meet the requirements to buy this item.");
                        world.send(buy, user);
                        return;
                    }
                    result.close();
                }

                world.users.turnInItems(user, item.requirements);
            }

            world.db.jdbc.beginTransaction();
            try {
                QueryResult userResult = world.db.jdbc.query("SELECT Gold, Coins FROM users WHERE id = ? FOR UPDATE", user.properties.get(Users.DATABASE_ID));
                if (userResult.next()) {
                    int coins = userResult.getInt("Coins");
                    int gold = userResult.getInt("Gold");
                    userResult.close();

                    boolean valid = (item.isCoins() && (cost <= coins)) ? true : (cost <= gold);

                    if (valid) {
                        QueryResult itemResult = world.db.jdbc.query("SELECT id FROM users_items WHERE ItemID = ? AND UserID = ?", itemId, user.properties.get(Users.DATABASE_ID));

                        int charItemId;

                        if (itemResult.next()) {
                            charItemId = itemResult.getInt("id");
                            itemResult.close();

                            if (item.getStack() > 1) {
                                int quantity = world.db.jdbc.queryForInt("SELECT Quantity FROM users_items WHERE ItemID = ? AND UserID = ? FOR UPDATE", itemId, user.properties.get(Users.DATABASE_ID));

                                if (quantity < item.getStack())
                                    world.db.jdbc.run("UPDATE users_items SET Quantity = ? WHERE ItemID = ? AND UserID = ?", (quantity + item.getQuantity()), itemId, user.properties.get(Users.DATABASE_ID));
                                else {
                                    world.db.jdbc.rollbackTransaction();
                                    buy.put("strMessage", "You cannot have more than " + item.getStack() + " of that item!");
                                    world.send(buy, user);
                                    world.users.log(user, "Packet Edit [BuyItem]", "Attempted to purchase more than stack value");
                                    return;
                                }
                            } else if (item.getStack() == 1) {
                                world.db.jdbc.rollbackTransaction();
                                buy.put("strMessage", "You cannot have more than " + item.getStack() + " of that item!");
                                world.send(buy, user);
                                world.users.log(user, "Packet Edit [BuyItem]", "Attempted to purchase more than stack value");
                                return;
                            }
                        } else {
                            world.db.jdbc.run("INSERT INTO users_items (UserID, ItemID, EnhID, Equipped, Quantity, Bank, DatePurchased) VALUES (?, ?, ?, 0, ?, 0, NOW())", user.properties.get(Users.DATABASE_ID), itemId, item.getEnhId(), item.getQuantity());
                            charItemId = Long.valueOf(world.db.jdbc.getLastInsertId()).intValue();
                        }
                        itemResult.close();

                        if (charItemId > 0) {
                            //Subtract User Coin/Gold in database
                            if (!item.isCoins()) {
                                int goldLeft = (gold - cost);

                                world.db.jdbc.run("UPDATE users SET Gold = ? WHERE id=?", goldLeft, user.properties.get(Users.DATABASE_ID));
                            } else {
                                int coinsLeft = (coins - cost);

                                world.db.jdbc.run("UPDATE users SET Coins = ? WHERE id=?", coinsLeft, user.properties.get(Users.DATABASE_ID));
                            }

                            //Update LQShop
                            if (shop.isLimited())
                                world.db.jdbc.run("UPDATE shops_items SET QuantityRemain = (QuantityRemain - 1) WHERE ShopID = ? AND ItemID = ?", shopId, itemId);

                            buy.put("bitSuccess", 1);
                            buy.put("CharItemID", charItemId);
                        } else {
                            world.db.jdbc.rollbackTransaction();
                            buy.put("strMessage", "An error occured while purchasing the item!");
                        }

                    } else
                        buy.put("strMessage", "Insufficient funds!");
                }
                userResult.close();
            } catch (JdbcException je) {
                if (world.db.jdbc.isInTransaction())
                    world.db.jdbc.rollbackTransaction();
                SmartFoxServer.log.severe("Error in buy item transaction: " + je.getMessage());
            } finally {
                if (world.db.jdbc.isInTransaction())
                    world.db.jdbc.commitTransaction();
            }

        }

        world.send(buy, user);
    }

}

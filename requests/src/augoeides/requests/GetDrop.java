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
import java.util.Map;
import java.util.Queue;
import jdbchelper.JdbcException;
import jdbchelper.QueryResult;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class GetDrop implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        Map<Integer, Queue<Integer>> drops = (Map<Integer, Queue<Integer>>) user.properties.get(Users.DROPS);

        int itemId = Integer.parseInt(params[0]);
        Item item = world.items.get(itemId);

        if (!drops.containsKey(itemId)) {
            world.users.log(user, "Packet Edit [GetDrop]", "Attemped get undropped item: " + item.getName());
            return;
        }

        Queue<Integer> quantities = drops.get(itemId);
        if(quantities == null) return;
        int quantityToDrop = quantities.poll();

        JSONObject gd = new JSONObject();
        gd.put("cmd", "getDrop");
        gd.put("ItemID", itemId);
        gd.put("bSuccess", "0");

        world.db.jdbc.beginTransaction();
        try {
            QueryResult itemResult = world.db.jdbc.query("SELECT id FROM users_items WHERE ItemID = ? AND UserID = ?", itemId, user.properties.get(Users.DATABASE_ID));

            int charItemId;

            if (itemResult.next()) {
                charItemId = itemResult.getInt("id");
                itemResult.close();
                if (item.getStack() > 1) {
                    int quantity = world.db.jdbc.queryForInt("SELECT Quantity FROM users_items WHERE ItemID = ? AND UserID = ? FOR UPDATE", itemId, user.properties.get(Users.DATABASE_ID));

                    if (quantity < item.getStack())
                        world.db.jdbc.run("UPDATE users_items SET Quantity = ? WHERE ItemID = ? AND UserID = ?", (quantity + quantityToDrop), itemId, user.properties.get(Users.DATABASE_ID));
                    else {
                        world.db.jdbc.rollbackTransaction();
                        world.send(gd, user);
                        return;
                    }
                } else if (item.getStack() == 1) {
                    world.db.jdbc.rollbackTransaction();
                    world.send(gd, user);
                    return;
                }
            } else {
                world.db.jdbc.run("INSERT INTO users_items (UserID, ItemID, EnhID, Equipped, Quantity, Bank, DatePurchased) VALUES (?, ?, ?, 0, ?, 0, NOW())", user.properties.get(Users.DATABASE_ID), itemId, item.getEnhId(), quantityToDrop);
                charItemId = Long.valueOf(world.db.jdbc.getLastInsertId()).intValue();
            }
            itemResult.close();

            if (charItemId > 0) {
                gd.put("CharItemID", charItemId);
                gd.put("bBank", false);
                gd.put("iQty", quantityToDrop);
                gd.put("bSuccess", "1");

                if (!item.getReqQuests().isEmpty())
                    gd.put("showDrop", "1");

                world.send(gd, user);

                if (quantities.isEmpty())
                    drops.remove(Integer.valueOf(itemId));
            } else
                world.db.jdbc.rollbackTransaction();
        } catch (JdbcException je) {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.rollbackTransaction();
            SmartFoxServer.log.severe("Error in get drop transaction: " + je.getMessage());
        } finally {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.commitTransaction();
        }
    }

}

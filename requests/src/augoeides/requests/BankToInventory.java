/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

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
public class BankToInventory implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        int userDbId = (Integer) user.properties.get(Users.DATABASE_ID);
        int itemId = Integer.parseInt(params[0]);
        int charItemId = Integer.parseInt(params[1]);

        if (world.items.get(itemId).isTemporary()) {
            world.db.jdbc.run("UPDATE users SET Access = 0, PermamuteFlag = 0 WHERE id = ?", user.properties.get(Users.DATABASE_ID));
            world.users.kick(user);
            world.users.log(user, "Packet Edit [BankToInventory]", "Attempting to transfer temporary items.");
        }

        int inventoryCount = world.db.jdbc.queryForInt("SELECT COUNT(*) AS rowcount FROM users_items LEFT JOIN items ON items.id = users_items.ItemID WHERE Equipment NOT IN ('ho','hi') AND Bank = 0 AND UserID = ?", user.properties.get(Users.DATABASE_ID));

        if (inventoryCount >= (Integer) user.properties.get(Users.SLOTS_BAG))
            throw new RequestException("Inventory Full!");

        world.db.jdbc.beginTransaction();
        try {
            QueryResult result = world.db.jdbc.query("SELECT ItemID, EnhID, UserID, Quantity FROM users_items WHERE id = ? FOR UPDATE", charItemId);

            if (result.next())
                if (userDbId == result.getInt("UserID") && itemId == result.getInt("ItemID")) {
                    world.db.jdbc.run("UPDATE users_items SET Bank = 0 WHERE id = ?", charItemId);

                    JSONObject bfi = new JSONObject();
                    bfi.put("cmd", "bankToInv");
                    bfi.put("ItemID", itemId);

                    world.send(bfi, user);
                } else {
                    world.users.kick(user);
                    world.users.log(user, "Packet Edit [BankToInventory]", "Attemping to put an item into inventory from bank not in possession.");
                }
            result.close();
        } catch (JdbcException je) {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.rollbackTransaction();
            SmartFoxServer.log.severe("Error in bank to inventory transaction: " + je.getMessage());
        } finally {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.commitTransaction();
        }
    }
}

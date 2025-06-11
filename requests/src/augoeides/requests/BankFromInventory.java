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
public class BankFromInventory implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        int userDbId = (Integer) user.properties.get(Users.DATABASE_ID);
        int itemId = Integer.parseInt(params[0]);
        int charItemId = Integer.parseInt(params[1]);

        if (world.items.get(itemId).isTemporary()) {
            world.db.jdbc.run("UPDATE users SET Access = 0, PermamuteFlag = 1 WHERE id = ?", user.properties.get(Users.DATABASE_ID));
            world.users.kick(user);
            world.users.log(user, "Packet Edit [BankToInventory]", "Attempting to transfer temporary items.");
        }

        if (!world.items.get(itemId).isCoins()) {
            int bankCount = world.users.getBankCount(user);
            int bankSlots = (Integer) user.properties.get(Users.SLOTS_BANK);

            if (bankCount >= bankSlots)
                throw new RequestException("Bank Inventory Full!");
        }

        world.db.jdbc.beginTransaction();
        try {
            QueryResult result = world.db.jdbc.query("SELECT ItemID, UserID FROM users_items WHERE id = ? FOR UPDATE", charItemId);

            if (result.next())
                if (userDbId == result.getInt("UserID") && itemId == result.getInt("ItemID")) {
                    int success = world.db.jdbc.execute("UPDATE users_items SET Bank = 1 WHERE id = ?", charItemId);

                    JSONObject bfi = new JSONObject();
                    bfi.put("cmd", "bankFromInv");
                    bfi.put("ItemID", itemId);
                    bfi.put("bSuccess", success);

                    if (success == 0)
                        bfi.put("msg", "An error occured while transferring your item to bank.");

                    world.send(bfi, user);
                } else {
                    world.users.kick(user);
                    world.users.log(user, "Packet Edit [BankFromInventory]", "Attemping to put an item into the bank not in possession.");
                }
            result.close();
        } catch (JdbcException je) {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.rollbackTransaction();
            SmartFoxServer.log.severe("Error in bank from inventory transaction: " + je.getMessage());
        } finally {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.commitTransaction();
        }
    }
}

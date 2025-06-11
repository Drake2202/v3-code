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
public class BankSwapInventory implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        int userDbId = (Integer) user.properties.get(Users.DATABASE_ID);
        int itemId = Integer.parseInt(params[0]);
        int charItemId = Integer.parseInt(params[1]);
        int itemId2 = Integer.parseInt(params[2]);
        int charItemId2 = Integer.parseInt(params[3]);

        if (world.items.get(itemId).isTemporary() || world.items.get(itemId2).isTemporary()) {
            world.db.jdbc.run("UPDATE users SET Access = 0, PermamuteFlag = 0 WHERE id = ?", user.properties.get(Users.DATABASE_ID));
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
            QueryResult result = world.db.jdbc.query("SELECT ItemID, UserID, EnhID, Quantity FROM users_items WHERE id IN (?, ?) FOR UPDATE", charItemId, charItemId2);

            boolean item1 = false;
            boolean item2 = false;

            while (result.next())
                if (userDbId == result.getInt("UserID") && itemId == result.getInt("ItemID"))
                    item1 = true;
                else if (userDbId == result.getInt("UserID") && itemId2 == result.getInt("ItemID"))
                    item2 = true;
            result.close();

            if (item1 && item2) {
                world.db.jdbc.run("UPDATE users_items SET Bank = 1 WHERE id = ?", charItemId);
                world.db.jdbc.run("UPDATE users_items SET Bank = 0 WHERE id = ?", charItemId2);

                JSONObject bsi = new JSONObject();
                bsi.put("cmd", "bankSwapInv");
                bsi.put("invItemID", itemId);
                bsi.put("bankItemID", itemId2);

                world.send(bsi, user);
            } else {
                world.users.kick(user);
                world.users.log(user, "Packet Edit [BankSwapInventory]", "Attemping to swap items not in possession.");
            }
        } catch (JdbcException je) {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.rollbackTransaction();
            SmartFoxServer.log.severe("Error bank swap inventory transaction: " + je.getMessage());
        } finally {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.commitTransaction();
        }
    }
}

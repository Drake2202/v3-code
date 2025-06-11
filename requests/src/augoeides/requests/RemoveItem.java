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
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import jdbchelper.QueryResult;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class RemoveItem implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        int userId = (Integer) user.properties.get(Users.DATABASE_ID);
        int itemId = Integer.parseInt(params[0]);
        int charItemId = Integer.parseInt(params[1]);
        int quantityToRemove = params.length > 2 ? Integer.parseInt(params[2]) : 1;

        Item item = world.items.get(itemId);

        if (quantityToRemove < 1) {
            world.users.log(user, "Packet Edit [RemoveItem]", "Quantity should not be lesser than 1");
            return;
        }

        QueryResult itemResult = world.db.jdbc.query("SELECT Quantity, UserID, ItemID FROM users_items WHERE id = ?", charItemId);
        if (itemResult.next()) {

            int quantity = itemResult.getInt("Quantity");
            int userDbId = itemResult.getInt("UserID");
            int itemDbId = itemResult.getInt("ItemID");

            itemResult.close();

            if (userDbId == userId && itemDbId == itemId) {
                if (item.getStack() > 1) {
                    int quantityLeft = (quantity - quantityToRemove);
                    if (quantityLeft > 0)
                        world.db.jdbc.run("UPDATE users_items SET Quantity = ? WHERE id = ?", quantityLeft, charItemId);
                    else
                        world.db.jdbc.run("DELETE FROM users_items WHERE id = ?", charItemId);
                } else
                    world.db.jdbc.run("DELETE FROM users_items WHERE id = ?", charItemId);

                JSONObject delete = new JSONObject();
                delete.put("cmd", "removeItem");
                delete.put("bitSuccess", 1);
                delete.put("CharItemID", charItemId);

                if (quantityToRemove > 1)
                    delete.put("iQty", quantityToRemove);

                world.send(delete, user);
            } else
                world.users.log(user, "Packet Edit [RemoveItem]", "Attempted to delete an item not in possession");
        }
        itemResult.close();
    }
}

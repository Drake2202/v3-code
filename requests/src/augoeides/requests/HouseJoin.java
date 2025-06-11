/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

import augoeides.db.objects.House;
import augoeides.db.objects.Item;
import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.Date;
import jdbchelper.NoResultException;
import jdbchelper.QueryResult;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class HouseJoin implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        String username = params[0].toLowerCase();
        QueryResult result = world.db.jdbc.query("SELECT id, Name, HouseInfo FROM users WHERE Name = ?", username);
        result.setAutoClose(true);
        if (result.next()) {
            int userDbId = result.getInt("id");
            try {
                int houseItemId = world.db.jdbc.queryForInt("SELECT ItemID FROM users_items LEFT JOIN items ON items.id = ItemID WHERE Equipment = 'ho' AND Equipped = 1 AND UserID = ?", userDbId);

                if (!world.areas.containsKey("house-" + userDbId)) {
                    House house = new House(getHouseItems(world, userDbId), result.getString("HouseInfo"), result.getString("Name").toLowerCase(), userDbId);
                    house.setFile(world.items.get(houseItemId).getFile());
                    world.areas.put("house-" + userDbId, house);
                }

                Room roomToJoin = world.zone.getRoomByName("house-" + userDbId);

                if (roomToJoin != null)
                    world.rooms.joinRoom(roomToJoin, user);

                roomToJoin = world.rooms.createRoom("house-" + userDbId);
                world.rooms.joinRoom(roomToJoin, user);
            } catch (NoResultException nre) {
                if (result.getString("Name").toLowerCase().equals(user.getName()))
                    world.rooms.basicRoomJoin(user, "buyhouse");
                else
                    world.send(new String[]{"warning", "This player does not own a house!"}, user);
            }
        } else {
            result.close();
            throw new RequestException("Player \"" + username + "\" could not be found!");
        }
        result.close();
    }

    public JSONArray getHouseItems(World world, int userDbId) {
        JSONArray items = new JSONArray();
        QueryResult result = world.db.jdbc.query("SELECT users_items.id, ItemID, Equipped, users_items.Quantity, DatePurchased FROM users_items LEFT JOIN items ON items.id = ItemID WHERE Equipment IN ('ho','hi') AND UserID = ?", userDbId);
        while (result.next()) {
            int itemId = result.getInt("ItemID");
            Item itemObj = world.items.get(itemId);

            JSONObject item = Item.getItemJSON(itemObj);
            item.put("CharItemID", result.getInt("id"));
            item.put("bEquip", result.getString("Equipped"));
            item.put("iQty", result.getString("Quantity"));

            /* For AC Items */
            if (itemObj.isCoins()) {
                Date startDate = result.getDate("DatePurchased");
                Date endDate = new Date();
                long diff = endDate.getTime() - startDate.getTime();
                long diffHours = diff / (60 * 60 * 1000);

                item.put("iHrs", diffHours);
                item.put("dPurchase", result.getString("DatePurchased").replaceAll(" ", "T"));
            }
            items.add(item);

        }
        result.close();
        return items;
    }
}

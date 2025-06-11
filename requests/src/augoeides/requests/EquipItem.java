/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

import augoeides.db.objects.Enhancement;
import augoeides.db.objects.House;
import augoeides.db.objects.Item;
import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.Users;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import jdbchelper.JdbcException;
import jdbchelper.NoResultException;
import jdbchelper.QueryResult;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class EquipItem implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        int itemId = Integer.parseInt(params[0]);
        Item item = world.items.get(itemId);
        if (item.isStaff() && !(user.isAdmin() || user.isModerator())) {
            world.db.jdbc.run("UPDATE users SET Access = 0, PermamuteFlag = 0 WHERE id = ?", user.properties.get(Users.DATABASE_ID));
            world.users.kick(user);
            world.users.log(user, "Packet Edit [EquipItem]", "Banned for item id exploit.");
        }

        world.db.jdbc.beginTransaction();
        try {
            QueryResult result = world.db.jdbc.query("SELECT EnhID, Quantity FROM users_items WHERE ItemID = ? AND UserID = ? FOR UPDATE", itemId, user.properties.get(Users.DATABASE_ID));
            if (result.next()) {

                int quantity = result.getInt("Quantity");

                Enhancement enhancement = world.enhancements.get(result.getInt("EnhID"));

                JSONObject eqp = (JSONObject) user.properties.get(Users.EQUIPMENT);

                if (eqp.has(item.getEquipment())) {
                    JSONObject oldItem = eqp.getJSONObject(item.getEquipment());
                    int oldItemId = oldItem.getInt("ItemID");
                    world.db.jdbc.run("UPDATE users_items SET Equipped = 0 WHERE ItemID = ? AND UserID = ?", oldItemId, user.properties.get(Users.DATABASE_ID));
                }

                world.db.jdbc.run("UPDATE users_items SET Equipped = 1 WHERE ItemID = ? AND UserID = ?", itemId, user.properties.get(Users.DATABASE_ID));

                JSONObject eqpObj = new JSONObject();
                JSONObject ei = new JSONObject();

                ei.put("uid", user.getUserId());
                ei.put("cmd", "equipItem");
                ei.put("ItemID", itemId);
                ei.put("strES", item.getEquipment());
                ei.put("sFile", item.getFile());
                ei.put("sLink", item.getLink());
                ei.put("sMeta", item.getMeta());

                eqpObj.put("ItemID", itemId);
                eqpObj.put("sFile", item.getFile());
                eqpObj.put("sLink", item.getLink());

                if (item.getEquipment().equals(Item.EQUIPMENT_WEAPON)) {
                    ei.put("sType", item.getType());
                    eqpObj.put("sType", item.getType());

                    user.properties.put(Users.ITEM_WEAPON, item);
                    user.properties.put(Users.ITEM_WEAPON_ENHANCEMENT, enhancement);
                }

                eqp.put(item.getEquipment(), eqpObj);

                world.sendToRoom(ei, user, room);

                if (item.getEquipment().equals(Item.EQUIPMENT_CLASS) || item.getEquipment().equals(Item.EQUIPMENT_CAPE) || item.getEquipment().equals(Item.EQUIPMENT_HELM) || item.getEquipment().equals(Item.EQUIPMENT_WEAPON)) {
                    if (item.getEquipment().equals(Item.EQUIPMENT_CLASS))
                        world.users.updateClass(user, item, quantity);

                    world.users.updateStats(user, enhancement, item.getEquipment());
                    world.users.sendStats(user);
                }

                if (item.getEquipment().equals(Item.EQUIPMENT_HOUSE)) {
                    world.db.jdbc.run("UPDATE users SET HouseInfo = '' WHERE id = ?", user.properties.get(Users.DATABASE_ID));

                    String houseId = "house-" + user.properties.get(Users.DATABASE_ID);
                    Room house = world.zone.getRoomByName(houseId);

                    if (house != null) {
                        House houseObj = (House) world.areas.get("house-" + user.properties.get(Users.DATABASE_ID));
                        houseObj.setHouseInfo("");
                        houseObj.setFile(item.getFile());

                        User[] arrUsers = house.getAllUsers();
                        for (User playerInRoom : arrUsers) {
                            world.send(new String[]{"server", "The map \"house-" + user.properties.get(Users.DATABASE_ID) + "\" is being rebuilt. You may join again in a few moments."}, playerInRoom);
                            world.rooms.basicRoomJoin(user, "faroff");
                        }
                    }
                }
            }
            result.close();
        } catch (NoResultException nre) {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.rollbackTransaction();
            SmartFoxServer.log.severe("Error in equip item: " + nre.getMessage());
        } catch (JdbcException je) {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.rollbackTransaction();
            SmartFoxServer.log.severe("Error in equip item transaction: " + je.getMessage());
        } finally {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.commitTransaction();
        }
    }
}

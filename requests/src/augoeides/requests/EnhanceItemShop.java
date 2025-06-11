/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

import augoeides.db.objects.Enhancement;
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
public class EnhanceItemShop implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        JSONObject eis = new JSONObject();

        int userItemId = Integer.parseInt(params[0]);
        int enhancementItemId = Integer.parseInt(params[1]);

        Item item = world.items.get(enhancementItemId);
        Enhancement enhance = world.enhancements.get(item.getEnhId());

        if (!item.requirements.isEmpty()) {
            world.users.kick(user);
            world.users.log(user, "Packet Edit [EnhanceItemShop]", "Trying to use an enhancement that can only be use locally.");
            return;
        }

        world.db.jdbc.beginTransaction();
        try {
            QueryResult result = world.db.jdbc.query("SELECT Gold, Coins FROM users WHERE id = ? FOR UPDATE", user.properties.get(Users.DATABASE_ID));

            if (result.next()) {
                int userGold = result.getInt("Gold");
                int userCoins = result.getInt("Coins");
                result.close();

                int cost = item.getCost();

                if (item.isCoins() && (cost <= userCoins)) {
                    int deltaCoins = (userCoins - item.getCost());
                    world.db.jdbc.run("UPDATE users SET Coins = ? WHERE id=?", deltaCoins, user.properties.get(Users.DATABASE_ID));
                } else if (cost <= userGold) {
                    int deltaGold = (userGold - item.getCost());
                    world.db.jdbc.run("UPDATE users SET Gold = ? WHERE id=?", deltaGold, user.properties.get(Users.DATABASE_ID));
                } else {
                    world.db.jdbc.rollbackTransaction();
                    world.users.log(user, "Packet Edit [EnhanceItemShop]", "Sent an enhancement request while lacking funds.");
                    return;
                }
            }
            result.close();

        } catch (JdbcException je) {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.rollbackTransaction();
            SmartFoxServer.log.severe("Error in enhance item transaction: " + je.getMessage());
        } finally {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.commitTransaction();
        }

        eis.put("EnhName", enhance.getName());
        eis.put("EnhPID", enhance.getPatternId());
        eis.put("EnhRng", item.getRange());
        eis.put("EnhRty", enhance.getRarity());
        eis.put("iCost", item.getCost());
        eis.put("bSuccess", 1);
        eis.put("EnhDPS", enhance.getDPS());
        eis.put("EnhLvl", enhance.getLevel());
        eis.put("EnhID", enhancementItemId);
        eis.put("cmd", "enhanceItemShop");
        eis.put("ItemID", userItemId);

        world.db.jdbc.run("UPDATE users_items SET EnhID = ? WHERE ItemID = ? AND UserID = ?", item.getEnhId(), userItemId, user.properties.get(Users.DATABASE_ID));
        world.send(eis, user);

        JSONObject eqp = (JSONObject) user.properties.get(Users.EQUIPMENT);
        for (Object obj : eqp.values()) {
            JSONObject equip = (JSONObject) obj;
            if (equip.getInt("ItemID") == userItemId) {
                world.users.sendStats(user);
                break;
            }
        }
        
        if (item.getEquipment().equals(Item.EQUIPMENT_CLASS) || item.getEquipment().equals(Item.EQUIPMENT_CAPE) || item.getEquipment().equals(Item.EQUIPMENT_HELM) || item.getEquipment().equals(Item.EQUIPMENT_WEAPON)) {
            world.users.updateStats(user, enhance, item.getEquipment());
            world.users.sendStats(user);
        }
    }

}

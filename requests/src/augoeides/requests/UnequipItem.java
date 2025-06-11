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
import augoeides.world.stats.Stats;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.Map;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class UnequipItem implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        int itemId = Integer.parseInt(params[0]);

        JSONObject uni = new JSONObject();

        Item item = world.items.get(itemId);
        String es = item.getEquipment();

        uni.put("cmd", "unequipItem");
        uni.put("ItemID", itemId);
        uni.put("uid", user.getUserId());
        uni.put("strES", es);

        if (item.getEquipment().equals(Item.EQUIPMENT_CLASS) || item.getEquipment().equals(Item.EQUIPMENT_CAPE) || item.getEquipment().equals(Item.EQUIPMENT_HELM) || item.getEquipment().equals(Item.EQUIPMENT_WEAPON)) {
            Stats stats = (Stats) user.properties.get(Users.STATS);
            if (item.getEquipment().equals(Item.EQUIPMENT_CLASS))
                for (Map.Entry<String, Double> entry : stats.armor.entrySet())
                    stats.armor.put(entry.getKey(), 0.0);
            if (item.getEquipment().equals(Item.EQUIPMENT_CAPE))
                for (Map.Entry<String, Double> entry : stats.cape.entrySet())
                    stats.cape.put(entry.getKey(), 0.0);
            if (item.getEquipment().equals(Item.EQUIPMENT_HELM))
                for (Map.Entry<String, Double> entry : stats.helm.entrySet())
                    stats.helm.put(entry.getKey(), 0.0);
            if (item.getEquipment().equals(Item.EQUIPMENT_WEAPON))
                for (Map.Entry<String, Double> entry : stats.weapon.entrySet())
                    stats.weapon.put(entry.getKey(), 0.0);
            world.users.sendStats(user);
        }

        JSONObject eqp = (JSONObject) user.properties.get(Users.EQUIPMENT);
        eqp.remove(es);

        world.sendToRoom(uni, user, room);

        world.db.jdbc.run("UPDATE users_items SET Equipped = 0 WHERE ItemID = ? AND UserID = ?", itemId, user.properties.get(Users.DATABASE_ID));
    }

}

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
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class EnhanceItemLocal implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        JSONObject eil = new JSONObject();

        int userItemId = Integer.parseInt(params[0]);
        int enhancementItemId = Integer.parseInt(params[1]);

        Item item = world.items.get(enhancementItemId);
        Enhancement enhance = world.enhancements.get(item.getEnhId());

        if (world.users.turnInItem(user, enhancementItemId, 1)) {
            eil.put("EnhName", enhance.getName());
            eil.put("EnhPID", enhance.getPatternId());
            eil.put("EnhRng", item.getRange());
            eil.put("EnhRty", enhance.getRarity());
            eil.put("iCost", item.getCost());
            eil.put("bSuccess", 1);
            eil.put("EnhDPS", enhance.getDPS());
            eil.put("EnhLvl", enhance.getLevel());
            eil.put("EnhID", enhancementItemId);
            eil.put("cmd", "enhanceItemLocal");
            eil.put("ItemID", userItemId);

            world.db.jdbc.run("UPDATE users_items SET EnhID = ? WHERE ItemID = ? AND UserID = ?", item.getEnhId(), userItemId, user.properties.get(Users.DATABASE_ID));
            world.send(eil, user);

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
        } else
            world.users.log(user, "Packet Edit [EnhanceItemLocal]", "Failed to pass turn in validation.");
    }

}

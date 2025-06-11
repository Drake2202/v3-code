/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests.guild;

import augoeides.db.objects.Enhancement;
import augoeides.db.objects.Item;
import augoeides.db.objects.Shop;
import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.Map;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class GetShop implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        int shopId = Integer.parseInt(params[1]);
        
        if (!world.shops.containsKey(shopId)) return;
        Shop shopObj = world.shops.get(shopId);

        JSONObject guildhall = new JSONObject();

        JSONObject shopinfo = new JSONObject();
        JSONArray items = new JSONArray();

        for (Map.Entry<Integer, Integer> entry : shopObj.items.entrySet()) {
            int shopItemId = entry.getKey();
            int itemId = entry.getValue();

            Item itemObj = world.items.get(itemId);

            if (itemObj != null) {
                Enhancement enhancement = world.enhancements.get(itemObj.getEnhId());

                JSONObject item;

                if (!(user.isAdmin() || user.isModerator()) && shopObj.isStaff()) {
                    item = Item.getItemJSON(itemObj, world.enhancements.get(1957));
                    item.put("ItemID", item.hashCode());
                    item.put("sName", itemObj.getName() + " Preview");
                    item.put("bStaff", 1);
                } else
                    item = Item.getItemJSON(itemObj, enhancement);

                if (shopObj.isLimited()) {
                    int quantityRemain = world.db.jdbc.queryForInt("SELECT QuantityRemain FROM shops_items WHERE id = ?", shopItemId);
                    item.put("iQtyRemain", quantityRemain);
                } else
                    item.put("iQtyRemain", -1);

                item.put("ShopItemID", shopItemId);

                item.put("iQty", itemObj.getQuantity());
                item.put("iQSindex", itemObj.getQuestStringIndex());
                item.put("iQSvalue", itemObj.getQuestStringValue());
                item.put("iReqCP", itemObj.getReqClassPoints());
                item.put("iReqRep", itemObj.getReqReputation());
                item.put("FactionID", itemObj.getFactionId());
                item.put("sFaction", world.factions.get(itemObj.getFactionId()));
                item.put("iCost", (itemObj.getCost() * itemObj.getQuantity()));

                if (itemObj.getReqClassId() > 0) {
                    item.put("iClass", itemObj.getReqClassId());
                    item.put("sClass", world.items.get(itemObj.getReqClassId()).getName());
                }

                if (!itemObj.requirements.isEmpty()) {
                    JSONArray turnInArr = new JSONArray();

                    for (Map.Entry<Integer, Integer> require : itemObj.requirements.entrySet()) {
                        int reqItemId = require.getKey();
                        Item reqItemObj = world.items.get(reqItemId);

                        int quantityNeeded = require.getValue() >= reqItemObj.getStack() ? reqItemObj.getStack() : require.getValue();

                        JSONObject wObj = new JSONObject();
                        wObj.put("ItemID", reqItemId);
                        wObj.put("iQty", quantityNeeded);
                        wObj.put("sName", reqItemObj.getName());
                        turnInArr.add(wObj);
                    }

                    item.put("turnin", turnInArr);
                }

                items.add(item);
            }
        }

        shopinfo.put("bHouse", shopObj.isHouse() ? 1 : 0);
        shopinfo.put("bStaff", shopObj.isStaff() ? 1 : 0);
        shopinfo.put("bUpgrd", shopObj.isUpgrade() ? 1 : 0);
        shopinfo.put("bLimited", shopObj.isLimited() ? 1 : 0);
        shopinfo.put("iIndex", "-1");
        shopinfo.put("items", items);
        shopinfo.put("ShopID", shopId);
        shopinfo.put("sField", shopObj.getField());
        shopinfo.put("sName", shopObj.getName());

        guildhall.put("shopinfo", shopinfo);
        guildhall.put("cmd", "guildhall");
        guildhall.put("gCmd", "guildShop");

        world.send(guildhall, user);
    }

}

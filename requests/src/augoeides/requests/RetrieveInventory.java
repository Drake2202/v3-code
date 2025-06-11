/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

import augoeides.config.ConfigData;
import augoeides.db.objects.Enhancement;
import augoeides.db.objects.EnhancementPattern;
import augoeides.db.objects.Item;
import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.Users;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.Date;
import java.util.Map;
import jdbchelper.QueryResult;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class RetrieveInventory implements IRequest {

    private JSONArray hitems = new JSONArray();
    private JSONArray items = new JSONArray();

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        retrieveInventory(user, world, room);
        sendEnhancementPatterns(user, world);
        loadInventoryBig(user, world);
        world.users.sendStats(user);
    }

    private void loadInventoryBig(User user, World world) {
        JSONObject lib = new JSONObject(); //load inventory big json

        lib.put("bankCount", world.users.getBankCount(user));
        lib.put("cmd", "loadInventoryBig");
        lib.put("items", this.items);
        lib.put("hitems", this.hitems);
        lib.put("factions", getFactions(user, world));

        if ((Integer) user.properties.get(Users.GUILD_ID) > 0)
            lib.put("guild", user.properties.get(Users.GUILD));

        world.send(lib, user);
        world.send(new String[]{"server", "Character load complete."}, user);

        //Update Friends
        JSONObject updateFriend = new JSONObject();
        JSONObject friendInfo = new JSONObject();

        updateFriend.put("cmd", "updateFriend");
        friendInfo.put("iLvl", (Integer) user.properties.get(Users.LEVEL));
        friendInfo.put("ID", user.properties.get(Users.DATABASE_ID));
        friendInfo.put("sName", user.properties.get(Users.USERNAME));
        friendInfo.put("sServer", ConfigData.SERVER_NAME);
        updateFriend.put("friend", friendInfo);

        QueryResult result = world.db.jdbc.query("SELECT Name FROM users LEFT JOIN users_friends ON FriendID = id WHERE UserID = ?", user.properties.get(Users.DATABASE_ID));
        while (result.next()) {
            User client = world.zone.getUserByName(result.getString("Name").toLowerCase());
            if (client != null) {
                world.send(updateFriend, client);
                world.send(new String[]{"server", user.getName() + " has logged in."}, client);
            }
        }
        result.close();

        world.db.jdbc.run("UPDATE users SET CurrentServer = ? WHERE id = ?", ConfigData.SERVER_NAME, user.properties.get(Users.DATABASE_ID));

        int guildId = (Integer) user.properties.get(Users.GUILD_ID);
        if (guildId > 0)
            world.sendGuildUpdate(world.users.getGuildObject(guildId));

        retrieveBoosts(user, world);
    }

    private JSONArray getFactions(User user, World world) {
        JSONArray factions = new JSONArray();
        QueryResult result = world.db.jdbc.query("SELECT * FROM users_factions WHERE UserID = ?", user.properties.get(Users.DATABASE_ID));
        while (result.next()) {
            JSONObject faction = new JSONObject();

            faction.put("FactionID", result.getString("FactionID"));
            faction.put("CharFactionID", result.getString("id"));
            faction.put("sName", world.factions.get(result.getInt("FactionID")));
            faction.put("iRep", result.getInt("Reputation"));

            factions.add(faction);
        }
        result.close();
        return factions;
    }

    private void sendEnhancementPatterns(User user, World world) {
        JSONObject enhp = new JSONObject();
        JSONObject o = new JSONObject();

        for (EnhancementPattern ep : world.patterns.values()) {
            JSONObject pattern = new JSONObject();
            Map<String, Integer> stats = ep.getStats();
            pattern.put("ID", String.valueOf(ep.getId()));
            pattern.put("sName", ep.getName());
            pattern.put("sDesc", ep.getDescription());
            pattern.put("iWIS", String.valueOf(stats.get("WIS")));
            pattern.put("iEND", String.valueOf(stats.get("END")));
            pattern.put("iLCK", String.valueOf(stats.get("LCK")));
            pattern.put("iSTR", String.valueOf(stats.get("STR")));
            pattern.put("iDEX", String.valueOf(stats.get("DEX")));
            pattern.put("iINT", String.valueOf(stats.get("INT")));

            o.put(String.valueOf(ep.getId()), pattern);
        }

        enhp.put("cmd", "enhp");
        enhp.put("o", o);

        world.send(enhp, user);
    }

    private void retrieveInventory(User user, World world, Room room) {
        JSONObject eqp = new JSONObject();
        this.items = new JSONArray();
        this.hitems = new JSONArray();

        QueryResult result = world.db.jdbc.query("SELECT * FROM users_items WHERE Bank = 0 AND UserID = ?", user.properties.get(Users.DATABASE_ID));
        while (result.next()) {
            int charItemId = result.getInt("id");
            int quantity = result.getInt("Quantity");
            int itemId = result.getInt("ItemID");
            int enhId = result.getInt("EnhID");

            boolean equipped = result.getBoolean("Equipped");

            Item itemObj = world.items.get(itemId);
            Enhancement enhancement = world.enhancements.get(enhId);

            JSONObject item = Item.getItemJSON(itemObj, enhancement);
            item.put("bBank", "0");
            item.put("CharItemID", charItemId);
            item.put("iQty", quantity);

            if (world.items.get(itemId).isCoins()) {
                Date startDate = result.getDate("DatePurchased");
                Date endDate = new Date();
                long diff = endDate.getTime() - startDate.getTime();
                long diffHours = diff / (60 * 60 * 1000);

                item.put("iHrs", diffHours);
                item.put("dPurchase", result.getString("DatePurchased").replaceAll(" ", "T"));
            }

            if (equipped) {
                item.put("bEquip", result.getString("Equipped"));
                JSONObject eqpObj = new JSONObject();

                eqpObj.put("ItemID", itemId);
                eqpObj.put("sFile", itemObj.getFile());
                eqpObj.put("sLink", itemObj.getLink());

                JSONObject ei = new JSONObject();
                ei.put("uid", user.getUserId());
                ei.put("cmd", "equipItem");
                ei.put("ItemID", itemId);
                ei.put("strES", itemObj.getEquipment());
                ei.put("sFile", itemObj.getFile());
                ei.put("sLink", itemObj.getLink());
                ei.put("sMeta", itemObj.getMeta());

                if (itemObj.getEquipment().equals(Item.EQUIPMENT_WEAPON)) {
                    eqpObj.put("sType", itemObj.getType());
                    ei.put("sType", itemObj.getType());

                    user.properties.put(Users.ITEM_WEAPON, itemObj);
                    if (enhancement != null)
                        user.properties.put(Users.ITEM_WEAPON_ENHANCEMENT, enhancement);
                }

                if (!eqp.has(itemObj.getEquipment())) {
                    world.sendToRoom(ei, user, room);

                    if (itemObj.getEquipment().equals(Item.EQUIPMENT_CLASS) || itemObj.getEquipment().equals(Item.EQUIPMENT_CAPE) || itemObj.getEquipment().equals(Item.EQUIPMENT_HELM) || itemObj.getEquipment().equals(Item.EQUIPMENT_WEAPON)) {
                        if (itemObj.getEquipment().equals(Item.EQUIPMENT_CLASS))
                            world.users.updateClass(user, itemObj, quantity);

                        world.users.updateStats(user, enhancement, itemObj.getEquipment());
                    }

                    eqp.put(itemObj.getEquipment(), eqpObj);
                }
            }

            if (itemObj.getEquipment().equals(Item.EQUIPMENT_HOUSE) || itemObj.getEquipment().equals(Item.EQUIPMENT_HOUSE_ITEM))
                this.hitems.add(item);
            else
                this.items.add(item);
        }
        result.close();

        user.properties.put(Users.EQUIPMENT, eqp);
    }

    private void retrieveBoosts(User user, World world) {
        //Check for boosts
        QueryResult boosts = world.db.jdbc.query("SELECT ExpBoostExpire, CpBoostExpire, GoldBoostExpire, RepBoostExpire FROM users WHERE id = ?", user.properties.get(Users.DATABASE_ID));
        if (boosts.next()) {
            JSONObject boost = new JSONObject();
            boost.put("bShowShop", "undefined");
            boost.put("op", "+");

            int xpSecsLeft = world.db.jdbc.queryForInt("SELECT TIMESTAMPDIFF(SECOND, NOW(), ?)", boosts.getString("ExpBoostExpire"));
            if (xpSecsLeft > 0) {
                boost.put("cmd", "xpboost");
                boost.put("iSecsLeft", xpSecsLeft);
                world.send(boost, user);

                user.properties.put(Users.BOOST_XP, true);
            }

            int cpSecsLeft = world.db.jdbc.queryForInt("SELECT TIMESTAMPDIFF(SECOND,NOW(),?)", boosts.getString("CpBoostExpire"));
            if (cpSecsLeft > 0) {
                boost.put("cmd", "cpboost");
                boost.put("iSecsLeft", cpSecsLeft);
                world.send(boost, user);

                user.properties.put(Users.BOOST_CP, true);
            }

            int repSecsLeft = world.db.jdbc.queryForInt("SELECT TIMESTAMPDIFF(SECOND,NOW(),?)", boosts.getString("RepBoostExpire"));
            if (repSecsLeft > 0) {
                boost.put("cmd", "repboost");
                boost.put("iSecsLeft", repSecsLeft);
                world.send(boost, user);

                user.properties.put(Users.BOOST_REP, true);
            }

            int goldSecsLeft = world.db.jdbc.queryForInt("SELECT TIMESTAMPDIFF(SECOND,NOW(),?)", boosts.getString("GoldBoostExpire"));
            if (goldSecsLeft > 0) {
                boost.put("cmd", "gboost");
                boost.put("iSecsLeft", goldSecsLeft);
                world.send(boost, user);

                user.properties.put(Users.BOOST_GOLD, true);
            }
        }
        boosts.close();
    }
}

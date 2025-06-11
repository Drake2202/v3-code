/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

import augoeides.db.objects.Item;
import augoeides.db.objects.Quest;
import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.Users;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import jdbchelper.JdbcException;
import jdbchelper.QueryResult;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class TryQuestComplete implements IRequest {

    private static final Random rand;
    private static final List<Integer> doom;
    private static final List<Integer> destiny;
    private static final int boost = 19189;
    private static final int potion = 18927;

    static {
        rand = new Random();
        doom = Arrays.asList(3073, 3074, 3075, 3076);
        destiny = Arrays.asList(3128, 3129, 3130, 3131);
    }

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        int questId = Integer.parseInt(params[0]);
        int itemId = Integer.parseInt(params[1]);

        Set<Integer> userQuests = (Set<Integer>) user.properties.get(Users.QUESTS);
        Quest quest = world.quests.get(questId);

        if (!quest.locations.isEmpty()) {
            int mapId = world.areas.get(room.getName().split("-")[0]).getId();
            if (!quest.locations.contains(mapId)) {
                world.users.log(user, "Invalid Quest Complete", "Quest complete triggered at different location.");
                return;
            }
        }

        JSONObject ccqr = new JSONObject();

        ccqr.put("cmd", "ccqr");
        ccqr.put("QuestID", questId);

        if (userQuests.contains(questId) || doom.contains(questId) || destiny.contains(questId)) {
            if (!quest.getField().isEmpty())
                if (world.users.getAchievement(quest.getField(), quest.getIndex(), user) != 0) {
                    ccqr.put("bSuccess", 0);
                    world.send(ccqr, user);
                    world.users.log(user, "Packet Edit [TryQuestComplete]", "Failed to pass achievement validation while attempting to complete quest: " + quest.getName());
                    return;
                }

            if (world.users.turnInItems(user, quest.requirements)) {
                if (doom.contains(questId))
                    doWheel(user, world, "doom", quest.rewards);
                else if (destiny.contains(questId))
                    doWheel(user, world, "destiny", quest.rewards);
                else if (quest.getRewardType().equals("C"))
                    if (quest.rewards.containsKey(itemId))
                        world.users.dropItem(user, itemId);
                    else {
                        //INSTA BAN
                        world.db.jdbc.run("UPDATE users SET Access = 0, PermamuteFlag = 0 WHERE id = ?", user.properties.get(Users.DATABASE_ID));
                        world.users.kick(user);
                        world.users.log(user, "Packet Edit [TryQuestComplete]", "Banned for item id exploit.");
                    }
                else if (quest.getRewardType().equals("R") || quest.getRewardType().equals("rand")) {
                    Integer[] setArray = quest.rewards.keySet().toArray(new Integer[quest.rewards.size()]);
                    int randKey = setArray[rand.nextInt(setArray.length)];
                    world.users.dropItem(user, randKey, quest.rewards.get(randKey));
                } else if (quest.getRewardType().equals("S"))
                    for (Map.Entry<Integer, Integer> entry : quest.rewards.entrySet())
                        world.users.dropItem(user, entry.getKey(), entry.getValue());

                world.users.giveRewards(user, quest.getExperience(), quest.getGold(), quest.getClassPoints(), quest.getReputation(), quest.getFactionId(), user.getUserId(), "p");

                JSONObject rewardObj = new JSONObject();
                rewardObj.put("intGold", quest.getGold());
                rewardObj.put("intExp", quest.getExperience());
                rewardObj.put("iCP", quest.getClassPoints());

                if (quest.getFactionId() > 0)
                    rewardObj.put("iRep", quest.getReputation());

                ccqr.put("rewardObj", rewardObj);
                ccqr.put("sName", quest.getName());

                if (quest.getSlot() > 0)
                    world.users.setQuestValue(user, quest.getSlot(), quest.getValue());

                if (!quest.getField().isEmpty())
                    world.users.setAchievement(quest.getField(), quest.getIndex(), 1, user);

                userQuests.remove(Integer.valueOf(questId));
            } else {
                ccqr.put("bSuccess", 0);
                world.users.log(user, "Packet Edit [TryQuestComplete]", "Failed to pass turn in validation while attempting to complete quest: " + quest.getName());
            }
        } else {
            ccqr.put("bSuccess", 0);
            world.users.log(user, "Packet Edit [TryQuestComplete]", "Attempted to complete an unaccepted quest: " + quest.getName());
        }

        world.send(ccqr, user);
    }

    private void doWheel(User user, World world, String wheelType, Map<Integer, Integer> rewards) throws RequestException {
        JSONObject wheel = new JSONObject();

        Integer[] setArray = rewards.keySet().toArray(new Integer[rewards.size()]);
        int itemId = setArray[rand.nextInt(setArray.length)];
        int quantity = rewards.get(itemId);
        Item item = world.items.get(itemId);
        int rarity = item.getRarity();

        int charItemId = -1;
        int quantity1 = 0;
        int quantity2 = 0;
        int charItemId1 = -1;
        int charItemId2 = -1;

        world.db.jdbc.beginTransaction();
        try {
            QueryResult result = world.db.jdbc.query("SELECT * FROM users_items WHERE ItemID = ? AND UserID = ? FOR UPDATE", itemId, user.properties.get(Users.DATABASE_ID));
            result.setAutoClose(true);
            if (!result.next()) {
                world.db.jdbc.run("INSERT INTO users_items (UserID, ItemID, EnhID, Equipped, Quantity, Bank, DatePurchased) VALUES (?, ?, ?, 0, ?, 0, NOW())", user.properties.get(Users.DATABASE_ID), itemId, item.getEnhId(), item.getQuantity());
                charItemId = Long.valueOf(world.db.jdbc.getLastInsertId()).intValue();
            }
            result.close();

            QueryResult boostResult = world.db.jdbc.query("SELECT id, Quantity FROM users_items WHERE ItemID = ? AND UserID = ? FOR UPDATE", boost, user.properties.get(Users.DATABASE_ID));
            if (boostResult.next()) {
                charItemId1 = boostResult.getInt("id");
                quantity1 = boostResult.getInt("Quantity");
                world.db.jdbc.run("UPDATE users_items SET Quantity = (Quantity + 1) WHERE id = ?", charItemId1);
            } else {
                world.db.jdbc.run("INSERT INTO users_items (UserID, ItemID, EnhID, Equipped, Quantity, Bank, DatePurchased) VALUES (?, ?, ?, 0, ?, 0, NOW())", user.properties.get(Users.DATABASE_ID), boost, 0, 1);
                charItemId1 = Long.valueOf(world.db.jdbc.getLastInsertId()).intValue();
                quantity1 = 1;
            }
            boostResult.close();

            QueryResult potionResult = world.db.jdbc.query("SELECT id, Quantity FROM users_items WHERE ItemID = ? AND UserID = ? FOR UPDATE", potion, user.properties.get(Users.DATABASE_ID));
            if (potionResult.next()) {
                charItemId2 = potionResult.getInt("id");
                quantity2 = potionResult.getInt("Quantity");
                world.db.jdbc.run("UPDATE users_items SET Quantity = (Quantity + 1) WHERE id = ?", charItemId2);
            } else {
                world.db.jdbc.run("INSERT INTO users_items (UserID, ItemID, EnhID, Equipped, Quantity, Bank, DatePurchased) VALUES (?, ?, ?, 0, ?, 0, NOW())", user.properties.get(Users.DATABASE_ID), potion, 0, 1);
                charItemId2 = Long.valueOf(world.db.jdbc.getLastInsertId()).intValue();
                quantity2 = 1;
            }
            potionResult.close();
        } catch (JdbcException je) {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.rollbackTransaction();
            SmartFoxServer.log.severe("Error in wheel transaction: " + je.getMessage());
        } finally {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.commitTransaction();
        }

        JSONObject itemJSON = Item.getItemJSON(item);
        itemJSON.put("iQty", quantity);

        wheel.put("cmd", "Wheel");

        if (charItemId > 0) {
            wheel.put("Item", itemJSON);
            world.send(new String[]{"wheel", "You won " + item.getName()}, user);

            if (rarity >= 30)
                world.sendToUsers(new String[]{"wheel", "Player <font color=\"#ffffff\">" + user.properties.get(Users.USERNAME) + "</font> has received " + item.getName() + " from the wheel of " + wheelType});
        } else
            world.send(new String[]{"wheel", "You have already won '" + item.getName() + "' before. Try your luck next time."}, user);

        JSONObject dropItems = new JSONObject();
        dropItems.put(String.valueOf(boost), Item.getItemJSON(world.items.get(boost)));
        dropItems.put(String.valueOf(potion), Item.getItemJSON(world.items.get(potion)));

        wheel.put("dropItems", dropItems);
        wheel.put("CharItemID", charItemId);
        wheel.put("charItem1", charItemId1);
        wheel.put("charItem2", charItemId2);
        wheel.put("iQty1", quantity1);
        wheel.put("iQty2", quantity2);

        world.send(wheel, user);
    }
}

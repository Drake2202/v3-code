/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

import augoeides.db.objects.Item;
import augoeides.db.objects.Quest;
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
public class GetQuests implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        JSONObject q = new JSONObject();
        JSONObject arrQuests = new JSONObject();

        for (String strId : params) {
            int questId = Integer.parseInt(strId);
            Quest questObj = world.quests.get(questId);

            if (questObj != null) {
                if (!questObj.locations.isEmpty()) {
                    int mapId = world.areas.get(room.getName().split("-")[0]).getId();
                    if (!questObj.locations.contains(mapId)) {
                        world.users.log(user, "Invalid Quest Load", "Quest load triggered at different location.");
                        continue;
                    }
                }

                JSONObject quest = new JSONObject();
                JSONObject oRewards = new JSONObject();
                JSONObject rewArray = new JSONObject();
                JSONArray rewards = new JSONArray();

                if (questObj.rewards.size() > 0) {
                    for (Map.Entry<Integer, Integer> entry : questObj.rewards.entrySet()) {
                        int itemId = entry.getKey();
                        int quantity = entry.getValue();
                        Item itemObj = world.items.get(itemId);
                        if (itemObj != null) {
                            JSONObject item = Item.getItemJSON(itemObj);

                            rewArray.put(String.valueOf(itemId), item);

                            JSONObject rewardObj = new JSONObject();
                            rewardObj.put("ItemID", itemId);
                            rewardObj.put("QuestID", questId);
                            rewardObj.put("iRate", 100);
                            rewardObj.put("iType", 0);
                            rewardObj.put("iQty", quantity);
                            rewards.add(rewardObj);
                        }
                    }

                    oRewards.put("items" + questObj.getRewardType(), rewArray);
                }

                JSONArray turnin = new JSONArray();
                JSONObject oItems = new JSONObject();

                for (Map.Entry<Integer, Integer> entry : questObj.requirements.entrySet()) {
                    int itemId = entry.getKey();
                    int quantity = entry.getValue();
                    Item itemObj = world.items.get(itemId);
                    if (itemObj != null) {
                        JSONObject item = Item.getItemJSON(itemObj);

                        oItems.put(itemId, item);

                        JSONObject turnInObj = new JSONObject();
                        turnInObj.put("ItemID", String.valueOf(itemId));
                        turnInObj.put("QuestID", questId);
                        turnInObj.put("iQty", quantity);

                        turnin.add(turnInObj);
                    }
                }

                quest.put("FactionID", questObj.getFactionId());

                if (questObj.getFactionId() > 1)
                    quest.put("sFaction", world.factions.get(questObj.getFactionId()));

                quest.put("QuestID", questId);
                quest.put("bOnce", questObj.isOnce() ? 1 : 0);
                quest.put("bStaff", 0);
                quest.put("bUpg", questObj.isUpgrade() ? 1 : 0);

                if (questObj.getReqClassId() > 0)
                    quest.put("sClass", world.items.get(questObj.getReqClassId()).getName());
                quest.put("iClass", questObj.getReqClassId());
                quest.put("iExp", questObj.getExperience());
                quest.put("iGold", questObj.getGold());
                quest.put("iLvl", questObj.getLevel());
                quest.put("iRep", questObj.getReputation());
                quest.put("iReqCP", questObj.getReqClassPoints());
                quest.put("iReqRep", questObj.getReqReputation());
                quest.put("iSlot", questObj.getSlot());
                quest.put("iValue", questObj.getValue());
                quest.put("iWar", 0);
                quest.put("oItems", oItems);
                quest.put("oRewards", oRewards);
                quest.put("reward", rewards);
                quest.put("sDesc", questObj.getDescription());
                quest.put("sEndText", questObj.getEndText());
                quest.put("sName", questObj.getName());
                quest.put("turnin", turnin);

                if (!questObj.getField().isEmpty()) {
                    quest.put("sField", questObj.getField());
                    quest.put("iIndex", questObj.getIndex());
                }

                arrQuests.put(strId, quest);
            }
        }

        q.put("cmd", "getQuests");
        q.put("quests", arrQuests);

        world.send(q, user);
    }

}

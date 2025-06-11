/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.db.objects;

import java.util.HashSet;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class House extends Area {

    private JSONArray houseItems;
    private String ownerName, houseInfo;
    private int ownerId;

    public House(JSONArray houseItems, String houseInfo, String ownerName, int ownerId) {
        super();

        this.houseItems = houseItems;
        this.houseInfo = houseInfo;
        this.ownerName = ownerName;
        this.ownerId = ownerId;
        this.monsters = new HashSet<MapMonster>();
    }

    public JSONObject getData() {
        JSONObject house = new JSONObject();
        house.put("strMapFileName", this.file);
        house.put("strMapName", "house");
        house.put("sHouseInfo", this.houseInfo);
        house.put("items", this.houseItems);
        house.put("roomName", "house-" + Integer.toString(this.ownerId));
        house.put("unm", this.ownerName);
        house.put("CharID", this.ownerId);

        return house;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void setHouseInfo(String houseInfo) {
        this.houseInfo = houseInfo;
    }

    public void setHouseItems(JSONArray houseItems) {
        this.houseItems = houseItems;
    }

    @Override
    public int getReqLevel() {
        return 0;
    }

    @Override
    public boolean isStaff() {
        return false;
    }

    @Override
    public boolean isUpgrade() {
        return false;
    }

    @Override
    public boolean isPvP() {
        return false;
    }

    @Override
    public int getMaxPlayers() {
        return 100000;
    }

}

/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.db.objects;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jdbchelper.BeanCreator;
import jdbchelper.ResultSetMapper;

/**
 *
 * @author Mystical
 */
public class Quest {

    private int id, factionId, reqReputation, reqClassId, reqClassPoints, experience, gold, reputation, classPoints, level,
            slot, value, index;
    private String name, description, endText, rewardType, field;
    private boolean upgrade, once;

    public Map<Integer, Integer> rewards;
    public Map<Integer, Integer> requirements;
    public Set<Integer> locations;

    public static final BeanCreator<Set<Integer>> beanLocations = new BeanCreator<Set<Integer>>() {

        @Override
        public Set<Integer> createBean(ResultSet rs) throws SQLException {
            Set<Integer> set = new HashSet<Integer>();

            set.add(rs.getInt("MapID"));

            while (rs.next())
                set.add(rs.getInt("MapID"));

            return set;
        }
    };

    public static final ResultSetMapper<Integer, Integer> requirementsRewardsMapper = new ResultSetMapper<Integer, Integer>() {

        @Override
        public AbstractMap.SimpleEntry<Integer, Integer> mapRow(ResultSet rs) throws SQLException {
            return new AbstractMap.SimpleEntry<Integer, Integer>(rs.getInt("ItemID"), rs.getInt("Quantity"));
        }
    };
    public static final ResultSetMapper<Integer, Quest> resultSetMapper = new ResultSetMapper<Integer, Quest>() {

        @Override
        public AbstractMap.SimpleEntry<Integer, Quest> mapRow(ResultSet rs) throws SQLException {
            Quest quest = new Quest();

            quest.id = rs.getInt("id");
            quest.factionId = rs.getInt("FactionID");
            quest.reqReputation = rs.getInt("ReqReputation");
            quest.reqClassId = rs.getInt("ReqClassID");
            quest.reqClassPoints = rs.getInt("ReqClassPoints");
            quest.experience = rs.getInt("Experience");
            quest.gold = rs.getInt("Gold");
            quest.reputation = rs.getInt("Reputation");
            quest.classPoints = rs.getInt("ClassPoints");
            quest.level = rs.getInt("Level");
            quest.slot = rs.getInt("Slot");
            quest.value = rs.getInt("Value");
            quest.index = rs.getInt("Index");

            quest.name = rs.getString("Name");
            quest.description = rs.getString("Description");
            quest.endText = rs.getString("EndText");
            quest.rewardType = rs.getString("RewardType");
            quest.field = rs.getString("Field");

            quest.once = rs.getBoolean("Once");
            quest.upgrade = rs.getBoolean("Upgrade");

            return new AbstractMap.SimpleEntry<Integer, Quest>(quest.getId(), quest);
        }
    };

    public int getId() {
        return id;
    }

    public int getFactionId() {
        return factionId;
    }

    public int getReqReputation() {
        return reqReputation;
    }

    public int getReqClassId() {
        return reqClassId;
    }

    public int getReqClassPoints() {
        return reqClassPoints;
    }

    public int getExperience() {
        return experience;
    }

    public int getGold() {
        return gold;
    }

    public int getReputation() {
        return reputation;
    }

    public int getClassPoints() {
        return classPoints;
    }

    public int getLevel() {
        return level;
    }

    public int getSlot() {
        return slot;
    }

    public int getValue() {
        return value;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getEndText() {
        return endText;
    }

    public String getRewardType() {
        return rewardType;
    }

    public String getField() {
        return field;
    }

    public boolean isUpgrade() {
        return upgrade;
    }

    public boolean isOnce() {
        return once;
    }
}

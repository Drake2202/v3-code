/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.db.objects;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import jdbchelper.ResultSetMapper;

/**
 *
 * @author Mystical
 */
public class Enhancement {

    private String name;
    private int id, patternId, rarity, DPS, level;

    public static final ResultSetMapper<Integer, Enhancement> resultSetMapper = new ResultSetMapper<Integer, Enhancement>() {

        @Override
        public AbstractMap.SimpleEntry<Integer, Enhancement> mapRow(ResultSet rs) throws SQLException {
            Enhancement enhancement = new Enhancement();

            enhancement.id = rs.getInt("id");
            enhancement.name = rs.getString("Name");
            enhancement.patternId = rs.getInt("PatternID");
            enhancement.rarity = rs.getInt("Rarity");
            enhancement.DPS = rs.getInt("DPS");
            enhancement.level = rs.getInt("Level");

            return new AbstractMap.SimpleEntry<Integer, Enhancement>(enhancement.id, enhancement);
        }
    };

    public String getName() {
        return name;
    }

    public int getPatternId() {
        return patternId;
    }

    public int getRarity() {
        return rarity;
    }

    public int getDPS() {
        return DPS;
    }

    public int getLevel() {
        return level;
    }

    public int getId() {
        return id;
    }
}

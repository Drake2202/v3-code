/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.db.objects;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import jdbchelper.ResultSetMapper;

/**
 *
 * @author Mystical
 */
public class EnhancementPattern {

    private int id;
    private String name, description;
    private Map<String, Integer> stats;

    public static final ResultSetMapper<Integer, EnhancementPattern> resultSetMapper = new ResultSetMapper<Integer, EnhancementPattern>() {

        @Override
        public AbstractMap.SimpleEntry<Integer, EnhancementPattern> mapRow(ResultSet rs) throws SQLException {
            EnhancementPattern ep = new EnhancementPattern();

            ep.id = rs.getInt("id");
            ep.name = rs.getString("Name");
            ep.description = rs.getString("Desc");

            ep.stats = new HashMap<String, Integer>();

            ep.stats.put("WIS", rs.getInt("Wisdom"));
            ep.stats.put("END", rs.getInt("Endurance"));
            ep.stats.put("LCK", rs.getInt("Luck"));
            ep.stats.put("STR", rs.getInt("Strength"));
            ep.stats.put("DEX", rs.getInt("Dexterity"));
            ep.stats.put("INT", rs.getInt("Intelligence"));

            return new AbstractMap.SimpleEntry<Integer, EnhancementPattern>(ep.getId(), ep);
        }
    };

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Integer> getStats() {
        return Collections.unmodifiableMap(stats);
    }
}

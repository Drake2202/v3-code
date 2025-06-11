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
public class AuraEffects {

    private int id;
    private String stat, type;
    private double value;

    public static final ResultSetMapper<Integer, AuraEffects> resultSetMapper = new ResultSetMapper<Integer, AuraEffects>() {

        @Override
        public AbstractMap.SimpleEntry<Integer, AuraEffects> mapRow(ResultSet rs) throws SQLException {
            AuraEffects ae = new AuraEffects();

            ae.id = rs.getInt("id");
            ae.stat = rs.getString("Stat");
            ae.type = rs.getString("Type");
            ae.value = rs.getDouble("Value");

            return new AbstractMap.SimpleEntry<Integer, AuraEffects>(ae.id, ae);
        }
    };

    public int getId() {
        return id;
    }

    public String getStat() {
        return stat;
    }

    public String getType() {
        return type;
    }

    public double getValue() {
        return value;
    }
}

/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.db.objects;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jdbchelper.BeanCreator;
import jdbchelper.ResultSetMapper;

/**
 *
 * @author Mystical
 */
public class Area {

    protected String name, file;
    protected int id, maxPlayers, reqLevel;
    private boolean upgrade, staff, PvP;
    public Set<MapMonster> monsters = Collections.EMPTY_SET;
    public Map<Integer, Cell> cells = Collections.EMPTY_MAP;
    public Set<Integer> items = Collections.EMPTY_SET;

    public static final BeanCreator<Set<Integer>> beanItems = new BeanCreator<Set<Integer>>() {

        @Override
        public Set<Integer> createBean(ResultSet rs) throws SQLException {
            Set<Integer> set = new HashSet<Integer>();

            set.add(rs.getInt("ItemID"));

            while (rs.next())
                set.add(rs.getInt("ItemID"));

            return set;
        }
    };

    public static final ResultSetMapper<String, Area> resultSetMapper = new ResultSetMapper<String, Area>() {

        @Override
        public AbstractMap.SimpleEntry<String, Area> mapRow(ResultSet rs) throws SQLException {
            Area area = new Area();

            area.name = rs.getString("Name").toLowerCase();
            area.file = rs.getString("File");

            area.id = rs.getInt("id");
            area.maxPlayers = rs.getInt("MaxPlayers");
            area.reqLevel = rs.getInt("ReqLevel");

            area.upgrade = rs.getBoolean("Upgrade");
            area.staff = rs.getBoolean("Staff");
            area.PvP = rs.getBoolean("PvP");

            return new AbstractMap.SimpleEntry<String, Area>(area.getName(), area);
        }
    };

    public String getName() {
        return name;
    }

    public String getFile() {
        return file;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getReqLevel() {
        return reqLevel;
    }

    public boolean isUpgrade() {
        return upgrade;
    }

    public boolean isStaff() {
        return staff;
    }

    public int getId() {
        return id;
    }

    public boolean isPvP() {
        return PvP;
    }
}

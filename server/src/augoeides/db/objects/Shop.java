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
public class Shop {

    private int id;
    private String name, field;
    private boolean house, upgrade, staff, limited;
    public Map<Integer, Integer> items;
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

    public static final ResultSetMapper<Integer, Integer> shopItemsMapper = new ResultSetMapper<Integer, Integer>() {

        @Override
        public AbstractMap.SimpleEntry<Integer, Integer> mapRow(ResultSet rs) throws SQLException {
            return new AbstractMap.SimpleEntry<Integer, Integer>(rs.getInt("id"), rs.getInt("ItemID"));
        }
    };

    public static final ResultSetMapper<Integer, Shop> resultSetMapper = new ResultSetMapper<Integer, Shop>() {

        @Override
        public AbstractMap.SimpleEntry<Integer, Shop> mapRow(ResultSet rs) throws SQLException {
            Shop shop = new Shop();

            shop.id = rs.getInt("id");

            shop.name = rs.getString("Name");
            shop.field = rs.getString("Field");

            shop.house = rs.getBoolean("House");
            shop.upgrade = rs.getBoolean("Upgrade");
            shop.staff = rs.getBoolean("Staff");
            shop.limited = rs.getBoolean("Limited");

            return new AbstractMap.SimpleEntry<Integer, Shop>(shop.getId(), shop);
        }
    };

    public String getName() {
        return name;
    }

    public String getField() {
        return field;
    }

    public boolean isHouse() {
        return house;
    }

    public boolean isUpgrade() {
        return upgrade;
    }

    public boolean isStaff() {
        return staff;
    }

    public boolean isLimited() {
        return limited;
    }

    public int getId() {
        return id;
    }

}

/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.db.objects;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import jdbchelper.BeanCreator;

/**
 *
 * @author Mystical
 */
public class Class {

    private String category, description, manaRegenerationMethods, statsDescription;
    public Set<Integer> skills;

    public static final BeanCreator<Set<Integer>> beanSkills = new BeanCreator<Set<Integer>>() {
        @Override
        public Set<Integer> createBean(ResultSet rs) throws SQLException {
            Set<Integer> set = new HashSet<Integer>();

            set.add(rs.getInt("id"));

            while (rs.next())
                set.add(rs.getInt("id"));

            return set;
        }
    };

    public static final BeanCreator<Class> beanCreator = new BeanCreator<Class>() {

        @Override
        public Class createBean(ResultSet rs) throws SQLException {
            Class oClass = new Class();

            oClass.category = rs.getString("Category");
            oClass.description = rs.getString("Description");
            oClass.manaRegenerationMethods = rs.getString("ManaRegenerationMethods");
            oClass.statsDescription = rs.getString("StatsDescription");

            return oClass;
        }
    };

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getManaRegenerationMethods() {
        return manaRegenerationMethods;
    }

    public String getStatsDescription() {
        return statsDescription;
    }
}

/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.db.objects;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Set;
import jdbchelper.BeanCreator;
import jdbchelper.ResultSetMapper;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class Aura {

    private int id, duration;
    private String name, category;
    private double damageIncrease, damageTakenDecrease;

    public Set<Integer> effects;

    public static final BeanCreator<Set<Integer>> beanEffects = new BeanCreator<Set<Integer>>() {

        @Override
        public Set<Integer> createBean(ResultSet rs) throws SQLException {
            Set<Integer> set = new HashSet<Integer>();
            set.add(rs.getInt("id"));

            while (rs.next())
                set.add(rs.getInt("id"));

            return set;
        }
    };

    public static final ResultSetMapper<Integer, Aura> resultSetMapper = new ResultSetMapper<Integer, Aura>() {

        @Override
        public AbstractMap.SimpleEntry<Integer, Aura> mapRow(ResultSet rs) throws SQLException {
            Aura aura = new Aura();

            aura.id = rs.getInt("id");
            aura.duration = rs.getInt("Duration");

            aura.name = rs.getString("Name");
            aura.category = rs.getString("Category");

            aura.damageIncrease = rs.getDouble("DamageIncrease");
            aura.damageTakenDecrease = rs.getDouble("DamageTakenDecrease");

            return new AbstractMap.SimpleEntry<Integer, Aura>(aura.id, aura);
        }
    };

    public JSONArray getAuraArray(boolean isNew) {
        JSONArray auras = new JSONArray();
        JSONObject auraInfo = new JSONObject();

        if (!this.getCategory().isEmpty() && !this.getCategory().equals("d")) {
            auraInfo.put("cat", this.getCategory());
            if (this.getCategory().equals("stun"))
                auraInfo.put("s", "s");
        }

        auraInfo.put("nam", this.getName());
        auraInfo.put("t", "s");
        auraInfo.put("dur", String.valueOf(this.getDuration()));
        auraInfo.put("isNew", isNew);

        auras.add(auraInfo);

        return auras;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getDamageIncrease() {
        return damageIncrease;
    }

    public double getDamageTakenDecrease() {
        return damageTakenDecrease;
    }

    public int getDuration() {
        return duration;
    }
}

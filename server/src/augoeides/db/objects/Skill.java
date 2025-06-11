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
public class Skill {

    private String name, animation, description, icon, dsrc, reference, target, effects, type, strl;
    private double damage;
    private int id, mana, range, hitTargets, cooldown, auraId;

    public static final ResultSetMapper<Integer, Skill> resultSetMapper = new ResultSetMapper<Integer, Skill>() {

        @Override
        public AbstractMap.SimpleEntry<Integer, Skill> mapRow(ResultSet rs) throws SQLException {
            Skill skill = new Skill();

            skill.id = rs.getInt("id");

            skill.name = rs.getString("Name");
            skill.animation = rs.getString("Animation");
            skill.description = rs.getString("Description");
            skill.icon = rs.getString("Icon");
            skill.dsrc = rs.getString("Dsrc");
            skill.reference = rs.getString("Reference");
            skill.target = rs.getString("Target");
            skill.effects = rs.getString("Effects");
            skill.type = rs.getString("Type");
            skill.strl = rs.getString("Strl");

            skill.damage = rs.getDouble("Damage");

            skill.mana = rs.getInt("Mana");
            skill.range = rs.getInt("Range");
            skill.hitTargets = rs.getInt("HitTargets");
            skill.cooldown = rs.getInt("Cooldown");
            skill.auraId = rs.getInt("AuraID");

            return new AbstractMap.SimpleEntry<Integer, Skill>(skill.id, skill);
        }
    };

    public boolean hasAuraId() {
        return auraId > 0;
    }

    public String getName() {
        return name;
    }

    public String getAnimation() {
        return animation;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    public String getDsrc() {
        return dsrc;
    }

    public String getReference() {
        return reference;
    }

    public String getTarget() {
        return target;
    }

    public String getEffects() {
        return effects;
    }

    public String getType() {
        return type;
    }

    public String getStrl() {
        return strl;
    }

    public double getDamage() {
        return damage;
    }

    public int getMana() {
        return mana;
    }

    public int getRange() {
        return range;
    }

    public int getHitTargets() {
        return hitTargets;
    }

    public int getCooldown() {
        return cooldown;
    }

    public int getId() {
        return id;
    }

    public int getAuraId() {
        return auraId;
    }
}

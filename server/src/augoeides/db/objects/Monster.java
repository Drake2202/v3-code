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

/**
 *
 * @author Mystical
 */
public class Monster {

    private String name, race, file, linkage, element;
    private int id, health, mana, level, gold, experience, reputation, DPS, teamId, minDmg, maxDmg;
    
    public Set<MonsterDrop> drops;
    public Set<Integer> skills;
    
    public static final BeanCreator<Set<Integer>> beanSkills = new BeanCreator<Set<Integer>>() {

        @Override
        public Set<Integer> createBean(ResultSet rs) throws SQLException {
            Set<Integer> skills = new HashSet<Integer>();
            
            skills.add(rs.getInt("SkillID"));
            
            while(rs.next()) skills.add(rs.getInt("SkillID"));
            
            return skills;
        }
        
    };

    public static final BeanCreator<Set<MonsterDrop>> beanDrops = new BeanCreator<Set<MonsterDrop>>() {

        @Override
        public Set<MonsterDrop> createBean(ResultSet rs) throws SQLException {
            Set<MonsterDrop> drops = new HashSet<MonsterDrop>();

            MonsterDrop md = new MonsterDrop();

            md.itemId = rs.getInt("ItemID");
            md.quantity = rs.getInt("Quantity");
            md.chance = rs.getDouble("Chance");

            drops.add(md);

            while (rs.next()) {
                MonsterDrop md2 = new MonsterDrop();

                md2.itemId = rs.getInt("ItemID");
                md2.quantity = rs.getInt("Quantity");
                md2.chance = rs.getDouble("Chance");

                drops.add(md2);
            }

            return drops;
        }
    };

    public static final ResultSetMapper<Integer, Monster> resultSetMapper = new ResultSetMapper<Integer, Monster>() {

        @Override
        public AbstractMap.SimpleEntry<Integer, Monster> mapRow(ResultSet rs) throws SQLException {
            Monster monster = new Monster();

            monster.id = rs.getInt("id");
            monster.name = rs.getString("Name");
            monster.race = rs.getString("Race");
            monster.file = rs.getString("File");
            monster.linkage = rs.getString("Linkage");
            monster.element = rs.getString("Element");

            monster.health = rs.getInt("Health");
            monster.mana = rs.getInt("Mana");
            monster.level = rs.getInt("Level");
            monster.gold = rs.getInt("Gold");
            monster.experience = rs.getInt("Experience");
            monster.reputation = rs.getInt("Reputation");
            monster.DPS = rs.getInt("DPS");
            monster.teamId = rs.getInt("TeamID");
            monster.minDmg = (int) Math.floor((monster.DPS - (monster.DPS * 0.1)));
            monster.maxDmg = (int) Math.ceil((monster.DPS + (monster.DPS * 0.1)));

            return new AbstractMap.SimpleEntry<Integer, Monster>(monster.id, monster);
        }
    };

    public String getName() {
        return name;
    }

    public String getRace() {
        return race;
    }

    public String getFile() {
        return file;
    }

    public String getLinkage() {
        return linkage;
    }

    public String getElement() {
        return element;
    }

    public int getHealth() {
        return health;
    }

    public int getMana() {
        return mana;
    }

    public int getLevel() {
        return level;
    }

    public int getGold() {
        return gold;
    }

    public int getExperience() {
        return experience;
    }

    public int getReputation() {
        return reputation;
    }

    public int getDPS() {
        return DPS;
    }

    public int getId() {
        return id;
    }

    public int getTeamId() {
        return teamId;
    }

    public int getMinDmg() {
        return minDmg;
    }

    public int getMaxDmg() {
        return maxDmg;
    }
}

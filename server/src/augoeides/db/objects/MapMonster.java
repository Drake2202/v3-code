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
public class MapMonster {

    private int monMapId, monsterId;
    private String frame;

    public static final BeanCreator<Set<MapMonster>> setCreator = new BeanCreator<Set<MapMonster>>() {

        @Override
        public Set<MapMonster> createBean(ResultSet rs) throws SQLException {
            Set<MapMonster> monsters = new HashSet<MapMonster>();

            MapMonster mapMonster = new MapMonster();

            mapMonster.frame = rs.getString("Frame");
            mapMonster.monsterId = rs.getInt("MonsterID");
            mapMonster.monMapId = rs.getInt("MonMapID");

            monsters.add(mapMonster);

            while (rs.next()) {
                mapMonster = new MapMonster();

                mapMonster.frame = rs.getString("Frame");
                mapMonster.monsterId = rs.getInt("MonsterID");
                mapMonster.monMapId = rs.getInt("MonMapID");

                monsters.add(mapMonster);
            }

            return monsters;
        }

    };

    public int getMonMapId() {
        return monMapId;
    }

    public int getMonsterId() {
        return monsterId;
    }

    public String getFrame() {
        return frame;
    }
}

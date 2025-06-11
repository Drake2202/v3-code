/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.db.objects;

import java.util.HashSet;

/**
 *
 * @author Mystical
 */
public class Hall extends Area {

    private int guildId;

    public Hall(int guildId) {
        this.guildId = guildId;
        this.monsters = new HashSet<MapMonster>();
    }

    @Override
    public int getReqLevel() {
        return 0;
    }

    @Override
    public boolean isStaff() {
        return false;
    }

    @Override
    public boolean isUpgrade() {
        return false;
    }

    @Override
    public boolean isPvP() {
        return false;
    }

    @Override
    public int getMaxPlayers() {
        return 100000;
    }

    @Override
    public String getFile() {
        return "Guildhall/guildHallTest.swf";
    }

    public int getGuildId() {
        return guildId;
    }
}

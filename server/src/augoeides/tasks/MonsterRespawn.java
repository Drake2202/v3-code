/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.tasks;

import augoeides.ai.MonsterAI;
import augoeides.world.World;

/**
 *
 * @author Mystical
 */
public class MonsterRespawn implements Runnable {

    private MonsterAI ai;
    private World world;

    public MonsterRespawn(World world, MonsterAI ai) {
        this.ai = ai;
        this.world = world;
    }

    @Override
    public void run() {
        this.ai.restore();
        this.world.send(new String[]{"respawnMon", Integer.toString(ai.getMapMonster().getMonMapId())}, this.ai.getRoom().getChannellList());
    }

}

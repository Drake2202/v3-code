/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.avatars;

import augoeides.ai.MonsterAI;
import augoeides.db.objects.Aura;
import augoeides.db.objects.Monster;
import augoeides.db.objects.MonsterDrop;
import augoeides.tasks.MonsterRespawn;
import augoeides.tasks.RemoveAura;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.User;
import it.gotoandplay.smartfoxserver.extensions.ExtensionHelper;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Mystical
 */
public class MonsterState extends State {

    private Set<Integer> targets;
    private World world;
    private MonsterAI monster;

    public MonsterState(World world, MonsterAI monster, int maxHealth, int maxMana) {
        super(maxHealth, maxMana);
        this.monster = monster;
        this.world = world;
        this.targets = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());
    }
    
    @Override
    public void die() {
        super.die();
        super.clearAuras();
        
        this.world.scheduleTask(new MonsterRespawn(this.world, monster), 20, TimeUnit.SECONDS);

        giveRewards();
        this.targets.clear();
    }
    
    
    private void giveRewards() {
        //Give Rewards
        Set<MonsterDrop> drops = getDrops();
        for (int userId : this.targets) {
            User user = ExtensionHelper.instance().getUserById(userId);
            if (user != null)
                dropRewards(user, drops);
        }
    }

    private Set<MonsterDrop> getDrops() {
        Monster mon = this.monster.getMonster();
        Set<MonsterDrop> drops = new HashSet<MonsterDrop>();
        for (MonsterDrop md : mon.drops)
            if (Math.random() <= (md.chance * world.DROP_RATE))
                drops.add(md);

        return drops;
    }

    private void dropRewards(User user, Set<MonsterDrop> drops) {
        Monster mon = this.monster.getMonster();
        for (MonsterDrop md : drops)
            this.world.users.dropItem(user, md.itemId, md.quantity);

        this.world.users.giveRewards(user, mon.getExperience(), mon.getGold(), 
                mon.getReputation(), 0, -1, this.monster.getMapMonster().getMonMapId(), "m");
    }

    @Override
    public void restore() {
        super.restore();
        this.targets.clear();
    }

    public int getRandomTarget() {
        Integer[] setArray = this.targets.toArray(new Integer[this.targets.size()]);
        return setArray[World.RANDOM.nextInt(setArray.length)].intValue();
    }
        
    public String getRandomTargets(int maxTargets) {
        StringBuilder sb = new StringBuilder();
        maxTargets = maxTargets < 1 ? 1 : maxTargets;
        while (maxTargets > 0) {
            int userId = getRandomTarget();
            if (!sb.toString().contains(String.valueOf(userId))) {
                sb.append(",");
                sb.append("p:");
                sb.append(userId);
            }
            maxTargets--;
        }
        sb.deleteCharAt(0);
        return sb.toString();
    }
    
    public boolean hasTargets() {
        return !this.targets.isEmpty();
    }

    public Set<Integer> getTargets() {
        return Collections.unmodifiableSet(this.targets);
    }

    public void addTarget(int userId) {
        if (!targets.contains(userId))
            targets.add(userId);
    }

    public void removeTarget(int userId) {
        targets.remove(Integer.valueOf(userId));
    }

    @Override
    public RemoveAura applyAura(Aura aura) {
        RemoveAura ra = new RemoveAura(this.world, aura, this.monster);

        ra.setRunning(this.world.scheduleTask(ra, aura.getDuration(), TimeUnit.SECONDS));
        addAura(ra);

        return ra;
    }

    public void setTargets(Set<Integer> targets) {
        this.targets = targets;
    }
}

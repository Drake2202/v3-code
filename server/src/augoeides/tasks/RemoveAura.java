/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.tasks;

import augoeides.ai.MonsterAI;
import augoeides.db.objects.Aura;
import augoeides.db.objects.AuraEffects;
import augoeides.world.Users;
import augoeides.world.World;
import augoeides.world.stats.Stats;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class RemoveAura implements Runnable, CancellableTask {

    private World world;
    private Aura aura;
    private DamageOverTime dot;
    private ScheduledFuture<?> running;
    private User user;
    private MonsterAI ai;

    public RemoveAura(World world, Aura aura, MonsterAI ai) {
        this.world = world;
        this.aura = aura;
        this.ai = ai;
    }

    public RemoveAura(World world, Aura aura, User user) {
        this.world = world;
        this.aura = aura;
        this.user = user;
    }

    @Override
    public void run() {
        JSONObject ct = new JSONObject();
        JSONArray a = new JSONArray();
        JSONObject o = new JSONObject();
        JSONObject auraInfo = new JSONObject();

        if (!this.aura.getCategory().isEmpty() && !this.aura.getCategory().equals("d")) {
            auraInfo.put("cat", this.aura.getCategory());
            if (this.aura.getCategory().equals("stun"))
                auraInfo.put("s", "s");
        }

        auraInfo.put("nam", aura.getName());

        o.put("cmd", "aura-");
        o.put("aura", auraInfo);

        if (this.user != null)
            o.put("tInf", "p:" + this.user.getUserId());
        else if (this.ai != null)
            o.put("tInf", "m:" + this.ai.getMapMonster().getMonMapId());

        a.add(o);

        ct.put("cmd", "ct");
        ct.put("a", a);

        //Process depending on type
        if (this.user != null) {
            Set<RemoveAura> auras = (Set<RemoveAura>) user.properties.get(Users.AURAS);
            auras.remove(this);

            if (!this.aura.effects.isEmpty()) {
                Stats stats = (Stats) user.properties.get(Users.STATS);
                Set<AuraEffects> auraEffects = new HashSet<AuraEffects>();
                for (int effectId : this.aura.effects) {
                    AuraEffects ae = world.effects.get(effectId);
                    stats.effects.remove(ae);
                    auraEffects.add(ae);
                }
                stats.update();
                stats.sendStatChanges(stats, auraEffects);
            }

            this.world.send(ct, this.world.zone.getRoom(this.user.getRoom()).getChannellList());
        } else if (this.ai != null) {
            this.ai.getState().removeAura(this);
            this.world.send(ct, this.ai.getRoom().getChannellList());
        }

        if (this.dot != null)
            this.dot.cancel();
    }

    @Override
    public void cancel() {
        if (this.dot != null)
            this.dot.cancel();
        if (this.running != null)
            this.running.cancel(false);
    }

    @Override
    public void setRunning(ScheduledFuture<?> running) {
        this.running = running;
    }

    public Aura getAura() {
        return aura;
    }

    public void setDot(DamageOverTime dot) {
        this.dot = dot;
    }

    @Override
    public int hashCode() {
        return this.aura.getId();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final RemoveAura other = (RemoveAura) obj;
        return this.aura.getId() == other.aura.getId();
    }
}

/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.ai;

import augoeides.avatars.MonsterState;
import augoeides.avatars.State;
import augoeides.combat.Damage;
import augoeides.combat.DamageType;
import augoeides.db.objects.MapMonster;
import augoeides.db.objects.Monster;
import augoeides.db.objects.Skill;
import augoeides.tasks.RemoveAura;
import augoeides.world.Users;
import augoeides.world.World;
import augoeides.world.stats.Stats;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class MonsterAI implements Runnable {

    private ScheduledFuture<?> attacking;
    private World world;
    private MonsterState state;
    private MapMonster mapMonster;
    private Monster monster;
    private MonsterSkills skills;
    private Room room;

    public MonsterAI(MapMonster mapMon, World world, Room room) {
        this.world = world;
        this.mapMonster = mapMon;
        this.monster = world.monsters.get(mapMon.getMonsterId());
        this.room = room;
        this.state = new MonsterState(world, this, this.monster.getHealth(), this.monster.getMana());
        this.skills = new MonsterSkills();

        for (int skillId : this.monster.skills)
            this.skills.addSkill(world.skills.get(skillId));
    }

    public void cancel() {
        if (this.state.hasTargets() && this.state.isNeutral()) {
            this.restore();
            this.attacking.cancel(false);
        }
    }

    @Override
    public void run() {
        if (this.state.isDead()) {
            this.attacking.cancel(false);
            return;
        }

        int userId = this.state.getRandomTarget();
        User user = SmartFoxServer.getInstance().getUserById(userId);

        if (user == null || (this.room.getId() != user.getRoom())
                || !this.mapMonster.getFrame().equals((String) user.properties.get(Users.FRAME))) {
            this.state.removeTarget(userId);
            cancel();
        } else
            attack(user);
    }

    private boolean isDead() {
        if (this.state.isDead()) {
            if (this.attacking != null) this.attacking.cancel(true);
            return true;
        }
        return false;
    }

    private void attack(User user) {
        if (isDead() || this.state.isDisabled()) return;

        Stats stats = (Stats) user.properties.get(Users.STATS);
        Set<RemoveAura> userAuras = (Set<RemoveAura>) user.properties.get(Users.AURAS);
        State userState = (State) user.properties.get(Users.USER_STATE);

        DamageType type = Damage.evaluateDamageType(0.2, stats.get$tdo(), 0.2);
        int damage = Damage.getReducedDamage(Damage.getRandomDamage(type, this.monster.getMaxDmg(),
                this.monster.getMinDmg()), userAuras);

        this.state.increaseManaByPercent(0.02);
        userState.decreaseHealth(damage);

        if (userState.isDead()) {
            this.state.removeTarget(user.getUserId());
            cancel();
        }

        JSONObject p = new JSONObject().accumulate(user.getName(), userState.getData());
        JSONArray sara = new JSONArray().element(new JSONObject()
                .accumulate("actionResult", getAction(type, damage, user))
                .accumulate("iRes", 1));
        JSONObject m = new JSONObject().accumulate(String.valueOf(this.mapMonster.getMonMapId()), new JSONObject()
                .accumulate("intMP", this.state.getMana()));
        JSONObject ct = new JSONObject().accumulate("anims", new JSONArray()
                .element(getAnim(user)))
                .accumulate("p", p)
                .accumulate("m", m)
                .accumulate("cmd", "ct");

        world.send(ct, getUserChannelButOne(room, user.getUserId()));
        ct.accumulate("sara", sara);
        world.send(ct, user);

        cast(this.skills.getSkill());
    }
    
    private LinkedList<SocketChannel> getUserChannelButOne(Room room, int userId) {
        LinkedList<SocketChannel> list = new LinkedList<SocketChannel>();
        
        User[] usersInRoom = room.getAllUsersButOne(userId);
        
        for(User user : usersInRoom) {
            list.add(user.getChannel());
        }
        
        return list;
    }

    private void cast(Skill skill) {
        if (skill == null) return;
        if (skill.getMana() > this.state.getMana()) return;

        String targets = this.state.getRandomTargets(skill.getHitTargets());

        JSONObject p = new JSONObject();
        JSONArray a = new JSONArray();
        JSONArray auras = new JSONArray();

        for (String tgt : targets.split(",")) {
            int userId = Integer.parseInt(tgt.split(":")[1]);
            User user = SmartFoxServer.getInstance().getUserById(userId);

            if (user == null || (this.room.getId() != user.getRoom())) {
                this.state.removeTarget(userId);
                continue;
            }

            Stats stats = (Stats) user.properties.get(Users.STATS);
            Set<RemoveAura> userAuras = (Set<RemoveAura>) user.properties.get(Users.AURAS);
            State userState = (State) user.properties.get(Users.USER_STATE);

            DamageType type = Damage.evaluateDamageType(0.2, stats.get$tdo(), 0.2);
            int damage = (int) (Damage.evaluateAuras(Damage.evaluateAuras(Damage.getRandomDamage(type, this.monster.getMaxDmg(),
                    this.monster.getMinDmg()), userAuras), state.getAuras()) * skill.getDamage());

            this.state.increaseManaByPercent(0.02);
            userState.decreaseHealth(damage);

            if (userState.isDead()) {
                this.state.removeTarget(user.getUserId());
                cancel();
            }

            if (skill.hasAuraId()) auras.add(userState.applyAura(world.auras.get(skill.getAuraId())));

            a.add(Damage.getDamageResult("m:" + this.mapMonster.getMonMapId(), tgt, damage, type));
            p.put(user.getName(), userState.getData());
        }

        this.state.decreaseMana(skill.getMana());

        JSONObject ct = new JSONObject().accumulate("p", p)
                .accumulate("m", new JSONObject().accumulate(String.valueOf(this.mapMonster.getMonMapId()), new JSONObject()
                                .accumulate("intMP", this.state.getMana())))
                .accumulate("sarsa", new JSONArray().element(new JSONObject()
                                .accumulate("cInf", "m:" + this.mapMonster.getMonMapId())
                                .accumulate("a", a)
                                .accumulate("iRes", 1)))
                .accumulate("cmd", "ct")
                .accumulate("a", auras)
                .accumulate("anims", new JSONArray().element(getAnim(this.mapMonster.getFrame(), targets, skill)));

        world.send(ct, room.getChannellList());
    }

    private JSONObject getAction(DamageType type, int damage, User user) {
        JSONObject action = new JSONObject();
        action.put("hp", damage);
        action.put("cInf", "m:" + this.mapMonster.getMonMapId());
        action.put("tInf", "p:" + Integer.toString(user.getUserId()));
        action.put("type", type.toString());
        return action;
    }

    private JSONObject getAnim(String frame, String targets, Skill skill) {
        JSONObject anim = new JSONObject();
        anim.put("strFrame", frame);
        anim.put("cInf", "m:" + this.mapMonster.getMonMapId());
        anim.put("fx", skill.getEffects());
        anim.put("tInf", targets);
        anim.put("animStr", skill.getAnimation());

        if (!skill.getStrl().isEmpty())
            anim.put("strl", skill.getStrl());

        return anim;
    }

    private JSONObject getAnim(User user) {
        JSONObject anim = new JSONObject();
        anim.put("strFrame", user.properties.get(Users.FRAME));
        anim.put("cInf", "m:" + this.mapMonster.getMonMapId());
        anim.put("fx", "m");
        anim.put("tInf", "p:" + Integer.toString(user.getUserId()));
        anim.put("animStr", "Attack1,Attack2");
        return anim;
    }

    public void restore() {
        this.state.restore();

        JSONObject mtls = new JSONObject();
        mtls.put("cmd", "mtls");
        mtls.put("id", this.mapMonster.getMonMapId());
        mtls.put("o", this.state.getData());

        this.world.send(mtls, room.getChannellList());
    }

    public Room getRoom() {
        return room;
    }

    public synchronized void setAttacking(ScheduledFuture<?> attacking) {
        if (this.attacking == null || this.attacking.isDone()) {
            this.state.setState(2);
            this.attacking = attacking;
        } else
            attacking.cancel(true);
    }

    public Monster getMonster() {
        return monster;
    }

    public MapMonster getMapMonster() {
        return mapMonster;
    }

    public MonsterState getState() {
        return state;
    }
}

/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

import augoeides.ai.MonsterAI;
import augoeides.aqw.Rank;
import augoeides.avatars.State;
import augoeides.avatars.UserState;
import augoeides.combat.Damage;
import augoeides.combat.DamageType;
import augoeides.db.objects.Area;
import augoeides.db.objects.Aura;
import augoeides.db.objects.AuraEffects;
import augoeides.db.objects.Skill;
import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.tasks.DamageOverTime;
import augoeides.tasks.RemoveAura;
import augoeides.world.Rooms;
import augoeides.world.Users;
import augoeides.world.World;
import augoeides.world.stats.Stats;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import it.gotoandplay.smartfoxserver.extensions.ExtensionHelper;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import jdbchelper.NoResultException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class Action implements IRequest {

    private World world;
    private Room room;
    private User user;
    private State state;
    private Stats stats;
    private Set<RemoveAura> userAuras;
    private Area area;

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        this.world = world;
        this.user = user;
        this.room = room;
        this.state = (State) user.properties.get(Users.USER_STATE);
        this.stats = (Stats) user.properties.get(Users.STATS);
        this.userAuras = (Set<RemoveAura>) user.properties.get(Users.AURAS);
        this.area = world.areas.get(room.getName().split("-")[0]);

        int actId = Integer.parseInt(params[0]);
        String skillReference = getSkillReference(params[1]);
        String tInf = parseTargetInfo(params[1]);
        String[] targets = tInf.split(",");
        Skill skill = getSkill(skillReference);

        if (state.isDead()) return;
        if (hasDuplicates(targets)) return;
        if (skill == null) return;

        JSONObject ct = new JSONObject();

        JSONArray auras = new JSONArray();
        JSONArray a = new JSONArray();
        JSONObject p = new JSONObject();
        JSONObject m = new JSONObject();

        String fromTarget = "p:" + user.getUserId();

        state.decreaseMana(skill.getMana());

        int manaIncrease = (int) (stats.get$INT() + stats.get_INT() / 2);
        state.increaseMana(World.RANDOM.nextInt(Math.abs(manaIncrease)));

        Map<Integer, MonsterAI> monsters = (ConcurrentHashMap<Integer, MonsterAI>) room.properties.get(Rooms.MONSTERS); //get monsters in room

        for (String target : targets) {
            DamageType type = DamageType.NONE;
            int damage = 0;

            String tgtType = target.split(":")[0];
            int tgtId = Integer.parseInt(target.split(":")[1]);

            if (tgtType.equals("m")) {
                MonsterAI ai = monsters.get(tgtId);
                
                if (ai == null) continue;

                type = Damage.evaluateDamageType(1 -(1 - world.coreValues.get("baseMiss")) + stats.get$thi(), 0.2, stats.get$tcr());
                damage = (int) (Damage.getRandomDamage(type, stats.getMaxDmg(), stats.getMinDmg()) * skill.getDamage());
                damage = Damage.evaluateAuras(Damage.evaluateAuras(damage, ai.getState().getAuras()), userAuras);
                
                ai.getState().decreaseHealth(damage);
                ai.getState().addTarget(user.getUserId());
                
                state.setState(2);

                if (ai.getState().isDead()) {
                    if (area.isPvP()) {
                        world.rooms.relayPvPEvent(ai, (Integer) user.properties.get(Users.PVP_TEAM)); //relay pvp event
                        ct.put("pvp", world.rooms.getPvPResult(room)); //get pvp result
                    }

                    Set<Integer> monTargets = ai.getState().getTargets();

                    for (int userId : monTargets) {
                        User userTgt = ExtensionHelper.instance().getUserById(userId);
                        if (userTgt != null) {
                            world.users.regen(user);

                            JSONObject userData = new JSONObject();
                            userData.put("intState", 1);
                            p.put(userTgt.getName(), userData);
                        }
                    }
                }

                if (ai.getState().isNeutral()) ai.setAttacking(world.scheduleTask(ai, 2500, TimeUnit.MILLISECONDS, true));
                if (skill.hasAuraId()) auras.add(applyAura(world, ai, skill.getAuraId(), fromTarget, damage));

                m.accumulate(String.valueOf(ai.getMapMonster().getMonMapId()), ai.getState().getData()
                        .accumulate("targets", ai.getState().getTargets()));
            }

            if (tgtType.equals("p")) {
                User userTgt = ExtensionHelper.instance().getUserById(tgtId);
                if (userTgt == null) continue;

                Set<RemoveAura> userTgtAuras = (Set<RemoveAura>) userTgt.properties.get(Users.AURAS);
                Stats userStats = (Stats) userTgt.properties.get(Users.STATS);

                type = Damage.evaluateDamageType(1 - (1 - world.coreValues.get("baseMiss")) + stats.get$thi(), userStats.get$tdo(), stats.get$tcr());
                damage = (int) (Damage.getRandomDamage(type, stats.getMaxDmg(), stats.getMinDmg()) * skill.getDamage());
                damage = Damage.evaluateAuras(Damage.evaluateAuras(damage, userAuras), userTgtAuras);

                processUser(userTgt, damage);

                UserState userState = (UserState) userTgt.properties.get(Users.USER_STATE);
                
                if(damage <= 0) type = DamageType.HIT;
                else userState.setState(2);

                if (area.isPvP()) ct.put("pvp", world.rooms.getPvPResult(room));
                if (skill.hasAuraId()) auras.add(applyAura(world, userTgt, skill.getAuraId(), fromTarget, damage));

                p.put(userTgt.getName(), userState.getData());
            }

            a.add(Damage.getDamageResult(fromTarget, target, damage, type));
        }

        if (!p.containsKey(user.getName())) p.put(user.getName(), state.getData());
        if (state.isNeutral()) world.users.regen(user);

        JSONArray sarsa = new JSONArray().element(new JSONObject()
                .accumulate("cInf", fromTarget)
                .accumulate("a", a)
                .accumulate("actID", actId)
                .accumulate("iRes", 1)
        );

        ct.put("m", m);
        ct.put("a", auras);
        ct.put("p", p);
        ct.put("cmd", "ct");
        ct.put("anims", getAnims(skill, tInf));

        if (area.isPvP()) {
            ct.put("sarsa", sarsa);
            world.sendToRoom(ct, user, room);
        } else {
            world.sendToRoomButOne(ct, user, room);
            ct.put("sarsa", sarsa);
            world.send(ct, user);
        }

    }

    private Skill getSkill(String skillReference) {
        Map<String, Integer> skills = (Map<String, Integer>) user.properties.get(Users.SKILLS);
        Skill skill = world.skills.get(skills.get(skillReference));
        try {
            int rank = Rank.getRankFromPoints((Integer) user.properties.get(Users.CLASS_POINTS));

            if ((rank < 2 && skill.getReference().equals("a2")) || (rank < 3 && skill.getReference().equals("a3"))
                    || (rank < 5 && skill.getReference().equals("a4"))) {
                world.users.log(user, "Packet Edit [Action]", "Using a skill when designated rank is not yet achieved.");
                skill = null;
            } else if (skill.getReference().equals("i1")) {
                int itemId = world.db.jdbc.queryForInt("SELECT id FROM items WHERE Meta = ?", skill.getId());
                if (!world.users.turnInItem(user, itemId, 1))
                    world.users.log(user, "Packet Edit [Action]", "TurnIn failed when using potions.");
                skill = null;
            }
        } catch (NoResultException nre) {
            throw new UnsupportedOperationException("Unassigned skill ID: " + skillReference);
        }
        return skill;
    }

    private JSONArray getAnims(Skill skill, String tInf) {
        JSONObject anim = new JSONObject()
                .accumulate("strFrame", user.properties.get(Users.FRAME))
                .accumulate("fx", skill.getEffects())
                .accumulate("tInf", tInf)
                .accumulate("cInf", "p:" + user.getUserId())
                .accumulate("animStr", skill.getAnimation());

        if (!skill.getStrl().isEmpty())
            anim.put("strl", skill.getStrl());

        return new JSONArray().element(anim);
    }

    private void processUser(User userTgt, int damage) {
        UserState userState = (UserState) userTgt.properties.get(Users.USER_STATE);
        
        if(damage > 0) state.setState(2);

        userState.decreaseHealth(damage);

        if (userState.isDead()) {
            state.setState(1);

            world.db.jdbc.run("UPDATE users SET DeathCount = (DeathCount + 1) WHERE id = ?", userTgt.properties.get(Users.DATABASE_ID));
            world.db.jdbc.run("UPDATE users SET KillCount = (KillCount + 1) WHERE id = ?", user.properties.get(Users.DATABASE_ID));

            if (area.isPvP()) {
                for (int itemId : area.items)
                    world.users.dropItem(user, itemId);

                if (room.getName().split("-")[0].equals("deadlock"))
                    world.rooms.addPvPScore(room, 1000, (Integer) user.properties.get(Users.PVP_TEAM));
                else
                    world.rooms.addPvPScore(room, (Integer) userTgt.properties.get(Users.LEVEL), (Integer) user.properties.get(Users.PVP_TEAM));
            }
        }
    }

    private boolean hasDuplicates(String[] targets) {
        List inputList = Arrays.asList(targets);
        Set inputSet = new HashSet(inputList);
        if (inputSet.size() < inputList.size()) {
            world.db.jdbc.run("UPDATE users SET Access = 0, PermamuteFlag = 1 WHERE id = ?", user.properties.get(Users.DATABASE_ID));
            world.users.kick(user);
            world.users.log(user, "Packet Edit [gar]", "Attack packet hack.");
            return true;
        }

        return false;
    }

    private JSONObject applyAura(World world, MonsterAI ai, int auraId, String fromTarget, int damage) {
        JSONObject aInfo = new JSONObject();

        Aura aura = world.auras.get(auraId);

        boolean auraExists = ai.getState().hasAura(aura.getId());

        aInfo.put("cInf", fromTarget);
        aInfo.put("cmd", "aura+");
        aInfo.put("auras", aura.getAuraArray(!auraExists));
        aInfo.put("tInf", "m:" + ai.getMapMonster().getMonMapId());

        if (auraExists) return aInfo;

        RemoveAura ra = ai.getState().applyAura(aura);

        if (aura.getCategory().equals("d")) {
            DamageOverTime dot = new DamageOverTime(world, ai, damage, fromTarget);

            dot.setRunning(world.scheduleTask(dot, 2, TimeUnit.SECONDS, true));
            ra.setDot(dot);
        }

        return aInfo;
    }

    private JSONObject applyAura(World world, User userTgt, int auraId, String fromTarget, int damage) {
        JSONObject aInfo = new JSONObject();

        Aura aura = world.auras.get(auraId);

        boolean auraExists = world.users.hasAura(userTgt, aura.getId());

        aInfo.put("cInf", fromTarget);
        aInfo.put("cmd", "aura+");
        aInfo.put("auras", aura.getAuraArray(!auraExists));
        aInfo.put("tInf", "p:" + userTgt.getUserId());

        if (auraExists) return aInfo;

        RemoveAura ra = world.users.applyAura(userTgt, aura);

        if (!aura.effects.isEmpty()) {
            Stats userStats = (Stats) userTgt.properties.get(Users.STATS);
            Set<AuraEffects> auraEffects = new HashSet<AuraEffects>();
            for (int effectId : aura.effects) {
                AuraEffects ae = world.effects.get(effectId);
                userStats.effects.add(ae);
                auraEffects.add(ae);
            }
            userStats.update();
            userStats.sendStatChanges(userStats, auraEffects);
        }

        if (aura.getCategory().equals("d")) {
            DamageOverTime dot = new DamageOverTime(world, userTgt, damage, fromTarget);

            dot.setRunning(world.scheduleTask(dot, 2, TimeUnit.SECONDS, true));
            ra.setDot(dot);
        }

        return aInfo;
    }

    private String getSkillReference(String str) {
        if (str.contains(","))
            return str.split(",")[0].split(">")[0];
        else
            return str.split(">")[0];
    }

    private String parseTargetInfo(String str) {
        StringBuilder tb = new StringBuilder();

        if (str.contains(",")) {
            String[] multi = str.split(",");
            for (int i = 0; i < multi.length; i++) {
                if (i != 0)
                    tb.append(",");
                tb.append(multi[i].split(">")[1]);
            }
        } else
            tb.append(str.split(">")[1]);

        return tb.toString();
    }

}

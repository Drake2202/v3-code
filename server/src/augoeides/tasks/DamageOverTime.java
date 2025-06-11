/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.tasks;

import augoeides.ai.MonsterAI;
import augoeides.avatars.MonsterState;
import augoeides.avatars.State;
import augoeides.combat.Damage;
import augoeides.combat.DamageType;
import augoeides.world.Users;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import it.gotoandplay.smartfoxserver.extensions.ExtensionHelper;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class DamageOverTime implements Runnable, CancellableTask {

    private static final Random rand;

    private World world;
    private ScheduledFuture<?> running;
    private User user;
    private MonsterAI ai;
    private String fromTarget;
    private int damage;

    static {
        rand = new Random();
    }

    public DamageOverTime(World world, User user, int damage, String fromTarget) {
        this.world = world;
        this.user = user;
        this.damage = damage;
        this.fromTarget = fromTarget;

        rand.setSeed(damage);
    }

    public DamageOverTime(World world, MonsterAI ai, int damage, String fromTarget) {
        this.world = world;
        this.ai = ai;
        this.damage = damage;
        this.fromTarget = fromTarget;

        rand.setSeed(damage);
    }

    @Override
    public void run() {
        if (this.damage == 0)
            throw new RuntimeException("damage is 0, pointless to continue.");

        int dotDamage = rand.nextInt(Math.abs(this.damage));
        dotDamage = (this.damage < 0) ? (dotDamage * -1) : dotDamage;

        if (this.user != null)
            processUser(dotDamage);

        if (this.ai != null) 
            processMonster(dotDamage);
    }

    public void processMonster(int dotDamage) {
        JSONObject ct = new JSONObject();
        
        SmartFoxServer.log.fine(" PROCESS MONSTER ");

        ct.put("cmd", "ct");
        ct.put("sara", new JSONArray().element(new JSONObject()
                .accumulate("actionResult", Damage.getDamageResult(fromTarget, "m:" + ai.getMapMonster().getMonMapId(), dotDamage,
                                DamageType.DAMAGE_OVER_TIME))
                .accumulate("iRes", 1)));

        JSONObject p = new JSONObject();
        
        MonsterState state = this.ai.getState();

        state.decreaseHealth(dotDamage);

        if (state.isDead()) {
            cancel();

            if (world.areas.get(ai.getRoom().getName().split("-")[0]).isPvP()) {
                int teamId = this.world.monsters.get(ai.getMapMonster().getMonsterId()).getTeamId() == 1 ? 0 : 1;
                world.rooms.relayPvPEvent(ai, teamId);
                ct.put("pvp", world.rooms.getPvPResult(ai.getRoom()));
            }

            Set<Integer> monTargets = state.getTargets();

            for (int userId : monTargets) {
                User userTgt = ExtensionHelper.instance().getUserById(userId);
                //Check if user exists
                if (userTgt != null) {
                    world.users.regen(user);

                    JSONObject userData = new JSONObject();
                    userData.put("intState", 1);
                    p.put(userTgt.getName(), userData);
                }
            }
        }
        ct.put("m", new JSONObject().accumulate(String.valueOf(ai.getMapMonster().getMonsterId()), state.getData()
                .accumulate("targets", state.getTargets())));

        if (!p.isEmpty())
            ct.put("p", p);

        world.send(ct, this.ai.getRoom().getChannellList());
    }

    public void processUser(int dotDamage) {
        JSONObject ct = new JSONObject();
        
        SmartFoxServer.log.fine(" PROCESS USER ");

        ct.put("cmd", "ct");
        ct.put("sara", new JSONArray().element(new JSONObject()
                .accumulate("actionResult", Damage.getDamageResult(fromTarget, "p:" + user.getUserId(), dotDamage,
                                DamageType.DAMAGE_OVER_TIME))
                .accumulate("iRes", 1)));

        Room room = world.zone.getRoom(user.getRoom());
        JSONObject p = new JSONObject();
        State userState = (State) user.properties.get(Users.USER_STATE);

        userState.decreaseHealth(dotDamage);

        if (userState.isDead()) {
            cancel();

            if (world.areas.get(room.getName().split("-")[0]).isPvP()) {
                int teamId = (Integer) user.properties.get(Users.PVP_TEAM) == 0 ? 1 : 0;
                if (room.getName().split("-")[0].equals("deadlock"))
                    world.rooms.addPvPScore(room, 1000, teamId);
                else
                    world.rooms.addPvPScore(room, (Integer) user.properties.get(Users.LEVEL), teamId);
                ct.put("pvp", world.rooms.getPvPResult(room));
            }
        }

        p.put(this.user.getName(), userState.getData());
        ct.put("p", p);

        world.sendToRoom(ct, user, room);
    }

    @Override
    public void cancel() {
        this.running.cancel(false);
    }

    @Override
    public void setRunning(ScheduledFuture<?> running) {
        this.running = running;
    }
}

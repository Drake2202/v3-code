/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

import augoeides.ai.MonsterAI;
import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.Rooms;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Mystical
 */
public class AggroMonster implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        if (user == null) return;

        Map<Integer, MonsterAI> monsters = (ConcurrentHashMap<Integer, MonsterAI>) room.properties.get(Rooms.MONSTERS);
        MonsterAI ai = monsters.get(Integer.parseInt(params[0]));

        if (ai == null) return;

        ai.getState().addTarget(user.getUserId());
        if (ai.getState().isNeutral())
            ai.setAttacking(world.scheduleTask(ai, 2500, TimeUnit.MILLISECONDS, true));
    }
}

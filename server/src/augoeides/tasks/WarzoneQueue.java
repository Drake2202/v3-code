/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.tasks;

import augoeides.db.objects.Area;
import augoeides.world.Users;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class WarzoneQueue implements Runnable {

    private ConcurrentHashMap<String, LinkedBlockingQueue<User>> warzoneQueues = new ConcurrentHashMap<String, LinkedBlockingQueue<User>>();
    private World world;

    public WarzoneQueue(World world) {
        this.world = world;

        SmartFoxServer.log.info("WarzoneQueue intialized.");
    }

    @Override
    public void run() {
        for (Map.Entry<String, LinkedBlockingQueue<User>> e : warzoneQueues.entrySet()) {
            LinkedBlockingQueue<User> pq = e.getValue();
            String warzone = e.getKey();
            
            Area area = world.areas.get(warzone);
            
            if (pq.size() == area.getMaxPlayers()) {
                JSONObject PVPI = new JSONObject();

                PVPI.put("cmd", "PVPI");
                PVPI.put("warzone", warzone);

                Room warzoneRoom = world.rooms.createRoom(warzone + "-" + Math.abs(PVPI.hashCode()));

                for (int i = 0; i < area.getMaxPlayers(); i++)
                    try {
                        User user = pq.take();
                        user.properties.put(Users.ROOM_QUEUED, warzoneRoom);

                        if (i % 2 == 0)
                            user.properties.put(Users.PVP_TEAM, 0);
                        else
                            user.properties.put(Users.PVP_TEAM, 1);

                        this.world.send(new String[]{"server", "A new Warzone battle has started!"}, user);
                        this.world.send(PVPI, user);
                    } catch (InterruptedException ex) {
                    }
            }
        }
    }

    private LinkedBlockingQueue<User> getWarzoneQueue(String warzone) {
        if (warzoneQueues.containsKey(warzone))
            return warzoneQueues.get(warzone);
        else {
            warzoneQueues.putIfAbsent(warzone, new LinkedBlockingQueue<User>());
            return warzoneQueues.get(warzone);
        }
    }

    public void removeUserFromQueues(User user) {
        for (LinkedBlockingQueue<User> pq : warzoneQueues.values())
            pq.remove(user);
    }

    public void queueUser(String warzone, User user) {
        LinkedBlockingQueue<User> pq = getWarzoneQueue(warzone);
        pq.offer(user);
    }
}

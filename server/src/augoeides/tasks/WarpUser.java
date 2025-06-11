/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.tasks;

import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.Set;

/**
 *
 * @author Mystical
 */
public class WarpUser implements Runnable {

    private Set<User> users;
    private World world;

    public WarpUser(World world, Set<User> users) {
        this.users = users;
        this.world = world;
    }

    @Override
    public void run() {
        for (User user : users)
            this.world.rooms.basicRoomJoin(user, "faroff");
    }
}

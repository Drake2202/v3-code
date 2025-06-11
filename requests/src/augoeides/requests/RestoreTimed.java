/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.Users;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Mystical
 */
public class RestoreTimed implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        Long respawnTime = (Long) user.properties.get(Users.RESPAWN_TIME);

        int elapsed = Long.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - respawnTime)).intValue();
        if (elapsed >= 8) {
            world.users.respawn(user);
            world.send(new String[]{"resTimed", user.properties.get(Users.FRAME).toString(), user.properties.get(Users.PAD).toString()}, user);
        } else
            world.users.log(user, "Packet Edit [RestoreTimed]", "Respawning when elapsed time is less than 8 seconds");
    }

}

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

/**
 *
 * @author Mystical
 */
public class Afk implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        boolean afk = Boolean.parseBoolean(params[0]);

        if (afk != (Boolean) user.properties.get(Users.AFK)) {
            if (!afk)
                world.send(new String[]{"server", "You are no longer Away From Keyboard (AFK)."}, user);
            else
                world.send(new String[]{"server", "You are now Away From Keyboard (AFK)."}, user);

            user.properties.put(Users.AFK, afk);
            world.send(new String[]{"uotls", user.getName(), "afk:" + Boolean.parseBoolean(params[0])}, room.getChannellList());
        }
    }

}

/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.dispatcher;

import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;

/**
 *
 * @author Mystical
 */
public interface IRequest {

    void process(String[] params, User user, World world, Room room) throws RequestException;
}

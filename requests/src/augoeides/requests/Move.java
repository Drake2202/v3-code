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
public class Move implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        int x = Integer.parseInt(params[0]);
        int y = Integer.parseInt(params[1]);
        int speed = Integer.parseInt(params[2]);

        user.properties.put(Users.TX, x);
        user.properties.put(Users.TY, y);

        StringBuilder sb = new StringBuilder();

        sb.append("tx:");
        sb.append(x);
        sb.append(",ty:");
        sb.append(y);
        sb.append(",sp:");
        sb.append(speed);
        sb.append(",strFrame:");
        sb.append(user.properties.get(Users.FRAME));

        world.sendToRoomButOne(new String[]{"uotls", user.getName(), sb.toString()}, user, room);
    }

}

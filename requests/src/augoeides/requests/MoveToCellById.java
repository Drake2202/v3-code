/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

import augoeides.db.objects.Cell;
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
public class MoveToCellById implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        Cell cell = world.areas.get(room.getName().split("-")[0]).cells.get(Integer.parseInt(params[0]));

        if (cell.getFrame().equals("Enter0") && (Integer) user.properties.get(Users.PVP_TEAM) != 0
                || cell.getFrame().equals("Enter1") && (Integer) user.properties.get(Users.PVP_TEAM) != 1)
            return;

        new MoveToCell().process(new String[]{cell.getFrame(), cell.getPad()}, user, world, room);
        world.send(new String[]{"mtcid", params[0]}, user);
    }

}

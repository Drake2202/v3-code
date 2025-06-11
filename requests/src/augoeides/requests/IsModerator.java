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
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class IsModerator implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        User target = world.zone.getUserByName(params[0].toLowerCase());
        if(target == null) return;

        JSONObject isModerator = new JSONObject();
        isModerator.put("cmd", "isModerator");
        isModerator.put("val", (target.isAdmin() || target.isModerator()));
        isModerator.put("unm", target.properties.get(Users.USERNAME));

        world.send(isModerator, user);
    }

}

/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests.guild;

import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class GuildDeclineInvite implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        User client = world.zone.getUserByName(params[2].toLowerCase());
        if (client == null) return;

        JSONObject gd = new JSONObject();
        gd.put("cmd", "gd");
        gd.put("unm", user.getName());

        world.send(new String[]{"server", "You declined the invitation."}, user);
        world.send(gd, client);
    }

}

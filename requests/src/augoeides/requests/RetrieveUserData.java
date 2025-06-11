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
import it.gotoandplay.smartfoxserver.extensions.ExtensionHelper;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class RetrieveUserData implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        ExtensionHelper helper = ExtensionHelper.instance();
        int userId = Integer.parseInt(params[0]);

        JSONObject data = world.users.getUserData(userId, false);
        JSONObject iud = new JSONObject();

        User otherUser = helper.getUserById(userId);

        if (otherUser == null) return;
        
        iud.put("cmd", "initUserData");
        iud.put("data", data);
        iud.put("strFrame", otherUser.properties.get(Users.FRAME));
        iud.put("strPad", otherUser.properties.get(Users.PAD));
        iud.put("uid", userId);
        world.send(iud, user);
    }

}

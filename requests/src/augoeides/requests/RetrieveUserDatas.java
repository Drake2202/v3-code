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
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class RetrieveUserDatas implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        JSONObject iud = new JSONObject();
        iud.put("cmd", "initUserDatas");

        JSONArray a = new JSONArray();
        for (String userId : params) {
            User userObj = ExtensionHelper.instance().getUserById(Integer.parseInt(userId));
            if (userObj == null)  continue;
            
            boolean addInfo = (user.getUserId() == Integer.parseInt(userId));
            JSONObject userData = world.users.getUserData(Integer.parseInt(userId), addInfo);
            JSONObject userInfo = new JSONObject();

            userInfo.put("uid", Integer.parseInt(userId));
            userInfo.put("strFrame", user.properties.get(Users.FRAME));
            userInfo.put("strPad", user.properties.get(Users.PAD));
            userInfo.put("data", userData);

            a.add(userInfo);
        }

        iud.put("a", a);
        world.send(iud, user);
    }

}

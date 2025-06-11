/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

import augoeides.avatars.State;
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
public class MoveToCell implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        String frame = params[0];
        String pad = params[1];

        user.properties.put(Users.FRAME, frame);
        user.properties.put(Users.PAD, pad);
        user.properties.put(Users.TX, 0);
        user.properties.put(Users.TY, 0);
        
        State state = (State) user.properties.get(Users.USER_STATE);
        
        if (!state.isNeutral()) {
            world.users.regen(user);

            JSONObject ct = new JSONObject();
            JSONObject p = new JSONObject();
            JSONObject pInfo = new JSONObject();

            p.put(user.getName(), state.getData());

            ct.put("cmd", "ct");
            ct.put("p", p);

            world.sendToRoom(ct, user, room);
        }

        StringBuilder sb = new StringBuilder();

        sb.append("strPad:");
        sb.append(user.properties.get(Users.PAD));
        sb.append(",tx:");
        sb.append(user.properties.get(Users.TX));
        sb.append(",strFrame:");
        sb.append(user.properties.get(Users.FRAME));
        sb.append(",ty:");
        sb.append(user.properties.get(Users.TX));

        world.sendToRoomButOne(new String[]{"uotls", user.getName(), sb.toString()}, user, room);

        user.properties.put(Users.LAST_AREA, room.getName() + "|" + frame + "|" + pad);
    }

}

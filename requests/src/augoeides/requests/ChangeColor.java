/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

import augoeides.db.objects.Hair;
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
public class ChangeColor implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        JSONObject cc = new JSONObject();

        int skinColor = Integer.parseInt(params[0]);
        int hairColor = Integer.parseInt(params[1]);
        int eyeColor = Integer.parseInt(params[2]);
        int hairId = Integer.parseInt(params[3]);

        Hair hair = world.hairs.get(hairId);

        world.db.jdbc.run("UPDATE users SET ColorSkin = ?, ColorHair = ?, ColorEye = ?, HairID = ? WHERE id = ?", Integer.toHexString(skinColor & 0xffffff).toUpperCase(),
                Integer.toHexString(hairColor & 0xffffff).toUpperCase(), Integer.toHexString(eyeColor & 0xffffff).toUpperCase(), hairId, user.properties.get(Users.DATABASE_ID));

        cc.put("uid", user.getUserId());
        cc.put("cmd", "changeColor");
        cc.put("HairID", hairId);
        cc.put("strHairName", hair.getName());
        cc.put("strHairFilename", hair.getFile());
        cc.put("intColorSkin", skinColor);
        cc.put("intColorHair", hairColor);
        cc.put("intColorEye", eyeColor);

        world.sendToRoomButOne(cc, user, room);

        user.properties.put(Users.HAIR_ID, hairId);
        user.properties.put(Users.COLOR_HAIR, hairColor);
        user.properties.put(Users.COLOR_SKIN, skinColor);
        user.properties.put(Users.COLOR_EYE, eyeColor);
    }

}

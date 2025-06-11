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
public class ChangeArmorColor implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        int base = Integer.parseInt(params[0]);
        int trim = Integer.parseInt(params[1]);
        int accessory = Integer.parseInt(params[2]);

        world.db.jdbc.run("UPDATE users SET ColorBase = ?, ColorAccessory = ?, ColorTrim = ? WHERE id = ?", Integer.toHexString(base & 0xffffff).toUpperCase(),
                Integer.toHexString(accessory & 0xffffff).toUpperCase(), Integer.toHexString(trim & 0xffffff).toUpperCase(), user.properties.get(Users.DATABASE_ID));
        JSONObject cac = new JSONObject();
        cac.put("uid", user.getUserId());
        cac.put("cmd", "changeArmorColor");
        cac.put("intColorBase", base);
        cac.put("intColorTrim", trim);
        cac.put("intColorAccessory", accessory);
        world.sendToRoomButOne(cac, user, room);

        user.properties.put(Users.COLOR_TRIM, trim);
        user.properties.put(Users.COLOR_BASE, base);
        user.properties.put(Users.COLOR_ACCESSORY, accessory);
    }

}

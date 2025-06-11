/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

import augoeides.db.objects.House;
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
public class HouseSave implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        world.db.jdbc.run("UPDATE users SET HouseInfo = ? WHERE id = ?", params[0], user.properties.get(Users.DATABASE_ID));
        Room house = world.zone.getRoomByName("house-" + user.properties.get(Users.DATABASE_ID));

        if (house == null) return;
        House houseObj = (House) world.areas.get(house.getName());
        if(houseObj == null) return;
        houseObj.setHouseInfo(params[0]);
    }

}

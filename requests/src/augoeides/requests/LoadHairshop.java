/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

import augoeides.db.objects.Hair;
import augoeides.db.objects.Hairshop;
import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.Users;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.Set;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class LoadHairshop implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        int shopId = Integer.parseInt(params[0]);

        JSONObject hairshop = new JSONObject();
        JSONArray arrHairs = new JSONArray();
        
        if(!world.hairshops.containsKey(shopId)) return;
        Hairshop hairshopObj = world.hairshops.get(shopId);
        

        Set<Integer> shopItems = hairshopObj.getShopItems((String) user.properties.get(Users.GENDER));
        for (int hairId : shopItems) {
            Hair hairObj = world.hairs.get(hairId);
            if (hairObj != null) {
                JSONObject hair = new JSONObject();
                hair.put("sFile", hairObj.getFile());
                hair.put("HairID", hairId);
                hair.put("sName", hairObj.getName());
                hair.put("sGen", hairObj.getGender());
                arrHairs.add(hair);
            }
        }

        hairshop.put("HairShopID", shopId);
        hairshop.put("cmd", "loadHairShop");

        hairshop.put("hair", arrHairs);
        world.send(hairshop, user);
    }

}

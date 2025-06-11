/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

import augoeides.db.objects.Enhancement;
import augoeides.db.objects.Item;
import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.Users;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.Arrays;
import java.util.List;
import jdbchelper.QueryResult;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class LoadBank implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        List<String> types = Arrays.asList(params);

        JSONObject lb = new JSONObject();
        JSONArray items = new JSONArray();

        QueryResult result = world.db.jdbc.query("SELECT * FROM users_items WHERE Bank = 1 AND UserID = ?", user.properties.get(Users.DATABASE_ID));
        while (result.next()) {
            int itemId = result.getInt("ItemID");
            int enhId = result.getInt("EnhID");
            Item item = world.items.get(itemId);
            Enhancement enhancement = world.enhancements.get(enhId);

            if (types.contains(item.getType())) {
                JSONObject itemObj = Item.getItemJSON(item, enhancement);
                itemObj.put("bBank", "1");
                itemObj.put("CharItemID", result.getString("id"));
                itemObj.put("iQty", result.getString("quantity"));
                items.add(itemObj);
            }
        }
        result.close();

        lb.put("cmd", "loadBank");
        lb.put("items", items);
        lb.put("bitSuccess", 1);

        world.send(lb, user);
    }

}

/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests.guild;

import augoeides.db.objects.Item;
import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.Users;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import jdbchelper.QueryResult;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class GetInventory implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        int guildId = (Integer) user.properties.get(Users.GUILD_ID);

        JSONObject guildinv = new JSONObject();
        JSONObject guildInventory = new JSONObject();
        QueryResult result = world.db.jdbc.query("SELECT guilds_inventory.*, users.Name FROM guilds_inventory LEFT JOIN users ON UserId = users.id WHERE GuildID = ?", guildId);
        while (result.next()) {
            Item item = world.items.get(result.getInt("ItemID"));
            String username = result.getString("Name");
            if (guildInventory.containsKey(username)) {
                JSONObject items = guildInventory.getJSONObject(username);
                items.put(String.valueOf(item.getId()), Item.getItemJSON(item));
            } else {
                JSONObject items = new JSONObject();
                items.put(String.valueOf(item.getId()), Item.getItemJSON(item));
                guildInventory.put(username, items);
            }
        }
        result.close();

        guildinv.put("cmd", "guildinv");
        guildinv.put("guildInventory", guildInventory);
        world.send(guildinv, user);
    }

}

/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.Users;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import jdbchelper.JdbcException;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class BuyBagSlots implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        int slotsToPurchase = Integer.parseInt(params[0]);
        int bagSlots = (Integer) user.properties.get(Users.SLOTS_BAG) + slotsToPurchase;
        int totalCost = (slotsToPurchase * 200);

        if (bagSlots > 250)
            throw new RequestException("You have already purchased the maximum amount!");

        world.db.jdbc.beginTransaction();
        try {
            int coins = world.db.jdbc.queryForInt("SELECT Coins FROM users WHERE id = ? FOR UPDATE", user.properties.get(Users.DATABASE_ID));
            int coinsLeft = (coins - totalCost);
            if (coinsLeft >= 0) {

                world.db.jdbc.run("UPDATE users SET SlotsBag = ?, Coins = ? WHERE id = ?", bagSlots, coinsLeft, user.properties.get(Users.DATABASE_ID));

                user.properties.put(Users.SLOTS_BAG, bagSlots);

                JSONObject object = new JSONObject();
                object.put("cmd", "buyBagSlots");
                object.put("iSlots", slotsToPurchase);
                object.put("bitSuccess", "1");

                world.send(object, user);
            } else
                throw new RequestException("You don't have enough coins!");
        } catch (JdbcException je) {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.rollbackTransaction();
            SmartFoxServer.log.severe("Error in buy bag slots transaction: " + je.getMessage());
        } finally {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.commitTransaction();
        }
    }

}

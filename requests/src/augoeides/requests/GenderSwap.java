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
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import jdbchelper.JdbcException;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class GenderSwap implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        Hair hair;
        String newGender;

        world.db.jdbc.beginTransaction();
        try {
            int coins = world.db.jdbc.queryForInt("SELECT Coins FROM users WHERE id = ? FOR UPDATE", user.properties.get(Users.DATABASE_ID));
            int deltaCoins = coins - 1000;
            if (deltaCoins > 0) {
                String gender = (String) user.properties.get(Users.GENDER);

                if (gender.equals("M")) {
                    hair = world.hairs.get(83);
                    newGender = "F";
                } else {
                    hair = world.hairs.get(52);
                    newGender = "M";
                }

                world.db.jdbc.run("UPDATE users SET Gender = ?, Coins = ?, HairID = ? WHERE id = ?", newGender, deltaCoins, hair.getId(), user.properties.get(Users.DATABASE_ID));

                user.properties.put(Users.GENDER, newGender);
                user.properties.put(Users.HAIR_ID, hair.getId());

                JSONObject genderSwap = new JSONObject();
                genderSwap.put("uid", user.getUserId());
                genderSwap.put("strHairFilename", hair.getFile());
                genderSwap.put("cmd", "genderSwap");
                genderSwap.put("bitSuccess", 1);
                genderSwap.put("HairID", hair.getId());
                genderSwap.put("strHairName", hair.getName());
                genderSwap.put("gender", newGender);
                genderSwap.put("intCoins", 1000);

                world.sendToRoom(genderSwap, user, room);
            } else {
                world.db.jdbc.rollbackTransaction();
                throw new RequestException("You don't have enough coins!");
            }
        } catch (JdbcException je) {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.rollbackTransaction();
            SmartFoxServer.log.severe("Error in gender swap transaction: " + je.getMessage());
        } finally {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.commitTransaction();
        }
    }

}

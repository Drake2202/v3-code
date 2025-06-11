/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

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
public class ServerUseItem implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        String option = params[0];
        if (option.equals("+")) {
            int itemid = Integer.parseInt(params[1]);

            Item item = world.items.get(itemid);
            if (item != null && item.getType().equals("ServerUse"))
                if (item.getLink().contains("::")) {
                    String[] itemParams = item.getLink().split("::");
                    String type = itemParams[0];
                    int minutes = Integer.parseInt(itemParams[1]);
                    boolean showShop = Boolean.parseBoolean(itemParams[2]);
                    if (world.users.turnInItem(user, itemid, 1)) {
                        JSONObject boost = new JSONObject();

                        boost.put("cmd", type);
                        boost.put("bShowShop", showShop);
                        boost.put("op", option);

                        QueryResult boosts = world.db.jdbc.query("SELECT ExpBoostExpire, CpBoostExpire, GoldBoostExpire, RepBoostExpire FROM users WHERE id = ?", user.properties.get(Users.DATABASE_ID));
                        if (boosts.next()) {
                            if (type.equals("xpboost")) {
                                int xpMinLeft = world.db.jdbc.queryForInt("SELECT TIMESTAMPDIFF(MINUTE, NOW(), ?)", boosts.getString("ExpBoostExpire"));
                                xpMinLeft = xpMinLeft >= 0 ? xpMinLeft : 0;
                                world.db.jdbc.run("UPDATE users SET ExpBoostExpire = DATE_ADD(NOW(), INTERVAL ? MINUTE) WHERE id = ?", (minutes + xpMinLeft), user.properties.get(Users.DATABASE_ID));
                                user.properties.put(Users.BOOST_XP, true);
                                boost.put("iSecsLeft", ((minutes + xpMinLeft) * 60));
                            } else if (type.equals("gboost")) {
                                int goldMinLeft = world.db.jdbc.queryForInt("SELECT TIMESTAMPDIFF(MINUTE,NOW(),?)", boosts.getString("GoldBoostExpire"));
                                goldMinLeft = goldMinLeft >= 0 ? goldMinLeft : 0;
                                world.db.jdbc.run("UPDATE users SET GoldBoostExpire = DATE_ADD(NOW(), INTERVAL ? MINUTE) WHERE id = ?", (minutes + goldMinLeft), user.properties.get(Users.DATABASE_ID));
                                user.properties.put(Users.BOOST_GOLD, true);
                                boost.put("iSecsLeft", ((minutes + goldMinLeft) * 60));
                            } else if (type.equals("cpboost")) {
                                int cpMinLeft = world.db.jdbc.queryForInt("SELECT TIMESTAMPDIFF(MINUTE, NOW(), ?)", boosts.getString("CpBoostExpire"));
                                cpMinLeft = cpMinLeft >= 0 ? cpMinLeft : 0;
                                world.db.jdbc.run("UPDATE users SET CpBoostExpire = DATE_ADD(NOW(), INTERVAL ? MINUTE) WHERE id = ?", (minutes + cpMinLeft), user.properties.get(Users.DATABASE_ID));
                                user.properties.put(Users.BOOST_CP, true);
                                boost.put("iSecsLeft", ((minutes + cpMinLeft) * 60));
                            } else if (type.equals("repboost")) {
                                int repMinLeft = world.db.jdbc.queryForInt("SELECT TIMESTAMPDIFF(MINUTE, NOW() , ?)", boosts.getString("RepBoostExpire"));
                                repMinLeft = repMinLeft >= 0 ? repMinLeft : 0;
                                world.db.jdbc.run("UPDATE users SET RepBoostExpire = DATE_ADD(NOW(), INTERVAL ? MINUTE) WHERE id = ?", (minutes + repMinLeft), user.properties.get(Users.DATABASE_ID));
                                user.properties.put(Users.BOOST_REP, true);
                                boost.put("iSecsLeft", ((minutes + repMinLeft) * 60));
                            }
                            world.send(boost, user);
                        }
                        boosts.close();
                    } else
                        // User may have clicked "Use" a few times due to slow internet connection
                        world.users.log(user, "Suspicious Request [ServerUseItem]", "Failed to pass turn-in validation, might be a duplicate request.");
                } else
                    throw new RequestException("This feature is not yet available.", "server");
        } else {
            String type = params[1];

            JSONObject boost = new JSONObject();

            boost.put("cmd", type);
            boost.put("op", option);

            if (type.equals("xpboost"))
                user.properties.put(Users.BOOST_XP, false);
            else if (type.equals("gboost"))
                user.properties.put(Users.BOOST_GOLD, false);
            else if (type.equals("cpboost"))
                user.properties.put(Users.BOOST_CP, false);
            else if (type.equals("repboost"))
                user.properties.put(Users.BOOST_REP, false);

            world.send(boost, user);
        }
    }

}

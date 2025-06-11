/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.PartyInfo;
import augoeides.world.Users;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.Iterator;
import jdbchelper.JdbcException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Mystical
 */
public class Message implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        if ((Integer) user.properties.get(Users.PERMAMUTE_FLAG) > 0)
            throw new RequestException("You are muted! Chat privileges have been permanently revoked.");
        else if (world.users.isMute(user)) {
            int seconds = world.users.getMuteTimeInSeconds(user);
            throw new RequestException(world.users.getMuteMessage(seconds));
        }

        String channel = params[1];
        String message = params[0];

        if (message.length() > 150)
            message = message.substring(0, 150);

        message = StringUtils.replaceEach(message, new String[]{"#038:", "&", "\"", "<", ">"}, new String[]{"&", "&amp;", "&quot;", "&lt;", "&gt;"});

        if (channel.equals("world")) {
            world.db.jdbc.beginTransaction();
            try {
                int coins = world.db.jdbc.queryForInt("SELECT Coins FROM users WHERE id = ? FOR UPDATE", user.properties.get(Users.DATABASE_ID));
                if (coins >= 100) {
                    JSONObject sell = new JSONObject();
                    sell.put("cmd", "sellItem");
                    sell.put("intAmount", -100);
                    sell.put("CharItemID", user.hashCode());
                    sell.put("bCoins", 1);

                    world.db.jdbc.run("UPDATE users SET Coins = (Coins - 100) WHERE id = ?", user.properties.get(Users.DATABASE_ID));

                    world.send(sell, user);
                    world.sendToUsers(new String[]{"chatm", "world~" + message, user.getName(), String.valueOf(1)});
                } else {
                    world.db.jdbc.rollbackTransaction();
                    throw new RequestException("You need at least 100ACs to send a message to world channel.", "server");
                }
            } catch (JdbcException je) {
                if (world.db.jdbc.isInTransaction())
                    world.db.jdbc.rollbackTransaction();
                SmartFoxServer.log.severe("Error in world message transaction: " + je.getMessage());
            } finally {
                if (world.db.jdbc.isInTransaction())
                    world.db.jdbc.commitTransaction();
            }
        } else if (channel.equals("party")) {
            int partyId = (Integer) user.properties.get(Users.PARTY_ID);
            if (partyId < 0)
                throw new RequestException("You are not in a party.", "server");
            PartyInfo pi = world.parties.getPartyInfo(partyId);
            world.send(new String[]{"chatm", "party~" + message, user.getName(), String.valueOf(1)}, pi.getChannelList());
        } else if (channel.equals("guild"))
            if ((Integer) user.properties.get(Users.GUILD_ID) > 0) {
                JSONObject guildData = (JSONObject) user.properties.get(Users.GUILD);
                JSONArray members = (JSONArray) guildData.get("ul");
                if (members != null && members.size() > 0)
                    for (Iterator<JSONObject> it = members.iterator(); it.hasNext();) {
                        JSONObject member = it.next();
                        User client = world.zone.getUserByName(member.get("userName").toString().toLowerCase());
                        if (client != null)
                            world.send(new String[]{"chatm", "guild~" + message, user.getName(), String.valueOf(1)}, client);
                    }
            } else
                throw new RequestException("You are not in a guild.", "server");
        else {
            int access = (Integer) user.properties.get(Users.ACCESS);

            switch (access) {
                case 60:
                    channel = "admin";
                    break;
                case 40:
                    channel = "mod";
                    break;
                case 4:
                    channel = "ps";
                    break;
                case 2:
                    channel = "vip";
                    break;
                default:
                    channel = "zone";
                    break;
            }

            world.sendToRoom(new String[]{"chatm", channel + "~" + message, user.getName(), String.valueOf(room.getId())}, user, room);
        }

        world.applyFloodFilter(user, message);
    }
}

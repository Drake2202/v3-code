/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

import augoeides.config.ConfigData;
import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.Users;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.Set;
import jdbchelper.QueryResult;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class AddFriend implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        User client = world.zone.getUserByName(params[0].toLowerCase());
        if(client == null) return;
        Set<Integer> userRequestedFriends = (Set<Integer>) user.properties.get(Users.REQUESTED_FRIEND);

        if (userRequestedFriends.contains(client.getUserId())) {
            userRequestedFriends.remove(Integer.valueOf(client.getUserId()));
            addFriend(user, client, world);
            addFriend(client, user, world);
        } else {
            world.users.kick(user);
            world.users.log(user, "Packet Edit [AddFriend]", "Forcing add friend.");
        }
    }

    private void addFriend(User userObj1, User userObj2, World world) throws RequestException {

        int userID1 = (Integer) userObj1.properties.get(Users.DATABASE_ID);
        int userID2 = (Integer) userObj2.properties.get(Users.DATABASE_ID);

        JSONObject addFriend = new JSONObject();
        addFriend.put("cmd", "addFriend");

        QueryResult result = world.db.jdbc.query("SELECT * FROM users_friends WHERE UserID = ? AND FriendID = ?", userID1, userID2);

        if (result.next()) {
            result.close();
            throw new RequestException(userObj2.getName() + " was already added to your friends list.");
        }
        result.close();

        world.db.jdbc.run("INSERT INTO users_friends (UserID, FriendID) VALUES (?, ?)", userID1, userID2);

        JSONObject friendInfo = new JSONObject();
        friendInfo.put("iLvl", (Integer) userObj2.properties.get(Users.LEVEL));
        friendInfo.put("ID", userObj2.properties.get(Users.DATABASE_ID));
        friendInfo.put("sName", userObj2.properties.get(Users.USERNAME));
        friendInfo.put("sServer", ConfigData.SERVER_NAME);
        addFriend.put("friend", friendInfo);

        world.send(addFriend, userObj1);
        world.send(new String[]{"server", userObj2.getName() + " has been added to your friends list."}, userObj1);
    }

}

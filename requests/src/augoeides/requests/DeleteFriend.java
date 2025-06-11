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
public class DeleteFriend implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        String friendName = params[1].toLowerCase();
        int friendId = world.db.jdbc.queryForInt("SELECT id FROM users WHERE Name = ?", friendName);

        deleteFriend((Integer) user.properties.get(Users.DATABASE_ID), friendId, world);
        deleteFriend(friendId, (Integer) user.properties.get(Users.DATABASE_ID), world);

        JSONObject deleteFriend = new JSONObject();
        deleteFriend.put("cmd", "deleteFriend");
        deleteFriend.put("ID", friendId);

        world.send(deleteFriend, user);

        User friend = world.zone.getUserByName(friendName);
        if (friend == null) return;
        
        deleteFriend.put("ID", (Integer) user.properties.get(Users.DATABASE_ID));
        world.send(deleteFriend, friend);
    }

    private void deleteFriend(int fromUser, int deleteId, World world) {
        world.db.jdbc.run("DELETE FROM users_friends WHERE UserID = ? AND FriendID = ?", fromUser, deleteId);
    }
}

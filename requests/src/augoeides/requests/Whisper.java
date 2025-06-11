/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

import augoeides.aqw.Settings;
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
public class Whisper implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        if ((Integer) user.properties.get(Users.PERMAMUTE_FLAG) > 0)
            throw new RequestException("You are muted! Chat privileges have been permanently revoked.");
        else if (world.users.isMute(user)) {
            int seconds = world.users.getMuteTimeInSeconds(user);
            throw new RequestException(world.users.getMuteMessage(seconds));
        }

        String message = params[0];
        String username = params[1].toLowerCase();
        User client = world.zone.getUserByName(username);

        if (client == null) throw new RequestException("Player \"" + username + "\" could not be found.");
        if (!Settings.isAllowed(Settings.WHISPER, user, client)) throw new RequestException("Player " + client.getName() + " is not accepting PMs at this time.");

        String[] msg = {"whisper", message, user.getName(), username, "0"};
        world.send(msg, user);
        world.send(msg, client);

        world.applyFloodFilter(user, message);
    }

}

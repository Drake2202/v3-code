/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

import augoeides.avatars.State;
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
public class Rest implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        State state = (State) user.properties.get(Users.USER_STATE);

        if (state.isNeutral() && (state.getHealth() < state.getMaxHealth() || 
                state.getMana() < state.getMaxMana())) {
            
            state.increaseHealthByPercent(0.1);
            state.increaseManaByPercent(0.1);

            world.users.sendUotls(user, true, false, true, false, false, false);
        }
    }

}

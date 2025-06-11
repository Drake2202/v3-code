/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

import augoeides.db.objects.Quest;
import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.Users;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.Set;

/**
 *
 * @author Mystical
 */
public class AcceptQuest implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        Set<Integer> quests = (Set<Integer>) user.properties.get(Users.QUESTS);
        int questId = Integer.parseInt(params[0]);
        quests.add(questId);

        Quest quest = world.quests.get(questId);
        if (!quest.locations.isEmpty()) {
            int mapId = world.areas.get(room.getName().split("-")[0]).getId();
            if (!quest.locations.contains(mapId))
                world.users.log(user, "Invalid Quest Accept", "Quest accept triggered at different location.");
        }
    }

}

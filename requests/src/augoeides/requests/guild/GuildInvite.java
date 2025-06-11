/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests.guild;

import augoeides.aqw.Settings;
import augoeides.avatars.State;
import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.Users;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.Iterator;
import java.util.Set;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class GuildInvite implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        if ((Integer) user.properties.get(Users.GUILD_RANK) < 2) throw new RequestException("Invalid /gi request.");

        String username = params[1].toLowerCase();
        User client = world.zone.getUserByName(username);
        if (client == null) throw new RequestException("Player \"" + username + "\" could not be found.");

        int clientGuildID = (Integer) client.properties.get(Users.GUILD_ID);
        int userGuildID = (Integer) user.properties.get(Users.GUILD_ID);

        if (userGuildID <= 0) throw new RequestException("You are not in a guild!");
        if (clientGuildID > 0) throw new RequestException(client.getName() + " already belongs to a guild.");
        if (!Settings.isAllowed(Settings.GUILD, user, client)) throw new RequestException("Player " + client.getName() + " is not accepting guild invites.", "server");
        if (((State) client.properties.get(Users.USER_STATE)).onCombat()) throw new RequestException(client.getName() + " is currently busy.");

        JSONObject guildData = (JSONObject) user.properties.get(Users.GUILD);
        JSONArray members = (JSONArray) guildData.get("ul");

        if (members.size() > 0)
            for (Iterator<JSONObject> iterator = members.iterator(); iterator.hasNext();) {
                JSONObject member = iterator.next();
                if (member.get("ID").toString().equals(client.properties.get(Users.DATABASE_ID).toString()))
                    throw new RequestException(client.getName() + " is already in your guild!");
            }

        if (members.size() >= (Integer) guildData.get("MaxMembers")) throw new RequestException("Your guild has reached the maximum number of members.");

        JSONObject gi = new JSONObject();

        gi.put("cmd", params[0]);
        gi.put("owner", user.getName());
        gi.put("gName", guildData.get("Name"));
        gi.put("guildID", userGuildID);

        Set<Integer> requestedGuild = (Set<Integer>) client.properties.get(Users.REQUESTED_GUILD);

        requestedGuild.add(userGuildID);

        world.send(new String[]{"server", "You have invited " + client.getName() + " to join your guild."}, user);
        world.send(gi, client);
    }

}

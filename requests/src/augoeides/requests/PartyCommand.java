/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.requests.party.PartyAccept;
import augoeides.requests.party.PartyAcceptSummon;
import augoeides.requests.party.PartyDecline;
import augoeides.requests.party.PartyDeclineSummon;
import augoeides.requests.party.PartyInvite;
import augoeides.requests.party.PartyKick;
import augoeides.requests.party.PartyLeave;
import augoeides.requests.party.PartyPromote;
import augoeides.requests.party.PartySummon;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;

/**
 *
 * @author Mystical
 */
public class PartyCommand implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        if (params[0].equals("pi")) // Party Invite
            new PartyInvite().process(params, user, world, room);
        else if (params[0].equals("pk")) //Party Kick
            new PartyKick().process(params, user, world, room);
        else if (params[0].equals("pl")) //Party Leave
            new PartyLeave().process(params, user, world, room);
        else if (params[0].equals("ps")) //Party Summon
            new PartySummon().process(params, user, world, room);
        else if (params[0].equals("psa")) //Acceot Party Summon
            new PartyAcceptSummon().process(params, user, world, room);
        else if (params[0].equals("psd")) //Decline Party Summon
            new PartyDeclineSummon().process(params, user, world, room);
        else if (params[0].equals("pp")) //Party Promote
            new PartyPromote().process(params, user, world, room);
        else if (params[0].equals("pa")) //Party Decline
            new PartyAccept().process(params, user, world, room);
        else if (params[0].equals("pd")) //Party Accept
            new PartyDecline().process(params, user, world, room);
    }

}

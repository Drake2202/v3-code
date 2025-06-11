/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.requests.guild.AddBuilding;
import augoeides.requests.guild.AddConnection;
import augoeides.requests.guild.AddFrame;
import augoeides.requests.guild.BuyPlot;
import augoeides.requests.guild.GetInterior;
import augoeides.requests.guild.GetInventory;
import augoeides.requests.guild.GetShop;
import augoeides.requests.guild.GuildAccept;
import augoeides.requests.guild.GuildBuyItem;
import augoeides.requests.guild.GuildCreate;
import augoeides.requests.guild.GuildDeclineInvite;
import augoeides.requests.guild.GuildDemote;
import augoeides.requests.guild.GuildInvite;
import augoeides.requests.guild.GuildMOTD;
import augoeides.requests.guild.GuildPromote;
import augoeides.requests.guild.GuildRemove;
import augoeides.requests.guild.GuildRename;
import augoeides.requests.guild.GuildSellItem;
import augoeides.requests.guild.GuildSlots;
import augoeides.requests.guild.RemoveBuilding;
import augoeides.requests.guild.RemoveConnection;
import augoeides.requests.guild.SaveInterior;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;

/**
 *
 * @author Mystical
 */
public class GuildCommand implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        if (params[0].equals("gc"))
            new GuildCreate().process(params, user, world, room);
        else if (params[0].equals("gi"))
            new GuildInvite().process(params, user, world, room);
        else if (params[0].equals("ga"))
            new GuildAccept().process(params, user, world, room);
        else if (params[0].equals("gr"))
            new GuildRemove().process(params, user, world, room);
        else if (params[0].equals("gdi"))
            new GuildDeclineInvite().process(params, user, world, room);
        else if (params[0].equals("rename"))
            new GuildRename().process(params, user, world, room);
        else if (params[0].equals("gp"))
            new GuildPromote().process(params, user, world, room);
        else if (params[0].equals("gd"))
            new GuildDemote().process(params, user, world, room);
        else if (params[0].equals("motd"))
            new GuildMOTD().process(params, user, world, room);
        else if (params[0].equals("slots"))
            new GuildSlots().process(params, user, world, room);
        else if (params[0].equals("getInterior"))
            new GetInterior().process(params, user, world, room);
        else if (params[0].equals("buyplot"))
            new BuyPlot().process(params, user, world, room);
        else if (params[0].equals("getInv"))
            new GetInventory().process(params, user, world, room);
        else if (params[0].equals("getShop"))
            new GetShop().process(params, user, world, room);
        else if (params[0].equals("saveInt"))
            new SaveInterior().process(params, user, world, room);
        else if (params[0].equals("addFrame"))
            new AddFrame().process(params, user, world, room);
        else if (params[0].equals("addBuilding"))
            new AddBuilding().process(params, user, world, room);
        else if (params[0].equals("removeBuilding"))
            new RemoveBuilding().process(params, user, world, room);
        else if (params[0].equals("buyItem"))
            new GuildBuyItem().process(params, user, world, room);
        else if (params[0].equals("sellItem"))
            new GuildSellItem().process(params, user, world, room);
        else if (params[0].equals("addConnection"))
            new AddConnection().process(params, user, world, room);
        else if (params[0].equals("removeConnection"))
            new RemoveConnection().process(params, user, world, room);
    }

}

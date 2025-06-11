/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

import augoeides.aqw.Settings;
import augoeides.avatars.State;
import augoeides.db.objects.Hall;
import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.tasks.Restart;
import augoeides.tasks.Shutdown;
import augoeides.world.Rooms;
import augoeides.world.Users;
import augoeides.world.World;
import augoeides.world.stats.Stats;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import it.gotoandplay.smartfoxserver.extensions.ExtensionHelper;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class UserCommand implements IRequest {

    public static final String JOIN_ROOM = "tfer";
    public static final String LIST_USERS = "who";
    public static final String IGNORE_LIST = "ignoreList";
    public static final String CHANGE_PREFERENCES = "uopref";
    public static final String GOTO = "goto";
    public static final String MUTE = "mute";
    public static final String UNMUTE = "unmute";
    public static final String SHUTDOWN = "shutdown";
    public static final String RESTART = "restart";
    public static final String SHUTDOWN_NOW = "shutdownnow";
    public static final String RESTART_NOW = "restartnow";
    public static final String HELP = "help";
    public static final String KICK = "kick";
    public static final String ITEM = "item";
    public static final String CLEAR = "clear";
    public static final String PULL = "pull";
    public static final String BAN = "ban";
    public static final String RATES = "rates";
    public static final String LEVEL = "level";
    public static final String ADD_GOLD = "addgold";
    public static final String ADD_CLASSPOINTS = "addcp";
    public static final String ADD_XP = "addxp";
    public static final String ADD_COIN = "addcoin";
    public static final String YELL = "iay";
    public static final String EMOTE_ALL = "emoteall";

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        String cmd = params[0];

        if (cmd.equals(UserCommand.JOIN_ROOM)) {
            String roomName = params[2].toLowerCase().replaceAll("battleon", "faroff");
            if (roomName.contains("guildhall")) {
                int guildId = (Integer) user.properties.get(Users.GUILD_ID);

                if (guildId <= 0) throw new RequestException("You are not in a guild!");

                JSONObject guildData = (JSONObject) user.properties.get(Users.GUILD);

                String guildName = guildData.getString("Name");

                if (!world.areas.containsKey(guildName)) {
                    Hall hall = new Hall(guildId);
                    world.areas.put(guildName, hall);
                }

                world.rooms.basicRoomJoin(user, guildName);
            } else
                world.rooms.basicRoomJoin(user, roomName);
        } else if (cmd.equals(UserCommand.LIST_USERS)) {
            JSONObject who = new JSONObject();
            JSONObject usersObj = new JSONObject();

            for (User userInRoom : room.getAllUsers()) {
                JSONObject uObj = new JSONObject();
                uObj.put("iLvl", userInRoom.properties.get(Users.LEVEL));
                uObj.put("ID", userInRoom.properties.get(Users.DATABASE_ID));
                uObj.put("sName", userInRoom.properties.get(Users.USERNAME));
                uObj.put("sClass", userInRoom.properties.get(Users.CLASS_NAME));
                usersObj.put(String.valueOf(userInRoom.getUserId()), uObj);
            }

            who.put("cmd", "who");
            who.put("users", usersObj);
            world.send(who, user);
        } else if (cmd.equals(UserCommand.GOTO)) {
            String username = params[1].toLowerCase();
            User client = world.zone.getUserByName(username);

            if (client == null) throw new RequestException("Player \"" + username + "\" could not be found.");
            if (((State) client.properties.get(Users.USER_STATE)).onCombat()) throw new RequestException(client.getName() + " is currently busy.");
            if (!Settings.isAllowed(Settings.GOTO, user, client)) throw new RequestException(client.getName() + " is ignoring goto requests.", "server");

            Room roomToJoin = world.zone.getRoom(client.getRoom());

            if (roomToJoin == null) return;
            if (world.rooms.checkLimits(roomToJoin, user) != Rooms.ROOM_OK) return;

            String userFrame = (String) client.properties.get(Users.FRAME);
            String userPad = (String) client.properties.get(Users.PAD);

            world.rooms.joinRoom(roomToJoin, user, userFrame, userPad);
        } else if (cmd.equals(UserCommand.IGNORE_LIST)) {
        } else if (cmd.equals(UserCommand.CHANGE_PREFERENCES))
            world.users.changePreferences(user, params[1], Boolean.parseBoolean(params[2]));
        else if (user.isAdmin()) {
            adminCommand(params, user, world, room);
            moderatorCommand(params, user, world, room);
        } else if (user.isModerator())
            moderatorCommand(params, user, world, room);
        else if (cmd.equals(UserCommand.MUTE)) {
            int muteTimeInMinutes = Integer.parseInt(params[1]);
            world.users.mute(user, muteTimeInMinutes, Calendar.MINUTE);
            int seconds = world.users.getMuteTimeInSeconds(user);
            world.send(new String[]{"warning", world.users.getMuteMessage(seconds)}, user);
        }
    }

    private void adminCommand(String[] params, User user, World world, Room room) {
        String cmd = params[0];

        if (cmd.equals(UserCommand.SHUTDOWN))
            world.scheduleTask(new Shutdown(world, user), 0, TimeUnit.SECONDS);
        else if (cmd.equals(UserCommand.RESTART))
            world.scheduleTask(new Restart(world), 0, TimeUnit.SECONDS);
        else if (cmd.equals(UserCommand.SHUTDOWN_NOW))
            try {
                world.send(new String[]{"logoutWarning", "", "60"}, world.zone.getChannelList());
                world.shutdown();
                Thread.sleep(TimeUnit.SECONDS.toMillis(2));
                SmartFoxServer.getInstance().halt(user);
                Thread.sleep(TimeUnit.SECONDS.toMillis(5));
                System.exit(0);
            } catch (InterruptedException ex) {
            }
        else if (cmd.equals(UserCommand.RESTART_NOW))
            try {
                world.send(new String[]{"logoutWarning", "", "60"}, world.zone.getChannelList());
                world.shutdown();
                Thread.sleep(TimeUnit.SECONDS.toMillis(2));
                ExtensionHelper.instance().rebootServer();
            } catch (InterruptedException ex) {
            }
        else if (cmd.equals(UserCommand.RATES)) {
            int rate = Integer.parseInt(params[2]);
            if (params[1].equals("gold")) {
                world.GOLD_RATE = rate;
                world.sendServerMessage("Gold rates has been changed to x" + rate + ".");
                world.sendToUsers(new String[]{"administrator", "Gold rates has been changed to x" + rate + "."});
            } else if (params[1].equals("drop")) {
                world.DROP_RATE = rate;
                world.sendServerMessage("Drop rates has been changed to x" + rate + ".");
                world.sendToUsers(new String[]{"administrator", "Drop rates has been changed to x" + rate + "."});
            } else if (params[1].equals("exp")) {
                world.EXP_RATE = rate;
                world.sendServerMessage("Experience rates has been changed to x" + rate + ".");
                world.sendToUsers(new String[]{"administrator", "Experience rates has been changed to x" + rate + "."});
            } else if (params[1].equals("rep")) {
                world.REP_RATE = rate;
                world.sendServerMessage("Reputation rates has been changed to x" + rate + ".");
                world.sendToUsers(new String[]{"administrator", "Reputation rates has been changed to x" + rate + "."});
            } else if (params[1].equals("cp")) {
                world.CP_RATE = rate;
                world.sendServerMessage("Class Point rates has been changed to x" + rate + ".");
                world.sendToUsers(new String[]{"administrator", "Class Point rates has been changed to x" + rate + "."});
            }
        } else if (cmd.equals("showdmg")) {
            Stats stats = (Stats) user.properties.get(Users.STATS);
            world.send(new String[]{"server", "minDmg: " + stats.getMinDmg()}, user);
            world.send(new String[]{"server", "maxDmg: " + stats.getMaxDmg()}, user);
        } else if (cmd.equals(UserCommand.HELP)) {
            world.send(new String[]{"server", "/shutdown"}, user);
            world.send(new String[]{"server", "/shutdownnow"}, user);
            world.send(new String[]{"server", "/restart"}, user);
            world.send(new String[]{"server", "/restartnow"}, user);
            world.send(new String[]{"server", "/rates (exp, drop, rep, cp, gold) (multiplier)"}, user);
        }
    }

    private void moderatorCommand(String[] params, User user, World world, Room room) throws RequestException {
        String cmd = params[0];

        if (cmd.equals(UserCommand.MUTE)) {
            User client = world.zone.getUserByName(params[2].toLowerCase());
            if (client == null) return;

            int muteTimeInMinutes = Integer.parseInt(params[1]);

            world.users.mute(client, muteTimeInMinutes, Calendar.MINUTE);
            int seconds = world.users.getMuteTimeInSeconds(user);
            world.send(new String[]{"warning", world.users.getMuteMessage(seconds)}, client);
        } else if (cmd.equals(UserCommand.UNMUTE)) {
            User client = world.zone.getUserByName(params[1].toLowerCase());
            if (client == null) return;
            if (world.users.isMute(client)) {
                world.send(new String[]{"unmute"}, client);
                world.users.unmute(client);
            }
        } else if (cmd.equals(UserCommand.KICK)) {
            User client = world.zone.getUserByName(params[1].toLowerCase());
            if (client == null) return;
            world.users.kick(client);
        } else if (cmd.equals(UserCommand.CLEAR)) {
            world.retrieveDatabaseObject(params[1]);
            world.send(new String[]{"server", "Server data cleared."}, user);
        } else if (cmd.equals(UserCommand.PULL)) {
            User client = world.zone.getUserByName(params[1].toLowerCase());
            if (client == null) throw new RequestException("Player \"" + params[1].toLowerCase() + "\" could not be found.");
            if (client.isAdmin() && !user.isAdmin()) throw new RequestException("Invalid /pull request.");

            Room clientRoom = world.zone.getRoom(client.getRoom());

            if (clientRoom.equals(room)) return;

            String userFrame = (String) user.properties.get(Users.FRAME);
            String userPad = (String) user.properties.get(Users.PAD);
            world.rooms.joinRoom(room, client, userFrame, userPad);
        } else if (cmd.equals(UserCommand.YELL))
            if (params[1].startsWith("@")) 
                world.sendServerMessage(params[1].substring(1));
            else {
                String username = user.getName();
                String message = params[1];
                
                if (params[1].contains("@")) {
                    username = params[1].split("@")[0].toLowerCase();
                    message = params[1].split("@")[1];
                }

                if (user.isAdmin())
                    world.send(new String[]{"administrator", "(" + username + "): " + message}, world.zone.getChannelList());
                else
                    world.send(new String[]{"moderator", "(" + username + "): " + message}, world.zone.getChannelList());
            }
        else if (cmd.equals(UserCommand.ADD_GOLD))
            world.users.giveRewards(user, 0, Integer.parseInt(params[1]), 0, 0, -1, user.getUserId(), "p");
        else if (cmd.equals(UserCommand.ADD_CLASSPOINTS))
            world.users.giveRewards(user, 0, 0, Integer.parseInt(params[1]), 0, -1, user.getUserId(), "p");
        else if (cmd.equals(UserCommand.ADD_XP))
            world.users.giveRewards(user, Integer.parseInt(params[1]), 0, 0, 0, -1, user.getUserId(), "p");
        else if (cmd.equals(UserCommand.ADD_COIN)) {
            int amount = Integer.parseInt(params[1]);

            JSONObject sell = new JSONObject();
            sell.put("cmd", "sellItem");
            sell.put("intAmount", amount);
            sell.put("CharItemID", user.hashCode());
            sell.put("bCoins", 1);
            world.send(sell, user);
            world.db.jdbc.run("UPDATE users SET Coins = (Coins + ?) WHERE id=?", amount, user.properties.get(Users.DATABASE_ID));

            world.send(new String[]{"server", amount + "ACs has been added to your account."}, user);
        } else if (cmd.equals(UserCommand.LEVEL))
            world.users.levelUp(user, Integer.parseInt(params[1]));
        else if (cmd.equals(UserCommand.EMOTE_ALL)) {
            User[] arrUsers = room.getAllUsers();
            for (User playerInRoom : arrUsers)
                world.sendToRoom(new String[]{"emotea", params[1], Integer.toString(playerInRoom.getUserId())}, playerInRoom, room);
        } else if (cmd.equals(UserCommand.BAN))
            world.send(new String[]{"warning", "Ban command is currently disabled for all staff. Please contact Yoshino as to why."}, user);
        else if (cmd.equals(UserCommand.ITEM))
            world.users.dropItem(user, Integer.parseInt(params[1]), Integer.parseInt(params[2])); //world.send(new String[] {"warning", "Item command is currently disabled for all staff. Please contact Yoshino as to why."}, user);
        else if (cmd.equals(UserCommand.HELP)) {
            world.send(new String[]{"server", "/item (item id) (quantity)"}, user);
            //world.send(new String[]{"server", "/ban (username)"}, user);
            world.send(new String[]{"server", "/kick (username)"}, user);
            world.send(new String[]{"server", "/mute (minutes) (username)"}, user);
            world.send(new String[]{"server", "/unmute (username)"}, user);
            world.send(new String[]{"server", "/clear (map, shop, quest, item, enhshop, setting)"}, user);
            world.send(new String[]{"server", "/pull (username)"}, user);
            world.send(new String[]{"server", "/iay (message)"}, user);
            world.send(new String[]{"server", "/addgold, /addcp, /addxp, /addcoin (amount)"}, user);
            world.send(new String[]{"server", "/level (level)"}, user);
            world.send(new String[]{"server", "/shop (shop id)"}, user);
        }
    }

}

/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides;

import augoeides.config.ConfigData;
import augoeides.console.Console;
import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.log.SimpleLogFormat;
import augoeides.ui.UserInterface;
import augoeides.world.Users;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import it.gotoandplay.smartfoxserver.events.InternalEventObject;
import it.gotoandplay.smartfoxserver.extensions.AbstractExtension;
import it.gotoandplay.smartfoxserver.extensions.ExtensionHelper;
import it.gotoandplay.smartfoxserver.lib.ActionscriptObject;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class AugoEidEs extends AbstractExtension {

    private final List<String> allowedRequestsForBannedUsers = Arrays.asList("mv", "firstJoin", "afk", "isModerator", "retrieveInventory", "moveToCell", "retrieveUserData", "retrieveUserDatas", "emotea");
    private final Map<String, String> requests = new HashMap<String, String>();
    private ExtensionHelper helper;
    private Console console;
    private World world;
    private UserInterface ui;

    public AugoEidEs() {
        Handler[] handlers = SmartFoxServer.log.getHandlers();

        for (Handler handler : handlers)
            handler.setFormatter(new SimpleLogFormat());

        this.console = new Console();
    }

    @Override
    public void init() {
        this.requests.putAll(ConfigData.REQUESTS);
        this.helper = ExtensionHelper.instance();
        this.world = new World(this, this.helper.getZone(this.getOwnerZone()));
        this.console.setWorld(this.world);
        this.console.setHelper(this.helper);

        SmartFoxServer.log.info("AugoEidEs initialized");

        if (Boolean.parseBoolean(System.getProperty("gui", "false"))) {
            try {
                javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
                this.ui = new UserInterface(this.world);
            } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            } catch (ClassNotFoundException ex) {
            } catch (InstantiationException ex) {
            } catch (IllegalAccessException ex) {
            } catch (IOException ex) {
            }
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ui.setVisible(true);
                }
            });
        }

        world.db.jdbc.run("UPDATE servers SET Online = 1 WHERE Name = ?", ConfigData.SERVER_NAME);
    }

    @Override
    public void handleRequest(String cmd, ActionscriptObject ao, User user, int fromRoom) {
        throw new UnsupportedOperationException("ActionScriptObject requests are not supported.");
    }

    private boolean isRequestFiltered(User user, String request) {
        boolean filtered = false;

        long lastRequestTime = ((Long) user.properties.get(Users.REQUEST_LAST_MILLISECONDS) + ConfigData.ANTI_REQUESTFLOOD_MIN_MSG_TIME);
        int requestCounter = (Integer) user.properties.get(Users.REQUEST_COUNTER);
        int requestWarningsCounter = (Integer) user.properties.get(Users.REQUEST_WARNINGS_COUNTER);
        int repeatedRequestCounter = (Integer) user.properties.get(Users.REQUEST_REPEATED_COUNTER);

        String lastRequest = (String) user.properties.get(Users.REQUEST_LAST);

        if (!user.isBeingKicked) {
            if (lastRequestTime > System.currentTimeMillis() && !ConfigData.ANTI_REQUESTFLOOD_EXCEPTIONS.contains(request)) {
                requestCounter++;

                user.properties.put(Users.REQUEST_COUNTER, requestCounter);
                if (requestCounter >= ConfigData.ANTI_REQUESTFLOOD_TOLERANCE) {
                    requestWarningsCounter++;
                    requestCounter = 0;

                    user.properties.put(Users.REQUEST_WARNINGS_COUNTER, requestWarningsCounter);
                    user.properties.put(Users.REQUEST_COUNTER, requestCounter);

                    filtered = true;
                }
            } else {
                requestCounter = 0;

                user.properties.put(Users.REQUEST_COUNTER, requestCounter);
            }
            if (ConfigData.ANTI_REQUESTFLOOD_REPEAT_ENABLED)
                if (request.equals(lastRequest) && !ConfigData.ANTI_REQUESTFLOOD_EXCEPTIONS.contains(request)) {
                    repeatedRequestCounter++;

                    user.properties.put(Users.REQUEST_REPEATED_COUNTER, repeatedRequestCounter);
                    if (repeatedRequestCounter >= ConfigData.ANTI_REQUESTFLOOD_MAX_REPEATED) {
                        requestWarningsCounter++;
                        repeatedRequestCounter = 0;

                        user.properties.put(Users.REQUEST_WARNINGS_COUNTER, requestWarningsCounter);
                        user.properties.put(Users.REQUEST_REPEATED_COUNTER, repeatedRequestCounter);

                        filtered = true;
                    }
                } else {
                    repeatedRequestCounter = 0;

                    user.properties.put(Users.REQUEST_REPEATED_COUNTER, repeatedRequestCounter);
                    user.properties.put(Users.REQUEST_LAST, request);
                }
            if (requestWarningsCounter >= ConfigData.ANTI_REQUESTFLOOD_WARNINGS) {
                //TO-DO KICK OR MUTE?

                user.isBeingKicked = true;
                SmartFoxServer.getInstance().addKickedUser(user, 1);
                this.world.users.kick(user);
            }
        }

        user.properties.put(Users.REQUEST_LAST_MILLISECONDS, System.currentTimeMillis());
        return filtered;
    }

    @Override
    public void handleRequest(String cmd, String[] params, User user, int fromRoom) {
        SmartFoxServer.log.fine("Recieved request: " + cmd);
        if (user == null) return;
        if (isRequestFiltered(user, cmd)) return;

        if (this.requests.containsKey(cmd)) {
            SmartFoxServer.log.fine("Processing request: " + cmd);

            int access = (Integer) user.properties.get(Users.ACCESS);

            if (access <= 0 && !this.allowedRequestsForBannedUsers.contains(cmd)) {
                this.world.send(new String[]{"warning", "Your account is currently disabled. Actions in-game are limited."}, user);
                return;
            }

            try {
                Class<IRequest> requestDefinition = (Class<IRequest>) Class.forName(this.requests.get(cmd));
                IRequest request = requestDefinition.newInstance();
                if (fromRoom == 1 || fromRoom == 32123 || fromRoom <= 0) {
                    Room room = this.world.zone.getRoom(user.getRoom());
                    request.process(params, user, this.world, room);
                } else {
                    Room room = this.world.zone.getRoom(fromRoom);
                    if (room != null) request.process(params, user, this.world, room);
                    else this.world.users.kick(user);
                }
            } catch (ClassNotFoundException ex) {
                SmartFoxServer.log.severe("Class not found:" + ex.getMessage());
            } catch (InstantiationException ex) {
                SmartFoxServer.log.severe("Instantiation error:" + ex.getMessage());
            } catch (IllegalAccessException ex) {
                SmartFoxServer.log.severe("Illegal access error:" + ex.getMessage());
           // } catch (NullPointerException ex) {
            //    SmartFoxServer.log.severe("Null error on " + cmd + " request on line " + ex.getStackTrace()[0].getLineNumber() + ": " + ex.getMessage());
            } catch (RequestException ex) {
                this.world.send(new String[]{ex.getType(), ex.getMessage()}, user);
            }
        } else {
            this.world.send(new String[]{"server", "The action you are trying to execute is not yet implemented. Please contact the developement staff if you want it available."}, user);
            SmartFoxServer.log.warning("Unknown request: " + cmd);
        }
    }

    @Override
    public void handleInternalEvent(InternalEventObject ieo) {
        String event = ieo.getEventName();

        SmartFoxServer.log.fine("System event: " + ieo.getEventName());

        if (event.equals(InternalEventObject.EVENT_SERVER_READY)) {
            if (!Boolean.parseBoolean(System.getProperty("gui", "false")))
                this.console.start();
        } else if (event.equals(InternalEventObject.EVENT_LOGIN)) {
            String nick = ieo.getParam("nick").split("~")[1];
            String pass = ieo.getParam("pass");
            SocketChannel chan = (SocketChannel) ieo.getObject("chan");
            this.world.users.login(nick.toLowerCase(), pass, chan);
        } else if (event.equals(InternalEventObject.EVENT_NEW_ROOM)) {
            Room room = (Room) ieo.getObject("room");
            SmartFoxServer.log.fine("New room created: " + room.getName());
        } else if (event.equals(InternalEventObject.EVENT_JOIN)) {
            Room room = (Room) ieo.getObject("room");
            User user = (User) ieo.getObject("user");

            JSONObject userObj = this.world.users.getProperties(user, room);

            JSONObject uJoin = new JSONObject();
            uJoin.put("cmd", "uotls");
            uJoin.put("o", userObj);
            uJoin.put("unm", user.getName());

            this.world.sendToRoomButOne(uJoin, user, room);
        } else if (event.equals(InternalEventObject.EVENT_USER_EXIT)) {
            Room room = (Room) ieo.getObject("room");
            User user = (User) ieo.getObject("user");

            this.world.rooms.exit(room, user);

            if (room.getUserCount() <= 0)
                this.helper.destroyRoom(this.world.zone, room.getId());
        } else if (event.equals(InternalEventObject.EVENT_USER_LOST)) {
            User user = (User) ieo.getObject("user");
            Room room = this.world.zone.getRoom(user.getRoom());

            if (room != null) {
                this.world.rooms.exit(room, user);
                room.removeUser(user, true, true);

                if (room.getUserCount() <= 0)
                    this.helper.destroyRoom(this.world.zone, room.getId());
            }

            this.world.users.lost(user);
            this.world.db.jdbc.run("UPDATE servers SET Count = ? WHERE Name = ?", this.world.zone.getUserCount(), ConfigData.SERVER_NAME);
        }
    }

    @Override
    public void destroy() {
        this.console.stop();
        if (this.ui != null)
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ui.setVisible(false);
                }
            });
        this.world.db.jdbc.run("UPDATE servers SET Online = 0 WHERE Name = ?", ConfigData.SERVER_NAME);

        this.world.destroy();
        SmartFoxServer.log.info("AugoEidEs destroyed");
    }
}

/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.world;

import augoeides.ai.MonsterAI;
import augoeides.avatars.State;
import augoeides.db.objects.*;
import augoeides.tasks.WarpUser;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import it.gotoandplay.smartfoxserver.data.Zone;
import it.gotoandplay.smartfoxserver.exceptions.ExtensionHelperException;
import it.gotoandplay.smartfoxserver.extensions.ExtensionHelper;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class Rooms {

    public static final String PVP_FACTIONS = "pvpfactions";
    public static final String PVP_DONE = "done";
    public static final String RED_TEAM_SCORE = "rscore";
    public static final String BLUE_TEAM_SCORE = "bscore";
    public static final String BLUE_TEAM_NAME = "bteamname";
    public static final String RED_TEAM_NAME = "rteamname";
    public static final String MONSTERS = "monsters";

    public static final int ROOM_LOCKED = 6;
    public static final int ROOM_STAFF_ONLY = 5;
    public static final int ROOM_REQUIRE_UPGRADE = 4;
    public static final int ROOM_LEVEL_LIMIT = 3;
    public static final int ROOM_USER_INSIDE = 2;
    public static final int ROOM_FULL = 1;
    public static final int ROOM_OK = 0;

    private final Zone zone;
    private final World world;
    private final ExtensionHelper helper;
    private final Random privKeyGenerator;

    public Rooms(Zone zone, World world) {
        this.world = world;
        this.zone = zone;
        this.helper = ExtensionHelper.instance();
        this.privKeyGenerator = new Random();
    }

    public void exit(Room room, User user) {
        String[] exit = {"exitArea", String.valueOf(user.getUserId()), user.getName()};
        this.world.sendToRoomButOne(exit, user, room);

        if (world.areas.get(room.getName().split("-")[0]) != null && world.areas.get(room.getName().split("-")[0]).isPvP())
            this.world.sendToRoomButOne(new String[]{"server", user.getName() + " has left the match."}, user, room);
    }

    private void moveToArea(Room room, User user) {
        JSONObject mta = new JSONObject();
        String mapName = room.getName().split("-")[0].equals("house") ? room.getName() : room.getName().split("-")[0];
        Area area = world.areas.get(mapName);

        JSONArray uoBranch = new JSONArray();

        User[] users = room.getAllUsers();
        for (User userInRoom : users) {
            JSONObject userObj = world.users.getProperties(userInRoom, room);
            uoBranch.add(userObj);
        }

        mta.put("cmd", "moveToArea");
        mta.put("areaId", room.getId());
        mta.put("areaName", room.getName());
        mta.put("sExtra", "");
        mta.put("strMapFileName", area.getFile());
        mta.put("strMapName", mapName);
        mta.put("uoBranch", uoBranch);
        mta.put("monBranch", getMonBranch(room, area));
        mta.put("intType", 2);

        if (area instanceof House)
            mta.put("houseData", ((House) area).getData());

        if (area instanceof Hall) {
            mta.put("guildData", this.world.users.getGuildHallData(((Hall) area).getGuildId()));
            mta.put("strMapName", "guildhall");
        }

        if (area.isPvP()) {
            mta.put("pvpTeam", user.properties.get(Users.PVP_TEAM));
            mta.put("PVPFactions", room.properties.get(Rooms.PVP_FACTIONS));

            JSONObject bs = new JSONObject();
            bs.put("v", room.properties.get(Rooms.BLUE_TEAM_SCORE));
            JSONObject rs = new JSONObject();
            rs.put("v", room.properties.get(Rooms.RED_TEAM_SCORE));
            JSONArray pvpScore = new JSONArray();
            pvpScore.add(bs);
            pvpScore.add(rs);

            mta.put("pvpScore", pvpScore);
        }

        if (!area.monsters.isEmpty()) {
            mta.put("mondef", getMonsterDefinition(area));
            mta.put("monmap", getMonMap(area));
        }

        world.send(mta, user);
    }

    public void basicRoomJoin(User user, String roomName, String roomFrame, String roomPad) {
        String mapName = roomName.split("-")[0];
        if (!this.world.areas.containsKey(mapName)) {
            world.send(new String[]{"warning", "\"" + mapName + "\" is not a recognized map name."}, user);
            return;
        }

        Room roomToJoin = lookForRoom(roomName);

        if (roomToJoin == null)
            roomToJoin = generateRoom(roomName);

        if (checkLimits(roomToJoin, user) == Rooms.ROOM_OK)
            joinRoom(roomToJoin, user, roomFrame, roomPad);
    }

    public void basicRoomJoin(User user, String roomName) {
        basicRoomJoin(user, roomName, "Enter", "Spawn");
    }

    public void joinRoom(Room room, User user) {
        joinRoom(room, user, "Enter", "Spawn");
    }

    public void joinRoom(Room room, User user, String frame, String pad) {
        if (room == null || user == null)
            return; //throw new NullPointerException("room is null");

        try {
            user.properties.put(Users.FRAME, frame);
            user.properties.put(Users.PAD, pad);
            user.properties.put(Users.TX, 0);
            user.properties.put(Users.TY, 0);

            this.helper.joinRoom(user, user.getRoom(), room.getId(), true, "", false, true);
            moveToArea(room, user);
            this.world.send(new String[]{"server", "You joined \"" + room.getName() + "\"!"}, user);
        } catch (ExtensionHelperException ex) {
            SmartFoxServer.log.warning("Error joining room: " + ex.getMessage());
        }
    }

    public Room lookForRoom(String name) {
        Room room = zone.getRoomByName(name);

        if (room != null)
            return room;
        else {
            String arr[] = name.split("-");
            String areaName = arr[0];

            if (arr.length > 1)
                try {
                    int roomKey = Integer.parseInt(arr[1]);

                    if (roomKey > 90000)
                        return generateRoom(name);
                } catch (NumberFormatException nre) {
                }

            //Search for keys ranging from 1 to 1000
            for (int i = 1; i < 1000; i++) {
                String search = areaName + "-" + i;
                Room test = zone.getRoomByName(search);

                //Don't return a full room.
                if (test != null)
                    if (test.getMaxUsers() > test.howManyUsers())
                        return test;
            }
        }

        return null;
    }

    public int checkLimits(Room room, User user) {
        if (room == null)
            throw new NullPointerException("room is null");

        String areaName = room.getName().split("-")[0].equals("house") ? room.getName() : room.getName().split("-")[0];
        Area area = world.areas.get(areaName);

        if (area.getReqLevel() > (Integer) user.properties.get(Users.LEVEL)) {
            world.send(new String[]{"warning", "\"" + areaName + "\" requires level " + area.getReqLevel() + " and above to enter."}, user);
            return Rooms.ROOM_LEVEL_LIMIT;
        } else if (area.isPvP()) {
            world.send(new String[]{"warning", "\"" + areaName + "\" is locked zone."}, user);
            return Rooms.ROOM_LOCKED;
        } else if (area.isStaff() && !(user.isAdmin() || user.isModerator())) {
            world.send(new String[]{"warning", "\"" + areaName + "\" is not a recognized map name."}, user);
            return Rooms.ROOM_STAFF_ONLY;
        } else if (area.isUpgrade() && (Integer) user.properties.get(Users.UPGRADE_DAYS) <= 0) {
            world.send(new String[]{"warning", "\"" + areaName + "\" is member only."}, user);
            return Rooms.ROOM_REQUIRE_UPGRADE;
        } else if (room.contains(user.getName())) {
            world.send(new String[]{"warning", "Cannot join a room you are currently in!"}, user);
            return Rooms.ROOM_USER_INSIDE;
        } else if (area instanceof Hall && ((Hall) area).getGuildId() != (Integer) user.properties.get(Users.GUILD_ID)) {
            world.send(new String[]{"warning", "You cannot access other guild halls!"}, user);
            return Rooms.ROOM_LOCKED;
        } else if (room.howManyUsers() >= room.getMaxUsers()) {
            world.send(new String[]{"warning", "Room join failed, destination room is full."}, user);
            return Rooms.ROOM_FULL;
        }

        return Rooms.ROOM_OK;
    }

    public Room generateRoom(String name) {
        if (name.contains("-"))
            try {
                int roomKey = Integer.parseInt(name.split("-")[1]);
                if (roomKey >= 90000) {
                    String generatedName = name.split("-")[0] + "-" + (privKeyGenerator.nextInt(9999) + 90000);
                    return createRoom(generatedName);
                } else if (roomKey >= 1000) {
                    String generatedName = name.split("-")[0] + "-" + roomKey;
                    return createRoom(generatedName);
                }
            } catch (NumberFormatException nre) {

            }

        String areaName = name.split("-")[0];
        for (int i = 1; i < 1000; i++) {
            String search = areaName + "-" + i;
            Room test = zone.getRoomByName(search);
            //room exists test if full
            if (test == null)
                return createRoom(search);
        }

        return null;
    }

    public Room createRoom(String name) {
        Map<String, String> map = new HashMap<String, String>();

        String mapName = name.split("-")[0].equals("house") ? name : name.split("-")[0];

        Area area = world.areas.get(mapName);

        map.put("isGame", "false");
        map.put("maxU", String.valueOf(area.getMaxPlayers()));
        map.put("name", name);
        map.put("uCount", "false");

        try {
            Room room = helper.createRoom(zone, map, null, false, true);

            //Create monsters
            Map<Integer, MonsterAI> monsters = new ConcurrentHashMap<Integer, MonsterAI>();

            if (!area.monsters.isEmpty())
                for (MapMonster mapMonster : area.monsters) {
                    MonsterAI monster = new MonsterAI(mapMonster, this.world, room);

                    monsters.put(mapMonster.getMonMapId(), monster);
                }

            if (area.isPvP()) {
                room.properties.put(Rooms.PVP_DONE, false);
                room.properties.put(Rooms.BLUE_TEAM_SCORE, 0);
                room.properties.put(Rooms.RED_TEAM_SCORE, 0);

                JSONObject b = new JSONObject();
                b.put("id", 8);
                b.put("sName", "Infinity");
                room.properties.put(Rooms.BLUE_TEAM_NAME, "Team Infinity");

                JSONObject r = new JSONObject();
                r.put("id", 7);
                r.put("sName", "Arts");
                room.properties.put(Rooms.RED_TEAM_NAME, "Team Arts");

                JSONArray PVPFactions = new JSONArray();
                PVPFactions.add(b);
                PVPFactions.add(r);

                room.properties.put(Rooms.PVP_FACTIONS, PVPFactions);
            }

            room.properties.put(Rooms.MONSTERS, monsters);

            return room;
        } catch (ExtensionHelperException ex) {
            //SmartFoxServer.log.severe("Error creating room: " + ex.getMessage());
            return null;
        }
    }

    private JSONArray getMonMap(Area area) {
        JSONArray monMap = new JSONArray();
        for (MapMonster mapMonster : area.monsters) {
            JSONObject monInfo = new JSONObject();

            monInfo.put("MonID", String.valueOf(mapMonster.getMonsterId()));
            monInfo.put("MonMapID", String.valueOf(mapMonster.getMonMapId()));
            monInfo.put("bRed", 0);
            monInfo.put("intRSS", String.valueOf(-1));
            monInfo.put("strFrame", mapMonster.getFrame());

            monMap.add(monInfo);
        }
        return monMap;
    }

    private JSONArray getMonsterDefinition(Area area) {
        JSONArray monDef = new JSONArray();
        for (MapMonster mapMonster : area.monsters) {
            JSONObject monInfo = new JSONObject();

            Monster monster = world.monsters.get(mapMonster.getMonsterId());

            monInfo.put("MonID", String.valueOf(mapMonster.getMonsterId()));
            monInfo.put("intHP", monster.getHealth());
            monInfo.put("intHPMax", monster.getHealth());
            monInfo.put("intLevel", monster.getLevel());
            monInfo.put("intMP", monster.getMana());
            monInfo.put("intMPMax", monster.getMana());
            monInfo.put("sRace", monster.getRace());
            monInfo.put("strBehave", "walk");
            monInfo.put("strElement", monster.getElement());
            monInfo.put("strLinkage", monster.getLinkage());
            monInfo.put("strMonFileName", monster.getFile());
            monInfo.put("strMonName", monster.getName());

            monDef.add(monInfo);
        }
        return monDef;
    }

    private JSONArray getMonBranch(Room room, Area area) {
        JSONArray monBranch = new JSONArray();
        Map<Integer, MonsterAI> monsters = (ConcurrentHashMap<Integer, MonsterAI>) room.properties.get(Rooms.MONSTERS);

        for (MonsterAI actMon : monsters.values()) {
            JSONObject mon = new JSONObject();
            
            State state = actMon.getState();
            Monster monster = actMon.getMonster();

            mon.put("MonID", String.valueOf(monster.getId()));
            mon.put("MonMapID", String.valueOf(actMon.getMapMonster().getMonMapId()));
            mon.put("bRed", "0");
            mon.put("iLvl", monster.getLevel());
            mon.put("intHP", state.getHealth());
            mon.put("intHPMax", monster.getHealth());
            mon.put("intMP", state.getMana());
            mon.put("intMPMax", monster.getMana());
            mon.put("intState", state.getState());
            mon.put("wDPS", monster.getDPS());

            if (area.isPvP()) {
                JSONArray react = new JSONArray();

                if (monster.getTeamId() > 0) {
                    react.add(0);
                    react.add(1);
                } else {
                    react.add(1);
                    react.add(0);
                }

                mon.put("react", react);
            }

            monBranch.add(mon);
        }

        return monBranch;
    }

    public void addPvPScore(Room room, int score, int teamId) {
        if ((Boolean) room.properties.get(Rooms.PVP_DONE))
            return;

        int rScore = (Integer) room.properties.get(Rooms.RED_TEAM_SCORE);
        int bScore = (Integer) room.properties.get(Rooms.BLUE_TEAM_SCORE);
        switch (teamId) {
            case 0:
                room.properties.put(Rooms.BLUE_TEAM_SCORE, (score + bScore) >= 1000 ? 1000 : (score + bScore));
                break;
            case 1:
                room.properties.put(Rooms.RED_TEAM_SCORE, (score + rScore) >= 1000 ? 1000 : (score + rScore));
                break;
            default:
                break;
        }
    }

    public void relayPvPEvent(MonsterAI ai, int teamId) {
        Monster monster = ai.getMonster();
        String monName = monster.getName();
        Room room = ai.getRoom();
        JSONObject pvpe = new JSONObject();

        pvpe.put("cmd", "PVPE");
        pvpe.put("typ", "kill");
        pvpe.put("team", teamId);

        if (monName.contains("Restorer")) {
            pvpe.put("val", "Restorer");
            addPvPScore(room, 50, teamId);
        } else if (monName.contains("Brawler")) {
            pvpe.put("val", "Brawler");
            addPvPScore(room, 25, teamId);
        } else if (monName.contains("Captain")) {
            pvpe.put("val", "Captain");
            addPvPScore(room, 1000, teamId);
        } else if (monName.contains("General")) {
            pvpe.put("val", "General");
            addPvPScore(room, 100, teamId);
        } else if (monName.contains("Knight")) {
            pvpe.put("val", "Knight");
            addPvPScore(room, 100, teamId);
        } else
            addPvPScore(room, monster.getLevel(), teamId);

        if (pvpe.containsKey("val"))
            this.world.send(pvpe, room.getChannellList());
    }

    public JSONObject getPvPResult(Room room) {
        JSONObject pvpcmd = new JSONObject();
        pvpcmd.put("cmd", "PVPS");
        JSONArray pvpScore = new JSONArray();
        JSONObject bs = new JSONObject();
        JSONObject rs = new JSONObject();

        int redScore = (Integer) room.properties.get(Rooms.RED_TEAM_SCORE);
        int blueScore = (Integer) room.properties.get(Rooms.BLUE_TEAM_SCORE);

        rs.put("v", redScore);
        bs.put("v", blueScore);

        if (!(Boolean) room.properties.get(Rooms.PVP_DONE) && (redScore >= 1000 || blueScore >= 1000)) {
            pvpcmd.put("cmd", "PVPC");

            String rName = (String) room.properties.get(Rooms.RED_TEAM_NAME);
            String bName = (String) room.properties.get(Rooms.BLUE_TEAM_NAME);

            if (redScore >= 1000) {
                this.world.sendServerMessage("<font color=\"#ffffff\"><a href=\"http://augoeides.org/?profile=" + rName + "\" target=\"_blank\">" + rName + "</a></font> won the match against <font color=\"#ffffff\"><a href=\"http://infinityarts.co/?profile=" + bName + "\" target=\"_blank\">" + bName + "</a></font>");

                Set<User> users = new HashSet<User>();

                for (User user : room.getAllUsers())
                    if ((Integer) user.properties.get(Users.PVP_TEAM) == 1)
                        users.add(user);

                this.world.scheduleTask(new WarpUser(this.world, users), 5, TimeUnit.SECONDS);
            } else if (blueScore >= 1000) {
                this.world.sendServerMessage("<font color=\"#ffffff\"><a href=\"http://augoeides.org/?profile=" + bName + "\" target=\"_blank\">" + bName + "</a></font> won the match against <font color=\"#ffffff\"><a href=\"http://infinityarts.co/?profile=" + rName + "\" target=\"_blank\">" + rName + "</a></font>");

                Set<User> users = new HashSet<User>();

                for (User user : room.getAllUsers())
                    if ((Integer) user.properties.get(Users.PVP_TEAM) == 0)
                        users.add(user);

                this.world.scheduleTask(new WarpUser(this.world, users), 9, TimeUnit.SECONDS);
            }

            room.properties.put(Rooms.PVP_DONE, true);
        }
        pvpScore.add(bs);
        pvpScore.add(rs);

        pvpcmd.put("pvpScore", pvpScore);
        return pvpcmd;
    }
}

/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.world;

import augoeides.aqw.Achievement;
import augoeides.aqw.Quests;
import augoeides.aqw.Rank;
import augoeides.aqw.Settings;
import augoeides.avatars.State;
import augoeides.avatars.UserState;
import augoeides.config.ConfigData;
import augoeides.db.objects.*;
import augoeides.db.objects.Class;
import augoeides.tasks.KickUser;
import augoeides.tasks.Regeneration;
import augoeides.tasks.RemoveAura;
import augoeides.world.stats.Stats;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import it.gotoandplay.smartfoxserver.data.Zone;
import it.gotoandplay.smartfoxserver.exceptions.LoginException;
import it.gotoandplay.smartfoxserver.extensions.ExtensionHelper;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import jdbchelper.BeanCreator;
import jdbchelper.JdbcException;
import jdbchelper.NoResultException;
import jdbchelper.QueryResult;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/**
 *
 * @author Mystical
 */
public class Users {

    private static final BeanCreator<ConcurrentHashMap> userProperties = new BeanCreator<ConcurrentHashMap>() {

        @Override
        public ConcurrentHashMap<Object, Object> createBean(ResultSet rs) throws SQLException {
            ConcurrentHashMap properties = new ConcurrentHashMap();

            properties.put(Users.DATABASE_ID, rs.getInt("id"));
            properties.put(Users.USERNAME, rs.getString("Name"));
            properties.put(Users.LEVEL, rs.getInt("Level"));
            properties.put(Users.ACCESS, rs.getInt("Access"));
            properties.put(Users.PERMAMUTE_FLAG, rs.getInt("PermamuteFlag"));
            properties.put(Users.GENDER, rs.getString("Gender"));
            properties.put(Users.COLOR_HAIR, Integer.valueOf(rs.getString("ColorHair"), 16));
            properties.put(Users.COLOR_SKIN, Integer.valueOf(rs.getString("ColorSkin"), 16));
            properties.put(Users.COLOR_EYE, Integer.valueOf(rs.getString("ColorEye"), 16));
            properties.put(Users.COLOR_BASE, Integer.valueOf(rs.getString("ColorBase"), 16));
            properties.put(Users.COLOR_TRIM, Integer.valueOf(rs.getString("ColorTrim"), 16));
            properties.put(Users.COLOR_ACCESSORY, Integer.valueOf(rs.getString("ColorAccessory"), 16));
            properties.put(Users.HAIR_ID, rs.getInt("HairID"));
            properties.put(Users.GUILD_ID, rs.getInt("GuildID"));
            properties.put(Users.SLOTS_BAG, rs.getInt("SlotsBag"));
            properties.put(Users.SLOTS_BANK, rs.getInt("SlotsBank"));
            properties.put(Users.SLOTS_HOUSE, rs.getInt("SlotsHouse"));
            properties.put(Users.UPGRADE_DAYS, rs.getInt("UpgradeDays"));
            properties.put(Users.LAST_AREA, rs.getString("LastArea"));
            properties.put(Users.QUESTS_1, rs.getString("Quests"));
            properties.put(Users.QUESTS_2, rs.getString("Quests2"));
            properties.put(Users.QUEST_DAILY_0, rs.getInt("DailyQuests0"));
            properties.put(Users.QUEST_DAILY_1, rs.getInt("DailyQuests1"));
            properties.put(Users.QUEST_DAILY_2, rs.getInt("DailyQuests2"));
            properties.put(Users.QUEST_MONTHLY_0, rs.getInt("MonthlyQuests0"));
            properties.put(Users.SETTINGS, rs.getInt("Settings"));
            properties.put(Users.ACHIEVEMENT, rs.getInt("Achievement"));
            properties.put(Users.GUILD_ID, rs.getInt("GuildID"));
            properties.put(Users.GUILD_RANK, rs.getInt("Rank"));

            return properties;
        }

    };

    public static final String ACCESS = "access";
    public static final String ACHIEVEMENT = "ia0";
    public static final String PERMAMUTE_FLAG = "permamute";

    public static final String AFK = "afk";
    public static final String FRAME = "frame";
    public static final String LEVEL = "level";
    public static final String PAD = "pad";
    public static final String TX = "tx";
    public static final String TY = "ty";
    public static final String USERNAME = "username";
    public static final String USER_STATE = "state";

    public static final String CLASS_NAME = "classname";
    public static final String CLASS_POINTS = "cp";
    public static final String CLASS_CATEGORY = "classcat";

    public static final String COLOR_ACCESSORY = "coloraccessory";
    public static final String COLOR_BASE = "colorbase";
    public static final String COLOR_EYE = "coloreye";
    public static final String COLOR_HAIR = "colorhair";
    public static final String COLOR_SKIN = "colorskin";
    public static final String COLOR_TRIM = "colortrim";

    public static final String DATABASE_ID = "dbId";
    public static final String GENDER = "gender";
    public static final String UPGRADE_DAYS = "upgdays";

    public static final String AURAS = "auras";
    public static final String EQUIPMENT = "equipment";

    public static final String GUILD_RANK = "guildrank";
    public static final String GUILD = "guildobj";
    public static final String GUILD_ID = "guildid";

    public static final String PARTY_ID = "partyId";

    public static final String PVP_TEAM = "pvpteam";

    public static final String REQUESTED_FRIEND = "requestedfriend";
    public static final String REQUESTED_PARTY = "requestedparty";
    public static final String REQUESTED_DUEL = "requestedduel";
    public static final String REQUESTED_GUILD = "requestedguild";

    public static final String HAIR_ID = "hairId";
    public static final String LAST_AREA = "lastarea";

    public static final String SETTINGS = "settings";

    public static final String BOOST_XP = "xpboost";
    public static final String BOOST_GOLD = "goldboost";
    public static final String BOOST_CP = "cpboost";
    public static final String BOOST_REP = "repboost";

    public static final String SLOTS_BAG = "bagslots";
    public static final String SLOTS_BANK = "bankslots";
    public static final String SLOTS_HOUSE = "houseslots";

    public static final String ITEM_WEAPON = "weaponitem";
    public static final String ITEM_WEAPON_ENHANCEMENT = "weaponitemenhancement";
    public static final String ITEM_HOUSE_INVENTORY = "houseitems";

    public static final String DROPS = "drops";

    public static final String TEMPORARY_INVENTORY = "tempinventory";

    public static final String STATS = "stats";

    public static final String QUESTS = "quests";
    public static final String QUESTS_1 = "quests1";
    public static final String QUESTS_2 = "quests2";
    public static final String QUEST_DAILY_0 = "dailyquests0";
    public static final String QUEST_DAILY_1 = "dailyquests1";
    public static final String QUEST_DAILY_2 = "dailyquests2";
    public static final String QUEST_MONTHLY_0 = "monthlyquests0";

    public static final String REGENERATION = "regenaration";
    public static final String RESPAWN_TIME = "respawntime";

    public static final String LAST_MESSAGE_TIME = "lastmessagetime";

    public static final String REQUEST_COUNTER = "requestcounter";
    public static final String REQUEST_WARNINGS_COUNTER = "requestwarncounter";
    public static final String REQUEST_LAST = "requestlast";
    public static final String REQUEST_REPEATED_COUNTER = "requestrepeatedcounter";
    public static final String REQUEST_LAST_MILLISECONDS = "requestlastmili";

    public static final String ROOM_QUEUED = "roomqueued";

    public static final String SKILLS = "skills";

    public static final int STATE_DEAD = 0;
    public static final int STATE_NORMAL = 1;
    public static final int STATE_COMBAT = 2;

    private final Zone zone;
    private final World world;
    private final ExtensionHelper helper;
    private final Map<String, Calendar> mutes;
    //private final Set<User> users;

    public Users(Zone zone, World world) {
        this.mutes = new HashMap<String, Calendar>();
        //this.users = new LinkedHashSet<User>();
        this.world = world;
        this.zone = zone;
        this.helper = ExtensionHelper.instance();
    }

    private void safeCloseChan(SocketChannel chan) {
        try {
            Thread.sleep(1000);
            chan.close();
        } catch (IOException ex) {
        } catch (InterruptedException ex) {
        }
    }

    private boolean isLoggedIn(User user) {
        return user != null;
    }

    private void multiLogin(User user, SocketChannel chan) {
        this.world.send(new String[]{"multiLoginWarning"}, chan);
        kick(user);
        safeCloseChan(chan);
    }

    public void login(String name, String hash, SocketChannel chan) {
        try {
            int databaseId = this.world.db.jdbc.queryForInt("SELECT id FROM users WHERE Name = ? AND Hash = ? LIMIT 1", name, hash);

            User userCheck = this.zone.getUserByName(name);

            if (isLoggedIn(userCheck)) {
                multiLogin(userCheck, chan);
                return;
            }

            User user = this.helper.canLogin(name, hash, chan, this.zone.getName(), true);
            user.properties = this.world.db.jdbc.queryForObject("SELECT users.*, users_guilds.GuildID, users_guilds.Rank FROM users LEFT JOIN users_guilds ON UserID = id WHERE id = ?", Users.userProperties, databaseId);

            if (user.properties == null) {
                failLogin(name, chan);
                return;
            }

            String[] loginResponse = new String[]{"loginResponse", "true", String.valueOf(user.getUserId()), name,
                this.world.messageOfTheDay, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()),
                this.world.newsString};

            int accessLevel = (Integer) user.properties.get(Users.ACCESS);

            if (accessLevel < 40 && ConfigData.STAFF_ONLY) {
                loginResponse = new String[]{"loginResponse", "false", "-1", name,
                    "A game update/maintenance is currently on-going. Only the InfinityArts staff can enter the server at the moment."};
                this.world.send(loginResponse, user);
                kick(user);
                return;
            }

            processLogin(user);

            if (user.getName().equals("iterator")) {
                loginResponse = new String[]{"loginIterator", "true", String.valueOf(user.getUserId()), name};
                this.world.send(loginResponse, user);
                return;
            }

            sendPreferences(user, Settings.PARTY);
            sendPreferences(user, Settings.GOTO);
            sendPreferences(user, Settings.FRIEND);
            sendPreferences(user, Settings.WHISPER);
            sendPreferences(user, Settings.TOOLTIPS);
            sendPreferences(user, Settings.DUEL);
            sendPreferences(user, Settings.GUILD);

            this.world.send(loginResponse, user);
        } catch (NoResultException nre) {
            failLogin(name, chan);
            SmartFoxServer.log.severe("NoResultException during login: " + nre.getMessage());
        } catch (JdbcException je) {
            failLogin(name, chan);
            SmartFoxServer.log.severe("JdbcException during login: " + je.getMessage());
        } catch (LoginException ex) {
            failLogin(name, chan);
            SmartFoxServer.log.severe("Login error: " + ex.getMessage());
        }
    }

    private void failLogin(String name, SocketChannel chan) {
        String[] loginResponse = new String[]{"loginResponse", "false", "-1", name,
            "User Data for '" + name + "' could not be retrieved. Please contact the InfinityArts staff to resolve the issue."};
        this.world.send(loginResponse, chan);
        safeCloseChan(chan);
    }

    public void sendUotls(User user, boolean showHp, boolean showHpMax, boolean showMp, boolean showMpMax, boolean showLevel, boolean showState) {
        JSONObject uotls = new JSONObject();
        JSONObject o = new JSONObject();

        uotls.put("cmd", "uotls");
        
        State state = (State) user.properties.get(Users.USER_STATE);

        if (showHp)
            o.put("intHP", state.getHealth());
        if (showHpMax)
            o.put("intHPMax", state.getMaxHealth());
        if (showMp)
            o.put("intMP", state.getMana());
        if (showMpMax)
            o.put("intMPMax", state.getMaxMana());
        if (showLevel)
            o.put("intLevel", (Integer) user.properties.get(Users.LEVEL));
        if (showState)
            o.put("intState", state.getState());

        uotls.put("o", o);
        uotls.put("unm", user.getName());

        this.world.send(uotls, this.world.zone.getRoom(user.getRoom()).getChannellList());
    }

    public boolean isMute(User user) {
        if (this.mutes.containsKey(user.getName())) {
            Calendar cal = this.mutes.get(user.getName());

            if (cal.getTimeInMillis() > System.currentTimeMillis())
                return true;
            else
                this.mutes.remove(user.getName());
        }
        return false;
    }

    public void mute(User user, int value, int type) {
        Calendar cal = Calendar.getInstance();
        cal.add(type, value);

        this.mutes.put(user.getName(), cal);
    }

    public int getBankCount(User user) {
        int bankCount = 0;
        QueryResult bankResult = this.world.db.jdbc.query("SELECT ItemID FROM users_items WHERE Bank = 1 AND UserID = ?", user.properties.get(Users.DATABASE_ID));

        while (bankResult.next()) {
            int itemid = bankResult.getInt("ItemID");
            if (!this.world.items.get(itemid).isCoins())
                bankCount++;
        }
        bankResult.close();

        return bankCount;
    }

    public void levelUp(User user, int level) {
        JSONObject levelUp = new JSONObject();

        int newLevel = level >= this.world.coreValues.get("intLevelMax").intValue() ? this.world.coreValues.get("intLevelMax").intValue() : level;

        levelUp.put("cmd", "levelUp");
        levelUp.put("intLevel", newLevel);
        levelUp.put("intExpToLevel", this.world.getExpToLevel(newLevel));

        user.properties.put(Users.LEVEL, newLevel);

        sendStats(user, true);

        this.world.db.jdbc.run("UPDATE users SET Level = ?, Exp = 0 WHERE id = ?", newLevel, user.properties.get(Users.DATABASE_ID));

        this.world.send(levelUp, user);
    }

    public void giveRewards(User user, int exp, int gold, int cp, int rep, int factionId, int fromId, String npcType) {
        boolean xpBoost = (Boolean) user.properties.get(Users.BOOST_XP);
        boolean goldBoost = (Boolean) user.properties.get(Users.BOOST_GOLD);
        boolean repBoost = (Boolean) user.properties.get(Users.BOOST_REP);
        boolean cpBoost = (Boolean) user.properties.get(Users.BOOST_CP);

        int calcExp = xpBoost ? (exp * (1 + this.world.EXP_RATE)) : (exp * this.world.EXP_RATE);
        int calcGold = goldBoost ? (gold * (1 + this.world.GOLD_RATE)) : (gold * this.world.GOLD_RATE);
        int calcRep = repBoost ? (rep * (1 + this.world.REP_RATE)) : (rep * this.world.REP_RATE);
        int calcCp = cpBoost ? (cp * (1 + this.world.CP_RATE)) : (cp * this.world.CP_RATE);

        int maxLevel = this.world.coreValues.get("intLevelMax").intValue();
        int expReward = (Integer) user.properties.get(Users.LEVEL) < maxLevel ? calcExp : 0;

        int classPoints = (Integer) user.properties.get(Users.CLASS_POINTS);
        int userLevel = (Integer) user.properties.get(Users.LEVEL);
        int userCp = (calcCp + classPoints) >= 302500 ? 302500 : (calcCp + classPoints);

        int curRank = Rank.getRankFromPoints((Integer) user.properties.get(Users.CLASS_POINTS));

        JSONObject addGoldExp = new JSONObject();

        addGoldExp.put("cmd", "addGoldExp");
        addGoldExp.put("id", fromId);
        addGoldExp.put("intGold", calcGold);
        addGoldExp.put("typ", npcType);

        if (userLevel < maxLevel) {
            addGoldExp.put("intExp", expReward);

            if (xpBoost)
                addGoldExp.put("bonusExp", (expReward / 2));
        }

        if (curRank != 10 && calcCp > 0) {
            addGoldExp.put("iCP", calcCp);

            if (cpBoost)
                addGoldExp.put("bonusCP", (calcCp / 2));

            user.properties.put(Users.CLASS_POINTS, userCp);
        }

        if (factionId > 1) {
            int rewardRep = calcRep >= 302500 ? 302500 : calcRep;

            addGoldExp.put("FactionID", factionId);
            addGoldExp.put("iRep", calcRep);

            if (repBoost)
                addGoldExp.put("bonusRep", (calcRep / 2));

            if (this.world.db.jdbc.queryForBoolean("SELECT COUNT(*) AS rowcount FROM users_factions WHERE UserID = ? AND FactionID = ?", user.properties.get(Users.DATABASE_ID), factionId))
                this.world.db.jdbc.run("UPDATE users_factions SET Reputation = (Reputation + ?) WHERE UserID = ? AND FactionID = ?", rewardRep, user.properties.get(Users.DATABASE_ID), factionId);
            else {
                this.world.db.jdbc.holdConnection();
                this.world.db.jdbc.run("INSERT INTO users_factions (UserID, FactionID, Reputation) VALUES (?, ?, ?)", user.properties.get(Users.DATABASE_ID), factionId, rewardRep);
                int charFactionId = Long.valueOf(this.world.db.jdbc.getLastInsertId()).intValue();
                this.world.db.jdbc.releaseConnection();

                JSONObject faction = new JSONObject();
                faction.put("FactionID", factionId);
                faction.put("bitSuccess", 1);
                faction.put("CharFactionID", charFactionId);
                faction.put("sName", this.world.factions.get(factionId));
                faction.put("iRep", calcRep);

                JSONObject addFaction = new JSONObject();
                addFaction.put("cmd", "addFaction");
                addFaction.put("faction", faction);
                this.world.send(addFaction, user);
            }
        }

        this.world.send(addGoldExp, user);

        //Update Database
        world.db.jdbc.beginTransaction();
        try {
            QueryResult userResult = world.db.jdbc.query("SELECT Gold, Exp FROM users WHERE id = ? FOR UPDATE", user.properties.get(Users.DATABASE_ID));
            if (userResult.next()) {
                int userXp = (userResult.getInt("Exp") + expReward);
                int userGold = (userResult.getInt("Gold") + calcGold);
                userResult.close();
                while (userXp >= this.world.getExpToLevel(userLevel)) {
                    userXp -= this.world.getExpToLevel(userLevel);
                    userLevel++;
                }

                //Update Level
                if (userLevel != (Integer) user.properties.get(Users.LEVEL)) {
                    levelUp(user, userLevel);
                    userXp = 0;
                }

                if ((calcGold > 0) || (expReward > 0 && (userLevel != maxLevel)))
                    this.world.db.jdbc.run("UPDATE users SET Gold = ?, Exp = ? WHERE id = ?", userGold, userXp, user.properties.get(Users.DATABASE_ID));
                if (curRank != 10 && calcCp > 0) {
                    JSONObject eqp = (JSONObject) user.properties.get(Users.EQUIPMENT);
                    if (eqp.has(Item.EQUIPMENT_CLASS)) {
                        JSONObject oldItem = eqp.getJSONObject(Item.EQUIPMENT_CLASS);
                        int itemId = oldItem.getInt("ItemID");
                        this.world.db.jdbc.run("UPDATE users_items SET Quantity = ? WHERE ItemID = ? AND UserID = ?", userCp, itemId, user.properties.get(Users.DATABASE_ID));

                        //Update Class
                        //Update Class Points
                        if (Rank.getRankFromPoints(userCp) > curRank)
                            loadSkills(user, this.world.items.get(itemId), userCp);
                    }
                }
            }
            userResult.close();
        } catch (JdbcException je) {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.rollbackTransaction();
            SmartFoxServer.log.severe("Error in rewards transaction: " + je.getMessage());
        } finally {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.commitTransaction();
        }
    }

    public String getMuteMessage(double seconds) {
        if (seconds <= 60)
            return String.format("You are muted! Chat privileges have been temporarily revoked. (%d second(s) remaining)", Math.round(seconds));
        else {
            double minutes = seconds / 60;
            if (minutes <= 60)
                return String.format("You are muted! Chat privileges have been temporarily revoked. (%d minute(s) and %d second(s) remaining)", Math.round(minutes), Math.round(seconds % 60));
            else {
                double hours = minutes / 60;
                if (hours <= 24)
                    return String.format("You are muted! Chat privileges have been temporarily revoked. (%d hour(s) and %d minute(s) remaining)", Math.round(hours), Math.round(hours % 60));
                else {
                    double days = hours / 24;
                    return String.format("You are muted! Chat privileges have been temporarily revoked. (%d day(s) and %d hour(s) remaining)", Math.round(days), Math.round(days % 24));
                }
            }
        }
    }

    public int getMuteTimeInDays(User user) {
        if (this.mutes.containsKey(user.getName()))
            return Long.valueOf(TimeUnit.MILLISECONDS.toDays((this.mutes.get(user.getName()).getTimeInMillis() - System.currentTimeMillis()))).intValue();
        return 0;
    }

    public int getMuteTimeInHours(User user) {
        if (this.mutes.containsKey(user.getName()))
            return Long.valueOf(TimeUnit.MILLISECONDS.toHours((this.mutes.get(user.getName()).getTimeInMillis() - System.currentTimeMillis()))).intValue();
        return 0;
    }

    public int getMuteTimeInMinutes(User user) {
        if (this.mutes.containsKey(user.getName()))
            return Long.valueOf(TimeUnit.MILLISECONDS.toMinutes((this.mutes.get(user.getName()).getTimeInMillis() - System.currentTimeMillis()))).intValue();
        return 0;
    }

    public int getMuteTimeInSeconds(User user) {
        if (this.mutes.containsKey(user.getName()))
            return Long.valueOf(TimeUnit.MILLISECONDS.toSeconds((this.mutes.get(user.getName()).getTimeInMillis() - System.currentTimeMillis()))).intValue();
        return 0;
    }

    public void unmute(User user) {
        if (this.mutes.containsKey(user.getName()))
            this.mutes.remove(user.getName());
    }

    public boolean hasAura(User user, int auraId) {
        Set<RemoveAura> auras = (Set<RemoveAura>) user.properties.get(Users.AURAS);
        for (RemoveAura ra : auras) {
            Aura aura = ra.getAura();
            if (aura.getId() == auraId)
                return true;
        }

        return false;
    }

    public void removeAura(User user, RemoveAura ra) {
        Set<RemoveAura> auras = (Set<RemoveAura>) user.properties.get(Users.AURAS);
        auras.remove(ra);
    }

    public RemoveAura applyAura(User user, Aura aura) {
        Set<RemoveAura> auras = (Set<RemoveAura>) user.properties.get(Users.AURAS);

        RemoveAura ra = new RemoveAura(this.world, aura, user);
        ra.setRunning(this.world.scheduleTask(ra, aura.getDuration(), TimeUnit.SECONDS));

        auras.add(ra);

        return ra;
    }

    private void processLogin(User user) {
        if ((Integer) user.properties.get(Users.ACCESS) >= 60) {
            user.setAsAdmin();
            SmartFoxServer.log.fine(user.getName() + " has administrator privileges.");
        } else if ((Integer) user.properties.get(Users.ACCESS) >= 40) {
            user.setAsModerator();
            SmartFoxServer.log.fine(user.getName() + " has moderator privileges.");
        }

        //Initializes objects
        user.properties.put(Users.REQUEST_COUNTER, 0);
        user.properties.put(Users.REQUEST_WARNINGS_COUNTER, 0);
        user.properties.put(Users.REQUEST_REPEATED_COUNTER, 0);
        user.properties.put(Users.REQUEST_LAST, "");
        user.properties.put(Users.REQUEST_LAST_MILLISECONDS, System.currentTimeMillis());

        //Stats
        user.properties.put(Users.STATS, new Stats(user, world));

        //Equipment
        user.properties.put(Users.EQUIPMENT, new JSONObject());

        //Regeneration
        user.properties.put(Users.REGENERATION, new Regeneration(user, this.world));

        //Guilds
        user.properties.put(Users.GUILD, getGuildObject((Integer) user.properties.get(Users.GUILD_ID)));

        //Auras
        user.properties.put(Users.AURAS, Collections.newSetFromMap(new ConcurrentHashMap<RemoveAura, Boolean>()));

        //Last M essage Time
        user.properties.put(Users.LAST_MESSAGE_TIME, System.currentTimeMillis());

        //Party ID
        user.properties.put(Users.PARTY_ID, -1);

        //Quest
        user.properties.put(Users.QUESTS, new HashSet<Integer>());

        //Drops
        user.properties.put(Users.DROPS, new HashMap<Integer, Queue<Integer>>());

        //Skill
        user.properties.put(Users.SKILLS, new HashMap<String, Integer>());

        //Temporary Inventory
        user.properties.put(Users.TEMPORARY_INVENTORY, new HashMap<Integer, Integer>());

        //Boosts
        user.properties.put(Users.BOOST_XP, false);
        user.properties.put(Users.BOOST_GOLD, false);
        user.properties.put(Users.BOOST_CP, false);
        user.properties.put(Users.BOOST_REP, false);

        //Anti Force Requests
        user.properties.put(Users.REQUESTED_PARTY, new HashSet<Integer>());
        user.properties.put(Users.REQUESTED_FRIEND, new HashSet<Integer>());
        user.properties.put(Users.REQUESTED_DUEL, new HashSet<Integer>());
        user.properties.put(Users.REQUESTED_GUILD, new HashSet<Integer>());

        //User State Properties
        user.properties.put(Users.USER_STATE, new UserState(world, user, 100, 100));
        user.properties.put(Users.AFK, false);
        user.properties.put(Users.PVP_TEAM, 0);
    }

    public void log(User user, String violation, String details) {
        int userId = (Integer) user.properties.get(Users.DATABASE_ID);
        world.db.jdbc.run("INSERT INTO users_logs (UserID, Violation, Details) VALUES (?, ?, ?)", userId, violation, details);

        if (!user.isBeingKicked)
            world.send(new String[]{"suspicious"}, user);
    }

    public void changePreferences(User user, String pref, boolean value) {
        int ia1 = (Integer) user.properties.get(Users.SETTINGS);
        ia1 = Settings.setPreferences(pref, ia1, value);
        user.properties.put(Users.SETTINGS, ia1);

        JSONObject uotls = new JSONObject();
        uotls.put("cmd", "uotls");
        uotls.put("unm", user.getName());

        //Helm
        if (pref.equals(Settings.HELM)) {
            uotls.put("o", new JSONObject().put("showHelm", Settings.getPreferences(Settings.HELM, ia1)));
            world.sendToRoomButOne(uotls, user, world.zone.getRoom(user.getRoom()));
        }

        //Cloak
        if (pref.equals(Settings.CLOAK)) {
            uotls.put("o", new JSONObject().put("showCloak", Settings.getPreferences(Settings.CLOAK, ia1)));
            world.sendToRoomButOne(uotls, user, world.zone.getRoom(user.getRoom()));
        }

        sendPreferences(user, pref);

        world.db.jdbc.run("UPDATE users SET Settings = ? WHERE id = ?", ia1, user.properties.get(Users.DATABASE_ID));
    }

    private void sendPreferences(User user, String pref) {
        int ia1 = (Integer) user.properties.get(Users.SETTINGS);
        boolean value = Settings.getPreferences(pref, ia1);

        if (pref.equals(Settings.PARTY) && value)
            this.world.send(new String[]{"server", Settings.PARTY_MESSAGE_ON}, user);
        else if (pref.equals(Settings.PARTY) && !value)
            this.world.send(new String[]{"warning", Settings.PARTY_MESSAGE_OFF}, user);

        if (pref.equals(Settings.GOTO) && value)
            this.world.send(new String[]{"server", Settings.GOTO_MESSAGE_ON}, user);
        else if (pref.equals(Settings.GOTO) && !value)
            this.world.send(new String[]{"warning", Settings.GOTO_MESSAGE_OFF}, user);

        if (pref.equals(Settings.FRIEND) && value)
            this.world.send(new String[]{"server", Settings.FRIEND_MESSAGE_ON}, user);
        else if (pref.equals(Settings.FRIEND) && !value)
            this.world.send(new String[]{"warning", Settings.FRIEND_MESSAGE_OFF}, user);

        if (pref.equals(Settings.WHISPER) && value)
            this.world.send(new String[]{"server", Settings.WHISPER_MESSAGE_ON}, user);
        else if (pref.equals(Settings.WHISPER) && !value)
            this.world.send(new String[]{"warning", Settings.WHISPER_MESSAGE_OFF}, user);

        if (pref.equals(Settings.TOOLTIPS) && value)
            this.world.send(new String[]{"server", Settings.TOOLTIPS_MESSAGE_ON}, user);
        else if (pref.equals(Settings.TOOLTIPS) && !value)
            this.world.send(new String[]{"warning", Settings.TOOLTIPS_MESSAGE_OFF}, user);

        if (pref.equals(Settings.DUEL) && value)
            this.world.send(new String[]{"server", Settings.DUEL_MESSAGE_ON}, user);
        else if (pref.equals(Settings.DUEL) && !value)
            this.world.send(new String[]{"warning", Settings.DUEL_MESSAGE_OFF}, user);

        if (pref.equals(Settings.GUILD) && value)
            this.world.send(new String[]{"server", Settings.GUILD_MESSAGE_ON}, user);
        else if (pref.equals(Settings.GUILD) && !value)
            this.world.send(new String[]{"warning", Settings.GUILD_MESSAGE_OFF}, user);
    }

    public void updateClass(User user, Item item, int classPoints) {
        JSONObject updateClass = new JSONObject();

        updateClass.put("cmd", "updateClass");
        updateClass.put("iCP", classPoints);
        updateClass.put("sClassCat", item.classObj.getCategory());
        updateClass.put("sDesc", item.classObj.getDescription());
        updateClass.put("sStats", item.classObj.getStatsDescription());
        updateClass.put("uid", user.getUserId());

        if (item.classObj.getManaRegenerationMethods().contains(":")) {
            JSONArray aMRM = new JSONArray();
            for (String s : item.classObj.getManaRegenerationMethods().split(","))
                aMRM.add(s + "\r");
            updateClass.put("aMRM", aMRM);
        } else
            updateClass.put("aMRM", item.classObj.getManaRegenerationMethods());

        updateClass.put("sClassName", item.getName());

        this.world.send(updateClass, user);

        updateClass.clear();
        updateClass.put("cmd", "updateClass");
        updateClass.put("iCP", classPoints);
        updateClass.put("sClassCat", item.classObj.getCategory());
        updateClass.put("sClassName", item.getName());
        updateClass.put("uid", user.getUserId());

        //Update User Properties
        user.properties.put(Users.CLASS_POINTS, classPoints);
        user.properties.put(Users.CLASS_NAME, item.getName());
        user.properties.put(Users.CLASS_CATEGORY, item.classObj.getCategory());

        this.world.sendToRoomButOne(updateClass, user, this.world.zone.getRoom(user.getRoom()));

        loadSkills(user, item, classPoints);
    }

    public void regen(User user) {
        State state = (State) user.properties.get(Users.USER_STATE);
        state.setState(1);
        Regeneration regen = (Regeneration) user.properties.get(Users.REGENERATION);
        regen.setRunning(this.world.scheduleTask(regen, 4, TimeUnit.SECONDS, true));
    }

    private void clearAuras(User user) {
        Set<RemoveAura> auras = (Set<RemoveAura>) user.properties.get(Users.AURAS);
        for (RemoveAura ra : auras)
            ra.cancel();

        auras.clear();

        Stats stats = (Stats) user.properties.get(Users.STATS); //Get user stats
        stats.effects.clear();

        JSONObject ca = new JSONObject();
        ca.put("cmd", "clearAuras");

        this.world.send(ca, user);
    }

    private void applyPassiveAuras(User user, int rank, Class classObj) {
        if (rank < 4) return;

        JSONObject aurap = new JSONObject();
        JSONArray auras = new JSONArray();

        Stats stats = (Stats) user.properties.get(Users.STATS); //Get user stats

        for (int skillId : classObj.skills) {
            Skill skill = this.world.skills.get(skillId);

            if (skill.getType().equals("passive") && skill.hasAuraId()) {
                Aura aura = this.world.auras.get(skill.getAuraId());

                if (!aura.effects.isEmpty()) {

                    JSONObject auraObj = new JSONObject();
                    JSONArray effects = new JSONArray();

                    for (int effectId : aura.effects) {
                        AuraEffects ae = this.world.effects.get(effectId);

                        JSONObject effect = new JSONObject();

                        effect.put("typ", ae.getType());
                        effect.put("sta", ae.getStat());
                        effect.put("id", ae.getId());
                        effect.put("val", ae.getValue());

                        effects.add(effect);

                        stats.effects.add(ae);
                    }

                    auraObj.put("nam", aura.getName());
                    auraObj.put("e", effects);

                    auras.add(auraObj);
                }
            }
        }

        aurap.put("auras", auras);
        aurap.put("cmd", "aura+p");
        aurap.put("tInf", "p:" + user.getUserId());

        this.world.send(aurap, user);
    }

    public JSONArray getGuildHallData(int guildId) {
        JSONArray guildData = new JSONArray();

        QueryResult halls = this.world.db.jdbc.query("SELECT * FROM guilds_halls WHERE GuildID = ?", guildId);

        while (halls.next()) {
            JSONObject hall = new JSONObject();

            hall.put("intY", halls.getInt("Y"));
            hall.put("intX", halls.getInt("X"));
            hall.put("strLinkage", halls.getString("Linkage"));
            hall.put("ID", halls.getInt("id"));
            hall.put("strCell", halls.getString("Cell"));
            hall.put("strBuildings", getBuildingString(halls.getInt("id")));
            hall.put("strConnections", getConnectionsString(halls.getInt("id")));
            hall.put("strInterior", halls.getString("Interior"));

            guildData.add(hall);
        }
        halls.close();

        return guildData;
    }

    public String getConnectionsString(int hallId) {
        StringBuilder sb = new StringBuilder();

        QueryResult result = this.world.db.jdbc.query("SELECT * FROM guilds_halls_connections WHERE HallID = ?", hallId);
        while (result.next()) {
            sb.append(result.getString("Pad")).append(",");
            sb.append(result.getString("Cell")).append(",");
            sb.append(result.getString("PadPosition")).append("|");
        }
        result.close();

        if (sb.length() <= 0)
            return sb.toString();
        else {
            int index = sb.length() - 1;
            return sb.deleteCharAt(index).toString();
        }
    }

    public String getBuildingString(int hallId) {
        StringBuilder sb = new StringBuilder();

        QueryResult result = this.world.db.jdbc.query("SELECT * FROM guilds_halls_buildings WHERE HallID = ?", hallId);
        while (result.next()) {
            Item building = this.world.items.get(result.getInt("ItemID"));

            sb.append("slot:").append(result.getInt("Slot")).append(",");
            sb.append("size:").append(result.getInt("Size")).append(",");
            sb.append("itemID:").append(result.getInt("ItemID")).append(",");
            sb.append("linkage:").append(building.getLink()).append(",");
            sb.append("file:").append(building.getFile()).append("|");
        }
        result.close();

        if (sb.length() <= 0)
            return sb.toString();
        else {
            int index = sb.length() - 1;
            return sb.deleteCharAt(index).toString();
        }
    }

    public JSONObject getGuildObject(int guildId) {
        JSONObject guild = new JSONObject();

        QueryResult result = this.world.db.jdbc.query("SELECT * FROM guilds WHERE id = ?", guildId);
        if (result.next()) {
            JSONArray members = new JSONArray();

            guild.put("Name", result.getString("Name"));
            guild.put("MOTD", result.getString("MessageOfTheDay").length() > 0 ? result.getString("MessageOfTheDay") : "undefined");
            guild.put("pending", new JSONObject());
            guild.put("MaxMembers", result.getInt("MaxMembers"));
            guild.put("dateUpdated", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(result.getDate("LastUpdated")));
            guild.put("Level", 1);
            guild.put("HallSize", result.getInt("HallSize"));
            //guild.put("guildHall", getGuildHallData(guildId));
            guild.put("guildHall", new JSONArray());

            result.close();

            QueryResult memberResult = this.world.db.jdbc.query("SELECT id, Name, Level, CurrentServer, Rank FROM users_guilds JOIN users WHERE id = UserID AND users_guilds.GuildID = ?", guildId);
            while (memberResult.next()) {
                JSONObject member = new JSONObject();

                member.put("ID", memberResult.getInt("id"));
                member.put("userName", memberResult.getString("Name"));
                member.put("Level", memberResult.getString("Level"));
                member.put("Rank", memberResult.getInt("Rank"));
                member.put("Server", memberResult.getString("CurrentServer"));
                members.add(member);
            }
            memberResult.close();
            guild.put("ul", members);
        }
        result.close();

        return guild;
    }

    private void loadSkills(User user, Item item, int classPoints) {
        int rank = Rank.getRankFromPoints(classPoints);

        Map<String, Integer> skills = (Map<String, Integer>) user.properties.get(Users.SKILLS);

        JSONArray active = new JSONArray();
        JSONArray passive = new JSONArray();
        JSONObject sAct = new JSONObject();
        sAct.put("cmd", "sAct");

        for (int skillId : item.classObj.skills) {
            Skill skill = this.world.skills.get(skillId);

            if (skill.getType().equals("passive")) {
                JSONObject passObj = new JSONObject();

                passObj.put("desc", skill.getDescription());
                passObj.put("fx", skill.getEffects());
                passObj.put("icon", skill.getIcon());
                passObj.put("id", skillId);
                passObj.put("nam", skill.getName());
                passObj.put("range", skill.getRange());
                passObj.put("ref", skill.getReference());
                passObj.put("tgt", skill.getTarget());
                passObj.put("typ", skill.getType());

                JSONArray arrAuras = new JSONArray();
                arrAuras.add(new JSONObject());

                passObj.put("auras", arrAuras);

                if (rank < 4)
                    passObj.put("isOK", false);
                else
                    passObj.put("isOK", true);
                passive.add(passObj);
                skills.put(skill.getReference(), skillId);
            } else {
                JSONObject actObj = new JSONObject();

                actObj.put("anim", skill.getAnimation());
                actObj.put("cd", String.valueOf(skill.getCooldown()));
                actObj.put("damage", skill.getDamage());
                actObj.put("desc", skill.getDescription());
                if (!skill.getDsrc().isEmpty())
                    actObj.put("dsrc", skill.getDsrc());
                actObj.put("fx", skill.getEffects());
                actObj.put("icon", skill.getIcon());
                actObj.put("id", skillId);
                actObj.put("isOK", true);
                actObj.put("mp", String.valueOf(skill.getMana()));
                actObj.put("nam", skill.getName());
                actObj.put("range", String.valueOf(skill.getRange()));
                actObj.put("ref", skill.getReference());
                if (!skill.getStrl().isEmpty())
                    actObj.put("strl", skill.getStrl());
                actObj.put("tgt", skill.getTarget());
                actObj.put("typ", skill.getType());

                if (rank < 2 && skill.getReference().equals("a2"))
                    actObj.put("isOK", false);
                if (rank < 3 && skill.getReference().equals("a3"))
                    actObj.put("isOK", false);
                if (rank < 5 && skill.getReference().equals("a4"))
                    actObj.put("isOK", false);

                if (skill.getHitTargets() > 0) {
                    actObj.put("tgtMax", String.valueOf(skill.getHitTargets()));
                    actObj.put("tgtMin", "1");
                }
                if (skill.getReference().equals("aa")) {
                    actObj.put("auto", true);
                    actObj.put("typ", "aa");

                    active.element(0, actObj);
                } else if (skill.getReference().equals("a1"))
                    active.element(1, actObj);
                else if (skill.getReference().equals("a2")) {
                    if (rank < 2)
                        actObj.put("isOK", false);
                    active.element(2, actObj);
                } else if (skill.getReference().equals("a3")) {
                    if (rank < 3)
                        actObj.put("isOK", false);
                    active.element(3, actObj);
                } else if (skill.getReference().equals("a4")) {
                    if (rank < 5)
                        actObj.put("isOK", false);
                    active.element(4, actObj);
                }

                skills.put(skill.getReference(), skillId);
            }
        }

        JSONObject potionObj = new JSONObject();
        potionObj.put("anim", "Cheer");
        potionObj.put("cd", "" + 60000);
        potionObj.put("desc", "Equip a potion or scroll from your inventory to use it here.");
        potionObj.put("fx", "");
        potionObj.put("icon", "icu1");
        potionObj.put("isOK", true);
        potionObj.put("mp", "" + 0);
        potionObj.put("nam", "Potions");
        potionObj.put("range", 808);
        potionObj.put("ref", "i1");
        potionObj.put("str1", "");
        potionObj.put("tgt", "f");
        potionObj.put("typ", "i");

        active.element(5, potionObj);

        JSONObject actions = new JSONObject();

        actions.put("active", active);
        actions.put("passive", passive);
        sAct.put("actions", actions);

        clearAuras(user);
        applyPassiveAuras(user, rank, item.classObj);

        this.world.send(sAct, user);
    }

    public JSONObject getProperties(User user, Room room) {
        JSONObject userprop = new JSONObject();
        
        State state = (State) user.properties.get(Users.USER_STATE);

        userprop.put("afk", (Boolean) user.properties.get(Users.AFK));
        userprop.put("entID", user.getUserId());
        userprop.put("entType", "p");
        userprop.put("intHP", state.getHealth());
        userprop.put("intHPMax", state.getMaxHealth());
        userprop.put("intLevel", (Integer) user.properties.get(Users.LEVEL));
        userprop.put("intMP", state.getMana());
        userprop.put("intMPMax", state.getMaxMana());
        userprop.put("intState", state.getState());
        userprop.put("showCloak", true);
        userprop.put("showHelm", true);
        userprop.put("strFrame", user.properties.get(Users.FRAME));
        userprop.put("strPad", user.properties.get(Users.PAD));
        userprop.put("strUsername", user.properties.get(Users.USERNAME));
        userprop.put("tx", (Integer) user.properties.get(Users.TX));
        userprop.put("ty", (Integer) user.properties.get(Users.TY));
        userprop.put("uoName", user.getName());

        if (!room.getName().contains("house") && world.areas.get(room.getName().split("-")[0]).isPvP())
            userprop.put("pvpTeam", (Integer) user.properties.get(Users.PVP_TEAM));

        return userprop;
    }

    public void updateStats(User user, Enhancement enhancement, String equipment) {
        Map<String, Double> itemStats = this.world.getItemStats(enhancement, equipment);
        Stats stats = (Stats) user.properties.get(Users.STATS);

        if (equipment.equals(Item.EQUIPMENT_CLASS))
            for (Map.Entry<String, Double> entry : itemStats.entrySet())
                stats.armor.put(entry.getKey(), entry.getValue());
        else if (equipment.equals(Item.EQUIPMENT_WEAPON))
            for (Map.Entry<String, Double> entry : itemStats.entrySet())
                stats.weapon.put(entry.getKey(), entry.getValue());
        else if (equipment.equals(Item.EQUIPMENT_CAPE))
            for (Map.Entry<String, Double> entry : itemStats.entrySet())
                stats.cape.put(entry.getKey(), entry.getValue());
        else if (equipment.equals(Item.EQUIPMENT_HELM))
            for (Map.Entry<String, Double> entry : itemStats.entrySet())
                stats.helm.put(entry.getKey(), entry.getValue());
        else
            throw new IllegalArgumentException("equipment " + equipment + " cannot have stat values!");
    }

    public void sendStats(User user) {
        sendStats(user, false);
    }

    public void sendStats(User user, boolean levelUp) {
        JSONObject stu = new JSONObject();
        JSONObject tempStat = new JSONObject();

        int userLevel = (Integer) user.properties.get(Users.LEVEL);
        Stats stats = (Stats) user.properties.get(Users.STATS);
        stats.update();

        int END = (int) (stats.get$END() + stats.get_END());
        int WIS = (int) (stats.get$WIS() + stats.get_WIS());

        int intHPperEND = this.world.coreValues.get("intHPperEND").intValue();
        int intMPperWIS = this.world.coreValues.get("intMPperWIS").intValue();

        int addedHP = (END * intHPperEND);

        //Calculate new HP and MP
        int userHp = this.world.getHealthByLevel(userLevel);

        userHp += addedHP;

        int userMp = this.world.getManaByLevel(userLevel) + (WIS * intMPperWIS);
        
        State state = (State) user.properties.get(Users.USER_STATE);
        
        state.setMaxHealth(userHp);
        state.setMaxMana(userMp);

        //Current
        if (state.getState() == Users.STATE_NORMAL || levelUp)
            state.setHealth(userHp);

        if (state.getState() == Users.STATE_NORMAL || levelUp)
            state.setMana(userMp);

        this.world.users.sendUotls(user, true, true, true, true, levelUp, false);

        JsonConfig config = new JsonConfig();

        config.setExcludes(new String[]{"maxDmg", "minDmg"});

        JSONObject stat = JSONObject.fromObject(stats, config);
        JSONObject ba = new JSONObject();
        JSONObject he = new JSONObject();
        JSONObject Weapon = new JSONObject();
        JSONObject innate = new JSONObject();
        JSONObject ar = new JSONObject();

        innate.put("INT", stats.innate.get("INT"));
        innate.put("STR", stats.innate.get("STR"));
        innate.put("DEX", stats.innate.get("DEX"));
        innate.put("END", stats.innate.get("END"));
        innate.put("LCK", stats.innate.get("LCK"));
        innate.put("WIS", stats.innate.get("WIS"));

        for (Map.Entry<String, Double> entry : stats.armor.entrySet())
            if (entry.getValue() > 0)
                ar.put(entry.getKey(), entry.getValue().intValue());

        for (Map.Entry<String, Double> entry : stats.helm.entrySet())
            if (entry.getValue() > 0)
                he.put(entry.getKey(), entry.getValue().intValue());

        for (Map.Entry<String, Double> entry : stats.weapon.entrySet())
            if (entry.getValue() > 0)
                Weapon.put(entry.getKey(), entry.getValue().intValue());

        for (Map.Entry<String, Double> entry : stats.cape.entrySet())
            if (entry.getValue() > 0)
                ba.put(entry.getKey(), entry.getValue().intValue());

        if (!ba.isEmpty())
            tempStat.put("ba", ba);
        if (!ar.isEmpty())
            tempStat.put("ar", ar);
        if (!Weapon.isEmpty())
            tempStat.put("Weapon", Weapon);
        if (!he.isEmpty())
            tempStat.put("he", he);

        tempStat.put("innate", innate);

        stu.put("tempSta", tempStat);
        stu.put("cmd", "stu");
        stu.put("sta", stat);
        stu.put("wDPS", stats.wDPS);

        this.world.send(stu, user);
    }

    public JSONArray getFriends(User user) {
        JSONArray friends = new JSONArray();
        QueryResult result = this.world.db.jdbc.query("SELECT id, Level, Name, CurrentServer FROM users LEFT JOIN users_friends ON FriendID = id WHERE UserID = ?", user.properties.get(Users.DATABASE_ID));
        while (result.next()) {
            JSONObject temp = new JSONObject();
            temp.put("iLvl", result.getInt("Level"));
            temp.put("ID", result.getInt("id"));
            temp.put("sName", result.getString("Name"));
            temp.put("sServer", result.getString("CurrentServer"));
            friends.add(temp);
        }
        result.close();
        return friends;
    }

    public void dropItem(User user, int itemId) {
        dropItem(user, itemId, 1);
    }

    public void dropItem(User user, int itemId, int quantity) {
        Item itemObj = this.world.items.get(itemId);

        if (!itemObj.getReqQuests().isEmpty()) {
            String[] arrQuests;
            Boolean getDrop = false;

            if (itemObj.getReqQuests().contains(","))
                arrQuests = itemObj.getReqQuests().split(",");
            else
                arrQuests = new String[]{itemObj.getReqQuests()};

            Set<Integer> acceptedQuests = (Set<Integer>) user.properties.get(Users.QUESTS);

            for (String questId : arrQuests)
                if (acceptedQuests.contains(Integer.parseInt(questId))) {
                    getDrop = true;
                    break;
                }

            if (!getDrop)
                return;
        }

        Map<Integer, Integer> tempInventory = (Map<Integer, Integer>) user.properties.get(Users.TEMPORARY_INVENTORY);

        if (!itemObj.isTemporary()) {
            QueryResult itemResult = this.world.db.jdbc.query("SELECT Quantity FROM users_items WHERE ItemID = ? AND UserID = ?", itemId, user.properties.get(Users.DATABASE_ID));
            if (itemResult.next()) {
                int quantityInInventory = itemResult.getInt("Quantity");
                itemResult.close();
                if (quantityInInventory >= itemObj.getStack())
                    return;
            }
            itemResult.close();
        } else if (tempInventory.containsKey(itemId))
            if (tempInventory.get(itemId) >= itemObj.getStack())
                return;

        JSONObject di = new JSONObject();
        JSONObject arrItems = new JSONObject();
        JSONObject item;
        
        if(itemObj.getType().equals("Enhancement")) {
            item = Item.getItemJSON(itemObj, world.enhancements.get(itemObj.getEnhId()));
        } else 
            item = Item.getItemJSON(itemObj);

        item.put("iQty", quantity);

        arrItems.put(String.valueOf(itemId), item);
        di.put("items", arrItems);
        di.put("cmd", itemObj.isTemporary() ? "addItems" : "dropItem");

        if (itemObj.isTemporary())
            if (tempInventory.containsKey(itemId)) {
                if (tempInventory.get(itemId) < itemObj.getStack())
                    this.addTemporaryItem(user, itemId, quantity);
            } else
                this.addTemporaryItem(user, itemId, quantity);
        else {
            Map<Integer, Queue<Integer>> userDrops = (Map<Integer, Queue<Integer>>) user.properties.get(Users.DROPS);
            if (userDrops.containsKey(itemId)) {
                Queue<Integer> quantities = userDrops.get(itemId);
                quantities.add(quantity);
            } else {
                Queue<Integer> quantities = new LinkedBlockingQueue<Integer>();
                quantities.add(quantity);
                userDrops.put(itemId, quantities);
            }
        }

        this.world.send(di, user);
    }

    public void setQuestValue(User user, int index, int value) {
        if (index > 99) {
            user.properties.put(Users.QUESTS_2, Quests.updateValue((String) user.properties.get(Users.QUESTS_2), (index - 100), value));
            this.world.db.jdbc.run("UPDATE users SET Quests2 = ? WHERE id =  ?", user.properties.get(Users.QUESTS_2), user.properties.get(Users.DATABASE_ID));
        } else {
            user.properties.put(Users.QUESTS_1, Quests.updateValue((String) user.properties.get(Users.QUESTS_1), index, value));
            this.world.db.jdbc.run("UPDATE users SET Quests = ? WHERE id = ?", user.properties.get(Users.QUESTS_1), user.properties.get(Users.DATABASE_ID));
        }

        JSONObject updateQuest = new JSONObject();
        updateQuest.put("cmd", "updateQuest");
        updateQuest.put("iIndex", index);
        updateQuest.put("iValue", value);

        this.world.send(updateQuest, user);
    }

    public int getQuestValue(User user, int index) {
        if (index > 99)
            return Quests.lookAtValue((String) user.properties.get(Users.QUESTS_2), (index - 100));
        return Quests.lookAtValue((String) user.properties.get(Users.QUESTS_1), index);
    }

    public void setAchievement(String field, int index, int value, User user) {
        if (field.equals("ia0")) {
            user.properties.put(Users.ACHIEVEMENT, Achievement.update((Integer) user.properties.get(Users.ACHIEVEMENT), index, value));
            this.world.db.jdbc.run("UPDATE users SET Achievement = ? WHERE id = ?", user.properties.get(Users.ACHIEVEMENT), user.properties.get(Users.DATABASE_ID));
        } else if (field.equals("id0")) {
            user.properties.put(Users.QUEST_DAILY_0, Achievement.update((Integer) user.properties.get(Users.QUEST_DAILY_0), index, value));
            this.world.db.jdbc.run("UPDATE users SET DailyQuests0 = ? WHERE id = ?", user.properties.get(Users.QUEST_DAILY_0), user.properties.get(Users.DATABASE_ID));
        } else if (field.equals("id1")) {
            user.properties.put(Users.QUEST_DAILY_1, Achievement.update((Integer) user.properties.get(Users.QUEST_DAILY_1), index, value));
            this.world.db.jdbc.run("UPDATE users SET DailyQuests1 = ? WHERE id = ?", user.properties.get(Users.QUEST_DAILY_1), user.properties.get(Users.DATABASE_ID));
        } else if (field.equals("id2")) {
            user.properties.put(Users.QUEST_DAILY_2, Achievement.update((Integer) user.properties.get(Users.QUEST_DAILY_2), index, value));
            this.world.db.jdbc.run("UPDATE users SET DailyQuests2 = ? WHERE id = ?", user.properties.get(Users.QUEST_DAILY_2), user.properties.get(Users.DATABASE_ID));
        } else if (field.equals("im0")) {
            user.properties.put(Users.QUEST_MONTHLY_0, Achievement.update((Integer) user.properties.get(Users.QUEST_MONTHLY_0), index, value));
            this.world.db.jdbc.run("UPDATE users SET MonthlyQuests0 = ? WHERE id = ?", user.properties.get(Users.QUEST_MONTHLY_0), user.properties.get(Users.DATABASE_ID));
        }

        JSONObject sa = new JSONObject();
        sa.put("cmd", "setAchievement");
        sa.put("field", field);
        sa.put("index", index);
        sa.put("value", value);

        this.world.send(sa, user);
    }

    public int getAchievement(String field, int index, User user) {
        if (field.equals("ia0"))
            return Achievement.get((Integer) user.properties.get(Users.ACHIEVEMENT), index);
        else if (field.equals("id0"))
            return Achievement.get((Integer) user.properties.get(Users.QUEST_DAILY_0), index);
        else if (field.equals("id1"))
            return Achievement.get((Integer) user.properties.get(Users.QUEST_DAILY_1), index);
        else if (field.equals("id2"))
            return Achievement.get((Integer) user.properties.get(Users.QUEST_DAILY_2), index);
        else if (field.equals("im0"))
            return Achievement.get((Integer) user.properties.get(Users.QUEST_MONTHLY_0), index);
        else
            return -1;
    }

    public String getGuildRank(int rank) {
        String rankName = "";
        switch (rank) {
            case 0:
                rankName = "duffer";
                break;
            case 1:
                rankName = "member";
                break;
            case 2:
                rankName = "officer";
                break;
            case 3:
                rankName = "leader";
                break;
            default:
                break;
        }
        return rankName;
    }

    public boolean turnInItem(User user, int itemId, int quantity) {
        Map<Integer, Integer> items = new HashMap<Integer, Integer>();
        items.put(itemId, quantity);
        return turnInItems(user, items);
    }

    public boolean turnInItems(User user, Map<Integer, Integer> items) {
        boolean valid = true;

        StringBuilder sItems = new StringBuilder();
        world.db.jdbc.beginTransaction();
        try {
            for (Map.Entry<Integer, Integer> entry : items.entrySet()) {
                int itemId = entry.getKey();
                int quantityRequirement = entry.getValue();

                Item item = this.world.items.get(itemId);
                if (item.isTemporary()) {
                    Map<Integer, Integer> temporaryInven = (Map<Integer, Integer>) user.properties.get(Users.TEMPORARY_INVENTORY);

                    if (temporaryInven.containsKey(itemId))
                        if (temporaryInven.get(itemId) >= quantityRequirement) {
                            temporaryInven.remove(Integer.valueOf(itemId));
                            valid = true;
                        } else {
                            valid = false;
                            this.log(user, "Suspicous TurnIn", "Quantity requirement for turning in item is lacking.");
                            world.db.jdbc.rollbackTransaction();
                            break;
                        }
                    else {
                        valid = false;
                        this.log(user, "Suspicous TurnIn", "Turning in a temporary item not found in temp. inventory.");
                        world.db.jdbc.rollbackTransaction();
                        break;
                    }
                } else {
                    QueryResult itemResult = this.world.db.jdbc.query("SELECT Quantity FROM users_items WHERE ItemID = ? AND UserID = ? FOR UPDATE", itemId, user.properties.get(Users.DATABASE_ID));
                    if (itemResult.next()) {
                        int quantity = itemResult.getInt("Quantity");
                        itemResult.close();
                        if (item.getStack() > 1) {
                            int quantityLeft = (quantity - quantityRequirement);
                            if (quantityLeft > 0)
                                this.world.db.jdbc.run("UPDATE users_items SET Quantity = ? WHERE ItemID = ? AND UserID = ?", quantityLeft, itemId, user.properties.get(Users.DATABASE_ID));
                            else
                                this.world.db.jdbc.run("DELETE FROM users_items WHERE ItemID = ? AND UserID = ?", itemId, user.properties.get(Users.DATABASE_ID));
                        } else
                            this.world.db.jdbc.run("DELETE FROM users_items WHERE ItemID = ? AND UserID = ?", itemId, user.properties.get(Users.DATABASE_ID));
                        valid = true;
                    } else {
                        valid = false;
                        itemResult.close();
                        this.world.users.log(user, "Suspicous TurnIn", "Item to turn in not found in database.");
                        world.db.jdbc.rollbackTransaction();
                        break;
                    }
                    itemResult.close();
                }

                sItems.append(itemId);
                sItems.append(":");
                sItems.append(quantityRequirement);
                sItems.append(",");
            }
        } catch (JdbcException je) {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.rollbackTransaction();
            SmartFoxServer.log.severe("Error in turn in transaction: " + je.getMessage());
        } finally {
            if (world.db.jdbc.isInTransaction())
                world.db.jdbc.commitTransaction();
        }

        if (valid && !items.isEmpty()) {
            JSONObject ti = new JSONObject();
            ti.put("cmd", "turnIn");
            ti.put("sItems", sItems.toString().substring(0, sItems.toString().length() - 1));
            world.send(ti, user);
        }

        return valid;
    }

    public void addTemporaryItem(User user, int itemId, int quantity) {
        Map<Integer, Integer> tempInventory = (Map<Integer, Integer>) user.properties.get(Users.TEMPORARY_INVENTORY);
        if (tempInventory.containsKey(itemId)) {
            int deltaQuantity = (tempInventory.get(itemId) + quantity);
            tempInventory.put(itemId, deltaQuantity);
        } else
            tempInventory.put(itemId, quantity);
    }

    public void lost(User user) {
        if (user == null) return;
        if (user.properties.isEmpty())
            return;
        
        UserState state = (UserState) user.properties.get(Users.USER_STATE);
        state.clearAuras();
        
        Regeneration regen = (Regeneration) user.properties.get(Users.REGENERATION);
        regen.cancel();

        //UPDATE PARTY
        int partyId = (Integer) user.properties.get(Users.PARTY_ID);
        if (partyId > 0) {
            PartyInfo pi = this.world.parties.getPartyInfo(partyId);

            if (pi.getOwner().equals(user.properties.get(Users.USERNAME)))
                pi.setOwner(pi.getNextOwner());

            pi.removeMember(user);

            JSONObject pr = new JSONObject();
            pr.put("cmd", "pr");
            pr.put("owner", pi.getOwner());
            pr.put("typ", "l");
            pr.put("unm", user.properties.get(Users.USERNAME));

            this.world.send(pr, pi.getChannelListButOne(user));
            this.world.send(pr, user);

            if (pi.getMemberCount() <= 0) {
                JSONObject pc = new JSONObject();
                pc.put("cmd", "pc");
                world.send(pc, pi.getOwnerObject());
                this.world.parties.removeParty(partyId);
                pi.getOwnerObject().properties.put(Users.PARTY_ID, -1);
            }
        }

        world.db.jdbc.run("UPDATE users SET LastArea = ?, CurrentServer = 'Offline' WHERE id = ?", user.properties.get(Users.LAST_AREA), user.properties.get(Users.DATABASE_ID));

        //UPDATE GUILD
        int guildId = (Integer) user.properties.get(Users.GUILD_ID);
        if (guildId > 0)
            this.world.sendGuildUpdate(getGuildObject(guildId));

        //UPDATE FRIEND
        JSONObject updateFriend = new JSONObject();
        JSONObject friendInfo = new JSONObject();

        updateFriend.put("cmd", "updateFriend");
        friendInfo.put("iLvl", (Integer) user.properties.get(Users.LEVEL));
        friendInfo.put("ID", user.properties.get(Users.DATABASE_ID));
        friendInfo.put("sName", user.properties.get(Users.USERNAME));
        friendInfo.put("sServer", "Offline");
        updateFriend.put("friend", friendInfo);

        QueryResult result = this.world.db.jdbc.query("SELECT Name FROM users LEFT JOIN users_friends ON FriendID = id WHERE UserID = ?", user.properties.get(Users.DATABASE_ID));
        while (result.next()) {
            User client = this.world.zone.getUserByName(result.getString("Name").toLowerCase());
            if (client != null) {
                this.world.send(updateFriend, client);
                this.world.send(new String[]{"server", user.getName() + " has logged out."}, client);
            }
        }
        result.close();
    }

    public JSONObject getUserData(int id, boolean self) {
        JSONObject userData = new JSONObject();

        User user = this.helper.getUserById(id);
        if (user != null) {
            int hairId = (Integer) user.properties.get(Users.HAIR_ID);
            Hair hair = this.world.hairs.get(hairId);

            String lastArea = (String) user.properties.get(Users.LAST_AREA);
            lastArea = lastArea.split("\\|")[0];

            userData.put("eqp", user.properties.get(Users.EQUIPMENT));
            userData.put("iCP", (Integer) user.properties.get(Users.CLASS_POINTS));
            userData.put("iUpgDays", (Integer) user.properties.get(Users.UPGRADE_DAYS));
            userData.put("intAccessLevel", (Integer) user.properties.get(Users.ACCESS));
            userData.put("intColorAccessory", (Integer) user.properties.get(Users.COLOR_ACCESSORY));
            userData.put("intColorBase", (Integer) user.properties.get(Users.COLOR_BASE));
            userData.put("intColorEye", (Integer) user.properties.get(Users.COLOR_EYE));
            userData.put("intColorHair", (Integer) user.properties.get(Users.COLOR_HAIR));
            userData.put("intColorSkin", (Integer) user.properties.get(Users.COLOR_SKIN));
            userData.put("intColorTrim", (Integer) user.properties.get(Users.COLOR_TRIM));
            userData.put("intLevel", (Integer) user.properties.get(Users.LEVEL));
            userData.put("strClassName", user.properties.get(Users.CLASS_NAME));
            userData.put("strGender", user.properties.get(Users.GENDER));
            userData.put("strHairFilename", hair.getFile());
            userData.put("strHairName", hair.getName());
            userData.put("strUsername", user.properties.get(Users.USERNAME));

            if ((Integer) user.properties.get(Users.GUILD_ID) > 0) {
                JSONObject guildData = (JSONObject) user.properties.get(Users.GUILD);
                JSONObject guild = new JSONObject();

                guild.put("id", user.properties.get(Users.GUILD_ID));
                guild.put("Name", guildData.get("Name"));
                guild.put("MOTD", guildData.get("MOTD"));

                userData.put("guild", guild);
                userData.put("guildRank", user.properties.get(Users.GUILD_RANK));
            }

            if (self) {
                State state = (State) user.properties.get(Users.USER_STATE);
                QueryResult result = world.db.jdbc.query("SELECT HouseInfo, ActivationFlag, Gold, Coins, Exp, Country, Email, DateCreated, UpgradeExpire, Age, Upgraded FROM users WHERE id = ?", user.properties.get(Users.DATABASE_ID));
                if (result.next()) {
                    userData.put("CharID", (Integer) user.properties.get(Users.DATABASE_ID));
                    userData.put("HairID", hairId);
                    userData.put("UserID", user.getUserId());
                    userData.put("bPermaMute", user.properties.get(Users.PERMAMUTE_FLAG));
                    userData.put("bitSuccess", "1");
                    userData.put("dCreated", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(result.getDate("DateCreated")));
                    userData.put("dUpgExp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(result.getDate("UpgradeExpire")));
                    userData.put("iAge", result.getString("Age"));
                    userData.put("iBagSlots", (Integer) user.properties.get(Users.SLOTS_BAG));
                    userData.put("iBankSlots", (Integer) user.properties.get(Users.SLOTS_BANK));
                    userData.put("iBoostCP", 0);
                    userData.put("iBoostG", 0);
                    userData.put("iBoostRep", 0);
                    userData.put("iBoostXP", 0);
                    userData.put("iDBCP", (Integer) user.properties.get(Users.CLASS_POINTS));
                    userData.put("iDEX", 0);
                    userData.put("iDailyAdCap", 6);
                    userData.put("iDailyAds", 0);
                    userData.put("iEND", 0);
                    userData.put("iFounder", 0);
                    userData.put("iHouseSlots", (Integer) user.properties.get(Users.SLOTS_HOUSE));
                    userData.put("iINT", 0);
                    userData.put("iLCK", 0);
                    userData.put("iSTR", 0);
                    userData.put("iUpg", result.getInt("Upgraded"));
                    userData.put("iWIS", 0);
                    userData.put("ia0", (Integer) user.properties.get(Users.ACHIEVEMENT));
                    userData.put("ia1", (Integer) user.properties.get(Users.SETTINGS));
                    userData.put("id0", (Integer) user.properties.get(Users.QUEST_DAILY_0));
                    userData.put("id1", (Integer) user.properties.get(Users.QUEST_DAILY_1));
                    userData.put("id2", (Integer) user.properties.get(Users.QUEST_DAILY_2));
                    userData.put("im0", (Integer) user.properties.get(Users.QUEST_MONTHLY_0));
                    userData.put("intActivationFlag", result.getInt("ActivationFlag"));
                    userData.put("intCoins", result.getInt("Coins"));
                    userData.put("intDBExp", result.getInt("Exp"));
                    userData.put("intDBGold", result.getInt("Gold"));
                    userData.put("intExp", result.getInt("Exp"));
                    userData.put("intExpToLevel", this.world.getExpToLevel((Integer) user.properties.get(Users.LEVEL)));
                    userData.put("intGold", result.getInt("Gold"));
                    userData.put("intHP", state.getHealth());
                    userData.put("intHPMax", state.getMaxHealth());
                    userData.put("intHits", 1267);
                    userData.put("intMP", state.getMana());
                    userData.put("intMPMax", state.getMaxMana());
                    userData.put("ip0", 0);
                    userData.put("ip1", 0);
                    userData.put("ip2", 0);
                    userData.put("iq0", 0);
                    userData.put("lastArea", lastArea);
                    userData.put("sCountry", result.getString("Country"));
                    userData.put("sHouseInfo", result.getString("HouseInfo"));
                    userData.put("strEmail", result.getString("Email"));
                    userData.put("strMapName", zone.getRoom(user.getRoom()).getName().split("-")[0]);
                    userData.put("strQuests", user.properties.get(Users.QUESTS_1));
                    userData.put("strQuests2", user.properties.get(Users.QUESTS_2));
                }
                result.close();
            }
        }
        return userData;
    }

    public void respawn(User user) {
        State state = (State) user.properties.get(Users.USER_STATE);
        state.restore();

        clearAuras(user);
        sendUotls(user, true, false, true, false, false, true);
    }

    public void kick(User user) {
        user.isBeingKicked = true;
        this.world.send(new String[]{"logoutWarning", "", "65"}, user);
        this.world.scheduleTask(new KickUser(user, this.world), 0, TimeUnit.SECONDS);
    }
}

/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.world;

import augoeides.config.ConfigData;
import augoeides.db.Database;
import augoeides.db.objects.Class;
import augoeides.db.objects.*;
import augoeides.tasks.ACGiveaway;
import augoeides.tasks.FreeDbPool;
import augoeides.tasks.WarzoneQueue;
import augoeides.world.stats.Stats;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import it.gotoandplay.smartfoxserver.data.Zone;
import it.gotoandplay.smartfoxserver.extensions.AbstractExtension;
import java.nio.channels.SocketChannel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import jdbchelper.BeanCreator;
import jdbchelper.ResultSetMapper;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class World {

    //<editor-fold defaultstate="collapsed" desc="Result Set Mappers">
    private static final ResultSetMapper<String, Double> coreValuesMapper = new ResultSetMapper<String, Double>() {

        @Override
        public AbstractMap.SimpleEntry<String, Double> mapRow(ResultSet rs) throws SQLException {
            return new AbstractMap.SimpleEntry<String, Double>(rs.getString("name"), rs.getDouble("value"));
        }
    };

    private static final ResultSetMapper<Integer, String> factionsMapper = new ResultSetMapper<Integer, String>() {

        @Override
        public AbstractMap.SimpleEntry<Integer, String> mapRow(ResultSet rs) throws SQLException {
            return new AbstractMap.SimpleEntry<Integer, String>(rs.getInt("id"), rs.getString("Name"));
        }
    };

    private static final BeanCreator<String> newsCreator = new BeanCreator<String>() {

        @Override
        public String createBean(ResultSet rs) throws SQLException {
            StringBuilder sb = new StringBuilder();

            sb.append(rs.getString("name"));
            sb.append("=");
            sb.append(rs.getString("value"));

            while (rs.next()) {
                sb.append(",");
                sb.append(rs.getString("name"));
                sb.append("=");
                sb.append(rs.getString("value"));
            }

            return sb.toString();
        }
    };
//</editor-fold>

    public HashMap<String, Area> areas;
    public HashMap<Integer, Item> items;
    public HashMap<Integer, Shop> shops;
    public HashMap<Integer, Hair> hairs;
    public HashMap<Integer, Skill> skills;
    public HashMap<Integer, Enhancement> enhancements;
    public HashMap<Integer, EnhancementPattern> patterns;
    public HashMap<Integer, Monster> monsters;
    public HashMap<Integer, Aura> auras;
    public HashMap<Integer, AuraEffects> effects;
    public HashMap<Integer, Hairshop> hairshops;
    public HashMap<Integer, Quest> quests;
    public HashMap<Integer, String> factions;
    public HashMap<String, Double> coreValues;
    public Database db;
    public Users users;
    public Rooms rooms;
    public Parties parties;
    public WarzoneQueue warzoneQueue;
    public Zone zone;
    
    public static final Random RANDOM = new Random();

    public String messageOfTheDay;
    public String newsString;

    public int EXP_RATE = 1;
    public int CP_RATE = 1;
    public int GOLD_RATE = 1;
    public int REP_RATE = 1;
    public int DROP_RATE = 1;

    private AbstractExtension ext;
    private ScheduledExecutorService tasks;

    public World(AbstractExtension ext, Zone zone) {
        this.ext = ext;
        this.zone = zone;
        this.db = new Database(ConfigData.DB_MAX_CONNECTIONS);
        this.rooms = new Rooms(zone, this);
        this.users = new Users(zone, this);
        this.parties = new Parties();
        this.tasks = Executors.newScheduledThreadPool((Runtime.getRuntime().availableProcessors()));
        this.warzoneQueue = new WarzoneQueue(this);

        this.tasks.scheduleAtFixedRate(this.warzoneQueue, 5, 5, TimeUnit.SECONDS);
        this.tasks.scheduleAtFixedRate(new ACGiveaway(this), 30, 30, TimeUnit.MINUTES);
        this.tasks.scheduleAtFixedRate(new FreeDbPool(this.db), 30, 30, TimeUnit.MINUTES);

        retrieveDatabaseObject("all");
        SmartFoxServer.log.info("World initialized.");
    }

    public void shutdown() {
        this.db.jdbc.run("UPDATE users SET CurrentServer = 'Offline'");
    }

    public void destroy() {
        this.tasks.shutdown();
        this.db.destroy();

        SmartFoxServer.log.info("World destroyed.");
    }

    public final boolean retrieveDatabaseObject(String type) {
        if (type.equals("item")) {
            HashMap<Integer, Item> itemsData = new HashMap<Integer, Item>(this.db.jdbc.queryForMap("SELECT * FROM items WHERE id > 0", Item.resultSetMapper));

            for (Item item : itemsData.values()) {
                Map<Integer, Integer> requirements = new HashMap<Integer, Integer>(this.db.jdbc.queryForMap("SELECT * FROM items_requirements WHERE ItemID = ?", Item.requirementMapper, item.getId()));
                item.requirements = requirements;

                if (item.getEquipment().equals(Item.EQUIPMENT_CLASS) && !item.getType().equals("Enhancement")) {
                    Class classObj = this.db.jdbc.queryForObject("SELECT * FROM classes WHERE ItemID = ?", Class.beanCreator, item.getId());

                    if (classObj == null)
                        throw new NullPointerException("An item with the equipment type 'Class' does not have a matching id in the classes table. ItemID: " + item.getId());

                    classObj.skills = this.db.jdbc.queryForObject("SELECT id FROM skills WHERE ItemID = ?", Class.beanSkills, item.getId());

                    if (classObj.skills == null)
                        throw new NullPointerException("A class contains an empty skill set, please delete this item first. ItemID: " + item.getId());

                    item.classObj = classObj;
                }
            }

            this.items = itemsData;

            HashMap<Integer, Skill> skillsData = new HashMap<Integer, Skill>(this.db.jdbc.queryForMap("SELECT * FROM skills WHERE id > 0", Skill.resultSetMapper));
            this.skills = skillsData;

            HashMap<Integer, Aura> aurasData = new HashMap<Integer, Aura>(this.db.jdbc.queryForMap("SELECT * FROM skills_auras WHERE id > 0", Aura.resultSetMapper));
            this.auras = aurasData;

            for (Aura aura : aurasData.values()) {
                aura.effects = this.db.jdbc.queryForObject("SELECT * FROM skills_auras_effects WHERE AuraID = ?", Aura.beanEffects, aura.getId());

                if (aura.effects == null)
                    aura.effects = Collections.EMPTY_SET;
            }

            HashMap<Integer, AuraEffects> auraEffectsData = new HashMap<Integer, AuraEffects>(this.db.jdbc.queryForMap("SELECT * FROM skills_auras_effects", AuraEffects.resultSetMapper));
            this.effects = auraEffectsData;

            HashMap<Integer, Hair> hairsData = new HashMap<Integer, Hair>(this.db.jdbc.queryForMap("SELECT * FROM hairs", Hair.resultSetMapper));
            this.hairs = hairsData;

            HashMap<Integer, Enhancement> enhancementsData = new HashMap<Integer, Enhancement>(this.db.jdbc.queryForMap("SELECT * FROM enhancements", Enhancement.resultSetMapper));
            this.enhancements = enhancementsData;

            HashMap<Integer, EnhancementPattern> patternsData = new HashMap<Integer, EnhancementPattern>(this.db.jdbc.queryForMap("SELECT * FROM enhancements_patterns WHERE id > 0", EnhancementPattern.resultSetMapper));
            this.patterns = patternsData;

            HashMap<Integer, String> factionsData = new HashMap<Integer, String>(this.db.jdbc.queryForMap("SELECT * FROM factions", World.factionsMapper));
            this.factions = factionsData;

            SmartFoxServer.log.info("Item objects retrieved.");
        } else if (type.equals("map")) {
            HashMap<String, Area> areasData = new HashMap<String, Area>(this.db.jdbc.queryForMap("SELECT * FROM maps", Area.resultSetMapper));

            for (Area area : areasData.values()) {
                area.monsters = this.db.jdbc.queryForObject("SELECT * FROM maps_monsters WHERE MapID = ?", MapMonster.setCreator, area.getId());
                if (area.monsters == null)
                    area.monsters = Collections.EMPTY_SET;
                area.items = this.db.jdbc.queryForObject("SELECT * FROM maps_items WHERE MapID = ?", Area.beanItems, area.getId());
                if (area.items == null)
                    area.items = Collections.EMPTY_SET;
                area.cells = this.db.jdbc.queryForMap("SELECT * FROM maps_cells WHERE MapID = ?", Cell.resultSetMapper, area.getId());
            }
            
            if(this.areas != null) {
                HashMap<String, Area> oldAreas = new HashMap<String, Area>(this.areas);

                for (Iterator<Map.Entry<String, Area>> it = oldAreas.entrySet().iterator(); it.hasNext();) {
                    Map.Entry<String, Area> entry = it.next();
                    if(!entry.getKey().contains("house-")) it.remove();
                }

                areasData.putAll(oldAreas);
            }
            
            this.areas = areasData;

            HashMap<Integer, Monster> monstersData = new HashMap<Integer, Monster>(this.db.jdbc.queryForMap("SELECT * FROM monsters", Monster.resultSetMapper));

            for (Monster monster : monstersData.values()) {
                monster.drops = this.db.jdbc.queryForObject("SELECT * FROM monsters_drops WHERE MonsterID = ?", Monster.beanDrops, monster.getId());

                if (monster.drops == null)
                    monster.drops = Collections.EMPTY_SET;
                
                monster.skills = this.db.jdbc.queryForObject("SELECT * FROM monsters_skills WHERE MonsterID = ?", Monster.beanSkills, monster.getId());
                
                if (monster.skills == null)
                    monster.skills = Collections.EMPTY_SET;
            }
            this.monsters = monstersData;

            SmartFoxServer.log.info("Map objects retrieved.");
        } else if (type.equals("quest")) {
            HashMap<Integer, Quest> questsData = new HashMap<Integer, Quest>(this.db.jdbc.queryForMap("SELECT * FROM quests", Quest.resultSetMapper));

            for (Quest quest : questsData.values()) {
                quest.rewards = this.db.jdbc.queryForMap("SELECT * FROM quests_rewards WHERE QuestID = ?", Quest.requirementsRewardsMapper, quest.getId());
                quest.requirements = this.db.jdbc.queryForMap("SELECT * FROM quests_requirements WHERE QuestID = ?", Quest.requirementsRewardsMapper, quest.getId());
                quest.locations = this.db.jdbc.queryForObject("SELECT * FROM quests_locations WHERE QuestID = ?", Quest.beanLocations, quest.getId());

                if (quest.locations == null)
                    quest.locations = Collections.EMPTY_SET;
            }

            this.quests = questsData;

            SmartFoxServer.log.info("Quest objects retrieved.");
        } else if (type.equals("shop")) {
            HashMap<Integer, Shop> shopsData = new HashMap<Integer, Shop>(this.db.jdbc.queryForMap("SELECT * FROM shops", Shop.resultSetMapper));

            for (Shop shop : shopsData.values()) {
                shop.items = this.db.jdbc.queryForMap("SELECT id, ItemID FROM shops_items WHERE ShopID = ?", Shop.shopItemsMapper, shop.getId());

                shop.locations = this.db.jdbc.queryForObject("SELECT * FROM shops_locations WHERE ShopID = ?", Shop.beanLocations, shop.getId());

                if (shop.locations == null)
                    shop.locations = Collections.EMPTY_SET;
            }

            this.shops = shopsData;

            HashMap<Integer, Hairshop> hairshopsData = new HashMap<Integer, Hairshop>(this.db.jdbc.queryForMap("SELECT * FROM hairs_shops", Hairshop.resultSetMapper));

            for (Hairshop hairshop : hairshopsData.values()) {
                hairshop.male = this.db.jdbc.queryForObject("SELECT * FROM hairs_shops_items WHERE Gender = ? AND ShopID = ?", Hairshop.beanHairshopItems, "M", hairshop.getId());
                hairshop.female = this.db.jdbc.queryForObject("SELECT * FROM hairs_shops_items WHERE Gender = ? AND ShopID = ?", Hairshop.beanHairshopItems, "F", hairshop.getId());
            }

            this.hairshops = hairshopsData;

            SmartFoxServer.log.info("Shop objects retrieved.");
        } else if (type.equals("enhshop")) {
            HashMap<Integer, Enhancement> enhancementsData = new HashMap<Integer, Enhancement>(this.db.jdbc.queryForMap("SELECT * FROM enhancements", Enhancement.resultSetMapper));
            this.enhancements = enhancementsData;

            HashMap<Integer, EnhancementPattern> patternsData = new HashMap<Integer, EnhancementPattern>(this.db.jdbc.queryForMap("SELECT * FROM enhancements_patterns WHERE id > 0", EnhancementPattern.resultSetMapper));
            this.patterns = patternsData;

            SmartFoxServer.log.info("Enhancements objects retrieved.");
        } else if (type.equals("settings")) {
            this.messageOfTheDay = this.db.jdbc.queryForString("SELECT MOTD FROM servers WHERE Name = ?", ConfigData.SERVER_NAME);
            this.newsString = this.db.jdbc.queryForObject("SELECT * FROM settings_login", World.newsCreator);
            HashMap<String, Double> coreValuesData = new HashMap<String, Double>(this.db.jdbc.queryForMap("SELECT * FROM settings_rates", World.coreValuesMapper));
            this.coreValues = coreValuesData;

            SmartFoxServer.log.info("Server settings retrieved.");
        } else if (type.equals("all")) {
            retrieveDatabaseObject("item");
            retrieveDatabaseObject("map");
            retrieveDatabaseObject("quest");
            retrieveDatabaseObject("shop");
            retrieveDatabaseObject("settings");
        } else
            throw new IllegalArgumentException("Type not found");

        return true;
    }

    public ScheduledFuture<?> scheduleTask(Runnable task, long delay, TimeUnit tu) {
        return scheduleTask(task, delay, tu, false);
    }

    public ScheduledFuture<?> scheduleTask(Runnable task, long delay, TimeUnit tu, boolean repeat) {
        if (repeat)
            return this.tasks.scheduleAtFixedRate(task, delay, delay, tu);
        else
            return this.tasks.schedule(task, delay, tu);
    }

    public int roundTens(int val) {
        int x = val;
        for (int i = 0; (i < 9) && (x % 10 != 0); i++)
            x++;
        return x;
    }

    public int getExpToLevel(int playerLevel) {
        if (playerLevel < this.coreValues.get("intLevelMax").intValue())
            return roundTens(getBaseValueByLevel(1000, 850000, 1.66, playerLevel).intValue());
        return 200000000;
    }

    public int getManaByLevel(int level) {
        int base = this.coreValues.get("PCmpBase1").intValue();
        int delta = this.coreValues.get("PCmpBase100").intValue();
        double curve = this.coreValues.get("curveExponent") + ((double) base / delta);

        return getBaseValueByLevel(base, delta, curve, level).intValue();
    }

    public int getHealthByLevel(int level) {
        int base = this.coreValues.get("PChpGoal1").intValue();
        int delta = this.coreValues.get("PChpGoal100").intValue();
        double curve = 1.5 + ((double) base / delta);

        return getBaseValueByLevel(base, delta, curve, level).intValue();
        //return Health.getHealthByLevel(level);
    }

    public int getBaseHPByLevel(int level) {
        int base = this.coreValues.get("PChpBase1").intValue();
        double curve = this.coreValues.get("curveExponent");
        int delta = this.coreValues.get("PChpDelta").intValue();

        return getBaseValueByLevel(base, delta, curve, level).intValue();
    }

    public int getIBudget(int itemLevel, int iRty) {
        int GstBase = this.coreValues.get("GstBase").intValue();
        int GstGoal = this.coreValues.get("GstGoal").intValue();
        double statsExponent = this.coreValues.get("statsExponent");
        int rarity = iRty < 1 ? 1 : iRty;
        int level = (itemLevel + rarity - 1);
        int delta = (GstGoal - GstBase);

        return getBaseValueByLevel(GstBase, delta, statsExponent, level).intValue();
    }

    public int getInnateStats(int userLevel) {
        int PCstBase = this.coreValues.get("PCstBase").intValue();
        int PCstGoal = this.coreValues.get("PCstGoal").intValue();
        double statsExponent = this.coreValues.get("statsExponent");
        int delta = (PCstGoal - PCstBase);
        return getBaseValueByLevel(PCstBase, delta, statsExponent, userLevel).intValue();
    }

    public Double getBaseValueByLevel(int base, int delta, double curve, int userLevel) {
        int levelCap = this.coreValues.get("intLevelCap").intValue();
        int level = userLevel < 1 ? 1 : userLevel > levelCap ? levelCap : userLevel;

        double x = ((double) (level - 1) / (levelCap - 1));
        return base + Math.pow(x, curve) * delta;
    }

    public Map<String, Double> getItemStats(Enhancement enhancement, String equipment) {
        Map<String, Double> itemStats = new LinkedHashMap<String, Double>();

        itemStats.put("END", 0.0);
        itemStats.put("STR", 0.0);
        itemStats.put("INT", 0.0);
        itemStats.put("DEX", 0.0);
        itemStats.put("WIS", 0.0);
        itemStats.put("LCK", 0.0);

        if (enhancement != null) {

            int patternId = enhancement.getPatternId();
            int rarity = enhancement.getRarity();
            int level = enhancement.getLevel();
            int iBudget = (int) Math.round(getIBudget(level, rarity) * Stats.ratioByEquipment.get(equipment));

            Map<String, Integer> statPattern = patterns.get(patternId).getStats();

            Set<String> keyEntry = itemStats.keySet();

            double valTotal = 0;

            for (String key : keyEntry) {
                double stat = ((iBudget * statPattern.get(key)) / 100);
                itemStats.put(key, stat);
                valTotal = (valTotal + stat);
            }

            Object[] keyArray = keyEntry.toArray();

            int i = 0;
            while (valTotal < iBudget) {
                String key = (String) keyArray[i];
                double statVal = (itemStats.get(key) + 1);
                itemStats.put(key, statVal);

                valTotal++;

                i++;
                if (i > (keyArray.length - 1))
                    i = 0;
            }
        }
        return itemStats;
    }

    /**
     * An import of SmartFoxServer's anti flood filter.
     */
    public void applyFloodFilter(User user, String message) {
        long lastMsgTime = ((Long) user.properties.get(Users.LAST_MESSAGE_TIME) + ConfigData.ANTI_MESSAGEFLOOD_MIN_MSG_TIME);

        if (lastMsgTime >= System.currentTimeMillis()) {
            user.floodCounter++;
            if (user.floodCounter >= ConfigData.ANTI_MESSAGEFLOOD_TOLERANCE) {
                user.floodWarningsCounter++;
                user.floodCounter = 0;

                this.send(new String[]{"warning", "Please do not flood the server with messages."}, user);
            }
        } else
            user.floodCounter = 0;
        if (message.equals(user.lastMessage)) {
            user.repeatedMsgCounter++;
            if (user.repeatedMsgCounter >= ConfigData.ANTI_MESSAGEFLOOD_MAX_REPEATED) {
                user.floodWarningsCounter++;
                user.repeatedMsgCounter = 0;

                this.send(new String[]{"warning", "Please do not flood the server with messages."}, user);
            }
        } else {
            user.repeatedMsgCounter = 0;
            user.lastMessage = message;
        }
        if (user.floodWarningsCounter >= ConfigData.ANTI_MESSAGEFLOOD_WARNINGS) {
            this.users.mute(user, 2, Calendar.MINUTE);

            user.floodWarningsCounter = 0;
        }
        user.properties.put(Users.LAST_MESSAGE_TIME, System.currentTimeMillis());
    }

    //<editor-fold defaultstate="collapsed" desc="Send Functions">
    public void sendServerMessage(String message) {
        JSONObject umsg = new JSONObject();
        umsg.put("cmd", "umsg");
        umsg.put("s", message);

        send(umsg, this.zone.getChannelList());
    }

    public void sendToUsers(JSONObject params) {
        ext.sendResponse(params, -1, null, zone.getChannelList());
    }

    public void sendToUsers(String[] params) {
        ext.sendResponse(params, -1, null, zone.getChannelList());
    }

    public void send(JSONObject params, LinkedList<SocketChannel> channels) {
        ext.sendResponse(params, -1, null, channels);
    }

    public void send(JSONObject params, User user) {
        if (user == null || params == null)
            return;
        LinkedList<SocketChannel> channels = new LinkedList<SocketChannel>();
        channels.add(user.getChannel());
        ext.sendResponse(params, -1, user, channels);
    }

    public void send(String[] params, SocketChannel chan) {
        if (chan == null || params == null)
            return;
        LinkedList<SocketChannel> channels = new LinkedList<SocketChannel>();
        channels.add(chan);
        ext.sendResponse(params, -1, null, channels);
    }

    public void send(String[] params, User user) {
        if (user == null || params == null)
            return;
        LinkedList<SocketChannel> channels = new LinkedList<SocketChannel>();
        channels.add(user.getChannel());
        ext.sendResponse(params, -1, user, channels);
    }

    public void send(String[] params, LinkedList<SocketChannel> channels) {
        ext.sendResponse(params, -1, null, channels);
    }

    public void sendToRoom(JSONObject params, User user, Room room) {
        if (user == null || room == null)
            return;
        if (user.getRoom() == room.getId())
            ext.sendResponse(params, -1, null, room.getChannellList());
        else
            this.users.kick(user);
    }

    public void sendToRoom(String[] params, User user, Room room) {
        if (user == null || room == null)
            return;
        if (user.getRoom() == room.getId())
            ext.sendResponse(params, -1, null, room.getChannellList());
        else
            this.users.kick(user);
    }

    public void sendToRoomButOne(JSONObject o, User _user, Room room) {
        if (_user == null || room == null)
            return;
        User[] _users = room.getAllUsersButOne(_user);
        LinkedList<SocketChannel> channels = new LinkedList<SocketChannel>();
        for (User user : _users) {
            if (user == null)
                continue;
            channels.add(user.getChannel());
        }

        send(o, channels);
    }

    public void sendToRoomButOne(String[] o, User _user, Room room) {
        if (_user == null || room == null)
            return;
        User[] _users = room.getAllUsersButOne(_user);
        LinkedList<SocketChannel> channels = new LinkedList<SocketChannel>();
        for (User user : _users) {
            if (user == null)
                continue;
            channels.add(user.getChannel());
        }

        send(o, channels);
    }

    public void sendToGuild(JSONObject params, JSONObject guildObj) {
        JSONArray members = (JSONArray) guildObj.get("ul");
        if (members != null && members.size() > 0)
            for (Iterator<JSONObject> it = members.iterator(); it.hasNext();) {
                JSONObject member = it.next();
                User guildMember = this.zone.getUserByName(member.get("userName").toString().toLowerCase());
                if (guildMember != null)
                    this.send(params, guildMember);
            }
    }

    public void sendToGuild(String[] params, JSONObject guildObj) {
        JSONArray members = (JSONArray) guildObj.get("ul");
        if (members != null && members.size() > 0)
            for (Iterator<JSONObject> it = members.iterator(); it.hasNext();) {
                JSONObject member = it.next();
                User guildMember = this.zone.getUserByName(member.get("userName").toString().toLowerCase());
                if (guildMember != null)
                    this.send(params, guildMember);
            }
    }

    public void sendGuildUpdate(JSONObject guildObj) {
        sendGuildUpdateButOne(null, guildObj);
    }

    public void sendGuildUpdateButOne(User user, JSONObject guildObj) {
        JSONObject updateGuild = new JSONObject();

        JSONArray members = (JSONArray) guildObj.get("ul");
        if (members != null && members.size() > 0)
            for (Iterator<JSONObject> it = members.iterator(); it.hasNext();) {
                JSONObject member = it.next();
                User guildMember = this.zone.getUserByName(member.get("userName").toString().toLowerCase());
                if (guildMember != null && !(user != null && guildMember.equals(user))) {
                    guildMember.properties.put(Users.GUILD, guildObj);
                    updateGuild.put("cmd", "updateGuild");
                    updateGuild.put("guild", guildObj);

                    this.send(updateGuild, guildMember);
                }
            }
    }
    //</editor-fold>
}

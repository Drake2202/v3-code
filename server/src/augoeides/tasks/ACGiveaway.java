/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.tasks;

import augoeides.world.Users;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.List;
import java.util.Random;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class ACGiveaway implements Runnable {

    private World world;
    private Random rand;

    public ACGiveaway(World world) {
        this.world = world;
        this.rand = new Random(System.currentTimeMillis());

        SmartFoxServer.log.info("ACGiveaway event initialized.");
    }

    private User getRandomUser() {
        List<User> users = (List<User>) this.world.zone.getUserList();
        User user = users.get(this.rand.nextInt(users.size()));

        if (user.isAdmin() || user.isModerator())
            return getRandomUser();
        else
            return user;
    }

    @Override
    public void run() {
        int total = this.world.zone.getUserCount();
        if (total <= 0)
            return;

        User target = getRandomUser();

        this.world.sendServerMessage("Congratulations! <font color=\"#ffffff\"><a href=\"http://augoeides.org/?profile=" + target.getName() + "\" target=\"_blank\">" + target.properties.get(Users.USERNAME) + "</a></font> has won <font color=\"#ffffff\">500</font> AdventureCoins!");
        this.world.send(new String[]{"administrator", "Congratulations! You just won 500 AdventureCoins!"}, target);
        this.world.sendToUsers(new String[]{"administrator", "Congratulations! <font color=\"#ffffff\">" + target.properties.get(Users.USERNAME) + "</font> has won 500 AdventureCoins!"});

        JSONObject sell = new JSONObject();
        sell.put("cmd", "sellItem");
        sell.put("intAmount", 500);
        sell.put("CharItemID", target.hashCode());
        sell.put("bCoins", 1);

        this.world.send(sell, target);

        this.world.db.jdbc.run("UPDATE users SET Coins = (Coins + ?) WHERE id = ?", 500, target.properties.get(Users.DATABASE_ID));

        SmartFoxServer.log.info("User [ " + target.getName() + " ] won 500 AdventureCoins.");
        try {
            Thread.sleep(5000);
            this.world.sendServerMessage("The next lucky winner will be selected randomly in the next 30 minutes.");
        } catch (InterruptedException ex) {
        }
    }

}

/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.tasks;

import augoeides.world.World;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.User;
import it.gotoandplay.smartfoxserver.extensions.ExtensionHelper;

/**
 *
 * @author Mystical
 */
public class KickUser implements Runnable {

    private User user;
    private World world;
    private ExtensionHelper helper;

    public KickUser(User user, World world) {
        this.user = user;
        this.world = world;
        this.helper = ExtensionHelper.instance();
    }

    @Override
    public void run() {
        try {
            this.world.send(new String[]{"logoutWarning", "", "60"}, user);
            Thread.sleep(1000);
            this.helper.disconnectUser(user);
            SmartFoxServer.log.info("User [ " + user.getName() + " ] has been kicked.");
        } catch (InterruptedException ex) {
        }
    }

}

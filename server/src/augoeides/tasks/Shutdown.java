/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.tasks;

import augoeides.world.World;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Mystical
 */
public class Shutdown implements Runnable {

    private World world;
    private User user;

    public Shutdown(World world, User user) {
        this.world = world;
        this.user = user;
    }

    @Override
    public void run() {
        try {
            this.world.send(new String[]{"server", "Server shutting down in 5 minutes."}, world.zone.getChannelList());
            Thread.sleep(TimeUnit.MINUTES.toMillis(4));
            this.world.send(new String[]{"warning", "Server shutting down in 1 minute."}, world.zone.getChannelList());
            Thread.sleep(TimeUnit.MINUTES.toMillis(1));
            this.world.send(new String[]{"logoutWarning", "", "60"}, world.zone.getChannelList());
            this.world.shutdown();
            Thread.sleep(TimeUnit.SECONDS.toMillis(2));
            SmartFoxServer.getInstance().halt(this.user);
            Thread.sleep(TimeUnit.SECONDS.toMillis(5));
            System.exit(0);
        } catch (InterruptedException ex) {
        }
    }

}

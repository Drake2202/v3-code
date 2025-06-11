/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.tasks;

import augoeides.world.World;
import it.gotoandplay.smartfoxserver.extensions.ExtensionHelper;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Mystical
 */
public class Restart implements Runnable {

    private World world;

    public Restart(World world) {
        this.world = world;
    }

    @Override
    public void run() {
        try {
            this.world.send(new String[]{"server", "Server restarting in 5 minutes."}, world.zone.getChannelList());
            Thread.sleep(TimeUnit.MINUTES.toMillis(4));
            this.world.send(new String[]{"warning", "Server restarting in 1 minute."}, world.zone.getChannelList());
            Thread.sleep(TimeUnit.MINUTES.toMillis(1));
            this.world.send(new String[]{"logoutWarning", "", "60"}, world.zone.getChannelList());
            this.world.shutdown();
            Thread.sleep(TimeUnit.SECONDS.toMillis(2));
            ExtensionHelper.instance().rebootServer();
        } catch (InterruptedException ex) {
        }
    }

}

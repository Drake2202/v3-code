/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */

package augoeides.avatars;

import augoeides.db.objects.Aura;
import augoeides.tasks.RemoveAura;
import augoeides.world.Users;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Mystical
 */
public class UserState extends State {
    
    private World world;
    private User user;

    public UserState(World world, User user, int maxHealth, int maxMana) {
        super(maxHealth, maxMana);
        this.world = world;
        this.user = user;
    }
    
    @Override
    public void die() {
        super.die();
        super.clearAuras();
        user.properties.put(Users.RESPAWN_TIME, System.currentTimeMillis());
    }

    @Override
    public RemoveAura applyAura(Aura aura) {
        RemoveAura ra = new RemoveAura(this.world, aura, this.user);

        ra.setRunning(this.world.scheduleTask(ra, aura.getDuration(), TimeUnit.SECONDS));
        addAura(ra);

        return ra;
    }
    
}

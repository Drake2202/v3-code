/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.tasks;

import augoeides.avatars.State;
import augoeides.world.Users;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.concurrent.ScheduledFuture;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class Regeneration implements Runnable, CancellableTask {

    private User user;
    private World world;
    private ScheduledFuture<?> running;

    public Regeneration(User user, World world) {
        this.user = user;
        this.world = world;
    }

    @Override
    public void run() {
        if (this.running == null || this.user == null)
            throw new RuntimeException("regen handle is null, unable to continue.");
        
        State state = (State) user.properties.get(Users.USER_STATE);

        if (state.isNeutral() && (state.getHealth() < state.getMaxHealth() || 
                state.getMana() < state.getMaxMana())) {
            
            state.increaseHealthByPercent(0.025);
            state.increaseManaByPercent(0.025);

            JSONObject ct = new JSONObject();
            JSONObject p = new JSONObject();

            p.put(this.user.getName(), state.getData());

            ct.put("cmd", "ct");
            ct.put("p", p);

            this.world.send(ct, this.world.zone.getRoom(this.user.getRoom()).getChannellList());
        } else
            this.running.cancel(false);
    }

    public boolean isRegenerating() {
        return (this.running != null && !this.running.isDone());
    }

    @Override
    public void setRunning(ScheduledFuture<?> running) {
        if (this.running == null || this.running.isDone())
            this.running = running;
        else
            running.cancel(true);
    }

    @Override
    public void cancel() {
        if (this.running != null)
            this.running.cancel(false);
    }

}

/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.tasks;

import java.util.concurrent.ScheduledFuture;

/**
 *
 * @author Mystical
 */
public interface CancellableTask {

    void setRunning(ScheduledFuture<?> running);

    void cancel();
}

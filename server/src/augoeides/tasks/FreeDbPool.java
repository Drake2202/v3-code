/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.tasks;

import augoeides.db.Database;

/**
 *
 * @author Mystical
 */
public class FreeDbPool implements Runnable {

    private Database db;

    public FreeDbPool(Database db) {
        this.db = db;
    }

    @Override
    public void run() {
        this.db.freeIdleConnections();
    }

}

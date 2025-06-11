/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */

package augoeides.combat;

/**
 *
 * @author Mystical
 */
public enum DamageType {
    HIT("hit"),
    MISS("miss"),
    DODGE("dodge"),
    CRITICAL("crit"),
    DAMAGE_OVER_TIME("d"),
    NONE("none");
    
    private String type;
    
    private DamageType(String type) {
        this.type = type;
    }
    
    @Override
    public String toString() {
        return type;
    }
}

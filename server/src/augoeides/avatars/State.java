/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.avatars;

import augoeides.db.objects.Aura;
import augoeides.tasks.RemoveAura;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public abstract class State {

    private int maxHealth, maxMana;
    private volatile int health, mana, state;
    private Set<RemoveAura> auras;

    public State(int maxHealth, int maxMana) {
        this.health = maxHealth;
        this.mana = maxMana;
        this.maxHealth = maxHealth;
        this.maxMana = maxMana;
        this.state = 1;
        this.auras = Collections.newSetFromMap(new ConcurrentHashMap<RemoveAura, Boolean>());
    }

    public int getHealth() {
        return health;
    }

    public boolean isNeutral() {
        return this.state == 1;
    }

    public boolean isDead() {
        return this.state == 0;
    }

    public boolean onCombat() {
        return this.state == 2;
    }

    public void setHealth(int health) {
        if (health <= 0) die();
        else if (health > maxHealth) this.health = maxHealth;
        else this.health = health;
    }

    public int getMana() {
        return mana;
    }

    public void decreaseHealth(int health) {
        setHealth(getHealth() - health);
    }

    public void increaseHealth(int health) {
        setHealth(getHealth() + health);
    }

    public void decreaseMana(int mana) {
        setMana(getMana() - mana);
    }

    public void increaseMana(int mana) {
        setMana(getMana() + mana);
    }

    public void decreaseHealthByPercent(double percent) {
        int amount = (int) (maxHealth * percent);
        setHealth(getHealth() - amount);
    }

    public void increaseHealthByPercent(double percent) {
        int amount = (int) (maxHealth * percent);
        setHealth(getHealth() + amount);
    }

    public void decreaseManaByPercent(double percent) {
        int amount = (int) (maxMana * percent);
        setMana(getMana() - amount);
    }

    public void increaseManaByPercent(double percent) {
        int amount = (int) (maxMana * percent);
        setMana(getMana() + amount);
    }

    public void die() {
        this.health = 0;
        this.state = 0;
        this.mana = 0;
    }

    public void restore() {
        this.state = 1;
        this.health = maxHealth;
        this.mana = maxMana;
    }

    public void setMana(int mana) {
        if (mana < 0) this.mana = 0;
        else if (mana > maxMana) this.mana = maxMana;
        else this.mana = mana;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        if (state < 0 || state > 2) throw new IllegalArgumentException("invalid state value");
        this.state = state;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public int getMaxMana() {
        return maxMana;
    }
    
    public JSONObject getData() {
        JSONObject data = new JSONObject();
        
        data.put("intMP", this.mana);
        data.put("intHP", this.health);
        data.put("intState", this.state);
        
        return data;
    }

    public void setMaxMana(int maxMana) {
        this.maxMana = maxMana;
    }

    public boolean isDisabled() {
        for (RemoveAura ra : this.auras) {
            Aura aura = ra.getAura();
            if (aura.getCategory().equals("stun") || aura.getCategory().equals("freeze")
                    || aura.getCategory().equals("stone") || aura.getCategory().equals("disabled"))
                return true;
        }
        return false;
    }

    public void clearAuras() {
        for (RemoveAura ra : this.auras) {
            ra.run();
            ra.cancel();
        }
        this.auras.clear();
    }

    public boolean hasAura(int auraId) {
        for (RemoveAura ra : this.auras) {
            Aura aura = ra.getAura();
            if (aura.getId() == auraId)
                return true;
        }
        return false;
    }

    public Set<RemoveAura> getAuras() {
        return Collections.unmodifiableSet(auras);
    }

    public void removeAura(RemoveAura ra) {
        this.auras.remove(ra);
    }

    public void addAura(RemoveAura ra) {
        if (!this.auras.contains(ra))
            this.auras.add(ra);
    }

    public abstract RemoveAura applyAura(Aura aura);
}

/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.combat;

import augoeides.db.objects.Aura;
import augoeides.tasks.RemoveAura;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import java.util.Random;
import java.util.Set;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class Damage {

    private static Random rand = new Random();

    private Damage() {
        throw new UnsupportedOperationException("not allowed to have an instance of this class");
    }

    public static JSONObject getDamageResult(String fromTgt, String tgt, int damage, DamageType type) {
        JSONObject damageResult = new JSONObject();
        damageResult.put("hp", damage);
        damageResult.put("cInf", fromTgt);
        damageResult.put("tInf", tgt);
        
        if (type.equals(DamageType.DAMAGE_OVER_TIME))
            damageResult.put("typ", type.toString());
        else
            damageResult.put("type", type.toString());
        
        return damageResult;
    }

    public static JSONObject getDamageResult(String tgt, int damage, DamageType type) {
        JSONObject damageResult = new JSONObject();
        damageResult.put("hp", damage);
        damageResult.put("tInf", tgt);
        
        if (type.equals(DamageType.DAMAGE_OVER_TIME))
            damageResult.put("typ", type.toString());
        else
            damageResult.put("type", type.toString());
        
        return damageResult;
    }

    public static int getRandomDamage(DamageType type, int maxDmg, int minDmg) {
        int damage = rand.nextInt(maxDmg - minDmg) + minDmg;
        if (type.equals(DamageType.CRITICAL)) damage = (int) (damage * 1.5);
        if (type.equals(DamageType.DODGE) || type.equals(DamageType.MISS) || type.equals(DamageType.NONE))
            damage = 0;

        return damage;
    }

    public static DamageType evaluateDamageType(double hitChance, double dodgeChance, double critChance) {
        SmartFoxServer.log.fine("HIT CHANCE > " + hitChance);

        boolean crit = (Math.random() < critChance);
        boolean dodge = (Math.random() < dodgeChance);
        boolean miss = (Math.random() < hitChance);

        if (crit) return DamageType.CRITICAL;
        if (dodge) return DamageType.DODGE;
        if (miss) return DamageType.MISS;
        return DamageType.HIT;
    }

    public static int evaluateAuras(int damage, Set<RemoveAura> auras) {
        return getReducedDamage(getIncreasedDamage(damage, auras), auras);
    }

    public static int getIncreasedDamage(int damage, Set<RemoveAura> auras) {
        for (RemoveAura ra : auras) {
            Aura aura = ra.getAura();
            if (!aura.getCategory().equals("d"))
                damage = (int) (damage * (1 + aura.getDamageIncrease()));
        }

        return damage;
    }

    public static int getReducedDamage(int damage, Set<RemoveAura> auras) {
        for (RemoveAura ra : auras) {
            Aura aura = ra.getAura();
            if (!aura.getCategory().equals("d"))
                damage = (int) (damage * (1 - aura.getDamageTakenDecrease()));
        }
        return damage;
    }
}

/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.world.stats;

import augoeides.db.objects.AuraEffects;
import augoeides.db.objects.Enhancement;
import augoeides.db.objects.Item;
import augoeides.db.objects.Skill;
import augoeides.world.Users;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class Stats {

    public static final Map<String, List<Double>> classCatMap;
    public static final Map<String, Double> ratioByEquipment;

    static {
        List<Double> M1 = Arrays.asList(0.27, 0.3, 0.22, 0.05, 0.1, 0.06);
        List<Double> M2 = Arrays.asList(0.2, 0.22, 0.33, 0.05, 0.1, 0.1);
        List<Double> M3 = Arrays.asList(0.24, 0.2, 0.2, 0.24, 0.07, 0.05);
        List<Double> M4 = Arrays.asList(0.3, 0.18, 0.3, 0.02, 0.06, 0.14);
        List<Double> C1 = Arrays.asList(0.06, 0.2, 0.11, 0.33, 0.15, 0.15);
        List<Double> C2 = Arrays.asList(0.08, 0.27, 0.1, 0.3, 0.1, 0.15);
        List<Double> C3 = Arrays.asList(0.06, 0.23, 0.05, 0.28, 0.28, 0.1);
        List<Double> S1 = Arrays.asList(0.22, 0.18, 0.21, 0.08, 0.08, 0.23);

        Map<String, List<Double>> catMap = new HashMap<String, List<Double>>(8);

        catMap.put("M1", M1);
        catMap.put("M2", M2);
        catMap.put("M3", M3);
        catMap.put("M4", M4);
        catMap.put("C1", C1);
        catMap.put("C2", C2);
        catMap.put("C3", C3);
        catMap.put("S1", S1);

        classCatMap = catMap;

        Map<String, Double> ratioEquip = new HashMap<String, Double>(4);

        ratioEquip.put("he", 0.25);
        ratioEquip.put("ar", 0.25);
        ratioEquip.put("ba", 0.2);
        ratioEquip.put("Weapon", 0.33);

        ratioByEquipment = ratioEquip;
    }

    private double $cai = 1;
    private double $cao = 1;
    private double $cdi = 1;
    private double $cdo = 1;
    private double $chi = 1;
    private double $cho = 1;
    private double $cmc = 1;
    private double $cmi = 1;
    private double $cmo = 1;
    private double $cpi = 1;
    private double $cpo = 1;
    private double $sbm = 0.7;
    private double $scm = 1.5;
    private double $sem = 0.05;
    private double $shb = 0;
    private double $smb = 0;
    private double $srm = 0.7;

    private double attackPower = 0;
    private double magicPower = 0;
    private double block = 0;
    private double criticalHit = 0.05;
    private double evasion = 0.04;
    private double haste = 0;
    private double hit = 0;
    private double parry = 0.03;
    private double resist = 0.7;

    private double _ap = 0;
    private double _cai = 1;
    private double _cao = 1;
    private double _cdi = 1;
    private double _cdo = 1;
    private double _chi = 1;
    private double _cho = 1;
    private double _cmc = 1;
    private double _cmi = 1;
    private double _cmo = 1;
    private double _cpi = 1;
    private double _cpo = 1;
    private double _sbm = 0.7;
    private double _scm = 1.5;
    private double _sem = 0.05;
    private double _shb = 0;
    private double _smb = 0;
    private double _sp = 0; //magic power
    private double _srm = 0.7;
    private double _tbl = 0;
    private double _tcr = 0; //total crit
    private double _tdo = 0; //total dodge
    private double _tha = 0; //total haste
    private double _thi = 0; //total hit
    private double _tpa = 0;
    private double _tre = 0;

    private int minDmg = 0;
    private int maxDmg = 0;

    public int wDPS = 0;
    //private int mDPS = 0;

    public Map<String, Double> innate = new LinkedHashMap<String, Double>(6);
    public Map<String, Double> weapon = new LinkedHashMap<String, Double>(6);
    public Map<String, Double> helm = new LinkedHashMap<String, Double>(6);
    public Map<String, Double> armor = new LinkedHashMap<String, Double>(6);
    public Map<String, Double> cape = new LinkedHashMap<String, Double>(6);

    public Set<AuraEffects> effects = new LinkedHashSet<AuraEffects>();

    private User user;
    private World world;

    public Stats(User user, World world) {
        //IN ORDER DO NOT TOUCH
        innate.put("STR", 0.0);
        innate.put("END", 0.0);
        innate.put("DEX", 0.0);
        innate.put("INT", 0.0);
        innate.put("WIS", 0.0);
        innate.put("LCK", 0.0);

        weapon.put("STR", 0.0);
        weapon.put("END", 0.0);
        weapon.put("DEX", 0.0);
        weapon.put("INT", 0.0);
        weapon.put("WIS", 0.0);
        weapon.put("LCK", 0.0);

        helm.put("STR", 0.0);
        helm.put("END", 0.0);
        helm.put("DEX", 0.0);
        helm.put("INT", 0.0);
        helm.put("WIS", 0.0);
        helm.put("LCK", 0.0);

        armor.put("STR", 0.0);
        armor.put("END", 0.0);
        armor.put("DEX", 0.0);
        armor.put("INT", 0.0);
        armor.put("WIS", 0.0);
        armor.put("LCK", 0.0);

        cape.put("STR", 0.0);
        cape.put("END", 0.0);
        cape.put("DEX", 0.0);
        cape.put("INT", 0.0);
        cape.put("WIS", 0.0);
        cape.put("LCK", 0.0);

        this.user = user;
        this.world = world;
    }

    public void sendStatChanges(Stats stat, Set<AuraEffects> effects) {
        JSONObject stu = new JSONObject();
        JSONObject sta = new JSONObject();

        for (AuraEffects ae : effects)
            if (ae.getStat().equals("tha"))
                sta.put("$tha", haste);
            else if (ae.getStat().equals("tdo"))
                sta.put("$tdo", evasion);
            else if (ae.getStat().equals("thi"))
                sta.put("$thi", hit);
            else if (ae.getStat().equals("tcr"))
                sta.put("$tcr", criticalHit);

        stu.put("cmd", "stu");
        stu.put("sta", sta);

        this.world.send(stu, this.user);
    }

    public void update() {
        initInnateStats();
        applyCoreStatRatings();
        applyAuraEffects();
        initDamage();
    }

    private void applyAuraEffects() {
        for (AuraEffects ae : this.effects)
            if (ae.getStat().equals("tha"))
                if (ae.getType().equals("+"))
                    haste += ae.getValue();
                else if (ae.getType().equals("-"))
                    haste -= ae.getValue();
                else
                    haste *= ae.getValue();
            else if (ae.getStat().equals("tdo"))
                if (ae.getType().equals("+"))
                    evasion += ae.getValue();
                else if (ae.getType().equals("-"))
                    evasion -= ae.getValue();
                else
                    evasion *= ae.getValue();
            else if (ae.getStat().equals("thi"))
                if (ae.getType().equals("+"))
                    hit += ae.getValue();
                else if (ae.getType().equals("-"))
                    hit -= ae.getValue();
                else
                    hit *= ae.getValue();
            else if (ae.getStat().equals("tcr"))
                if (ae.getType().equals("+"))
                    criticalHit += ae.getValue();
                else if (ae.getType().equals("-"))
                    criticalHit -= ae.getValue();
                else
                    criticalHit *= ae.getValue();
    }

    private void initInnateStats() {
        int level = (Integer) user.properties.get(Users.LEVEL);
        String cat = (String) user.properties.get(Users.CLASS_CATEGORY);

        int innateStat = world.getInnateStats(level);
        List<Double> ratios = Stats.classCatMap.get(cat);

        Set<String> keyEntry = innate.keySet();

        int i = 0;
        for (String key : keyEntry) {
            double stat = Math.round((ratios.get(i) * innateStat));
            innate.put(key, stat);
            i++;
        }
    }

    private void resetValues() {
        _ap = 0;
        attackPower = 0;
        _sp = 0;
        magicPower = 0;
        _tbl = 0;
        _tpa = 0;
        _tdo = 0;
        _tcr = 0;
        _thi = 0;
        _tha = 0;
        _tre = 0;
        block = world.coreValues.get("baseBlock");
        parry = world.coreValues.get("baseParry");
        evasion = world.coreValues.get("baseDodge");
        criticalHit = world.coreValues.get("baseCrit");
        hit = world.coreValues.get("baseHit");
        haste = world.coreValues.get("baseHaste");
        resist = 0; //baseResist
        _cpo = 1;
        _cpi = 1;
        _cao = 1;
        _cai = 1;
        _cmo = 1;
        _cmi = 1;
        _cdo = 1;
        _cdi = 1;
        _cho = 1;
        _chi = 1;
        _cmc = 1;
        $cpo = 1;
        $cpi = 1;
        $cao = 1;
        $cai = 1;
        $cmo = 1;
        $cmi = 1;
        $cdo = 1;
        $cdi = 1;
        $cho = 1;
        $chi = 1;
        $cmc = 1;
        _scm = world.coreValues.get("baseCritValue");
        _sbm = world.coreValues.get("baseBlockValue");
        _srm = world.coreValues.get("baseResistValue");
        _sem = world.coreValues.get("baseEventValue");
        $scm = world.coreValues.get("baseCritValue");
        $sbm = world.coreValues.get("baseBlockValue");
        $srm = world.coreValues.get("baseResistValue");
        $sem = world.coreValues.get("baseEventValue");
        _shb = 0;
        _smb = 0;
        $shb = 0;
        $smb = 0;
    }

    private void applyCoreStatRatings() {
        String cat = (String) user.properties.get(Users.CLASS_CATEGORY);
        Enhancement enhancement = (Enhancement) user.properties.get(Users.ITEM_WEAPON_ENHANCEMENT);
        int level = (Integer) user.properties.get(Users.LEVEL);

        double wLvl = enhancement != null ? enhancement.getLevel() : 1;
        double iDPS = enhancement != null ? enhancement.getDPS() : 100;
        iDPS = iDPS == 0 ? 100 : iDPS;
        iDPS = (iDPS / 100);

        double intAPtoDPS = world.coreValues.get("intAPtoDPS").intValue();
        double PCDPSMod = world.coreValues.get("PCDPSMod");
        //int intSPtoDPS = world.coreValues.get("intSPtoDPS").intValue();

        double hpTgt = world.getBaseHPByLevel(level);
        double TTD = 20;
        double tDPS = ((hpTgt / 20) * 0.7);
        double sp1pc = ((2.25 * tDPS) / (100 / intAPtoDPS) / 2);

        resetValues();

        Set<String> keyEntry = innate.keySet();
        for (String key : keyEntry) {
            double val = (innate.get(key) + armor.get(key) + weapon.get(key) + helm.get(key) + cape.get(key));

            if (key.equals("STR")) {
                double bias1 = sp1pc;
                if (cat.equals("M1"))
                    $sbm -= (((val / bias1) / 100) * 0.3);
                if (cat.equals("S1"))
                    attackPower += Math.round((val * 1.4));
                else
                    attackPower += (val * 2);
                if (cat.equals("M1") || cat.equals("M2") || cat.equals("M3") || cat.equals("M4") || cat.equals("S1"))
                    if (cat.equals("M4"))
                        criticalHit += (((val / bias1) / 100) * 0.7);
                    else
                        criticalHit += (((val / bias1) / 100) * 0.4);
            } else if (key.equals("INT")) {
                double bias1 = sp1pc;
                $cmi -= ((val / bias1) / 100);
                if (cat.substring(0, 1).equals("C") || cat.equals("M3"))
                    $cmo += ((val / bias1) / 100);
                if (cat.equals("S1"))
                    magicPower += Math.round((val * 1.4));
                else
                    magicPower += (val * 2);
                if (cat.equals("C1") || cat.equals("C2") || cat.equals("C3") || cat.equals("M3") || cat.equals("S1"))
                    if (cat.equals("C2"))
                        haste += (((val / bias1) / 100) * 0.5);
                    else
                        haste += (((val / bias1) / 100) * 0.3);
            } else if (key.equals("DEX")) {
                double bias1 = sp1pc;
                if (cat.equals("M1") || cat.equals("M2") || cat.equals("M3") || cat.equals("M4") || cat.equals("S1")) {
                    if (!cat.substring(0, 1).equals("C"))
                        hit += (((val / bias1) / 100) * 0.2);
                    if (cat.equals("M2") || cat.equals("M4"))
                        haste += (((val / bias1) / 100) * 0.5);
                    else
                        haste += (((val / bias1) / 100) * 0.3);
                    if (cat.equals("M1") && _tbl > 0.01)
                        block += (((val / bias1) / 100) * 0.5);
                }
                if (!cat.equals("M2") && !cat.equals("M3"))
                    evasion += (((val / bias1) / 100) * 0.3);
                else
                    evasion += (((val / bias1) / 100) * 0.5);
            } else if (key.equals("WIS")) {
                double bias1 = sp1pc;
                if (cat.equals("C1") || cat.equals("C2") || cat.equals("C3") || cat.equals("S1")) {
                    if (cat.equals("C1"))
                        criticalHit += (((val / bias1) / 100) * 0.7);
                    else
                        criticalHit += (((val / bias1) / 100) * 0.4);
                    hit += (((val / bias1) / 100) * 0.2);
                }
                evasion += (((val / bias1) / 100) * 0.3);
            } else if (key.equals("LCK")) {
                double bias1 = sp1pc;
                $sem += (((val / bias1) / 100) * 2);
                if (cat.equals("S1")) {
                    attackPower += Math.round(val * 1);
                    magicPower += Math.round(val * 1);
                    criticalHit += (((val / bias1) / 100) * 0.3);
                    hit += (((val / bias1) / 100) * 0.1);
                    haste += (((val / bias1) / 100) * 0.3);
                    evasion += (((val / bias1) / 100) * 0.25);
                    $scm += (((val / bias1) / 100) * 2.5);
                } else {
                    if (cat.equals("M1") || cat.equals("M2") || cat.equals("M3") || cat.equals("M4"))
                        attackPower += Math.round((val * 0.7));
                    if (cat.equals("C1") || cat.equals("C2") || cat.equals("C3") || cat.equals("M3"))
                        magicPower += Math.round((val * 0.7));
                    criticalHit += (((val / bias1) / 100) * 0.2);
                    hit += (((val / bias1) / 100) * 0.1);
                    haste += (((val / bias1) / 100) * 0.1);
                    evasion += (((val / bias1) / 100) * 0.1);
                    $scm += (((val / bias1) / 100) * 5);
                }
            }
        }
        wDPS = (int) (Math.round((((world.getBaseHPByLevel((int) wLvl) / TTD) * iDPS) * PCDPSMod)) + Math.round((attackPower / intAPtoDPS)));
        //mDPS = (int) (Math.round((((world.getBaseHPByLevel(wLvl) / TTD) * iDPS) * PCDPSMod)) + Math.round(($sp / intSPtoDPS)));
    }

    private void initDamage() {
        //Calculate Damage

        Map<String, Integer> userSkills = (Map<String, Integer>) user.properties.get(Users.SKILLS);
        Item weaponItem = (Item) user.properties.get(Users.ITEM_WEAPON);

        if (userSkills != null && weaponItem != null) {
            Skill autoAttack = world.skills.get(userSkills.get("aa"));

            double wSPD = 2;
            double wDMG = (wDPS * wSPD);
            double wepRng = weaponItem.getRange();
            double iRNG = (wepRng / 100);
            double tDMG = (wDMG * autoAttack.getDamage());
            minDmg = (int) Math.floor((tDMG - (tDMG * iRNG)));
            maxDmg = (int) Math.ceil((tDMG + (tDMG * iRNG)));
            SmartFoxServer.log.fine("wSPD : " + wSPD);
            SmartFoxServer.log.fine("wDMG : " + wDMG);
            SmartFoxServer.log.fine("iRNG : " + iRNG);
            SmartFoxServer.log.fine("tDMG : " + tDMG);
        }
    }

    public int get$DEX() {
        return (int) (weapon.get("DEX") + armor.get("DEX") + helm.get("DEX") + cape.get("DEX"));
    }

    public int get$END() {
        return (int) (weapon.get("END") + armor.get("END") + helm.get("END") + cape.get("END"));
    }

    public int get$INT() {
        return (int) (weapon.get("INT") + armor.get("INT") + helm.get("INT") + cape.get("INT"));
    }

    public int get$LCK() {
        return (int) (weapon.get("LCK") + armor.get("LCK") + helm.get("LCK") + cape.get("LCK"));
    }

    public int get$STR() {
        return (int) (weapon.get("STR") + armor.get("STR") + helm.get("STR") + cape.get("STR"));
    }

    public int get$WIS() {
        return (int) (weapon.get("WIS") + armor.get("WIS") + helm.get("WIS") + cape.get("WIS"));
    }

    public double get$ap() {
        return attackPower;
    }

    public double get$cai() {
        return $cai;
    }

    public double get$cao() {
        return $cao;
    }

    public double get$cdi() {
        return $cdi;
    }

    public double get$cdo() {
        return $cdo;
    }

    public double get$chi() {
        return $chi;
    }

    public double get$cho() {
        return $cho;
    }

    public double get$cmc() {
        return $cmc;
    }

    public double get$cmi() {
        return $cmi;
    }

    public double get$cmo() {
        return $cmo;
    }

    public double get$cpi() {
        return $cpi;
    }

    public double get$cpo() {
        return $cpo;
    }

    public double get$sbm() {
        return $sbm;
    }

    public double get$scm() {
        return $scm;
    }

    public double get$sem() {
        return $sem;
    }

    public double get$shb() {
        return $shb;
    }

    public double get$smb() {
        return $smb;
    }

    public double get$sp() {
        return magicPower;
    }

    public double get$srm() {
        return $srm;
    }

    public double get$tbl() {
        return block;
    }

    public double get$tcr() {
        return criticalHit;
    }

    public double get$tdo() {
        return evasion;
    }

    public double get$tha() {
        return haste;
    }

    public double get$thi() {
        return hit;
    }

    public double get$tpa() {
        return parry;
    }

    public double get$tre() {
        return resist;
    }

    public double get_DEX() {
        return innate.get("DEX");
    }

    public double get_END() {
        return innate.get("END");
    }

    public double get_INT() {
        return innate.get("INT");
    }

    public double get_LCK() {
        return innate.get("LCK");
    }

    public double get_STR() {
        return innate.get("STR");
    }

    public double get_WIS() {
        return innate.get("WIS");
    }

    public double get_ap() {
        return _ap;
    }

    public double get_cai() {
        return _cai;
    }

    public double get_cao() {
        return _cao;
    }

    public double get_cdi() {
        return _cdi;
    }

    public double get_cdo() {
        return _cdo;
    }

    public double get_chi() {
        return _chi;
    }

    public double get_cho() {
        return _cho;
    }

    public double get_cmc() {
        return _cmc;
    }

    public double get_cmi() {
        return _cmi;
    }

    public double get_cmo() {
        return _cmo;
    }

    public double get_cpi() {
        return _cpi;
    }

    public double get_cpo() {
        return _cpo;
    }

    public double get_sbm() {
        return _sbm;
    }

    public double get_scm() {
        return _scm;
    }

    public double get_sem() {
        return _sem;
    }

    public double get_shb() {
        return _shb;
    }

    public double get_smb() {
        return _smb;
    }

    public double get_sp() {
        return _sp;
    }

    public double get_srm() {
        return _srm;
    }

    public double get_tbl() {
        return _tbl;
    }

    public double get_tcr() {
        return _tcr;
    }

    public double get_tdo() {
        return _tdo;
    }

    public double get_tha() {
        return _tha;
    }

    public double get_thi() {
        return _thi;
    }

    public double get_tpa() {
        return _tpa;
    }

    public double get_tre() {
        return _tre;
    }

    public int getMinDmg() {
        return minDmg;
    }

    public int getMaxDmg() {
        return maxDmg;
    }
}

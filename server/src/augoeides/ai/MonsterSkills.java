/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */

package augoeides.ai;

import augoeides.db.objects.Skill;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Mystical
 */
public class MonsterSkills {

    private Map<Skill, Long> skills;
    
    public MonsterSkills() {
        skills = new HashMap<Skill, Long>();
    }
    
    public void addSkill(Skill skill) {
        skills.put(skill, System.currentTimeMillis());
    }
    
    public Skill getSkill() {
        for(Map.Entry<Skill, Long> entry : this.skills.entrySet()) {
            Skill skill = entry.getKey();
            long cooldown = (entry.getValue() - System.currentTimeMillis());
            
            if(cooldown <= 0) {
                entry.setValue(System.currentTimeMillis());
                return skill;
            }
        }
        
        return null;
    }
}

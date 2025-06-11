/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.requests;

import augoeides.db.objects.Aura;
import augoeides.db.objects.AuraEffects;
import augoeides.db.objects.Skill;
import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.Users;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.Arrays;
import java.util.Map;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class GetPotionEffect implements IRequest {

    @Override
    public void process(String[] params, User user, World world, Room room) throws RequestException {
        String ref = params[0];
        int skillId = Integer.parseInt(params[1]);

        Skill skill = world.skills.get(skillId);

        if (skill != null) {
            JSONObject seia = new JSONObject();
            JSONObject o = new JSONObject();

            o.put("id", skillId);
            o.put("ref", ref);
            o.put("nam", skill.getName());
            o.put("anim", skill.getAnimation());
            o.put("mp", skill.getMana());
            o.put("desc", skill.getDescription());
            o.put("range", skill.getRange());
            o.put("fx", skill.getEffects());
            o.put("tgt", skill.getTarget());
            o.put("typ", skill.getType());
            o.put("strl", skill.getStrl());
            o.put("cd", skill.getCooldown());

            if (skill.hasAuraId()) {
                JSONArray auras = new JSONArray();
                Aura aura = world.auras.get(skill.getAuraId());

                if (!aura.effects.isEmpty()) {

                    JSONObject auraInfo = new JSONObject();
                    JSONArray effects = new JSONArray();

                    for (int effectId : aura.effects) {
                        AuraEffects ae = world.effects.get(effectId);

                        JSONObject effect = new JSONObject();

                        effect.put("typ", ae.getType());
                        effect.put("sta", ae.getStat());
                        effect.put("id", ae.getId());
                        effect.put("val", ae.getValue());

                        effects.add(effect);
                    }

                    if (!effects.isEmpty())
                        auraInfo.put("e", effects);
                    if (aura.getDuration() > 0)
                        auraInfo.put("t", "s");

                    auraInfo.put("nam", aura.getName());

                    auras.add(auraInfo);
                }
                o.put("auras", auras);
            }

            seia.put("cmd", seia);
            seia.put("o", o);

            world.send(seia, user);

            Map<String, Integer> skills = (Map<String, Integer>) user.properties.get(Users.SKILLS);
            skills.put(skill.getReference(), skillId);
        } else
            throw new RequestException("Potion Info not found!");
    }

}

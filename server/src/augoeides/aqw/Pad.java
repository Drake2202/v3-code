/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.aqw;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Mystical
 */
public class Pad {

    /*
     Pad1: Left
     Pad2: Top1
     Pad3: Top2
     Pad4: Top3
     Pad5: Right
     Pad6: Bottom3
     Pad7: Bottom2
     Pad8: Bottom1
     */
    private static final Map<String, String> pads;
    private static final Map<String, String> pairs;

    static {
        pads = new HashMap<String, String>();

        pads.put("Pad1", "Right"); //Check
        pads.put("Pad2", "Down1"); //to be confirmed
        pads.put("Pad3", "Down1"); //check
        pads.put("Pad4", "Down2"); //check
        pads.put("Pad5", "Left"); //Check
        pads.put("Pad6", "Top1");  //check
        pads.put("Pad7", "Top3"); //check
        pads.put("Pad8", "Top2"); //check

        pairs = new HashMap<String, String>();

        pairs.put("Pad3", "Pad7"); //Check
        pairs.put("Pad5", "Pad1"); //to be confirmed
        pairs.put("Pad2", "Pad6"); //check
        pairs.put("Pad1", "Pad5"); //check
        pairs.put("Pad7", "Pad3"); //Check
        pairs.put("Pad6", "Pad2");  //check
        pairs.put("Pad4", "Pad8"); //check
        pairs.put("Pad8", "Pad4"); //check
    }

    public static String getPair(String pad) {
        return pairs.get(pad);
    }

    public static String getPad(String pad) {
        return pads.get(pad);
    }

    private Pad() {
        throw new UnsupportedOperationException("not allowed to have an instance of this class");
    }
}

/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.aqw;

/**
 *
 * @author Mystical
 */
public class Health {

    private static final int[] arrHP = new int[100];

    static {
        double intLevelCap = 100;
        double intHPBase3 = 550;
        double intHPConst3 = 20000;
        double intScaling3 = 1.3;
        int i = 0;
        while (i < intLevelCap) {
            arrHP[i] = (int) Math.round((intHPBase3 + (Math.pow((i / intLevelCap), intScaling3) * intHPConst3)));
            i++;
        }
    }

    public static int getHealthByLevel(int level) {
        return arrHP[(level - 1)];
    }

    private Health() {
        throw new UnsupportedOperationException("not allowed to have an instance of this class");
    }
}

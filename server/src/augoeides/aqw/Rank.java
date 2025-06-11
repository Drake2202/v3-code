/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.aqw;

/**
 *
 * @author Mystical
 */
public class Rank {

    private static final int[] arrRanks;

    private Rank() {
        throw new UnsupportedOperationException("not allowed to have an instance of this class");
    }

    static {
        //Init arrRanks
        arrRanks = new int[10];
        //Init Rep
        int i = 1;
        while (i < arrRanks.length) {
            int rankExp = (int) (Math.pow((i + 1), 3) * 100);
            if (i > 1)
                arrRanks[i] = (rankExp + arrRanks[(i - 1)]);
            else
                arrRanks[i] = (rankExp + 100);
            i++;
        }
    }

    public static int getRankFromPoints(int cp) {
        int i = 1;
        while (i < arrRanks.length) {
            if (cp < arrRanks[i])
                return i;
            i++;
        }

        return 10;
    }
}

/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.aqw;

/**
 *
 * @author Mystical
 */
public class Achievement {

    private Achievement() {
        throw new UnsupportedOperationException("not allowed to have an instance of this class");
    }

    public static int get(int value, int index) {
        if ((index < 0) || (index > 31))
            return -1;

        return (((value & (int) Math.pow(2, index)) == 0) ? 0 : 1);
    }

    public static int update(int valueToSet, int index, int value) {
        int newValue = 0;
        if (value == 0)
            newValue = (valueToSet & ~(int) (Math.pow(2, index)));
        else if (value == 1)
            newValue = (valueToSet | (int) Math.pow(2, index));
        return newValue;
    }

}

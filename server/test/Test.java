/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */

/**
 *
 * @author Mystical
 */
public class Test {

    public static void main(String[] args) {
        //System.out.println(Integer.toHexString(6697932 & 0xffffff).toUpperCase());

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO shops_items (ShopID, ItemID, QuantityRemain) VALUES ");
        for (int i = 2009445; i <= 2009458; i++)
            sb.append(String.format(("(672, %d, 0),"), i));
        System.out.println(sb.toString());
    }
}

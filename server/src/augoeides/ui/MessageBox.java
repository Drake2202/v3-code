/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.ui;

import javax.swing.JOptionPane;

/**
 *
 * @author Mystical
 */
public class MessageBox {

    public static void showMessage(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static int showConfirm(String message, String title, int type) {
        return JOptionPane.showConfirmDialog(null, message, title, type);
    }

    private MessageBox() {
    }
}

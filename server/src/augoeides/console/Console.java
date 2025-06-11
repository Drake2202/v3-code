/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.console;

import augoeides.world.World;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.extensions.ExtensionHelper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.logging.Level;

/**
 *
 * @author Mystical
 */
public class Console implements Runnable {

    private World world;
    private Thread thread;
    private ExtensionHelper helper;
    private boolean running;

    public Console() {
        this.thread = new Thread(this);
        this.thread.setDaemon(false);
    }

    public Console(World world, ExtensionHelper helper) {
        this.world = world;
        this.helper = helper;

    }

    public void start() {
        if (this.world != null || this.helper != null) {
            this.running = true;
            this.thread.start();
        } else
            throw new UnsupportedOperationException("World and Helper must be set first.");
    }

    public void stop() {
        this.running = false;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, Charset.forName("UTF-8")));

            System.out.print("\nconsole > ");

            while (this.running) {
                String cmd = reader.readLine();
                if (cmd.equals("help") || cmd.equals("?")) {
                    System.out.println("log (all,severe,warning,info,fine,finest) - logs for messages depending on given type");
                    System.out.println("msg (message) - broadcast a message throughout the server");
                    System.out.println("restart - restarts the server");
                } else if (cmd.startsWith("log"))
                    try {
                        cmd = cmd.length() == 3 ? "log all" : cmd;
                        SmartFoxServer.log.setLevel(Level.parse(cmd.substring(4).toUpperCase()));
                        System.out.println("Press ENTER to exit. Now logging for " + cmd.substring(4).toUpperCase() + " messages:\n\n");
                        reader.readLine();
                    } catch (IllegalArgumentException iea) {
                        System.out.println(iea.getMessage());
                    } finally {
                        SmartFoxServer.log.setLevel(Level.SEVERE);
                    }
                else if (cmd.startsWith("msg")) {
                    System.out.println("Entered broadcast mode. Type 'quit' to exit.");
                    System.out.print("\nmessage > ");
                    while (!cmd.equalsIgnoreCase("quit")) {
                        cmd = reader.readLine();
                        if (!cmd.equalsIgnoreCase("quit")) {
                            this.world.send(new String[]{"administrator", cmd}, this.world.zone.getChannelList());
                            System.out.print("message > ");
                        } else
                            System.out.println();
                    }
                } else if (cmd.equals("restart")) {
                    this.world.send(new String[]{"logoutWarning", "", "60"}, this.world.zone.getChannelList());
                    this.helper.rebootServer();
                } else
                    System.out.println("Unknown command, type 'help' or '?' for a full list of commands.");

                System.out.print("console > ");
            }
        } catch (IOException ex) {
            SmartFoxServer.log.severe("Error in console: " + ex.getMessage());
        }
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public void setHelper(ExtensionHelper helper) {
        this.helper = helper;
    }

}

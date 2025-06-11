/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.config;

import it.gotoandplay.smartfoxserver.SmartFoxServer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author Mystical
 */
public class ConfigData {

    public static final String DB_HOST;
    public static final String DB_NAME;
    public static final String DB_USERNAME;
    public static final String DB_PASSWORD;
    public static final int DB_PORT;
    public static final int DB_MAX_CONNECTIONS;

    public static final String SERVER_NAME;
    public static final boolean STAFF_ONLY;

    public static final long ANTI_MESSAGEFLOOD_MIN_MSG_TIME;
    public static final int ANTI_MESSAGEFLOOD_TOLERANCE;
    public static final int ANTI_MESSAGEFLOOD_MAX_REPEATED;
    public static final int ANTI_MESSAGEFLOOD_WARNINGS;

    public static final long ANTI_REQUESTFLOOD_MIN_MSG_TIME;
    public static final int ANTI_REQUESTFLOOD_TOLERANCE;
    public static final int ANTI_REQUESTFLOOD_MAX_REPEATED;
    public static final int ANTI_REQUESTFLOOD_WARNINGS;

    public static final boolean ANTI_REQUESTFLOOD_REPEAT_ENABLED;

    public static final Map<String, String> REQUESTS;
    public static final Set<String> ANTI_REQUESTFLOOD_EXCEPTIONS;

    private ConfigData() {
        throw new UnsupportedOperationException("not allowed to have an instance of this class");
    }

    static {
        try {
            String confName = System.getProperty("augoeides.config") != null ? System.getProperty("augoeides.config") : "AugoEidEs.conf";
            Properties config = new Properties();
            String curDir = (new File(".").getCanonicalPath());
            File dir = new File(curDir + File.separatorChar + "conf" + File.separatorChar);
            if (!dir.exists() && !dir.mkdir())
                throw new RuntimeException("Unable to create directory.");

            String filePath = curDir + File.separatorChar + "conf" + File.separatorChar + confName;
            File conf = new File(filePath);
            if (!conf.exists())
                if (conf.createNewFile()) {
                    Writer writer = null;
                    try {
                        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "8859_1"));
                        config.setProperty("server.name", "AugoEidEs");
                        config.setProperty("server.staffonly", "false");
                        config.setProperty("database.host", "127.0.0.1");
                        config.setProperty("database.port", "3306");
                        config.setProperty("database.connections.max", "50");
                        config.setProperty("database.user", "root");
                        config.setProperty("database.pass", "");
                        config.setProperty("database.name", "mextv3");
                        config.setProperty("antiflood.message.tolerance", "5");
                        config.setProperty("antiflood.message.maxrepeated", "3");
                        config.setProperty("antiflood.message.warnings", "2");
                        config.setProperty("antiflood.message.minimumtime", "1000");
                        config.setProperty("antiflood.request.tolerance", "5");
                        config.setProperty("antiflood.request.maxrepeated", "3");
                        config.setProperty("antiflood.request.enablerepeatfilter", "false");
                        config.setProperty("antiflood.request.warnings", "2");
                        config.setProperty("antiflood.request.minimumtime", "1000");
                        config.store(writer, "AugoEidEs Configuration");
                    } catch (IOException ieo) {
                        SmartFoxServer.log.severe("Error in writing configuration: " + ieo.getMessage());
                    } finally {
                        try {
                            if (writer != null)
                                writer.close();
                        } catch (IOException ieo) {
                            SmartFoxServer.log.severe("Error in closing write stream: " + ieo.getMessage());
                        }
                    }
                }

            InputStream fin = null;
            try {
                fin = new FileInputStream(filePath);
                config.load(fin);
            } finally {
                try {
                    if (fin != null)
                        fin.close();
                } catch (IOException ieo) {
                    SmartFoxServer.log.severe("Error in closing input stream: " + ieo.getMessage());
                }
            }

            DB_HOST = config.getProperty("database.host");
            DB_USERNAME = config.getProperty("database.user");
            DB_PASSWORD = config.getProperty("database.pass");
            DB_NAME = config.getProperty("database.name");
            DB_PORT = Integer.parseInt(config.getProperty("database.port"));
            DB_MAX_CONNECTIONS = Integer.parseInt(config.getProperty("database.connections.max"));
            SERVER_NAME = config.getProperty("server.name");
            STAFF_ONLY = Boolean.parseBoolean(config.getProperty("server.staffonly"));
            ANTI_MESSAGEFLOOD_MIN_MSG_TIME = Long.parseLong(config.getProperty("antiflood.message.minimumtime"));
            ANTI_MESSAGEFLOOD_TOLERANCE = Integer.parseInt(config.getProperty("antiflood.message.tolerance"));
            ANTI_MESSAGEFLOOD_MAX_REPEATED = Integer.parseInt(config.getProperty("antiflood.message.maxrepeated"));
            ANTI_MESSAGEFLOOD_WARNINGS = Integer.parseInt(config.getProperty("antiflood.message.warnings"));
            ANTI_REQUESTFLOOD_MIN_MSG_TIME = Long.parseLong(config.getProperty("antiflood.request.minimumtime"));
            ANTI_REQUESTFLOOD_TOLERANCE = Integer.parseInt(config.getProperty("antiflood.request.tolerance"));
            ANTI_REQUESTFLOOD_MAX_REPEATED = Integer.parseInt(config.getProperty("antiflood.request.maxrepeated"));
            ANTI_REQUESTFLOOD_WARNINGS = Integer.parseInt(config.getProperty("antiflood.request.warnings"));
            ANTI_REQUESTFLOOD_REPEAT_ENABLED = Boolean.parseBoolean(config.getProperty("antiflood.request.enablerepeatfilter"));

            Set<String> filters = new HashSet<String>();
            //iterate through filters property
            for (int i = 1; i <= 20; i++)
                if (config.getProperty("antiflood.request.exceptions." + i) != null)
                    filters.add(config.getProperty("antiflood.request.exceptions." + i));

            Map<String, String> requests = new HashMap<String, String>();

            //iterate through requests property
            for (int i = 1; i <= 100; i++)
                if (config.getProperty("handler.requests." + i) != null) {
                    String request = config.getProperty("handler.requests." + i);
                    String[] requestProp = request.split("=");

                    requests.put(requestProp[0], requestProp[1]);
                }

            REQUESTS = requests;
            ANTI_REQUESTFLOOD_EXCEPTIONS = filters;

        } catch (IOException e) {
            SmartFoxServer.log.severe("Error in loading configuration: " + e.getMessage());
        }
    }

}

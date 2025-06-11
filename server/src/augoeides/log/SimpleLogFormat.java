/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.log;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 *
 * @author Mystical
 */
public class SimpleLogFormat extends Formatter {

    public SimpleLogFormat() {
        super();
    }

    private static final String nl = System.getProperty("line.separator");

    @Override
    public String format(LogRecord record) {
        String s = "[ " + record.getLevel() + " ][ " + formatLocation(record) + " ] " + record.getMessage() + nl;

        Throwable t = record.getThrown();

        if (t == null)
            return s;
        StackTraceElement[] elements = t.getStackTrace();
        StringBuilder sb = new StringBuilder(s);
        sb.append(" ").append(t.toString()).append(nl);

        for (StackTraceElement element : elements)
            sb.append("\t").append(element.toString()).append(nl);

        return sb.toString();
    }

    private String formatLocation(LogRecord record) {
        String className = record.getSourceClassName();
        int idx = className.lastIndexOf(".");
        if (idx != -1)
            className = className.substring(idx + 1);
        return className + "." + record.getSourceMethodName();
    }
}

/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.db;

import it.gotoandplay.smartfoxserver.db.DbManager;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 *
 * @author Mystical
 */
@Deprecated
public class SFSDataSource implements DataSource {

    private final DbManager db;

    public SFSDataSource(DbManager db) {
        this.db = db;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return db.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return db.getConnection();
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {

    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {

    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object unwrap(Class iface) throws SQLException {
        if (!DataSource.class.equals(iface))
            throw new SQLException("DataSource of type [" + getClass().getName()
                    + "] can only be unwrapped as [javax.sql.DataSource], not as [" + iface.getName());
        return this;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return DataSource.class.equals(iface);
    }

}

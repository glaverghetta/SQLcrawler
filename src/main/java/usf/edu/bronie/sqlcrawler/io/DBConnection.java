package usf.edu.bronie.sqlcrawler.io;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import usf.edu.bronie.sqlcrawler.constants.CredentialConstants;

public class DBConnection {

    private static HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;

    static {
        config.setJdbcUrl( CredentialConstants.DB_URL );
        config.setUsername( CredentialConstants.DB_USER );
        config.setPassword( CredentialConstants.DB_PASS );
        config.addDataSourceProperty( "cachePrepStmts" , "true" );
        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );

        ds = new HikariDataSource( config );
    }

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}

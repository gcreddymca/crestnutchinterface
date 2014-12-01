package com.hm.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DBConnection {

    private static String JDBC_CONNECTION_URL = "jdbc:oracle:thin:@localhost:1521:XE";
    private static String JDBC_USERNAME = "CREST";
    private static String JDBC_PASSWORD = "CREST";
    private final static Logger logger = Logger.getLogger(DBConnection.class.getName());

   /** This method will provide the database connection for mysql database */            
   public static Connection getConnection() {
        Connection con = null;
        try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
            con = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_USERNAME, JDBC_PASSWORD);
        } catch (ClassNotFoundException e) {
            logger.severe(e.getMessage());
        } catch (SQLException e) {
            logger.severe(e.getMessage());
        }
        return con;
   }
}
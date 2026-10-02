package com.opportunityguard.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** JDBC connection factory. Every repository opens and closes its own connection (try-with-resources). */
public final class Db {
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("MySQL Connector/J is missing from the classpath: " + e);
        }
    }

    private Db() {}

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(Config.dbUrl(), Config.dbUser(), Config.dbPassword());
    }
}

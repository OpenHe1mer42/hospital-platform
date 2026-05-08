package carely.service;

import carely.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseService {
    private static Connection connection = null;

    private DatabaseService() {};

    public static Connection initConnection() {
        try {
            return DatabaseConfig.getConnection();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static Connection getConnection() {
        if(connection == null) {
            connection = initConnection();
        }
        return connection;
    }
}

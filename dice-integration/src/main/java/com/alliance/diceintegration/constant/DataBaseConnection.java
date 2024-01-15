package com.alliance.diceintegration.constant;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.stereotype.Component;

import com.alliance.diceintegration.utility.SystemParam;


@ConstructorBinding
@Component
public class DataBaseConnection {

    public static DataBaseConnection connection = null;

    public Connection connectionDBOBDB;

    public static DataBaseConnection getInstance() {

        if (connection == null) {
            connection = new DataBaseConnection();
            try {
                connection.init();
            } catch (ClassNotFoundException e) {
                System.out.println("Database issue : " + e.getMessage());
            }
        }
        return connection;
    }

    public void init() throws ClassNotFoundException {
        String url = SystemParam.getInstance().getDBOSConnectionString();
        String user = SystemParam.getInstance().getDBOSUsername();
        String password = SystemParam.getInstance().getDBOSPassword();

        Connection connection = null;

        try {
            System.out.println("url : " + url);
            // System.out.println("user : " + user);
            // System.out.println("password : " + password);
            Class.forName(SystemParam.getInstance().getDB2className());
            connection = DriverManager.getConnection(url, user, password);

        } catch (ClassNotFoundException e) {
            System.out.println("Database issue : " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database issue : " + e.getMessage());
        } finally {
            if (connection != null) {
                System.out.println("//----Connected to DBOBDB----//");
                connectionDBOBDB = connection;
            }
        }

    }

    public Connection getConnection() {
        return connectionDBOBDB;
    }

    public static void setConnectionNull() {
        DataBaseConnection.connection = null;
    }

    public Connection getConnectionDBOBDB() {
        return connectionDBOBDB;
    }

    public void setConnectionDBOBDB(Connection connectionDBOBDB) {
        this.connectionDBOBDB = connectionDBOBDB;
    }

}
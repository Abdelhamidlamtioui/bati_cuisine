package main.java.config;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:postgresql://localhost:5432/bati_cuisine";
    private static final String USER = "BatiCuisine";
    private static final String PASSWORD = "1234";


    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);



        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC Driver not found.", e);
        } catch (SQLException e) {
            throw new RuntimeException("Connection to PostgreSQL database failed.", e);
        }

        return connection;
    }
}
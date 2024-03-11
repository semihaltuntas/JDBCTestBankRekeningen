package be.vdab.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class AbstractRepository {
    private static final String URL = "jdbc:mysql://localhost/bank";
    private static final String USER = "root";
    private static final String PASSWORD = "Polatalemdar571632";

    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

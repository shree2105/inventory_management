package org.inventory.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class dbconfig {

    private static final String ADMIN_URL = "jdbc:mysql://localhost:3306/?serverTimezone=UTC";
    private static final String URL = "jdbc:mysql://localhost:3306/inventory_db?serverTimezone=UTC&useSSL=false";
    private static final String USER = "root"; // your MySQL username
    private static final String PASSWORD = "Shree@21"; // your MySQL password

    // Create the database if not exists
    public static void createDatabase() {
        try (Connection conn = DriverManager.getConnection(ADMIN_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            String createDB = "CREATE DATABASE IF NOT EXISTS inventorydb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
            stmt.executeUpdate(createDB);
            System.out.println("database created successfully.");

        } catch (SQLException e) {
            System.err.println("Failed to create database: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // Get connection to inventorydb
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Create tables according to the final schema
    public static void initializeTables() {
        try (Connection con = getConnection(); Statement st = con.createStatement()) {
            String createInventoryStock = """
                CREATE TABLE IF NOT EXISTS inventorystock (
                    product_id INT AUTO_INCREMENT PRIMARY KEY,
                    product_name VARCHAR(100),
                     model VARCHAR(100),
                    price_per_quantity DOUBLE,
                    unit INT,
                    total_price DOUBLE,
                    status VARCHAR(20),
                    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                ) ENGINE=InnoDB
                """;

            String createStaff = """
                CREATE TABLE IF NOT EXISTS staff (
                    staff_id INT AUTO_INCREMENT PRIMARY KEY,
                    staff_name VARCHAR(100) NOT NULL,
                    department VARCHAR(100),
                    designation VARCHAR(50),
                    rights VARCHAR(50),
                    email VARCHAR(100) UNIQUE NOT NULL,
                    phone_number VARCHAR(15),
                    password VARCHAR(100) NOT NULL,  
                    status VARCHAR(20),
                    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                ) ENGINE=InnoDB
                """;

            st.executeUpdate(createInventoryStock);
            st.executeUpdate(createStaff);

            System.out.println("Tables inventorystock and staff verified/created successfully.");
        } catch (SQLException e) {
            System.err.println("Failed to initialize tables: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // Main for quick test (creates DB + tables)
    public static void main(String[] args) {
        createDatabase();
        initializeTables();
}
}
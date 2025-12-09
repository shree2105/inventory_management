

package org.inventory.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class InsertDummyData {

    public static void main(String[] args) {
        boolean reset = false;
        if (args != null && args.length > 0) {
            for (String a : args) {
                if ("reset".equalsIgnoreCase(a) || "truncate".equalsIgnoreCase(a)) {
                    reset = true;
                    break;
                }
            }
        }

        try (Connection conn = dbconfig.getConnection()) {
            System.out.println("Connected to DB via dbconfig.");
            conn.setAutoCommit(false);
            try {
                if (reset) {
                    truncateTablesForDev(conn);
                }

                // ---------- INVENTORY DUMMY ROWS ----------
                insertInventoryIfMissing(conn, "Microwave X100", "MX-100", 4999.00, 10, "active");
                insertInventoryIfMissing(conn, "Phone Model A", "PMA-2025", 15999.00, 5, "active");
                insertInventoryIfMissing(conn, "LED TV 42", "LTV-42X", 25999.00, 3, "active");

                // ---------- STAFF DUMMY ROWS ----------
                insertStaffIfMissing(conn, "Raja", "Management", "Manager", "staff", "raja@gmail.com", "9123456789", "Raja@123","ACTIVE");
                insertStaffIfMissing(conn, "Swathi", "Electronics", "Technician", "staff", "swathi@gmail.com", "9123456790", "Swathi@123"    ,"ACTIVE");
                insertStaffIfMissing(conn, "Kumar", "Support", "Support Engineer", "staff", "kumar@gmail.com", "9123456791","Kumar@123" ,"ACTIVE");

                conn.commit();
                System.out.println("✅ Dummy data inserted or already present.");
            } catch (SQLException ex) {
                try { conn.rollback(); } catch (SQLException e) { /* ignore */ }
                System.err.println("❌ Insert failed, rolled back. Reason: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                try { conn.setAutoCommit(true); } catch (SQLException e) { /* ignore */ }
            }

        } catch (SQLException e) {
            System.err.println("DB error (couldn't get connection): " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void truncateTablesForDev(Connection conn) throws SQLException {
        System.out.println("RESET MODE: truncating tables (dev only)...");
        try (Statement stmt = conn.createStatement()) {
            // Disable FK checks temporarily (dangerous if used in prod)
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            stmt.execute("TRUNCATE TABLE staff");
            stmt.execute("TRUNCATE TABLE inventorystock");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
        System.out.println("Truncate complete. AUTO_INCREMENT counters reset.");
    }

    // ---------- INVENTORY TABLE ----------
    private static void insertInventoryIfMissing(Connection conn,
                                                 String productName,
                                                 String model,
                                                 double pricePerQuantity,
                                                 int unit,
                                                 String status) throws SQLException {

        // Check if product already exists (use column names from your DB)
        String checkSql = "SELECT product_id FROM inventorystock WHERE product_name = ?";
        try (PreparedStatement check = conn.prepareStatement(checkSql)) {
            check.setString(1, productName);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    System.out.println("inventorystock: product_name=\"" + productName + "\" already exists — skipping.");
                    return;
                }
            }
        }

        String insertSql = "INSERT INTO inventorystock " +
                "(product_name, model, price_per_quantity, unit, total_price, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(insertSql)) {
            pst.setString(1, productName);
            pst.setString(2, model);
            pst.setDouble(3, pricePerQuantity);
            pst.setInt(4, unit);
            pst.setDouble(5, pricePerQuantity * unit); // compute total_price
            pst.setString(6, status);
            int r = pst.executeUpdate();
            if (r > 0) System.out.println("Inserted inventory: " + productName + " (model=" + model + ")");
        }
    }

    // ---------- STAFF TABLE ----------
    private static void insertStaffIfMissing(Connection conn,
                                             String staffName,
                                             String department,
                                             String designation,
                                             String rights,
                                             String email,
                                             String phoneNumber,
                                             String password,
                                             String status) throws SQLException {

        // Check by email (unique)
        String checkSql = "SELECT staff_id FROM staff WHERE email = ?";
        try (PreparedStatement check = conn.prepareStatement(checkSql)) {
            check.setString(1, email);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    System.out.println("staff: email=" + email + " already exists — skipping.");
                    return;
                }
            }
        }

        // Insert staff record (column names must match your DB)
        String insertSql = "INSERT INTO staff " +
                "(staff_name, department, designation, rights, email, phone_number,password, status) " +
                "VALUES (?, ?, ?, ?, ?, ?,?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(insertSql)) {
            pst.setString(1, staffName);
            pst.setString(2, department);
            pst.setString(3, designation);
            pst.setString(4, rights);
            pst.setString(5, email);
            pst.setString(6, phoneNumber);
            pst.setString(7, password);
            pst.setString(8, status);
            int r = pst.executeUpdate();
            if (r > 0) System.out.println("Inserted staff: " + staffName + " (" + email + ")");
        }
    }
}


















package org.inventory.repository;

import org.inventory.config.dbconfig;
import org.inventory.model.Staff;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class StaffRepository {

    // Insert staff row — NOW includes password
    public Staff insert(Staff s) throws SQLException {
        String sql = "INSERT INTO staff (staff_name, department, designation, rights, email, phone_number, status, password) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbconfig.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pst.setString(1, s.getStaffName());
            pst.setString(2, s.getDepartment());
            pst.setString(3, s.getDesignation());
            pst.setString(4, s.getRights());
            pst.setString(5, s.getEmail());
            pst.setString(6, s.getPhoneNumber());
            pst.setString(7, s.getStatus() == null ? "Active" : s.getStatus());
            pst.setString(8, s.getPassword());

            int affected = pst.executeUpdate();
            if (affected == 0) throw new SQLException("Insert failed, no rows affected.");

            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (keys.next()) {
                    s.setStaffId(keys.getInt(1));
                }
            }

            return findById(s.getStaffId()).orElse(s);
        }
    }

    // Find all staff — now includes password for mapping
    public List<Staff> findAll() throws SQLException {
        String sql = "SELECT staff_id, staff_name, department, designation, rights, email, phone_number, status, password, created_date, updated_date FROM staff";

        List<Staff> list = new ArrayList<>();
        try (Connection conn = dbconfig.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // Find by id — includes password
    public Optional<Staff> findById(int id) throws SQLException {
        String sql = "SELECT staff_id, staff_name, department, designation, rights, email, phone_number, status, password, created_date, updated_date FROM staff WHERE staff_id = ?";

        try (Connection conn = dbconfig.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    // Find by email — includes password
    public Optional<Staff> findByEmail(String email) throws SQLException {
        String sql = "SELECT staff_id, staff_name, department, designation, rights, email, phone_number, status, password, created_date, updated_date FROM staff WHERE email = ?";

        try (Connection conn = dbconfig.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, email);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    // Update staff
    public boolean update(Staff s) throws SQLException {
        String sql = "UPDATE staff SET staff_name = ?, department = ?, designation = ?, rights = ?, email = ?, phone_number = ?, status = ?, password = ? WHERE staff_id = ?";

        try (Connection conn = dbconfig.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, s.getStaffName());
            pst.setString(2, s.getDepartment());
            pst.setString(3, s.getDesignation());
            pst.setString(4, s.getRights());
            pst.setString(5, s.getEmail());
            pst.setString(6, s.getPhoneNumber());
            pst.setString(7, s.getStatus() == null ? "Active" : s.getStatus());
            pst.setString(8, s.getPassword());
            pst.setInt(9, s.getStaffId());

            return pst.executeUpdate() > 0;
        }
    }

    // Delete staff
    public boolean deleteById(int id) throws SQLException {
        String sql = "DELETE FROM staff WHERE staff_id = ?";

        try (Connection conn = dbconfig.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        }
    }

    // Login check
    public Optional<Staff> findByEmailAndPassword(String email, String password) throws SQLException {
        String sql = "SELECT staff_id, staff_name, department, designation, rights, email, phone_number, status, password, created_date, updated_date " +
                "FROM staff WHERE email = ? AND password = ?";

        try (Connection conn = dbconfig.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, email);
            pst.setString(2, password);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }
    // CHECK EMAIL EXISTS
    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM staff WHERE email = ?";
        try (Connection conn = dbconfig.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, email);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    // CHECK PHONE EXISTS
    public boolean existsByPhoneNumber(String phone) throws SQLException {
        String sql = "SELECT COUNT(*) FROM staff WHERE phone_number = ?";
        try (Connection conn = dbconfig.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, phone);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    // FIND BY EMAIL + PHONE (for login)
    public Optional<Staff> findByEmailAndPhoneNumber(String email, String phone) throws SQLException {
        String sql = "SELECT staff_id, staff_name, department, designation, rights, email, phone_number, status, password, created_date, updated_date FROM staff WHERE email = ? AND phone_number = ?";
        try (Connection conn = dbconfig.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, email);
            pst.setString(2, phone);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    // Row mapper
    private Staff mapRow(ResultSet rs) throws SQLException {
        Staff s = new Staff();

        s.setStaffId(rs.getInt("staff_id"));
        s.setStaffName(rs.getString("staff_name"));
        s.setDepartment(rs.getString("department"));
        s.setDesignation(rs.getString("designation"));
        s.setRights(rs.getString("rights"));
        s.setEmail(rs.getString("email"));
        s.setPhoneNumber(rs.getString("phone_number"));
        s.setStatus(rs.getString("status"));
        s.setPassword(rs.getString("password"));

        Timestamp c = rs.getTimestamp("created_date");
        Timestamp u = rs.getTimestamp("updated_date");

        if (c != null) s.setCreatedDate(c.toLocalDateTime());
        if (u != null) s.setUpdatedDate(u.toLocalDateTime());

        return s;
    }
}

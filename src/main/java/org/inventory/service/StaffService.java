package org.inventory.service;

import org.inventory.model.Staff;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface StaffService {
    Staff createStaff(Staff s) throws SQLException;
    List<Staff> getAllStaff() throws SQLException;
    Optional<Staff> getStaffById(int id) throws SQLException;
    Optional<Staff> getStaffByEmail(String email) throws SQLException;
    boolean updateStaff(Staff s) throws SQLException;
    boolean deleteStaff(int id) throws SQLException;
}


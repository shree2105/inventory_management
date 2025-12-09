package org.inventory.service.impl;

import org.inventory.model.Staff;
import org.inventory.repository.StaffRepository;
import org.inventory.service.StaffService;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class StaffServiceImpl implements StaffService {

    private final StaffRepository repo;

    public StaffServiceImpl(StaffRepository repo) {
        this.repo = repo;
    }

    @Override
    public Staff createStaff(Staff s) throws SQLException {
        // check duplicate email
        Optional<Staff> exists = repo.findByEmail(s.getEmail());
        if (exists.isPresent()) {
            throw new SQLException("Staff with email already exists: " + s.getEmail());
        }
        return repo.insert(s);
    }

    @Override
    public List<Staff> getAllStaff() throws SQLException {
        return repo.findAll();
    }

    @Override
    public Optional<Staff> getStaffById(int id) throws SQLException {
        return repo.findById(id);
    }

    @Override
    public Optional<Staff> getStaffByEmail(String email) throws SQLException {
        return repo.findByEmail(email);
    }

    @Override
    public boolean updateStaff(Staff s) throws SQLException {
        return repo.update(s);
    }

    @Override
    public boolean deleteStaff(int id) throws SQLException {
        return repo.deleteById(id);
}
}